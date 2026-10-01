import { useEffect, useState } from "react";
import { api } from "../services/api";
import type { PanelResponse, PagedResponse } from "../types";
import Input from "../components/Input";
import Select from "../components/Select";
import PanelSvg, { parsePanelDimensions } from "../components/PanelSvg";
import Drawer from "../components/Drawer";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";
import ErrorMessage from "../components/ErrorMessage";

export default function Panels() {
  const [search, setSearch] = useState("");
  const [brand, setBrand] = useState("");
  const [sort, setSort] = useState("brand");
  const [direction, setDirection] = useState("asc");
  const [minWattage, setMinWattage] = useState("");
  const [maxWattage, setMaxWattage] = useState("");
  const [minEfficiency, setMinEfficiency] = useState("");
  const [page, setPage] = useState(0);
  const [result, setResult] = useState<PagedResponse<PanelResponse> | null>(
    null,
  );
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selected, setSelected] = useState<PanelResponse | null>(null);
  const [error, setError] = useState("");
  function load() {
    setError("");
    const params = new URLSearchParams({
      search,
      sort,
      direction,
      page: String(page),
      size: "12",
    });
    if (brand) params.set("brand", brand);
    if (minWattage) params.set("minWattage", minWattage);
    if (maxWattage) params.set("maxWattage", maxWattage);
    if (minEfficiency) params.set("minEfficiency", minEfficiency);
    api
      .panels(`?${params}`)
      .then(setResult)
      .catch(() => setError("The panel catalog could not be loaded."));
  }
  useEffect(load, [search, sort, direction, brand, minWattage, maxWattage, minEfficiency, page]);
  useEffect(() => {
    if (selectedId == null) {
      setSelected(null);
      return;
    }
    api.panel(selectedId).then(setSelected).catch(() => setError("The panel details could not be loaded."));
  }, [selectedId]);
  return (
    <main id="main-content" className="page-shell">
      <header className="page-intro">
        <div>
          <p className="eyebrow">Reference catalog</p>
          <h1>Solar panels</h1>
          <p>
            Browse the real panel records used by the recommendation service.
          </p>
        </div>
      </header>
      <section className="catalog-toolbar">
        <label>
          Search
          <input
            className="ui-input"
            value={search}
            onChange={(event) => {
              setPage(0);
              setSearch(event.target.value);
            }}
            placeholder="Brand or model"
          />
        </label>
        <label>
          Minimum wattage
          <Input type="number" min="0" value={minWattage} onChange={(event) => { setPage(0); setMinWattage(event.target.value); }} />
        </label>
        <label>
          Maximum wattage
          <Input type="number" min="0" value={maxWattage} onChange={(event) => { setPage(0); setMaxWattage(event.target.value); }} />
        </label>
        <label>
          Minimum efficiency
          <Input type="number" min="0" step="any" value={minEfficiency} onChange={(event) => { setPage(0); setMinEfficiency(event.target.value); }} />
        </label>
        <label>
          Brand
          <Input
            value={brand}
            onChange={(event) => {
              setPage(0);
              setBrand(event.target.value);
            }}
            placeholder="All brands"
          />
        </label>
        <label>
          Sort
          <Select
            value={sort}
            onChange={(event) => {
              setPage(0);
              setSort(event.target.value);
            }}
          >
            <option value="brand">Brand</option>
            <option value="model">Model</option>
            <option value="wattage">Wattage</option>
            <option value="efficiency">Efficiency</option>
          </Select>
        </label>
        <label>
          Direction
          <Select value={direction} onChange={(event) => { setPage(0); setDirection(event.target.value); }}>
            <option value="asc">Ascending</option>
            <option value="desc">Descending</option>
          </Select>
        </label>
      </section>
      <p className="catalog-note">
        Catalog is for information. It does not include prices or purchase
        actions.
      </p>
      {error ? (
        <ErrorMessage message={error} onRetry={load} />
      ) : !result ? (
        <Loading label="Loading panel catalog" />
      ) : result.content.length === 0 ? (
        <EmptyState
          title="No panels found"
          message="Try a different search or clear the filters."
          action={false}
        />
      ) : (
        <div className="panel-list">
          {result.content.map((panel) => (
            <button
              className="panel-item"
              key={panel.id}
              type="button"
              onClick={() => setSelectedId(panel.id)}
            >
              <PanelSvg {...parsePanelDimensions(panel.dimensions)} />
              <span>
                <strong>{panel.brand}</strong>
                <b>{panel.model}</b>
                <small>
                  {panel.wattage} W · {panel.efficiency}% efficiency
                </small>
              </span>
              <span className="panel-arrow">View details</span>
            </button>
          ))}
        </div>
      )}
      {result && result.totalPages > 1 && (
        <nav aria-label="Panel pages" className="pagination">
          <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)}>
            Previous
          </button>
          <span>Page {result.page + 1} of {result.totalPages}</span>
          <button type="button" disabled={page >= result.totalPages - 1} onClick={() => setPage((current) => current + 1)}>
            Next
          </button>
        </nav>
      )}
      <Drawer
        open={Boolean(selected)}
        title={
          selected ? `${selected.brand} · ${selected.model}` : "Panel details"
        }
        onClose={() => setSelectedId(null)}
      >
        {selected && (
          <div className="ledger">
            <Ledger label="Wattage" value={`${selected.wattage} W`} />
            <Ledger label="Daily output" value={selected.dailyOutput != null ? `${selected.dailyOutput} kWh/day` : "Not provided"} />
            <Ledger label="Monthly output" value={selected.monthlyOutput != null ? `${selected.monthlyOutput} kWh/month` : "Not provided"} />
            <Ledger label="Efficiency" value={`${selected.efficiency}%`} />
            <Ledger label="Voltage (Vmpp)" value={selected.vmpp != null ? `${selected.vmpp} V` : "Not provided"} />
            <Ledger label="Current (Impp)" value={selected.impp != null ? `${selected.impp} A` : "Not provided"} />
            <PanelSvg {...parsePanelDimensions(selected.dimensions)} />
            <Ledger label="Dimensions" value={selected.dimensions || "Not provided"} />
            <Ledger label="Weight" value={selected.weight != null ? `${selected.weight} kg` : "Not provided"} />
          </div>
        )}
      </Drawer>
    </main>
  );
}

function Ledger({ label, value }: { label: string; value: string }) {
  return (
    <div className="ledger-row">
      <span>{label}</span>
      <strong>{value}</strong>
    </div>
  );
}
