from __future__ import annotations
from typing import Any, Dict
from .client import http, expect_status, try_json

def create_typical_todo(title: str = "Script Demo Todo",
                        done: bool = False,
                        description: str = "Created by demo script") -> Dict[str, Any]:
    resp = http("POST", "/todos", json={
        "title": title,
        "doneStatus": done,
        "description": description
    })
    expect_status(resp, 201)
    data = try_json(resp)
    if not isinstance(data, dict) or "id" not in data:
        raise AssertionError(f"Create todo did not return an id. Body: {resp.text}")
    return data

def delete_todo(todo_id: str) -> None:
    resp = http("DELETE", f"/todos/{todo_id}")
    expect_status(resp, 200)
