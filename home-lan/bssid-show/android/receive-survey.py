#!/usr/bin/env python3
"""@tag:survey-upload Одноразовый HTTP PUT-приёмник файла замеров."""

import argparse
import re
from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from urllib.parse import unquote, urlsplit

PATTERN = re.compile(r"survey-\d{8}-\d{4}\.csv")
MAX_BYTES = 10 * 1024 * 1024
DEFAULT_DIR = Path("~/Projects/home/home-config-secrets/ansible/survey-results").expanduser()


def survey_name(path):
    name = unquote(urlsplit(path).path).removeprefix("/")
    if not PATTERN.fullmatch(name):
        raise ValueError("ожидается имя survey-YYYYMMDD-HHMM.csv")
    return name


class Receiver(BaseHTTPRequestHandler):
    def do_PUT(self):
        try:
            name = survey_name(self.path)
            size = int(self.headers["Content-Length"])
            if not 0 < size <= MAX_BYTES:
                raise ValueError("некорректный размер файла")
        except (KeyError, TypeError, ValueError) as error:
            self.send_error(400, str(error))
            return

        destination = self.server.output / name
        partial = destination.with_suffix(".csv.part")
        try:
            left = size
            with partial.open("wb") as out:
                while left:
                    chunk = self.rfile.read(min(left, 64 * 1024))
                    if not chunk:
                        raise OSError("соединение закрылось до конца файла")
                    out.write(chunk)
                    left -= len(chunk)
            partial.replace(destination)
        except OSError as error:
            partial.unlink(missing_ok=True)
            self.send_error(400, str(error))
            return

        self.send_response(201)
        self.end_headers()
        self.server.received = destination
        print(f"получен: {destination} ({size} байт)")

    def log_message(self, *_):
        pass


def check():
    assert survey_name("/survey-20260808-1234.csv") == "survey-20260808-1234.csv"
    for bad in ("/x.csv", "/../survey-20260808-1234.csv", "/survey-20260808.csv"):
        try:
            survey_name(bad)
            raise AssertionError(bad)
        except ValueError:
            pass
    print("receive-survey: ok")


def main():
    parser = argparse.ArgumentParser(description="Принять один CSV замеров по HTTP PUT")
    parser.add_argument("directory", nargs="?", type=Path, default=DEFAULT_DIR)
    parser.add_argument("--port", type=int, default=8765)
    parser.add_argument("--check", action="store_true")
    args = parser.parse_args()
    if args.check:
        check()
        return

    output = args.directory.expanduser().resolve()
    output.mkdir(parents=True, exist_ok=True)
    HTTPServer.allow_reuse_address = True
    server = HTTPServer(("0.0.0.0", args.port), Receiver)
    server.output = output
    server.received = None
    print(f"жду один файл: http://192.168.2.122:{args.port}/")
    print(f"каталог: {output}")
    try:
        while server.received is None:
            server.handle_request()
    except KeyboardInterrupt:
        print("\nостановлен без файла")


if __name__ == "__main__":
    main()
