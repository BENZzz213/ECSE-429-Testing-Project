from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    print("Session 3 - Script 03: PUT replacement defaults + empty title validation")

    created = create_typical_todo(title="PUT replace base", done=True, description="Before PUT")
    todo_id = str(created["id"])
    print("Base todo:", created)

    # A) PUT with title only resets other fields
    resp = http("PUT", f"/todos/{todo_id}", json={
        "title": "Updated Task (PUT)"
    })

    if resp.status_code != 200:
        raise AssertionError(f"Expected 200 for PUT, got {resp.status_code}. Body: {resp.text}")

    data = try_json(resp) or {}
    if str(data.get("doneStatus")) != "false":
        raise AssertionError(f"Expected doneStatus reset to false, got {data.get('doneStatus')}")
    if str(data.get("description")) != "":
        raise AssertionError(f"Expected description reset to empty string, got {data.get('description')}")

    print("PASS: PUT with partial body resets unspecified fields to defaults.")
    print("PUT response:", data)

    # B) Empty title rejected
    resp2 = http("PUT", f"/todos/{todo_id}", json={
        "title": ""
    })

    if resp2.status_code != 400:
        raise AssertionError(f"Expected 400 for empty title, got {resp2.status_code}. Body: {resp2.text}")

    msg2 = str(try_json(resp2) or {})
    if "can not be empty" not in msg2:
        raise AssertionError(f"Expected 'title can not be empty' message. Body: {resp2.text}")

    print("PASS: Empty title is rejected on PUT (400).")


if __name__ == "__main__":
    main()
