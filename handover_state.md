<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# حالة التسليم — Y- Dimensions

## ملخص بسيط

التطبيق مثل صندوق أدوات: تكتب المهام والمصروف، وتكسب نقاطاً. الخادم يحفظ الحساب، واللوحة تتحكم بالمستخدمين والمطورين، والمطور يختبر كل ميزة بيده.

## الحالة الحالية

- الإصدار: `0.18.0` / `code 22`.
- آخر وسم: `v0.18.0-test.1`، الإصدار منشور مع `app-qa.apk`، وPR #30 مدموج في `dev`.
- الدخول أونلاين أولاً؛ مبدأ أوفلاين أولاً ملغى.
- حتى 3 حسابات في اللوحة والتطبيق، مع عزل حالة كل حساب.
- تعيين مستخدمي التطبيق كـ`admin` أو `developer` من لوحة التحكم.
- وضع المطور: 4 أقسام، 62 اختباراً يدوياً، رجوع قبل الاختبار، بيانات التطبيق، وفروق الإصدارات. لا اختبارات تلقائية.
- مساعد تشخيص العتاد: قراءات فعلية، تقرير عربي، `fix_actions` بعد موافقة، حلول معروفة، و`server_report` مجهّل بلا IMEI/هاتف/SSID/موقع.
- اللوحة الأساسية: `server/public/panel/` فيها 17 تبويباً وواجهات `devmode.php` و`diagnostics.php`.
- لوحة الأم التابعة: `server/public/boards/y-dimensions/` بقيت دون مساس، مع `MAP.json` وVerify.
- `sister_apps` فارغة؛ البريد النهائي `yswefalhmzy@gmail.com`.

## ملفات مهمة

- `app/`: تطبيق أندرويد وCompose.
- `server/public/panel/index.php`: اللوحة التنفيذية.
- `server/migrations/004_devmode.sql`: جداول المطورين والمشرفين والتشخيص.
- `config/devmode_tests.json` و`config/releases.json`: كتالوج الاختبارات والإصدارات.
- `docs/diagnostics/SYSTEM_PROMPT.md`: برومت التشخيص الكامل.
- `docs/plan/index.html`: ملخص الخطة البسيط.
- `docs/plan/feedback.html`: صفحة الاقتراحات والملاحظات الموحّدة.

## قيود ثابتة

- قاعدة بيانات واحدة، `users` موحّد، ولا جدول مستخدمين خاص بتطبيق.
- الجداول الجديدة غير المكررة مسموح بها.
- التطبيق `Y- Dimensions` من نوع `mobile` فقط، والمطوّر `يوسف الحمزي`.
- توقيع المطوّر في بداية ونهاية كل ملف يتم تعديله.

## لم يُختبر بعد

- استيراد `004_devmode.sql` على MySQL حقيقي وضبط `server/config/config.php`.
- تشغيل API واللوحة على استضافة فعلية.
- تشغيل التطبيق على جهاز فعلي وصلاحيات التشخيص.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
