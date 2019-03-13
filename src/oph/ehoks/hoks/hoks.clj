(ns oph.ehoks.hoks.hoks
  (:require [oph.ehoks.db.postgresql :as db]))

(defn set-olemassa-olevat-ammatilliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-ammatilliset-tutkinnon-osat
    (db/select-olemassa-olevat-ammatilliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-hankitun-osaamisen-naytto [o]
  (let [naytot (db/select-hankitun-osaamisen-naytot-by-ppto-id (:id o))]
    (assoc
      o
      :hankitun-osaamisen-naytto
      (mapv
        #(-> %
             (assoc
               :koulutuksen-jarjestaja-arvioijat
               (db/select-koulutuksen-jarjestaja-arvioijat-by-hon-id (:id o))
               :nayttoymparisto
               (db/select-nayttoymparisto-by-id (:nayttoymparisto-id %)))
             (dissoc :nayttoymparisto-id))
        naytot))))

(defn set-puuttuvat-paikalliset-tutkinnon-osat [h]
  (let [c (db/select-puuttuvat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))]
    (assoc
      h
      :puuttuvat-paikalliset-tutkinnon-osat
      (mapv
        set-hankitun-osaamisen-naytto
        c))))

(defn set-olemassa-olevat-paikalliset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-paikalliset-tutkinnon-osat
    (db/select-olemassa-olevat-paikalliset-tutkinnon-osat-by-hoks-id (:id h))))

(defn set-olemassa-olevat-yhteiset-tutkinnon-osat [h]
  (assoc
    h
    :olemassa-olevat-yhteiset-tutkinnon-osat
    (db/select-olemassa-olevat-yhteiset-tutkinnon-osat-by-hoks-id (:id h))))

(defn get-hokses-by-oppija [oid]
  (map
    #(-> %
         set-olemassa-olevat-ammatilliset-tutkinnon-osat
         set-puuttuvat-paikalliset-tutkinnon-osat
         set-olemassa-olevat-yhteiset-tutkinnon-osat)
    (db/select-hoks-by-oppija-oid oid)))

(defn save-hoks! [h]
  (let [saved-hoks (first (db/insert-hoks! h))]
    (db/insert-puuttuvat-paikalliset-tutkinnon-osat!
      (map
        #(assoc % :hoks-id (:id saved-hoks))
        (:puuttuvat-paikalliset-tutkinnon-osat h)))))
