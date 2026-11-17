# الواجهات بلا منطق (المرحلة 1)

- الحالة: تم، منشور بالتاج v0.1.0-test.1 مع app-qa.apk، بانتظار التحقق اليدوي على الجهاز
- الحزمة: core.design وcore.navigation وfeature.*
- المسؤولية: ثيم Material الافتراضي وواجهتان (احترافية 18+ وبسيطة) وتنقل بين الشاشات، دون منطق أو تخزين أو شبكة
- الشاشات: onboarding وhome وsettings وgame (فرق السحر) و26 شاشة ميزة بحالة فارغة
- التبعيات: Kotlin المدمج في AGP 9 (compiler plugin 2.4.10)، AGP 9.1.1، Gradle 9.3.1، Compose BOM 2026.06.01، Navigation 2.8.0، material-icons-core 1.7.8، compileSdk/targetSdk 36، minSdk 34
- البناء: build type qa مع مهمة assembleTester (AGP 9 يمنع الأسماء التي تبدأ بـ test)، وتوقيع debug في بناء الاختبار
- الإعدادات: config/app.json (طاقة ونقاط ومستويات بموافقة المالك)، وUiState في الذاكرة (الحفظ لاحقاً مع core.storage)
- الاختبارات: يدوية في docs/site/dev/tests-data.js (قسم ui)
- مشاكل مفتوحة: نص الترخيص فارغ، والإنجليزية للواجهة لاحقاً، وRoom وWorkManager والخادم في المراحل التالية
