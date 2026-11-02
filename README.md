# master-prj-backend

Spring Boot 4 / Kotlin metrics-collection backend for the master's thesis comparison "PWAs vs Native AR Filters". Receives `TrackingSessionRequest` payloads from the PWA and React Native apps after each AR session, stores them in PostgreSQL, and exposes aggregation endpoints for analysis.

**Stores only metrics — never camera frames or images.** This is enforced by the absence of any file/BLOB fields or multipart-handling endpoints (verifies F16 and F17 of `Anforderungen.pdf`).

See `../ARCHITECTURE.md` for the full system architecture and shared data model.

## Prerequisites

- JDK 24
- Docker + Docker Compose (recommended) — runs PostgreSQL + backend + Caddy
- Or: PostgreSQL 17 running locally with database `masterprj`, user `masterprj`, and the password exported as `DB_PASSWORD`

## Run with Docker (recommended)

```bash
cp .env.example .env   # then fill in the three values (openssl rand -hex 32)
docker compose up --build
```

## Access control (since 3 November 2026)

The records are participants' pseudonymous data, so nothing reads them without a
token (`config/StudyAccessFilter.kt`):

| Request | Needs |
|---|---|
| `POST /api/tracking/sessions` | header `X-Study-Token: $STUDY_WRITE_TOKEN` (built into the study builds of both apps) |
| every other `/api/tracking/*`, `/actuator/*`, API docs | header `Authorization: Bearer $STUDY_READ_TOKEN` |
| `GET /api/tracking/health`, `GET /actuator/health` | nothing |

CORS is granted only to `STUDY_ALLOWED_ORIGINS` (default: the deployed PWA).
Postgres publishes no port; it is reachable only from the backend container.
An unset token rejects every request it guards.

### Changing the database password

`POSTGRES_PASSWORD` only applies when the volume is first created. The old
default password is in this repository's history, so on an existing volume
change it inside Postgres too, then restart with the new `.env`:

```bash
docker exec -it masterprj-postgres psql -U masterprj -d masterprj \
  -c "ALTER USER masterprj WITH PASSWORD '<new value from .env>';"
docker compose up -d --build
```

This brings up PostgreSQL 17, the backend (port 8080 inside Docker), and a Caddy reverse proxy on host ports 80/443. The Caddy site name is configured in `Caddyfile`.

## Run with Maven (development)

```bash
# In one terminal — Postgres only
docker compose up postgres

# In another terminal — backend with hot reload via spring-boot-devtools
./mvnw spring-boot:run
```

The backend listens on `http://localhost:8080`. Hot reload is enabled in dev because `spring-boot-devtools` is on the runtime classpath.

## Tests

```bash
./mvnw test
```

The test profile uses H2 in-memory (no PostgreSQL needed for tests). Currently only a smoke `contextLoads()` test exists.

## API surface

Base path: `/api/tracking`. Six endpoints:

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/sessions` | Submit a completed session (used by both apps). Returns `201 Created`. |
| `GET` | `/sessions` | List session summaries. Optional `?platform=` and `?mode=` filters. |
| `GET` | `/sessions/{id}` | Fetch full session detail. |
| `GET` | `/sessions/range?start=&end=` | Sessions within a time window (ISO-8601 instants). |
| `GET` | `/comparison` | Aggregated stats per `(platform, mode)` pair. |
| `GET` | `/health` | Liveness probe. |

Once OpenAPI is wired in, interactive docs are at `http://localhost:8080/swagger-ui.html`.

CORS is wide open (`config/CorsFilter.kt`) — fine for thesis testing, would not be acceptable in production.

## Data model

Entity: `entity/TrackingSession.kt`. DTO contract shared with both apps: `dto/TrackingDTOs.kt`. The DTO file is hand-synced with `master-prj-pwa/src/domain/tracking.dto.ts` and `master-prj-native/src/domain/tracking.dto.ts`. When adding a metric, update all three.

Schema is managed by Hibernate `ddl-auto: update` (`application.yml`). No SQL migrations — fields can be added safely as long as they are nullable. To drop or rename columns, do it manually in PostgreSQL first.

## Source tree

```
src/main/kotlin/at/fhcampuswien/masterprj/
  MasterPrjBackendApplication.kt   — Spring Boot entry point
  config/CorsFilter.kt             — open CORS for PWA cross-origin POSTs
  controller/TrackingController.kt — REST endpoints
  service/TrackingSessionService.kt — business logic + aggregation
  repository/                      — Spring Data JPA repositories
  entity/TrackingSession.kt        — JPA entity (one row per submitted session)
  dto/TrackingDTOs.kt              — request/response/summary DTOs
src/main/resources/application.yml — PostgreSQL config, Hibernate settings
src/test/                          — context-loads smoke test
docker-compose.yml                 — Postgres + backend + Caddy
Dockerfile                         — multi-stage Maven → JRE image
Caddyfile                          — reverse proxy config
```

## Inspecting data

```bash
# psql via Docker
docker exec -it masterprj-postgres psql -U masterprj -d masterprj

# Most-recent sessions
SELECT id, platform, mode, avg_fps, avg_inference_time_ms, session_duration_ms, recorded_at, active_filters
FROM tracking_sessions
ORDER BY recorded_at DESC
LIMIT 10;

# Aggregate comparison
SELECT platform, mode, COUNT(*) AS n, AVG(avg_fps) AS fps, AVG(avg_inference_time_ms) AS inference_ms
FROM tracking_sessions
GROUP BY platform, mode
ORDER BY platform, mode;
```

## Deployment

Currently deployed at `https://master-backend.el-shaarawi.com` via Caddy with auto-TLS. To deploy your own copy, edit the host name in `Caddyfile` and point DNS at the host running `docker compose`.
