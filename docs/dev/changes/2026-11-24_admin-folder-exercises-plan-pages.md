<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# 2026-11-24 — مجلد admin، مكتبة التمارين، صفحات الخطة

- لوحة التحكم: `server/public/admin.php` → `server/public/admin/index.php` مع `README.md` و`.htaccess` و`settings.example.php`. المسارات تُحسب من جذر الخادم (مستويان فوق المجلد) أو `settings.php` أو `YD_SERVER_ROOT`. تحويلات الدخول والخروج صارت نسبية للمسار الحالي. حُدّثت وثائق الخادم والموقع.
- التمارين: مكتبة من free-exercise-db (876 تمريناً، ملكية عامة) في `assets/exercises/exercises.json`، وشاشة `ExerciseLibraryScreen` بالبحث وفلترة العضلة والأداة والمستوى وتفصيل بالصور. الصور تُجلب من raw.githubusercontent.com وتُخزَّن مؤقتاً. الملفات: `ExerciseLibrary.kt` و`ExerciseLibraryScreen.kt` وتعديل `WorkoutsScreen.kt` و`THIRD_PARTY.md`.
- docs/plan: تقسيم الصفحة الواحدة إلى 14 صفحة بقائمة جانبية، لون لكل صفحة، خط أصغر، جداول بطاقية للجوال. أُضيفت صفحتا `review.html` و`admin.html`.
- لم يُبنَ التطبيق (لا Android SDK في بيئة التعديل): يلزم تشغيل `assembleTester` والتحقق.
