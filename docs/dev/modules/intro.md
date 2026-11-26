<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# مقدمة الأنمي والبوابة

- الحالة: تم
- الحزمة: feature.intro
- المسؤولية: مقدمة افتتاحية بطابع أنمي قبل الدخولية
- الشاشات: IntroScreen
- التبعيات: Compose Canvas وAnimatable وInfiniteTransition فقط
- الإعدادات: 4.6 ثانية، ثم تتسع البوابة وينتهي المشهد بوميض ذهبي، وزر تخطي ظاهر
- الاختبارات: قسم intro في docs/site/dev/tests-data.js
- مشاكل مفتوحة: لا صوت ولا فيديو؛ الحركة مرسومة بالكود، وتظهر في كل إقلاع
