/** @type {import('tailwindcss').Config} */
export default {
  content: ['./index.html', './src/**/*.{ts,tsx}'],
  theme: { extend: { colors: { night: '#0B1F33', cell: '#1F4E79', sky: '#DCEEF5', paper: '#F6F9FA', sun: '#FFB627', signal: '#C8402A' }, fontFamily: { display: ['Bricolage Grotesque', 'sans-serif'], sans: ['Instrument Sans', 'system-ui', 'sans-serif'] } } },
  plugins: []
};
