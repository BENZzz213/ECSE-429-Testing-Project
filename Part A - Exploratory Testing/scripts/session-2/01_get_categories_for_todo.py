from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    # Create a typical todo and link a category so GET has meaningful data
    created = create_typical_todo(title="Session2 - Get categories demo")
    todo_id = str(created["id"])

    # Link category 1 (typical data from initial dataset)
    link_resp = http("POST", f"/todos/{todo_id}/categories", json={"id": "1"})
    expect_status(link_resp, 201)

    # Retrieve related categories
    resp = http("GET", f"/todos/{todo_id}/categories")
    expect_status(resp, 200)
    data = try_json(resp)

    print("Session 2 - Capability: Retrieve categories related to a todo")
    print("Todo id:", todo_id)
    print("Response:", data if data is not None else resp.text)

    # Basic sanity check: ensure category 1 appears
    try:
        categories = data.get("categories", [])
        ids = {c.get("id") for c in categories if isinstance(c, dict)}
        assert "1" in ids, f"Expected category '1' in related categories, got {ids}"
        print("PASS: Category 1 is linked and visible via GET /todos/:id/categories")
    except Exception as e:
        raise AssertionError(f"Unexpected categories response format.\nBody: {resp.text}\nError: {e}")


if __name__ == "__main__":
    main()
