<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# الدخولية وتسجيل البيانات

- الحالة: تم في الكود، بانتظار التحقق والتاج
- الحزمة: feature.onboarding وfeature.profile وcore.design
- المسؤولية: حوار المعلمة نور ثم تسجيل بيانات البطل وعرضها وتعديلها
- الشاشات: OnboardingScreen وRegisterScreen وProfileScreen
- التبعيات: صورة mentor.webp وCompose فقط
- الإعدادات: البيانات في Store ضمن state.json (الاسم والعمر والهدف والفريق وتاريخ الانضمام)
- الاختبارات: قسم design في docs/site/dev/tests-data.js
- مشاكل مفتوحة: لا حساب سحابي ولا كلمة مرور؛ التسجيل محلي حتى مرحلة الخادم
