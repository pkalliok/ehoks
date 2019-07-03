(ns oph.ehoks.user-test
  (:require [clojure.test :refer [deftest testing is]]
            [oph.ehoks.utils :refer [eq]]
            [oph.ehoks.user :as user]
            [oph.ehoks.external.http-client :as client]))

(deftest get-auth-info-test
  (testing "Mapping kayttooikeus-service data to eHOKS privileges"
    (eq (user/get-auth-info
          {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                            :kayttooikeudet [{:palvelu "EHOKS"
                                              :oikeus "CRUD"}]}
                           {:organisaatioOid "1.2.246.562.10.00000000001"
                            :kayttooikeudet [{:palvelu "EHOKS"
                                              :oikeus "OPHPAAKAYTTAJA"}]}]})
        {:organisation-privileges
         '({:oid "1.2.246.562.10.00000000002"
            :privileges #{:read :write :update :delete}
            :roles #{}}
            {:oid "1.2.246.562.10.00000000001"
             :privileges #{:read :write :update :delete}
             :roles #{:oph-super-user}})})

    (eq (user/get-auth-info
          {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                            :kayttooikeudet [{:palvelu "EHOKS"
                                              :oikeus "READ"}]}]})
        {:organisation-privileges
         '({:oid "1.2.246.562.10.00000000002"
            :privileges #{:read}
            :roles #{}})})

    (eq (user/get-auth-info
          {:organisaatiot [{:organisaatioOid "1.2.246.562.10.00000000002"
                            :kayttooikeudet [{:palvelu "SERVICE"
                                              :oikeus "CRUD"}]}]})
        {:organisation-privileges
         '({:oid "1.2.246.562.10.00000000002"
            :privileges #{}
            :roles #{}})})))

(deftest oph-super-user
  (testing "Check if user is OPH super user"
    (is (user/oph-super-user?
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000003"
              :privileges #{}
              :roles #{}}
              {:oid "1.2.246.562.10.00000000001"
               :privileges #{}
               :roles #{:oph-super-user}}
              {:oid "1.2.246.562.10.00000000002"
               :privileges #{}
               :roles #{}})}))
    (is (not
          (user/oph-super-user?
            {:organisation-privileges
             '({:oid "1.2.246.562.10.00000000001"
                :privileges #{}
                :roles #{}})})))))

(deftest get-organisation-privileges
  (testing "Get organisation privileges"
    (client/set-get!
      (fn [url options]
        (cond
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000003")
          {:status 200
           :body {:parentOidPath
                  "|1.2.246.562.10.00000000001|1.2.246.562.10.00000000002"}}
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000001")
          {:status 200
           :body {}}
          (.endsWith
            url "/rest/organisaatio/v4/1.2.246.562.10.00000000002")
          {:status 200
           :body {:parentOidPath
                  "|1.2.246.562.10.00000000001|"}})))

    (eq (user/get-organisation-privileges
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read :write :update :delete}
              :roles #{}}
              {:oid "1.2.246.562.10.00000000001"
               :privileges #{}
               :roles #{:oph-super-user}})}
          "1.2.246.562.10.00000000002")
        #{:read :write :update :delete})

    (is (nil?
          (user/get-organisation-privileges
            {:organisation-privileges
             '({:oid "1.2.246.562.10.00000000002"
                :privileges #{:read :write :update :delete}
                :roles #{}}
                {:oid "1.2.246.562.10.00000000003"
                 :privileges #{}
                 :roles #{:oph-super-user}})}
            "1.2.246.562.10.00000000001")))

    ; From parentOidPath
    (eq (user/get-organisation-privileges
          {:organisation-privileges
           '({:oid "1.2.246.562.10.00000000002"
              :privileges #{:read :write :update :delete}
              :roles #{}})}
          "1.2.246.562.10.00000000003")
        #{:read :write :update :delete})
    (client/reset-functions!)))
