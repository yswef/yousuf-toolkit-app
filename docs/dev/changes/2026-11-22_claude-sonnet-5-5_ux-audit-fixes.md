<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تدقيق تجربة المستخدم وإصلاحات المنطق

- المنفذ: Claude Sonnet 5.5
- التاريخ: 2026-11-22
- الملفات: Store.kt، Theme.kt، Sections.kt، Surfaces.kt (جديد)، HomeScreen.kt، TodayTab.kt، PermissionsSection.kt، strings_perms.xml (جديد)، PdfReaderScreen.kt، AdhkarScreen.kt، MoneyProScreen.kt، LifeScreen.kt.

## المنطق
- `Store.save`: كتابة مع مزامنة القرص ثم نقل ذري، بدل حذف الملف وإعادة التسمية.
- `Store.read`: تعيد نجاحاً أو فشلاً. عند تلف `state.json` يُنسخ إلى `state.corrupt.json` قبل أن تكتب الحالة الافتراضية فوقه.
- `Store.restoreBackup`: تعيد false وتسترجع الحالة السابقة إذا فشلت القراءة، وكانت تعيد true بعد استعادة جزئية.
- `Store.toggleTask`: التراجع عن إنجاز مهمة متكررة يحذف النسخة التالية المولّدة منها (أول مهمة مفتوحة بنفس العنوان والتكرار والقائمة ومعرّفها أكبر).
- `Store.nextId`: متزامنة، لأن خدمة الإشعارات تكتب من خيط آخر.
- `importMoneyCsv` و`importTaskCsv`: تحليل أعمدة يدعم الحقول المقتبسة، فلا يتشظى مبلغ مثل "1,500".

## الواجهات
- `Theme.kt`: لون `outline` أغمق لتباين حدود الحقول، وأشكال مدورة موحدة، و`primaryContainer` مشتق من لون الهوية المختار.
- `Surfaces.kt`: `AppCard` و`StatTile`.
- `Sections.kt`: التبويبات والصفوف بحد أدنى للمس 48 و56dp مع أدوار وصول، و`ToggleRow` يتفاعل بالصف كله، و`InfoCard` بأيقونة، و`SectionTitle` بدلالة عنوان.
- `HomeScreen.kt`: زر الرجوع من تبويب غير اليوم يعود إلى اليوم بدل الخروج.
- `TodayTab.kt`: أزيل لون ثابت من التدرج، ونص المستوى بلون `onPrimary` ليقرأ في الوضع الداكن، و`MiniStat` يستخدم `StatTile`.
- `PdfReaderScreen.kt`: كان بلا شريط علوي ولا رجوع، والرسم والنسخ على الخيط الرئيسي. صار داخل `ScreenScaffold` والعمل على IO.
- `PermissionsSection.kt`: النصوص نُقلت إلى `strings_perms.xml` حسب القاعدة 3 في RULES.md.
- إزالة `!!` من أربعة مواضع، وإضافة مفاتيح لثلاث قوائم في `LifeScreen.kt`.

## لم يُمس
`IntroScreen.kt`، `OnboardingScreen.kt`، `RegisterScreen.kt`، `Type.kt`، `NavGraph.kt`، `MentorVoice.kt`، `MagicSymbol.kt`، `mentor.webp`، `nour_intro.mp3`، `nour_rank.mp3`، `strings.xml`، `values-en/strings.xml` (بصمة SHA-256 مطابقة للأصل).

## الاختبار المطلوب على جهاز
بناء `./gradlew assembleDebug` أولاً. ثم: تعديل `state.json` يدوياً ليصبح JSON غير صالح وفتح التطبيق، والتحقق من ظهور `state.corrupt.json`. استعادة نسخة فاسدة ثم نسخة سليمة. إنجاز مهمة يومية ثم التراجع والتحقق من عدم تكرارها. زر الرجوع من تبويب المهام. فتح قارئ PDF بملف كبير. تبديل لون الهوية إلى الذهبي والتحقق من ألوان الحاويات. التحقق من الـ Intro والشخصية الافتتاحية بصرياً.
فُحص فقط: صياغة Kotlin (tree-sitter) وأسماء الموارد وبصمات الملفات المحمية. لم يُبن المشروع لغياب Android SDK.
