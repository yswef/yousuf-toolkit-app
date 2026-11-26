<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تعديل

- الموديل: arena-agent
- الميزة: المكتبة السحرية وتجربة الدخول وتسجيل البيانات
- التاريخ: 2026-11-19
- الملفات المتأثرة: core/design (Theme وMagicBackground وScreenScaffold وChoices وProfileFields وMagicSymbol)، feature/home (HomeScreen وBooks وMagicBooks)، feature/onboarding (OnboardingScreen وRegisterScreen)، feature/profile (ProfileScreen)، feature/intro، feature/settings، data/local/Store.kt، core/navigation (Routes وNavGraph)، res/values/strings.xml وdrawable-nodpi/mentor.webp، docs (هذا الملف وCHANGELOG وmodules وSYMBOLS وtests-data وstatus-data وREADME وhandover_state وuser)

## ما تم

- ثيم جديد بألوان الشعار: بنفسجي ساطع، ذهبي، فيروزي، وخلفيات متدرجة بدل الأبيض، مع وضع داكن كوني.
- الرئيسية صارت مكتبة كتب سحرية في شبكة عمودين، لكل كتاب تدرج لونه ورمز سحري وغلاف بحدود ذهبية، مع بطاقة بطل وملخص اليوم.
- فتح الكتاب: نافذة كاملة، الكتاب يتقدم ويتكبر، يفتح غلافه بتدوير ثلاثي الأبعاد، مع حلقة سحرية وشرارات، ثم ينتقل إلى الميزة.
- الدخولية: مقدمة البوابة تتسع وتنتهي بوميض ذهبي، ثم مشهد المعلمة نور بصورة أنمي كاملة الخلفية، وأربعة أسطر حوار بتأثير الكتابة التدريجية، مع زر تخطي.
- تسجيل البيانات: شاشة لبيانات البطل (الاسم، العمر، الهدف الأول، الفريق السحري) تُحفظ محلياً، ويظهر الاسم في الرئيسية.
- الملف الشخصي: بطاقة بطل تعرض البيانات والمستوى والنقاط، مع تعديل البيانات من الشاشة أو من الإعدادات.
