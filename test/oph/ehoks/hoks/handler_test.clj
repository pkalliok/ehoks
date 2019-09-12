(ns oph.ehoks.hoks.handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.external.http-client :as client]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.test-data :as test-data]))

(def url "/ehoks-virkailija-backend/api/v1/hoks")

(use-fixtures :each utils/with-database)

(def oto-path "opiskeluvalmiuksia-tukevat-opinnot")

(deftest post-and-get-opiskeluvalmiuksia-tukevat-opinnot
  (testing "GET opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response (hoks-utils/create-mock-post-request
                            oto-path test-data/oto-data app hoks)
            get-response (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)]
        (hoks-utils/assert-post-response-is-ok oto-path post-response)
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc test-data/oto-data :id 1)})))))

(def ^:private one-value-of-oto-patched
  {:nimi "Muuttunut Nimi"})

(deftest patch-one-value-of-opiskeluvalmiuksia-tukevat-opinnot
  (testing "PATCH one value of opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        oto-path test-data/oto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             oto-path app one-value-of-oto-patched)
            get-response (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (is (= (:nimi get-response-data)
               (:nimi one-value-of-oto-patched))
            "Patched value should change.")
        (is (= (:kuvaus get-response-data)
               (:kuvaus test-data/oto-data))
            "Value should stay unchanged")))))

(def ^:private all-values-of-oto-patched
  {:nimi "Muuttunut Nimi"
   :kuvaus "Uusi Kuvaus"
   :alku "2020-01-12"
   :loppu "2021-10-20"})

(deftest patch-multiple-values-of-oto
  (testing "PATCH all opiskeluvalmiuksia tukevat opinnot"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        oto-path test-data/oto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             oto-path app all-values-of-oto-patched)
            get-response (hoks-utils/create-mock-hoks-osa-get-request oto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq get-response-data (assoc all-values-of-oto-patched :id 1))))))

(defn add-empty-hoks-values [hoks]
  (assoc
    hoks
    :aiemmin-hankitut-ammat-tutkinnon-osat []
    :hankittavat-paikalliset-tutkinnon-osat []
    :hankittavat-ammat-tutkinnon-osat []
    :aiemmin-hankitut-yhteiset-tutkinnon-osat []
    :hankittavat-yhteiset-tutkinnon-osat []
    :aiemmin-hankitut-paikalliset-tutkinnon-osat []
    :opiskeluvalmiuksia-tukevat-opinnot []))

(deftest get-created-hoks
  (testing "GET newly created HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"}
          response
          (hoks-utils/mock-st-post (hoks-utils/create-app nil) url hoks-data)
          body (utils/parse-body (:body response))]
      (is (= (:status response) 200))
      (eq body {:data {:uri (format "%s/1" url)} :meta {:id 1}})
      (let [hoks (-> (get-in body [:data :uri]) hoks-utils/get-authenticated :data)]
        (eq
          hoks
          (assoc (add-empty-hoks-values hoks-data)
                 :id 1
                 :eid (:eid hoks)
                 :manuaalisyotto false))))))

(deftest prevent-creating-hoks-with-existing-opiskeluoikeus
  (testing "Prevent POST HOKS with existing opiskeluoikeus"
    (let [app (hoks-utils/create-app nil)
          hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (hoks-utils/mock-st-post app url hoks-data)
      (let [response
            (hoks-utils/mock-st-post app url hoks-data)]
        (is (= (:status response) 400))
        (is (= (utils/parse-body (:body response))
               {:error
                "HOKS with the same opiskeluoikeus-oid already exists"}))))))

(deftest prevent-creating-hoks-with-non-existing-oppija
  (testing "Prevent POST HOKS with non-existing oppija"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.40404040404"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post (hoks-utils/create-app nil) url hoks-data)]
        (is (= (:status response) 400))
        (is (= (utils/parse-body (:body response))
               {:error
                "Oppija not found in Oppijanumerorekisteri"}))))))

(deftest prevent-creating-unauthorized-hoks
  (testing "Prevent POST unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post (hoks-utils/create-app nil) url hoks-data)]
        (is (= (:status response) 401))))))

(deftest prevent-getting-unauthorized-hoks
  (testing "Prevent GET unauthorized HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000002"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]

      (let [response
            (utils/with-service-ticket
              (hoks-utils/create-app nil)
              (-> (mock/request :post url)
                  (mock/json-body hoks-data))
              "1.2.246.562.24.47861388608")
            body (utils/parse-body (:body response))]
        (is (= (:status
                 (hoks-utils/mock-st-get (hoks-utils/create-app nil)
                              (get-in body [:data :uri])))
               401))))))

