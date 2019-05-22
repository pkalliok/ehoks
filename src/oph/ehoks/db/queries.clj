(ns oph.ehoks.db.queries
  (:require [clojure.java.io :as io]
            [clojure.string :as cstr]))

(defn read-sql-file [f] (slurp (io/resource f)))

(def select-by-template (read-sql-file "select_by.sql"))

(def select-join-template (read-sql-file "select_join.sql"))

(defn populate-sql [m sql]
  (reduce
    (fn [c [k v]]
      (cstr/replace c (str k) v))
    sql
    m))

(defn generate-select-by [m]
  (populate-sql m select-by-template))

(defn generate-select-join [m]
  (populate-sql m select-join-template))

(defn parse-sql [n]
  (let [[table column] (rest (clojure.string/split
                               (cstr/replace n #"-" "_")
                               #"(_by_)|(select_)"))]
    {:table table :column column}))

(defmacro defq [query-name & filename]
  `(def ~query-name (if (nil? (first (quote ~filename)))
                      (generate-select-by (parse-sql (str (quote ~query-name))))
                      (read-sql-file (cstr/join (quote ~filename))))))

(defq select-hoksit "hoksit/select.sql")
(defq select-hoksit-by-oppija-oid)
(defq select-hoksit-by-id)
(defq select-hoksit-by-opiskeluoikeus-oid)
(defq select-hoksit-by-eid)
(defq select-hoksit-eid-by-eid "hoksit/select_eid.sql")
(defq select-aiemmin-hankitut-ammat-tutkinnon-osat-by-hoks-id)
(def select-hankitun-osaamisen-naytot-by-ooato-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "aiemmin_hankitun_ammat_tutkinnon_osan_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "aiemmin_hankittu_ammat_tutkinnon_osa_id"}))
(def select-osa-alueet-by-hankitun-osaamisen-naytto
  (generate-select-join
    {:table "koodisto_koodit"
     :join "hankitun_osaamisen_nayton_osa_alueet"
     :secondary-column "koodisto_koodi_id"
     :primary-column "id"
     :column "hankitun_osaamisen_naytto_id"}))
(defq select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id)
(defq select-hankittavat-paikalliset-tutkinnon-osat-by-id)
(defq select-aiemmin-hankitut-paikalliset-tutkinnon-osat-by-hoks-id)
(def select-tarkentavat-tiedot-naytto-by-oopto-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "aiemmin_hankitun_paikallisen_tutkinnon_osan_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(def select-arvioijat-by-oopto-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_arvioijat"
     :join "aiemmin_hankitun_paikallisen_tutkinnon_osan_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_arvioija_id"
     :primary-column "id"
     :column "aiemmin_hankittu_paikallinen_tutkinnon_osa_id"}))
(defq select-aiemmin-hankitut-yhteiset-tutkinnon-osat-by-hoks-id)
(def select-hankitun-osaamisen-naytot-by-ppto-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "hankittavan_paikallisen_tutkinnon_osan_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "hankittava_paikallinen_tutkinnon_osa_id"}))
(def select-koulutuksen-jarjestaja-arvioijat-by-hon-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_arvioijat"
     :join "hankitun_osaamisen_nayton_koulutuksen_jarjestaja_arvioija"
     :secondary-column "koulutuksen_jarjestaja_arvioija_id"
     :primary-column "id"
     :column "hankitun_osaamisen_naytto_id"}))
(def select-tyoelama-arvioijat-by-hon-id
  (generate-select-join
    {:table "tyoelama_arvioijat"
     :join "hankitun_osaamisen_nayton_tyoelama_arvioija"
     :secondary-column "tyoelama_arvioija_id"
     :primary-column "id"
     :column "hankitun_osaamisen_naytto_id"}))
(defq select-nayttoymparistot-by-id)
(def select-tyotehtavat-by-hankitun-osaamisen-naytto-id
  (generate-select-by {:table "hankitun_osaamisen_tyotehtavat"
                       :column "hankitun_osaamisen_naytto_id"}))
(def select-osaamisen-hankkmistavat-by-ppto-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "hankittava_paikallinen_tutkinnon_osa_id"}))
(defq select-tyopaikalla-jarjestettavat-koulutukset-by-id)
(def select-henkilot-by-tho-id
  (generate-select-by
    {:table "tyopaikalla_jarjestettavan_koulutuksen_henkilot"
     :column "tyopaikalla_jarjestettava_koulutus_id"}))
(def select-tyotehtavat-by-tho-id
  (generate-select-by
    {:table "tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat"
     :column "tyopaikalla_jarjestettava_koulutus_id"}))
(defq select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id)
(defq select-todennettu-arviointi-lisatiedot-by-id)
(def select-arvioijat-by-todennettu-arviointi-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_arvioijat"
     :join "todennettu_arviointi_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_arvioija_id"
     :primary-column "id"
     :column "todennettu_arviointi_lisatiedot_id"}))
(def select-hankitun-osaamisen-naytot-by-ooyto-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "aiemmin_hankitun_yhteisen_tutkinnon_osan_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-hankitun-osaamisen-naytot-by-ooyto-osa-alue-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "aiemmin_hankitun_yto_osa_alueen_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yto_osa_alue_id"}))
(def select-arvioijat-by-ooyto-id
  (generate-select-join
    {:table "koulutuksen_jarjestaja_arvioijat"
     :join "aiemmin_hankitun_yhteisen_tutkinnon_osan_arvioijat"
     :secondary-column "koulutuksen_jarjestaja_arvioija_id"
     :primary-column "id"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(def select-osa-alueet-by-ooyto-id
  (generate-select-by
    {:table "aiemmin_hankitut_yto_osa_alueet"
     :column "aiemmin_hankittu_yhteinen_tutkinnon_osa_id"}))
(defq select-hankittavat-ammat-tutkinnon-osat-by-hoks-id)
(def select-hankitun-osaamisen-naytot-by-pato-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "hankittavan_ammat_tutkinnon_osan_naytto"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "hankittava_ammat_tutkinnon_osa_id"}))
(def select-osaamisen-hankkmistavat-by-pato-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "hankittava_ammat_tutkinnon_osa_id"}))
(defq select-opiskeluvalmiuksia-tukevat-opinnot-by-hoks-id)
(defq select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id)
(def select-osaamisen-hankkimistavat-by-pyto-osa-alue-id
  (generate-select-join
    {:table "osaamisen_hankkimistavat"
     :join "yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat"
     :secondary-column "osaamisen_hankkimistapa_id"
     :primary-column "id"
     :column "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def select-yto-osa-alueet-by-yto-id
  (generate-select-by
    {:table "yhteisen_tutkinnon_osan_osa_alueet"
     :column "yhteinen_tutkinnon_osa_id"}))
(def select-hankitun-osaamisen-naytot-by-yto-osa-alue-id
  (generate-select-join
    {:table "hankitun_osaamisen_naytot"
     :join "yhteisen_tutkinnon_osan_osa_alueen_naytot"
     :secondary-column "hankitun_osaamisen_naytto_id"
     :primary-column "id"
     :column "yhteisen_tutkinnon_osan_osa_alue_id"}))
(def select-hankitun-yto-osaamisen-nayton-osaamistavoitteet
  (generate-select-by
    {:table "hankitun_yto_osaamisen_nayton_osaamistavoitteet"
     :column "hankitun_osaamisen_naytto_id"}))
