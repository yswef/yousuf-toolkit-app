# handover_state

- آخر تحديث: 2026-11-19
- حدّثه: arena-agent
- المهمة: المكتبة السحرية + الدخولية بالحوار + تسجيل بيانات البطل + تاج v0.3.0-test.1

كل موديل يقرأ هذا الملف أولاً ولا يقرأ كامل الكود. وفي نهاية عمله يحدّثه هو الملف الوحيد للتسليم.

## هيكلة المجلدات

انظر README.md. التطبيق في app/src/main/kotlin: `core` (config وdesign وnavigation وstorage الفارغ) و`data/local` (التخزين) و`domain/model` (النماذج) و`feature` (الشاشات). الموارد في app/src/main/res (عربية وvalues-en للفرق فقط)، والقيم في config/app.json وتُحزم في assets عبر app/build.gradle.kts.

## حالة المكونات

| المكون | الحالة | ملاحظة |
|---|---|---|
| هيكل المستودع | تم | بانتظار تحققك من صفحة الاختبارات |
| خط البناء release.yml | تم | ubuntu-24.04 وSDK المثبت، وتخطي التوقيع في بناء الاختبار |
| مشروع Gradle | تم | Kotlin المدمج (AGP 9)، AGP 9.1.1، Gradle 9.3.1، ونوع qa مع مهمة assembleTester |
| الواجهات | تم | ثيم زاهٍ وخلفيات متدرجة لكل الشاشات |
| مقدمة الأنمي | تم في الكود | بوابة تتسع ووميض ذهبي ثم حوار المعلمة نور، وتُعاد من الإعدادات |
| المكتبة السحرية | تم في الكود | 27 كتاباً في شبكة، ولكل كتاب لون ورمز، وفتح ثلاثي الأبعاد |
| تسجيل البيانات | تم في الكود | الاسم والعمر والهدف والفريق في Store، وتعديل من الملف الشخصي والإعدادات |
| النواة (مهام، مال، تمارين، مذكرات) | تم في الكود | حفظ JSON محلي، طاقة ونقاط، بلا مزامنة |
| الحياة اليومية | تم في الكود | ماء ونوم وصلوات وتعليم ومكتبة |
| الخادم PHP | لم يبدأ | المرحلة 5 |
| لوحة التحكم | لم يبدأ | المرحلة 5 |

## المسارات والروابط

- المستودع: https://github.com/yswef/yousuf-toolkit-app
- Release الاختبار السابق: https://github.com/yswef/yousuf-toolkit-app/releases/tag/v0.1.0-test.1
- حالة المشروع: docs/site/dev/index.html
- الاختبارات: docs/site/dev/tests.html
- سجل التعديلات: docs/dev/changes/ ومؤشره docs/dev/CHANGELOG.md
- قواعد الفريق: docs/dev/RULES.md
- فهرس الرموز: docs/dev/SYMBOLS.md
- توثيق الوحدات: docs/dev/modules/ui-phase1.md وcore-phase2.md وdaily-phase3.md وintro.md

## التبعيات والإصدارات

- Android 14 (API 34) كأقل إصدار، compileSdk/targetSdk 36، applicationId com.yousuf.toolkit، versionName 0.3.0
- AGP 9.1.1، Gradle 9.3.1، Java 17، Kotlin المدمج مع compose compiler plugin 2.4.10
- Compose BOM 2026.06.01، Navigation 2.8.0، activity-compose 1.13.0، material-icons-core 1.7.8
- لا مكتبات جديدة: التخزين بـorg.json من أندرويد وكتابة ذرية على Executor واحد، والحركة بـCompose Canvas
- صورة المعلمة نور أصلية مولدة وموجودة في drawable-nodpi/mentor.webp بحجم 182KB
- إعداد القيم يُقرأ من `assets/app.json` (نسخة config/app.json عبر sourceSets)

## المشاكل المفتوحة

- نص الترخيص لم يُحدد، ولا ملف LICENSE بعد
- ملف التوقيع وأسرار GitHub لم تُضف (مطلوبة فقط لبناء beta/release)
- قيم جديدة في config/app.json بانتظار موافقة المالك: نقاط faith وlearn وreading وsleep، وwaterGoal وsleepGoalHours
- مقدمة الأنمي بلا صوت أو فيديو، وتظهر في كل إقلاع (مع تخطي)؛ القرار للمالك
- تسجيل البيانات محلي فقط بلا حساب أو كلمة مرور حتى مرحلة الخادم
- صورة المعلمة نور تحتاج موافقة المالك على الأسلوب قبل النشر
- التخزين ملف JSON واحد بلا Room؛ يكفي للاستخدام الفردي ولا يصلح لمزامنة أو استعلامات كبيرة
- لا اختبارات آلية جديدة؛ التحقق يدوي عبر docs/site/dev/tests-data.js، والبناء والتاج عبر GitHub Actions
- شاشات المرحلة 4 والخادم ما زالت فارغة
