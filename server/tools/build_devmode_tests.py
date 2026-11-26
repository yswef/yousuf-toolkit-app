#!/usr/bin/env python3
# Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
"""يولّد كتالوج اختبارات وضع المطور من كود التطبيق نفسه (Books.kt + Routes.kt + strings)."""
from __future__ import annotations

import json
import re
import shutil
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
BOOKS = ROOT / "app/src/main/kotlin/feature/home/Books.kt"
ROUTES = ROOT / "app/src/main/kotlin/core/navigation/Routes.kt"
VALUES = ROOT / "app/src/main/res/values"
OUT_CONFIG = ROOT / "config/devmode_tests.json"
OUT_PANEL = ROOT / "server/public/panel/data/devmode_tests.json"

strings: dict[str, str] = {}
for f in sorted(VALUES.glob("*.xml")):
    for name, value in re.findall(r'<string\s+name="([^"]+)"\s*>(.*?)</string>', f.read_text(encoding="utf-8"), re.S):
        strings[name] = re.sub(r"<[^>]+>", "", value).replace("\\'", "'").replace('\\"', '"').strip()

books_kt = BOOKS.read_text(encoding="utf-8")
books = re.findall(r'MagicBook\(Routes\.(\w+),\s*R\.string\.(\w+)', books_kt)
section_of: dict[str, str] = {}
for block in re.findall(r'BookSection\(\s*R\.string\.(\w+),\s*listOf\((.*?)\)\s*\)', books_kt, re.S):
    sec, body = block
    for route, _ in re.findall(r'MagicBook\(Routes\.(\w+),\s*R\.string\.(\w+)', body):
        section_of[route] = strings.get(sec, sec)

all_routes = re.findall(r'const val (\w+) = "(\w+)"', ROUTES.read_text(encoding="utf-8"))
book_routes = {r for r, _ in books}
route_values = {name: value for name, value in all_routes}

extra_labels = {
    "AUTH": "بوابة الدخول", "DEVICES": "الأجهزة المسجّلة", "APPS": "إخفاء التطبيقات",
    "VISIBILITY": "الإخفاء والترتيب", "MEDIA": "الصور والفيديو", "FILES": "الملفات والمكتبة",
    "GIT": "إدارة مشاريع Git", "DIAGNOSTICS": "تشخيص العتاد والنظام",
    "UPDATER": "التحديثات", "CLEANER": "المنظّف", "BLOCKER": "الحاجز",
    "CONTACTS": "جهات الاتصال", "FIREWALL": "الجدار الناري", "NOTIFLOG": "سجل الإشعارات",
    "VAULT": "الخزنة", "WALLETS": "المحافظ", "MONEYPRO": "المالية المتقدّمة",
    "TASKSPRO": "المهام المتقدّمة", "DEVICEPRO": "الجهاز المتقدّم",
    "LEARNPRO": "التعلّم المتقدّم", "FAITHPRO": "الإيمان المتقدّم",
    "LIBRARYPRO": "المكتبة المتقدّمة", "STATS": "الإحصائيات", "PATH": "المسار",
    "SETTINGS": "الإعدادات", "ACCOUNTS": "الحسابات", "ACCOUNT": "الحساب",
    "INTRO": "المقدمة", "ONBOARDING": "التهيئة", "REGISTER": "التسجيل",
    "HOME": "الرئيسية", "PDFREADER": "قارئ PDF", "SETTINGS": "الإعدادات",
}

