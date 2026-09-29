import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { api } from "../services/api";
import type { DashboardResponse } from "../types";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import EmptyState from "../components/EmptyState";
import StatCard from "../components/StatCard";
import LedgerRow from "../components/LedgerRow";
import Badge from "../components/Badge";

const number = (value: number) =>
  value.toLocaleString("en-IN", { maximumFractionDigits: 2 });

export default function Dashboard() {
  const [data, setData] = useState<DashboardResponse | null>(null);
  const [error, setError] = useState("");
  function load() {
    setError("");
    api
      .dashboard()
      .then(setData)
      .catch(() => setError("The dashboard could not be loaded."));
  }
  useEffect(load, []);
  if (error)
    return (
      <main id="main-content" className="page-shell">
        <ErrorMessage message={error} onRetry={load} />
      </main>
    );
  if (!data)
    return (
      <main id="main-content" className="page-shell">
        <Loading label="Loading your dashboard" />
      </main>
    );
  if (!data.latest)
    return (
      <main id="main-content" className="page-shell">
        <PageIntro
          eyebrow="Your solar notebook"
          title="Dashboard"
          copy="Keep your latest estimate and past assessments in one place."
        />
        <EmptyState />
      </main>
    );
  const latest = data.latest;
  return (
    <main id="main-content" className="page-shell">
      <PageIntro
        eyebrow="Your solar notebook"
        title="Dashboard"
        copy="A quieter view of the estimates you have explored."
        action="New assessment"
      />
      <section className="dashboard-lead">
        <div>
          <p className="eyebrow">Latest recommendation</p>
          <StatCard
            label="Recommended capacity"
            value={number(latest.recommendedCapacityKw)}
            unit="kW estimated"
          />
        </div>
        <div className="ledger">
          <LedgerRow
            label="Selected panel"
            value={`${latest.selectedPanel.brand} · ${latest.selectedPanel.model}`}
          />
          <LedgerRow label="Panel count" value={latest.panelCount} estimated />
          <LedgerRow
            label="Annual generation"
            value={number(latest.annualGeneration)}
            estimated
          />
          <LedgerRow
            label="Annual savings"
            value={`₹${number(latest.annualSavings)}`}
            estimated
          />
          <LedgerRow
            label="Installation cost"
            value={`₹${number(latest.estimatedCost)}`}
            estimated
          />
        </div>
      </section>
      <section className="recent-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">History</p>
            <h2>Recent assessments</h2>
          </div>
          <Link className="quiet-link" to="/assessment">
            Start another
          </Link>
        </div>
        {data.recentAssessments.length === 0 ? (
          <p>No previous assessments yet.</p>
        ) : (
          <div
            className="assessment-table"
            role="table"
            aria-label="Recent assessments"
          >
            <div className="table-row table-header" role="row">
              <span>Date</span>
              <span>Capacity</span>
              <span>Savings</span>
            </div>
            {data.recentAssessments.map((item) => (
              <div className="table-row" role="row" key={item.id}>
                <span>
                  {new Date(item.createdAt).toLocaleDateString("en-IN")}
                </span>
                <span>{number(item.recommendedCapacityKw)} kW estimated</span>
                <span>₹{number(item.annualSavings)} estimated</span>
              </div>
            ))}
          </div>
        )}
      </section>
    </main>
  );
}

function PageIntro({
  eyebrow,
  title,
  copy,
  action,
}: {
  eyebrow: string;
  title: string;
  copy: string;
  action?: string;
}) {
  return (
    <header className="page-intro">
      <div>
        <p className="eyebrow">{eyebrow}</p>
        <h1>{title}</h1>
        <p>{copy}</p>
      </div>
      {action && (
        <Link className="button button-primary" to="/assessment">
          {action}
        </Link>
      )}
    </header>
  );
}