(deftest get-last-version-of-hoks
  (testing "GET latest (second) version of HOKS"
    (let [hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}]
      (let [response
            (hoks-utils/mock-st-post (hoks-utils/create-app nil) url hoks-data)
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (eq body {:data {:uri (format "%s/1" url)} :meta {:id 1}})
        (let [hoks (-> (get-in body [:data :uri]) hoks-utils/get-authenticated :data)]
          (is (= (count (:eid hoks)) 36))
          (eq
            hoks
            (assoc (add-empty-hoks-values hoks-data)
                   :id 1
                   :eid (:eid hoks)
                   :manuaalisyotto false)))))))

(deftest prevent-oppija-opiskeluoikeus-patch
  (testing "Prevent patching opiskeluoikeus or oppija oid"
    (let [app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-post
              app
              url
              {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
               :oppija-oid "1.2.246.562.24.12312312312"
               :ensikertainen-hyvaksyminen "2018-12-15"})
            body (utils/parse-body (:body response))]
        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :opiskeluoikeus-oid "1.2.246.562.15.00000000002"})
                 :status)
               204)
            "Should return bad request for updating opiskeluoikeus oid")

        (is (= (get
                 (hoks-utils/mock-st-patch
                   app
                   (get-in body [:data :uri])
                   {:id (get-in body [:meta :id])
                    :oppija-oid "1.2.246.562.24.12312312314"})
                 :status)
               204)
            "Should return bad request for updating oppija oid")
        (let [get-body (utils/parse-body
                         (get
                           (hoks-utils/mock-st-get
                             app
                             (get-in body [:data :uri]))
                           :body))]
          (is (= (get-in get-body [:data :opiskeluoikeus-oid])
                 "1.2.246.562.15.00000000001")
              "Opiskeluoikeus oid should be unchanged")
          (is (= (get-in get-body [:data :oppija-oid])
                 "1.2.246.562.24.12312312312")
              "Oppija oid should be unchanged"))))))

(def one-value-of-hoks-patched
  {:id 1
   :ensikertainen-hyvaksyminen "2020-01-05"})

(deftest patch-one-value-of-hoks
  (testing "PATCH updates value of created HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 one-value-of-hoks-patched app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (is (= (:ensikertainen-hyvaksyminen get-response-data)
             (:ensikertainen-hyvaksyminen one-value-of-hoks-patched))
          "Patched value should change.")
      (is (= (:kuvaus get-response-data)
             (:kuvaus one-value-of-hoks-patched))
          "Value should stay unchanged"))))

(def main-level-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"})

(deftest hoks-put-removes-parts
  (testing "PUT only main level HOKS values, removes parts"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1 main-level-of-hoks-updated app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (is (empty? (:opiskeluvalmiuksia-tukevat-opinnot get-response-data)))
      (is (empty? (:hankittavat-ammat-tutkinnon-osat get-response-data)))
      (is (empty? (:hankittavat-paikalliset-tutkinnon-osat get-response-data)))
      (is (empty? (:hankittavat-yhteiset-tutkinnon-osat get-response-data)))
      (is (empty? (:aiemmin-hankitut-ammat-tutkinnon-osat get-response-data)))
      (is (empty?
            (:aiemmin-hankitut-paikalliset-tutkinnon-osat get-response-data)))
      (is (empty?
            (:aiemmin-hankitut-yhteiset-tutkinnon-osat get-response-data))))))

(defn mock-replace-ahyto [& _]
  (throw (Exception. "Failed.")))

(deftest hoks-part-put-fails-whole-operation-is-aborted
  (testing (str "PUT of HOKS should be inside transaction so that when"
                "one part of operation fails, everything is aborted"
                (with-redefs [oph.ehoks.hoks.hoks/replace-ahyto!
                              mock-replace-ahyto]
                  (let [app (hoks-utils/create-app nil)
                        post-response (hoks-utils/create-mock-post-request
                                        "" test-data/hoks-data app)
                        put-response (hoks-utils/create-mock-hoks-put-request
                                       1 main-level-of-hoks-updated app)
                        get-response (hoks-utils/create-mock-hoks-get-request 1 app)
                        get-response-data (:data (utils/parse-body
                                                   (:body get-response)))]
                    (is (= (:status post-response) 200))
                    (is (= (:status put-response) 500))
                    (is (= (:status get-response) 200))
                    (is (= (:versio get-response-data) 4))
                    (is (not-empty (:opiskeluvalmiuksia-tukevat-opinnot
                                     get-response-data)))
                    (is (not-empty (:hankittavat-ammat-tutkinnon-osat
                                     get-response-data)))
                    (is (not-empty (:hankittavat-paikalliset-tutkinnon-osat
                                     get-response-data)))
                    (is (not-empty (:hankittavat-yhteiset-tutkinnon-osat
                                     get-response-data)))
                    (is (not-empty (:aiemmin-hankitut-ammat-tutkinnon-osat
                                     get-response-data)))
                    (is (not-empty
                          (:aiemmin-hankitut-paikalliset-tutkinnon-osat
                            get-response-data)))
                    (is (not-empty (:aiemmin-hankitut-yhteiset-tutkinnon-osat
                                     get-response-data))))))))