data_keys = {
    "TASKS": "tasks", "MONEY": "ledger", "WORKOUTS": "workouts", "JOURNAL": "journal",
    "WATER": "water", "SLEEP": "sleep", "FAITH": "faithDays", "ADHKAR": "adhkar",
    "WATCH": "watch", "LEARN": "learn", "LIBRARY": "books", "GAME": "points",
    "SHOP": "points", "RAIDS": "campaigns", "PROFILE": "profileName", "SKIN": "accent",
    "DEVICE": "serverUrl", "ACCOUNTS": "accounts", "FORUM": "serverUrl",
    "ALARM": "alarms", "LICENSE": "licenseCode", "DEVMODE": "version", "TESTS": "version",
    "AUTH": "authToken", "DEVICES": "accountDevices", "APPS": "hiddenApps",
    "VISIBILITY": "hiddenRoutes", "MEDIA": "screenshots", "FILES": "version",
    "GIT": "gitRepo", "DIAGNOSTICS": "version", "UPDATER": "version",
    "VAULT": "vault", "WALLETS": "wallets", "NOTIFLOG": "notifications",
    "BLOCKER": "blockedApps", "PATH": "xp", "STATS": "points", "SETTINGS": "accent",
}

actions = {
    "TASKS": "add_task", "MONEY": "add_ledger", "WORKOUTS": "add_workout",
    "JOURNAL": "add_journal", "WATER": "add_water", "SLEEP": "add_sleep",
    "LEARN": "add_learn", "LIBRARY": "add_book", "GAME": "add_progress",
    "SHOP": "add_progress", "RAIDS": "add_campaign", "PROFILE": "add_progress",
    "SKIN": "cycle_accent", "ALARM": "add_alarm", "VAULT": "add_vault",
    "WALLETS": "add_wallet", "NOTIFLOG": "add_notification", "AUTH": "check_session",
    "DEVICES": "check_devices", "GIT": "check_git", "DIAGNOSTICS": "run_diagnostics",
    "UPDATER": "check_updates", "TESTS": "run_selftest", "INTRO": "replay_intro",
    "SOUND": "play_sound", "LEVEL": "level_up",
}

special = [
    ("SOUND", "الصوتيات والمؤثرات", "المؤثرات", "sound", "play_sound"),
    ("LEVEL", "ارتفاع المستوى والرُتب", "التقدّم", "level", "level_up"),
    ("INTRO", "إعادة عرض المقدمة", "التجربة الأولى", "onboarded", "replay_intro"),
    ("NOTIFICATION", "الإشعارات المحلية", "النظام", "notifications", "add_notification"),
    ("SYNC", "مزامنة الحساب مع الخادم", "السحابة", "authToken", "check_sync"),
    ("CONTENT", "التحديثات الفرعية", "السحابة", "version", "check_updates"),
    ("BACKUP", "نسخ حالة التطبيق واستعادتها", "البيانات", "version", "check_backup"),
]

tests = []
for route, res in books:
    tests.append({
        "id": route, "route": route_values.get(route, route.lower()), "title": strings.get(res, res),
        "section": section_of.get(route, "كتب التطبيق"),
        "data_key": data_keys.get(route, "routes"),
        "action": actions.get(route, "open"), "enabled": True,
    })
for route, value in all_routes:
    if route in book_routes:
        continue
    label = extra_labels.get(route, strings.get("feature_" + route.lower(), route))
    tests.append({
        "id": route, "route": route_values.get(route, route.lower()), "title": label,
        "section": "الميزات الإضافية",
        "data_key": data_keys.get(route, "routes"),
        "action": actions.get(route, "open"), "enabled": True,
    })
for sid, title, section, key, action in special:
    tests.append({
        "id": sid, "route": route_values.get(sid, sid.lower()), "title": title, "section": section,
        "data_key": key, "action": action, "enabled": True,
    })

payload = {
    "schema": "y-dimensions.devmode-tests/1.0",
    "app_id": "y-dimensions",
    "note": "كل ميزة على حدى مع زر اختبار يدوي بجانب بياناتها. القائمة مولّدة من Books.kt وRoutes.kt.",
    "tests": tests,
}
text = json.dumps(payload, ensure_ascii=False, indent=2) + "\n"
OUT_CONFIG.write_text(text, encoding="utf-8")
OUT_PANEL.parent.mkdir(parents=True, exist_ok=True)
OUT_PANEL.write_text(text, encoding="utf-8")
print(f"devmode tests rebuilt: {len(tests)} tests -> {OUT_CONFIG} + {OUT_PANEL}")
# Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
