import { createRoot } from "react-dom/client";
import App from "./App";
import "./style.css";
import "@fontsource/bricolage-grotesque/400.css";
import "@fontsource/bricolage-grotesque/600.css";
import "@fontsource/instrument-sans/400.css";
import "@fontsource/instrument-sans/600.css";
createRoot(document.getElementById("root")!).render(<App />);
