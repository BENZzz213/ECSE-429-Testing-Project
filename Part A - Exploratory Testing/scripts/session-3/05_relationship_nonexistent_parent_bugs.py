from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, try_json


def _has_duplicates(items: list, key: str) -> bool:
    seen = set()
    for it in items:
        if not isinstance(it, dict):
            continue
        v = it.get(key)
        if v in seen:
            return True
        seen.add(v)
    return False


def main() -> None:
    print("Session 3 - Script 05: Relationship bugs when parent todo does not exist (id=1000)")

    bad_todo_id = "1000"

    # A) categories list on nonexistent parent returns duplicated data (bug)
    resp = http("GET", f"/todos/{bad_todo_id}/categories")
    if resp.status_code != 200:
        raise AssertionError(f"Expected 200 (observed bug behavior) for categories on nonexistent todo, got {resp.status_code}. Body: {resp.text}")

    data = try_json(resp) or {}
    cats = data.get("categories", [])
    if not isinstance(cats, list) or len(cats) == 0:
        raise AssertionError(f"Expected categories list (bug), got: {resp.text}")

    if not _has_duplicates(cats, "id"):
        raise AssertionError(f"Expected duplicate categories (bug), but did not detect duplicates. Body: {resp.text}")

    print("PASS (bug demonstrated): GET /todos/1000/categories returns duplicated categories instead of 404.")
    print("Categories count:", len(cats))

    # B) tasksof list on nonexistent parent returns duplicated projects (bug)
    resp2 = http("GET", f"/todos/{bad_todo_id}/tasksof")
    if resp2.status_code != 200:
        raise AssertionError(f"Expected 200 (observed bug behavior) for tasksof on nonexistent todo, got {resp2.status_code}. Body: {resp2.text}")

    data2 = try_json(resp2) or {}
    projs = data2.get("projects", [])
    if not isinstance(projs, list) or len(projs) == 0:
        raise AssertionError(f"Expected projects list (bug), got: {resp2.text}")

    if not _has_duplicates(projs, "id"):
        raise AssertionError(f"Expected duplicate projects (bug), but did not detect duplicates. Body: {resp2.text}")

    print("PASS (bug demonstrated): GET /todos/1000/tasksof returns duplicated projects instead of 404.")
    print("Projects count:", len(projs))

    # C) delete relationship on nonexistent parent returns internal error message
    resp3 = http("DELETE", f"/todos/{bad_todo_id}/categories/1")
    if resp3.status_code != 400:
        raise AssertionError(f"Expected 400 (observed internal error behavior) for DELETE categories on nonexistent todo, got {resp3.status_code}. Body: {resp3.text}")

    msg3 = str(try_json(resp3) or {})
    if "parent" not in msg3 and "null" not in msg3:
        raise AssertionError(f"Expected internal 'parent is null' style message. Body: {resp3.text}")

    print("PASS (bug demonstrated): DELETE /todos/1000/categories/1 returns internal exception message (parent null).")
    print("Error body:", try_json(resp3) if try_json(resp3) else resp3.text)

    resp4 = http("DELETE", f"/todos/{bad_todo_id}/tasksof/1")
    if resp4.status_code != 400:
        raise AssertionError(f"Expected 400 (observed internal error behavior) for DELETE tasksof on nonexistent todo, got {resp4.status_code}. Body: {resp4.text}")

    msg4 = str(try_json(resp4) or {})
    if "parent" not in msg4 and "null" not in msg4:
        raise AssertionError(f"Expected internal 'parent is null' style message. Body: {resp4.text}")

    print("PASS (bug demonstrated): DELETE /todos/1000/tasksof/1 returns internal exception message (parent null).")


if __name__ == "__main__":
    main()
