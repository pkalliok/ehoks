SELECT kyselylinkki, tyyppi, alkupvm
    FROM kyselylinkit
    WHERE oppija_oid = ?
      AND alkupvm <= now()