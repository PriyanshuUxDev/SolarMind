import Button from "./Button";
import Badge from "./Badge";
import CellGrid from "./CellGrid";
import PanelSvg from "./PanelSvg";
import SunArc from "./SunArc";
import StatFigure from "./StatFigure";
export default function DesignKit() {
  return (
    <main className="design-kit">
      <p className="eyebrow">Development only</p>
      <h1>SolarMind design kit</h1>
      <p>Foundation primitives and visual direction.</p>
      <section className="kit-swatch">
        <CellGrid />
        <SunArc />
      </section>
      <div className="kit-row">
        <Button>Get recommendation</Button>
        <Button variant="outline">Secondary action</Button>
        <Badge tone="good">Estimated · feasible</Badge>
      </div>
      <div className="kit-row">
        <StatFigure
          label="Recommended capacity"
          value="4.2"
          unit="kW estimated"
        />
        <PanelSvg />
      </div>
    </main>
  );
}
