(ns oph.ehoks.virkailija.handler-test
  (:require [oph.ehoks.virkailija.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [clojure.test :as t]
            [oph.ehoks.utils :as utils]
            [oph.ehoks.session-store :refer [test-session-store]]
            [oph.ehoks.db.postgresql :as db]))

(def base-url "/ehoks-virkailija-backend/api/v1")

(t/deftest buildversion
  (t/testing "GET /buildversion.txt"
    (let [app (common-api/create-app handler/app-routes)
          response (app (mock/request
                          :get "/ehoks-virkailija-backend/buildversion.txt"))
          body (slurp (:body response))]
      (t/is (= (:status response) 200))
      (t/is (re-find #"^artifactId=" body)))))

(t/deftest not-found
  (t/testing "GET route which does not exist"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/non-existing-resource/")))]
      (t/is (= (:status response) 404)))))

(t/deftest healthcheck
  (t/testing "GET healthcheck"
    (let [app (common-api/create-app handler/app-routes)
          response (app
                     (mock/request
                       :get (str base-url "/healthcheck")))
          body (utils/parse-body (:body response))]
      (t/is (= (:status response) 200))
      (t/is (= body {})))))

(t/deftest test-environment
  (t/testing "GET environment info"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app
                     (mock/request
                       :get (str base-url "/misc/environment")))]
      (t/is (= (:status response) 200))
      (let [data (-> response :body utils/parse-body :data)]
        (t/is (some? (:opintopolku-login-url data)))
        (t/is (some? (:opintopolku-logout-url data)))
        (t/is (some? (:eperusteet-peruste-url data)))
        (t/is (some? (:virkailija-login-url data)))))))

(defn with-test-virkailija
  ([request virkailija]
    (let [session "12345678-1234-1234-1234-1234567890ab"
          cookie (str "ring-session=" session)
          store (atom
                  {session
                   {:virkailija-user virkailija}})
          app (common-api/create-app
                handler/app-routes (test-session-store store))]
      (app (mock/header request :cookie cookie))))
  ([request] (with-test-virkailija
               request
               {:name "Test"
                :organisation-privileges
                [{:oid "1.2.246.562.10.12000000000"
                  :privileges #{:read}}]})))

(t/deftest test-unauthorized-virkailija
  (t/testing "GET unauthorized virkailija"
    (let [response (with-test-virkailija
                     (mock/request
                       :get
                       (str base-url "/virkailija/oppijat")
                       {:oppilaitos-oid "1.2.246.562.10.12000000000"})
                     nil)]
      (t/is (= (:status response) 401)))))

(t/deftest test-list-oppijat-with-empty-index
  (t/testing "GET empty oppijat list"
    (utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000000"}))]
        (t/is (= (:status response) 200))))))

(t/deftest test-virkailija-privileges
  (t/testing "Prevent getting other organisation oppijat"
    (utils/with-db
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000001"}))]
        (t/is (= (:status response) 403))))))

(defn- add-oppija [oppija]
  (db/insert-oppija
    {:oid (:oid oppija)
     :nimi (:nimi oppija)})
  (db/insert-opiskeluoikeus
    {:oid (:opiskeluoikeus-oid oppija)
     :oppija_oid (:oid oppija)
     :oppilaitos_oid (:oppilaitos-oid oppija)
     :koulutustoimija_oid (:koulutustoimija-oid oppija)
     :tutkinto ""
     :osaamisala ""}))

(t/deftest test-list-virkailija-oppijat
  (t/testing "GET virkailija oppijat"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Testi 1"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :koulutustoimija-oid ""})
      (add-oppija {:oid "1.2.246.562.24.44000000002"
                   :nimi "Testi 2"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000002"
                   :oppilaitos-oid "1.2.246.562.10.12000000001"
                   :koulutustoimija-oid ""})
      (add-oppija {:oid "1.2.246.562.24.44000000003"
                   :nimi "Testi 3"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000003"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :koulutustoimija-oid ""})
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000000"}))]
        (t/is (= (:status response) 200))
        (let [body (utils/parse-body (:body response))]
          (t/is (= (count (:data body)) 2))
          (t/is (= (get-in body [:data 0 :oid]) "1.2.246.562.24.44000000001"))
          (t/is (= (get-in body [:data 1 :oid]) "1.2.246.562.24.44000000003")))))))

(t/deftest test-virkailija-with-no-read
  (t/testing "Prevent GET virkailija oppijat without read privilege"
    (utils/with-db
      (add-oppija {:oid "1.2.246.562.24.44000000001"
                   :nimi "Testi 1"
                   :opiskeluoikeus-oid "1.2.246.562.15.76000000001"
                   :oppilaitos-oid "1.2.246.562.10.12000000000"
                   :koulutustoimija-oid ""})
      (let [response (with-test-virkailija
                       (mock/request
                         :get
                         (str base-url "/virkailija/oppijat")
                         {:oppilaitos-oid "1.2.246.562.10.12000000000"})
                       {:name "Test"
                        :organisation-privileges
                        [{:oid "1.2.246.562.10.12000000000"
                          :privileges #{}}]})]
        (t/is (= (:status response) 403))))))