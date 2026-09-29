import { useReducedMotion } from "../motion/useReducedMotion";
export default function SunArc({ className = "" }: { className?: string }) {
  const reduced = useReducedMotion();
  return (
    <svg
      className={`sun-arc ${className}`}
      viewBox="0 0 640 180"
      aria-hidden="true"
    >
      <path
        className="sun-arc-line"
        pathLength="1"
        d="M12 160 C150 8 470 8 628 160"
        style={reduced ? { strokeDashoffset: 0 } : undefined}
      />
      <circle
        className={`sun-disc ${reduced ? "sun-disc-rest" : ""}`}
        cx="320"
        cy="39"
        r="10"
      />
    </svg>
  );
}
