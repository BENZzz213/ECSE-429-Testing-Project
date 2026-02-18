from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    print("Session 3 - Script 04: POST amend bug (id ignored) + coercion")

    created = create_typical_todo(title="POST amend base", done=False, description="Before amend")
    todo_id = str(created["id"])
    print("Base todo:", created)

    # A) BUG: send an id field, API returns 200 but doesn't change id
    resp = http("POST", f"/todos/{todo_id}", json={"id": 100})
    if resp.status_code != 200:
        raise AssertionError(f"Expected 200 for POST amend with id field, got {resp.status_code}. Body: {resp.text}")

    data = try_json(resp) or {}
    if str(data.get("id")) != todo_id:
        raise AssertionError(f"Expected id to remain {todo_id}, got {data.get('id')}")

    print("PASS (bug demonstrated): POST /todos/:id accepts body with id=100 and returns 200, while id remains unchanged.")
    print("POST response:", data)

    # B) description boolean coerces to string
    resp2 = http("POST", f"/todos/{todo_id}", json={"description": False})
    if resp2.status_code != 200:
        raise AssertionError(f"Expected 200 for POST amend description=false, got {resp2.status_code}. Body: {resp2.text}")

    data2 = try_json(resp2) or {}
    if str(data2.get("description")) != "false":
        raise AssertionError(f"Expected description coerced to 'false', got {data2.get('description')}")

    print("PASS: POST coerces boolean description to string 'false'.")
    print("POST response:", data2)


if __name__ == "__main__":
    main()
