import { ReactNode } from "react";
export default function Badge({
  children,
  tone = "neutral",
}: {
  children: ReactNode;
  tone?: "neutral" | "good" | "signal";
}) {
  return <span className={`badge badge-${tone}`}>{children}</span>;
}
