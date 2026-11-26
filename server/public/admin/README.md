<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# admin — لوحة التحكم

مجلد واحد مستقل: `index.php` هو اللوحة كلها. لا ملفات أخرى مطلوبة داخل الواجهة.

## الرفع
- ارفع المجلد كاملاً إلى `public_html/admin/` (أو بجانب `api/` في `server/public/`).
- يجد اللوحة `config/config.php` و`src/license.php` على بعد مستويين من المجلد (جذر الخادم).
  إن اختلف الترتيب: انسخ `settings.example.php` إلى `settings.php` وحدد `server_root`، أو اضبط المتغير `YD_SERVER_ROOT`.
- الدخول: `https://النطاق/admin/` بالرمز `admin_token` من `config.php` (12 حرفاً على الأقل).
- نصيحة: غيّر اسم المجلد إلى اسم غير مألوف، وقيّد الوصول بعنوان IP في `.htaccess` إن أمكن.

## الدمج مع لوحة أخرى
- انسخ المجلد كما هو داخل اللوحة الأخرى، أو اربط `index.php` من قائمتها.
- اسم الجلسة `yd_admin` وملف قفل المحاولات يحملان بادئة `yd_admin_`، فلا يتعارضان مع جلسة اللوحة الأخرى.
- كل الأنماط والسكربتات داخل الصفحة نفسها (CSP لا يسمح بملفات خارجية)، فلا حاجة لنقل CSS أو JS.
- الجداول التي تنشئها اللوحة عند الحاجة لا تمس جداول اللوحة الأخرى.

## الأقسام
نظرة عامة، سرعة وصحة، مستخدمون، تراخيص (توليد كود)، متجر، حملات/غارات، إعلانات، منتدى، سجل الإجراءات.
