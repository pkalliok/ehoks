SELECT o.* FROM osaamisen_hankkimistavat AS o
  LEFT OUTER JOIN puuttuvan_ammatillisen_tutkinnon_osan_osaamisen_hankkimistavat AS p
    ON (p.osaamisen_hankkimistapa_id = o.id)
  WHERE p.puuttuva_ammatillinen_tutkinnon_osa_id = ? AND o.deleted_at IS NULL
