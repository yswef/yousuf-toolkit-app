<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# واجهات أصلية — الدفعة 1

- الموديل: claude-sonnet-5-5
- الميزة: نظام تصميم جديد (v3) وهيكل الرئيسية وتبويب اليوم
- التاريخ: 2026-11-22
- الملفات المتأثرة:
  - جديد: `app/src/main/kotlin/core/design/AppIcons.kt`
  - جديد: `app/src/main/kotlin/core/design/Native.kt`
  - جديد: `app/src/main/res/values/strings_native.xml`
  - معدّل: `app/src/main/kotlin/feature/home/HomeScreen.kt`
  - معدّل: `app/src/main/kotlin/feature/home/TodayTab.kt`
  - جديد (تصميم): `docs/design/mockups/v3/` (v3.css وicons.js و7 صفحات)

## السبب

طلب المالك إعادة تصميم كل الواجهات لتكون أسهل وأقرب لتطبيقات أندرويد الأصلية مع إبقاء المقدمة (`feature/intro`) كما هي. التصميم يُعتمد بصرياً في `docs/design/mockups/v3` أولاً ثم يُنقل إلى Compose بنفس القيم.

## ما تغيّر

1. **AppIcons.kt**: أيقونات تُبنى من نفس مسارات SVG الموجودة في `v3/icons.js` عبر `addPathNodes`، فلا حاجة لمكتبة `material-icons-extended`. `ChevronEnd` بخاصية `autoMirror` لتتجه للنهاية في RTL.
2. **Native.kt**: مكونات مشتركة تُعاد استخدامها في بقية الشاشات:
   - `ProgressRing`: حلقة تقدم متحركة، تبدأ من الأعلى وتدور عكس عقارب الساعة (اتجاه RTL).
   - `CircleCheck`: دائرة إنجاز بحجم لمس 48dp.
   - `GroupCard` و`GroupDivider`: قائمة مجمّعة بسطح `surfaceContainer` وزوايا 24dp وفواصل داخلية بدل بطاقة لكل صف.
   - `ListSectionHeader`: عنوان قسم مع إجراء اختياري.
   - `HomeHeader`: رأس الرئيسية: أفاتار الاسم يفتح الحساب، العنوان، ثم الإعدادات. يطبّق `statusBarsPadding` لأن `enableEdgeToEdge()` مفعّل.
3. **HomeScreen.kt**: استبدال `TopAppBar` بـ`HomeHeader`، وشريط التنقل بسطح `surfaceContainer` وخط عريض للتبويب النشط، وزر الإضافة صار ممتداً بنص «إضافة»، وأيقونة تبويب الأدوات صارت `AppIcons.Apps`. لم يُمس منطق التبويبات ولا `QuickAddSheet`.
4. **TodayTab.kt**: بطاقة تقدم (حلقة المهام المنجزة + المستوى والرتبة + شريط الرتبة)، مهام اليوم كقائمة مجمّعة بدائرة إنجاز، العادات الثلاث كبلاطات بحلقات تقدم، وصف «الإحصائيات» للانتقال لشاشة الإحصاء بدل `MiniStat` الذي أُزيل.
5. **strings_native.xml**: ثلاثة نصوص جديدة فقط: `today_hero_title` و`today_level_rank` و`habit_plus`.

## كيف يعمل المنطق الجديد

لا منطق جديد. كل استدعاءات `Store` كما كانت: `toggleTask` (وفشلها يفتح حوار الطاقة)، `addWater(1)`، `togglePrayer` لأول صلاة غير مؤداة، وترتيب المهام نفسه (أقرب موعد ثم الأعلى أولوية، أول 3). حلقة المهام = `done / tasks.size`، والمجموع صفر يعطي صفراً دون قسمة.

## ما أُزيل من الواجهة

- بطاقات `MiniStat` الثلاث (مفتوحة/منجزة/نسبة): ملخصها صار في حلقة البطاقة العلوية.
- زر إضافة صلاة/نوم الصغير: النوم يُفتح بالضغط على البلاطة، والصلاة بزر ✓ على البلاطة.
- النقاط بالـXP في البطاقة العلوية: ما زالت في شاشة المسار.

## التحقق

لم يُبنَ التطبيق ولم يُشغَّل (لا Android SDK ولا شبكة في بيئة التنفيذ). فُحص فقط توازن الأقواس، ومطابقة أسماء `Store` والنصوص مع الكود الحالي، وعُرضت الـmockups في متصفح حقيقي بمقاس 412×915 فاتحاً وداكناً.

## المطلوب من المالك

1. `./gradlew assembleDebug` وإرسال أي خطأ ترجمة كما هو.
2. فتح التطبيق على جهاز وفحص: التبويبات الأربعة، سحب الشريط، الوضع الداكن، خط كبير، وتبويب اليوم بلا مهام وبمهام.

## القادم

التبويبات الثلاثة (مهام ومال وأدوات) ثم الحساب والإعدادات ثم بقية الشاشات عبر المكونات المشتركة نفسها.
