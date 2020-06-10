(ns oph.ehoks.mocked-routes.mock-eperusteet-routes)

(def routes
  (GET "/eperusteet-service/api/perusteet" request..0
       (json-response-file
         "dev-routes/eperusteet_api_perusteet.json"))

  (GET "/eperusteet-amosaa-service/api/julkinen/koodi/:koodi" request
       (if (= (get-in request [:params :koodi])
              "paikallinen_tutkinnonosa_1.2.246.562.10.41253773158_1983")
         (json-response-file
           "dev-routes/eperusteet-amosaa-service_api_julkinen_koodi_paikallinen__tutkinnonosa__1.2.246.562.10.41253773158__1983.json")
         (json-response [])))

  (GET "/eperusteet-service/api/tutkinnonosat/52824/viitteet" []
       (json-response-file
         "dev-routes/eperusteet-service_api_tutkinnonosat_52824_viitteet.json"))

  (GET "/eperusteet-service/api/perusteet/diaari" []
       (json-response-file
         "dev-routes/eperusteet-service_api_perusteet_diaari.json"))

  (GET "/eperusteet-service/api/perusteet/3397335/suoritustavat/reformi/rakenne" []
       (json-response-file
         "dev-routes/eperusteet-service_api_perusteet_3397335_suoritustavat_reformi_rakenne.json"))

  (GET "/eperusteet-service/api/perusteet/1352660/suoritustavat/ops/rakenne" []
       (json-response-file
         "dev-routes/eperusteet-service_api_perusteet_1352660_suoritustavat_ops_rakenne.json"))

  (GET "/eperusteet-service/api/tutkinnonosat" request
       (if (= (get-in request [:query-params "koodiUri"]) "tutkinnonosat_101056")
         (json-response-file
           "dev-routes/eperusteet-service_api_tutkinnonosat_not_found.json")
         (json-response-file
           "dev-routes/eperusteet-service_api_tutkinnonosat.json"))))
