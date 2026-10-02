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
  dailyOutput?: number;
  monthlyOutput?: number;
  efficiency: number;
  vmpp?: number;
  impp?: number;
  dimensions: string;
  weight?: number;
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
  co2Kg: number;
  co2Tonnes: number;
  effectiveTariff: number;
  requiredRoofArea: number;
  roofFeasible: boolean;
  withinBudget: boolean;
  assumptionsUsed: Record<string, unknown>;
  createdAt: string;
};
export type AssessmentSummary = {
  id: number;
  locationLabel: string;
  selectedPanelBrand: string;
  selectedPanelModel: string;
  recommendedCapacityKw: number;
  actualCapacityKw: number;
  annualSavings: number;
  estimatedCost: number;
  paybackYears: number | null;
  roofFeasible: boolean;
  withinBudget: boolean;
  createdAt: string;
};
export type DashboardResponse = {
  latest: AssessmentResponse | null;
  recentAssessments: AssessmentSummary[];
  cumulativeCostComparison: {
    year: number;
    withoutSolar: number;
    withSolar: number;
  }[];
  monthlyBillComparison: {
    before: number;
    after: number;
  } | null;
  solarCoveragePercent: number | null;
  budgetFit: {
    budget: number;
    estimatedCost: number;
    difference: number;
    withinBudget: boolean;
  } | null;
  roofUtilizationPercent: number | null;
  lifetimeSavings: number | null;
  selectedPanel: {
    brand: string;
    model: string;
    wattage: number;
    efficiency: number;
  } | null;
  assessmentDashboards: DashboardAssessment[];
};
export type DashboardAssessment = {
  assessmentId: number;
  assessment: AssessmentSummary;
  panelCount: number;
  annualGeneration: number;
  cumulativeCostComparison: {
    year: number;
    withoutSolar: number;
    withSolar: number;
  }[];
  monthlyBillComparison: { before: number; after: number } | null;
  solarCoveragePercent: number | null;
  budgetFit: {
    budget: number;
    estimatedCost: number;
    difference: number;
    withinBudget: boolean;
  } | null;
  roofUtilizationPercent: number | null;
  lifetimeSavings: number | null;
  selectedPanel: {
    brand: string;
    model: string;
    wattage: number;
    efficiency: number;
  } | null;
};
export type ApiErrorPayload = {
  timestamp?: string;
  status?: number;
  error?: string;
  message?: string;
  path?: string;
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
