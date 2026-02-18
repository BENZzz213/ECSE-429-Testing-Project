from pathlib import Path
import sys

REPO_ROOT = Path(__file__).resolve().parents[2]
if str(REPO_ROOT) not in sys.path:
    sys.path.insert(0, str(REPO_ROOT))

from scripts.common.client import http, try_json


def main() -> None:
    print("Session 3 - Script 02: POST type coercion + doneStatus type validation")

    # A) Title and description ints become strings
    resp = http("POST", "/todos", json={
        "title": 3,
        "doneStatus": False,
        "description": 2
    })

    if resp.status_code != 201:
        raise AssertionError(f"Expected 201 for creating todo, got {resp.status_code}. Body: {resp.text}")

    data = try_json(resp)
    if not isinstance(data, dict) or "id" not in data:
        raise AssertionError(f"Expected created todo JSON with id. Body: {resp.text}")

    # observed behavior: "3.0" / "2.0"
    if str(data.get("title")) not in ("3.0", "3"):
        raise AssertionError(f"Expected title coerced to string, got: {data.get('title')}")
    if str(data.get("description")) not in ("2.0", "2"):
        raise AssertionError(f"Expected description coerced to string, got: {data.get('description')}")

    print("PASS: title/description integers are coerced to string values.")
    print("Created todo:", data)

    # B) doneStatus int rejected
    resp2 = http("POST", "/todos", json={
        "title": "doneStatus-int test",
        "doneStatus": 1,
        "description": "should fail"
    })

    if resp2.status_code != 400:
        raise AssertionError(f"Expected 400 when doneStatus is int, got {resp2.status_code}. Body: {resp2.text}")

    data2 = try_json(resp2) or {}
    msg2 = str(data2)
    if "doneStatus should be BOOLEAN" not in msg2:
        raise AssertionError(f"Expected doneStatus BOOLEAN validation message. Body: {resp2.text}")

    print("PASS: doneStatus integer is rejected with 400.")


if __name__ == "__main__":
    main()
