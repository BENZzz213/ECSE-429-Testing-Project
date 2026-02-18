from pathlib import Path
import sys

# Allow running from anywhere
REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, expect_status

def main() -> None:
    resp = http("OPTIONS", "/todos")
    expect_status(resp, 200)

    allow = resp.headers.get("Allow", "")
    print("Capability 8: Support HTTP OPTIONS")
    print("Status:", resp.status_code)
    print("Allow header:", allow if allow else "(missing)")

if __name__ == "__main__":
    main()


