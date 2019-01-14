# eHOKS

## Teknologiat

### Frontend

[Github Repository](https://github.com/Opetushallitus/ehoks-ui)

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Clojure.test](https://clojure.github.io/clojure/clojure.test-api.html)
+ [Compojure-api 2](https://github.com/metosin/compojure-api/)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 9.5](https://www.postgresql.org/docs/9.5/static/index.html)
+ [HugSQL](https://www.hugsql.org/)
+ [Flyway](https://flywaydb.org/)
+ [clj-http](https://github.com/dakrone/clj-http)
+ [Cheshire](https://github.com/dakrone/cheshire)
+ [Environ](https://github.com/weavejester/environ)
+ [Redis](https://redis.io/)
+ [Redis Client](https://github.com/ptaoussanis/carmine)
+ [Logback](https://logback.qos.ch/)
+ [tools.logging](https://github.com/clojure/tools.logging)

#### RESTful API
Backend pyrkii seuraamaan
[RESTful](https://en.wikipedia.org/wiki/Representational_state_transfer)
periaatteita. Kaikki vataukset (paitsi no content) sisältävät meta- ja
dataobjektit.

Avaimet seuraavat Clojuren notaatiota.

## QA

[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).


Repossa on `.editorconfig` jonka avulla voit kertoa editorillesi käytettävät
tyylit.

### Testien ajaminen

Kerran:

``` shell
lein test
```

Muutoksista:

``` shell
lein auto test
```

Testien kattavuus:

``` shell
lein with-profile test cloverage
```

### Lintterit

Staattiset linterit ajetaan:

``` shell
lein checkall
```

tämä ajaa Kibit, Bikeshed, Eastwood ja cljfmt kerralla. Jokainen työkalu on
ajettavissa myös erikseen:

``` shell
lein kibit
lein bikeshed
lein eastwood
lein cljfmt check
```

### Lisää tietoa

+ [kibit](https://github.com/jonase/kibit)
+ [lein-bikeshed](https://github.com/dakrone/lein-bikeshed)
+ [eastwood](https://github.com/jonase/eastwood)
+ [cljfmt](https://github.com/weavejester/cljfmt)

## Kehitys

### Ohjelman ajaminen

Kehitysmoodissa:

``` shell
lein run
```

Tuotantomoodissa:

``` shell
lein with-profile -dev run
```

Replissä `lein repl`:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server))
```

Tai omalla konfiguraatiolla:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server "config/custom.edn"))
```

Ja ohjelman sammuttaminen:

``` repl
user> (.stop server)
```

### Testit

Ulkoiset API-kutsut voidaan mockata. Kehitysresursseissa on konfiguroitava
HTTP-asiakasohjelma, jonka GET- ja POST-kutsut voidaan yliajaa. Tämä toimii
ainoastaan `test`-profiililla.

### Redis

Redis toimii istunnon tallennuksessa.

Paikallisessa ajossa voidaan käyttää valmista Docker-imagea:

Kontin luonti:

``` shell
cd scripts/redis-docker
docker build -t ehoks-redis .
```

Kontin ajaminen:

``` shell
docker run --rm --name ehoks-redis -p 6379:6379 --volume ~/path/to/ehoks-redis-data:/data ehoks-redis
```

Rediksen voi jättää myös pois, jolloin istuntoa pidetään muistissa. Tämä
tapahtuu asettamalla `:redis-url` konfiguraation nil:ksi.

### Kehityksen endpointit

Sovellus tukee dummy-JSON-rajapintoja. Laita valmiit JSON-tiedostot kansioon
`resource/dev/dev-routes`. Tiedostoa vastaava endpoint luodaan automaattisesti
muuttamalla alaviivat kauttaviivoiksi. Esimerkiksi `/hello/world` endpoint
tarjoilee `hello_world.json`-tiedoston. Turvallisuussyistä ainoastaan tiedostot
kansiossa `dev-routes` luetaan ja endpointit toimivat ainoastaan
kehityspalvelimen kanssa.

## Dummy-datan luonti

Sovellus käyttää kehityksessä yksinkertaista muistitietokantaa, jolle voidaan
ladata alkuarvot tiedostoista. Lisää tiedostot kansioon
`resources/dev/demo-data/`. Data ladataan ainoastaan kehitysympäristön
käynnistyksessä.

## Konfigurointi

Oletuskonfiguraatio on `config/default.edn`. Arvoja voi yliajaa luomalla oman
konfiguraatiotiedoston ja antamalla sen joko `CONFIG`-ympäristömuuttujassa,
JVM system propertyssä `config` tai kehityspalvelimen parametrina.

Konfiguraatiotiedostot yhdistetään niin, että oma kustomoitu tiedosto yliajaa
vain ne arvot, mitkä siinä on määritelty. Konfiguraatio validoidaan ladattaessa.

### CAS-tunnistus

Sovellus käyttää CAS-tunnistautumista ulkoisten rajapintojen kanssa. Lisää
CAS-tunnukset ja sovelluksen tunniste (ent. client sub system code) ennen
rajapintojen käyttämistä.

## Ajettava jar

Ajettava jar riippuvuuksineen luodaan:

```
lein uberjar
```

Ja ajetaan:

``` shell
java -jar target/ehoks-standalone.jar
```

## Integraatiot

Service | Documentation
--------|--------------
Opintohallintojärjestelmät |
AMOSAA |
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet)
KOSKI | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koski-palvelukortti)

## Linkit

+ [eHOKS Confluence](https://confluence.csc.fi/display/OPHPALV/eHOKS+-+hanke)
