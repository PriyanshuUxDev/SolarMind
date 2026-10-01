# SolarMind: How to Run

SolarMind runs as three services: a Spring Boot backend, a React frontend, and an optional FastAPI AI service. The backend uses MySQL and Flyway. There is no H2 database.

## Prerequisites

- Java 17 or newer
- Maven 3.9 or IntelliJ IDEA's bundled Maven
- Node.js 20 or newer and npm
- Python 3.11 or newer for the AI service
- MySQL 8

Check the tools:

```powershell
java -version
mvn -version
node --version
npm --version
python --version
```

## Configuration

Copy `.env.example` into your shell environment or use it as a checklist. Do not commit real secrets.

Required database variables:

```text
DATABASE_URL=jdbc:mysql://localhost:3306/solarmind
DATABASE_USERNAME=...
DATABASE_PASSWORD=...
```

Required runtime variables also include `JWT_SECRET`, `AI_INTERNAL_TOKEN`, `AI_SERVICE_URL`, `GENERATION_FACTOR_KWH_PER_KW_YEAR`, `INSTALLATION_COST_PER_KW`, `GRID_EMISSION_FACTOR`, `ROOF_LAYOUT_FACTOR`, the three `ROOF_TYPE_USABLE_FACTOR_*` values, `CSV_IMPORT_ENABLED`, `PANELS_CSV_PATH`, and `LOCATIONS_CSV_PATH`. The frontend may use `VITE_API_URL`.

Create the database once:

```sql
CREATE DATABASE solarmind
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

## Backend profiles

The project has two Spring profiles:

| Profile | Use | Schema handling | CSV/AI behavior |
| --- | --- | --- | --- |
| `local` | Normal local launcher | Flyway enabled; Hibernate `validate` | CSV import enabled; AI points to `http://localhost:8000`; local JWT expiry is one hour |
| `dev` | Deliberate schema-development work | Flyway remains enabled; Hibernate `update` | Uses the base environment-backed settings |

Use `local` for ordinary development. Use `dev` only when you intentionally want Hibernate to update the MySQL schema while developing. Both profiles use MySQL; neither uses H2.

### PowerShell launcher

Set the required variables first, then run from the repository root:

```powershell
.\backend\run-dev.ps1
```

The script loads `.env` from the repository root when it exists, without
overwriting variables already set in the shell. It then fails if
`DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `JWT_SECRET`, or
`AI_INTERNAL_TOKEN` is missing. It supplies absolute paths for the two
repository CSV files and starts the `local` profile.

If PowerShell execution policy blocks local scripts, run it explicitly with:

```powershell
powershell.exe -ExecutionPolicy Bypass -File .\backend\run-dev.ps1
```

### Bash launcher

```bash
export DATABASE_URL='jdbc:mysql://localhost:3306/solarmind'
export DATABASE_USERNAME='your-user'
export DATABASE_PASSWORD='your-password'
export JWT_SECRET='a-long-random-secret'
export AI_INTERNAL_TOKEN='a-long-random-internal-token'
./backend/run-dev.sh
```

The Bash launcher performs the same required-variable checks and starts the `local` profile.

### Start the `dev` profile manually

```powershell
cd backend
mvn spring-boot:run "-Dspring-boot.run.profiles=dev"
```

## Flyway behavior and baseline

Flyway runs migrations from `backend/src/main/resources/db/migration/` before the application uses the repositories. New databases should be empty so Flyway can apply `V1__create_schema.sql` and `V2__remove_product_link.sql` normally.

`baseline-on-migrate` is not enabled by this project. If an existing non-empty database already contains the SolarMind tables but has no `flyway_schema_history`, take a backup and confirm that its schema matches the migration baseline. Then perform a one-time baseline using the deployment's Flyway/Spring configuration, for example with `SPRING_FLYWAY_BASELINE_ON_MIGRATE=true` and the intended baseline version. Remove that temporary setting afterward. Do not baseline a new empty database and do not edit migrations that have already been applied.

Inspect migration state in MySQL:

```sql
USE solarmind;
SELECT installed_rank, version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

## Start the frontend

In a second terminal:

```powershell
cd frontend
npm install
npm run dev
```

Open http://localhost:5173. Set `VITE_API_URL` if the backend is not at `http://localhost:8080`.

## Start the AI service

The AI service is required for assistant answers but not for the basic catalog and assessment pages.

```powershell
cd ai-service
Copy-Item .env.example .env
pip install -r requirements.txt
.\run-dev.ps1
```

Set `AI_INTERNAL_TOKEN`, `GEMINI_API_KEY`, and `GEMINI_MODEL` in
`ai-service/.env`. The launcher also accepts those values from the repository
`.env`. The service sends the Gemini key as the `x-goog-api-key` header and
validates the backend token with constant-time comparison.

Health checks:

```powershell
Invoke-RestMethod http://localhost:8080/api/health
Invoke-RestMethod http://localhost:8000/health
```

## Verify persisted data

```sql
USE solarmind;
SHOW TABLES;
SELECT COUNT(*) AS users FROM users;
SELECT COUNT(*) AS locations FROM locations;
SELECT COUNT(*) AS panels FROM solar_panels;
SELECT COUNT(*) AS assessments FROM assessments;

SELECT a.id, a.user_id, u.email, a.created_at
FROM assessments a
JOIN users u ON u.id = a.user_id
ORDER BY a.created_at DESC;
```

Passwords must be stored as hashes, not readable credentials. Assessments must be linked to the authenticated user's `user_id`.

## Automated checks

Backend tests:

```powershell
cd backend
mvn test
```

AI tests:

```powershell
cd ai-service
pytest
```

Frontend production build:

```powershell
cd frontend
npm run build
```

Repository whitespace check:

```powershell
cd ..
git diff --check
```

## Main manual flow

1. Register and confirm the account is authenticated.
2. Create an assessment with a valid location and budget.
3. Confirm the result displays an assessment ID.
4. Confirm the dashboard shows the assessment and expands its cost, payback, roof, and budget details.
5. Log out and back in; confirm the assessment remains in MySQL and the dashboard.
6. Open the assistant and confirm it loads the selected assessment details.
7. Try a budget below the cheapest valid system and confirm the backend reason is shown.
