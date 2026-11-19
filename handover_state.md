# handover_state

- آخر تحديث: 2026-11-19
- حدّثه: Claude Sonnet 5.5
- المهمة: الواجهات بلا منطق (المرحلة 1) + بناء ونشر v0.1.0-test.1

كل موديل يقرأ هذا الملف أولاً ولا يقرأ كامل الكود. وفي نهاية عمله يحدّثه هو الملف الوحيد للتسليم.

## هيكلة المجلدات

انظر README.md. مشروع Gradle موجود (settings وbuild وcatalog وwrapper)، والتطبيق في app/src/main/kotlin (core وfeature، وdata وdomain فارغة)، والموارد في app/src/main/res (values عربية وvalues-en للفرق فقط)، والقيم في config/app.json.

## حالة المكونات

| المكون | الحالة | ملاحظة |
|---|---|---|
| هيكل المستودع | تم | بانتظار تحققك من صفحة الاختبارات |
| خط البناء release.yml | تم | ubuntu-24.04 وSDK المثبت، وتخطي التوقيع في بناء الاختبار |
| مشروع Gradle | تم | Kotlin المدمج (AGP 9)، AGP 9.1.1، Gradle 9.3.1، ونوع qa مع مهمة assembleTester |
| الواجهات | تم | المرحلة 1: ثيم وواجهتان وتنقل و30 شاشة بلا منطق. أُصلح انهيار الفتح (MainActivity بلا package) ووُضعت الأيقونة المؤقتة |
| التاج v0.1.0-test.1 | تم | منشور مع app-qa.apk، وكل خطوات Actions نجحت، بانتظار التحقق اليدوي |
| النواة (مهام، مال، تمارين، مذكرات) | لم يبدأ | المرحلة 2 |
| الخادم PHP | لم يبدأ | |
| لوحة التحكم | لم يبدأ | |

## المسارات والروابط

- المستودع: https://github.com/yswef/yousuf-toolkit-app
- Release الاختبار: https://github.com/yswef/yousuf-toolkit-app/releases/tag/v0.1.0-test.1
- حالة المشروع: docs/site/dev/index.html
- الاختبارات: docs/site/dev/tests.html
- سجل التعديلات: docs/dev/changes/ ومؤشره docs/dev/CHANGELOG.md
- قواعد الفريق: docs/dev/RULES.md
- فهرس الرموز: docs/dev/SYMBOLS.md
- توثيق الوحدة: docs/dev/modules/ui-phase1.md

## التبعيات والإصدارات

- Android 14 (API 34) كأقل إصدار، compileSdk/targetSdk 36، applicationId com.yousefalhmzy.ydimension (نسخة qa تضيف .tester، وdebug تضيف .debug)
- AGP 9.1.1، Gradle 9.3.1، Java 17، Kotlin المدمج في AGP مع compose compiler plugin 2.4.10
- Compose BOM 2026.06.01، Navigation 2.8.0، activity-compose 1.13.0، material-icons-core 1.7.8 (مثبت صراحة لأن BOM لم يعد يجلبه)
- compileSdk 37 غير مستخدم لأن حزمة platforms;android-37 غير منشورة باسم مسطح (توجد android-37.0 فقط)

## المشاكل المفتوحة

- ملف LICENSE أُضيف (ترخيص خاص مغلق الحقوق). شاشة الترخيص ما زالت فارغة
- ملف التوقيع وأسرار GitHub لم تُضف (مطلوبة فقط لبناء beta/release، وليس لبناء الاختبار)
- قيم الطاقة والنقاط في config/app.json بموافقة المالك وتُعدّل لاحقاً
- ربط كود التفعيل بحساب المستخدم (لم يُنفذ بعد)
- الإنجليزية للواجهة لاحقاً (values-en يحوي الفرق واسم التطبيق فقط)
- حفظ إعدادات الواجهة في الذاكرة فقط (الحفظ مع core.storage لاحقاً)
- AGP 10 سيلغي تعطيل Kotlin المدمج؛ لا يوجد تعطيل حالياً، والبناء يسير على المسار المدمج
- تغيّر applicationId عن النسخة المنشورة v0.1.0-test.1: يلزم حذف النسخة القديمة من الهاتف قبل تثبيت الجديدة
- الإصلاح لم يُبنَ ولم يُجرَّب (لا Gradle في بيئتي): التحقق على جهازك بعد بناء tag جديد
- نظام الحماية (src وvault وتوقيع الملفات) من حزمة v2 غير مطبق في هذا المستودع بعد
