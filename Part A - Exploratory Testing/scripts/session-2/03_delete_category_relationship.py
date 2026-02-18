from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    created = create_typical_todo(title="Session2 - Delete category relationship demo")
    todo_id = str(created["id"])

    # Link category 1, then delete it
    link = http("POST", f"/todos/{todo_id}/categories", json={"id": "1"})
    expect_status(link, 201)

    del_resp = http("DELETE", f"/todos/{todo_id}/categories/1")
    expect_status(del_resp, 200)

    # Verify category 1 is no longer linked
    verify = http("GET", f"/todos/{todo_id}/categories")
    expect_status(verify, 200)
    data = try_json(verify)
    categories = data.get("categories", [])
    ids = {c.get("id") for c in categories if isinstance(c, dict)}
    assert "1" not in ids, f"Expected category '1' removed, but still present: {ids}"

    print("Session 2 - Capability: Delete todo-category relationship")
    print("Todo id:", todo_id)
    print("PASS: Deleted relationship /categories/1 and verified it is removed.")

    # Deleting a relationship that doesn't exist should return 404 (per your notes)
    del_missing = http("DELETE", f"/todos/{todo_id}/categories/2")
    if del_missing.status_code != 404:
        raise AssertionError(
            f"Expected 404 when deleting non-linked relationship, got {del_missing.status_code}\n"
            f"Body: {del_missing.text}"
        )

    print("PASS: Deleting non-linked relationship returned 404 as expected.")
    print("Error body:", try_json(del_missing) if try_json(del_missing) else del_missing.text)


if __name__ == "__main__":
    main()
