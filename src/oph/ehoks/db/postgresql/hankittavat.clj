(ns oph.ehoks.db.postgresql.hankittavat
  (:require [oph.ehoks.db.db-operations.hoks :as h]
            [oph.ehoks.db.queries :as queries]
            [oph.ehoks.db.db-operations.db-helpers :as db-ops]))

(defn select-tyopaikalla-jarjestettava-koulutus-by-id
  "Työpaikalla järjestettävä koulutus"
  [id]
  (first
    (db-ops/query
      [queries/select-tyopaikalla-jarjestettavat-koulutukset-by-id id]
      {:row-fn h/tyopaikalla-jarjestettava-koulutus-from-sql})))

(defn select-tyotehtavat-by-tho-id
  "Työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [id]
  (db-ops/query
    [queries/select-tyotehtavat-by-tho-id id]
    {:row-fn h/tyotehtava-from-sql}))

(defn select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id
  "Muut oppimisympäristöt osaamisen hankkimistavalle"
  [id]
  (db-ops/query
    [queries/select-muut-oppimisymparistot-by-osaamisen-hankkimistapa-id id]
    {:row-fn h/muu-oppimisymparisto-from-sql}))

(defn select-osaamisen-osoittamiset-by-ppto-id
  "Hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-ppto-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-osaamisen-hankkimistavat-by-hpto-id
  "Hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/query
    [queries/select-osaamisen-hankkmistavat-by-ppto-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn select-hankittava-paikallinen-tutkinnon-osa-by-id
  "Hankittava paikallisen tutkinnon osa"
  [id]
  (first
    (db-ops/query
      [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-id id]
      {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql})))

(defn select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
  "Hankittavat paikallisen tutkinnon osat"
  [id]
  (db-ops/query
    [queries/select-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-paikallinen-tutkinnon-osa-from-sql}))

(defn select-osaamisen-hankkimistavat-by-hato-id
  "Hankittavan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/query
    [queries/select-osaamisen-hankkmistavat-by-hato-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn select-osaamisen-osoittamiset-by-hato-id
  "Hankittavan ammatillisen tutkinnon osan osaamisen näytöt"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-hato-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-osaamisen-hankkimistavat-by-hyto-osa-alue-id
  "Hankittavan yhteisen tutkinnon osan osa-alueen osaamisen hankkimistavat"
  [id]
  (db-ops/query
    [queries/select-osaamisen-hankkimistavat-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-hankkimistapa-from-sql}))

(defn select-osaamisen-osoittamiset-by-yto-osa-alue-id
  "Hankittavan yhteisen tutkinnon osan osa-alueen osaamisen näytöt"
  [id]
  (db-ops/query
    [queries/select-osaamisen-osoittamiset-by-yto-osa-alue-id id]
    {:row-fn h/osaamisen-osoittaminen-from-sql}))

(defn select-yto-osa-alueet-by-yto-id
  "Yhteisen tutkinnon osan osa-alueet"
  [id]
  (db-ops/query
    [queries/select-yto-osa-alueet-by-yto-id id]
    {:row-fn h/yhteisen-tutkinnon-osan-osa-alue-from-sql}))

(defn select-hankittava-ammat-tutkinnon-osa-by-id
  "Hankittavan ammatillisen tutkinnon osa"
  [id]
  (->
    (db-ops/query
      [queries/select-hankittavat-ammat-tutkinnon-osat-by-id id])
    first
    h/hankittava-ammat-tutkinnon-osa-from-sql))

(defn select-hankittavat-ammat-tutkinnon-osat-by-hoks-id
  "Hankittavat ammatillisen tutkinnon osat"
  [id]
  (db-ops/query
    [queries/select-hankittavat-ammat-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-ammat-tutkinnon-osa-from-sql}))

(defn select-hankittava-yhteinen-tutkinnon-osa-by-id
  "Hankittava yhteisen tutkinnon osa"
  [hyto-id]
  (->
    (db-ops/query [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-id
                   hyto-id])
    first
    h/hankittava-yhteinen-tutkinnon-osa-from-sql))

(defn select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
  "Hankittavat yhteisen tutkinnon osat"
  [id]
  (db-ops/query
    [queries/select-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id id]
    {:row-fn h/hankittava-yhteinen-tutkinnon-osa-from-sql}))

(defn insert-tho-tyotehtavat!
  "Lisää työpaikalla hankittavan osaamisen keskeiset työtehtävät"
  [tho c]
  (db-ops/insert-multi!
    :tyopaikalla_jarjestettavan_koulutuksen_tyotehtavat
    (map
      #(hash-map
         :tyopaikalla_jarjestettava_koulutus_id (:id tho)
         :tyotehtava %)
      c)))

(defn insert-tyopaikalla-jarjestettava-koulutus!
  "Lisää työpaikalla järjestettävä koulutus"
  [o]
  (when (some? o)
    (let [o-db (db-ops/insert-one!
                 :tyopaikalla_jarjestettavat_koulutukset
                 (h/tyopaikalla-jarjestettava-koulutus-to-sql o))]
      (insert-tho-tyotehtavat! o-db (:keskeiset-tyotehtavat o))
      o-db)))

(defn insert-osaamisen-hankkimistapa!
  "Lisää osaamisen hankkimistapa"
  [oh]
  (db-ops/insert-one!
    :osaamisen_hankkimistavat
    (h/osaamisen-hankkimistapa-to-sql oh)))

(defn insert-osaamisen-hankkimistavan-muut-oppimisymparistot!
  "Lisää osaamisen hankkimistavan muut oppimisympäristöt"
  [oh c]
  (db-ops/insert-multi!
    :muut_oppimisymparistot
    (map
      #(db-ops/to-sql
         (assoc % :osaamisen-hankkimistapa-id (:id oh)))
      c)))

