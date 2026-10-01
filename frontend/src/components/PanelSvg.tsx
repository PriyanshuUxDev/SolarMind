export default function PanelSvg({
  widthMm = 1000,
  heightMm = 1700,
}: {
  widthMm?: number;
  heightMm?: number;
}) {
  const ratio = Math.max(0.45, Math.min(1.8, widthMm / heightMm));
  return (
    <div
      className="panel-svg"
      style={{ aspectRatio: `${ratio}` }}
      aria-label="Solar panel illustration, not to scale"
    >
      <svg viewBox="0 0 100 170" preserveAspectRatio="none" aria-hidden="true">
        <rect width="100" height="170" rx="2" fill="#1F4E79" />
        <path
          d="M0 20h100M0 40h100M0 60h100M0 80h100M0 100h100M0 120h100M0 140h100M20 0v170M40 0v170M60 0v170M80 0v170"
          stroke="#DCEEF5"
          strokeOpacity=".55"
          strokeWidth="1"
        />
      </svg>
      <small>Illustration, not to scale</small>
    </div>
  );
}

export function parsePanelDimensions(dimensions?: string) {
  const values = dimensions?.match(/[0-9]+(?:[.,][0-9]+)?/g) ?? [];
  const widthMm = values[0] ? Number(values[0].replace(",", ".")) : 1000;
  const heightMm = values[1] ? Number(values[1].replace(",", ".")) : 1700;
  return { widthMm, heightMm };
}
