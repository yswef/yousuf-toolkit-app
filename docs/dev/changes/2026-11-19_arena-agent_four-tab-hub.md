<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# الواجهة الموحّدة والأقسام الأربعة

## ما تغيّر

- الرئيسية صارت أربعة أقسام في شريط سفلي: المسار، الإحصائيات، الأدوات، حسابي.
- المسار: خريطة متعرجة برتب، منصات مجسّمة، شخصية بطل متحركة، وحملات مع تسجيل سريع.
- الإحصائيات: حلقة إنجاز اليوم، أشرطة نشاط الأسبوع، دائرة المال، السلسلة، وبطاقات لكل نشاط.
- الأدوات: كل الكتب السحرية، ونسخ احتياطي JSON، واستعادة، واستيراد مالية ومهام من CSV.
- حسابي: الملف والرتبة والطاقة، وألوان الواجهة والرموز مباشرة، والوضع الداكن والواجهة.
- زر إضافة عائم يفتح لوحة سريعة: مهمة، مال، ماء، تمرين، مذكرة، حملة.
- كل نماذج الإضافة صارت لوحة سفلية بدل نافذة عادية.

## الملفات

`HomeScreen.kt`, `PathTab.kt`, `StatsTab.kt`, `ToolsTab.kt`, `AccountTab.kt`, `QuickAdd.kt`,
`FormDialog.kt`, `Store.kt`, `strings.xml`.

## الاختبار

من `docs/site/dev/tests.html` مجموعة `hub`، أو يدوياً: تنقّل بين الأقسام الأربعة، أضف من الزر العائم،
صدّر نسخة ثم استعدها، واستورد ملف CSV بسيطاً.
