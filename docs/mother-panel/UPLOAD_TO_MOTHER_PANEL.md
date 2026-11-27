<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# رفع اللوحة التنفيذية إلى لوحة أم موجودة على نفس الدومين — 0.19.0

> **تصحيح مهم:** الشرح السابق كان يفترض أن `public_html/` فارغ. على دومينك توجد لوحة/موقع آخر
> شغّال في الجذر؛ لذلك **لا ترفع محتويات `server/public/` كاملة إلى `public_html/`**،
> ولا تُنشئ لوحة تحكم ثانية في الجذر. المطلوب حزمة ZIP واحدة تضعها لوحة الأم في مجلد تخصصه،
> ثم تفتح لوحة التطبيق من بطاقتها داخل لوحة الأم.

## 1. ما الذي يبقى وما الذي يُرفع؟

الصورة من File Manager تُظهر داخل `public_html/`: `config.php`، `config-sample.php`،
`index.php`، `admin/`، `app/`، `assets/`، `core/`، `data/`، `docs/`، `lang/`،
`cron_backup.php`، `cron_sync.php`، `cron_update.php`، `manifest.webmanifest`،
`status.php`، `update_start.php`، `update_step.php`، و`.htaccess`. **كلها تبقى كما هي.**

| العنصر | أين يذهب؟ | ملاحظة |
|---|---|---|
| `docs/mother-panel/y-dimensions-mother-panel.zip` | المجلد الذي تخصصه لوحة الأم: `{mother_panel.apps_dir}/<allocated-folder>/` | الحزمة الوحيدة التي ترفعها؛ جذرها هو محتويات المجلد المخصص، بلا مجلد إضافي فوقها |
| `public/panel/` داخل الحزمة | `.../<allocated-folder>/public/panel/` | **اللوحة التنفيذية**، 18 تبويباً، ومنها `?p=learning` |
| `config/config.php` داخل الحزمة | `.../<allocated-folder>/config/config.php` | يُنشأ من `config.example.php` بقيم المالك؛ ليس `public_html/config.php` |
| `server/public/boards/y-dimensions/` | `{mother_panel.boards_dir}/y-dimensions/` | لوحة تابعة إعلانية منفصلة، عبر `MAP.json` و`mother_panel.install.map` فقط |
| `public/admin/` | **لا يُرفع** | مستبعد من الحزمة عمداً حتى لا تصير لوحة إدارة ثانية |

الأسماء المتشابهة لا تتعارض لأنها في مجلدات مختلفة: `public_html/config.php` يخص الموقع/لوحة الأم،
و`.../<allocated-folder>/config/config.php` يخص هذا التطبيق وحده. لا تستبدل الأول ولا تدمج المجلدات.

## 2. جهّز الحزمة

من جذر المستودع:

```bash
python3 server/tools/build_mother_panel_zip.py
```

الناتج: `docs/mother-panel/y-dimensions-mother-panel.zip` — ملف صغير (نحو 87 كيلوبايت)
**منشور في Git** ويُرفق تلقائياً كأصل في كل إصدار `v*` يبنيه `.github/workflows/release.yml`، لأن المطلوب تنزيله مباشرة
من GitHub لا توليده محلياً فقط. الرابط: `https://github.com/yswef/yousuf-toolkit-app/blob/dev/docs/mother-panel/y-dimensions-mother-panel.zip`. بداخله `INSTALL.md` و`mother-panel.json` يشرحان للوحة الأم
المطلوب، و`.htaccess` يحمي
`src/` و`config/` و`migrations/`، و`public/` فيه `api/` و`panel/` و`content/` و`learn/`.
لا يوجد داخل الحزمة `config.php` حقيقي ولا توكن ولا كلمة مرور.

## 3. ارفعها من لوحة الأم

1. إن كان في لوحة الأم زر «رفع حزمة تطبيق / Install from ZIP»، ارفع الملف هناك. المطلوب من
   `mother_panel.apps.allocate_dir` أن ينشئ مجلداً فريداً داخل `{mother_panel.apps_dir}`
   ويستخرج الحزمة **داخله**، ثم يُرجع ثلاث قيم: `app_public_url`، `panel_url`، `api_base`.
2. إن لم يوجد الزر: من File Manager أنشئ مجلداً جديداً تحت `public_html/` (مثلاً
   `public_html/apps/y-dimensions/`)، ارفع الـ ZIP داخله، ثم `Extract` **داخل المجلد نفسه**.
   لا تختر `public_html/` كوجهة، ولا تترك أعلى الحزمة مجلداً إضافياً.
