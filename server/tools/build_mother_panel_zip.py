#!/usr/bin/env python3
# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
"""Build a Mother-Panel ZIP for a domain that already hosts another site/panel."""

import json
import re
import zipfile
from pathlib import Path, PurePosixPath

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "docs/mother-panel" / "y-dimensions-mother-panel.zip"
SIGN = "يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com"
SIGN_START = "توقيع المطوّر: " + SIGN + " (بداية الملف)"
SIGN_END = "توقيع المطوّر: " + SIGN + " (نهاية الملف)"

ROOT_HTACCESS = """# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
Options -Indexes
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteRule ^(src|config|migrations)(/|$) - [F,L]
    RewriteRule ^(INSTALL\\.md|mother-panel\\.json|config/config\\.example\\.php)$ - [F,L]
</IfModule>
# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
"""

PUBLIC_HTACCESS = """# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
Options -Indexes
DirectoryIndex index.php
<IfModule mod_rewrite.c>
    RewriteEngine On
    RewriteRule ^(\\.htaccess|\\.gitkeep)$ - [F,L]
</IfModule>
# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
"""

SETTINGS_EXAMPLE = """<?php
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com (بداية الملف)
return ['server_root' => dirname(__DIR__, 2)];
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com (نهاية الملف)
"""

INSTALL_MD = """<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com (بداية الملف) -->
# تثبيت لوحة Y- Dimensions التنفيذية داخل لوحة أم موجودة

هذه الحزمة **لا تُفرَّغ في جذر `public_html/`**. جذر الدومين فيه مسبقاً `config.php`،
`config-sample.php`، `index.php`، `admin/`، `app/`، `assets/`، `core/`، `data/`،
`docs/`، `lang/`، `cron_*.php`، `status.php`، `update_start.php`، `update_step.php`،
و`manifest.webmanifest`. كلها تبقى كما هي ولا تُستبدل.

هذه الحزمة منشورة داخل المستودع في `docs/mother-panel/y-dimensions-mother-panel.zip`
وتُرفق تلقائياً كأصل في كل إصدار `v*` يبنيه `.github/workflows/release.yml`؛
رابط المستودع: https://github.com/yswef/yousuf-toolkit-app/blob/dev/docs/mother-panel/y-dimensions-mother-panel.zip
لا تُؤخذ من مجلد `build/` المستثنى من Git، ولا تُنسَب إلى إصدار قديم لم يحتوِ هذا الأصل.

## 1. أين تُستخرج؟

اسأل لوحة الأم عن `{mother_panel.apps_dir}` وعن المجلد الذي خصصته للتطبيق،
وليكن مثلاً `<allocated-folder>` (مثال فقط: `public_html/apps/y-dimensions/`).
استخرج **محتويات هذا الملف المضغوط مباشرة داخل `<allocated-folder>`**، بلا مجلد إضافي فوقها:

```
<allocated-folder>/
├── .htaccess
├── INSTALL.md
├── mother-panel.json
├── config/          ← config.example.php + .htaccess، وأنشئ config.php هنا فقط
├── src/             ← محمي بـ .htaccess
├── migrations/      ← محمي بـ .htaccess
└── public/
    ├── .htaccess
    ├── index.php     ← فحص صحة JSON
    ├── api/          ← نقاط API الحقيقية
    ├── content/VERSION
    ├── learn/
    └── panel/        ← لوحة التحكم التنفيذية (18 تبويباً)
```

إن كان في لوحة الأم زر «رفع حزمة تطبيق / Install from ZIP»، ارفع هذا الملف هناك واتركها
تُنشئ المجلد وتُرجع لك الرابط. إن لم يوجد، أنشئ المجلد من File Manager وارفع الحزمة
ثم `Extract` داخله. **لا تختر `public_html/` كوجهة الاستخراج.**

## 2. الإعداد

1. انسخ `config/config.example.php` إلى `config/config.php` **داخل المجلد المخصص**.
2. املأ `db`, `base_url`, `license_secret`, `admin_token`, `allowed_origins` من مالك
   لوحة الأم/الاستضافة فقط. لا تخمّنها، ولا تلمس `public_html/config.php` ولا
   `public_html/config-sample.php`، ولا ترفع `config.php` إلى Git.
3. `base_url` هو رابط المجلد العام الذي تعطيه لوحة الأم، مثالاً:
   `https://<domain>/<allocated-path>/public` (بدون `/panel` في النهاية).

## 3. قاعدة البيانات — مهم

القاعدة واحدة و`users` موحّد كما في `docs/dev/RULES.md` و`MOTHER_PANEL_HANDOFF.md`.
ملف `migrations/001_init.sql` في هذه الحزمة **ينشئ جدول `users`**؛ لذلك لا تستورده كما هو
على قاعدة لوحة الأم الموحّدة. المطلوب من موديل لوحة الأم تنفيذ عقد التسليم: ربط الجداول
الموحّدة، واستيراد الترحيلات غير المكررة فقط (`002_admin.sql`…`006_learning.sql` بعد
التأكد)، أو تعديل `001_init.sql` إلى `CREATE TABLE IF NOT EXISTS` مع بقاء `users` الأصلي.
إلى أن يتم ذلك تعمل اللوحة في **وضع القراءة فقط** بالبيانات المحلية؛ لا تُعلن اكتمال التشغيل.

## 4. الروابط التي تُرجعها لوحة الأم

بعد الاستخراج تكتب لوحة الأم في بطاقة `y-dimensions`:

- `app_public_url = https://<domain>/<allocated-path>/public`
- `panel_url      = https://<domain>/<allocated-path>/public/panel/`
- `api_base       = app_public_url` (أو `.../public/panel` لأن `panel/api/*.php` تحوّل الطلبات)
- `mother_panel.apps.open_panel` → `panel_url` في **تبويب/صفحة جديدة**.

اللوحة ترسل `X-Frame-Options: DENY` افتراضياً؛ إن أردت عرضها داخل إطار في لوحة الأم،
فضبط `YD_PANEL_PREVIEW=1` يكون من إعداد لوحة الأم/الخادم فقط، لا من هذه الحزمة العامة،
لأن تفعيله يسمح بتضمين اللوحة في أي موقع.

## 5. جعل التطبيق يتواصل مع المجلد

في التطبيق: شاشة الحساب ← «عنوان API للخادم» ← ضع `api_base` أعلاه. هذا يكفي للأجهزة
المثبتة بلا إصدار جديد. إن أردت أن يكون الرابط الافتراضي في كل بناء، غيّر
`config/environments.json` → `apiBaseUrl` إلى `api_base`، ثم شغّل من جذر المستودع:

```bash
python3 server/tools/build_panel_data.py
python3 server/tools/build_mother_panel_zip.py
```

ثم أعد بناء APK إن أردت تغيير الافتراضي داخل التطبيق. لا يُنشأ وسم/إصدار جديد في هذه الخطوة.

## 6. اللوحة التابعة لا تدخل هذه الحزمة

`server/public/boards/y-dimensions/` بـ`MAP.json` و25 ملفاً تُثبّت وحدها داخل
`{mother_panel.boards_dir}/y-dimensions/` عبر `mother_panel.install.map` ثم
`mother_panel.verify.run`. لا تُنسخ داخل هذه الحزمة ولا داخل `public/`.

## 7. التحقق

- `GET app_public_url/` → JSON فيه `ok=true`.
- `GET app_public_url/api/content.php?action=manifest` → JSON لا HTML.
- `GET panel_url?p=learning` → تبويبات اللوحة ومنها مراجعة الخطط.
- بطاقة `y-dimensions` في لوحة الأم تفتح `panel_url`.
- من التطبيق: إنشاء حساب/تسجيل دخول على `api_base` يرجع JSON.

إن استُخرجت الحزمة في `public_html/` بالغلط، احذف الملفات التي أنشأتها داخل الجذر فقط،
ولا تحذف `config.php` أو `index.php` أو مجلدات الموقع/لوحة الأم الأصلية.

الشرح الكامل: `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com (نهاية الملف) -->
"""


