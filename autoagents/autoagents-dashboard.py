#!/usr/bin/env python3
"""Ephemeral LAN dashboard for autoagents task progress."""

from __future__ import annotations

import argparse
import os
import socket
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
ACTIVE_STATUSES = ("NEW", "FEAT", "WIP", "UNTESTED", "TESTING", "CLOSED")
COMPLETED_ACTIVE = {"TESTING", "CLOSED"}


def task_status(name: str) -> str | None:
    if name in SKIP_NAMES or not name.endswith(".md"):
        return None
    prefix = name.split("-", 1)[0].upper()
    return prefix if prefix in ACTIVE_STATUSES else None


def collect_tasks() -> tuple[dict[str, list[str]], int, int]:
    columns: dict[str, list[str]] = {s: [] for s in ACTIVE_STATUSES}

    for path in sorted(TASK_DIR.glob("*.md")):
        status = task_status(path.name)
        if status:
            columns[status].append(path.name)

    total = sum(len(v) for v in columns.values())
    completed = sum(len(columns[s]) for s in COMPLETED_ACTIVE)
    return columns, total, completed


def render_html(columns: dict[str, list[str]], total: int, completed: int) -> bytes:
    pct = 100 if total == 0 else round((completed / total) * 100)
    cols_html = []
    for status in ACTIVE_STATUSES:
        items = columns[status]
        if items:
            body = "\n".join(f"<li>{name}</li>" for name in items)
        else:
            body = '<li class="empty">—</li>'
        cols_html.append(
            f'<section class="col"><h3>{status} <span>({len(items)})</span></h3><ul>{body}</ul></section>'
        )

    html = f"""<!DOCTYPE html>
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
<footer>Actualiza cada 5 s · {TASK_DIR}</footer>
</div>
</body>
</html>"""
    return html.encode("utf-8")


class DashboardHandler(BaseHTTPRequestHandler):
    def log_message(self, fmt: str, *args) -> None:
        pass

    def do_GET(self) -> None:
        path = urlparse(self.path).path
        if path in ("/", "/index.html"):
            columns, total, completed = collect_tasks()
            body = render_html(columns, total, completed)
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
    print("Ctrl+C to stop")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\ndashboard stopped")


if __name__ == "__main__":
    main()
