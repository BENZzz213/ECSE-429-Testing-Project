from __future__ import annotations
import os
import requests
from typing import Any, Dict, Optional

DEFAULT_BASE_URL = "http://localhost:4567"

def base_url() -> str:
    return os.getenv("BASE_URL", DEFAULT_BASE_URL).rstrip("/")

def http(method: str, path: str, *, json: Optional[Dict[str, Any]] = None,
         headers: Optional[Dict[str, str]] = None) -> requests.Response:
    url = f"{base_url()}{path}"
    return requests.request(method, url, json=json, headers=headers, timeout=10)

def expect_status(resp: requests.Response, expected: int) -> None:
    if resp.status_code != expected:
        raise AssertionError(
            f"Expected {expected} but got {resp.status_code}\n"
            f"URL: {resp.request.method} {resp.url}\n"
            f"Response: {resp.text}"
        )

def try_json(resp: requests.Response) -> Any:
    try:
        return resp.json()
    except Exception:
        return None
