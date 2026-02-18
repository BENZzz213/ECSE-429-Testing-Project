from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status
from scripts.common.fixtures import create_typical_todo

def main() -> None:
    created = create_typical_todo(title="Delete demo")
    todo_id = str(created["id"])

    resp = http("DELETE", f"/todos/{todo_id}")
    expect_status(resp, 200)

    # Verify it's gone (typical behavior check)
    resp2 = http("GET", f"/todos/{todo_id}")
    if resp2.status_code != 404:
        raise AssertionError(f"Expected 404 after delete, got {resp2.status_code}. Body: {resp2.text}")

    print("Capability 7: Delete todo with specific id")
    print("Deleted id:", todo_id)
    print("Verified GET after delete returns 404")

if __name__ == "__main__":
    main()


