export type User = { id: number; name: string; email: string };
export type AuthResponse = { token: string; user: User };
export type AskRequest = { question: string; assessmentId?: number };
export type AskResponse = { answer: string };
export type LocationResponse = {
  id: number;
  region: string;
  city: string;
  district: string;
  state: string;
  latitude: number;
  longitude: number;
};
export type PanelResponse = {
  id: number;
  brand: string;
  model: string;
  wattage: number;
  efficiency: number;
  dimensions: string;
};
export type AssessmentResponse = {
  id: number;
  location: LocationResponse;
  selectedPanel: PanelResponse;
  recommendedCapacityKw: number;
  panelCount: number;
  actualCapacityKw: number;
  annualGeneration: number;
  annualSavings: number;
  estimatedCost: number;
  paybackYears: number | null;
  co2Reduction: number;
  effectiveTariff: number;
  requiredRoofArea: number;
  roofFeasible: boolean;
  withinBudget: boolean;
  assumptionsUsed: Record<string, unknown>;
  createdAt: string;
};
export type AssessmentSummary = {
  id: number;
  recommendedCapacityKw: number;
  annualSavings: number;
  createdAt: string;
};
export type DashboardResponse = {
  latest: AssessmentResponse | null;
  recentAssessments: AssessmentSummary[];
};
export type PagedResponse<T> = {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};
export type AssessmentRequest = {
  locationId: number;
  monthlyConsumption: number;
  monthlyBill: number;
  roofArea: number;
  roofType: string;
  budget: number;
};
