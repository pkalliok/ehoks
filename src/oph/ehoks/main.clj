(ns oph.ehoks.main
  (:gen-class)
  (:require [ring.adapter.jetty :as jetty]
            [oph.ehoks.db.migrations :as m]
            [clojure.string :refer [lower-case]]
            [clojure.tools.logging :as log]
            [oph.ehoks.common.api :as common-api]
            [oph.ehoks.ehoks-app :as ehoks-app]
            [oph.ehoks.redis :refer [redis-store]]
            [oph.ehoks.config :refer [config]]
            [environ.core :refer [env]]))

(defn has-arg? [args s]
  (some? (some #(when (= (lower-case %) s) %) args)))

(defn -main [& args]
  (cond
    (has-arg? args "--help")
    (do (println "eHOKS")
        (println "Usage: java -jar {uberjar-filename}.jar [options]")
        (println "Options:")
        (println "--run-migrations    Run migrations")
        (println "--help              Print this help"))
    (has-arg? args "--run-migrations")
    (do (println "Running migrations")
        (m/migrate!)
        0)
    :else
    (let [app-name (lower-case (:name env "both"))
          hoks-app
          (common-api/create-app
           (ehoks-app/create-app app-name)
           (when (seq (:redis-url config))
             (redis-store {:pool {}
                           :spec {:uri (:redis-url config)}})))]
      (log/infof "Starting %s" app-name)
      (log/info "Running migrations")
      (m/migrate!)
      (jetty/run-jetty hoks-app {:port (:port config)
                                 :join? true
                                 :async? true}))))