(defn insert-hankittavan-paikallisen-tutkinnon-osan-osaamisen-hankkimistapa!
  "Lisää hankittavan paikallisen tutkinnon osan osaamisen hankkimistapa"
  [ppto oh]
  (db-ops/insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_hankkimistapa_id (:id oh)}))

(defn delete-osaamisen-hankkimistavat-by-hpto-id!
  "Poista hankittavan paikallisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn insert-hpto-osaamisen-osoittaminen!
  "Lisää hankittavan paikallisen tutkinnon osan hankitun osaamisen näyttö"
  [ppto h]
  (db-ops/insert-one!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    {:hankittava_paikallinen_tutkinnon_osa_id (:id ppto)
     :osaamisen_osoittaminen_id (:id h)}))

(defn delete-osaamisen-osoittamiset-by-ppto-id!
  "Poista hankittavan paikallisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_paikallisen_tutkinnon_osan_naytto
    ["hankittava_paikallinen_tutkinnon_osa_id = ?" id]))

(defn update-hankittava-paikallinen-tutkinnon-osa-by-id!
  "Päivitä hankittavan paikallisen tutkinnon osa"
  [id m]
  (db-ops/update!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn insert-yto-osa-alueen-osaamisen-osoittaminen!
  "Lisää yhteisen tutkinnon osan osa-alueen osaamisen näytöt"
  [yto-id naytto-id]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_naytot
    {:yhteisen_tutkinnon_osan_osa_alue_id yto-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-hankittava-paikallinen-tutkinnon-osa!
  "Lisää hankittavan paikallisen tutkinnon osa"
  [m]
  (db-ops/insert-one!
    :hankittavat_paikalliset_tutkinnon_osat
    (h/hankittava-paikallinen-tutkinnon-osa-to-sql m)))

(defn insert-hankittavan-ammat-tutkinnon-osan-osaamisen-hankkimistapa!
  "Lisää hankittavan ammatillisen tutkinnon osan osaamisen hankkimistapa"
  [pato-id oh-id]
  (db-ops/insert-one!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    {:hankittava_ammat_tutkinnon_osa_id pato-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn insert-hato-osaamisen-osoittaminen!
  "Lisää hankittavan ammatillisen tutkinnon osan osaamisen näyttö"
  [hato-id naytto-id]
  (db-ops/insert-one!
    :hankittavan_ammat_tutkinnon_osan_naytto
    {:hankittava_ammat_tutkinnon_osa_id hato-id
     :osaamisen_osoittaminen_id naytto-id}))

(defn insert-hankittava-ammat-tutkinnon-osa!
  "Lisää hankittavan ammatillisen tutkinnon osa"
  [m]
  (db-ops/insert-one!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)))

(defn delete-osaamisen-hankkimistavat-by-hato-id!
  "Poista hankittavan ammatillisen tutkinnon osan osaamisen hankkimistavat"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_osaamisen_hankkimistavat
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn delete-osaamisen-osoittamiset-by-pato-id!
  "Poista hankittavan ammatillisen tutkinnon osan hankitun osaamisen näytöt"
  [id]
  (db-ops/shallow-delete!
    :hankittavan_ammat_tutkinnon_osan_naytto
    ["hankittava_ammat_tutkinnon_osa_id = ?" id]))

(defn update-hankittava-ammat-tutkinnon-osa-by-id!
  "Päivitä hankittavan ammatillisen tutkinnon osa"
  [id m]
  (db-ops/update!
    :hankittavat_ammat_tutkinnon_osat
    (h/hankittava-ammat-tutkinnon-osa-to-sql m)
    ["id = ? AND deleted_at IS NULL" id]))

(defn insert-hyto-osa-alueen-osaamisen-hankkimistapa!
  "Lisää hankittavan yhteisen tutkinnon osan osa-alueen osaamisen hankkimistapa"
  [hyto-osa-alue-id oh-id]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat
    {:yhteisen_tutkinnon_osan_osa_alue_id hyto-osa-alue-id
     :osaamisen_hankkimistapa_id oh-id}))

(defn insert-yhteisen-tutkinnon-osan-osa-alue!
  "Lisää yhteisen tutkinnon osan osa-alue"
  [osa-alue]
  (db-ops/insert-one!
    :yhteisen_tutkinnon_osan_osa_alueet
    (h/yhteisen-tutkinnon-osan-osa-alue-to-sql osa-alue)))

(defn insert-hankittava-yhteinen-tutkinnon-osa!
  "Lisää hankittavan yhteisen tutkinnon osa"
  [m]
  (db-ops/insert-one!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql m)))

(defn delete-hyto-osa-alueet!
  "Poista hankittavan yhteisen tutkinnon osan osa-alueet"
  [hyto-id]
  (db-ops/shallow-delete!
    :yhteisen_tutkinnon_osan_osa_alueet
    ["yhteinen_tutkinnon_osa_id = ?" hyto-id]))

(defn update-hankittava-yhteinen-tutkinnon-osa-by-id!
  "Päivitä hankittavan yhteisen tutkinnon osa"
  [hyto-id new-values]
  (db-ops/update!
    :hankittavat_yhteiset_tutkinnon_osat
    (h/hankittava-yhteinen-tutkinnon-osa-to-sql new-values)
    ["id = ? AND deleted_at IS NULL" hyto-id]))

(defn delete-hankittavat-ammatilliset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat ammatillisen tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_ammat_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-hankittavat-paikalliset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat paikalliset tutkinnot osat"
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_paikalliset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))

(defn delete-hankittavat-yhteiset-tutkinnon-osat-by-hoks-id
  "Poista hankittavat yhteiset tutkinnon osat"
  [hoks-id db-conn]
  (db-ops/shallow-delete!
    :hankittavat_yhteiset_tutkinnon_osat
    ["hoks_id = ?" hoks-id] db-conn))