import { FormEvent, useEffect, useState } from "react";
import { Link, useLocation } from "react-router-dom";
import { ApiError, api } from "../services/api";
import type { AssessmentResponse, LocationResponse } from "../types";
import Field from "../components/Field";
import Input from "../components/Input";
import Select from "../components/Select";
import Button from "../components/Button";
import ErrorMessage from "../components/ErrorMessage";
import Loading from "../components/Loading";
import LedgerRow from "../components/LedgerRow";
import Badge from "../components/Badge";
import PanelSvg, { parsePanelDimensions } from "../components/PanelSvg";
import SunArc from "../components/SunArc";
import CountUp from "../components/CountUp";

const money = (value: number) =>
  `₹${value.toLocaleString("en-IN", { maximumFractionDigits: 2 })}`;
const decimal = (value: number) =>
  value.toLocaleString("en-IN", { maximumFractionDigits: 2 });

export default function Assessment() {
  const { pathname } = useLocation();
  const [query, setQuery] = useState("");
  const [locations, setLocations] = useState<LocationResponse[]>([]);
  const [activeLocationIndex, setActiveLocationIndex] = useState(-1);
  const [location, setLocation] = useState<LocationResponse | null>(null);
  const [result, setResult] = useState<AssessmentResponse | null>(null);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    monthlyConsumption: "",
    monthlyBill: "",
    roofArea: "",
    roofType: "FLAT",
    budget: "",
  });

  useEffect(() => {
    setResult(null);
    setBusy(false);
    setError("");
    setQuery("");
    setLocations([]);
    setLocation(null);
    setForm({
      monthlyConsumption: "",
      monthlyBill: "",
      roofArea: "",
      roofType: "FLAT",
      budget: "",
    });
  }, [pathname]);
  function update(key: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [key]: value }));
  }
  useEffect(() => {
    if (query.trim().length < 2 || location) return;
    const timer = window.setTimeout(
      () =>
        api
          .locations(query)
          .then((response) => {
            setLocations(response.content);
            setActiveLocationIndex(response.content.length ? 0 : -1);
          })
          .catch(() => setLocations([])),
      250,
    );
    return () => window.clearTimeout(timer);
  }, [query, location]);
  function chooseLocation(item: LocationResponse) {
    setLocation(item);
    setQuery(`${item.city}, ${item.state}`);
    setLocations([]);
    setActiveLocationIndex(-1);
  }
  async function submit(event: FormEvent) {
    event.preventDefault();
    if (!location) {
      setError("Choose a location from the real search results.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      setResult(
        await api.createAssessment({
          locationId: location.id,
          monthlyConsumption: Number(form.monthlyConsumption),
          monthlyBill: Number(form.monthlyBill),
          roofArea: Number(form.roofArea),
          roofType: form.roofType,
          budget: Number(form.budget),
        }),
      );
    } catch (requestError) {
      setError(
        requestError instanceof ApiError
          ? requestError.message
          : "The recommendation could not be completed. Check your inputs and try again.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <main id="main-content" className="page-shell assessment-page">
      <header className="page-intro">
        <div>
          <p className="eyebrow">A considered estimate</p>
          <h1>Solar assessment</h1>
          <p>
            Use your monthly bill, roof and location. The backend does the
            recommendation math.
          </p>
        </div>
        {result && (
          <Button variant="quiet" onClick={() => setResult(null)}>
            Edit inputs
          </Button>
        )}
      </header>
      <div
        className={
          result ? "assessment-grid assessment-grid-result" : "assessment-grid"
        }
      >
        <form
          className={`assessment-form form-stack ${result ? "assessment-form-compact" : ""}`}
          onSubmit={submit}
        >
          <div className="form-group">
            <h2>Your location</h2>
            <Field label="City or district">
              <Input
                value={query}
                onChange={(event) => {
                  setQuery(event.target.value);
                  setLocation(null);
                }}
                onKeyDown={(event) => {
                  if (!locations.length) return;
                  if (event.key === "ArrowDown") {
                    event.preventDefault();
                    setActiveLocationIndex((current) =>
                      current < locations.length - 1 ? current + 1 : 0,
                    );
                  } else if (event.key === "ArrowUp") {
                    event.preventDefault();
                    setActiveLocationIndex((current) =>
                      current > 0 ? current - 1 : locations.length - 1,
                    );
                  } else if (event.key === "Enter" && activeLocationIndex >= 0) {
                    event.preventDefault();
                    chooseLocation(locations[activeLocationIndex]);
                  } else if (event.key === "Escape") {
                    setLocations([]);
                    setActiveLocationIndex(-1);
                  }
                }}
                placeholder="Search a city or district"
                required
                role="combobox"
                aria-expanded={locations.length > 0}
                aria-autocomplete="list"
                aria-controls="location-results"
                aria-activedescendant={
                  activeLocationIndex >= 0
                    ? `location-option-${locations[activeLocationIndex].id}`
                    : undefined
                }
              />
              {locations.length > 0 && (
                <ul
                  id="location-results"
                  className="suggestions"
                  role="listbox"
                >
                  {locations.map((item, index) => (
                    <li key={item.id}>
                      <button
                        id={`location-option-${item.id}`}
                        type="button"
                        role="option"
                        aria-selected={activeLocationIndex === index}
                        onMouseEnter={() => setActiveLocationIndex(index)}
                        onClick={() => chooseLocation(item)}
                      >
                        {item.city}, {item.district}, {item.state}
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </Field>
            {location && (
              <p className="selected-location">
                Selected: {location.city}, {location.state}
              </p>
            )}
          </div>
          <div className="form-group">
            <h2>Your electricity</h2>
            <Field label="Monthly consumption (kWh)">
              <Input
                type="number"
                min="0"
                step="any"
                value={form.monthlyConsumption}
                onChange={(event) =>
                  update("monthlyConsumption", event.target.value)
                }
                required
              />
            </Field>
            <Field label="Monthly bill (rupees)">
              <Input
                type="number"
                min="0"
                step="any"
                value={form.monthlyBill}
                onChange={(event) => update("monthlyBill", event.target.value)}
                required
              />
            </Field>
          </div>
          <div className="form-group">
            <h2>Your roof</h2>
            <Field label="Usable roof area (square metres)">
              <Input
                type="number"
                min="0"
                step="any"
                value={form.roofArea}
                onChange={(event) => update("roofArea", event.target.value)}
                required
              />
            </Field>
            <Field label="Roof type">
              <Select
                value={form.roofType}
                onChange={(event) => update("roofType", event.target.value)}
              >
                <option value="FLAT">Flat</option>
                <option value="SLOPED">Sloped</option>
                <option value="OTHER">Other</option>
              </Select>
            </Field>
            <Field label="Budget (rupees)">
              <Input
                type="number"
                min="0"
                step="any"
                value={form.budget}
                onChange={(event) => update("budget", event.target.value)}
                required
              />
            </Field>
          </div>
          {error && <ErrorMessage message={error} />}
          {busy ? (
            <Loading label="Preparing your estimate" />
          ) : (
            <Button type="submit">Get recommendation</Button>
          )}
        </form>
        {result && <Result result={result} />}
      </div>
    </main>
  );
}

function Result({ result }: { result: AssessmentResponse }) {
  return (
    <section className="result-panel">
      <div className="result-header">
        <SunArc />
        <p className="eyebrow">Recommendation ready</p>
        <h2>
          <CountUp value={result.recommendedCapacityKw} /> kW
        </h2>
        <p>recommended capacity · estimated</p>
      </div>
        <div className="result-body">
        <div className="ledger">
          <LedgerRow label="Assessment ID" value={result.id} />
        </div>
        <div className="status-row">
          <Badge tone={result.roofFeasible ? "good" : "signal"}>
            {result.roofFeasible
              ? "Approximate roof fit"
              : "May not fit the roof"}
          </Badge>
          <Badge tone={result.withinBudget ? "good" : "signal"}>
            {result.withinBudget ? "Within budget" : "Above budget"}
          </Badge>
        </div>
        <p className="caution">
          Roof feasibility is approximate. This is not an engineering or
          structural assessment.
        </p>
        <div className="result-detail-grid">
          <div className="ledger">
            <LedgerRow
              label="Selected panel"
              value={`${result.selectedPanel.brand} · ${result.selectedPanel.model}`}
            />
            <LedgerRow
              label="Panel count"
              value={result.panelCount}
              estimated
            />
            <LedgerRow
              label="Actual capacity"
              value={`${decimal(result.actualCapacityKw)} kW`}
              estimated
            />
            <LedgerRow
              label="Annual generation"
              value={decimal(result.annualGeneration)}
              estimated
            />
            <LedgerRow
              label="Annual savings"
              value={money(result.annualSavings)}
              estimated
            />
            <LedgerRow
              label="Installation cost"
              value={money(result.estimatedCost)}
              estimated
            />
            <LedgerRow
              label="Payback"
              value={
                result.paybackYears === null
                  ? "Not applicable"
                  : `${decimal(result.paybackYears)} years`
              }
              estimated
            />
            <LedgerRow
              label="CO₂ reduction"
              value={`${decimal(result.co2Tonnes)} tonnes (${decimal(result.co2Kg)} kg)`}
              estimated
            />
          </div>
          <PanelSvg {...parsePanelDimensions(result.selectedPanel.dimensions)} />
        </div>
        <details className="assumptions">
          <summary>Assumptions used</summary>
          <pre>{JSON.stringify(result.assumptionsUsed, null, 2)}</pre>
        </details>
        <div className="result-actions">
          <Link
            className="button button-primary"
            to={`/assistant?assessment=${result.id}`}
          >
            Ask the assistant about this
          </Link>
          <Link className="quiet-link" to="/assessment">
            New assessment
          </Link>
        </div>
      </div>
    </section>
  );
}
