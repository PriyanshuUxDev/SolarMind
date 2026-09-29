# Architecture map

## Current tree

```text
frontend/             React + TypeScript + Vite skeleton
backend/              Spring Boot + Maven skeleton
ai-service/           FastAPI skeleton
data/raw/             Reserved immutable CSV location
docs/                 Project documentation
context/              Binding source documents
```

The supplied CSVs currently exist under `data/`, not `data/raw/`, and were not moved or modified. Their headers are recorded in the phase notes: `solar_panels_india.csv` has Brand, Panel Name/Model, Wattage (W), Daily Output, Monthly Output, Efficiency, Vmpp, Impp, Dimensions, Weight, Product Link. `punjab_delhi_ncr_coordinates.csv` has region, city, two blank columns, district, state, latitude, longitude. Import mapping is intentionally deferred.

Phase 1 now validates those headers at startup, imports only valid mapped rows, skips existing panel `(brand, model)` and location `(city, district, state, latitude, longitude)` keys, logs imported/duplicate/malformed counts, and performs no CSV writes. Phase 2 owns BCrypt registration, JWT issue/validation, protected assessment ownership, and generic authentication errors. Phase 3 owns location/panel search. Phase 4 owns all recommendation math in `RecommendationService`. Phase 8 owns the guarded FastAPI Gemini boundary.

## Boundaries

Spring Boot owns authentication, persistence, validation, recommendation orchestration, and API DTOs. FastAPI is only the internal AI explanation boundary. React only collects and displays data.

## Backend class inventory

| Layer | Classes | Responsibility |
|---|---|---|
| Controller | AuthController, LocationController, PanelController, AssessmentController, DashboardController, AIController, HealthController | Map the documented REST endpoints and delegate to services; health is the smoke-test endpoint. |
| Service | AuthService, LocationService, PanelService, AssessmentService, RecommendationService, DashboardService, AIService | Stable service boundaries; methods currently return skeleton 501 responses. |
| Service seam | GenerationEstimator, ConfigGenerationEstimator | Future generation-estimation boundary; no calculation yet. |
| Repository | UserRepository, LocationRepository, SolarPanelRepository, AssessmentRepository | JPA repository boundaries for the four entities. |
| Entity | User, Location, SolarPanel, Assessment | Persistence model with documented relationships and indexes. |
| DTO | AuthRequests, AssessmentRequest, AskRequest; AuthResponse, AssessmentResponse, AssessmentSummary, DashboardResponse, PanelResponse, LocationResponse, AskResponse, PagedResponse, ApiError | Request validation and response contracts; entities are not returned by controllers. |
| Security | SecurityConfig, JwtUtil, JwtAuthFilter, CustomUserDetailsService, CurrentUser, JsonAuthenticationEntryPoint, JsonAccessDeniedHandler | Stateless JWT filter chain, CORS, and JSON 401/403 responses. |
| Config | SolarProperties, AiProperties, JwtProperties, CorsProperties, RestClientConfig | Environment-backed configuration and AI HTTP client bean. |
| Exceptions | ResourceNotFoundException, InvalidInputException, NoSuitablePanelException, AiServiceUnavailableException, NotImplementedException, GlobalExceptionHandler | Error taxonomy and JSON error mapping. |
| Importer | PanelCsvImporter, LocationCsvImporter | ApplicationRunner placeholders; parsing is deferred until CSV mapping is confirmed. |

## Endpoint map

| Endpoint | Controller | Service |
|---|---|---|
| POST `/api/auth/register` | AuthController | AuthService |
| POST `/api/auth/login` | AuthController | AuthService |
| GET `/api/locations/search` | LocationController | LocationService |
| GET `/api/panels`, GET `/api/panels/{id}` | PanelController | PanelService |
| POST `/api/assessments`, GET `/api/assessments/{id}` | AssessmentController | AssessmentService → RecommendationService later |
| GET `/api/dashboard` | DashboardController | DashboardService |
| POST `/api/ai/ask` | AIController | AIService → FastAPI later |
| GET `/api/health` | HealthController | none |

## Future ticket placement

SM-001: data inspection notes. SM-002: repository/build files. SM-003: `backend/config`. SM-101–104: `entity`, `repository`, `importer`. SM-201–204: `security` and `AuthService`. SM-301–304: location/panel controllers and services. SM-401–409: assessment and recommendation service. SM-501: dashboard. SM-601–708: frontend pages/components. SM-801–804: `ai-service` and `AIService`. SM-901–906: tests, docs, and audit.

## Decisions and open confirmations

- JPA `ddl-auto=update` is used only for the development profile; production is set to validate. This avoids adding a migration tool in the skeleton phase.
- Assumption values remain placeholders and fail fast until supplied.
- No CSV was moved because the working rule forbids modifying source data.
- Confirm whether the two blank location CSV columns are intentionally unnamed and which source file should be canonical before import work.
- Confirm concrete generation, cost, emission, layout, roof-factor, JWT, database, and AI secret values before running production configuration.
