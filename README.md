# SolarMind

SolarMind is a scoped rooftop-solar recommendation app. Spring Boot owns authentication, CSV-backed data, recommendation math, persistence, and API contracts. React displays and collects data. FastAPI is the internal Gemini explanation boundary.

## Prerequisites

- Java 17+
- Maven 3.9+ (Maven is not installed in the current workspace)
- Node.js 20+ and npm
- Python 3.11+ and uvicorn
- MySQL 8+

## Run

### Local backend with MySQL

From PowerShell at the repository root:

```powershell
.\backend\run-dev.ps1
```

Before starting, set `DATABASE_URL`, `DATABASE_USERNAME`, and
`DATABASE_PASSWORD` for your MySQL `solarmind` database. The launcher runs the
`local` profile against MySQL, applies the Flyway schema, imports the supplied
CSV files. `JWT_SECRET` and `AI_INTERNAL_TOKEN` must be set explicitly.
`GET http://localhost:8080/api/health` should return `{"status":"ok"}`.

### Full local stack with MySQL

1. Copy `.env.example` to local environment variables and replace every `PLACEHOLDER_*` value. Do not commit the local file.
2. Start MySQL and create the `solarmind` database.
3. Backend: `cd backend; mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`
4. AI service: `cd ai-service; python -m uvicorn main:app --reload --port 8000`
5. Frontend: `cd frontend; npm install; npm run dev`

The two supplied CSVs are read from `data/` by default through `PANELS_CSV_PATH` and `LOCATIONS_CSV_PATH`. They are never modified. The generation factor, installation cost, emission factor, roof layout factor, and roof-type factors must be supplied through environment variables; no real-looking defaults are committed.

For an existing database without `flyway_schema_history`, take a backup and
perform a one-time Flyway baseline before normal migration validation. See the
run guide for the difference between the `local` and `dev` profiles.

See [docs/architecture-map.md](docs/architecture-map.md) for the structure and endpoint map.
