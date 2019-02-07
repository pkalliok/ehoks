(ns oph.ehoks.hoks.handler
  (:require [compojure.api.sweet :as c-api]
            [ring.util.http-response :as response]
            [oph.ehoks.schema :as schema]
            [oph.ehoks.hoks.schema :as hoks-schema]
            [oph.ehoks.restful :as rest]
            [oph.ehoks.db.memory :as db]
            [oph.ehoks.external.koodisto :as koodisto]
            [oph.ehoks.schema.generator :as g]
            [schema.core :as s])
  (:import (java.time LocalDate)))

(def ^:private puuttuva-paikallinen-tutkinnon-osa
  (c-api/context "/:hoks-id/puuttuva-paikallinen-tutkinnon-osa" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan paikallisen tutkinnon osan"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/PaikallinenTutkinnonOsa)
      (rest/rest-ok (db/get-ppto-by-id id)))

    (c-api/POST
      "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan paikallisen
 tutkinnon osan"
      :body
      [ppto hoks-schema/PaikallinenTutkinnonOsaLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ppto-response (db/create-ppto! ppto)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request)
                                    (:id ppto-response))})))

    (c-api/PUT
      "/:id"
      []
      :summary "Päivittää HOKSin puuttuvan paikallisen
tutkinnon osan"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PaikallinenTutkinnonOsaPaivitys]
      (if (db/update-ppto! id values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))

    (c-api/PATCH
      "/:id"
      []
      :summary
      "Päivittää HOKSin puuttuvan paikallisen tutkinnon
  osan arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PaikallinenTutkinnonOsaKentanPaivitys]
      (if (db/update-ppto-values! id values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))))

(def ^:private puuttuva-ammatillinen-osaaminen
  (c-api/context "/:hoks-id/puuttuva-ammatillinen-osaaminen" [hoks-id]

    (c-api/GET "/:id" [:as id]
      :summary "Palauttaa HOKSin puuttuvan ammatillisen
osaamisen"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/PuuttuvaAmmatillinenOsaaminen)
      (rest/rest-ok (db/get-ppao-by-id id)))

    (c-api/POST
      "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan ammatillisen
  osaamisen HOKSiin"
      :body
      [ppao hoks-schema/PuuttuvaAmmatillinenOsaaminenLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ppao-response (db/create-ppao! ppao)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request)
                                    (:id ppao-response))})))

    (c-api/PUT
      "/:id" []
      :summary "Päivittää HOKSin puuttuvan ammatillisen
    osaamisen"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/PuuttuvaAmmatillinenOsaaminenPaivitys]
      (if (db/update-ppao! id values)
        (response/no-content)
        (response/not-found "PPAO not found with given PPAO ID")))

    (c-api/PATCH "/:id" []
      :summary   "Päivittää HOKSin puuttuvan ammatillisen
        osaamisen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/PuuttuvaAmmatillinenOsaaminenKentanPaivitys]
      (if (db/update-ppao-values! id values)
        (response/no-content)
        (response/not-found  "PPAO not found with given PPAO ID")))))

(def ^:private puuttuvat-yhteisen-tutkinnon-osat
  (c-api/context "/:hoks-id/puuttuvat-yhteisen-tutkinnon-osat" [hoks-id]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin puuttuvat yhteisen tutkinnon osat	"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/PuuttuvaYTO)
      (rest/rest-ok (db/get-pyto-by-id id)))

    (c-api/POST "/" [:as request]
      :summary
      "Luo (tai korvaa vanhan) puuttuvan yhteisen tutkinnon osat HOKSiin"
      :body
      [pyto hoks-schema/PuuttuvaYTOLuonti]
      :return (rest/response schema/POSTResponse)
      (let [pyto-response (db/create-pyto! pyto)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request)
                                    (:id pyto-response))})))

    (c-api/PUT
      "/:id" []
      :summary "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/PuuttuvaYTOPaivitys]
      (if (db/update-pyto! id values)
        (response/no-content)
        (response/not-found "PYTO not found with given PYTO ID")))

    (c-api/PATCH
      "/:id" []
      :summary
      "Päivittää HOKSin puuttuvan yhteisen tutkinnon osat arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/PuuttuvaYTOKentanPaivitys]
      (if (db/update-pyto-values! id values)
        (response/no-content)
        (response/not-found "PPTO not found with given PPTO ID")))))

(def ^:private opiskeluvalmiuksia-tukevat-opinnot
  (c-api/context "/:hoks-id/opiskeluvalmiuksia-tukevat-opinnot" [hoks-id]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin opiskeluvalmiuksia tukevat opinnot	"
      :path-params [id :- s/Int]
      :return (rest/response
                hoks-schema/OpiskeluvalmiuksiaTukevatOpinnot)
      (rest/rest-ok (db/get-ovatu-by-id id)))

    (c-api/POST "/"  [:as request]
      :summary
      "Luo (tai korvaa vanhan) opiskeluvalmiuksia tukevat opinnot HOKSiin"
      :body
      [ovatu hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotLuonti]
      :return (rest/response schema/POSTResponse)
      (let [ovatu-response (db/create-ovatu! ovatu)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request)
                                    (:id ovatu-response))})))

    (c-api/PUT
      "/:id" []
      :summary "Päivittää HOKSin opiskeluvalmiuksia tukevat opinnot"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotPaivitys]
      (if (db/update-ovatu! id values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))

    (c-api/PATCH
      "/:id" []
      :summary
      "Päivittää HOKSin opiskeluvalmiuksia tukevat opintojen arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body
      [values hoks-schema/OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys]
      (if (db/update-ovatu-values! id values)
        (response/no-content)
        (response/not-found "OVATU not found with given OVATU ID")))))

(def routes
  (c-api/context "/hoks" []
    :tags ["hoks"]

    (c-api/GET "/:id" [id]
      :summary "Palauttaa HOKSin"
      :path-params [id :- s/Int]
      :return (rest/response hoks-schema/HOKS)
      (rest/rest-ok (db/get-hoks-by-id id)))

    (c-api/POST "/" [:as request]
      :summary "Luo uuden HOKSin"
      :body [hoks hoks-schema/HOKSLuonti]
      :return (rest/response schema/POSTResponse)
      (let [h (db/create-hoks! hoks)]
        (rest/rest-ok {:uri (format "%s/%d" (:uri request) (:id h))})))

    (c-api/PUT "/:id" [id]
      :summary "Päivittää olemassa olevaa HOKSia"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HOKSPaivitys]
      (if (db/update-hoks! id values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    (c-api/PATCH "/:id" []
      :summary "Päivittää olemassa olevan HOKSin arvoa tai arvoja"
      :path-params [id :- s/Int]
      :body [values hoks-schema/HOKSKentanPaivitys]
      (if (db/update-hoks-values! id values)
        (response/no-content)
        (response/not-found "HOKS not found with given eHOKS ID")))

    puuttuva-ammatillinen-osaaminen
    puuttuva-paikallinen-tutkinnon-osa
    puuttuvat-yhteisen-tutkinnon-osat
    opiskeluvalmiuksia-tukevat-opinnot))
