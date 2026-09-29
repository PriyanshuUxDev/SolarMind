import { ButtonHTMLAttributes } from "react";
export default function Button({
  variant = "primary",
  className = "",
  ...props
}: ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "quiet" | "outline";
}) {
  return (
    <button
      className={`ui-button ui-button-${variant} ${className}`}
      {...props}
    />
  );
}
