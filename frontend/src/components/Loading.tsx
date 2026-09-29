import Skeleton from "./Skeleton";

export default function Loading({ label = "Loading" }: { label?: string }) {
  return (
    <div className="loading-state" role="status" aria-live="polite">
      <span>{label}</span>
      <Skeleton lines={2} />
    </div>
  );
}
