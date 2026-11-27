<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# MOTHER_PANEL_HANDOFF.md — من لوحة تحكم Y- Dimensions إلى موديل لوحة التحكم الأم

> هذا هو الملف الإلزامي الذي يقرأه موديل لوحة الأم قبل إعلان اكتمال تثبيت التطبيق.
> التطبيق: `Y- Dimensions` · موبايل · يوسف الحمزي · `yswefalhmzy@gmail.com` · الإصدار `0.17.0`.
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
| `mother_panel.apps.open_panel` | GET | فتح لوحة التطبيق بكل تبويباتها داخل لوحة الأم | boards, boards_dir |
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
| Y- Dimensions | استيراد `001_init.sql`, `002_admin.sql`, `003_devices.sql` | Hostinger | **ناقص** | 0 | خطوة نشر |
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
- الترحيلات `004_devmode.sql` و`005_journal.sql` و`006_learning.sql` مطلوبة قبل إعلان الاكتمال؛ `users` بقي موحّداً ولم يتكرر.
- برومت مساعد التشخيص الكامل محفوظ في `docs/diagnostics/SYSTEM_PROMPT.md` ومنفّذ في `core/diagnostics/`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
