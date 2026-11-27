<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# كيف أرفع لوحة التحكم إلى لوحة التحكم الأم؟

هذا الشرح يفرّق بين شيئين لا يُرفعان إلى نفس المكان:

| الملف/المجلد | ما هو؟ | يرفع إلى أين؟ | من ينفّذه؟ |
|---|---|---|---|
| `server/public/panel/` | **اللوحة الأساسية التنفيذية**، فيها PHP و18 تبويباً و`panel/api/*.php` | استضافة التطبيق: `public_html/panel/` | خادم PHP + MySQL + `config.php` |
| `server/public/boards/y-dimensions/` | **لوحة تابعة إعلانية** بلا كود ولا توكن، مع `MAP.json` و25 ملفاً | `{mother_panel.boards_dir}/y-dimensions/` داخل لوحة الأم | `mother_panel.install.map` ثم `mother_panel.verify.run` |
| `docs/mother-panel/MOTHER_PANEL_HANDOFF.md` | عقد التسليم الذي يقرأه موديل لوحة الأم | يُقرأ قبل إعلان اكتمال التثبيت | موديل لوحة الأم |

**لا تنسخ `server/public/panel/` داخل مجلد لوحات الأم.** هي لوحة تنفيذية مستقلة؛ لوحة الأم تفتحها أو تربطها، أما الملفات التي تُثبّت داخلها فهي حزمة `boards/y-dimensions/` فقط.

## 1. جهّز اللوحة التنفيذية على الاستضافة أولاً

1. بعد أي تعديل على `config/app.json` أو `Books.kt` أو الإصدار، شغّل من جذر المستودع:
   ```bash
   python3 server/tools/build_panel_data.py
   ```
2. ارفع **محتويات `server/public/` كاملة** إلى `public_html/`، فيصبح المسار `public_html/panel/index.php` موجوداً. لا ترفع `config.php` إلى Git.
3. ارفع `server/src/` و`server/config/` و`server/migrations/` خارج `public_html`، ثم أنشئ `config/config.php` من `config.example.php` بالقيم الحقيقية التي يعطيك المالك: `db`, `base_url`, `license_secret`, `admin_token`. **لا تخمّنها.**
4. استورد بالترتيب: `001_init.sql`, `002_admin.sql`, `003_devices.sql`, `004_devmode.sql`, `005_journal.sql`, `006_learning.sql`.
5. افتح `https://<domain>/` وتحقق أن فحص الصحة يعيد JSON، ثم افتح `https://<domain>/panel/` وتحقق أن التبويبات تظهر، ومنها `panel/?p=learning`.
6. تحقق أن `api/learning.php?action=ready` و`api/content.php?action=manifest` يعيدان JSON لا صفحة HTML.

ال domain و`admin_token` و`boards_dir` قيم تُطلب من مالك لوحة الأم/الاستضافة، ولا تُكتب في هذا الملف.

## 2. اربط اللوحة التنفيذية ببطاقة التطبيق في لوحة الأم

في لوحة الأم، سجّل بطاقة التطبيق بالقيم الموجودة في `server/public/panel/data/app.json` و`config/environments.json`:

- `app_id = y-dimensions`، الاسم `Y- Dimensions`، النوع `mobile`، الإصدار `0.19.0`، الأيقونة `panel/assets/icon.svg`.
- عنوان الـ API من `api_base`، وعنوان فتح اللوحة `https://<domain>/panel/`.
- نقطة `mother_panel.apps.open_panel` تفتح هذا العنوان داخل لوحة الأم أو في صفحة/تبويب، حسب ما تتيحه لوحة الأم. إن استُخدم إطار، فإعداد السماح بالعرض يكون من لوحة الأم لا من ملفات اللوحة التابعة.
- نقاط `mother_panel.apps.api`, `mother_panel.devices.*`, `mother_panel.releases.*`, `mother_panel.content.*`, `mother_panel.requirements.*` تُسجّل كما في `MOTHER_PANEL_HANDOFF.md`، وكلها مقيّدة بـ `app_id`, وتُسجّل كتاباتها في `audit_log` و`sync_log` بـ `request_id`.
- **لوحة الأم وحدها تملك الكود والتوكن.** لا تضع توكن الدخول ولا `config.php` داخل `boards/y-dimensions/`.

## 3. ارفع/ثبّت اللوحة التابعة داخل لوحة الأم

الحزمة هي `server/public/boards/y-dimensions/` كما هي، بـ25 ملفاً. لا تعدّل أي ملف داخلها يدوياً؛ `MAP.json` يحسب بصماتها وترتيبها.

1. اسأل مالك لوحة الأم عن `{mother_panel.boards_dir}` وعن طريقة الاستيراد المتاحة (رفع مجلد، أو ملف ZIP، أو استدعاء `mother_panel.install.map`). لا تخترع المسار ولا رمز التثبيت.
2. إن استخدمت ZIP، اجعل جذره يحتوي ملفات `app.json`, `board.json`, `MAP.json`, `sections/`, `views/`, `docs/`, `assets/` مباشرة، بلا مجلد إضافي فوقها.
3. نفّذ `mother_panel.install.map` على `MAP.json`: يقرأ `install.order` بالترتيب 1…25 وينسخ كل ملف إلى `{mother_panel.boards_dir}/y-dimensions/<path>`.
4. بعد النسخ نفّذ إلزامياً `mother_panel.verify.run`: 13 فحصاً، و25/25 ملفاً، وبصمات مطابقة عدا الملفات المعفاة صراحةً، وصحة JSON، وتوقيع المطوّر، وغياب DDL/جدول `users` محلي/كود مستقل/رمز.
5. اقرأ تقرير `docs/VERIFY.md` الذي يكتبه التحقق. عند أي فشل: `abort_and_rollback`، ثم أعد نسخ الحزمة الأصلية كما هي؛ لا تصلح بصمة بتعديل ملف.
6. لا ترفع `boards/y-dimensions/` وحدها إلى استضافة عادية وتنتظر أن تُملأ الحقول: الرموز `{{mother_panel.*}}` يحلّها مُصيّر لوحة الأم فقط.

## 4. قائمة تحقق قبل إعلان الاكتمال

- `public_html/panel/index.php` يعمل، و`panel/?p=learning` يراجع خطط التعلم.
- `server/config/config.php` موجود على الخادم فقط، وليس في Git، وقيمه من المالك.
- الترحيلات الستة مستوردة، و`users` موحّد ولم يتكرر.
- نقاط API تعيد JSON، ولا تظهر صفحة HTML بدلاً منها.
- بطاقة `y-dimensions` في لوحة الأم تفتح `/panel/` وتعرض الإصدار `0.19.0`.
- `mother_panel.install.map` أنهى 25 خطوة بالترتيب، و`mother_panel.verify.run` كتب `VERIFY.md` بنجاح.
- لم يتغير أي ملف داخل `server/public/boards/y-dimensions/`.
- لم يُكتب توكن ولا كلمة مرور ولا مسار قاعدة بيانات داخل الحزمة التابعة.

الشرح التنفيذي الكامل للنشر على الاستضافة: `server/HOSTINGER.md`. عقد لوحة الأم الإلزامي: `docs/mother-panel/MOTHER_PANEL_HANDOFF.md`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
