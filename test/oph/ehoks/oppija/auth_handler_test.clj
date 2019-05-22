(ns oph.ehoks.oppija.auth-handler-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.handler :as handler]
            [oph.ehoks.common.api :as common-api]
            [ring.mock.request :as mock]
            [oph.ehoks.utils :refer [parse-body]]))

(def base-url "/ehoks-oppija-backend/api/v1/oppija/session/")

(defn authenticate [app]
  (app (-> (mock/request :get (str base-url "opintopolku/"))
           (mock/header "FirstName" "Teuvo Testi")
           (mock/header "cn" "Teuvo")
           (mock/header "givenname" "Teuvo")
           (mock/header "hetu" "190384-9245")
           (mock/header "sn" "Testaaja"))))

(deftest session-without-authentication
  (testing "GET current session without authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response (app (mock/request
                          :get
                          base-url))]
      (is (= (:status response) 401))
      (is (empty? (:body response))))))

(deftest session-authenticate
  (testing "POST authenticate"
    (let [response (authenticate
                     (common-api/create-app handler/app-routes nil))]
      (is (= (:status response) 303)))))

(deftest prevent-malformed-authentication
  (testing "Prevents malformed authentication"
    (let [app (common-api/create-app handler/app-routes nil)
          response
          (app (-> (mock/request
                     :get (str base-url "opintopolku/")
                     {"FirstName" "Teuvo Testi"
                      "cn" "Teuvo"
                      "hetu" "190384-9245"
                      "sn" "Testaaja"})
                   (mock/header "FirstName" "Teuvo Testi")
                   (mock/header "cn" "Teuvo")
                   (mock/header "givenname" "Teuvo")
                   (mock/header "sn" "Testaaja")))]
      (is (= (:status response) 400)))))

(deftest session-authenticated
  (testing "GET current authenticated session"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)))
          body (parse-body (:body response))]
      (is (= (:status response) 200))
      (is (= (:data body) [{:first-name "Teuvo Testi"
                            :common-name "Teuvo"
                            :surname "Testaaja"}])))))

(deftest session-delete-authenticated
  (testing "DELETE authenticated session"
    (let [app (common-api/create-app handler/app-routes nil)
          auth-response (authenticate app)
          session-cookie (first (get-in auth-response [:headers "Set-Cookie"]))
          authenticated-response
          (app (-> (mock/request
                     :get
                     base-url)
                   (mock/header :cookie session-cookie)))
          authenticated-body (parse-body (:body authenticated-response))
          delete-response
          (app (-> (mock/request
                     :delete
                     base-url)
                   (mock/header :cookie session-cookie)))
          response (app (-> (mock/request
                              :get
                              base-url)
                            (mock/header :cookie session-cookie)))]
      (is (= (:status authenticated-response) 200))
      (is (= (:data authenticated-body)
             [{:first-name "Teuvo Testi"
               :common-name "Teuvo"
               :surname "Testaaja"}]))
      (is (= (:status response) 401))
      (is (= (:status delete-response) 200))
      (is (= (:status delete-response) 200)))))