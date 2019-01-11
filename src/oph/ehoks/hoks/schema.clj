(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]
            [schema-tools.core :as st]
            [oph.ehoks.schema-tools :refer [describe modify]])
  (:import (java.time LocalDate)))

(s/defschema
  Organisaatio
  (describe
    "Organisaatio"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Organisaation nimi"
    (s/optional-key :y-tunnus) s/Str "Organisaation y-tunnus"))

(s/defschema
  KoodiMetadata
  (describe
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"
    (s/optional-key :nimi) (s/maybe s/Str) "Koodisto-koodin nimi"
    (s/optional-key :lyhyt-nimi) (s/maybe s/Str) "Koodisto-koodin lyhyt nimi"
    (s/optional-key :kuvaus) (s/maybe s/Str) "Koodisto-koodin kuvaus"
    :kieli s/Str "Koodisto-koodin kieli"))

(s/defschema
  KoodistoKoodi
  (describe
    "Koodisto-koodi"
    :koodi-arvo s/Str "Koodisto-koodin arvo"
    :koodi-uri s/Str "Koodiston URI"
    :versio s/Int "Koodisto-koodin versio"
    (s/optional-key :metadata) [KoodiMetadata]
    "Koodisto-koodin metadata, joka haetaan Koodisto-palvelusta"))

(s/defschema
  TutkinnonOsa
  (describe
    "Tutkinnon osa"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    :eperusteet-id s/Str "Tunniste ePerusteet-palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    (s/optional-key :kuvaus) s/Str
    "Tutkinnon osan kuvaus ePerusteet-palvelussa"))

(s/defschema
  YhteisenTutkinnonOsanOsa
  (describe
    "Yhteisen tutkinnon osan (YTO) osa"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :eperusteet-tunniste s/Int
    "Osan tunniste ePerusteet-palvelussa. Tunnisteen tyyppi voi vielä muuttua"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    :tunniste KoodistoKoodi "Koodisto-koodi (ammatillisenoppiaineet)"))

(s/defschema
  YhteinenTutkinnonOsa
  (describe
    "Yhteinen Tutkinnon osa (YTO)"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :tutkinnon-osat [YhteisenTutkinnonOsanOsa]
    "Yhteisen tutkinnon osan osat"
    :tunniste KoodistoKoodi "Koodisto-koodi (tutkinnonosat)"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    :eperusteet-id s/Str "Tunniste ePerusteet-palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    (s/optional-key :kuvaus) s/Str
    "Tutkinnon osan kuvaus ePerusteet-palvelussa"
    :pakollinen s/Bool "Onko tutkinnon osa pakollinen vai ei"))

(s/defschema
  MuuTutkinnonOsa
  (describe
    "Muu tutkinnon osa (ei ePerusteet-palvelussa)"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Tutkinnon osan nimi"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :laajuus s/Int "Tutkinnon osan laajuus osaamispisteissä"
    :kesto s/Int "Tutkinnon osan kesto päivinä"
    :suorituspvm LocalDate "Tutkinnon suorituspäivä muodossa YYYY-MM-DD"))

(s/defschema
  Henkilo
  (describe
    "Henkilö"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :organisaatio Organisaatio "Henkilön organisaatio"
    :nimi s/Str "Henkilön nimi"
    :rooli s/Str "Henkilön rooli"))

(s/defschema
  Aikavali
  (describe
    "Aikaväli"
    :alku LocalDate "Alkupäivämäärä muodossa YYYY-MM-DD"
    :loppu LocalDate "Loppupäivämäärä muodossa YYYY-MM-DD"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnot
  (describe
    "Opiskeluvalmiuksia tukevat opinnot"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Opintojen nimi"
    :kuvaus s/Str "Opintojen kuvaus"
    :kesto s/Int "Opintojen kesto päivinä"
    :ajankohta Aikavali "Opintojen ajoittuminen"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotLuonti
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:eid]}))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotPaivitys
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    "Opiskeluvalmiuksia tukevien opintojen tiedot merkintää ylikirjoittaessa
     (PUT)"))

