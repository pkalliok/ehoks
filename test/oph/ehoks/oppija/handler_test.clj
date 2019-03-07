(ns oph.ehoks.oppija.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.handler :refer [create-app]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.db.postgresql :as db]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.session-store :refer [test-session-store]]))

(def url "/ehoks-backend/api/v1/oppija/oppijat")

(def hoks
  {:puuttuvat-paikalliset-tutkinnon-osat
   [{:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :tavoitteet-ja-sisallot ""
     :nimi "Orientaatio alaan"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Infotilaisuus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Opintojen ohjaus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Tutkintotilaisuus"}
    {:koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto []
     :osaamisen-hankkimistavat []
     :nimi "Työpaikalla oppiminen "}]
   :ensikertainen-hyvaksyminen (java.util.Date.)
   :luotu (java.util.Date.)
   :urasuunnitelma-koodi-uri "urasuunnitelma_0001"
   :hyvaksytty (java.util.Date.)
   :olemassa-olevat-ammatilliset-tutkinnon-osat []
   :olemassa-olevat-yhteiset-tutkinnon-osat []
   :puuttuvat-ammatilliset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_103590"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto
     [{:alku (java.time.LocalDate/now)
       :loppu (java.time.LocalDate/now)
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921329"}
       :nayttoymparisto {:nimi "" :y-tunnus ""}
       :koulutuksenjarjestaja-arvioijat
       [{:nimi "Olson,Wava"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921329"}}]}]
     :osaamisen-hankkimistavat []}
    {:tutkinnon-osa-koodi-uri "tutkinnonosat_103590"
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"
     :hankitun-osaamisen-naytto
     [{:alku (java.time.LocalDate/now)
       :loppu (java.time.LocalDate/now)
       :jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453921329"}
       :nayttoymparisto {:nimi "", :y-tunnus ""}
       :koulutuksenjarjestaja-arvioijat
       [{:nimi "Moen,Pearl"
         :organisaatio {:oppilaitos-oid "1.2.246.562.10.54453921329"}}]}]
     :osaamisen-hankkimistavat []}]
   :puuttuvat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_103596"
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_fk"}]
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.54453921329"}]
   :opiskeluoikeus-oid "1.2.246.562.15.76811932037"
   :laatija {:nimi "Simonis,Hollie"}
   :versio 0
   :paivitetty (java.util.Date.)
   :eid "0000"
   :paivittaja {:nimi "Ei tietoa"}
   :oppija-oid "1.2.246.562.24.29790141661"
   :hyvaksyja {:nimi "Ei tietoa"}
   :opiskeluvalmiuksia-tukevat-opinnot []})

(defn set-hoks-data! [h]
  (db/clear!)
  (db/insert-hoks!
    (assoc h :versio 1 :version 1 :paivittaja {:nimi "Tapio Testaaja"})))

(deftest get-hoks
  (testing "GET enriched HOKS"
    (set-hoks-data! hoks)
    (let [store (atom {})
          app (create-app (test-session-store store))
          response
          (utils/with-authenticated-oid
            store
            (:oppija-oid hoks)
            app
            (mock/request
              :get
              (format "%s/%s/hoks" url (:oppija-oid hoks))))]
      (is (= (:status response) 200))
      (let [body (utils/parse-body (:body response))]
        (eq
          body
          {:data
           [(-> hoks
                (assoc
                  :paivittaja {:nimi "Tapio Testaaja"}
                  :versio 1
                  :luotu (get-in body [:data 0 :luotu])
                  :paivitetty (get-in body [:data 0 :paivitetty])
                  :hyvaksytty (get-in body [:data 0 :hyvaksytty])
                  :ensikertainen-hyvaksyminen
                  (get-in body [:data 0 :ensikertainen-hyvaksyminen]))
                (update
                  :puuttuvat-ammatilliset-tutkinnon-osat
                  (fn [oc]
                    (mapv
                      (fn [o]
                        (update
                          o
                          :hankitun-osaamisen-naytto
                          (fn [c]
                            (mapv
                              #(update
                                 (update %  :alku str)
                                 :loppu str)
                              c))))
                      oc))))]
           :meta {}})))))
