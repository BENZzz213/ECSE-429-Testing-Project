from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, try_json


def main() -> None:
    print("Session 3 - Script 01: POST /todos validation (no ID injection, mandatory title)")

    # A) Cannot create with explicit ID
    resp = http("POST", "/todos", json={
        "id": 100,
        "title": 3,
        "doneStatus": False,
        "description": 2
    })

    if resp.status_code != 400:
        raise AssertionError(f"Expected 400 when creating with id, got {resp.status_code}. Body: {resp.text}")

    data = try_json(resp) or {}
    msg = str(data)
    if "Not allowed to create with id" not in msg:
        raise AssertionError(f"Expected error about id not allowed. Body: {resp.text}")

    print("PASS: Creating a todo with 'id' is rejected (400).")

    # B) Missing mandatory title
    resp2 = http("POST", "/todos", json={
        "doneStatus": False,
        "description": "no title"
    })

    if resp2.status_code != 400:
        raise AssertionError(f"Expected 400 when title missing, got {resp2.status_code}. Body: {resp2.text}")

    data2 = try_json(resp2) or {}
    msg2 = str(data2)
    if "title : field is mandatory" not in msg2:
        raise AssertionError(f"Expected mandatory title error. Body: {resp2.text}")

    print("PASS: Missing title is rejected (400).")


if __name__ == "__main__":
    main()
