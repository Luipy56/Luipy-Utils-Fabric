#!/usr/bin/env python3
"""Ephemeral LAN dashboard for autoagents task progress."""

from __future__ import annotations

import argparse
import html
import os
import socket
import sys
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from urllib.parse import urlparse

SCRIPT_DIR = Path(__file__).resolve().parent
TASK_DIR = Path(os.environ.get("AGENT_TASKDIR", SCRIPT_DIR / "tasks"))
PRIMORDIAL_CSS = Path(
    os.environ.get("AGENT_PRIMORDIAL_CSS", "/home/luipy/Documents/primordial.css")
)
DEFAULT_PORT = int(os.environ.get("AGENT_DASHBOARD_PORT", "8765"))

SKIP_NAMES = {"README.md", "TEMPLATE.md"}
COLUMN_ORDER = ("GITHUB", "NEW", "FEAT", "WIP", "UNTESTED", "TESTING", "CLOSED")
FILE_STATUSES = ("NEW", "FEAT", "WIP", "UNTESTED", "TESTING", "CLOSED")
COMPLETED_ACTIVE = {"TESTING", "CLOSED"}


def load_env() -> None:
    env_file = SCRIPT_DIR / ".env"
    if not env_file.is_file():
        return
    for line in env_file.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, val = line.partition("=")
        os.environ.setdefault(key.strip(), val.strip().strip('"').strip("'"))


load_env()
sys.path.insert(0, str(SCRIPT_DIR))
from issue_checker_agent import has_task_file  # noqa: E402
from lib.gh_issue_actions import _gh_json, gh_available  # noqa: E402

GH_REPO = os.environ.get("AGENT_GH_REPO", "Luipy56/Luipy-Utils-McMod")


def task_status(name: str) -> str | None:
    if name in SKIP_NAMES or not name.endswith(".md"):
        return None
    prefix = name.split("-", 1)[0].upper()
    return prefix if prefix in FILE_STATUSES else None


def collect_github_queue() -> tuple[list[str], str]:
    if not gh_available():
        return [], "GitHub CLI no disponible"
    data = _gh_json(["issue", "list", "--state", "open", "--json", "number,title,url"])
    if data is None:
        return [], f"No se pudo listar issues en {GH_REPO} (gh auth / repo)"
    issues = data if isinstance(data, list) else []
    if not issues:
        return [], ""
    queued: list[str] = []
    for issue in issues:
        num = int(issue["number"])
        if has_task_file(num):
            continue
        title = html.escape(str(issue.get("title") or ""))
        url = html.escape(str(issue.get("url") or ""))
        queued.append(
            f'<a href="{url}" target="_blank" rel="noopener">#{num} {title}</a>'
        )
    if not queued and issues:
        return [], "issues abiertos ya enlazados a tareas"
    return queued, ""


def collect_tasks() -> tuple[dict[str, list[str]], int, int, str]:
    columns: dict[str, list[str]] = {s: [] for s in COLUMN_ORDER}

    for path in sorted(TASK_DIR.glob("*.md")):
        status = task_status(path.name)
        if status:
            columns[status].append(path.name)

    gh_items, gh_note = collect_github_queue()
    columns["GITHUB"] = gh_items

    file_total = sum(len(columns[s]) for s in FILE_STATUSES)
    total = file_total + len(gh_items)
    completed = sum(len(columns[s]) for s in COMPLETED_ACTIVE)
    return columns, total, completed, gh_note


