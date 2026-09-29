# SolarMind

SolarMind is a scoped rooftop-solar recommendation app. Spring Boot owns authentication, CSV-backed data, recommendation math, persistence, and API contracts. React displays and collects data. FastAPI is the internal Gemini explanation boundary.

## Prerequisites

- Java 17+
- Maven 3.9+ (Maven is not installed in the current workspace)
- Node.js 20+ and npm
- Python 3.11+ and uvicorn
- MySQL 8+

## Run

### Fast local backend smoke run

From PowerShell at the repository root:

```powershell
.\backend\run-dev.ps1
```

This uses an isolated in-memory H2 database, imports the supplied CSV files,
and generates a temporary JWT secret for that process. The assumptions in the
`local` profile are test-only values; do not use them for real recommendations.
`GET http://localhost:8080/api/health` should return `{"status":"ok"}`.

### Full local stack with MySQL

1. Copy `.env.example` to local environment variables and replace every `PLACEHOLDER_*` value. Do not commit the local file.
2. Start MySQL and create the `solarmind` database.
3. Backend: `cd backend; mvn spring-boot:run "-Dspring-boot.run.profiles=dev"`
4. AI service: `cd ai-service; python -m uvicorn main:app --reload --port 8000`
5. Frontend: `cd frontend; npm install; npm run dev`

The two supplied CSVs are read from `data/` by default through `PANELS_CSV_PATH` and `LOCATIONS_CSV_PATH`. They are never modified. The generation factor, installation cost, emission factor, roof layout factor, and roof-type factors must be supplied through environment variables; no real-looking defaults are committed.

See [docs/architecture-map.md](docs/architecture-map.md) for the structure and endpoint map.