3. الرابط المتوقع بعد الاستخراج (استبدل `<...>` بما تعطيك لوحة الأم فعلياً):
   - `app_public_url = https://<domain>/<allocated-path>/public`
   - `panel_url = https://<domain>/<allocated-path>/public/panel/`
   - `api_base = app_public_url` أو `.../public/panel` لأن `panel/api/*.php` تحوّل الطلبات إلى `api/*.php`.
4. سجّل بطاقة `y-dimensions` في لوحة الأم: `mother_panel.apps.open_panel` → `panel_url`.
   افتحها في **تبويب/صفحة جديدة**؛ اللوحة ترسل `X-Frame-Options: DENY` افتراضياً. عرضها داخل
   إطار يحتاج ضبط `YD_PANEL_PREVIEW=1` من لوحة الأم/الخادم فقط، ولا نفعّله في الحزمة العامة.

## 4. الإعداد وقاعدة البيانات

1. انسخ `.../<allocated-folder>/config/config.example.php` إلى `config.php` في نفس المجلد،
   واملأ `db`, `base_url`, `license_secret`, `admin_token`, `allowed_origins` من مالك
   لوحة الأم/الاستضافة. **لا تخمّنها**، ولا تلمس `public_html/config.php`.
2. `base_url` = `app_public_url` بدون `/panel`.
3. **قاعدة واحدة و`users` موحّد** كما في `docs/dev/RULES.md` و`MOTHER_PANEL_HANDOFF.md`.
   لكن `migrations/001_init.sql` ما زال ينشئ `users`؛ لذلك لا تستورده كما هو على قاعدة لوحة الأم.
   على موديل لوحة الأم تنفيذ عقد التسليم: ربط الجداول الموحّدة واستيراد الترحيلات غير المكررة
   فقط، أو تعديل `001_init.sql` إلى `CREATE TABLE IF NOT EXISTS` مع بقاء `users` الأصلي.
   إلى أن يتحقق ذلك تعمل اللوحة في **وضع القراءة فقط**؛ لا تُعلن اكتمال التشغيل.

## 5. اجعل التطبيق يتواصل مع المجلد

- للأجهزة المثبتة: شاشة الحساب ← «عنوان API للخادم» ← ضع `api_base`. لا يحتاج إصداراً جديداً.
- لتغيير الافتراضي في كل بناء: غيّر `config/environments.json` → `apiBaseUrl` إلى `api_base`،
  ثم `python3 server/tools/build_panel_data.py` وأعد توليد الحزمة/APK عند الحاجة. لا وسم جديد هنا.
- لا تضع `api_base` داخل `boards/y-dimensions/`؛ تلك قوالب تحلّها لوحة الأم وحدها.

## 6. اللوحة التابعة تبقى منفصلة

`server/public/boards/y-dimensions/` بـ25 ملفاً و`MAP.json` تُثبّت وحدها داخل
`{mother_panel.boards_dir}/y-dimensions/` عبر `mother_panel.install.map` بالترتيب 1…25،
ثم إلزامياً `mother_panel.verify.run` (13 فحصاً وتقرير `VERIFY.md`). لا تُنسخ داخل حزمة
اللوحة التنفيذية ولا داخل `public/`، ولا تُعدّل ملفاتها يدوياً.

## 7. قائمة تحقق قبل إعلان الاكتمال

- `docs/mother-panel/y-dimensions-mother-panel.zip` ظاهر في المستودع على GitHub، وأصول كل إصدار `v*` تحتوي نسخة منه تلقائياً.
- ملفات جذر `public_html/` الأصلية لم تتغير، ولم يُستبدل `config.php` ولا `index.php`.
- الحزمة مستخرجة داخل مجلد التطبيق المخصص فقط، و`public/panel/index.php` موجود.
- `config/config.php` موجود داخل المجلد المخصص فقط، وليس في Git، وقيمه من المالك.
- `GET app_public_url/` يرجع JSON، و`api/content.php?action=manifest` يرجع JSON لا HTML.
- `panel_url?p=learning` يفتح التبويبات، وبطاقة لوحة الأم تفتح `panel_url`.
- التطبيق يسجّل الدخول على `api_base` ويرجع JSON.
- `users` موحّد ولم يُنشأ جدول مستخدمين ثانٍ؛ الترحيلات غير المكررة فقط هي المستوردة.
- `mother_panel.install.map` أنجز 25 خطوة و`mother_panel.verify.run` نجح، ولم تتغير بصمات اللوحة التابعة.

الملف التنفيذي داخل الحزمة: `INSTALL.md`. عقد لوحة الأم: `docs/mother-panel/MOTHER_PANEL_HANDOFF.md`.
النشر على دومين فارغ فقط: `server/HOSTINGER.md`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
