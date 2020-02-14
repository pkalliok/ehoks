(ns oph.ehoks.hoks.aiemmin-hankitut-handler-test
  (:require [clojure.test :refer [deftest testing is use-fixtures]]
            [oph.ehoks.utils :as utils :refer [eq]]
            [oph.ehoks.hoks.hoks-test-utils :as hoks-utils]
            [oph.ehoks.hoks.test-data :as test-data]))

(use-fixtures :each utils/with-database)

(def ahyto-path "aiemmin-hankittu-yhteinen-tutkinnon-osa")
(def ahato-path "aiemmin-hankittu-ammat-tutkinnon-osa")
(def ahpto-path "aiemmin-hankittu-paikallinen-tutkinnon-osa")

(deftest post-and-get-aiemmin-hankitut-yhteiset-tutkinnon-osat
  (testing "POST ahyto and then get the created ahyto"
    (hoks-utils/test-post-and-get-of-aiemmin-hankittu-osa
      ahyto-path test-data/ahyto-data)))

(deftest put-ahyto-of-hoks
  (testing "PUTs aiemmin hankitut yhteiset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahyto-of-hoks-updated
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahpto-of-hoks
  (testing "PUTs aiemmin hankitut paikalliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahpto-of-hoks-updated
      :aiemmin-hankitut-paikalliset-tutkinnon-osat
      test-data/hoks-data)))

(deftest put-ahato-of-hoks
  (testing "PUTs aiemmin hankitut ammatilliset tutkinnon osat of HOKS"
    (hoks-utils/assert-partial-put-of-hoks
      test-data/ahato-of-hoks-updated
      :aiemmin-hankitut-ammat-tutkinnon-osat
      test-data/hoks-data)))

(defn- test-patch-of-aiemmin-hankittu-osa
  [osa-path osa-data osa-patched-data assert-function]
  (hoks-utils/with-hoks-and-app
    [hoks app]
    (let [post-response (hoks-utils/create-mock-post-request
                          osa-path osa-data app hoks)
          patch-response (hoks-utils/create-mock-hoks-osa-patch-request
                           osa-path app osa-patched-data)
          get-response
          (hoks-utils/create-mock-hoks-osa-get-request osa-path app hoks)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status patch-response) 204))
      (is (= (:status get-response) 200))
      (assert-function get-response-data osa-data))))

(defn- assert-ahyto-is-patched-correctly [updated-data initial-data]
  (is (= (:valittu-todentamisen-prosessi-koodi-uri updated-data)
         "osaamisentodentamisenprosessi_2000"))
  (is (= (:tutkinnon-osa-koodi-versio updated-data)
         (:tutkinnon-osa-koodi-versio initial-data)))
  (eq (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
      (:tarkentavat-tiedot-osaamisen-arvioija
        test-data/multiple-ahyto-values-patched))
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahyto-values-patched first)
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahyto-values-patched second)
  (eq (utils/dissoc-uuids (:osa-alueet updated-data))
      (:osa-alueet test-data/multiple-ahyto-values-patched)))

(deftest patch-aiemmin-hankittu-yhteinen-tutkinnon-osa
  (testing "Patching values of ahyto"
    (test-patch-of-aiemmin-hankittu-osa
      ahyto-path
      test-data/ahyto-data
      test-data/multiple-ahyto-values-patched
      assert-ahyto-is-patched-correctly)))

(defn- get-arvioija [model]
  (-> model
      :aiemmin-hankitut-yhteiset-tutkinnon-osat
      first
      :osa-alueet
      first
      :tarkentavat-tiedot-osaamisen-arvioija))

(deftest ahyto-osa-alue-has-arvioija
  (testing "tarkentavat-tiedot-osaamisen-arvioija was addded to ahyto osa-alue
            according to EH-806"
    (let [app (hoks-utils/create-app nil)
          post-response
          (hoks-utils/create-mock-post-request "" test-data/hoks-data app)
          get-response (hoks-utils/create-mock-hoks-get-request 1 app)
          get-response-data (:data (utils/parse-body (:body get-response)))]
      (is (= (:status post-response) 200))
      (is (= (:status get-response) 200))
      (let [output-arvioija (get-arvioija get-response-data)
            input-arvioija (get-arvioija test-data/hoks-data)]
        (eq output-arvioija input-arvioija)))))

(deftest post-and-get-aiemmin-hankitut-ammatilliset-tutkinnon-osat
  (testing "POST ahato and then get the created ahato"
    (hoks-utils/test-post-and-get-of-aiemmin-hankittu-osa
      ahato-path test-data/ahato-data)))

(defn- assert-ahato-data-is-patched-correctly [updated-data old-data]
  (is (= (:tutkinnon-osa-koodi-versio updated-data) 3000))
  (is (= (:valittu-todentamisen-prosessi-koodi-versio updated-data)
         (:valittu-todentamisen-prosessi-koodi-versio old-data)))
  (is (= (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
         (:tarkentavat-tiedot-osaamisen-arvioija
           test-data/multiple-ahato-values-patched)))
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahato-values-patched first))

(deftest patch-aiemmin-hankitut-ammat-tutkinnon-osat
  (testing "Patching multiple values of ahato"
    (test-patch-of-aiemmin-hankittu-osa
      ahato-path
      test-data/ahato-data
      test-data/multiple-ahato-values-patched
      assert-ahato-data-is-patched-correctly)))

(deftest post-and-get-aiemmin-hankitut-paikalliset-tutkinnon-osat
  (testing "POST ahpto and then get the created ahpto"
    (hoks-utils/test-post-and-get-of-aiemmin-hankittu-osa
      ahpto-path test-data/ahpto-data)))

(defn- assert-ahpto-data-is-patched-correctly [updated-data old-data]
  (is (= (:tavoitteet-ja-sisallot updated-data) "Muutettu tavoite."))
  (is (= (:nimi updated-data) (:nimi old-data)))
  (eq (:tarkentavat-tiedot-osaamisen-arvioija updated-data)
      (:tarkentavat-tiedot-osaamisen-arvioija
        test-data/multiple-ahpto-values-patched))
  (eq (utils/dissoc-uuids
        (first (:tarkentavat-tiedot-naytto updated-data)))
      (first (:tarkentavat-tiedot-naytto
               test-data/multiple-ahpto-values-patched)))
  (hoks-utils/compare-tarkentavat-tiedot-naytto-values
    updated-data test-data/multiple-ahpto-values-patched first))

(deftest patch-aiemmin-hankittu-paikalliset-tutkinnon-osat
  (testing "Patching multiple values of ahpto"
    (test-patch-of-aiemmin-hankittu-osa
      ahpto-path
      test-data/ahpto-data
      test-data/multiple-ahpto-values-patched
      assert-ahpto-data-is-patched-correctly)))
