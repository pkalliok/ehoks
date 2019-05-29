SELECT 
  o.oid, o.nimi, 
  oo.oid AS opiskeluoikeus_oid, oo.oppilaitos_oid, oo.koulutustoimija_oid,
  oo.tutkinto, oo.osaamisala
FROM oppijat AS o
  LEFT OUTER JOIN opiskeluoikeudet AS oo
    ON (o.oid = oo.oppija_oid)
  WHERE 
    ((oo.oppilaitos_oid IS NOT NULL AND oo.oppilaitos_oid LIKE ?) OR 
     (oo.koulutustoimija_oid IS NOT NULL AND oo.koulutustoimija_oid LIKE ?)) AND
    o.nimi ILIKE ? AND
    oo.tutkinto ILIKE ? AND
    oo.osaamisala ILIKE ?
  ORDER BY :column :desc
  LIMIT ?
  OFFSET ?