def version_info():
    text = (ROOT / "app/build.gradle.kts").read_text(encoding="utf-8")
    name = re.search(r'versionName\s*=\s*"([^"]+)"', text)
    code = re.search(r"versionCode\s*=\s*(\d+)", text)
    return (name.group(1) if name else ""), int(code.group(1)) if code else 0


def manifest():
    name, code = version_info()
    return json.dumps({
        "__developer_signature__": SIGN_START,
        "schema": "mother-panel.app-package/1.0",
        "app_id": "y-dimensions",
        "app_name": "Y- Dimensions",
        "app_type": "mobile",
        "version": name,
        "version_code": code,
        "developer": "يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com",
        "package_root": ".",
        "public_root": "public",
        "panel_path": "public/panel/",
        "api_base_path": "public/",
        "api_shim_path": "public/panel/",
        "config_template": "config/config.example.php",
        "config_target": "config/config.php",
        "private_dirs": ["src", "config", "migrations"],
        "excluded_from_public": ["public/admin/", "public/boards/"],
        "database_policy": "one_database_unified_users",
        "migration_warning": "001_init.sql creates users; do not run as-is on a unified Mother Panel database",
        "frame_policy": "open_in_new_tab_by_default",
        "returned_values": ["app_public_url", "panel_url", "api_base"],
        "child_board_installer": "mother_panel.install.map + mother_panel.verify.run",
        "child_board_target": "{mother_panel.boards_dir}/y-dimensions/",
        "repository_path": "docs/mother-panel/y-dimensions-mother-panel.zip",
        "release_asset_policy": "attached automatically to every v* release by .github/workflows/release.yml",
        "release_asset_name": "y-dimensions-mother-panel.zip",
        "__developer_signature_end__": SIGN_END,
    }, ensure_ascii=False, indent=2) + "\n"