(s/defschema
  OpiskeluvalmiuksiaTukevatOpinnotKentanPaivitys
  (modify
    OpiskeluvalmiuksiaTukevatOpinnot
    (str "Opiskeluvalmiuksia tukevien opintojen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :kuvaus :kesto :ajankohta]}))

(s/defschema
  TyopaikallaHankittavaOsaaminen
  (describe
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :hankkijan-edustaja Henkilo
    "Oppisopimuskoulutusta hankkineen koulutuksen järjestäjän edustaja"
    :vastuullinen-ohjaaja Henkilo "Vastuullinen työpaikkaohjaaja"
    :jarjestajan-edustaja Henkilo "Koulutuksen järjestäjän edustaja"
    (s/optional-key :muut-osallistujat) [Henkilo]
    "Muut ohjaukseen osallistuvat henkilöt"
    :keskeiset-tyotehtavat [s/Str] "Keskeiset työtehtävät"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")
    (s/optional-key :erityisen-tuen-aika) Aikavali
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  MuuOppimisymparisto
  (describe
    "Muu oppimisympäristö, missä osaamisen hankkiminen tapahtuu"
    :tarkenne KoodistoKoodi "Oppimisympäristön tarkenne, eHOS Koodisto-koodi"
    :selite s/Str "Oppimisympäristön nimi"
    :ohjaus-ja-tuki s/Bool
    "Onko opiskelijalla tunnistettu ohjauksen ja tuen tarvetta"
    :erityinen-tuki s/Bool
    (str "Onko opiskelijalla tunnistettu tuen tarvetta tai onko hänellä "
         "erityisen tuen päätös")
    (s/optional-key :erityisen-tuen-aika) Aikavali
    (str "Erityisen tuen alkamispvm ja päättymispvm kyseisessä tutkinnon tai"
         "koulutuksen osassa")))

(s/defschema
  OsaamisenHankkimistapa
  (describe
    "Osaamisen hankkimisen tapa"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :ajankohta Aikavali "Hankkimisen ajankohta"
    :osaamisen-hankkimistavan-tunniste KoodistoKoodi
    "Osaamisen hankkimisen Koodisto-koodi (URI: osaamisenhankkimistapa)"
    (s/optional-key :tyopaikalla-hankittava-osaaminen)
    TyopaikallaHankittavaOsaaminen
    "Työpaikalla tapahtuvaan osaamisen hankkimiseen liittyvät tiedot"
    (s/optional-key :muut-oppimisymparisto)
    MuuOppimisymparisto
    (str "Muussa oppimisympäristössä tapahtuvaan osaamisen hankkimiseen "
         "liittyvät tiedot")))

(s/defschema
  NaytonJarjestaja
  (describe
    "Näytön tai osaamisen osoittamisen järjestäjä"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Näytön tai osaamisen osoittamisen järjestäjän nimi"
    (s/optional-key :oid) s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid-numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  Arvioija
  (describe
    "Arvioija"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :nimi s/Str "Arvioijan nimi"
    :rooli KoodistoKoodi "Arvioijan roolin Koodisto-koodi"
    :organisaatio Organisaatio "Arvioijan organisaatio"))

(s/defschema
  Arviointikriteeri
  (describe
    "Arviointikriteeri"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :osaamistaso s/Int "Osaamistaso"
    :kuvaus s/Str "Arviointikriteerin kuvaus"))

(s/defschema
  HankitunOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta Aikavali "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    :ammattitaitovaatimukset [s/Int]
    (str "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Lista "
         "ePerusteet tunnisteita. Tunnisteen tyyppi voi vielä päivittyä.")
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"
    (s/optional-key :yksilolliset-arviointikriteerit) [Arviointikriteeri]
    "Yksilölliset arvioinnin kriteerit"))

(s/defschema
  HankitunYTOOsaamisenNaytto
  (describe
    "Hankitun YTO osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta Aikavali "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    :osaamistavoitteet [s/Int]
    (str "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan. Lista "
         "ePerusteet tunnisteita. Tunnisteen tyyppi voi vielä päivittyä.")
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"
    (s/optional-key :yksilolliset-arviointikriteerit) [Arviointikriteeri]
    "Yksilölliset arvioinnin kriteerit"))

(s/defschema
  HankitunPaikallisenOsaamisenNaytto
  (describe
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :jarjestaja NaytonJarjestaja "Näytön tai osaamisen osoittamisen järjestäjä"
    :nayttoymparisto Organisaatio
    "Organisaatio, jossa näyttö tai osaamisen osoittaminen annetaan"
    :kuvaus s/Str
    (str "Näyttöympäristön kuvaus. Tiivis selvitys siitä, millainen "
         "näyttöympäristö on kyseessä. Kuvataan ympäristön luonne lyhyesti, "
         "esim. kukkakauppa, varaosaliike, ammatillinen oppilaitos, "
         "simulaattori")
    :ajankohta Aikavali "Näytön tai osaamisen osoittamisen ajankohta"
    :sisalto s/Str "Näytön tai osaamisen osoittamisen sisältö tai työtehtävät"
    :ammattitaitovaatimukset [s/Str]
    "Ammattitaitovaatimukset, joiden osaaminen näytössä osoitetaan."
    :arvioijat [Arvioija] "Näytön tai osaamisen osoittamisen arvioijat"
    (s/optional-key :yksilolliset-arviointikriteerit) [Arviointikriteeri]
    "Yksilölliset arvioinnin kriteerit"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminen
  (describe
    "Puuttuvan ammatillisen osaamisen tiedot (GET)"
    :eid s/Int "Tunniste eHOKS-järjestelmässä"
    :tutkinnon-osa TutkinnonOsa "Tutkinnon osa"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "Ammattitaitovaatimuksista tai osaamistavoitteista poikkeaminen"
    (s/optional-key :hankitun-osaamisen-naytto) HankitunOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    (s/optional-key :tarvittava-opetus) s/Str "Tarvittava opetus"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenLuonti
  (modify
    PuuttuvaAmmatillinenOsaaminen
    "Puuttuvan ammatillisen osaamisen tiedot uutta merkintää luotaessa (POST)"
    {:removed [:eid]}))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenPaivitys
  (modify
    PuuttuvaAmmatillinenOsaaminen
    "Puuttuvan ammatillisen osaamisen tiedot merkintää ylikirjoittaessa (PUT)"))

(s/defschema
  PuuttuvaAmmatillinenOsaaminenKentanPaivitys
  (modify
    PuuttuvaAmmatillinenOsaaminen
    (str "Puuttuvan ammatillisen osaamisen tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:tutkinnon-osa :osaamisen-hankkimistavat :koulutuksen-jarjestaja-oid]}))

(s/defschema
  PuuttuvaYTOOsa
  (describe
    "Puuttuvan yhteinen tutkinnon osan (YTO) osan tiedot"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :tunniste KoodistoKoodi "Koodisto-koodi"
    (s/optional-key :laajuus) s/Int "Tutkinnon laajuus ePerusteet palvelussa"
    :eperusteet-id s/Str "Tunniste ePerusteet-palvelussa"
    (s/optional-key :nimi) s/Str "Tutkinnon osan nimi ePerusteet-palvelussa"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    (s/optional-key :vaatimuksista-tai-tavoitteista-poikkeaminen) s/Str
    "vaatimuksista tai osaamistavoitteista poikkeaminen"
    :hankitun-osaamisen-naytto HankitunYTOOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :tarvittava-opetus s/Str "Tarvittava opetus"))

(s/defschema
  PuuttuvaYTO
  (describe
    "Puuttuvan yhteinen tutkinnon osan tiedot"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    :eperusteet-id s/Int
    "Osan tunniste ePerusteet-palvelussa. Tunnisteen tyyppi voi vielä muuttua"
    :tutkinnon-osat [PuuttuvaYTOOsa] "Puuttuvat YTO osat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")))

(s/defschema
  PuuttuvaYTOLuonti
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:eid]}))

(s/defschema
  PuuttuvaYTOPaivitys
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osa tiedot merkintää "
         "ylikirjoittaessa (PUT)")))

(s/defschema
  PuuttuvaYTOKentanPaivitys
  (modify
    PuuttuvaYTO
    (str "Puuttuvan yhteinen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:eperusteet-id :tutkinnon-osat :koulutuksen-jarjestaja-oid]}))

(s/defschema
  PaikallinenTutkinnonOsa
  (describe
    "Puuttuva paikallinen tutkinnon osa"
    :eid s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :amosaa-tunniste) s/Str
    "Tunniste ePerusteet AMOSAA -palvelussa"
    :nimi s/Str "Tutkinnon osan nimi"
    :laajuus s/Int "Tutkinnon osan laajuus"
    :kuvaus s/Str "Tutkinnon osan kuvaus"
    :osaamisen-hankkimistavat [OsaamisenHankkimistapa]
    "Osaamisen hankkimistavat"
    :koulutuksen-jarjestaja-oid s/Str
    (str "Organisaation tunniste Opintopolku-palvelussa. Oid numero, joka on "
         "kaikilla organisaatiotasoilla: toimipisteen oid, koulun oid, "
         "koulutuksen järjestäjän oid.")
    :hankitun-osaamisen-naytto HankitunPaikallisenOsaamisenNaytto
    "Hankitun osaamisen osoittaminen: Näyttö tai muu osaamisen osoittaminen"
    :tarvittava-opetus s/Str "Tarvittava opetus"))

(s/defschema
  PaikallinenTutkinnonOsaLuonti
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot uutta merkintää "
         "luotaessa (POST)")
    {:removed [:eid]}))

(s/defschema
  PaikallinenTutkinnonOsaPaivitys
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot merkintää "
         "ylikirjoittaessa (PUT)")))

