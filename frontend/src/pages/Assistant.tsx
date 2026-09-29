import { FormEvent, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api } from "../services/api";
import type { AssessmentResponse } from "../types";
import Input from "../components/Input";
import Button from "../components/Button";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import LedgerRow from "../components/LedgerRow";

const suggestions = [
  "What assumptions were used?",
  "Explain the payback estimate",
  "Could the roof fit be an issue?",
];

export default function Assistant() {
  const [params] = useSearchParams();
  const [assessments, setAssessments] = useState<AssessmentResponse[]>([]);
  const [assessmentId, setAssessmentId] = useState<number | undefined>(
    params.get("assessment") ? Number(params.get("assessment")) : undefined,
  );
  const [question, setQuestion] = useState("");
  const [answer, setAnswer] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const liveRef = useRef<HTMLDivElement>(null);
  useEffect(() => {
    api
      .assessments()
      .then(setAssessments)
      .catch(() => setError("Your assessments could not be loaded."));
  }, []);
  const selected = assessments.find((item) => item.id === assessmentId);
  async function ask(event: FormEvent) {
    event.preventDefault();
    if (!question.trim()) return;
    setBusy(true);
    setError("");
    setAnswer("");
    try {
      setAnswer((await api.ask({ question, assessmentId })).answer);
    } catch {
      setError("The assistant is unavailable right now.");
    } finally {
      setBusy(false);
    }
  }
  return (
    <main id="main-content" className="page-shell assistant-page">
      <header className="page-intro">
        <div>
          <p className="eyebrow">Explain, don’t recalculate</p>
          <h1>Assistant</h1>
          <p>
            The assistant explains backend results. It does not change them.
          </p>
        </div>
      </header>
      <div className="assistant-layout">
        <aside className="assistant-context">
          <label>
            Assessment context
            <select
              className="ui-input"
              value={assessmentId ?? ""}
              onChange={(event) =>
                setAssessmentId(
                  event.target.value ? Number(event.target.value) : undefined,
                )
              }
            >
              <option value="">No assessment selected</option>
              {assessments.map((item) => (
                <option key={item.id} value={item.id}>
                  {new Date(item.createdAt).toLocaleDateString("en-IN")} ·{" "}
                  {item.recommendedCapacityKw} kW
                </option>
              ))}
            </select>
          </label>
          {selected ? (
            <div className="ledger">
              <LedgerRow
                label="Capacity"
                value={`${selected.recommendedCapacityKw} kW`}
                estimated
              />
              <LedgerRow
                label="Savings"
                value={`₹${selected.annualSavings}`}
                estimated
              />
              <LedgerRow label="Panel" value={selected.selectedPanel.model} />
            </div>
          ) : (
            <p>Select an assessment to give the assistant context.</p>
          )}
        </aside>
        <section className="chat-panel" aria-label="Assistant conversation">
          <div className="chat-messages" ref={liveRef} aria-live="polite">
            <div className="chat-message assistant-message">
              <span className="assistant-glyph" aria-hidden="true">
                ☼
              </span>
              <p>Ask me to explain an estimate or its assumptions.</p>
            </div>
            {answer && (
              <div className="chat-message assistant-message">
                <span className="assistant-glyph" aria-hidden="true">
                  ☼
                </span>
                <p>{answer}</p>
              </div>
            )}
            {busy && <Loading label="Assistant is answering" />}
            {error && (
              <ErrorMessage
                message={error}
                onRetry={() => ask({ preventDefault() {} } as FormEvent)}
              />
            )}
          </div>
          <div className="suggestion-row">
            {suggestions.map((item) => (
              <button
                key={item}
                className="suggestion"
                onClick={() => setQuestion(item)}
              >
                {item}
              </button>
            ))}
          </div>
          <form className="chat-form" onSubmit={ask}>
            <Input
              value={question}
              onChange={(event) => setQuestion(event.target.value)}
              maxLength={500}
              placeholder="Ask about your estimate"
              aria-label="Question"
              disabled={busy}
              required
            />
            <Button type="submit" disabled={busy}>
              {busy ? "Sending…" : "Ask assistant"}
            </Button>
          </form>
        </section>
      </div>
    </main>
  );
}
