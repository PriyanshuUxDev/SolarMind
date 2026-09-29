import { ReactNode } from "react";
import { useReducedMotion } from "./useReducedMotion";
export default function Reveal({
  children,
  className = "",
  delay = 0,
}: {
  children: ReactNode;
  className?: string;
  delay?: number;
}) {
  const reduced = useReducedMotion();
  return (
    <div
      className={`reveal ${reduced ? "reveal-static" : ""} ${className}`}
      style={reduced ? undefined : { animationDelay: `${delay}ms` }}
    >
      {children}
    </div>
  );
}
