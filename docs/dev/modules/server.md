# الخادم والمجتمع

## البنية

- `server/src/bootstrap.php` يحمّل `config/config.php` ويرد JSON.
- `server/src/db.php` اتصال PDO MySQL.
- `server/src/auth.php` تسجيل ودخول ورموز Bearer مخزنة كـSHA-256.
- `server/src/sync_rows.php` إدخال/تحديث صفوف العميل.
- `server/public/api/*.php` نقاط النهاية.
- `server/migrations/001_init.sql` المخطط الكامل.

## النقاط

| المسار | الوظيفة |
|---|---|
| `/` | فحص صحة |
| `/api/auth.php?action=register\|login\|logout\|forgot` | الحساب |
| `/api/sync.php` | مزامنة النقاط والملف والمذكرات والمال والكتب والمحافظ |
| `/api/forum.php?action=threads\|posts\|new_thread\|new_post` | المنتدى |
| `/api/shop.php?action=list\|buy` | المتجر |
| `/api/raids.php?action=info\|checkin` | الغارات |
| `/api/vault.php` | خزنة مشفّرة من الجهاز |

## التطبيق

- `ForumScreen` و`ShopScreen` و`RaidsScreen` تعمل فقط بعد تسجيل الدخول من تبويب المزامنة.
- المتجر يخصم من `users.points` على الخادم، والرصيد المحلي لا يُخصم.
- الغارة تزيد تقدماً واحداً كل `raid_cooldown_seconds`؛ القيمة في إعداد الخادم.
- الخزنة تحفظ حمولة مشفّرة كما هي؛ لا يملك الخادم مفتاحها.

## عوائق

- لا بريد لإرسال استعادة كلمة المرور؛ `forgot` يرد بنجاح عام فقط.
- لا لوحة تحكم ويب بعد.
- `main` لم يُزامَن مع `dev` في هذه الجلسة.
