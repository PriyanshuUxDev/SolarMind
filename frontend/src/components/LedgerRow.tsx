export default function LedgerRow({
  label,
  value,
  estimated = false,
}: {
  label: string;
  value: React.ReactNode;
  estimated?: boolean;
}) {
  return (
    <div className="ledger-row">
      <span>{label}</span>
      <strong>
        {value}
        {estimated && <small> estimated</small>}
      </strong>
    </div>
  );
}
