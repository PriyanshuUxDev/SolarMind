import { FormEvent, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { ApiError, api } from "../services/api";
import { useAuth } from "../context/AuthContext";
import Field from "../components/Field";
import Input from "../components/Input";
import Button from "../components/Button";
import ErrorMessage from "../components/ErrorMessage";

export default function Login() {
  const { login } = useAuth();
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);
  async function submit(event: FormEvent) {
    event.preventDefault();
    setBusy(true);
    setError("");
    try {
      const response = await api.login({ email, password });
      login(response.token);
      navigate("/dashboard");
    } catch (requestError) {
      setError(
        requestError instanceof ApiError
          ? requestError.message
          : "That email or password was not recognised.",
      );
    } finally {
      setBusy(false);
    }
  }
  return (
    <main id="main-content" className="auth-page">
      <section className="auth-form-panel">
        <p className="eyebrow">Welcome back</p>
        <h1>Log in</h1>
        <p className="intro-copy">
          Pick up where you left off and keep your estimate in view.
        </p>
        <form className="form-stack" onSubmit={submit}>
          <Field label="Email">
            <Input
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              autoComplete="email"
            />
          </Field>
          <Field label="Password">
            <Input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              autoComplete="current-password"
            />
          </Field>
          {error && <ErrorMessage message={error} />}
          <Button type="submit" disabled={busy}>
            {busy ? "Logging in…" : "Log in"}
          </Button>
        </form>
        <p className="form-footnote">
          New to SolarMind? <Link to="/register">Create an account</Link>
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
        <p className="eyebrow">Follow the sun</p>
        <h2>Clear inputs. Honest estimates.</h2>
        <p>
          Your numbers remain yours. SolarMind shows the assumptions behind the
          recommendation.
        </p>
      </div>
      <div className="auth-visual-art">
        <span className="media-sun" />
        <span className="roof-line" />
      </div>
    </aside>
  );
}
