# Spring Modulith Shopping Showcase

A reference implementation of the shopping domain specified in
[`docs/requirements`](docs/requirements/README.md), built with Spring Boot, Spring Modulith and
jMolecules.

## Architecture

```
                    ┌──────────────────────────────────────────┐
   REST  ─────────► │              cart-order-app                │
                    │                                            │
                    │   cart module ──(in-process event,         │
                    │        │          Event Publication        │
                    │        │          Registry)──► order module │
                    │        │                          │ ACL     │
                    │        │                          ▼ (stub)  │
                    │   item popularity            external order │
                    │   (read model)                  fulfillment │
                    └────────┬───────────────────────────────────┘
                             │  externalized events (Kafka)
                             ▼
                    shopping-cart-events (topic)
                             │
                             ▼
                    ┌────────────────────┐
   REST ──────────► │    analytics-app    │  (separate deployable)
                    └────────────────────┘
```

- **cart-order-app** — a Spring Modulith application bundling two modules:
  - `cart` — the cart aggregate, item-popularity read model, REST API; publishes domain events.
  - `order` — listens to the in-process `CartCheckedOut` event and creates an order through an
    anti-corruption layer that calls a (stubbed) external fulfillment service.
  - Cart events are also **externalized to Kafka** for the Analytics application.
- **analytics-app** — a separate Spring Boot application consuming the Kafka event stream
  (counts, throughput, idempotent/duplicate-tolerant, offset-based resume).
- **events-contract** — a shared, framework-neutral library of the Kafka event payloads.

Persistence is JPA + Postgres. Reliable in-process delivery uses the Spring Modulith Event
Publication Registry (JPA-backed).

## Module / project layout

| Project | Description |
|---------|-------------|
| `events-contract` | Shared integration event records + topic constants (plain Java) |
| `cart-order-app`  | Spring Modulith app: `cart` + `order` modules |
| `analytics-app`   | Standalone Kafka consumer / analytics app |

## Prerequisites

- JDK 21
- Docker + Docker Compose (for running the full stack)

The Gradle wrapper is included; no local Gradle install is required.

## Build and test

```bash
./gradlew build
```

This compiles all projects, runs the unit tests, the Spring Modulith module-verification test
(`ApplicationModules.verify()`), and the checkout→order scenario test, and produces the runnable
`bootJar`s.

## Run the full stack with Docker Compose

The application images copy the pre-built `bootJar`s (the image build itself does not run Gradle),
so build them on the host first:

```bash
./gradlew :cart-order-app:bootJar :analytics-app:bootJar
docker compose up --build
```

This starts Postgres (with `shoppingdb` and `analyticsdb`), Kafka (KRaft, single node), creates the
`shopping-cart-events` topic, and runs both applications.

| Service | URL |
|---------|-----|
| cart-order-app | http://localhost:8080 |
| analytics-app  | http://localhost:8081 |
| Postgres       | localhost:5432 (user `postgres` / `postgres`) |
| Kafka (host)   | localhost:29092 |

### Try it

```bash
# add items to a cart
curl -X POST localhost:8080/carts/cart-1/items -H 'Content-Type: application/json' \
  -d '{"itemId":"socks","quantity":3}'
curl -X POST localhost:8080/carts/cart-1/items -H 'Content-Type: application/json' \
  -d '{"itemId":"t-shirt","quantity":2}'

# read the cart
curl localhost:8080/carts/cart-1

# update / remove
curl -X PUT localhost:8080/carts/cart-1/items/socks -H 'Content-Type: application/json' \
  -d '{"quantity":5}'

# check out (triggers order creation + Kafka externalization)
curl -X POST localhost:8080/carts/cart-1/checkout

# the resulting order
curl localhost:8080/orders/cart-1

# item popularity
curl localhost:8080/items/socks/popularity

# analytics view of consumed events
curl localhost:8081/analytics/stats
```

## Run applications locally (without Compose)

Start Postgres and Kafka yourself (or use the Compose `postgres`/`kafka` services), then:

```bash
./gradlew :cart-order-app:bootRun
./gradlew :analytics-app:bootRun
```

Defaults assume Postgres on `localhost:5432` and Kafka on `localhost:9092`; override via the
`SPRING_DATASOURCE_URL` and `SPRING_KAFKA_BOOTSTRAP_SERVERS` environment variables.
