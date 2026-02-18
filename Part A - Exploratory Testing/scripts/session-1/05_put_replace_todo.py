from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo

def main() -> None:
    created = create_typical_todo(title="PUT replace demo", description="Before PUT")
    todo_id = str(created["id"])

    resp = http("PUT", f"/todos/{todo_id}", json={
        "title": "Updated Task (PUT)",
        "doneStatus": True,
        "description": "Updated via PUT"
    })
    expect_status(resp, 200)
    data = try_json(resp)

    print("Capability 5: Replace todo using PUT")
    print("Updated id:", todo_id)
    print("Response:", data if data is not None else resp.text)

if __name__ == "__main__":
    main()


