import { Link } from "react-router-dom";
import CellGrid from "./CellGrid";

export default function EmptyState({
  title = "No assessments yet",
  message = "Start with your electricity bill.",
  action = true,
}: {
  title?: string;
  message?: string;
  action?: boolean;
}) {
  return (
    <section className="empty-state">
      <div className="empty-visual">
        <CellGrid />
        <span className="empty-sun" />
      </div>
      <h2>{title}</h2>
      <p>{message}</p>
      {action && (
        <Link className="button button-primary" to="/assessment">
          Start an assessment
        </Link>
      )}
    </section>
  );
}
