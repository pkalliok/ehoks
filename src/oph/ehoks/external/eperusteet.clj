(ns oph.ehoks.external.eperusteet
  (:require [oph.ehoks.external.connection :as c]
            [oph.ehoks.external.cache :as cache]
            [oph.ehoks.external.oph-url :as u]
            [com.rpl.specter :as spc :refer [ALL NONE FIRST]]))

(defn map-perusteet
  "Map perusteet values"
  [values]
  (map
    (fn [v]
      (-> (select-keys v [:id :nimi :osaamisalat :tutkintonimikkeet])
          (update :nimi select-keys [:fi :en :sv])
          (update :osaamisalat (fn [x] (map #(select-keys % [:nimi]) x)))
          (update :tutkintonimikkeet
                  (fn [x] (map #(select-keys % [:nimi]) x)))))
    values))

(def asteikkomuunnos
  "Scale tranformations"
  {:1 {:1 ""}
   :2 {:2 "1" :3 "3" :4 "5"}
   :3 {:5 "1" :7 "3" :9 "5"}})

(defn- adjust-osaamistaso
  "Convert osaamistaso according to values in asteikkomuunnos"
  [asteikko osaamistaso]
  (get-in asteikkomuunnos [(keyword asteikko) (keyword osaamistaso)]))

(defn- remove-empty-kriteerit
  "Remove empty kriteerit from several subfields"
  [values]
  (spc/setval [ALL :arviointi :arvioinninKohdealueet ALL
               :arvioinninKohteet ALL :osaamistasonKriteerit ALL
               #(empty? (:kriteerit %))]
              NONE values))

(defn- adjust-osaamistaso-based-on-asteikko
  "Adjusts the osaamistaso in each appropriate spot in object"
  [asteikko values]
  (spc/transform [ALL :arviointi :arvioinninKohdealueet ALL
                  :arvioinninKohteet ALL :osaamistasonKriteerit
                  ALL :_osaamistaso]
                 #(adjust-osaamistaso asteikko %) values))

(defn adjust-tutkinnonosa-arviointi
  "Adjusts osaamistasonKriteerit based on the osaamistaso of the tutkinnonosa"
  [values]
  ; Every tutkinnonosa should currently have the same arviointiAsteikko
  ; for all of its arvioinninKohteet
  (let [asteikko (spc/select-first
                   [ALL :arviointi :arvioinninKohdealueet ALL
                    :arvioinninKohteet FIRST :_arviointiAsteikko] values)]
    (->> values
         (remove-empty-kriteerit)
         (adjust-osaamistaso-based-on-asteikko asteikko))))

(defn- get-peruste-by-id
  "Get peruste by ID. Uses eperusteet external api."
  [^Long id]
  (let [result (c/with-api-headers
                 {:method :get
                  :service (u/get-url "eperusteet-service-url")
                  :url (u/get-url "eperusteet-service.external-api.find-peruste"
                                  id)
                  :options {:as :json}})]
    (:body result)))

(defn find-perusteet-external
  "Find perusteet using eperusteet external api. Returns eperusteet response
   body as is."
  [query-params]
  (let [result (c/with-api-headers
                 {:method :get
                  :service (u/get-url "eperusteet-service-url")
                  :url
                  (u/get-url "eperusteet-service.external-api.find-perusteet")
                  :options {:as :json
                            :query-params (merge {:poistuneet true}
                                                 query-params)}})]
    (:body result)))

(defn get-koulutuksenOsa-by-koodiUri
  "Search for perusteet that match a koodiUri. Uses eperusteet external api."
  [^String koodiUri]
  (let [data (:data (find-perusteet-external {:koodi koodiUri}))]
    (when (empty? (seq data))
      (throw (ex-info (str "eperusteet not found with koodiUri " koodiUri)
                      {:status 404})))
    (let [id (:id (first data))
          peruste (get-peruste-by-id id)
          koulutuksenOsat (:koulutuksenOsat peruste)
          koulutuksenOsa
          (filter #(= koodiUri (get-in % [:nimiKoodi :uri])) koulutuksenOsat)
          ;; TODO: tarvittava id UI:n ePerusteet urlia varten
          ;; ATM lähetetään vain 12345
          koulutuksenOsaPeruste
          (map
            (fn [v]
              (-> (select-keys v [:id :osaamisalat])
                  (assoc :nimi (get-in v [:nimiKoodi :nimi]))
                  (update :nimi select-keys [:fi :en :sv])
                  (update
                    :osaamisalat (fn [x] (map #(select-keys % [:nimi]) x)))
                  (assoc :koulutuksenOsaId "12345")))
            koulutuksenOsa)]
      koulutuksenOsaPeruste)))

(defn search-perusteet-info
  "Search for perusteet that match a particular name"
  [nimi]
  (:data (find-perusteet-external {:nimi nimi})))

(defn find-tutkinnon-osat
  "Find tutkinnon osat by koodi URL"
  [^String koodi-uri]
  (let [found-perusteet (:data (find-perusteet-external {:koodi koodi-uri}))
        all-tutkinnonosat
        (flatten
          (map #(:tutkinnonOsat (get-peruste-by-id (:id %)))
               found-perusteet))]
    (filter #(= (:koodiUri %) koodi-uri)
            all-tutkinnonosat)))

(defn get-tutkinnon-osa-viitteet
  "Get tutkinnon osa viitteet by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-tutkinnonosa-viitteet" id)
       :options {:as :json}})
    :body))

(defn get-tutkinnon-osan-osa-alueet
  "Get tutkinnon osa-alueet by ID"
  [^Long id]
  (get
    (cache/with-cache!
      {:method :get
       :service (u/get-url "eperusteet-service-url")
       :url (u/get-url "eperusteet-service.get-tutkinnonosa-osa-alueet" id)
       :options {:as :json}})
    :body))

(defn find-tutkinto
  "Get perusteet by diaari number"
  [^String diaarinumero]
  (let [data (:data (find-perusteet-external {:diaarinumero diaarinumero}))
        matching (filter #(= (:diaarinumero %) diaarinumero) data)]
    (if (empty? (seq matching))
      (throw (ex-info "HTTP Exception" {:status 404}))
      (first matching))))

(defn- get-suoritustavat
  [^Long id ^String suoritustapakoodi]
  (filter #(= (:suoritustapakoodi %) suoritustapakoodi)
          (:suoritustavat (get-peruste-by-id id))))

(defn get-rakenne
  "Get rakenne by peruste ID and suoritustapakoodi"
  [^Long id ^String suoritustapakoodi]
  (let [has-rakenne (filter #(contains? % :rakenne)
                            (get-suoritustavat id suoritustapakoodi))]
    (if (some? (seq has-rakenne))
      (:rakenne (first has-rakenne))
      (throw (ex-info "HTTP Exception" {:status 404})))))
