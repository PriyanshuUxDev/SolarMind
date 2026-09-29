import type {
  AskRequest,
  AskResponse,
  AssessmentRequest,
  AssessmentResponse,
  AuthResponse,
  DashboardResponse,
  LocationResponse,
  PanelResponse,
  PagedResponse,
} from "../types";
const base = import.meta.env.VITE_API_URL || "http://localhost:8080";
async function request<T>(path: string, init: RequestInit = {}): Promise<T> {
  const token = localStorage.getItem("solarmind_token");
  const r = await fetch(`${base}${path}`, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(init.headers || {}),
    },
  });
  if (r.status === 401) {
    localStorage.removeItem("solarmind_token");
    window.location.href = "/login";
  }
  if (!r.ok) throw new Error(await r.text());
  return r.json();
}
export const api = {
  health: () => request<{ status: string }>("/api/health"),
  register: (body: unknown) =>
    request<AuthResponse>("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  login: (body: unknown) =>
    request<AuthResponse>("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  locations: (q: string) =>
    request<PagedResponse<LocationResponse>>(
      `/api/locations/search?query=${encodeURIComponent(q)}&limit=10`,
    ),
  panels: (params = "") =>
    request<PagedResponse<PanelResponse>>(`/api/panels${params}`),
  assessment: (body: AssessmentRequest) =>
    request<AssessmentResponse>("/api/assessments", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  assessments: () => request<AssessmentResponse[]>("/api/assessments"),
  dashboard: () => request<DashboardResponse>("/api/dashboard"),
  ask: (body: AskRequest) =>
    request<AskResponse>("/api/ai/ask", {
      method: "POST",
      body: JSON.stringify(body),
    }),
};
