# server

خادم PHP 8.1+ لقاعدة MySQL 8، بواجهة JSON تحت `/api`.

## التهيئة

1. انسخ `config/config.example.php` إلى `config/config.php` واملأ `db` و`base_url`.
2. `allowed_origins` لقائمة أصول الويب المسموح لها، واتركها فارغة لتطبيق أندرويد.
3. استورد `migrations/001_init.sql` في قاعدة البيانات.
4. اجعل جذر النطاق `server/public`، لا `server/`؛ مجلدات الإعداد والمصدر محمية بـ `.htaccess`.
5. أضف عناصر المتجر ومواسم الغارات من SQL؛ لا توجد بيانات تجريبية مبطّنة.

## نقاط النهاية

- `GET /` فحص صحة.
- `POST /api/auth.php?action=register|login|logout|forgot`.
- `POST /api/sync.php` مع `Authorization: Bearer <token>`.
- `GET|POST /api/forum.php?action=threads|posts|new_thread|new_post`.
- `GET|POST /api/shop.php?action=list|buy`.
- `GET|POST /api/raids.php?action=info|checkin`.
- `GET|POST|DELETE /api/vault.php`.
- `GET|POST /api/license.php`.
- `GET|POST /admin.php` لوحة إدارة محمية بـ `admin_token`.

نشر هوستنجر: `HOSTINGER.md`.

الحقول الزمنية كلها Unix UTC بالثواني. الخزنة تستقبل حمولة مشفّرة من الجهاز كما هي ولا يفكّه الخادم.