def add_bytes(z, arcname, data, mode=0o644):
    info = zipfile.ZipInfo(str(PurePosixPath(arcname)), date_time=(2026, 11, 25, 0, 0, 0))
    info.compress_type = zipfile.ZIP_DEFLATED
    info.external_attr = (mode & 0xFFFF) << 16
    z.writestr(info, data)


def add_text(z, arcname, text, mode=0o644):
    add_bytes(z, arcname, text.encode("utf-8"), mode)


def copy_tree(z, src, prefix, skip=None):
    skip = skip or set()
    for path in sorted(src.rglob("*")):
        if not path.is_file():
            continue
        rel = path.relative_to(src).as_posix()
        if rel in skip or any(rel.startswith(s) for s in skip):
            continue
        add_bytes(z, str(PurePosixPath(prefix) / rel), path.read_bytes())


def main():
    OUT.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(OUT, "w", compression=zipfile.ZIP_DEFLATED) as z:
        add_text(z, ".htaccess", ROOT_HTACCESS)
        add_text(z, "INSTALL.md", INSTALL_MD)
        add_text(z, "mother-panel.json", manifest())
        add_text(z, "public/.htaccess", PUBLIC_HTACCESS)
        add_text(z, "public/panel/settings.example.php", SETTINGS_EXAMPLE)
        copy_tree(z, ROOT / "server/config", "config", skip={".gitkeep", "config.php"})
        copy_tree(z, ROOT / "server/src", "src", skip={".gitkeep"})
        copy_tree(z, ROOT / "server/migrations", "migrations", skip={".gitkeep"})
        copy_tree(
            z,
            ROOT / "server/public",
            "public",
            skip={"admin/", "boards/", ".gitkeep", "panel/settings.php"},
        )
    print(f"built {OUT} ({OUT.stat().st_size} bytes)")


if __name__ == "__main__":
    main()
# توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
