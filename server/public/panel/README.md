<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions (بداية الملف) -->
# اللوحة الأساسية — Y- Dimensions (موبايل)

هذه هي **اللوحة الأساسية للتطبيق**: تحتوي الكود التنفيذي، نقاط الـ API، تنفيذ الأوامر،
التطبيقات، الكتب السحرية، بقية الأقسام، وصورة التطبيق. ليست لوحة إعلانية تابعة.

## الجديد في 0.17.0
- تبويب **«متطلبات التطبيقات»** بقسمين: «مصادر الأشياء» (قسم مستقل لكل نوع أصل) و«متطلبات التطبيقات» مع الفرز (الناقص أولاً) والتأشير على البند عند توفيره.
- تبويبات: **التحديثات الفرعية**، **الأجهزة المسجّلة**، **الإصدارات**.
- **زر عائم** في كل تبويب: «الموقع الأساسي» و«لوحة التحكم الأساسية»؛ الصفحات المقفلة تعيد للوحة التحكم الأساسية.
- نقاط API جديدة: `devices.php`, `content.php`, `requirements.php` (ومحوّلاتها داخل `panel/api/`).
- التصحيح: الموقع **PHP** وليس WordPress. الجداول الجديدة غير المكررة مسموحة (`003_devices.sql`).
- ملفات `docs/mother-panel/`: `PROMPT.md` (برومت لوحة الأم)، `APP_PANEL_PROMPT.md` (برومت موديل لوحة التطبيق)، `MOTHER_PANEL_HANDOFF.md` (التسليم الإلزامي).

## ماذا ترفع؟

ارفع **محتويات `server/public/` كاملة** إلى `public_html/` (مجلد واحد فيه كل شيء):

```
public_html/
├── index.php            → فحص صحة + رابط panel/
├── api/                 → نقاط API الحقيقية (auth, sync, forum, shop, raids, vault, license, announcements)
├── admin/               → لوحة الإدارة التفصيلية (نفس الجلسة والرمز)
├── panel/               → هذه اللوحة الأساسية
│   ├── index.php        → الواجهة + تنفيذ الأوامر + التعبئة التلقائية
│   ├── api/*.php        → محوّلات: تسمح باستعمال /panel كعنوان API أيضاً
│   ├── assets/icon.svg  → أيقونة التطبيق
│   ├── assets/icon.png  → أيقونة launcher الحقيقية
│   └── data/*.json      → مصادر التعبئة التلقائية
└── boards/y-dimensions/ → اللوحة التابعة الإعلانية للوحة الأم (MAP.json + 13 فحصاً)
```

وارفع خارج `public_html` (في `domains/<site>/`):

```
src/          ← server/src
config/       ← server/config  + أنشئ config.php من config.example.php
migrations/   ← server/migrations  + استورد 001_init.sql ثم 002_admin.sql
```

الدخول: `https://النطاق/panel/` برمز `admin_token` من `config.php`.
إن لم يكن `config.php` مضبوطاً تفتح اللوحة في **وضع القراءة فقط** بالبيانات المحلية،
ويُعطَّل تنفيذ الأوامر حتى يُضبط الإعداد.

## ليش الحقول ما انسحبت تلقائي في اللوحة التابعة؟

`boards/y-dimensions/views/*.html` قوالب **إعلانية** تحوي رموزاً مثل
`{{mother_panel.stats.home.users_count}}`. هذه الرموز يحلّها مُصيّر لوحة الأم فقط؛
المجلد بحد ذاته بلا كود ولا استعلامات (هذا كان شرط اللوحة التابعة: الكود والتوكن في لوحة الأم وحدها).
لذلك عند رفعه على استضافة عادية تظهر القيم فارغة أو كنص الرمز نفسه.

هذه اللوحة الأساسية تحلّ المشكلة: **كل حقل يُقرأ ويُعرض من مصدره عند كل طلب**:

| المصدر | ما يُعبّأ منه |
|---|---|
| `data/app.json` | الاسم، المعرّف، النوع، الإصدار، المطوّر، البريد، الوسوم، الرُتب، الطاقة، النقاط، التطبيقات الأخرى |
| `data/environments.json` | عنوان الـ API |
| `data/books.json` | 24 كتاباً سحرياً في 5 أقسام (مستخرجة من `Books.kt` مع الترجمات العربية) |
| قاعدة البيانات | المستخدمون، الجلسات، التراخيص، المتجر، المنتدى، الكتب، المحافظ، الخزنة، السجل |

تبويب **«الحقول التلقائية»** يعرض كل حقل وقيمته ومصدره بالضبط. لا توجد قيم `{{...}}` هنا.

## الـ API والتطبيق

العنوان الافتراضي في التطبيق: `config/environments.json` → `https://yousef.romoozbasmah.com`.
التطبيق ينادي `POST {base}/api/auth.php?action=register`. إن أردت استعمال مجلد اللوحة كعنوان:
اضبط `https://النطاق/panel` في شاشة الحساب ← «عنوان API للخادم»؛ ملفات `panel/api/*.php`
تحوّل الطلب إلى `api/*.php` الحقيقية.

**شرط التسجيل السحابي:** أن تكون `api/auth.php` موجودة فعلاً على النطاق، و`config.php`
مضبوطاً، وجداول `001_init.sql` مستوردة. إن جاء الرد صفحة HTML، فملفات الـ API غير مرفوعة.

## تنفيذ الأوامر

تبويب «تنفيذ الأوامر» ينفّذ **قائمة مغلقة** فقط، بلا SQL حر ولا أوامر shell:

`health.check` · `stats.refresh` · `api.selftest` · `apps.reload` · `books.list` ·
`tokens.purge` · `user.logout` · `license.create` · `license.revoke` · `logs.tail`

كل تنفيذ محمي بـ CSRF ورمز الدخول، ويُسجَّل في `admin_log`. رمز الترخيص يطابق
`src/license.php` (`YD-XXXX-XXXX-XXXX-XXXX`).

## توليد البيانات تلقائياً

بعد أي تعديل على `config/app.json` أو `Books.kt` أو الإصدار، شغّل من جذر المستودع:

```bash
python3 server/tools/build_panel_data.py
```

يعيد توليد `panel/data/app.json` و`books.json` و`environments.json` وينسخ الأيقونتين.

## ملاحظات

- لا جداول جديدة: اللوحة تقرأ جداول `001_init.sql` و`002_admin.sql` فقط.
- `sister_apps` فارغ حالياً؛ أضف البطاقات في `data/app.json` لتظهر في تبويب «التطبيقات» تلقائياً.
- اللوحة التابعة `boards/y-dimensions/` لم تُلمس: بصماتها و`VERIFY 13/13` سليمة.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions (نهاية الملف) -->


## جديد 0.18.0
- تبويبات: **المشرفون والمطورون** (تعيين مستخدمين + حتى 3 حسابات مشرف مع تبديل/خروج/دخول بحساب آخر)، **اختبارات المطور** (62 اختباراً يدوياً)، **بلاغات التشخيص**.
- صلاحية وضع المطور في التطبيق تأتي من `devmode.php?action=status` فقط.
- التحديثات الفرعية تضيف `devmode_tests.json` و`releases.json`.
