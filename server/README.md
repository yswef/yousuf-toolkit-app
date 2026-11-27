<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# server

خادم PHP 8.1+ لقاعدة MySQL 8، بواجهة JSON تحت `/api`.

## التهيئة

1. انسخ `config/config.example.php` إلى `config/config.php` واملأ `db` و`base_url`.
2. `allowed_origins` لقائمة أصول الويب المسموح لها، واتركها فارغة لتطبيق أندرويد.
3. استورد `migrations/001_init.sql` ثم `002_admin.sql` ثم `003_devices.sql` ثم `004_devmode.sql` ثم `005_journal.sql` ثم `006_learning.sql` في قاعدة البيانات.
4. اجعل جذر النطاق `server/public`، لا `server/`؛ مجلدات الإعداد والمصدر محمية بـ `.htaccess`. المجلد الواحد يضم `api/` و`admin/` و`panel/` و`boards/`؛ **اللوحة الأساسية التنفيذية هي `panel/`** (الواجهة، تنفيذ الأوامر، التطبيقات، الكتب، الأيقونة، التعبئة التلقائية للحقول).
5. أضف عناصر المتجر ومواسم الغارات من SQL؛ لا توجد بيانات تجريبية مبطّنة.

## نقاط النهاية

- `GET /` فحص صحة (يعيد أيضاً `panel/`).
- `GET /panel/` اللوحة الأساسية: الرئيسية، التطبيقات، الكتب، الأدوات والجداول، المستخدمون، التراخيص، الآراء، تنفيذ الأوامر، الحقول التلقائية، الـ API. نفس `admin_token` و`yd_admin`.
- `GET /panel/api/*.php` محوّلات إلى نقاط `/api/*.php` الحقيقية، ليصلح `/panel` كعنوان API للتطبيق.
- `POST /api/auth.php?action=register|login|logout|forgot`.
- `POST /api/sync.php` مع `Authorization: Bearer <token>`.
- `GET|POST /api/forum.php?action=threads|posts|new_thread|new_post`.
- `GET|POST /api/shop.php?action=list|buy`.
- `GET|POST /api/raids.php?action=info|checkin`.
- `GET|POST|DELETE /api/vault.php`.
- `GET|POST /api/license.php`.
- `GET|POST /admin/` لوحة إدارة محمية بـ `admin_token`، وكلها في مجلد واحد `public/admin/`.
- `GET /panel/` اللوحة الأساسية التنفيذية: متطلبات التطبيقات، التحديثات الفرعية، الأجهزة، الإصدارات، الأوامر، تبويب الـ API، مع زر عائم.
- `GET|POST /api/devices.php` الأجهزة المسجّلة وتسجيل الخروج عن بُعد.
- `GET /api/content.php?action=manifest` بيان التحديثات الفرعية (بيانات فقط بلا APK) و`action=file` لتنزيل ملف متغيّر.
- `GET|POST /api/requirements.php` متطلبات التطبيقات ومصادر الأصول مع الفرز والتأشير.

نشر هوستنجر: `HOSTINGER.md`. شرح اللوحة الأساسية ولماذا تُملأ الحقول تلقائياً: `public/panel/README.md`. توليد بياناتها: `python3 tools/build_panel_data.py`.

الحقول الزمنية كلها Unix UTC بالثواني. الخزنة تستقبل حمولة مشفّرة من الجهاز كما هي ولا يفكّه الخادم.

- `GET /api/devmode.php?action=status` صلاحية المطور وكتالوج الاختبارات (توكن المستخدم).
- `GET|POST /api/learning.php` رفع خطة تعليمية ومراجعتها ونشر الموقع المعتمد، مع `panel/?p=learning`.
- `GET|POST /api/diagnostics.php` مطابقة حل معروف / استقبال بلاغ تشخيص مجهّل / سرد البلاغات للوحة.
- `panel/?p=developers` تعيين المطورين وحسابات المشرفين (حد أقصى 3)، `p=devtests` كتالوج الاختبارات، `p=diagnostics` البلاغات والحلول.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
