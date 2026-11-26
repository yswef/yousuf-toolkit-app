<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تعديل

- الموديل: arena-agent
- الميزة: build and tag v0.1.0-test.1
- التاريخ: 2026-11-17
- الملفات المتأثرة: .github/workflows/release.yml، build.gradle.kts، app/build.gradle.kts، gradle/libs.versions.toml، gradle.properties، app/src/main/kotlin/feature/home/HomeScreen.kt، docs (هذا الملف وCHANGELOG وmodules/ui-phase1 وtests-data وstatus-data وhandover_state)

## ما تم

دمج فرع العمل في dev عبر PR، وإنشاء التاج v0.1.0-test.1 ونشر بناء اختبار (app-qa.apk) بنجاح. تصحيح خط البناء: استخدام Android SDK المثبت على ubuntu-24.04، وتخطي أسرار التوقيع في بناء الاختبار، وإضافة Android SDK تلقائياً عبر AGP.

تصحيحات توافق AGP 9: Kotlin المدمج (حذف org.jetbrains.kotlin.android)، وDSL الجديد مع isDebuggable، وتثبيت material-icons-core 1.7.8، والعودة إلى compileSdk/targetSdk 36 وCompose BOM 2026.06.01 لأن حزمة android-37 غير متاحة باسم مسطح. اسم build type يبدأ بـ test ممنوع في AGP 9، فصار qa مع مهمة assembleTester كاسم مستقر للـworkflow.

## يحتاج تحققاً يدوياً

تثبيت app-qa.apk واختبار الشاشات والتبديل بين الواجهتين والوضع الداكن، كما في docs/site/dev/tests-data.js.
