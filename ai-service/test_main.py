import json
from unittest.mock import Mock, patch

import pytest
from fastapi.testclient import TestClient

import main


client = TestClient(main.app)


@pytest.fixture(autouse=True)
def configured_settings(monkeypatch):
    monkeypatch.setattr(main.settings, "ai_internal_token", "test-internal-token")
    monkeypatch.setattr(main.settings, "gemini_api_key", "test-gemini-key")
    monkeypatch.setattr(main.settings, "gemini_model", "test-model")


def test_ask_sends_gemini_key_as_header_and_returns_answer():
    response = Mock()
    response.json.return_value = {
        "candidates": [{"content": {"parts": [{"text": "Use the verified estimate."}]}}]
    }
    response.raise_for_status.return_value = None

    with patch("main.httpx.post", return_value=response) as post:
        result = client.post(
            "/api/ai/ask",
            headers={"X-Internal-Token": "test-internal-token"},
            json={"question": "Explain this", "context": {}},
        )

    assert result.status_code == 200
    assert result.json() == {"answer": "Use the verified estimate."}
    assert post.call_args.kwargs["headers"] == {"x-goog-api-key": "test-gemini-key"}
    assert "params" not in post.call_args.kwargs


def test_invalid_internal_token_is_rejected():
    result = client.post(
        "/api/ai/ask",
        headers={"X-Internal-Token": "wrong-token"},
        json={"question": "Explain this", "context": {}},
    )

    assert result.status_code == 401


def test_invalid_gemini_json_returns_service_unavailable():
    response = Mock()
    response.raise_for_status.return_value = None
    response.json.side_effect = json.JSONDecodeError("invalid json", "", 0)

    with patch("main.httpx.post", return_value=response):
        result = client.post(
            "/api/ai/ask",
            headers={"X-Internal-Token": "test-internal-token"},
            json={"question": "Explain this", "context": {}},
        )

    assert result.status_code == 503
