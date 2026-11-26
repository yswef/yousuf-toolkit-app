<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# صفحة الخطة والمراجعة

كل قسم صار صفحة مستقلة بلون خاص، وقائمة جانبية موحدة (تُفتح بزر ☰ على الجوال). الخط أصغر والجداول تتحول إلى بطاقات على الشاشات الصغيرة.

- `plan.html`: الفكرة والقرارات (الرابط القصير القديم يعمل كما هو).
- المراجعة: `compare.html` (المقارنة) و`checklist.html` (قائمة التحقق) و`notes.html` (ملاحظاتي) و`suggestions.html` (اقتراحات) و`review.html` (ملاحظات الاختبار).
- التفاصيل: `features.html` و`refs.html` و`paid.html` و`tech.html` و`admin.html` (لوحة التحكم) و`sources.html`.
- أدوات: `prompts.html` و`steps.html`.
- `plan.css` و`plan.js` و`plan-ui.js`: مشتركة بين كل الصفحات.
- `review-2026-11-20.md`: نص المراجعة الأصلي (يُعرض أيضاً في `review.html`).

التحديد والملاحظات تُحفظ في متصفح الجهاز عبر `localStorage` وتظهر في كل الصفحات. افتح الصفحات من المجلد نفسه (أو من الاستضافة) كي يشاركها المتصفح التخزين.
