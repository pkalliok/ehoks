ALTER TABLE hoksit ADD COLUMN osaamisen_hankkimisen_tarve BOOLEAN;

ALTER TABLE muut_oppimisymparistot DROP COLUMN lisatiedot;

ALTER TABLE osaamisen_osoittamiset ADD COLUMN
vaatimuksista_tai_tavoitteista_poikkeaminen TEXT;

ALTER TABLE osaamisen_osoittamisen_tyotehtavat RENAME TO
osaamisen_osoittamisen_sisallot;

ALTER TABLE osaamisen_osoittamisen_sisallot RENAME COLUMN
tyotehtava TO sisallon_kuvaus;

ALTER TABLE yhteisen_tutkinnon_osan_osa_alueet ADD COLUMN
olennainen_seikka BOOLEAN;

ALTER TABLE aiemmin_hankitut_yto_osa_alueet ADD COLUMN
olennainen_seikka BOOLEAN;

ALTER TABLE hankittavat_ammat_tutkinnon_osat ADD COLUMN
olennainen_seikka BOOLEAN;

ALTER TABLE hankittavat_paikalliset_tutkinnon_osat ADD COLUMN
olennainen_seikka BOOLEAN;

ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat ADD COLUMN
olennainen_seikka BOOLEAN;

CREATE TABLE osaamisen_osoittamisen_yksilolliset_kriteerit(
  id SERIAL PRIMARY KEY,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
  deleted_at TIMESTAMP WITH TIME ZONE,
  osaamisen_osoittaminen_id INTEGER REFERENCES osaamisen_osoittamiset(id),
  yksilollinen_kriteeri TEXT
);

ALTER TABLE tyoelama_arvioijat RENAME TO
tyoelama_osaamisen_arvioijat;
