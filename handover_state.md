<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# حالة التسليم — Y- Dimensions

## ملخص بسيط

التطبيق مثل صندوق أدوات: تكتب المهام والمصروف، وتكسب نقاطاً. الخادم يحفظ الحساب، واللوحة تتحكم بالمستخدمين والمطورين، والمطور يختبر كل ميزة بيده.

## الحالة الحالية

- الإصدار: `0.19.0` / `code 23`.
- أوسمة محلية: `v0.19.0-note.01` حتى `v0.19.0-note.10`؛ والملاحظة 11 تصنيفية بلا وسم. إصدار `v0.18.0-test.1` و`app-qa.apk` وPR #30 مدموجة سابقاً في `dev`.
- نُفّذت الملاحظات الـ11: دفتر المذكرات، برومت/استيراد التعلم، المزامنة، القفل والبصمة، الصوت المدفوع، رفع/مراجعة/نشر الخطط، الحقوق والخصوصية، صور وZIP الاقتراحات، محرر الاقتراحات، تكبير الخط، وقبول `[عام]`.
- تم اختيار تخطيط ZIP A: مجلد لكل مدخل، و`all-notes.md` في الجذر، وأسماء صور «الأولى/الثانية».
- الدخول أونلاين أولاً؛ مبدأ أوفلاين أولاً ملغى.
- حتى 3 حسابات في اللوحة والتطبيق، مع عزل حالة كل حساب.
- تعيين مستخدمي التطبيق كـ`admin` أو `developer` من لوحة التحكم.
- وضع المطور: 4 أقسام، 62 اختباراً يدوياً، رجوع قبل الاختبار، بيانات التطبيق، وفروق الإصدارات. لا اختبارات تلقائية.
- مساعد تشخيص العتاد: قراءات فعلية، تقرير عربي، `fix_actions` بعد موافقة، حلول معروفة، و`server_report` مجهّل بلا IMEI/هاتف/SSID/موقع.
- اللوحة الأساسية: `server/public/panel/` فيها 18 تبويباً، مع `learning.php` و`panel/?p=learning`.
- **تسجيل عمليات المال من الإشعارات:** بارسر واحد لإشعارات محفظة جيب والرسائل النصية، مع ملاحظة «من/إلى» والرصيد بعد العملية، ومفتاح منع تكرار موحّد؛ الإشعارات هي المصدر الأساسي والرسائل اختيارية.
- أُعيد توليد `docs/mother-panel/y-dimensions-mother-panel.zip` بعد تحديث `api/sync.php` لحفظ ملاحظة العملية مع العنوان عند المزامنة.
- **تصحيح الرفع:** `public_html/` فيه لوحة أم/موقع آخر؛ لا يُرفع `server/public/` إلى الجذر. اللوحة التنفيذية تُرفع كحزمة `docs/mother-panel/y-dimensions-mother-panel.zip` داخل مجلد تخصصه لوحة الأم، وتُفتح من `mother_panel.apps.open_panel`.
- أُضيف مولّد الحزمة `server/tools/build_mother_panel_zip.py` وشرح `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md` المحدَّث، مع `INSTALL.md` و`mother-panel.json` داخل الحزمة.
- الحزمة نفسها منشورة في `docs/mother-panel/y-dimensions-mother-panel.zip` وتُرفق تلقائياً كأصل في كل إصدار `v*` عبر `.github/workflows/release.yml`؛ مجلد `build/` مستثنى من Git ولا يظهر على GitHub.
- التوقيع في CI: وسوم `beta` والإصدارات الرسمية تبني `assembleRelease`؛ عند غياب أسرار `KEYSTORE_*` تُوقّع بمفتاح الاختبار المضمّن `ci/test-signing.p12` مع `::warning::`، ولا يصبح التوقيع إنتاجياً إلا بعد إضافة الأسرار الأربعة في إعدادات المستودع.
- لوحة الأم التابعة: `server/public/boards/y-dimensions/` بقيت دون مساس، مع `MAP.json` وVerify.
- `sister_apps` فارغة؛ البريد النهائي `yswefalhmzy@gmail.com`.

## ملفات مهمة

- `app/`: تطبيق أندرويد وCompose.
- `server/public/panel/index.php`: اللوحة التنفيذية.
- `server/migrations/004_devmode.sql`, `005_journal.sql`, `006_learning.sql`: ترحيلات المطورين والمذكرات وخطط التعلم.
- `config/devmode_tests.json` و`config/releases.json`: كتالوج الاختبارات والإصدارات.
- `docs/diagnostics/SYSTEM_PROMPT.md`: برومت التشخيص الكامل.
- `docs/plan/index.html`: ملخص الخطة البسيط.
- `docs/plan/feedback.html`: صفحة الاقتراحات والملاحظات الموحّدة مع الصور وZIP والمحرر.
- `docs/plan/feedback-zip.js`: كاتب ZIP مستقل بلا مكتبات خارجية.
- `app/src/main/kotlin/core/device/WalletTxParser.kt` و`config/wallet_notifications.json`: قواعد استخراج عمليات المحفظة من الإشعارات/الرسائل.
- `docs/mother-panel/UPLOAD_TO_MOTHER_PANEL.md`: رفع حزمة ZIP داخل مجلد تخصصه لوحة الأم، وربطها، وتثبيت اللوحة التابعة منفصلة.
- `server/tools/build_mother_panel_zip.py`: يولّد `docs/mother-panel/y-dimensions-mother-panel.zip` بلا `admin/` ولا `boards/` ولا أسرار.

## قيود ثابتة

- قاعدة بيانات واحدة، `users` موحّد، ولا جدول مستخدمين خاص بتطبيق؛ `001_init.sql` لا يُستورد كما هو على قاعدة لوحة الأم.
- الجداول الجديدة غير المكررة مسموح بها.
- التطبيق `Y- Dimensions` من نوع `mobile` فقط، والمطوّر `يوسف الحمزي`.
- توقيع المطوّر في بداية ونهاية كل ملف يتم تعديله.
- التصنيف `[عام]` مقبول للملاحظات القادمة؛ لا تطلب تصنيفاً أدق.

## لم يُختبر بعد

- استيراد `004_devmode.sql` و`005_journal.sql` و`006_learning.sql` على MySQL حقيقي وضبط `config/config.php` داخل المجلد المخصص، وتوحيد `users` مع لوحة الأم.
- استخراج `y-dimensions-mother-panel.zip` فعلياً على Hostinger داخل مجلد تخصصه لوحة الأم، وضبط `mother_panel.apps.allocate_dir` / `install_zip` من طرف لوحة الأم.
- تشغيل API واللوحة على استضافة فعلية، خصوصاً رفع الخطط والنشر وCSRF.
- تشغيل التطبيق على جهاز فعلي: WebView، الصوت، البصمة/الوجه، الإشعارات، والصلاحيات؛ خصوصاً التقاط إشعارات جيب ومنع تكرارها مع الرسائل.
- لا يوجد `java`/`kotlinc`/Gradle/SDK محلياً؛ بناء GitHub Actions عند دفع الأوسمة هو المرجع.
- تحقق ثابت فقط لـ Kotlin/PHP/XML/JS/JSON وZIP؛ لا اختبار متصفح حقيقي لواجهة الاقتراحات.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
