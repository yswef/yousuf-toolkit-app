# 2026-11-24 — مجلد admin، مكتبة التمارين، صفحات الخطة

- لوحة التحكم: `server/public/admin.php` → `server/public/admin/index.php` مع `README.md` و`.htaccess` و`settings.example.php`. المسارات تُحسب من جذر الخادم (مستويان فوق المجلد) أو `settings.php` أو `YD_SERVER_ROOT`. تحويلات الدخول والخروج صارت نسبية للمسار الحالي. حُدّثت وثائق الخادم والموقع.
- التمارين: مكتبة من free-exercise-db (876 تمريناً، ملكية عامة) في `assets/exercises/exercises.json`، وشاشة `ExerciseLibraryScreen` بالبحث وفلترة العضلة والأداة والمستوى وتفصيل بالصور. الصور تُجلب من raw.githubusercontent.com وتُخزَّن مؤقتاً. الملفات: `ExerciseLibrary.kt` و`ExerciseLibraryScreen.kt` وتعديل `WorkoutsScreen.kt` و`THIRD_PARTY.md`.
- docs/plan: تقسيم الصفحة الواحدة إلى 14 صفحة بقائمة جانبية، لون لكل صفحة، خط أصغر، جداول بطاقية للجوال. أُضيفت صفحتا `review.html` و`admin.html`.
- لم يُبنَ التطبيق (لا Android SDK في بيئة التعديل): يلزم تشغيل `assembleTester` والتحقق.
