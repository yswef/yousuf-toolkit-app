#!/usr/bin/env python3
# Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
"""Regenerate server/public/panel/data/* from the app sources before uploading."""

import collections
import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
PANEL = ROOT / "server/public/panel"
DATA = PANEL / "data"
ASSETS = PANEL / "assets"
SIGN_START = "توقيع المطوّر: يوسف الحمزي — Y- Dimensions (بداية الملف)"
SIGN_END = "توقيع المطوّر: يوسف الحمزي — Y- Dimensions (نهاية الملف)"


def arabic_strings():
    out = {}
    for name in ("strings.xml", "strings_v3.xml"):
        path = ROOT / "app/src/main/res/values" / name
        if not path.is_file():
            continue
        for key, value in re.findall(
            r'<string\s+name="([^"]+)">(.*?)</string>', path.read_text(encoding="utf-8"), re.S
        ):
            out[key] = re.sub(r"<[^>]+>", "", value).replace('\\"', '"').strip()
    return out


def books_catalog(strings):
    source = (ROOT / "app/src/main/kotlin/feature/home/Books.kt").read_text(encoding="utf-8")
    sections = []
    for match in re.finditer(
        r"BookSection\(\s*R\.string\.([A-Za-z0-9_]+),\s*listOf\((.*?)\)\s*\)", source, re.S
    ):
        books = []
        for book in re.finditer(
            r"MagicBook\(Routes\.([A-Za-z0-9_]+),\s*R\.string\.([A-Za-z0-9_]+),"
            r"\s*Color\(0xFF([0-9A-Fa-f]{6})\),\s*Color\(0xFF([0-9A-Fa-f]{6})\)",
            match.group(2),
        ):
            books.append(collections.OrderedDict([
                ("route", book.group(1)),
                ("key", book.group(2)),
                ("title", strings.get(book.group(2), book.group(2))),
                ("from", "#" + book.group(3)),
                ("to", "#" + book.group(4)),
            ]))
        sections.append(collections.OrderedDict([
            ("title", strings.get(match.group(1), match.group(1))),
            ("books", books),
        ]))
    return collections.OrderedDict([
        ("__developer_signature__", SIGN_START),
        ("schema", "y-dimensions.book-catalog/1.0"),
        ("source", "app/src/main/kotlin/feature/home/Books.kt (استخراج تلقائي)"),
        ("app_id", "y-dimensions"),
        ("count", sum(len(s["books"]) for s in sections)),
        ("sections", sections),
        ("__developer_signature_end__", SIGN_END),
    ])


def version():
    text = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
    code = re.search(r"versionCode\s*=\s*(\d+)", text)
    name = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    return (name.group(1) if name else ""), int(code.group(1)) if code else 0


def signed_json(payload):
    return collections.OrderedDict([
        ("__developer_signature__", SIGN_START),
        *payload.items(),
        ("__developer_signature_end__", SIGN_END),
    ])


def main():
    DATA.mkdir(parents=True, exist_ok=True)
    ASSETS.mkdir(parents=True, exist_ok=True)
    strings = arabic_strings()
    app = json.loads((ROOT / "config/app.json").read_text(encoding="utf-8"))
    env = json.loads((ROOT / "config/environments.json").read_text(encoding="utf-8"))
    name, code = version()
    app["app"] = collections.OrderedDict([
        ("id", "y-dimensions"),
        ("name", "Y- Dimensions"),
        ("type", "mobile"),
        ("version", name),
        ("version_code", code),
        ("icon", "assets/icon.svg"),
        ("developer_name", app.get("developer", {}).get("name", "يوسف الحمزي")),
        ("developer_email", app.get("developer", {}).get("email", "yswefalhmzy@gmail.com")),
        ("api_base", env.get("apiBaseUrl", "")),
        ("tags", ["أدوات", "مهام", "مال", "صحة", "كتب سحرية", "موبايل"]),
        ("description", "تطبيق Y- Dimensions: مهام، مال، عادات، كتب سحرية، وأدوات الجهاز في واجهة عربية سهلة."),
    ])
    (DATA / "app.json").write_text(
        json.dumps(signed_json(app), ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    (DATA / "environments.json").write_text(
        json.dumps(signed_json(collections.OrderedDict(env)), ensure_ascii=False, indent=2) + "\n",
        encoding="utf-8",
    )
    catalog = books_catalog(strings)
    (DATA / "books.json").write_text(
        json.dumps(catalog, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    shutil.copyfile(
        ROOT / "server/public/boards/y-dimensions/assets/icon.svg", ASSETS / "icon.svg"
    )
    shutil.copyfile(
        ROOT / "app/src/main/res/mipmap-xxxhdpi/ic_launcher.png", ASSETS / "icon.png"
    )
    for extra in ("releases.json", "devmode_tests.json"):
        src = ROOT / "config" / extra
        if src.is_file():
            data = json.loads(src.read_text(encoding="utf-8"))
            if "__developer_signature__" not in data:
                data = signed_json(collections.OrderedDict(data))
            text = json.dumps(data, ensure_ascii=False, indent=2) + "\n"
            (DATA / extra).write_text(text, encoding="utf-8")
    print(f"panel data rebuilt: {catalog['count']} books, {len(catalog['sections'])} sections, version {name} ({code})")


if __name__ == "__main__":
    main()
# Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
