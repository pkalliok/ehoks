ALTER TABLE aiemmin_hankitut_ammat_tutkinnon_osat RENAME COLUMN
tarkentavat_tiedot_arvioija_id TO tarkentavat_tiedot_osaamisen_arvioija_id;

ALTER TABLE aiemmin_hankitut_paikalliset_tutkinnon_osat RENAME COLUMN
tarkentavat_tiedot_arvioija_id TO tarkentavat_tiedot_osaamisen_arvioija_id;

ALTER TABLE aiemmin_hankitut_yhteiset_tutkinnon_osat RENAME COLUMN
tarkentavat_tiedot_arvioija_id TO tarkentavat_tiedot_osaamisen_arvioija_id;