from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    created = create_typical_todo(title="Session2 - Create category relationship demo")
    todo_id = str(created["id"])

    # Link category 2
    r1 = http("POST", f"/todos/{todo_id}/categories", json={"id": "2"})
    expect_status(r1, 201)

    # Link category 1 (should not overwrite category 2)
    r2 = http("POST", f"/todos/{todo_id}/categories", json={"id": "1"})
    expect_status(r2, 201)

    # Loose validation test: extra fields beyond "id"
    r3 = http("POST", f"/todos/{todo_id}/categories", json={"id": "1", "title": "test"})
    expect_status(r3, 201)

    # Verify both 1 and 2 are linked
    resp = http("GET", f"/todos/{todo_id}/categories")
    expect_status(resp, 200)
    data = try_json(resp)

    print("Session 2 - Capability: Create todo-category relationships (multiple, non-overwriting)")
    print("Todo id:", todo_id)
    print("Related categories:", data if data is not None else resp.text)

    categories = data.get("categories", [])
    ids = {c.get("id") for c in categories if isinstance(c, dict)}
    assert "1" in ids and "2" in ids, f"Expected linked categories {{'1','2'}}, got {ids}"

    print("PASS: Multiple category relationships exist without overwriting.")
    print("PASS: Relationship creation accepted extra fields (loose validation).")


if __name__ == "__main__":
    main()
