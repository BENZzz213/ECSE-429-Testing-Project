# Scripts Folder

This folder contains the exploratory testing scripts for the REST API project plus shared helpers.

## Structure
- session-1: core todo capability demos (01–09)
- session-2: relationship-focused demos (todo ↔ category/tasksof)
- session-3: edge cases, validation, and bug-focused explorations
- common: shared HTTP client and fixtures
- run_all_demos.py: runs all session scripts in order and writes a timestamped log file in this folder
- demo_run_log_YYYYMMDD_HHMMSS.txt: output from the last run(s) of `run_all_demos.py`

## Notes
- Each session folder contains numbered scripts and is executed in order by `run_all_demos.py`.
- If you add a new session, update `run_all_demos.py` (unless it is later made to auto-discover sessions).