(s/defschema
  PaikallinenTutkinnonOsaKentanPaivitys
  (modify
    PaikallinenTutkinnonOsa
    (str "Puuttuvan paikallisen tutkinnon osan tiedot kenttää tai kenttiä "
         "päivittäessä (PATCH)")
    {:optionals
     [:nimi :laajuus :kuvaus :osaamisen-hankkimistavat
      :koulutuksen-jarjestaja-oid :hankitun-osaamisen-naytto
      :tarvittava-opetus]}))

(s/defschema
  Tutkinto
  (describe
    "Tutkinnon perustiedot ePerusteet järjestelmässä"
    :laajuus s/Int "Tutkinnon laajuus"
    :nimi s/Str "Tutkinnon nimi"))

(s/defschema
  Opiskeluoikeus
  (describe
    "Opiskeluoikeuden tiedot Koski-järjestelmässä"
    :oid s/Str "Opinto-oikeuden tunniste Opintopolku-ympäristössä"
    :tutkinto Tutkinto "Opinto-oikeuden tutkinto"))

(s/defschema
  OlemassaOlevaAmmatillinenOsaaminen
  (describe
    (str "Ammatillinen osaaminen, joka osaamisen tunnustamisen perusteella
    sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    (s/optional-key :tutkinnon-id) s/Int "Tutkinnon osan id,
  johon tunnistettava olemassaoleva osaaminen liittyy"
    :valittu-todentamisen-prosessi
    (s/enum :valittu-todentaminen-suoraan :valittu-todentaminen-arvioijat
            :valittu-todentaminen-naytto)
    "Todentamisen prosessin kuvaus (suoraan/arvioijien kautta/näyttö)"
    (s/optional-key :tarkentavat-tiedot) [HankitunOsaamisenNaytto]
    "Mikäli valittu näytön kautta, tuodaan myös näytön tiedot."))

(s/defschema
  OlemassaOlevaOsaaminen
  (describe
    (str "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    :eid s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :olemassaoleva-ammatillinen-osaaminen)
    [OlemassaOlevaAmmatillinenOsaaminen] "Olemassa oleva ammatillinen osaaminen"
    (s/optional-key :olemassaolevat-yto-osa-alueet) [YhteinenTutkinnonOsa]
    "Olemassaolevat yton osa-alueet"
    (s/optional-key :olemassaoleva-paikallinen-tutkinnon-osa)
    [PaikallinenTutkinnonOsa] "Olemassaoleva paikallinen tutkinnon osa"))

(s/defschema
  OlemassaOlevaOsaaminenLuonti
  (modify
    OlemassaOlevaOsaaminen
    (str "Olemassa olevan osaamisen tunnustamisen perusteella sisällytetty"
         "osaaminen uutta merkintää luotaessa (POST)")
    {:removed [:eid]}))

(s/defschema
  OlemassaOlevaOsaaminenPaivitys
  (modify
    OlemassaOlevaOsaaminen
    (str "Olemassa olevan osaamisen tunnustamisen perusteella sisällytettyjen"
         "osaamisten tiedot merkintää ylikirjoittaessa (PUT)")))

(s/defschema
  OlemassaOlevaOsaaminenKentanPaivitys
  (modify
    OlemassaOlevaOsaaminen
    (str "Olemassa olevan osaamisen tunnustamisen perusteella sisällytettyjen"
         "osaamisten tiedot kenttää tai kenttiä päivittäessä (PATCH)")
    {:optionals
     [:olemassaoleva-ammatillinen-osaaminen
      :olemassaolevat-yto-osa-alueet
      :olemassaoleva-paikallinen-tutkinnon-osa]}))

(s/defschema
  Opinnot
  (describe
    "Opinnot"
    (s/optional-key :eid) s/Int "Tunniste eHOKS-järjestelmässä"
    (s/optional-key :ammatilliset-opinnot) [TutkinnonOsa]
    "Osaamisen ammattilliset opinnot"
    (s/optional-key :yhteiset-tutkinnon-osat) [YhteinenTutkinnonOsa]
    "Osaamisen yhteiset tutkinnon osat (YTO)"
    (s/optional-key :paikalliset-osaamiset) [PaikallinenTutkinnonOsa]
    "Osaamisen paikallisen tutkinnon osat"))

(s/defschema
  TunnustettavanaOlevaOsaaminen
  (st/merge
    (describe
      "Osaaminen, joka on toimitettu arvioijille osaamisen tunnustamista varten"
      :todentajan-nimi s/Str
      "Osaamisen todentaneen toimivaltaisen viranomaisen nimi")
    Opinnot))

(s/defschema
  HOKS
  (describe
    "Henkilökohtainen osaamisen kehittämissuunnitelmadokumentti (GET)"
    :eid s/Int "Tunniste eHOKS-järjestelmässä"
    :oppijan-oid s/Str "Oppijan tunniste Opintopolku-ympäristössä"
    :opiskeluoikeus-oid s/Str
    "Opiskeluoikeuden yksilöivä tunniste Koski-järjestelmässä."
    :opiskeluoikeus Opiskeluoikeus
    "Opiskeluoikeuden tiedot Koski-järjestelmässä"
    (s/optional-key :urasuunnitelma) KoodistoKoodi
    "Opiskelijan tavoite 1, urasuunnitelman Koodisto-koodi"
    :versio s/Int "HOKS-dokumentin versio"
    :luonut s/Str "HOKS-dokumentin luoneen henkilön nimi"
    :paivittanyt s/Str "HOKS-dokumenttia viimeksi päivittäneen henkilön nimi"
    :hyvaksynyt s/Str "Luodun HOKS-dokumentn hyväksyjän nimi"
    :luotu s/Inst "HOKS-dokumentin luontiaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    :hyvaksytty s/Inst
    "HOKS-dokumentin hyväksymisaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    :paivitetty s/Inst
    "HOKS-dokumentin viimeisin päivitysaika muodossa YYYY-MM-DDTHH:mm:ss.sssZ"
    (s/optional-key :olemassa-oleva-osaaminen) OlemassaOlevaOsaaminen
    (str "Osaamisen tunnustamisen perusteella sisällytetty suoraan osaksi "
         "opiskelijan tutkintoa")
    (s/optional-key :opiskeluvalmiuksia-tukevat-opinnot)
    OpiskeluvalmiuksiaTukevatOpinnot
    "Opiskeluvalmiuksia tukevat opinnot"
    (s/optional-key :puuttuva-ammatillinen-osaaminen)
    [PuuttuvaAmmatillinenOsaaminen]
    "Puuttuvan ammatillisen osaamisen hankkimisen tiedot"
    (s/optional-key :puuttuva-yhteisen-tutkinnon-osat) [PuuttuvaYTO]
    "Puuttuvan yhteisen tutkinnon osan hankkimisen tiedot"
    (s/optional-key :puuttuva-paikallinen-tutkinnon-osa)
    [PaikallinenTutkinnonOsa]
    "Puuttuvat paikallisen tutkinnon osat"))

(s/defschema
  HOKSPaivitys
  (modify
    HOKS
    "HOKS-dokumentin ylikirjoitus (PUT)"
    {:removed [:versio :luotu :hyvaksytty :paivitetty]}))

(s/defschema
  HOKSKentanPaivitys
  (modify
    HOKS
    "HOKS-dokumentin arvon tai arvojen päivitys (PATCH)"
    {:removed [:versio :luotu :hyvaksytty :paivitetty]
     :optionals [:opiskeluoikeus-oid :luonut :paivittanyt :hyvaksynyt]}))

(s/defschema
  HOKSLuonti
  (modify
    HOKS
    "HOKS-dokumentin arvot uutta merkintää luotaessa (POST)"
    {:removed [:eid :versio :luotu :hyvaksytty :paivitetty]}))
