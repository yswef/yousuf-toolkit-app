<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تعديل

- الموديل: arena-agent
- الميزة: ui phase1
- التاريخ: 2026-11-17
- الملفات المتأثرة: مشروع Gradle (settings وbuild وcatalog وwrapper)، app/build.gradle.kts، AndroidManifest.xml، MainActivity.kt، core/design (Theme وType وUiState)، core/navigation (Routes وNavGraph)، شاشات feature الـ30، res/values وvalues-en وthemes وcolors والأيقونة، config/app.json، .github/workflows/release.yml، docs (هذا الملف وCHANGELOG وmodules/ui-phase1 وSYMBOLS وtests-data وstatus-data وhandover_state)

## ما تم

إنشاء مشروع Gradle (Kotlin 2.4.10 وAGP 9.1.1 وGradle 9.3.1 وCompose BOM 2026.09.00 وcompileSdk 37 وminSdk 34) مع نوع البناء tester والتوقيع من متغيرات البيئة. الواجهات بلا منطق: ثيم Material الافتراضي (فاتح وداكن) وواجهتان (احترافية وبسيطة) تتبدلان من الإعدادات، وتنقل بين البداية والرئيسية والإعدادات والميزات الـ30، وكل النصوص في الموارد بالعربية أولاً وRTL. شاشة اللعبة تعرض فرق السحر العشرة من المالك. قيم الطاقة والنقاط في config/app.json بموافقة المالك وتُعدّل لاحقاً. إضافة Android SDK إلى خط البناء.