(deftest hoks-put-adds-non-existing-part
  (testing "If HOKS part doesn't currently exist, PUT creates it"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request
            "" (dissoc test-data/hoks-data :opiskeluvalmiuksia-tukevat-opinnot) app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1
                         (-> test-data/hoks-data
                             (assoc :id 1)
                             (dissoc :opiskeluoikeus-oid :oppija-oid))
                         app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (eq (:opiskeluvalmiuksia-tukevat-opinnot test-data/hoks-data)
          (:opiskeluvalmiuksia-tukevat-opinnot get-response-data)))))

(deftest patching-of-hoks-part-not-allowed
  (testing "PATCH of HOKS can't be used to update sub entities of HOKS"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          patch-response (hoks-utils/create-mock-hoks-patch-request
                           1 test-data/hoks-data app)]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 400)))))

(def oto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :opiskeluvalmiuksia-tukevat-opinnot
   [{:nimi "Uusi Nimi"
     :kuvaus "joku kuvaus"
     :alku "2019-06-22"
     :loppu "2021-05-07"}]})

(deftest put-oto-of-hoks
  (testing "PUTs opiskeluvalmiuksia tukevat opinnot of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      oto-of-hoks-updated :opiskeluvalmiuksia-tukevat-opinnot test-data/hoks-data)))

(def multiple-otos-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :opiskeluvalmiuksia-tukevat-opinnot
   [{:nimi "Uusi Nimi"
     :kuvaus "joku kuvaus"
     :alku "2019-06-22"
     :loppu "2021-05-07"}
    {:nimi "Toinen Nimi"
     :kuvaus "eri kuvaus"
     :alku "2018-06-22"
     :loppu "2022-05-07"}]})

(deftest put-multiple-oto-of-hoks
  (testing "PUTs multiple opiskeluvalmiuksia tukevat opinnot of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      multiple-otos-of-hoks-updated
      :opiskeluvalmiuksia-tukevat-opinnot
      test-data/hoks-data)))

(deftest omitted-hoks-fields-are-nullified
  (testing "If HOKS main level value isn't given in PUT, it's nullified"
    (let [app (hoks-utils/create-app nil)
          post-response (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          put-response (hoks-utils/create-mock-hoks-put-request
                         1 main-level-of-hoks-updated app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status put-response) 204))
      (is (= (:status get-response) 200))
      (is (nil? (:versio get-response-data)))
      (is (nil? (:sahkoposti get-response-data)))
      (is (nil? (:urasuunnitelma-koodi-uri get-response-data)))
      (is (nil? (:osaamisen-hankkimisen-tarve get-response-data)))
      (is (nil? (:hyvaksytty get-response-data)))
      (is (nil? (:urasuunnitelma-koodi-versio get-response-data)))
      (is (nil? (:paivitetty get-response-data))))))

(def hato-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :hankittavat-ammat-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_300222"
     :tutkinnon-osa-koodi-versio 2
     :vaatimuksista-tai-tavoitteista-poikkeaminen
     "Ei poikkeamia."
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000005"
     :osaamisen-hankkimistavat
     [{:alku "2018-12-12"
       :loppu "2018-12-20"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri "oppimisymparistot_0003"
         :oppimisymparisto-koodi-versio 1
         :alku "2019-03-10"
         :loppu "2019-03-19"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hank"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.54453924330"}
       :nayttoymparisto {:nimi "Testiympäristö 2"
                         :y-tunnus "12345671-2"
                         :kuvaus "Testi test"}
       :sisallon-kuvaus ["Testaus"]
       :koulutuksen-jarjestaja-osaamisen-arvioijat
       [{:nimi "Timo Testaaja2"
         :organisaatio {:oppilaitos-oid
                        "1.2.246.562.10.54452521332"}}]
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Taneli Työmies2"
         :organisaatio {:nimi "Tanelin Paja Oy"
                        :y-tunnus "12345622-2"}}]
       :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_kl"
                     :koodi-versio 3}]
       :alku "2019-03-10"
       :loppu "2019-03-19"
       :yksilolliset-kriteerit ["Yksi kriteeri" "toinen kriteeri"]}]}]})

