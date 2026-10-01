import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiError, api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import Field from "../components/Field";
import Input from "../components/Input";
import Button from "../components/Button";
import ErrorMessage from "../components/ErrorMessage";

export default function Register() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    confirmPassword: "",
  });
  function update(key: keyof typeof form, value: string) {
    setForm((current) => ({ ...current, [key]: value }));
  }
  async function submit(event: FormEvent) {
    event.preventDefault();
    if (form.password.length < 8 || form.password !== form.confirmPassword) {
      setError("Use at least 8 characters and confirm the password exactly.");
      return;
    }
    setBusy(true);
    setError("");
    try {
      const response = await api.register(form);
      login(response.token);
      navigate("/dashboard");
    } catch (requestError) {
      setError(
        requestError instanceof ApiError
          ? requestError.message
          : "That email may already be registered. Try logging in instead.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <main id="main-content" className="auth-page">
      <section className="auth-form-panel">
        <p className="eyebrow">Start with your bill</p>
        <h1>Create an account</h1>
        <p className="intro-copy">
          Keep your assessments together and ask questions about the results.
        </p>
        <form className="form-stack" onSubmit={submit}>
          <Field label="Name">
            <Input
              value={form.name}
              onChange={(event) => update("name", event.target.value)}
              required
              autoComplete="name"
            />
          </Field>
          <Field label="Email">
            <Input
              type="email"
              value={form.email}
              onChange={(event) => update("email", event.target.value)}
              required
              autoComplete="email"
            />
          </Field>
          <Field label="Password" help="At least 8 characters.">
            <Input
              type="password"
              value={form.password}
              onChange={(event) => update("password", event.target.value)}
              required
              autoComplete="new-password"
            />
          </Field>
          <Field label="Confirm password">
            <Input
              type="password"
              value={form.confirmPassword}
              onChange={(event) =>
                update("confirmPassword", event.target.value)
              }
              required
              autoComplete="new-password"
            />
          </Field>
          {error && <ErrorMessage message={error} />}
          <Button type="submit" disabled={busy}>
            {busy ? "Creating account…" : "Create account"}
          </Button>
        </form>
        <p className="form-footnote">
          Already registered? <Link to="/login">Log in</Link>
        </p>
      </section>
      <AuthVisual />
    </main>
  );
}

function AuthVisual() {
  return (
    <aside className="auth-visual">
      <div className="auth-visual-copy">
        <p className="eyebrow">Start with clarity</p>
        <h2>A recommendation you can explain.</h2>
        <p>
          SolarMind keeps the calculation in the backend and puts the
          assumptions beside the result.
        </p>
      </div>
      <div className="auth-visual-art">
        <span className="media-sun" />
        <span className="roof-line" />
      </div>
    </aside>
  );
}
