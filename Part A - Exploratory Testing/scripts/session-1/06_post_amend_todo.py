from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json
from scripts.common.fixtures import create_typical_todo

def main() -> None:
    created = create_typical_todo(title="POST amend demo", description="Before POST amend")
    todo_id = str(created["id"])

    # Partial update (typical amend use)
    resp = http("POST", f"/todos/{todo_id}", json={
        "description": "Amended via POST"
    })
    expect_status(resp, 200)
    data = try_json(resp)

    print("Capability 6: Amend todo using POST /:id")
    print("Amended id:", todo_id)
    print("Response:", data if data is not None else resp.text)

if __name__ == "__main__":
    main()


