import type {
  AskRequest,
  AskResponse,
  AssessmentRequest,
  AssessmentResponse,
  AuthResponse,
  AssessmentSummary,
  DashboardResponse,
  LocationResponse,
  PanelResponse,
  PagedResponse,
} from "../types";
const base = import.meta.env.VITE_API_URL || "http://localhost:8080";
export class ApiError extends Error {
  status?: number;
  fieldMessages: Record<string, string>;

  constructor(message: string, status?: number, fieldMessages: Record<string, string> = {}) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.fieldMessages = fieldMessages;
  }
}

function parseFieldMessages(message: string) {
  const fields: Record<string, string> = {};
  const match = message.match(/^([A-Za-z][\w.]*)\s*:\s*(.+)$/);
  if (match) fields[match[1]] = match[2];
  return fields;
}

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
  if (!r.ok) {
    const text = await r.text();
    let payload: { message?: string; detail?: string; status?: number } = {};
    try {
      payload = JSON.parse(text);
    } catch {
      // The fallback below keeps network/proxy errors user-readable.
    }
    const message = payload.message || payload.detail || text || "Request failed";
    throw new ApiError(message, payload.status || r.status, parseFieldMessages(message));
  }
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
  createAssessment: (body: AssessmentRequest) =>
    request<AssessmentResponse>("/api/assessments", {
      method: "POST",
      body: JSON.stringify(body),
    }),
  assessments: () => request<AssessmentSummary[]>("/api/assessments"),
  assessment: (id: number) => request<AssessmentResponse>(`/api/assessments/${id}`),
  panel: (id: number) => request<PanelResponse>(`/api/panels/${id}`),
  dashboard: () => request<DashboardResponse>("/api/dashboard"),
  ask: (body: AskRequest) =>
    request<AskResponse>("/api/ai/ask", {
      method: "POST",
      body: JSON.stringify(body),
    }),
};
