# eHOKS

## Technologies

### Frontend

[Github Repository](https://github.com/Opetushallitus/ehoks-ui)

### Backend

+ [Clojure 1.9.0](https://clojure.org/)
+ [Clojure.test](https://clojure.github.io/clojure/clojure.test-api.html)
+ [Compojure-api 2](https://github.com/metosin/compojure-api/)
+ [Leiningen](https://leiningen.org/)
+ [PostgreSQL 9.5](https://www.postgresql.org/docs/9.5/static/index.html) as a
database
+ [HugSQL](https://www.hugsql.org/)
+ [Flyway](https://flywaydb.org/) for database migrations
+ [clj-http](https://github.com/dakrone/clj-http) for http requests with
integrations
+ [Cheshire](https://github.com/dakrone/cheshire) for JSON decoding/encoding
+ [Environ](https://github.com/weavejester/environ) for environment variables
+ [Redis](https://redis.io/) for session storage
+ [Redis Client](https://github.com/ptaoussanis/carmine)

## Quality assurance

[The Clojure Style Guidea](https://github.com/bbatsov/clojure-style-guide).

Repository has `.editorconfig` file for configuring your editor.

Static linters for backend can be run with command:

``` shell
lein checkall
```

It runs Kibit, Bikeshed, Eastwood, and cljfmt all at once. Every tool can also
be run individually:

``` shell
lein kibit
lein bikeshed
lein eastwood
lein cljfmt check
```

### More info

+ [kibit](https://github.com/jonase/kibit)
+ [lein-bikeshed](https://github.com/dakrone/lein-bikeshed)
+ [eastwood](https://github.com/jonase/eastwood)
+ [cljfmt](https://github.com/weavejester/cljfmt)

## Development

### Running application

``` shell
lein ring server-headless
```

Or in development mode (for example development CORS)

``` shell
lein with-profile dev ring server-headless
```

Or inside repl with file reload:

``` repl
user> (use 'oph.ehoks.dev-server)
user> (def server (start-server))
```

And shutting down:

``` repl
user> (.stop server)
```

### Database

For database there is proper Docker script in `scripts/psql-docker` folder. Use
this only in development environment.

Initializing

``` shell
cd scripts/postgres-docker
docker build -t ehoks-postgres:9.5 .
```

Running

``` shell
docker run --rm --name ehoks-postgres -p 5432:5432 --volume ~/path/to/ehoks-postgres-data:/var/lib/postgresql/data ehoks-postgres:9.5
```

### Redis

Redis is being used as a session storage.

For local development use you can use Docker script in `scripts/redis-docker`
folder.

Initializing

``` shell
cd scripts/redis-docker
docker build -t ehoks-redis .
```

Running

``` shell
docker run --rm --name ehoks-redis -p 6379:6379 --volume ~/path/to/ehoks-redis-data:/data ehoks-redis
```

Or you can always skip runnign Redis with leaving `REDIS_URL` environment
variable or `:redis-url` cofigure option nil.

## Configuration

Default configuration file is `config/default.edn`. You may override
these values by creating your own config file and supplying path to the
file either via environment variable `CONFIG` or JVM system property
`config`.

## Running tests

Running tests once

``` shell
lein test
```

Or on change

``` shell
lein auto test
```

## Creating runnable JAR

```
lein do clean, ring uberjar
java -jar target/ehoks-backend.jar
```

## Integrations

Service | Documentation
--------|--------------
Opintohallintojärjestelmät |
AMOSAA |
ePerusteet | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/ePerusteet)
KOSKI | [palvelukortti](https://confluence.csc.fi/display/OPHPALV/Koski-palvelukortti)

## Links

+ [eHOKS Confluence](https://confluence.csc.fi/display/OPHPALV/eHOKS+-+hanke)
