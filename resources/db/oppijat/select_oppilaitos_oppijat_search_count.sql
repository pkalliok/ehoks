SELECT 
  COUNT(o.oid)
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