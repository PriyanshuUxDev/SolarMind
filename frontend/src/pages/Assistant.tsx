import { FormEvent, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { api, ApiError } from "../services/api";
import type { AssessmentResponse, AssessmentSummary } from "../types";
import Input from "../components/Input";
import Button from "../components/Button";
import Loading from "../components/Loading";
import ErrorMessage from "../components/ErrorMessage";
import LedgerRow from "../components/LedgerRow";

const suggestions = [
  "Why did you recommend this capacity?",
  "Why do I need this many panels?",
  "How is payback calculated?",
  "What does panel efficiency mean?",
  "What affects solar generation?",
  "Which panel is suitable for my assessment?",
];

type Message = { role: "user" | "assistant"; text: string };

export default function Assistant() {
  const [params] = useSearchParams();
  const [assessments, setAssessments] = useState<AssessmentSummary[]>([]);
  const [assessmentId, setAssessmentId] = useState<number | undefined>(
    params.get("assessment") ? Number(params.get("assessment")) : undefined,
  );
  const [selectedAssessment, setSelectedAssessment] = useState<AssessmentResponse | null>(null);
  const [question, setQuestion] = useState("");
  const [lastQuestion, setLastQuestion] = useState("");
  const [messages, setMessages] = useState<Message[]>([]);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const liveRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    api.assessments().then((items) => {
      setAssessments(items);
      if (!assessmentId && items[0]) setAssessmentId(items[0].id);
    }).catch(() => setError("Your assessments could not be loaded."));
  }, [assessmentId]);

  useEffect(() => {
    if (!assessmentId) {
      setSelectedAssessment(null);
      return;
    }
    setSelectedAssessment(null);
    api.assessment(assessmentId).then(setSelectedAssessment).catch(() => setError("The selected assessment could not be loaded."));
  }, [assessmentId]);

  useEffect(() => {
    liveRef.current?.scrollTo({ top: liveRef.current.scrollHeight });
  }, [messages, busy]);

  async function sendQuestion(text: string) {
    const trimmed = text.trim();
    if (!trimmed || busy) return;
    setBusy(true);
    setError("");
    setLastQuestion(trimmed);
    setQuestion("");
    setMessages((current) => [...current, { role: "user", text: trimmed }]);
    try {
      const response = await api.ask({ question: trimmed, assessmentId });
      setMessages((current) => [...current, { role: "assistant", text: response.answer }]);
    } catch (cause) {
      setError(cause instanceof ApiError ? cause.message : "The assistant is unavailable right now.");
    } finally {
      setBusy(false);
    }
  }

  function ask(event: FormEvent) {
    event.preventDefault();
    void sendQuestion(question);
  }

  return (
    <main id="main-content" className="page-shell assistant-page">
      <header className="page-intro">
        <div>
          <p className="eyebrow">Explain, don’t recalculate</p>
          <h1>Assistant</h1>
          <p>The assistant explains backend results. It does not change them.</p>
        </div>
      </header>
      <div className="assistant-layout">
        <aside className="assistant-context">
          <label>
            Assessment context
            <select className="ui-input" value={assessmentId ?? ""} onChange={(event) => setAssessmentId(event.target.value ? Number(event.target.value) : undefined)}>
              <option value="">No assessment selected</option>
              {assessments.map((item) => (
                <option key={item.id} value={item.id}>
                  {new Date(item.createdAt).toLocaleDateString("en-IN")} · {item.recommendedCapacityKw} kW
                </option>
              ))}
            </select>
          </label>
          {selectedAssessment ? (
            <div className="ledger">
              <LedgerRow label="Assessment ID" value={selectedAssessment.id} />
              <LedgerRow label="Capacity" value={`${selectedAssessment.recommendedCapacityKw} kW`} estimated />
              <LedgerRow label="Savings" value={`₹${selectedAssessment.annualSavings}`} estimated />
              <LedgerRow label="Panel" value={selectedAssessment.selectedPanel.model} />
            </div>
          ) : <p>Select an assessment to give the assistant context.</p>}
        </aside>
        <section className="chat-panel" aria-label="Assistant conversation">
          <div className="chat-messages" ref={liveRef} aria-live="polite">
            <div className="chat-message assistant-message">
              <span className="assistant-glyph" aria-hidden="true">☼</span>
              <p>Ask me to explain an estimate or its assumptions.</p>
            </div>
            {messages.map((message, index) => (
              <div className={`chat-message ${message.role}-message`} key={`${message.role}-${index}`}>
                {message.role === "assistant" && <span className="assistant-glyph" aria-hidden="true">☼</span>}
                <p>{message.text}</p>
              </div>
            ))}
            {busy && <Loading label="Assistant is answering" />}
            {error && <ErrorMessage message={error} onRetry={() => void sendQuestion(lastQuestion)} />}
          </div>
          <div className="suggestion-row">
            {suggestions.map((item) => (
              <button key={item} className="suggestion" type="button" onClick={() => setQuestion(item)}>{item}</button>
            ))}
          </div>
          <form className="chat-form" onSubmit={ask}>
            <Input value={question} onChange={(event) => setQuestion(event.target.value)} maxLength={500} placeholder="Ask about your estimate" aria-label="Question" disabled={busy} required />
            <Button type="submit" disabled={busy}>{busy ? "Sending…" : "Ask assistant"}</Button>
          </form>
        </section>
      </div>
    </main>
  );
}
