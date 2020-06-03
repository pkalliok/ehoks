(ns oph.ehoks.oppija.share-handler
  (:require [compojure.api.sweet :as c-api]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.oppija.schema :as oppija-schema]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.db.db-operations.shared-modules :as db]
            [oph.ehoks.db.postgresql.common :as cdb]
            [oph.ehoks.db.postgresql.hankittavat :as hdb]
            [ring.util.http-response :as response]))

(defn- get-tutkinnonosa-details [type uuid]
  (cond
    (= type "hato")
    (db/select-hankittavat-ammat-tutkinnon-osat-by-module-id uuid)
    (= type "hyto")
    (db/select-hankittavat-yhteiset-tutkinnon-osat-by-module-id uuid)
    (= type "hpto")
    (db/select-hankittavat-paikalliset-tutkinnon-osat-by-module-id uuid)))

(defn- combine-osaamisen-hankkiminen [uuid]
  (let [module (db/select-osaamisen-hankkimistavat-by-module-id uuid)
        tho (hdb/select-tyopaikalla-jarjestettava-koulutus-by-id
              (:tyopaikalla-jarjestettava-koulutus-id module))]
    (assoc
      module
      :muut-oppimisymparistot
      (hdb/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
        (:id module))
      :tyopaikalla_jarjestettava_koulutus tho
      :sovitut-tyotehtavat (hdb/select-tyotehtavat-by-tho-id (:id tho)))))

(defn- combine-osaamisen-osoittaminen [uuid]
  (let [module (db/select-osaamisen-osoittamiset-by-module-id uuid)]
    (assoc
      module
      :sisallot
      (cdb/select-osaamisen-osoittamisen-sisallot-by-osaamisen-osoittaminen-id
        (:id module))
      :koulutuksen-jarjestaja-arvioijat
      (cdb/select-koulutuksen-jarjestaja-osaamisen-arvioijat-by-hon-id
        (:id module))
      :tyoelama-osaamisen-arvioijat
      (cdb/select-tyoelama-osaamisen-arvioijat-by-hon-id
        (:id module))
      :nayttoymparistot (db/select-nayttoymparisto-by-osaamisen-osoittaminen-id
                          (:id module))
      :osa-alueet (cdb/select-osa-alueet-by-osaamisen-osoittaminen
                    (:id module))
      :yksilolliset-kriteerit
      (cdb/select-osaamisen-osoittamisen-kriteerit-by-osaamisen-osoittaminen-id
        (:id module)))))

(defn- get-module-details [type uuid]
  (cond
    (= type "osaamisenhankkiminen")
    (combine-osaamisen-hankkiminen uuid)
    (= type "osaamisenosoittaminen")
    (combine-osaamisen-osoittaminen uuid)))

(defn- fetch-shared-link-data
  "Queries and combines data associated with the shared link"
  [uuid]
  (if-let [jakolinkki (db/select-shared-link uuid)]
    [(assoc (db/select-oppija-opiskeluoikeus-for-shared-link uuid)
            :module (get-module-details
                      (:shared-module-tyyppi jakolinkki)
                      (:shared-module-uuid jakolinkki))
            :tutkinnonosa (get-tutkinnonosa-details
                            (:tutkinnonosa-tyyppi jakolinkki)
                            (:tutkinnonosa-module-uuid jakolinkki)))]))

(def routes
  (c-api/context "/" []
    :tags ["jaot"]

    (c-api/context "/jakolinkit" []

      (c-api/POST "/" [:as request]
        :body [values oppija-schema/JakolinkkiLuonti]
        :return (rest/response schema/POSTResponse :uuid String)
        :summary "Luo uuden jakolinkin"
        (try
          (let [jakolinkki (db/insert-shared-module! values)
                share-id (str (:share_id jakolinkki))]
            (rest/rest-ok
              {:uri (format "%s/%s" (:uri request) share-id)}
              :uuid share-id))
          (catch Exception e
            (response/bad-request! {:error (ex-message e)}))))

      (c-api/GET "/:uuid" []
        :return (rest/response [oppija-schema/Jakolinkki])
        :summary "Jakolinkkiin liitettyjen tietojen haku"
        :path-params [uuid :- String]
        (let [jakolinkki (fetch-shared-link-data uuid)]
          (if (pos? (count jakolinkki))
            (rest/rest-ok jakolinkki)
            (response/not-found))))

      (c-api/DELETE "/:uuid" []
        :summary "Poistaa jakolinkin"
        :path-params [uuid :- String]
        (let [deleted (db/delete-shared-module! uuid)]
          (if (pos? (first deleted))
            (response/ok)
            (response/not-found)))))

    (c-api/GET "/moduulit/:uuid" []
      :return (rest/response [oppija-schema/Jakolinkki])
      :summary "Jaettuun moduuliin liitettyjen jakolinkkien haku"
      :path-params [uuid :- String]
      (let [jakolinkit (db/select-shared-module-links uuid)]
        (if (pos? (count jakolinkit))
          (rest/rest-ok jakolinkit)
          (rest/rest-ok '()))))))
