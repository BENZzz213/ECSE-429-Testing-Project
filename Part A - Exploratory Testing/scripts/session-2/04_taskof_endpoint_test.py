from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo


def main() -> None:
    created = create_typical_todo(title="Session2 - tasksof endpoint mismatch demo")
    todo_id = str(created["id"])
    project_id = "1"  # typical project id from dataset

    print("Session 2 - Capability: Todo <-> Project relationship (tasksof) + spec mismatch checks")
    print("Todo id:", todo_id)

    # Create relationship using the supported relationship collection endpoint
    link = http("POST", f"/todos/{todo_id}/tasksof", json={"id": project_id})
    expect_status(link, 201)
    print("PASS: Linked todo to project via POST /todos/:id/tasksof")

    # Confirm relationship list is retrievable
    list_resp = http("GET", f"/todos/{todo_id}/tasksof")
    expect_status(list_resp, 200)
    list_data = try_json(list_resp)
    print("Related projects:", list_data if list_data is not None else list_resp.text)

    # Now test the sub-resource endpoint that docs say is 405 but you observed 404:
    # GET /todos/:id/tasksof/:id
    sub_get = http("GET", f"/todos/{todo_id}/tasksof/{project_id}")
    if sub_get.status_code not in (404, 405):
        raise AssertionError(
            f"Expected 404 or 405 for GET /tasksof/:id, got {sub_get.status_code}\nBody: {sub_get.text}"
        )
    print(f"Observed GET /todos/:id/tasksof/:id status = {sub_get.status_code} (expected 405 per docs, observed often 404)")

    # POST /todos/:id/tasksof/:id
    sub_post = http("POST", f"/todos/{todo_id}/tasksof/{project_id}", json={"id": project_id})
    if sub_post.status_code not in (404, 405):
        raise AssertionError(
            f"Expected 404 or 405 for POST /tasksof/:id, got {sub_post.status_code}\nBody: {sub_post.text}"
        )
    print(f"Observed POST /todos/:id/tasksof/:id status = {sub_post.status_code} (expected 405 per docs, observed often 404)")

    print("PASS: tasksof relationship works via collection endpoint; sub-resource endpoint shows 404/405 mismatch as documented in session notes.")


if __name__ == "__main__":
    main()
