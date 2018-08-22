(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]))

(defn search-qualification-info [name]
  (-> (client/get (format "%s/perusteet/info" (:eperusteet-url config))
                  {:query-params {"nimi" name}})
      :body
      (cheshire/parse-string true)
      :data))
