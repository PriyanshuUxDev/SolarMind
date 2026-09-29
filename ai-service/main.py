from fastapi import FastAPI, Header, HTTPException
import httpx
from pydantic import BaseModel, Field
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    ai_internal_token: str = ""
    gemini_api_key: str = ""
    gemini_model: str = ""
    class Config: env_file = ".env"

settings = Settings()
app = FastAPI(title="SolarMind AI Service")

class AskContext(BaseModel):
    recommendedCapacity: float | None = None
    panelBrand: str | None = None
    panelModel: str | None = None

class AskRequest(BaseModel):
    question: str = Field(min_length=1, max_length=2000)
    context: dict = Field(default_factory=dict)

class AskResponse(BaseModel):
    answer: str

SYSTEM_INSTRUCTION = "You explain a solar recommendation calculated by a verified backend. Use only the supplied values; do not recalculate or alter them. If a value is missing, say it is not available. Label generation, savings, cost, payback and CO2 as estimates. Do not expose personal identifiers, secrets, or internal tokens."

@app.get("/health")
def health(): return {"status": "ok"}

@app.post("/api/ai/ask", response_model=AskResponse)
def ask(request: AskRequest, x_internal_token: str | None = Header(default=None)):
    if settings.ai_internal_token and x_internal_token != settings.ai_internal_token:
        raise HTTPException(status_code=401, detail="Invalid internal token")
    if not settings.gemini_api_key or not settings.gemini_model:
        raise HTTPException(status_code=503, detail="AI service is not configured")
    payload = {"system_instruction": {"parts": [{"text": SYSTEM_INSTRUCTION}]}, "contents": [{"role": "user", "parts": [{"text": f"Question: {request.question}\nVerified assessment context: {request.context}"}]}]}
    try:
        response = httpx.post(f"https://generativelanguage.googleapis.com/v1beta/models/{settings.gemini_model}:generateContent", params={"key": settings.gemini_api_key}, json=payload, timeout=20)
        response.raise_for_status()
        text = response.json()["candidates"][0]["content"]["parts"][0]["text"]
        return AskResponse(answer=text)
    except (httpx.HTTPError, KeyError, IndexError, TypeError) as exc:
        raise HTTPException(status_code=503, detail="AI service is unavailable") from exc
