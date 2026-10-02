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
    <main id="main-content" className="landing-page">
      <section className="landing-hero">
        <div className="landing-hero-copy">
          <p className="eyebrow">Solar planning, made human</p>
          <h1>Know what your roof can do.</h1>
          <p>
            Turn your bill, roof and location into a clear solar estimate you
            can actually understand—before you make a decision.
          </p>
          <div className="landing-actions">
            <Link className="button button-primary" to={token ? "/assessment" : "/register"}>
              {token ? "Start assessment" : "Explore your potential"}
            </Link>
            <a className="landing-text-link" href="#how-it-works">How it works <span>↗</span></a>
          </div>
          <div className="landing-proof">
            <span className="proof-dot" />
            <span>Built around explainable estimates</span>
          </div>
        </div>
        <div className="landing-hero-art" aria-label="Solar panels beneath a rising sun">
          <CellGrid />
          <MediaSlot src="/media/home-hero.mp4" poster="/media/home-hero.jpg" />
          <div className="landing-art-sun" />
          <div className="landing-art-label"><strong>01</strong><span>Find your fit<br /><small>in a few minutes</small></span></div>
          <SunArc className="landing-art-arc" />
        </div>
      </section>
      <section className="landing-metrics" aria-label="SolarMind benefits">
        <div><strong>01</strong><span>Real panel data</span></div>
        <div><strong>02</strong><span>Clear assumptions</span></div>
        <div><strong>03</strong><span>Plain-language answers</span></div>
      </section>
      <section className="content-section narrow-section landing-benefits">
        <Reveal>
          <p className="eyebrow">What you get</p>
          <h2>A useful estimate, with nothing hidden.</h2>
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
      <section className="landing-steps" id="how-it-works">
        <div className="content-section">
          <Reveal>
            <p className="eyebrow">How it works</p>
            <h2>From your bill to a better decision.</h2>
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
      <section className="honest-section content-section landing-honesty">
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
      <section className="closing-cta content-section landing-cta">
        <div><p className="eyebrow">Your next sunny step</p><h2>Ready to see your estimate?</h2></div>
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
      </footer>
    </main>
  );
}
