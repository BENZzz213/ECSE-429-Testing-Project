from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status, try_json

def main() -> None:
    resp = http("GET", "/todos")
    expect_status(resp, 200)
    data = try_json(resp)
    print("Capability 1: Retrieve all todos")
    print("Status:", resp.status_code)
    print("Body:", data if data is not None else resp.text)

if __name__ == "__main__":
    main()


