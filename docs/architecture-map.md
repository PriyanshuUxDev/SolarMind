# SolarMind architecture map

## Current tree

```text
frontend/             React + TypeScript + Vite application
backend/              Spring Boot + Maven API and MySQL persistence layer
ai-service/           FastAPI Gemini explanation boundary
data/                 Immutable CSV files used for reference-data seeding
docs/                 Project documentation and API collection
```

Spring Boot owns authentication, JWT security, validation, MySQL persistence,
Flyway migrations, CSV seeding, recommendation calculations, and API contracts.
React collects inputs and renders backend responses. FastAPI is an internal
Gemini explanation boundary and does not recalculate recommendations.

## Runtime boundaries

| Boundary | Responsibility |
| --- | --- |
| React frontend | Collect inputs, call the API, and render catalog, assessment, dashboard, and assistant views. It performs no recommendation calculations. |
| Spring Boot backend | Authentication, validation, MySQL persistence, CSV seeding, panel/location search, recommendation calculations, ownership checks, and API errors. |
| FastAPI AI service | Authenticate backend requests, call Gemini, and explain verified results. |
| Flyway | Apply versioned MySQL schema migrations before normal repository access. |

## Backend class inventory

| Layer | Classes | Responsibility |
| --- | --- | --- |
| Controllers | `AuthController`, `LocationController`, `PanelController`, `AssessmentController`, `DashboardController`, `AIController`, `HealthController` | REST endpoints and request delegation. |
| Services | `AuthService`, `LocationService`, `PanelService`, `AssessmentService`, `RecommendationService`, `DashboardService`, `AIService` | Business operations and orchestration. |
| Importers | `PanelCsvImporter`, `LocationCsvImporter`, `SimpleCsv` | Validate and idempotently seed reference data from CSV files. |
| Repositories | `UserRepository`, `LocationRepository`, `SolarPanelRepository`, `AssessmentRepository` | JPA persistence access. |
| Entities | `User`, `Location`, `SolarPanel`, `Assessment` | MySQL database model and relationships. |
| DTOs | Request DTOs and response DTOs including `AssessmentSummary`, `AssessmentResponse`, `PanelResponse`, and `DashboardResponse` | Validated API input and output contracts. |
| Security | `SecurityConfig`, `JwtUtil`, `JwtAuthFilter`, `CustomUserDetailsService`, `CurrentUser` | Stateless JWT authentication and ownership context. |
| Configuration | `SolarProperties`, `AiProperties`, `JwtProperties`, `CorsProperties`, `CsvProperties`, `RestClientConfig` | Environment-backed runtime settings. |

## Profiles and configuration

All profiles use MySQL through `DATABASE_URL`, `DATABASE_USERNAME`, and
`DATABASE_PASSWORD`.

- `local` is the normal launcher profile. It enables Flyway, validates the
  schema with Hibernate, imports the repository CSV files, points the AI client
  at `http://localhost:8000`, and uses local JWT expiry settings.
- `dev` is for deliberate schema-development work. Flyway remains enabled and
  Hibernate uses `ddl-auto=update` so schema changes can be explored against
  MySQL.
- The base `application.yml` uses `ddl-auto=validate` and environment-backed
  settings. There is no H2 configuration.

The PowerShell and Bash launchers require database credentials, `JWT_SECRET`,
and `AI_INTERNAL_TOKEN`; they fail before Maven starts if those values are
missing. The launchers provide absolute CSV paths for `data/`.

## Database lifecycle

1. Flyway applies `V1__create_schema.sql` and later migrations from
   `backend/src/main/resources/db/migration/`.
2. Hibernate validates the schema in `local` and the base configuration.
3. Hibernate may update the schema only when the `dev` profile is explicitly
   selected.
4. CSV importers seed `locations` and `solar_panels` when CSV import is enabled;
   source files are never modified.
5. Assessment writes persist the authenticated `user_id`, and assessment reads
   are restricted to that owner.

`baseline-on-migrate` is not enabled. New databases must be empty so Flyway can
apply the migrations normally. An existing non-empty database without
`flyway_schema_history` requires a backup, schema review, and a one-time
operator-controlled baseline before normal migration validation. Applied
migrations must not be edited.

The panel source CSV may contain a `Product Link` column, but the application
does not read, persist, or expose it. The location CSV contains extra unnamed
columns which are intentionally ignored.

## Endpoint map

| Endpoint | Purpose |
| --- | --- |
| `POST /api/auth/register` | Create a user and issue a JWT. |
| `POST /api/auth/login` | Authenticate a user and issue a JWT. |
| `GET /api/locations/search` | Search seeded locations. |
| `GET /api/panels` | Search and filter the seeded panel catalog. |
| `GET /api/panels/{id}` | Read one panel's complete catalog details. |
| `POST /api/assessments` | Validate inputs, calculate a budget-aware recommendation, and persist it. |
| `GET /api/assessments` | Read summary records owned by the authenticated user. |
| `GET /api/assessments/{id}` | Read one complete assessment owned by the authenticated user. |
| `GET /api/dashboard` | Return the latest assessment and recent history. |
| `POST /api/ai/ask` | Explain a verified assessment through the internal AI service. |
| `GET /api/health` | Public health smoke check. |

## Frontend build

The frontend uses Vite, React, React Router, TypeScript, and CSS. Tailwind is
not part of the current frontend toolchain. Fontsource packages provide
Bricolage Grotesque and Instrument Sans. Dependency versions are pinned in
`frontend/package.json` and `frontend/package-lock.json`.

Use `npm run build` to run TypeScript checking and the Vite production build.
