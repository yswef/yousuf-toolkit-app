<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# MOTHER_PANEL_HANDOFF.md — من لوحة تحكم Y- Dimensions إلى موديل لوحة التحكم الأم

> هذا هو الملف الإلزامي الذي يقرأه موديل لوحة الأم قبل إعلان اكتمال تثبيت التطبيق.
> التطبيق: `Y- Dimensions` · موبايل · يوسف الحمزي · `yswefalhmzy@gmail.com` · الإصدار `0.19.0`.
> الموقع: PHP على Hostinger (ليس WordPress). مبدأ أوفلاين ملغى.

## 1. تصحيحات على البرومت التأسيسي
- احذف قاعدة «لا جداول جديدة»: المقصود كان **عدم تكرار** الجداول الموجودة وعدم إنشاء
  `users` لكل تطبيق. الجداول الجديدة غير المكررة مسموحة ومطلوبة أدناه.
- احذف أي ذكر لـ WordPress؛ الموقع PHP.
- احذف مبدأ «أوفلاين أولاً»: تسجيل الدخول إلزامي قبل تشغيل التطبيق، والمزامنة بين الأجهزة مطلوبة.
- اجعل لوحة كل تطبيق API فعلياً (قراءة/كتابة/أوامر/إصدارات)، لا عرضاً إعلانياً فقط.

## 2. نقاط API المطلوب إضافتها في لوحة الأم
| النقطة | الطريقة | الغرض | الجداول |
|---|---|---|---|
| `mother_panel.apps.open_panel` | GET | فتح لوحة التطبيق بكل تبويباتها داخل لوحة الأم | apps, sync_log |
| `mother_panel.apps.allocate_dir` | POST | تخصيص مجلد فريد داخل `{mother_panel.apps_dir}` وإرجاع `app_public_url`, `panel_url`, `api_base` | apps_dir, apps |
| `mother_panel.apps.install_zip` | POST | استخراج حزمة اللوحة التنفيذية ZIP داخل المجلد المخصص | apps, sync_log, audit_log |
| `mother_panel.apps.api` | GET/POST | تمرير أوامر التحكم بالتطبيق (بيانات، إعدادات، محتوى) | sync_log, audit_log |
| `mother_panel.devices.list` | GET | الأجهزة المسجّلة للمستخدم مع آخر ظهور | devices, tokens |
| `mother_panel.devices.revoke` | POST | تسجيل الخروج عن بُعد من جهاز | devices, tokens, audit_log |
| `mother_panel.releases.list` | GET | الإصدارات مع الوصف والتاريخ والملف | releases |
| `mother_panel.releases.publish` | POST | نشر إصدار كامل (APK) | releases, app_files |
| `mother_panel.content.manifest` | GET | بيان التحديثات الفرعية (ملفات + sha256 + إصدار) | content_packs |
| `mother_panel.content.file` | GET | تنزيل ملف بيانات متغيّر فقط | content_packs, app_files |
| `mother_panel.requirements.list` | GET | متطلبات التطبيقات مع المصادر والحالة والفرز | app_requirements, asset_sources |
| `mother_panel.requirements.mark` | POST | التأشير على بند تم توفيره | app_requirements, audit_log |

كل استعلام مقيّد بـ `app_id = 'y-dimensions'`، وكل كتابة تُسجّل في `audit_log` و`sync_log`
بـ `request_id` فريد لمنع التكرار. الاستجابات JSON فقط.

## 3. الجداول الجديدة غير المكررة (مسموحة صراحةً من المطوّر)
- `users` — **موجود وموحّد**، لا يُنشأ ولا يتكرر؛ الفصل بين التطبيقات بـ `app_id`،
  ونفس البريد مسموح بحساب منفصل لكل تطبيق، بلا انتشار تلقائي.
- `devices(id, app_id, user_id, token_hash, label, model, last_seen BIGINT, created_at BIGINT, revoked TINYINT)`
  فريد `(app_id, token_hash)`.
- `content_packs(id, app_id, version, file_path, sha256, size_bytes, active, created_at BIGINT)`
  فريد `(app_id, version, file_path)`.
- `app_requirements(id, app_id, category, item, source, provided, priority, note, created_at BIGINT)`.
- `asset_sources(id, app_id, kind, name, path_or_url, license, status, created_at BIGINT)`.
- `releases(id, app_id, version, version_code, apk_path, notes, min_supported, active, published_at BIGINT)`.

لا يوجد `users_per_app` ولا أي جدول مكرر من الجداول الـ24 الموجودة.

