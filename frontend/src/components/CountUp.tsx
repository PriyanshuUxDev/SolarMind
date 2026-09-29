import { useEffect, useState } from "react";
import { useReducedMotion } from "../motion/useReducedMotion";
export default function CountUp({
  value,
  duration = 900,
}: {
  value: number;
  duration?: number;
}) {
  const reduced = useReducedMotion();
  const [shown, setShown] = useState(reduced ? value : 0);
  useEffect(() => {
    if (reduced) {
      setShown(value);
      return;
    }
    let start = performance.now();
    let frame = 0;
    const tick = (now: number) => {
      const p = Math.min(1, (now - start) / duration);
      setShown(value * (1 - Math.pow(1 - p, 3)));
      if (p < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [value, duration, reduced]);
  return <>{shown.toLocaleString("en-IN", { maximumFractionDigits: 2 })}</>;
}
