from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json

def main() -> None:
    # Uses typical IDs seen in the initial dataset (category 1, project 1)
    resp = http("POST", "/todos", json={
        "title": "Relationships demo",
        "doneStatus": False,
        "description": "Todo created with relationships",
        "categories": [{"id": "1"}],
        "tasksof": [{"id": "1"}]
    })
    expect_status(resp, 201)
    data = try_json(resp)

    print("Capability 4: Create todo including relationships")
    print("Created:", data if data is not None else resp.text)

if __name__ == "__main__":
    main()


