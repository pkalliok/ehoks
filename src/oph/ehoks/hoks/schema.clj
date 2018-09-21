(ns oph.ehoks.hoks.schema
  (:require [schema.core :as s]))

(s/defschema Koulutus
             "HOKS koulutus"
             {:id s/Int
              :tyyppi s/Str
              :diaarinumero s/Str
              :hoks-id s/Int})

(s/defschema Osaamisala
             "Osaamisala koodistosta"
             {:versio s/Int
              :uri s/Str})

(s/defschema Osaaminen
             "HOKSin olemassa oleva osaaminen"
             {:tyyppi s/Str
              :hoks-id s/Int
              :diaarinumero s/Str
              :osaamisala Osaamisala
              :suorituspvm s/Inst})

(s/defschema SuunniteltuOsaaminen
             "HOKSin puuttuvan osaamisen hankkimisen suunnitelma"
             {:tyyppi s/Str
              :hoks-id s/Int
              :diaarinumero s/Str
              :osaamisala Osaamisala
              :suoritustapa s/Str
              :sisalto s/Str
              :alku s/Inst
              :loppu s/Inst
              :organisaatio s/Str
              :keskeiset-tehtavat [s/Str]
              :ohjaus-ja-tuki s/Bool
              :erityinen-tuki s/Bool})

(s/defschema HOKSArvot
             "HOKS arvot uuden HOKSin luomiseen"
             {:oppijan-oid s/Str
              :uratavoite s/Str
              :tutkintotavoite s/Str
              :tutkinto-diaarinumero s/Str
              :osaamisala Osaamisala
              :opiskeluoikeus-alkupvm s/Inst
              :opiskeluoikeus-paattymispvm s/Inst})

(s/defschema HOKS
             "HOKS"
             (merge
               HOKSArvot
               {:id s/Int
                :versio s/Int
                :luojan-oid s/Str
                :paivittajan-oid s/Str
                :luonnin-hyvaksyjan-oid s/Str
                :paivityksen-hyvaksyjan-oid s/Str
                :luotu s/Inst
                :hyvaksytty s/Inst
                :paivitetty s/Inst
                :osaamiset [Osaaminen]
                :koulutukset [Koulutus]
                :suunnitellut-osaamiset [SuunniteltuOsaaminen]}))
