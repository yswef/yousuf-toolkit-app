<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# النشر على هوستنجر

النطاق: `https://yousef.romoozbasmah.com`

> **حالتك الحالية — مهم:** `public_html/` ليس فارغاً؛ فيه `config.php`، `config-sample.php`،
> `index.php`، `admin/`، `app/`، `assets/`، `core/`، `data/`، `docs/`، `lang/`،
> `cron_backup.php`، `cron_sync.php`، `cron_update.php`، `manifest.webmanifest`،
> `status.php`، `update_start.php`، `update_step.php`، و`.htaccess`. هذه ملفات لوحة الأم/الموقع
> الآخر وتبقى كما هي. **لا ترفع `server/public/` فوقها ولا تستبدلها.** انتقل مباشرة إلى القسم التالي.

## الرفع داخل لوحة أم موجودة — المسار الصحيح الآن

1. من جذر المستودع: `python3 server/tools/build_mother_panel_zip.py` →
   `build/y-dimensions-mother-panel.zip` (مجلد `build/` مستثنى من Git).
2. إن كان في لوحة الأم زر «رفع حزمة تطبيق / Install from ZIP»، ارفع الملف هناك واتركها تخصص
   المجلد وتُرجع الرابط. وإلا: من File Manager أنشئ مجلداً جديداً مثل
   `public_html/apps/y-dimensions/`، ارفع الـ ZIP **داخله** ثم `Extract` داخله فقط.
   **لا تختر `public_html/` كوجهة ولا تترك مجلداً إضافياً فوق الحزمة.**
3. داخل المجلد المخصص يصبح:
   `INSTALL.md`, `mother-panel.json`, `.htaccess`, `config/`, `src/`, `migrations/`,
   و`public/` وفيه `index.php`, `api/`, `content/`, `learn/`, `panel/`.
   `public/admin/` و`public/boards/` مستبعدان عمداً: لا لوحة إدارة ثانية، ولا خلط باللوحة التابعة.
4. انسخ `.../config/config.example.php` إلى `config.php` **في نفس المجلد المخصص**، واملأ
   `db`, `base_url`, `license_secret`, `admin_token`, `allowed_origins` من مالك لوحة الأم/الاستضافة.
   `base_url = https://<domain>/<allocated-path>/public`. لا تلمس `public_html/config.php`
   ولا `config-sample.php`، ولا ترفع `config.php` إلى Git.
5. **قاعدة واحدة و`users` موحّد** كما في `docs/dev/RULES.md` و`MOTHER_PANEL_HANDOFF.md`.
   ملف `001_init.sql` ينشئ `users`؛ لا تستورده كما هو على قاعدة لوحة الأم. نفّذ عقد التسليم
   (توحيد الجداول) ثم استورد الترحيلات غير المكررة `002_admin.sql`…`006_learning.sql`.
   إلى أن يتم ذلك تعمل اللوحة في **وضع القراءة فقط**.
6. لوحة الأم تُرجع `app_public_url`، `panel_url`، `api_base`، وتسجّل
   `mother_panel.apps.open_panel` → `panel_url` في **تبويب/صفحة جديدة**. اللوحة ترسل
   `X-Frame-Options: DENY`؛ الإطار يحتاج ضبط `YD_PANEL_PREVIEW=1` من لوحة الأم فقط.
7. التطبيق: شاشة الحساب ← «عنوان API للخادم» ← ضع `api_base`
   (`https://<domain>/<allocated-path>/public` أو `.../public/panel`). لتغيير الافتراضي في
   كل بناء: `config/environments.json` → `apiBaseUrl`، ثم `python3 server/tools/build_panel_data.py`.
8. اللوحة التابعة `server/public/boards/y-dimensions/` تُثبّت وحدها داخل
   `{mother_panel.boards_dir}/y-dimensions/` عبر `MAP.json` و`mother_panel.install.map`
   (25 خطوة) ثم `mother_panel.verify.run` (13 فحصاً و`VERIFY.md`)، لا داخل حزمة ZIP هذه.

التحقق: `GET app_public_url/` يرجع JSON `ok=true`، و`api/content.php?action=manifest` يرجع
JSON لا HTML، و`panel_url?p=learning` يفتح التبويبات، وبطاقة `y-dimensions` تفتح `panel_url`.
الشرح الكامل: `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md`، وداخل الحزمة `INSTALL.md`.

## دومين فارغ فقط — النشر القديم

هذا القسم **لا يُستخدم على دومينك الحالي**؛ يصلح فقط لو كان `public_html/` جديداً بلا أي موقع:

1. من hPanel فعّل PHP 8.1+ وأنشئ قاعدة MySQL ومستخدماً لها.
2. ارفع محتويات `server/public/` كاملة إلى `public_html/`، و`server/src/` و`server/config/`
   و`server/migrations/` خارجه في `domains/<site>/`.
3. انسخ `config/config.example.php` إلى `config.php` واملأ القيم، ثم استورد
   `001_init.sql`…`006_learning.sql` بالترتيب، وفعّل SSL.
4. افتح `https://<domain>/` لفحص الصحة، و`https://<domain>/panel/` للوحة، و`.../admin/`
   للوحة التفصيلية، ثم أضف `items` و`raid_seasons` من phpMyAdmin.
5. لتوليد كود ترخيص: `php tools/code.php <user_id>` بعد ضبط `license_secret`.

قبل أي رفع، وبعد تعديل `config/app.json` أو `Books.kt` أو الإصدار:
`python3 server/tools/build_panel_data.py` يعيد توليد بيانات اللوحة (24 كتاباً، بيانات التطبيق،
الأيقونتان). شرح اللوحة: `server/public/panel/README.md`.

نقاط API بعد الرفع: `api/devices.php`، `api/content.php`، `api/requirements.php`،
`api/devmode.php`، `api/diagnostics.php`، `api/learning.php`، مع محوّلاتها في `panel/api/`.
`004_devmode.sql` يضيف `developer_accounts`, `panel_admins`, `diagnostic_reports`,
`diagnostic_solutions`؛ `006_learning.sql` يضيف `learning_plans` وينشر `site.html` من
`panel/?p=learning`. لا ترفع `config.php` إلى Git.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
