import subprocess
import datetime
from pathlib import Path

# List of scripts to run (in order) by session
SESSION_SCRIPTS = {
    "session-1": [
        "01_get_all_todos.py",
        "02_get_todo_by_id.py",
        "03_create_todo_mandatory.py",
        "04_create_todo_with_relationships.py",
        "05_put_replace_todo.py",
        "06_post_amend_todo.py",
        "07_delete_todo_by_id.py",
        "08_options_todos.py",
        "09_head_todos.py",
    ],
    "session-2": [
        "01_get_categories_for_todo.py",
        "02_create_category_relationship.py",
        "03_delete_category_relationship.py",
        "04_taskof_endpoint_test.py",
    ],
    "session-3": [
        "01_post_create_validation.py",
        "02_post_type_coercion_and_doneStatus.py",
        "03_put_replacement_edge_cases.py",
        "04_post_amend_id_ignored_and_type_coercion.py",
        "05_relationship_nonexistent_parent_bugs.py",
    ],
}

BASE_DIR = Path(__file__).parent
LOG_FILE = BASE_DIR / f"demo_run_log_{datetime.datetime.now().strftime('%Y%m%d_%H%M%S')}.txt"


def run_script(session_name, script_name):
    script_path = BASE_DIR / session_name / script_name

    print(f"Running {session_name}/{script_name}...")

    result = subprocess.run(
        ["python", str(script_path)],
        capture_output=True,
        text=True
    )

    return {
        "name": f"{session_name}/{script_name}",
        "returncode": result.returncode,
        "stdout": result.stdout,
        "stderr": result.stderr
    }


def main():
    with open(LOG_FILE, "w", encoding="utf-8") as log:

        log.write("REST API TODO MANAGER - DEMO SCRIPT RUN\n")
        log.write("=" * 60 + "\n\n")

        for session_name, scripts in SESSION_SCRIPTS.items():
            log.write(f"SESSION: {session_name}\n")
            log.write("-" * 60 + "\n\n")

            for script in scripts:
                result = run_script(session_name, script)

                log.write(f"--- {result['name']} ---\n")
                log.write(f"Return code: {result['returncode']}\n\n")

                if result["stdout"]:
                    log.write("STDOUT:\n")
                    log.write(result["stdout"] + "\n")

                if result["stderr"]:
                    log.write("STDERR:\n")
                    log.write(result["stderr"] + "\n")

                log.write("\n" + "=" * 60 + "\n\n")

        log.write("All scripts executed.\n")

    print("\nFinished running all demo scripts.")
    print(f"Logs saved to: {LOG_FILE}")


if __name__ == "__main__":
    main()