(deftest put-hato-of-hoks
  (testing "PUTs hankittavat ammatilliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      hato-of-hoks-updated :hankittavat-ammat-tutkinnon-osat test-data/hoks-data)))

(def hpto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :hankittavat-paikalliset-tutkinnon-osat
   [{:nimi "testinimi"
     :koulutuksen-jarjestaja-oid
     "1.2.246.562.10.00000000001"
     :olennainen-seikka false
     :osaamisen-hankkimistavat
     [{:alku "2019-12-12"
       :loppu "2020-12-20"
       :ajanjakson-tarkenne "Tarkenne muuttunut"
       :osaamisen-hankkimistapa-koodi-uri
       "osaamisenhankkimistapa_koulutussopimus"
       :osaamisen-hankkimistapa-koodi-versio 1
       :muut-oppimisymparistot
       [{:oppimisymparisto-koodi-uri
         "oppimisymparistot_0004"
         :oppimisymparisto-koodi-versio 2
         :alku "2019-03-10"
         :loppu "2021-03-19"}]
       :hankkijan-edustaja
       {:nimi "Heikki Hankk"
        :rooli "Opettaja"
        :oppilaitos-oid "1.2.246.562.10.54452422420"}}]
     :osaamisen-osoittaminen
     [{:jarjestaja {:oppilaitos-oid
                    "1.2.246.562.10.00000000022"}
       :koulutuksen-jarjestaja-osaamisen-arvioijat []
       :osa-alueet []
       :sisallon-kuvaus
       ["ensimmäinen sisältö" "toinenkin" "kolkki"]
       :nayttoymparisto {:nimi "aaab"}
       :alku "2018-12-12"
       :loppu "2018-12-20"
       :tyoelama-osaamisen-arvioijat
       [{:nimi "Nimi2"
         :organisaatio {:nimi "Organisaation nimi"}}]
       :vaatimuksista-tai-tavoitteista-poikkeaminen
       "Poikkeama tämä."
       :yksilolliset-kriteerit
       ["kriteeri 1"]}]}]})

(deftest put-hpto-of-hoks
  (testing "PUTs hankittavat paikalliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      hpto-of-hoks-updated :hankittavat-paikalliset-tutkinnon-osat test-data/hoks-data)))

(def hyto-of-hoks-updated
  {:id 1
   :ensikertainen-hyvaksyminen "2018-12-15"
   :hankittavat-yhteiset-tutkinnon-osat
   [{:tutkinnon-osa-koodi-uri "tutkinnonosat_3002690"
     :tutkinnon-osa-koodi-versio 3
     :koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000007"
     :osa-alueet
     [{:osa-alue-koodi-uri "ammatillisenoppiaineet_bi"
       :osa-alue-koodi-versio 1
       :vaatimuksista-tai-tavoitteista-poikkeaminen "poikkeaminen"
       :olennainen-seikka true
       :osaamisen-hankkimistavat
       [{:alku "2018-12-15"
         :loppu "2018-12-23"
         :osaamisen-hankkimistapa-koodi-uri "osaamisenhankkimistapa_joku"
         :osaamisen-hankkimistapa-koodi-versio 3
         :muut-oppimisymparistot
         [{:oppimisymparisto-koodi-uri "oppimisymparistot_0232"
           :oppimisymparisto-koodi-versio 3
           :alku "2016-03-10"
           :loppu "2021-03-19"}]}]
       :osaamisen-osoittaminen
       [{:jarjestaja {:oppilaitos-oid "1.2.246.562.10.00000000032"}
         :nayttoymparisto {:nimi "aaab"}
         :osa-alueet [{:koodi-uri "ammatillisenoppiaineet_ru"
                       :koodi-versio 4}]
         :koulutuksen-jarjestaja-osaamisen-arvioijat
         [{:nimi "Erkki Esimerkkitest"
           :organisaatio {:oppilaitos-oid
                          "1.2.246.562.10.13490579322"}}]
         :alku "2018-12-12"
         :loppu "2019-12-20"
         :sisallon-kuvaus ["Kuvaus" "toinen"]
         :tyoelama-osaamisen-arvioijat [{:nimi "Nimi" :organisaatio
                                         {:nimi "Organisaation name"}}]
         :yksilolliset-kriteerit ["Ensimmäinen kriteeri" "toka"]}]}]}]})

(deftest put-hyto-of-hoks
  (testing "PUTs hankittavat yhteiset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      hyto-of-hoks-updated :hankittavat-yhteiset-tutkinnon-osat test-data/hoks-data)))

(deftest patch-non-existing-hoks
  (testing "PATCH prevents updating non existing HOKS"
    (let [response (hoks-utils/create-mock-hoks-patch-request
                     1 {:id 1} (hoks-utils/create-app nil))]
      (is (= (:status response) 404)))))

(deftest put-non-existing-hoks
  (testing "PUT prevents updating non existing HOKS"
    (let [response (hoks-utils/create-mock-hoks-put-request
                     1 {:id 1} (hoks-utils/create-app nil))]
      (is (= (:status response) 404)))))

(deftest get-hoks-by-id-not-found
  (testing "GET HOKS by hoks-id"
    (let [response
          (hoks-utils/mock-st-get (hoks-utils/create-app nil) (format "%s/%s" url 43857))]
      (is (= (:status response) 404)))))

(deftest get-hoks-by-opiskeluoikeus-oid
  (testing "GET HOKS by opiskeluoikeus-oid"
    (let [opiskeluoikeus-oid "1.2.246.562.15.00000000001"
          hoks-data {:opiskeluoikeus-oid opiskeluoikeus-oid
                     :oppija-oid "1.2.246.562.24.12312312312"
                     :ensikertainen-hyvaksyminen "2018-12-15"
                     :osaamisen-hankkimisen-tarve false}
          app (hoks-utils/create-app nil)]
      (let [response
            (hoks-utils/mock-st-get
              app (format "%s/opiskeluoikeus/%s" url opiskeluoikeus-oid))]
        (is (= (:status response) 404)))
      (hoks-utils/mock-st-post app url hoks-data)
      (let [response
            (hoks-utils/mock-st-get app (format "%s/opiskeluoikeus/%s"
                                     url opiskeluoikeus-oid))
            body (utils/parse-body (:body response))]
        (is (= (:status response) 200))
        (is (= (-> body
                   :data
                   :opiskeluoikeus-oid)
               opiskeluoikeus-oid))))))

(deftest non-service-user-test
  (testing "Deny access from non-service user"
    (client/with-mock-responses
      [(fn [url options]
         (cond (.endsWith
                 url "/koski/api/opiskeluoikeus/1.2.246.562.15.00000000001")
               {:status 200
                :body {:oppilaitos {:oid "1.2.246.562.10.12944436166"}}}
               (.endsWith url "/serviceValidate")
               {:status 200
                :body
                (str "<cas:serviceResponse"
                     "  xmlns:cas='http://www.yale.edu/tp/cas'>"
                     "<cas:authenticationSuccess><cas:user>ehoks</cas:user>"
                     "<cas:attributes>"
                     "<cas:longTermAuthenticationRequestTokenUsed>false"
                     "</cas:longTermAuthenticationRequestTokenUsed>"
                     "<cas:isFromNewLogin>false</cas:isFromNewLogin>"
                     "<cas:authenticationDate>2019-02-20T10:14:24.046+02:00"
                     "</cas:authenticationDate></cas:attributes>"
                     "</cas:authenticationSuccess></cas:serviceResponse>")}
               (.endsWith url "/kayttooikeus-service/kayttooikeus/kayttaja")
               {:status 200
                :body [{:oidHenkilo "1.2.246.562.24.11474338834"
                        :username "ehoks"
                        :kayttajaTyyppi "VIRKAILIJA"
                        :organisaatiot
                        [{:organisaatioOid "1.2.246.562.10.12944436166"
                          :kayttooikeudet [{:palvelu "EHOKS"
                                            :oikeus "CRUD"}]}]}]}))
       (fn [url options]
         (cond
           (.endsWith url "/v1/tickets")
           {:status 201
            :headers {"location" "http://test.ticket/1234"}}
           (= url "http://test.ticket/1234")
           {:status 200
            :body "ST-1234-testi"}))]
      (let [app (hoks-utils/create-app nil)
            hoks-data {:opiskeluoikeus-oid "1.2.246.562.15.00000000001"
                       :oppija-oid "1.2.246.562.24.12312312312"
                       :laatija {:nimi "Teppo Tekijä"}
                       :paivittaja {:nimi "Pekka Päivittäjä"}
                       :hyvaksyja {:nimi "Heikki Hyväksyjä"}
                       :ensikertainen-hyvaksyminen "2018-12-15"}
            response (app (-> (mock/request :post url)
                              (mock/json-body hoks-data)
                              (mock/header "Caller-Id" "test")
                              (mock/header "ticket" "ST-testitiketti")))]
        (is (= (:status response) 403))
        (is (= (utils/parse-body (:body response))
               {:error "User type 'PALVELU' is required"}))))))
