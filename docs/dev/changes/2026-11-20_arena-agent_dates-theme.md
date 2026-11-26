<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# تواريخ العمليات ومظهر الجهاز

- المنفذ: Arena agent
- التاريخ: 2026-11-20
- الملفات: Store.kt، MoneyScreen.kt، MainActivity.kt، UiState.kt، SettingsScreen.kt، strings.xml.

تتيح إضافة الحركة اختيار اليوم أو أمس أو قبل أمس أو تاريخ آخر، وتُحفظ مع تاريخ الحركة في صيغة البيانات الموجودة. يُرفض المبلغ غير المحدود أو غير الصالح. يتبع المظهر إعداد الجهاز افتراضياً ويتغير معه؛ يستطيع المستخدم إلغاء التتبع واختيار المظهر، ويُحفظ اختياره محلياً. لا يشمل ذلك تدقيق تباين جميع الشاشات.

## الاختبار المطلوب على جهاز

إضافة حركات لكل اختيار وتاريخ مخصص ثم إغلاق التطبيق وفتحه والتحقق من الترتيب والتاريخ، وتجربة إلغاء منتقي التاريخ. تغيير مظهر النظام أثناء فتح التطبيق، ثم تعيين مظهر يدوي وإعادة التشغيل والعودة إلى اتباع النظام. فُحص ملف الموارد XML وتكرار أسمائها وdiff فقط؛ لم يُبن المشروع لغياب Java وAndroid SDK في البيئة.
