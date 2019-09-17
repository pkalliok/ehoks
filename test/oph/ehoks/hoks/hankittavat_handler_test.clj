(ns oph.ehoks.hoks.hankittavat-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.test-data :as test-data]))

(use-fixtures :each utils/with-database)

(def hpto-path "hankittava-paikallinen-tutkinnon-osa")
(def hyto-path "hankittava-yhteinen-tutkinnon-osa")
(def hao-path "hankittava-ammat-tutkinnon-osa")

(deftest post-and-get-hankittava-paikallinen-tutkinnon-osa
  (testing "GET newly created hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [ppto-response (hoks-utils/mock-st-post
                            app (hoks-utils/get-hoks-url hoks hpto-path)
                            test-data/hpto-data)
            body (utils/parse-body (:body ppto-response))]
        (is (= (:status ppto-response) 200))
        (eq body {:data
                  {:uri
                   (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))}
                  :meta {:id 1}})
        (let [ppto-new (hoks-utils/mock-st-get
                         app
                         (hoks-utils/get-hoks-url
                           hoks (format "%s/1" hpto-path)))]
          (eq
            (:data (utils/parse-body (:body ppto-new)))
            (assoc
              test-data/hpto-data
              :id 1)))))))

(deftest patch-all-hankittavat-paikalliset-tutkinnon-osat
  (testing "PATCH all hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/mock-st-post
        app (hoks-utils/get-hoks-url hoks hpto-path) test-data/hpto-data)
      (let [patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))
              (assoc test-data/hpto-data :nimi "333" :olennainen-seikka false))]
        (is (= (:status patch-response) 204))))))

(deftest patch-one-hankittava-paikallinen-tutkinnon-osa
  (testing "PATCH one value hankittava paikallinen tutkinnon osa"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [ppto-response
            (hoks-utils/mock-st-post
              app (hoks-utils/get-hoks-url hoks hpto-path) test-data/hpto-data)
            ppto-body (utils/parse-body (:body ppto-response))
            patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (format "%s/1" hpto-path))
              {:id 1 :nimi "2223"})
            get-response (-> (get-in ppto-body [:data :uri])
                             hoks-utils/get-authenticated
                             :data)]
        (is (= (:status patch-response) 204))
        (eq get-response
            (assoc test-data/hpto-data
                   :id 1
                   :nimi "2223"))))))

(deftest post-and-get-hankittava-yhteinen-tukinnon-osa
  (testing "POST hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response (hoks-utils/create-mock-post-request
                            hyto-path test-data/hyto-data app hoks)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)]
        (hoks-utils/assert-post-response-is-ok hyto-path post-response)
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc test-data/hyto-data :id 1)})))))

(def ^:private one-value-of-hyto-patched
  {:koulutuksen-jarjestaja-oid "1.2.246.562.10.00000000012"})

(deftest patch-one-value-of-hankittava-yhteinen-tutkinnon-osa
  (testing "PATCH one value hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path app one-value-of-hyto-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (is (= (:koulutuksen-jarjestaja-oid get-response-data)
               (:koulutuksen-jarjestaja-oid one-value-of-hyto-patched))
            "Patched value should change.")
        (is (= (:tutkinnon-osa-koodi-versio get-response-data)
               (:tutkinnon-osa-koodi-versio test-data/hyto-data))
            "Value should stay unchanged")))))

(deftest patch-multiple-values-of-hankittavat-yhteiset-tutkinnon-osat
  (testing "PATCH all hankittavat yhteisen tutkinnon osat"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path
                             app
                             test-data/multiple-hyto-values-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq (:osa-alueet get-response-data)
            (:osa-alueet test-data/multiple-hyto-values-patched))))))

(def hyto-sub-entity-patched
  {:osa-alueet test-data/osa-alueet-of-hyto})

(deftest only-sub-entity-of-hyto-patched
  (testing "PATCH only osa-alueet of hyto and leave base hyto untouched."
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request
        hyto-path test-data/hyto-data app hoks)
      (let [patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                             hyto-path app hyto-sub-entity-patched)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hyto-path app hoks)
            get-response-data (:data (utils/parse-body (:body get-response)))]
        (is (= (:status patch-response) 204))
        (eq (:osa-alueet get-response-data)
            (:osa-alueet hyto-sub-entity-patched))))))

(deftest post-and-get-hankittava-ammatillinen-osaaminen
  (testing "POST hankittava ammatillinen osaaminen and then get created hao"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (let [post-response
            (hoks-utils/create-mock-post-request
              hao-path test-data/hao-data app hoks)
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status post-response) 200))
        (eq (utils/parse-body
              (:body post-response))
            {:meta {:id 1}
             :data
             {:uri
              (format
                "%s/1/hankittava-ammat-tutkinnon-osa/1"
                hoks-utils/base-url)}})
        (is (= (:status get-response) 200))
        (eq (utils/parse-body
              (:body get-response))
            {:meta {} :data (assoc test-data/hao-data :id 1)})))))

(deftest patch-all-hankittava-ammatillinen-osaaminen
  (testing "PATCH ALL hankittava ammat osaaminen"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/create-mock-post-request hao-path test-data/hao-data app hoks)
      (let [patch-response
            (hoks-utils/mock-st-patch
              app
              (hoks-utils/get-hoks-url hoks (str hao-path "/1"))
              (assoc test-data/patch-all-hao-data :id 1))
            get-response
            (hoks-utils/create-mock-hoks-osa-get-request hao-path app hoks)]
        (is (= (:status patch-response) 204))
        (eq (utils/parse-body (:body get-response))
            {:meta {} :data  (assoc test-data/patch-all-hao-data :id 1)})))))

(deftest patch-one-hankittava-ammatilinen-osaaminen
  (testing "PATCH one value hankittava ammatillinen osaaminen"
    (hoks-utils/with-hoks-and-app
      [hoks app]
      (hoks-utils/mock-st-post
        app
        (format
          "%s/1/hankittava-ammat-tutkinnon-osa"
          hoks-utils/base-url) test-data/hao-data)
      (let [response
            (hoks-utils/mock-st-patch
              app
              (format
                "%s/1/%s/1"
                hoks-utils/base-url hao-path)
              {:id 1
               :vaatimuksista-tai-tavoitteista-poikkeaminen "Test"})]
        (is (= (:status response) 204))))))