## 4. الصلاحيات والأدوار
- لوحة الأم وحدها تملك الكود والتوكن؛ اللوحة التابعة بلا كود مستقل ولا توكن.
- `admin` يملك كل النقاط؛ `user` يملك `devices.list/revoke` على أجهزته فقط.
- الجلسات تُحفظ في `tokens` و`devices`؛ تسجيل الخروج عن بُعد يحذف التوكن ويُعلّم الجهاز `revoked=1`.

## 5. متطلبات التطبيق (تُسجّل في تبويب «متطلبات التطبيقات»)
| التطبيق | البند | المصدر | الحالة | الأولوية | ملاحظة |
|---|---|---|---|---|---|
| Y- Dimensions | أيقونة التطبيق | `panel/assets/icon.svg` + `icon.png` | متوفر | 1 | موجودة داخل المستودع |
| Y- Dimensions | صوت introductory / rank | `res/raw/nour_intro.mp3`, `nour_rank.mp3` | متوفر | 2 | مطلوب فحص الترخيص |
| Y- Dimensions | صورة `nour.webp` | `docs/design/mockups/assets/nour.webp` | ناقص | 1 | غير منسوخة إلى `res/drawable-nodpi` |
| Y- Dimensions | خط Cairo | `res/font` | متوفر | 2 | — |
| Y- Dimensions | بيانات الكتب (24 كتاباً) | `panel/data/books.json` | متوفر | 1 | مستخرجة تلقائياً |
| Y- Dimensions | بيانات `app.json` | `panel/data/app.json` | متوفر | 1 | — |
| Y- Dimensions | `config.php` + قاعدة MySQL | Hostinger | **ناقص** | 0 | يجب على المطوّر إدخال القيم الحقيقية؛ لا تخترعها |
| Y- Dimensions | `admin_token`, `license_secret` | Hostinger | **ناقص** | 0 | اطلبهما من المطوّر |
| Y- Dimensions | توحيد `users` ثم استيراد الترحيلات غير المكررة فقط | لوحة الأم | **ناقص** | 0 | `001_init.sql` ينشئ `users`؛ لا يُنفَّذ كما هو على قاعدة موحّدة |
| Y- Dimensions | توقيع APK موحّد | `ci/test-signing.p12` / `KEYSTORE_B64` | **ناقص** | 1 | اختلاف التوقيع هو سبب طلب الصلاحيات بعد كل تحديث |

الفرز المطلوب: الناقص أولاً، ثم الأولوية تصاعدياً، ثم اسم التطبيق.

## 6. خطوات التحقق قبل إعلان الاكتمال
1. قراءة هذا الملف وتنفيذ كل بند وتسجيل النتيجة في `sync_log`.
2. التحقق من ترتيب التثبيت وبصمات `MAP.json` (25 ملفاً، 13 فحصاً).
3. التأكد أن `users` موحّد ولم يتكرر، وأن كل استعلام مقيّد بـ `app_id`.
4. التأكد أن كل نقطة API تعيد JSON لا HTML، وأن `content.manifest` يُسحب عند بدء التشغيل.
5. التأكد من وجود الزر العائم في كل صفحة/وثيقة/لوحة، وأن الصفحة المقفلة تعيد للوحة الأساسية.
6. التأكد أن تبويب «متطلبات التطبيقات» يعرض القسمين مع الفرز والتأشير.
7. كتابة تقرير تحقق موقّت باسم `VERIFY.md`.

**لا تخترع قيماً حسّاسة.** اطلب `config.php` و`admin_token` و`license_secret` وتوقيع APK من المطوّر.


## 9. تحديث 0.18.0 — المطورون والاختبارات والحسابات والتشخيص
- أضف تبويب **«المشرفون والمطورون»**: تعيين مستخدم من `users` كـ `developer`/`admin` في الجدول الجديد غير المكرر `developer_accounts`، وحسابات لوحة تحكم (حد أقصى 3) في `panel_admins`.
- أضف تبويب **«اختبارات المطور»** من `config/devmode_tests.json` (62 اختباراً يدوياً) مع تمكين/تعطيل كل اختبار.
- أضف تبويب **«بلاغات التشخيص»**؛ استقبل البلاغات المجهّلة في `diagnostic_reports` وطابق `diagnostic_solutions` قبل إعادة التشخيص.
- التطبيق: `devmode.php?action=status` يغلق وضع المطور لغير المصرّح له؛ `diagnostics.php` لل_match/report/list.
- التطبيق: تبديل حتى 3 حسابات مع نسخة حالة لكل حساب؛ وضع المطور بأربعة أقسام (اختبارات يدوية، رجوع، بيانات، فوارق الإصدارات).
- الترحيلات `004_devmode.sql` و`005_journal.sql` و`006_learning.sql` مطلوبة قبل إعلان الاكتمال؛ `users` بقي موحّداً ولم يتكرر، ولا يُستورد `001_init.sql` كما هو على قاعدة لوحة الأم.
- برومت مساعد التشخيص الكامل محفوظ في `docs/diagnostics/SYSTEM_PROMPT.md` ومنفّذ في `core/diagnostics/`.

