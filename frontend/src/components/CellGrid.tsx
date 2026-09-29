export default function CellGrid() {
  return (
    <svg className="cell-grid" aria-hidden="true">
      <defs>
        <pattern
          id="cell-lines"
          width="28"
          height="18"
          patternUnits="userSpaceOnUse"
        >
          <path d="M0 0H28M0 9H28M0 18H28M14 0V18" />
        </pattern>
      </defs>
      <rect width="100%" height="100%" fill="url(#cell-lines)" />
    </svg>
  );
}
