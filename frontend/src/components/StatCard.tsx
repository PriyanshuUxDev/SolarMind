import { ReactNode } from "react";

export default function StatCard({
  label,
  value,
  unit,
  note,
}: {
  label: string;
  value: ReactNode;
  unit?: string;
  note?: string;
}) {
  return (
    <article className="stat-card">
      <span className="stat-label">{label}</span>
      <strong className="stat-value">{value}</strong>
      {unit && <span className="stat-unit">{unit}</span>}
      {note && <small>{note}</small>}
    </article>
  );
}
