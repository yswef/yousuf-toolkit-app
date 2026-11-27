<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
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

## ماذا ترفع؟ — عند وجود لوحة أم/موقع آخر في `public_html/`

**لا ترفع محتويات `server/public/` كاملة إلى جذر `public_html/`.** جذر الدومين فيه
`config.php`, `index.php`, `admin/`, `core/`… تخص الموقع/لوحة الأم وتبقى كما هي.
اللوحة التنفيذية تُرفع كملف ZIP واحد داخل المجلد الذي تخصصه لوحة الأم:

```bash
python3 server/tools/build_mother_panel_zip.py
```

```
<allocated-folder>/            ← المجلد الذي تُرجعه لوحة الأم، وليس public_html/
├── .htaccess + INSTALL.md + mother-panel.json
├── config/                    ← config.example.php، وأنشئ config.php هنا فقط
├── src/ migrations/           ← محميان بـ .htaccess
└── public/
    ├── index.php              ← فحص صحة JSON
    ├── api/                   ← نقاط API الحقيقية
    ├── content/VERSION, learn/
    └── panel/                 ← هذه اللوحة: index.php + api/ + assets/ + data/
```

`public/admin/` و`public/boards/` مستبعدان من الحزمة عمداً: الأول حتى لا تصير لوحة إدارة ثانية،
والثاني لأنه يُثبّت وحده داخل `{mother_panel.boards_dir}` عبر `MAP.json`.

الدخول: `panel_url` الذي تُرجعه لوحة الأم، مثالاً
`https://<domain>/<allocated-path>/public/panel/`، برمز `admin_token` من
`config/config.php` داخل المجلد المخصص. إن لم يكن مضبوطاً تفتح اللوحة في
**وضع القراءة فقط** بالبيانات المحلية، ويُعطَّل تنفيذ الأوامر حتى يُضبط الإعداد.

> للدومين الفارغ فقط: `server/HOSTINGER.md` يشرح نشر `server/public/` في `public_html/`.

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

الافتراضي في APK يأتي من `config/environments.json`؛ وبعد أن تُرجع لوحة الأم
`api_base` ضعه في شاشة الحساب ← «عنوان API للخادم»، مثالاً
`https://<domain>/<allocated-path>/public` (أو `.../public/panel` لأن `panel/api/*.php`
تحوّل الطلبات إلى `api/*.php`). التطبيق ينادي `POST {api_base}/api/auth.php?action=register`.

**شرط التسجيل السحابي:** أن تكون `api/auth.php` موجودة فعلاً تحت `public/`، و`config/config.php`
مضبوطاً داخل المجلد المخصص، وقاعدة البيانات موحّدة بلا `users` مكرر. إن جاء الرد صفحة HTML،
فملفات الـ API غير مرفوعة أو المسار خاطئ. لا تستورد `001_init.sql` كما هو على قاعدة لوحة الأم.

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

- القاعدة واحدة و`users` موحّد؛ لا جداول مكررة. `001_init.sql` ينشئ `users` فلا يُستورد كما هو على قاعدة لوحة الأم.
- `sister_apps` فارغ حالياً؛ أضف البطاقات في `data/app.json` لتظهر في تبويب «التطبيقات» تلقائياً.
- اللوحة التابعة `boards/y-dimensions/` لم تُلمس: بصماتها و`VERIFY 13/13` سليمة.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions (نهاية الملف) -->


## جديد 0.18.0
- تبويبات: **المشرفون والمطورون** (تعيين مستخدمين + حتى 3 حسابات مشرف مع تبديل/خروج/دخول بحساب آخر)، **اختبارات المطور** (62 اختباراً يدوياً)، **بلاغات التشخيص**.
- صلاحية وضع المطور في التطبيق تأتي من `devmode.php?action=status` فقط.
- التحديثات الفرعية تضيف `devmode_tests.json` و`releases.json`.
- `panel/?p=learning` يراجع `learning_plans` بعد استيراد `006_learning.sql` وينشر `site.html`.

## الرفع إلى لوحة التحكم الأم

هذه **اللوحة التنفيذية** تُرفع كحزمة `build/y-dimensions-mother-panel.zip` داخل مجلد تخصصه
لوحة الأم، لا داخل `public_html/` ولا داخل `{mother_panel.boards_dir}`. لوحة الأم تُرجع
`app_public_url`, `panel_url`, `api_base`، و`mother_panel.apps.open_panel` يفتح `panel_url`
من بطاقة `y-dimensions` في تبويب جديد. أما التي تُثبّت داخل لوحة الأم فهي الحزمة التابعة
`server/public/boards/y-dimensions/` باستخدام `MAP.json` و`mother_panel.install.map`
ثم `mother_panel.verify.run` فقط.

الشرح الكامل: `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
