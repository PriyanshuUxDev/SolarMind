import { useEffect, useState } from "react";
import { api } from "../services/api";
import type { PanelResponse, PagedResponse } from "../types";
import Input from "../components/Input";
import Select from "../components/Select";
import PanelSvg from "../components/PanelSvg";
import Drawer from "../components/Drawer";
import Loading from "../components/Loading";
import EmptyState from "../components/EmptyState";
import ErrorMessage from "../components/ErrorMessage";

export default function Panels() {
  const [search, setSearch] = useState("");
  const [brand, setBrand] = useState("");
  const [sort, setSort] = useState("brand");
  const [result, setResult] = useState<PagedResponse<PanelResponse> | null>(
    null,
  );
  const [selected, setSelected] = useState<PanelResponse | null>(null);
  const [error, setError] = useState("");
  function load() {
    setError("");
    const params = new URLSearchParams({
      search,
      sort,
      direction: "asc",
      page: "0",
      size: "12",
    });
    if (brand) params.set("brand", brand);
    api
      .panels(`?${params}`)
      .then(setResult)
      .catch(() => setError("The panel catalog could not be loaded."));
  }
  useEffect(load, [search, sort, brand]);
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
            onChange={(event) => setSearch(event.target.value)}
            placeholder="Brand or model"
          />
        </label>
        <label>
          Brand
          <Input
            value={brand}
            onChange={(event) => setBrand(event.target.value)}
            placeholder="All brands"
          />
        </label>
        <label>
          Sort
          <Select
            value={sort}
            onChange={(event) => setSort(event.target.value)}
          >
            <option value="brand">Brand</option>
            <option value="model">Model</option>
            <option value="wattage">Wattage</option>
            <option value="efficiency">Efficiency</option>
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
              onClick={() => setSelected(panel)}
            >
              <PanelSvg />
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
      <Drawer
        open={Boolean(selected)}
        title={
          selected ? `${selected.brand} · ${selected.model}` : "Panel details"
        }
        onClose={() => setSelected(null)}
      >
        {selected && (
          <div className="ledger">
            <Ledger label="Wattage" value={`${selected.wattage} W`} />
            <Ledger label="Efficiency" value={`${selected.efficiency}%`} />
            <Ledger
              label="Dimensions"
              value={selected.dimensions || "Not provided"}
            />
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
