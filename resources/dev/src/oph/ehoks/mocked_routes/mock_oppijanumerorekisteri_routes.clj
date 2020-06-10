(ns oph.ehoks.mocked-routes.mock-oppijanumerorekisteri-routes
  (:require [ring.util.http-response :as response]
            [oph.ehoks.mock-gen :as mock-gen]))

(def routes
  (GET "/oppijanumerorekisteri-service/henkilo" request
       (json-response
         {:results
          [{:oidHenkilo "1.2.246.562.24.44651722625"
            :hetu "250103-5360"
            :etunimet "Aarto Maurits"
            :kutsumanimi "Aarto"
            :sukunimi "Väisänen-perftest"}]}))

  (GET "/oppijanumerorekisteri-service/henkilo/1.2.246.562.24.44651722625" []
       (json-response
         {:oidHenkilo "1.2.246.562.24.44651722625"
          :hetu "250103-5360"
          :etunimet "Aarto Maurits"
          :kutsumanimi "Aarto"
          :sukunimi "Väisänen-perftest"
          :yhteystiedotRyhma
          '({:id 0
             :readOnly true
             :ryhmaAlkuperaTieto "testiservice"
             :ryhmaKuvaus "testiryhmä"
             :yhteystieto
             [{:yhteystietoArvo "kayttaja@domain.local"
               :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})}))

  (GET "/oppijanumerorekisteri-service/henkilo/1.2.246.562.24.00000000000" []
       (response/not-found))

  (GET "/oppijanumerorekisteri-service/henkilo/:oid" request
       (let [first-name (mock-gen/generate-first-name)]
         (json-response
           {:oidHenkilo (get-in request [:params :oid])
            :hetu "250103-5360"
            :etunimet (format "%s %s" first-name (mock-gen/generate-first-name))
            :kutsumanimi first-name
            :sukunimi (mock-gen/generate-last-name)
            :yhteystiedotRyhma
            '({:id 0
               :readOnly true
               :ryhmaAlkuperaTieto "testiservice"
               :ryhmaKuvaus "testiryhmä"
               :yhteystieto
               [{:yhteystietoArvo "kayttaja@domain.local"
                 :yhteystietoTyyppi "YHTEYSTIETO_SAHKOPOSTI"}]})}))))