def render_html(columns: dict[str, list[str]], total: int, completed: int, gh_note: str) -> bytes:
    pct = 100 if total == 0 else round((completed / total) * 100)
    cols_html = []
    for status in COLUMN_ORDER:
        items = columns[status]
        if items:
            if status == "GITHUB":
                body = "\n".join(f"<li>{item}</li>" for item in items)
            else:
                body = "\n".join(f"<li>{html.escape(name)}</li>" for name in items)
        else:
            body = '<li class="empty">—</li>'
        cols_html.append(
            f'<section class="col"><h3>{status} <span>({len(items)})</span></h3><ul>{body}</ul></section>'
        )

    gh_footer = f" · GitHub {html.escape(GH_REPO)}"
    if gh_note:
        gh_footer += f" · {html.escape(gh_note)}"

    html_doc = f"""<!DOCTYPE html>
<html lang="es" data-theme="verde">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta http-equiv="refresh" content="5">
<title>autoagents dashboard</title>
<link rel="stylesheet" href="/primordial.css">
<style>
.wrap {{ max-width: 1280px; }}
.progress-wrap {{ margin-bottom: 1.5rem; }}
.progress-meta {{ display: flex; justify-content: space-between; margin-bottom: 0.5rem; font-size: 0.9rem; color: var(--blanco-muted); }}
.progress-track {{ height: 1rem; border-radius: var(--radius-sm); background: var(--negro); border: 1px solid var(--gris-borde); overflow: hidden; }}
.progress-fill {{ height: 100%; width: {pct}%; background: linear-gradient(90deg, var(--accent), var(--accent-hover)); box-shadow: 0 0 12px var(--accent-glow); transition: width 0.4s ease; }}
.columns {{ display: grid; grid-template-columns: repeat(auto-fit, minmax(160px, 1fr)); gap: 1rem; }}
.col {{ background: var(--gris-card); border: 1px solid var(--gris-borde); border-radius: var(--radius); padding: 0.75rem; min-height: 8rem; }}
.col h3 {{ margin: 0 0 0.75rem; font-size: 0.85rem; font-family: 'JetBrains Mono', monospace; color: var(--accent); letter-spacing: 0.04em; }}
.col h3 span {{ color: var(--blanco-muted); font-weight: 400; }}
.col ul {{ list-style: none; margin: 0; padding: 0; }}
.col li {{ font-size: 0.78rem; line-height: 1.35; margin-bottom: 0.35rem; word-break: break-word; color: var(--blanco); }}
.col li a {{ color: var(--accent); text-decoration: none; }}
.col li a:hover {{ color: var(--accent-hover); }}
.col li.empty {{ color: var(--blanco-muted); font-style: italic; }}
footer {{ margin-top: 1.5rem; font-size: 0.8rem; color: var(--blanco-muted); }}
</style>
</head>
<body>
<div class="wrap">
<header><h1>autoagents</h1></header>
<main class="form">
<h2>Progreso</h2>
<div class="progress-wrap">
<div class="progress-meta"><span>{completed} / {total} testeadas y cerradas</span><span>{pct}%</span></div>
<div class="progress-track"><div class="progress-fill"></div></div>
</div>
<div class="columns">
{"".join(cols_html)}
</div>
</main>
<footer>Actualiza cada 5 s · {html.escape(str(TASK_DIR))}{gh_footer}</footer>
</div>
</body>
</html>"""
    return html_doc.encode("utf-8")


class DashboardHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt: str, *args) -> None:
        pass

    def do_GET(self) -> None:
        path = urlparse(self.path).path
        if path in ("/", "/index.html"):
            columns, total, completed, gh_note = collect_tasks()
            body = render_html(columns, total, completed, gh_note)
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return
        if path == "/primordial.css":
            if not PRIMORDIAL_CSS.is_file():
                self.send_error(404, "primordial.css not found")
                return
            body = PRIMORDIAL_CSS.read_bytes()
            self.send_response(200)
            self.send_header("Content-Type", "text/css; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return
        self.send_error(404)


def lan_ip() -> str:
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as s:
            s.connect(("8.8.8.8", 80))
            return s.getsockname()[0]
    except OSError:
        return "127.0.0.1"


def main() -> None:
    parser = argparse.ArgumentParser(description="Ephemeral autoagents task dashboard")
    parser.add_argument("--host", default="0.0.0.0")
    parser.add_argument("--port", type=int, default=DEFAULT_PORT)
    args = parser.parse_args()
    server = ThreadingHTTPServer((args.host, args.port), DashboardHandler)
    ip = lan_ip()
    print(f"autoagents dashboard → http://127.0.0.1:{args.port}/  (LAN: http://{ip}:{args.port}/)")
    print(f"GitHub repo: {GH_REPO}")
    print("Ctrl+C to stop")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\ndashboard stopped")


if __name__ == "__main__":
    main()