## 10. تصحيح رفع اللوحة التنفيذية — 0.19.0

اقرأ `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md` قبل التثبيت. **الشرح السابق كان يفترض
`public_html/` فارغاً؛ هذا غير صحيح على دومين فيه لوحة الأم/موقع آخر.** لا ترفع محتويات
`server/public/` إلى جذر `public_html/`، ولا تستبدل `config.php` أو `index.php` أو `admin/` الأصلية.

- **اللوحة التنفيذية** تُرفع كملف ZIP واحد: `docs/mother-panel/y-dimensions-mother-panel.zip`،
  يُنشأ بـ `python3 server/tools/build_mother_panel_zip.py`. لوحة الأم تخصص لها مجلداً داخل
  `{mother_panel.apps_dir}` وتستخرج الحزمة داخله؛ النتيجة `.../public/panel/`، لا `public_html/panel/`.
- `mother_panel.apps.open_panel` يفتح `panel_url` من بطاقة `y-dimensions` في تبويب/صفحة جديدة؛
  اللوحة تمنع الإطار افتراضياً (`X-Frame-Options: DENY`).
- **اللوحة التابعة** `server/public/boards/y-dimensions/` تبقى وحدها داخل `{mother_panel.boards_dir}`
  عبر `MAP.json` و`mother_panel.install.map` بالترتيب 1…25، ثم `mother_panel.verify.run`
  (13 فحصاً و`VERIFY.md`). لا تُنسخ داخل حزمة ZIP التنفيذية.
- اللوحة 18 تبويباً، منها `panel/?p=learning`؛ وتحتاج `config/config.php` داخل المجلد المخصص فقط،
  بقيم `db`, `base_url`, `license_secret`, `admin_token` من المالك.
- **قاعدة واحدة و`users` موحّد:** `001_init.sql` ينشئ `users`؛ لا يُستورد كما هو على قاعدة لوحة الأم.
  نفّذ توحيد الجداول ثم استيراد الترحيلات غير المكررة فقط قبل إعلان الاكتمال.
- لا تخترع `{mother_panel.apps_dir}` ولا `{mother_panel.boards_dir}` ولا `admin_token` ولا `config.php`؛
  اطلبها من المالك، ولا تضعها في الحزمة التابعة ولا في Git.
- لا تعدّل ملفات `boards/y-dimensions/` يدوياً؛ بصمات `MAP.json` سليمة ويجب أن تبقى كذلك.

## 11. ما المطلوب من موديل لوحة الأم عند وجود موقع آخر في الجذر؟

- أضف `mother_panel.apps.allocate_dir`: ينشئ مجلداً فريداً داخل `{mother_panel.apps_dir}`
  ويُرجع `app_public_url`, `panel_url`, `api_base`. لا يلمس جذر `public_html/`.
- أضف `mother_panel.apps.install_zip`: يستقبل `y-dimensions-mother-panel.zip`، يقرأ
  `mother-panel.json`، ويستخرج المحتويات مباشرة داخل المجلد المخصص، ويحفظ المسارات في بطاقة التطبيق.
- امنع أي نسخ إلى `public_html/config.php`, `public_html/index.php`, `public_html/admin/`,
  `public_html/core/` وما شابهها. الأسماء المتشابهة تُحل بالعزل داخل المجلد المخصص لا بالدمج.
- سجّل `api_base` في بطاقة التطبيق واعرضه للمطوّر ليضعه في شاشة الحساب ← «عنوان API للخادم»؛
  لوحة الأم لا تغيّر APK المثبت، وإن أُريد تغيير الافتراضي فذاك `config/environments.json` + بناء APK.
- نفّذ عقد قاعدة البيانات الموحّدة قبل استيراد الترحيلات، ثم تحقق من JSON والتبويبات و`open_panel`
  واكتب تقرير `VERIFY.md`. لا تُعلن الاكتمال قبل ذلك.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
