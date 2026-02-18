from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json

def main() -> None:
    resp = http("POST", "/todos", json={
        "title": "Mandatory fields demo"
        # typical minimal input: title only
    })
    expect_status(resp, 201)
    data = try_json(resp)

    print("Capability 3: Create todo with mandatory fields")
    print("Created:", data if data is not None else resp.text)

if __name__ == "__main__":
    main()


