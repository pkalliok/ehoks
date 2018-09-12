(ns oph.ehoks.external.connection
  (:require [oph.ehoks.config :refer [config]]
            [clj-http.client :as client]
            [cheshire.core :as cheshire]
            [clj-time.core :as t])
  (:import [com.fasterxml.jackson.core JsonParseException]))

(defonce service-ticket
  (atom {:url nil
         :expires nil}))

(defn get-service-ticket-url []
  (get-in
    (client/post (:cas-service-ticket-url config)
                 {:form-params {:username (:cas-username config)
                                :password (:cas-password config)}})
    [:headers "location"]))

(defn add-cas-ticket [url data]
  (when (or (nil? (:url @service-ticket))
            (t/after? (t/now) (:expires @service-ticket)))
    (reset! service-ticket
            {:url (get-service-ticket-url)
             :expires (t/plus (t/now) (t/hours 2))}))
  (let [ticket (:body (client/post (:url @service-ticket)
                                   {:form-params {:service url}}))]
    (-> data
        (assoc-in
          [:headers "clientSubSystemCode"] (:client-sub-system-code config))
        (assoc-in [:query-params :ticket] ticket))))

(defn api-get [url data]
  (let [response
        (client/get url (add-cas-ticket url data))]
    (try
      (update response :body cheshire/parse-string true)
      (catch JsonParseException _ response))))
