(ns oph.ehoks.virkailija.system-handler
  (:require [compojure.api.sweet :as c-api]
            [compojure.api.core :refer [route-middleware]]
            [oph.ehoks.virkailija.middleware :as m]
            [oph.ehoks.restful :as restful]
            [oph.ehoks.virkailija.schema :as virkailija-schema]
            [oph.ehoks.external.cache :as c]
            [oph.ehoks.oppijaindex :as op]
            [clojure.core.async :as a]
            [ring.util.http-response :as response]))

(def routes
  (route-middleware
    [m/wrap-oph-super-user]

    (c-api/GET "/system-info" []
      :summary "Järjestelmän tiedot"
      :return (restful/response virkailija-schema/SystemInfo)
      (let [runtime (Runtime/getRuntime)]
        (restful/rest-ok
          {:cache {:size (c/size)}
           :memory {:total (.totalMemory runtime)
                    :free (.freeMemory runtime)
                    :max (.maxMemory runtime)}
           :oppijaindex
           {:unindexedOppijat
            (op/get-oppijat-without-index-count)
            :unindexedOpiskeluoikeudet
            (op/get-opiskeluoikeudet-without-index-count)
            :unindexedTutkinnot
            (op/get-opiskeluoikeudet-without-tutkinto-count)}})))

    (c-api/POST "/index" []
      :summary "Indeksoi oppijat ja opiskeluoikeudet"
      (a/go
        (op/update-oppijat-without-index!)
        (op/update-opiskeluoikeudet-without-index!)
        (op/update-opiskeluoikeudet-without-tutkinto!)
        (response/ok)))

    (c-api/DELETE "/cache" []
      :summary "Välimuistin tyhjennys"
      (c/clear-cache!)
      (response/ok))))