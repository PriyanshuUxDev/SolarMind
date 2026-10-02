import hmac
import json
from pathlib import Path

from fastapi import FastAPI, Header, HTTPException
import httpx
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings, SettingsConfigDict

class Settings(BaseSettings):
    ai_internal_token: str = ""
    gemini_api_key: str = ""
    gemini_model: str = ""

    model_config = SettingsConfigDict(
        env_file=(
            Path(__file__).resolve().with_name(".env"),
            Path(__file__).resolve().parent.parent / ".env",
        ),
        env_file_encoding="utf-8",
        extra="ignore",
    )

settings = Settings()
app = FastAPI(title="SolarMind AI Service")

def configured(value: str) -> bool:
    """Treat template values as missing so the service fails clearly at startup/use."""
    return bool(value.strip()) and not value.strip().lower().startswith("placeholder")

class AskRequest(BaseModel):
    question: str = Field(min_length=1, max_length=2000)
    context: dict = Field(default_factory=dict)

class AskResponse(BaseModel):
    answer: str

SYSTEM_INSTRUCTION = "You explain a solar recommendation calculated by a verified backend. Use only the supplied values; do not recalculate or alter them. If a value is missing, say it is not available. Label generation, savings, cost, payback and CO2 as estimates. Do not expose personal identifiers, secrets, or internal tokens."

@app.get("/health")
def health():
    return {"status": "ok"}

@app.post("/api/ai/ask", response_model=AskResponse)
def ask(request: AskRequest, x_internal_token: str | None = Header(default=None)):
    if not configured(settings.ai_internal_token):
        raise HTTPException(status_code=503, detail="AI service authentication is not configured")
    if x_internal_token is None or not hmac.compare_digest(x_internal_token, settings.ai_internal_token):
        raise HTTPException(status_code=401, detail="Invalid internal token")
    if not configured(settings.gemini_api_key) or not configured(settings.gemini_model):
        raise HTTPException(status_code=503, detail="AI service is not configured")
    model = settings.gemini_model.strip()
    if model.startswith("models/"):
        model = model.removeprefix("models/")
    payload = {
        "system_instruction": {"parts": [{"text": SYSTEM_INSTRUCTION}]},
        "contents": [
            {
                "role": "user",
                "parts": [
                    {
                        "text": (
                            f"Question: {request.question}\n"
                            f"Verified assessment context: {request.context}"
                        )
                    }
                ],
            }
        ],
    }
    try:
        response = httpx.post(
            f"https://generativelanguage.googleapis.com/v1beta/models/{model}:generateContent",
            headers={"x-goog-api-key": settings.gemini_api_key},
            json=payload,
            timeout=20,
        )
        response.raise_for_status()
        candidates = response.json().get("candidates", [])
        text = candidates[0]["content"]["parts"][0]["text"]
        if not text.strip():
            raise ValueError("Gemini returned an empty answer")
        return AskResponse(answer=text)
    except (httpx.HTTPError, json.JSONDecodeError, KeyError, IndexError, TypeError, ValueError) as exc:
        raise HTTPException(status_code=503, detail="AI service is unavailable") from exc
