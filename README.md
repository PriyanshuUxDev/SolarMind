# ☀️ SolarMind

**A rooftop-solar recommendation platform for homes in Delhi-NCR and Punjab.**

Tell SolarMind how much electricity you use, what you pay, how big your roof is and what you can spend. It picks a panel from a real Indian panel catalogue, sizes the system, and shows what it would cost, how much you would save, how long it takes to pay back, and how much CO₂ you avoid. An AI assistant then explains the result in plain language.

![Java](https://img.shields.io/badge/Java-17-orange) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.5-6DB33F) ![React](https://img.shields.io/badge/React-19-61DAFB) ![TypeScript](https://img.shields.io/badge/TypeScript-blue) ![FastAPI](https://img.shields.io/badge/FastAPI-0.115-009688) ![MySQL](https://img.shields.io/badge/MySQL-8-4479A1)

---

## Table of contents

1. [What SolarMind does](#what-solarmind-does)
2. [Architecture](#architecture)
3. [Tech stack](#tech-stack)
4. [How a recommendation is calculated](#how-a-recommendation-is-calculated)
5. [The dashboard](#the-dashboard)
6. [The AI assistant](#the-ai-assistant)
7. [Project structure](#project-structure)
8. [Data and database](#data-and-database)
9. [API reference](#api-reference)
10. [Security](#security)
11. [Configuration](#configuration)
12. [Getting started](#getting-started)
13. [Testing](#testing)
14. [Design decisions](#design-decisions)
15. [Known limitations and future work](#known-limitations-and-future-work)
16. [Author](#author)

---

## What SolarMind does

| Feature | Description |
| --- | --- |
| **Accounts** | Register and log in. Every assessment belongs to its owner and nobody else can read it. |
| **Location search** | Search more than 7,700 villages, towns and districts across Punjab and Delhi-NCR. |
| **Panel catalogue** | Browse, search, filter and sort around 30 solar panels from Indian suppliers by brand, wattage and efficiency. |
| **Assessment** | Enter consumption, bill, roof area, roof type and budget to get a sized system recommendation. |
| **Dashboard** | See your latest assessment with charts: cumulative cost with and without solar, bill before and after, solar coverage, roof usage, budget fit and lifetime savings. |
| **AI assistant** | Ask questions about a saved assessment and get a plain-language explanation, grounded only in the numbers the backend calculated. |

---

## Architecture

SolarMind is three services plus a database. Each has one clear job.

```
┌────────────────┐    REST + JWT     ┌──────────────────────┐        ┌───────────┐
│  React + Vite  │ ────────────────► │   Spring Boot API    │ ─────► │  MySQL 8  │
│   (frontend)   │ ◄──────────────── │      (backend)       │ ◄───── │  (Flyway) │
│  :5173         │                   │      :8080           │        └───────────┘
└────────────────┘                   └──────────┬───────────┘
                                                │ X-Internal-Token
                                                ▼
                                     ┌──────────────────────┐        ┌──────────┐
                                     │   FastAPI service    │ ─────► │  Gemini  │
                                     │    (ai-service)      │        │   API    │
                                     │    :8000             │        └──────────┘
                                     └──────────────────────┘
```

| Service | Responsibility | Deliberately does **not** |
| --- | --- | --- |
| **Frontend** | Collect inputs, call the API, render results and charts. | Do any recommendation maths. |
| **Backend** | Auth, validation, persistence, CSV seeding, all calculations, ownership checks, error handling. | Talk to Gemini directly. |
| **AI service** | Authenticate the backend, call Gemini, explain verified results. | Recalculate or alter any numbers. |

The key idea is that **the backend is the single source of truth**. The AI only explains numbers; it never produces them.

---

## Tech stack

**Backend** – Java 17, Spring Boot 3.4.5, Spring Web, Spring Data JPA (Hibernate), Spring Security, Bean Validation, Flyway, JJWT 0.12.6, BCrypt, MySQL Connector/J. Built with Maven.

**Frontend** – React 19, TypeScript, Vite, React Router 7, Recharts, plain CSS with a custom design system, Fontsource fonts (Bricolage Grotesque and Instrument Sans).

**AI service** – Python 3.11+, FastAPI, Uvicorn, Pydantic Settings, HTTPX, Google Gemini (`generateContent` REST API).

**Database** – MySQL 8 with versioned Flyway migrations.

---

## How a recommendation is calculated

All of this lives in `RecommendationService` and is configured through environment-backed assumptions, so every number can be audited and changed without touching code.

1. **Annual consumption** = `monthlyConsumption × 12`
2. **Effective tariff** = `monthlyBill ÷ monthlyConsumption` (₹ per kWh the user really pays)
3. **Required capacity (kW)** = `annualConsumption ÷ generationFactor` (kWh per kW per year)
4. **Usable roof area** = `roofArea × roofTypeFactor` (separate factors for `FLAT`, `SLOPED`, `OTHER`)
5. **Candidate panels.** Every panel with a valid wattage and parseable dimensions is considered. Dimensions such as `2266 x 1133 x 35` are read as millimetres and turned into a panel area in m².
6. **For each candidate:**
   - Panel count = `ceil(requiredCapacityKw × 1000 ÷ panelWattage)`
   - Actual capacity = `panelCount × wattage ÷ 1000`
   - Estimated cost = `actualCapacityKw × installationCostPerKw`
   - Required roof area = `panelArea × panelCount × roofLayoutFactor`
   - **Roof feasible?** required roof area ≤ usable roof area
   - **Within budget?** estimated cost ≤ budget
7. **Selection.** Roof-feasible systems are ranked first, then by highest panel efficiency, then by highest wattage. The top option wins.
8. **Outputs for the winner:**
   - Annual generation = `actualCapacityKw × generationFactor`
   - Annual savings = `annualGeneration × tariff`
   - Payback (years) = `estimatedCost ÷ annualSavings`
   - CO₂ avoided (kg) = `annualGeneration × gridEmissionFactor`, also reported in tonnes

The response always includes the formulas and the assumption values used, so the UI can show users exactly how the figures were derived.

### Input limits

| Field | Rule | Maximum |
| --- | --- | --- |
| `monthlyConsumption` (kWh) | greater than 0 | 100,000 |
| `monthlyBill` | 0 or more | 10,000,000 |
| `roofArea` (m²) | greater than 0 | 1,000,000 |
| `budget` | greater than 0 | 1,000,000,000 |
| `roofType` | `FLAT`, `SLOPED` or `OTHER` | – |

---

## The dashboard

`DashboardService` turns the stored assessment into chart-ready data over a planning horizon (default **25 years**):

- **Cumulative cost comparison.** Year-by-year total spend without solar versus with solar (installation cost plus remaining electricity bills).
- **Monthly bill comparison.** Bill before and after solar.
- **Solar coverage %.** Annual generation as a share of annual consumption, capped at 100%.
- **Roof utilisation %.** Required roof area as a share of the actual roof.
- **Budget fit.** Budget, cost, difference and a within-budget flag.
- **Lifetime savings.** Sum of yearly savings over the horizon.
- **Selected panel summary.** Brand, model, wattage and efficiency.

Tariff escalation and panel degradation are supported but default to **0%**, so the defaults make no optimistic claims. Set `TARIFF_ESCALATION_RATE` and `PANEL_DEGRADATION_RATE` only when you have evidence to back them.

---

## The AI assistant

1. The user asks a question (up to 500 characters) in the **Assistant** page, optionally tied to a saved assessment.
2. The Spring backend loads that assessment, **checking that it belongs to the logged-in user**, and sends the question plus the verified assessment to the FastAPI service with an `X-Internal-Token` header.
3. FastAPI validates the token using a constant-time comparison, then calls Gemini with a strict system instruction.
4. The answer comes back through the backend to the UI.

The system instruction tells the model to use only the supplied values, never recalculate or change them, say when a value is missing, label generation, savings, cost, payback and CO₂ as **estimates**, and never reveal personal identifiers, secrets or tokens.

The AI service is optional. Catalogue, assessment and dashboard pages work without it.

---

## Project structure

```
solarMind/
├── backend/                     Spring Boot API
│   ├── pom.xml
│   ├── run-dev.sh / run-dev.ps1 Launchers (load .env, check required vars, start "local" profile)
│   └── src/
│       ├── main/java/com/solarmind/
│       │   ├── config/          Typed settings: Solar, Ai, Jwt, Cors, Csv, RestClient
│       │   ├── controller/      Auth, Location, Panel, Assessment, Dashboard, AI, Health
│       │   ├── dto/             Validated request and response records
│       │   ├── entity/          User, Location, SolarPanel, Assessment
│       │   ├── exception/       Custom exceptions and a global JSON error handler
│       │   ├── importer/        Idempotent CSV seeding for panels and locations
│       │   ├── repository/      Spring Data JPA repositories
│       │   ├── security/        JWT filter and util, Security config, current-user helper
│       │   └── service/         Business logic (Recommendation, Assessment, Dashboard, AI, …)
│       ├── main/resources/
│       │   ├── application*.yml Base, local and dev profiles
│       │   └── db/migration/    V1__create_schema.sql, V2__remove_product_link.sql
│       └── test/                Unit and MockMvc tests
│
├── frontend/                    React + TypeScript + Vite
│   └── src/
│       ├── pages/               Home, Login, Register, Dashboard, Assessment, Panels, Assistant
│       ├── components/          Reusable UI kit (Button, Input, Drawer, StatCard, SunArc, …)
│       ├── context/             AuthContext (JWT session)
│       ├── motion/              Reveal animation and reduced-motion support
│       ├── services/api.ts      Typed API client
│       └── types/               Shared TypeScript types
│
├── ai-service/                  FastAPI Gemini boundary
│   ├── main.py
│   ├── test_main.py
│   └── run-dev.sh / run-dev.ps1
│
├── data/
│   ├── solar_panels_india.csv           Panel catalogue (read-only source of truth)
│   └── punjab_delhi_ncr_coordinates.csv Locations with coordinates (read-only)
│
├── docs/                        How-to-run guide, architecture map, Postman collection
├── context/                     Product and design planning documents
├── .env.example                 Template for every required variable
└── README.md
```

---

## Data and database

### Seed data

| File | Contents |
| --- | --- |
| `data/solar_panels_india.csv` | Brand, model, wattage, daily and monthly output, efficiency, Vmpp, Impp, dimensions, weight. |
| `data/punjab_delhi_ncr_coordinates.csv` | Region, city, district, state, latitude and longitude for 7,700+ places. |

On startup the importers load both files into MySQL. Imports are **idempotent** (unique constraints stop duplicates) and the CSVs are **never modified**. The panel CSV has a `Product Link` column that the application intentionally ignores, and the location CSV has blank columns that are skipped.

### Tables

| Table | Purpose |
| --- | --- |
| `users` | `name`, unique `email`, BCrypt-hashed `password`, `created_at`. |
| `locations` | Region, city, district, state, latitude, longitude. Unique on the combination of those fields. |
| `solar_panels` | Catalogue specs. Unique on brand plus model. |
| `assessments` | Inputs, the chosen panel, and every calculated output. Foreign keys to user, location and panel, with an index on `(user_id, created_at)`. |

Schema changes are managed only through Flyway migrations in `backend/src/main/resources/db/migration/`. Never edit a migration that has already been applied; add a new one.

---

## API reference

Base URL: `http://localhost:8080`. Everything except `/api/auth/**` and `/api/health` needs `Authorization: Bearer <token>`.

| Method | Endpoint | Purpose |
| --- | --- | --- |
| `GET` | `/api/health` | Public health check. Returns `{"status":"ok"}`. |
| `POST` | `/api/auth/register` | Create an account and receive a JWT. |
| `POST` | `/api/auth/login` | Log in and receive a JWT. |
| `GET` | `/api/locations/search?query=&page=&limit=` | Search seeded locations. |
| `GET` | `/api/panels` | List panels. Query params: `page`, `size`, `search`, `brand`, `minWattage`, `maxWattage`, `minEfficiency`, `sort`, `direction`. |
| `GET` | `/api/panels/{id}` | One panel's full details. |
| `POST` | `/api/assessments` | Validate inputs, calculate, persist and return a recommendation. |
| `GET` | `/api/assessments` | The user's 10 most recent assessments (summaries). |
| `GET` | `/api/assessments/{id}` | One full assessment, only if owned by the caller. |
| `GET` | `/api/dashboard` | Latest assessment, recent history and chart data. |
| `POST` | `/api/ai/ask` | Ask the AI to explain an assessment. |

**Example: create an assessment**

```http
POST /api/assessments
Authorization: Bearer <token>
Content-Type: application/json

{
  "locationId": 120,
  "monthlyConsumption": 450,
  "monthlyBill": 3600,
  "roofArea": 60,
  "roofType": "FLAT",
  "budget": 400000
}
```

Errors come back as consistent JSON from the global exception handler (validation problems, bad credentials, missing resources, AI service unavailable, and so on). A ready-made **Postman collection** is in `docs/solarmind.postman_collection.json`.

---

## Security

- **Stateless JWT authentication** with a configurable secret and expiry (1 hour in the `local` profile).
- **BCrypt** password hashing; plain-text passwords are never stored.
- **Ownership checks.** Assessments are always queried by both ID and the authenticated user, so one user cannot read another's data.
- **CORS** is restricted to configured origins (defaults to `localhost:5173` and `localhost:5174`).
- **Internal service token** between the backend and the AI service, compared in constant time.
- **No secrets in the repo.** `.env` files are git-ignored, `.env.example` contains placeholders only, and the AI service refuses to run if values still look like `PLACEHOLDER_…`.
- **Input validation** on every request DTO, with hard upper limits.
- **No hidden assumptions.** The app will not start without the solar assumptions being set, so there are no "magic" defaults baked in.

---

## Configuration

Copy `.env.example` to `.env` in the repository root and replace every `PLACEHOLDER_*` value. **Never commit `.env`.**

| Variable | Purpose |
| --- | --- |
| `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD` | MySQL connection. |
| `JWT_SECRET` | Long random secret used to sign tokens. |
| `JWT_EXPIRATION_MS` | Token lifetime (the `local` profile fixes this at one hour). |
| `AI_SERVICE_URL` | Where the AI service lives, e.g. `http://localhost:8000`. |
| `AI_INTERNAL_TOKEN` | Shared secret between backend and AI service. |
| `GEMINI_API_KEY`, `GEMINI_MODEL` | Gemini credentials and model name. |
| `GENERATION_FACTOR_KWH_PER_KW_YEAR` | Annual kWh produced per installed kW. |
| `INSTALLATION_COST_PER_KW` | Installed cost per kW. |
| `GRID_EMISSION_FACTOR` | kg CO₂ per kWh of grid electricity displaced. |
| `ROOF_LAYOUT_FACTOR` | Extra space factor for spacing and access around panels. |
| `ROOF_TYPE_USABLE_FACTOR_FLAT` / `_SLOPED` / `_OTHER` | Share of each roof type that is usable. |
| `CSV_IMPORT_ENABLED`, `PANELS_CSV_PATH`, `LOCATIONS_CSV_PATH` | CSV seeding controls. |
| `SYSTEM_LIFESPAN_YEARS` *(optional, default 25)* | Dashboard planning horizon. |
| `TARIFF_ESCALATION_RATE` *(optional, default 0)* | Yearly tariff growth. |
| `PANEL_DEGRADATION_RATE` *(optional, default 0)* | Yearly output loss. |
| `AI_TIMEOUT` *(optional, default 20s)* | Backend timeout for AI calls. |
| `CORS_ALLOWED_ORIGIN` *(optional)* | Comma-separated allowed origins. |
| `VITE_API_URL` | Backend URL for the frontend (defaults to `http://localhost:8080`). |

### Spring profiles

| Profile | Use | Schema handling |
| --- | --- | --- |
| `local` | Normal day-to-day development (used by the launchers). | Flyway on, Hibernate `validate`, CSV import on. |
| `dev` | Deliberate schema experiments. | Flyway on, Hibernate `update`. |

Both profiles use MySQL. There is no H2 database.

---

## Getting started

### Prerequisites

- Java 17+
- Maven 3.9+ (or the one bundled with IntelliJ IDEA)
- Node.js 20+ and npm
- Python 3.11+ (for the AI service)
- MySQL 8

### 1. Clone and configure

```bash
git clone https://github.com/PriyanshuUxDev/SolarMind.git
cd SolarMind
cp .env.example .env     # then edit .env and replace every PLACEHOLDER_* value
```

### 2. Create the database

```sql
CREATE DATABASE solarmind
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
```

Start with an **empty** database so Flyway can apply the migrations from scratch.

### 3. Start the backend

Windows (PowerShell):

```powershell
.\backend\run-dev.ps1
```

macOS / Linux:

```bash
./backend/run-dev.sh
```

The launcher loads `.env`, checks that the required variables exist, and starts the `local` profile. On first run it applies the Flyway migrations and seeds the CSV data. Confirm it works:

```
GET http://localhost:8080/api/health   →   {"status":"ok"}
```

### 4. Start the AI service (optional)

```bash
cd ai-service
cp .env.example .env     # set AI_INTERNAL_TOKEN, GEMINI_API_KEY, GEMINI_MODEL
pip install -r requirements.txt
python -m uvicorn main:app --reload --port 8000
```

Check it at `http://localhost:8000/health`. `AI_INTERNAL_TOKEN` must match the value used by the backend.

### 5. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173**.

### Try it end to end

1. Register an account.
2. Open **Assessment**, search for your town, and fill in consumption, bill, roof and budget.
3. Check the result and its assessment ID.
4. Open the **Dashboard** to see the charts.
5. Open the **Assistant** and ask something like *"Why was this panel chosen?"*
6. Log out and back in, and confirm your assessment is still there.

For more detail (Flyway baselining, SQL checks, troubleshooting) see [`docs/how-to-run.md`](docs/how-to-run.md).

---

## Testing

```bash
# Backend: 17 tests (recommendation maths, assessments, dashboard, auth, access control, error handling)
cd backend && mvn test

# AI service
cd ai-service && pytest

# Frontend type-check and production build
cd frontend && npm run build
```

Notable coverage: recommendation calculations, assessment ownership (`AssessmentAccessMockMvcTest`), dashboard calculations, authentication, and the global JSON error handler.

---

## Design decisions

- **Backend owns the maths.** Keeping calculations server-side means one tested implementation, and results can be verified and stored.
- **AI as explainer, not calculator.** The model is told to use only supplied values, which avoids made-up figures.
- **Assumptions are configuration.** Generation factor, costs, emission factor and roof factors are explicit environment settings, not buried constants.
- **Conservative dashboard defaults.** Zero tariff growth and zero degradation unless you configure otherwise.
- **Idempotent, read-only seeding.** The source CSVs stay untouched and repeated starts do not create duplicates.
- **Migrations over auto-DDL.** Flyway versions the schema; Hibernate only validates in normal use.
- **Accessible, restrained UI.** A custom design kit with reduced-motion support and no heavy CSS framework.

---

## Known limitations and future work

- **Location does not yet change the estimate.** Generation uses a single configured factor for every location. The `GenerationEstimator` interface exists so a location-aware (irradiance-based) estimator can be plugged in without touching the rest of the code.
- **Budget is reported, not enforced.** The recommendation chooses the best roof-feasible panel and flags whether it is within budget; it does not currently search for a cheaper option that fits the budget.
- **Estimates only.** Costs, savings, payback and CO₂ are planning estimates, not quotes or guarantees. Subsidies, net-metering rules, financing and tax effects are not modelled.
- **Timeouts.** The `local` profile gives AI calls 5 seconds in the backend while the AI service allows 20 seconds for Gemini, so slow responses may time out locally. Raise `solarmind.ai.timeout` if needed.
- **Coverage area.** Location data covers Punjab and Delhi-NCR only.

Ideas for next steps: location-based irradiance data, multi-panel comparison, subsidy and net-metering support, PDF report export, Docker Compose for one-command startup, and CI that runs the test suites on every push.

---

## Author

Built by **Priyanshu Kardam**

GitHub: [@PriyanshuUxDev](https://github.com/PriyanshuUxDev) · Email: priyanshukardam223@gmail.com
