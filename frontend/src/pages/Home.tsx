import { Link } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import CellGrid from "../components/CellGrid";
import MediaSlot from "../components/MediaSlot";
import SunArc from "../components/SunArc";
import Reveal from "../motion/Reveal";

const benefits = [
  "Capacity and panel count",
  "Estimated annual generation",
  "Estimated savings and cost",
  "Payback and CO₂ reduction",
];
const steps = [
  ["01", "Enter your details", "Share your bill, roof and location."],
  [
    "02",
    "Get a recommendation",
    "The backend compares your inputs with the catalog.",
  ],
  ["03", "Review the estimates", "See the assumptions behind every result."],
  [
    "04",
    "Ask the assistant",
    "Get a plain-language explanation of the result.",
  ],
];

export default function Home() {
  const { token } = useAuth();
  return (
    <main id="main-content">
      <section className="hero-section">
        <CellGrid />
        <div className="hero-media">
          <MediaSlot src="/media/home-hero.mp4" poster="/media/home-hero.jpg" />
        </div>
        <div className="hero-scrim" />
        <div className="hero-content">
          <p className="eyebrow">Follow the sun</p>
          <h1>A clearer way to read your rooftop potential.</h1>
          <p className="hero-copy">
            SolarMind turns your electricity bill, roof and real panel data into
            an explainable rooftop-solar estimate.
          </p>
          <div className="hero-actions">
            <Link
              className="button button-sun"
              to={token ? "/assessment" : "/register"}
            >
              {token ? "Get recommendation" : "Get started"}
            </Link>
            <a className="quiet-link" href="#how-it-works">
              See how it works
            </a>
          </div>
        </div>
        <SunArc className="hero-arc" />
      </section>
      <section className="content-section narrow-section">
        <Reveal>
          <p className="eyebrow">What you get</p>
          <h2>A useful estimate, with its limits in view.</h2>
        </Reveal>
        <div className="benefit-ledger">
          {benefits.map((benefit, index) => (
            <Reveal key={benefit} delay={index * 40}>
              <div className="ledger-row">
                <span>0{index + 1}</span>
                <strong>
                  {benefit} <small>estimated</small>
                </strong>
              </div>
            </Reveal>
          ))}
        </div>
        <p className="disclaimer">
          Figures are estimates based on your inputs, available panel data and
          configurable assumptions. They are not a quote or an engineering
          approval.
        </p>
      </section>
      <section className="steps-section" id="how-it-works">
        <div className="content-section">
          <Reveal>
            <p className="eyebrow">How it works</p>
            <h2>Four steps from bill to understanding.</h2>
          </Reveal>
          <div className="steps-list">
            {steps.map(([number, title, copy]) => (
              <Reveal key={number} delay={Number(number) * 40}>
                <article className="step-item">
                  <span className="step-number">{number}</span>
                  <div>
                    <h3>{title}</h3>
                    <p>{copy}</p>
                  </div>
                </article>
              </Reveal>
            ))}
          </div>
        </div>
      </section>
      <section className="honest-section content-section">
        <div>
          <p className="eyebrow">The honest note</p>
          <h2>Every number has a source.</h2>
          <p>
            Your bill and consumption set the starting point. Location and panel
            records come from the project data. Generation, cost and emissions
            use the assumptions shown with your result.
          </p>
          <p>
            That makes the estimate explainable. It does not make it a quote,
            guarantee or structural assessment.
          </p>
        </div>
        <div className="honest-visual">
          <CellGrid />
          <SunArc />
        </div>
      </section>
      <section className="closing-cta content-section">
        <h2>Ready to see your estimate?</h2>
        <Link
          className="button button-primary"
          to={token ? "/assessment" : "/register"}
        >
          {token ? "Start assessment" : "Create an account"}
        </Link>
      </section>
      <footer className="site-footer">
        <span>SolarMind</span>
        <span>Estimates are not quotes or engineering approvals.</span>
        <Link to="/">Media credits</Link>
      </footer>
    </main>
  );
}
