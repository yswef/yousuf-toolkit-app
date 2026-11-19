# المراحل 5 إلى 7

## ما أُضيف

- رتب مشتقة من المستوى مع شريط تقدم واسم الرتبة التالية.
- حملات يومية: اسم وعدد أيام، تسجيل واحد كل يوم، وحذف.
- أربعة ألوان واجهة وثمانية رموز بطل محفوظة محلياً.
- ترخيص مرتبط بالحساب عبر `POST /api/license.php` مع أداة توليد `tools/code.php`.
- استيراد حركات المال من رسائل الجهاز بعد موافقة المستخدم.
- صفحة فحص محلي للإعدادات والصلاحيات.
- أيقونة Y-Dimension: خلفية `#121025`، ياء ذهبية `#C09843`، قوس وماسة بنفسجية `#755DCF`، مع صور لكل الكثافات.
- توقيع اختبار ثابت في `ci/test-signing.p12` حتى تُثبّت التحديثات فوق بعضها.
- إعداد النشر على `yousef.romoozbasmah.com` في `server/HOSTINGER.md`.

## الملفات

`GameScreen.kt`, `SkinScreen.kt`, `LicenseScreen.kt`, `DevmodeScreen.kt`, `TestsScreen.kt`,
`MoneyScreen.kt`, `SmsReader.kt`, `Skin.kt`, `Campaign.kt`, `SmsMoney.kt`, `Store.kt`,
`Theme.kt`, `MagicBackground.kt`, `NavGraph.kt`, `AppConfig.kt`, `config/*.json`,
`ic_launcher_foreground.xml`, `mipmap-*`, `server/*`, `docs/site/dev/*-data.js`.

## الاختبار

من `docs/site/dev/tests.html` مجموعة `game` و`style` و`insights`، أو يدوياً:
اللعبة > حملة جديدة > تسجيل اليوم؛ الإعدادات > المظهر والرمز؛ المال > من الرسائل.

## الأثر

لا تغيير في صيغة البيانات المحفوظة، وتُقرأ المفاتيح الجديدة بقيم افتراضية عند غيابها.
