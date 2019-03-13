(ns oph.ehoks.db.hoks
  (:require [clojure.set :refer [rename-keys]]))

(defn convert-keys [f m]
  (rename-keys
    m
    (reduce
      (fn [c n]
        (assoc c n (f n)))
      {}
      (keys m))))

(defn to-underscore-keys [m]
  (convert-keys #(keyword (.replace (name %) \- \_)) m))

(defn to-dash-keys [m]
  (convert-keys #(keyword (.replace (name %) \_ \-)) m))

(defn- replace-in [h sk tks]
  (if (some? (get h sk))
    (dissoc (assoc-in h tks (get h sk)) sk)
    h))

(defn- remove-nils [m]
  (apply dissoc m (filter #(nil? (get m %)) (keys m))))

(defn- replace-from [h sks tk]
  (if (get-in h sks)
    (if (= (count (get-in h (drop-last sks))) 1)
      (apply
        dissoc
        (assoc h tk (get-in h sks))
        (drop-last sks))
      (update-in
        h
        (drop-last sks)
        dissoc
        (last sks)))
    h))

(defn hoks-from-sql [h]
  (-> h
      (replace-in :laatija_nimi [:laatija :nimi])
      (replace-in :hyvaksyja_nimi [:hyvaksyja :nimi])
      (replace-in :paivittaja_nimi [:paivittaja :nimi])
      remove-nils
      to-dash-keys))

(defn hoks-to-sql [h]
  (-> h
      (dissoc :olemassa-olevat-ammatilliset-tutkinnon-osat
              :olemassa-olevat-paikalliset-tutkinnon-osat
              :olemassa-olevat-yhteiset-tutkinnon-osat
              :puuttuvat-ammatilliset-tutkinnon-osat
              :puuttuvat-yhteiset-tutkinnon-osat
              :opiskeluvalmiuksia-tukevat-opinnot
              :puuttuvat-paikalliset-tutkinnon-osat)
      (update :eid #(if (nil? %) (str (java.util.UUID/randomUUID)) %)) ; generate and check, move to insert and lock
      (replace-from [:laatija :nimi] :laatija-nimi)
      (replace-from [:hyvaksyja :nimi] :hyvaksyja-nimi)
      (replace-from [:paivittaja :nimi] :paivittaja-nimi)
      to-underscore-keys))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-from-sql [m]
  (to-dash-keys m))

(defn olemassa-oleva-ammatillinen-tutkinnon-osa-to-sql [m]
  (-> m
      (dissoc :tarkentavat-tiedot-naytto :tarkentavat-tiedot-arvioija)
      remove-nils
      to-underscore-keys))

(defn puuttuva-paikallinen-tutkinnon-osa-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version :hoks_id)
      remove-nils
      to-dash-keys))

(defn puuttuva-paikallinen-tutkinnon-osa-to-sql [m]
  (-> m
      (dissoc :hankitun-osaamisen-naytto :osaamisen-hankkimistavat)
      remove-nils
      to-underscore-keys))

(defn hankitun-osaamisen-naytto-from-sql [m]
  (-> m
      (dissoc :created_at :updated_at :deleted_at :version)
      to-dash-keys))

(defn hankitun-osaamisen-naytto-to-sql [m]
  (to-underscore-keys m))

(defn nayttoymparisto-to-sql [m]
  (to-underscore-keys m))

(defn olemassa-oleva-paikallinen-tutkinnon-osa-from-sql [m]
  (to-dash-keys m))

(defn olemassa-oleva-paikallinen-tutkinnon-osa-to-sql [m]
  (-> m
      remove-nils
      to-underscore-keys))

(defn olemassa-oleva-yhteinen-tutkinnon-osa-from-sql [m]
  (-> m
      (replace-in
        :lahetetty_arvioitavaksi
        [:todennettu_arviointi_lisatiedot :lahetetty_arvioitavaksi])
      remove-nils
      to-underscore-keys))
