from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status

def main() -> None:
    resp = http("HEAD", "/todos")
    expect_status(resp, 200)

    # HEAD should have no body
    body_len = len(resp.content or b"")
    print("Capability 9: Support HTTP HEAD")
    print("Status:", resp.status_code)
    print("Content-Type:", resp.headers.get("Content-Type", "(missing)"))
    print("Body length:", body_len)

if __name__ == "__main__":
    main()


