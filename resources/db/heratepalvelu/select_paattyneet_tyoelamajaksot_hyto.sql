SELECT
  h.id AS hoks_id,
  h.opiskeluoikeus_oid AS opiskeluoikeus_oid,
  h.oppija_oid AS oppija_oid,
  osa.id AS tutkinnonosa_id,
  osat.tutkinnon_osa_koodi_uri AS tutkinnonosa_nimi,
  oh.id AS hankkimistapa_id,
  oh.osaamisen_hankkimistapa_koodi_uri AS hankkimistapa_tyyppi,
  oh.alku AS alkupvm,
  oh.loppu AS loppupvm,
  tjk.tyopaikan_nimi AS tyopaikan_nimi,
  tjk.tyopaikan_y_tunnus AS tyopaikan_ytunnus,
  tjk.vastuullinen_tyopaikka_ohjaaja_nimi AS tyopaikkaohjaaja_nimi,
  tjk.vastuullinen_tyopaikka_ohjaaja_sahkoposti AS tyopaikkaohjaaja_email
FROM hoksit h
  LEFT OUTER JOIN hankittavat_yhteiset_tutkinnon_osat AS osat
    ON (h.id = osat.hoks_id AND osat.deleted_at IS NULL)
  LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueet AS osa
    ON (osat.id = osa.yhteinen_tutkinnon_osa_id)
  LEFT OUTER JOIN yhteisen_tutkinnon_osan_osa_alueen_osaamisen_hankkimistavat AS ytooh
    ON (osa.id = ytooh.yhteisen_tutkinnon_osan_osa_alue_id)
  LEFT OUTER JOIN osaamisen_hankkimistavat AS oh
    ON (ytooh.osaamisen_hankkimistapa_id = oh.id)
  LEFT OUTER JOIN tyopaikalla_jarjestettavat_koulutukset AS tjk
    ON (oh.tyopaikalla_jarjestettava_koulutus_id = tjk.id)
WHERE
  (oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_koulutussopimus' or
  oh.osaamisen_hankkimistapa_koodi_uri = 'osaamisenhankkimistapa_oppisopimus')
  AND oh.loppu >= ?
  AND oh.loppu <= ?
  AND oh.tep_kasitelty = false
LIMIT ?
