import { useEffect, useState } from "react";
import type { ReactNode } from "react";
import { Link } from "react-router-dom";
import {
  Bar, BarChart, CartesianGrid, Legend, Line, LineChart,
  ResponsiveContainer, Tooltip, XAxis, YAxis,
} from "recharts";
import { api, ApiError } from "../services/api";
import type { AssessmentSummary, DashboardResponse } from "../types";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import EmptyState from "../components/EmptyState";
import StatCard from "../components/StatCard";

const number = (value: number | null | undefined) => value == null ? "—" : value.toLocaleString("en-IN", { maximumFractionDigits: 2 });
const money = (value: number) => `₹${number(value)}`;
const chartMoney = (value: number) => `₹${Math.round(value).toLocaleString("en-IN")}`;

export default function Dashboard() {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [error, setError] = useState("");
  const [pendingAssessmentId, setPendingAssessmentId] = useState<number | null>(null);
  const [activeAssessmentId, setActiveAssessmentId] = useState<number | null>(null);

  function load() {
    setError("");
    api.dashboard().then(setData).catch((cause) => {
      setError(cause instanceof ApiError
        ? `${cause.message}${cause.status ? ` (HTTP ${cause.status})` : ""}`
        : "The dashboard could not be loaded.");
    });
  }
  useEffect(load, []);

  if (error) return <main id="main-content" className="page-shell"><ErrorMessage message={error} onRetry={load} /></main>;
  if (!data) return <main id="main-content" className="page-shell"><Loading label="Loading your dashboard" /></main>;
  if (!data.latest) return <main id="main-content" className="page-shell"><PageIntro eyebrow="Your solar notebook" title="Dashboard" copy="Keep your latest estimate and past assessments in one place." /><EmptyState title="No saved assessments yet" message="Complete an assessment to see estimated savings, coverage, and payback here." /></main>;

  const latest = data.latest;
  const assessments = data.recentAssessments ?? [];
  const dashboards = data.assessmentDashboards ?? [];
  const activeDashboard = dashboards.find((item) => item.assessmentId === activeAssessmentId) ?? null;
  const activeSummary = activeDashboard?.assessment ?? assessments[0] ?? null;
  const activeCosts = activeDashboard?.cumulativeCostComparison ?? data.cumulativeCostComparison ?? [];
  const activeMonthlyBill = activeDashboard?.monthlyBillComparison ?? data.monthlyBillComparison;
  const activeBudgetFit = activeDashboard?.budgetFit ?? data.budgetFit;
  const activePanel = activeDashboard?.selectedPanel ?? data.selectedPanel;
  const activeCoverage = activeDashboard?.solarCoveragePercent ?? data.solarCoveragePercent;
  const activeRoofUtilization = activeDashboard?.roofUtilizationPercent ?? data.roofUtilizationPercent;
  const activeLifetimeSavings = activeDashboard?.lifetimeSavings ?? data.lifetimeSavings;
  const selectedAssessmentId = pendingAssessmentId ?? activeAssessmentId ?? latest.id;

  return (
    <main id="main-content" className="page-shell dashboard-page">
      <PageIntro eyebrow="Your solar notebook" title="Dashboard" copy="A clearer view of your latest estimated solar recommendation." action="New assessment" />
      <section className="dashboard-lead dashboard-latest-card">
        <div>
          <p className="eyebrow">{activeSummary?.id === latest.id ? "Latest recommendation" : "Selected assessment"}</p>
          <StatCard label="Recommended capacity" value={number(activeSummary?.recommendedCapacityKw)} unit="kW estimated" note={`${activeDashboard?.panelCount ?? latest.panelCount} panels estimated`} />
          <Link className="button button-primary dashboard-ai-button" to={`/assistant?assessment=${activeSummary?.id ?? latest.id}`}>Ask AI about this</Link>
        </div>
        <div className="dashboard-latest-details">
          <InfoRow label="Selected panel" value={activePanel ? `${activePanel.brand} · ${activePanel.model}` : "Panel information is not available"} />
          <InfoRow label="Annual generation" value={`${number(activeDashboard?.annualGeneration ?? latest.annualGeneration)} kWh estimated`} />
          <InfoRow label="Annual savings" value={`${money(activeSummary?.annualSavings ?? latest.annualSavings)} estimated`} />
          <InfoRow label="Installation cost" value={`${money(activeSummary?.estimatedCost ?? latest.estimatedCost)} estimated`} />
          <InfoRow label="Payback" value={activeSummary?.paybackYears == null ? "Not applicable" : `${number(activeSummary.paybackYears)} years estimated`} />
        </div>
      </section>

      <section className="dashboard-kpi-grid" aria-label="Estimated dashboard KPIs">
        <StatCard label="Estimated solar coverage" value={percent(activeCoverage)} unit="of annual consumption" />
        <StatCard label="Estimated budget fit" value={budgetLabel(activeBudgetFit)} unit={budgetUnit(activeBudgetFit)} />
        <StatCard label="Estimated roof utilization" value={percent(activeRoofUtilization)} unit="of usable roof area" />
        <StatCard label="Estimated 25-year savings" value={activeLifetimeSavings == null ? "—" : money(activeLifetimeSavings)} unit="configured lifespan estimate" />
      </section>

      <section className="dashboard-chart-grid">
        <ChartCard title="Cumulative Cost: With vs Without Solar (25 years)" className="dashboard-cost-chart">
          <ResponsiveContainer width="100%" height={320}>
            <LineChart data={activeCosts} margin={{ top: 12, right: 16, left: 8, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#cbdcc7" /><XAxis dataKey="year" tickFormatter={(value) => `Y${value}`} /><YAxis tickFormatter={chartMoney} width={76} />
              <Tooltip formatter={(value) => chartMoney(Number(value))} labelFormatter={(label) => `Year ${label}`} /><Legend />
              <Line type="monotone" dataKey="withoutSolar" name="Without solar" stroke="#d45b3f" strokeWidth={3} dot={false} />
              <Line type="monotone" dataKey="withSolar" name="With solar" stroke="#238b57" strokeWidth={3} dot={false} />
            </LineChart>
          </ResponsiveContainer>
        </ChartCard>
        <ChartCard title="Monthly Bill: Before vs After Solar">
          <ResponsiveContainer width="100%" height={320}>
            <BarChart data={activeMonthlyBill ? [{ name: "Estimated bill", before: activeMonthlyBill.before, after: activeMonthlyBill.after }] : []} margin={{ top: 12, right: 16, left: 8, bottom: 8 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#cbdcc7" /><XAxis dataKey="name" /><YAxis tickFormatter={chartMoney} width={76} />
              <Tooltip formatter={(value) => chartMoney(Number(value))} /><Legend />
              <Bar dataKey="before" name="Before solar" fill="#f6b51b" radius={[8, 8, 0, 0]} /><Bar dataKey="after" name="After solar" fill="#238b57" radius={[8, 8, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </ChartCard>
      </section>

      <section className="dashboard-panel-grid">
        <section className="dashboard-card panel-used-card"><div className="section-heading"><div><p className="eyebrow">Selected equipment</p><h2>Panel used</h2></div><Link className="quiet-link" to="/panels">Browse catalog</Link></div>{activePanel ? <div className="panel-used-details"><strong>{activePanel.brand}</strong><span>{activePanel.model}</span><small>{activePanel.wattage} W · {number(activePanel.efficiency)}% efficiency</small></div> : <p>Panel information is not available.</p>}</section>
        <section className="dashboard-card bill-card"><p className="eyebrow">Monthly view</p><h2>{activeMonthlyBill ? money(activeMonthlyBill.after) : "—"}</h2><p>Estimated monthly bill after solar, compared with {activeMonthlyBill ? money(activeMonthlyBill.before) : "your current bill"} before solar.</p></section>
      </section>

      <section className="recent-section dashboard-recent-section"><div className="section-heading"><div><p className="eyebrow">History</p><h2>Recent assessments</h2></div><div className="dashboard-selection-actions"><span className="dashboard-selection-hint">Select an assessment, then load it</span>{pendingAssessmentId != null && pendingAssessmentId !== activeAssessmentId && <button className="button button-primary" type="button" onClick={() => { setActiveAssessmentId(pendingAssessmentId); setPendingAssessmentId(null); }}>Load selected assessment</button>}</div></div>{assessments.length === 0 ? <p>No previous assessments yet.</p> : <AssessmentTable items={assessments} selectedId={selectedAssessmentId} onSelect={setPendingAssessmentId} />}</section>
      <p className="dashboard-footer-note">All figures are estimates based on your inputs and configured assumptions.</p>
    </main>
  );
}

function PageIntro({ eyebrow, title, copy, action }: { eyebrow: string; title: string; copy: string; action?: string }) {
  return <header className="page-intro"><div><p className="eyebrow">{eyebrow}</p><h1>{title}</h1><p>{copy}</p></div>{action && <Link className="button button-primary" to="/assessment">{action}</Link>}</header>;
}
function InfoRow({ label, value }: { label: string; value: string }) { return <div className="dashboard-info-row"><span>{label}</span><strong>{value}</strong></div>; }
function ChartCard({ title, children, className = "" }: { title: string; children: ReactNode; className?: string }) { return <section className={`dashboard-card chart-card ${className}`}><div className="section-heading"><h2>{title}</h2></div>{children}</section>; }
function AssessmentTable({ items, selectedId, onSelect }: { items: AssessmentSummary[]; selectedId: number; onSelect: (id: number) => void }) {
  return <div className="assessment-table dashboard-assessment-table"><div className="dashboard-table-row dashboard-table-header"><span>Select</span><span>Date</span><span>Location</span><span>Capacity</span><span>Savings</span><span>Payback</span></div>{items.map((item) => <button className={`dashboard-table-row dashboard-table-button ${selectedId === item.id ? "selected" : ""}`} type="button" key={item.id} onClick={() => onSelect(item.id)}><span aria-label={selectedId === item.id ? "Selected" : "Not selected"}>{selectedId === item.id ? "●" : "○"}</span><span>{new Date(item.createdAt).toLocaleDateString("en-IN")}</span><span>{item.locationLabel}</span><span>{number(item.actualCapacityKw ?? item.recommendedCapacityKw)} kW estimated</span><span>{money(item.annualSavings)} estimated</span><span>{item.paybackYears == null ? "Not applicable" : `${number(item.paybackYears)} years`}</span></button>)}</div>;
}
function percent(value: number | null) { return value == null ? "—" : `${number(value)}%`; }
function budgetLabel(value: DashboardResponse["budgetFit"]) { if (!value) return "—"; return value.withinBudget ? "Within budget" : "Over budget"; }
function budgetUnit(value: DashboardResponse["budgetFit"]) { if (!value) return "estimate unavailable"; return value.withinBudget ? `${money(Math.max(0, value.difference))} remaining` : `${money(Math.abs(value.difference))} over`; }
