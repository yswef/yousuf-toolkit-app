<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# النشر على هوستنجر

النطاق: `https://yousef.romoozbasmah.com`

1. من hPanel فعّل PHP 8.1 أو أحدث، وأنشئ قاعدة MySQL ومستخدماً لها.
2. ارفع محتويات `server/public/` **كاملة** إلى `public_html/` حتى تصبح `public_html/api/auth.php` و`public_html/admin/index.php` و`public_html/panel/index.php`. المجلد الواحد يحتوي الـ API واللوحة الأساسية واللوحة التابعة؛ **اللوحة الأساسية هي `panel/`** (واجهة + أوامر + تطبيقات + كتب + صورة التطبيق). لا ترفع `boards/y-dimensions/` وحدها وتنتظر أن تعمل الحقول: تلك قوالب إعلانية تحلّها لوحة الأم فقط.
3. ارفع `server/src/` و`server/config/` و`server/migrations/` إلى مجلد النطاق خارج `public_html`:
   `domains/yousef.romoozbasmah.com/src` و`config` و`migrations`.
4. انسخ `config/config.example.php` إلى `config/config.php` واملأ `db` و`base_url` و`license_secret`.
5. استورد `migrations/001_init.sql` ثم `002_admin.sql` ثم `003_devices.sql` ثم `004_devmode.sql` ثم `005_journal.sql` ثم `006_learning.sql` من phpMyAdmin (جداول جديدة غير مكررة).
6. فعّل SSL المجاني، ثم افتح `https://yousef.romoozbasmah.com/` وتحقق من `ok=true`، وافتح `https://yousef.romoozbasmah.com/panel/` للوحة الأساسية، و`.../admin/` للوحة التفصيلية (نفس `admin_token`).
7. أضف عناصر `items` ومواسم `raid_seasons` من phpMyAdmin.
8. لتوليد كود ترخيص: `php tools/code.php <user_id>` بعد وضع `license_secret` في `config.php`.

نقاط جديدة بعد الرفع: `api/devices.php` (الأجهزة)، `api/content.php` (التحديثات الفرعية)، `api/requirements.php` (متطلبات التطبيقات)، و`panel/` فيه تبويب «متطلبات التطبيقات» + زر عائم للعودة. التحديثات الفرعية تُسحب تلقائياً عند تشغيل التطبيق، والإصدار الكامل يُنزّل ويُثبّت من داخل التطبيق.

لا ترفع `config.php` إلى git. التطبيق يقرأ العنوان من `config/environments.json` وهو مضبوط على هذا النطاق؛ ويمكن ضبطه من شاشة الحساب ← «عنوان API للخادم»، أو استعمال `https://النطاق/panel` لأن `panel/api/*.php` تحوّل الطلبات إلى `api/*.php`.

قبل الرفع، بعد أي تعديل على `config/app.json` أو `Books.kt` أو الإصدار: `python3 server/tools/build_panel_data.py` يعيد توليد بيانات اللوحة الأساسية (24 كتاباً، بيانات التطبيق، الأيقونتان). شرح اللوحة: `public/panel/README.md`.


## جديد 0.18.0
- استيراد `006_learning.sql` يضيف `learning_plans`، و`api/learning.php` يرفع الخطة وينشرها بعد مراجعة اللوحة.
- استيراد `004_devmode.sql` يضيف جداول غير مكررة: `developer_accounts`, `panel_admins`, `diagnostic_reports`, `diagnostic_solutions`.
- من تبويب «المشرفون والمطورون» عيّن مستخدمي التطبيق كمطورين، وأنشئ حتى 3 حسابات مشرف للوحة مع تبديلها وتسجيل الخروج.
- نقاط جديدة: `api/devmode.php`, `api/diagnostics.php` (ومحوّلاتها في `panel/api/`). التحديثات الفرعية تشمل الآن `devmode_tests.json` و`releases.json`.

## الربط بلوحة التحكم الأم

بعد نشر `panel/` على الاستضافة، اربطها من لوحة الأم عبر `mother_panel.apps.open_panel`؛ ولا ترفع `panel/` داخل مجلد لوحات الأم. لتثبيت بطاقة/قوالب التطبيق داخل لوحة الأم استخدم `server/public/boards/y-dimensions/MAP.json` و`mother_panel.install.map` فقط. الشرح الكامل: `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
