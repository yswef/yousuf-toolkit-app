<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# خطة التطبيق

صفحتان فقط:

- `index.html`: شرح بسيط كأنه لطفل، وملخص آخر تحديثات `0.19.0`.
- `feedback.html`: صفحة واحدة جميلة للاقتراحات والملاحظات. الاقتراح له «اسم» و«وصف» فقط. الملاحظة لها «مكان المشكلة» و«النص» وصور اختيارية. لا يوجد حقل «المسار».

نافذة صغيرة تطلب النوع والخيار الثاني والاسم، ثم «بدء التسجيل» يفتح محرراً كبيراً قابلاً للتكبير مع قوائم مرقمة ونقطية وأبجدية ومستويات متعددة. المدخلات تُعرض في صناديق كود، ويمكن تعديلها أو حذفها أو حذف الكل. زر التنزيل ينتج ZIP بلا مكتبات خارجية.

تم اختيار التخطيط A: مجلد لكل مدخل فيه `note.md` وصوره، و`all-notes.md` في الجذر. المدخلات التي معها صور يظهر في الجذر اسم الميزة ومسار المجلد فقط. أسماء الصور «الأولى» و«الثانية» وهكذا، وتظهر مع معاينة وحذف في الموقع. فوق الإدخال يوجد برومت جاهز للنسخ مع الملف.

`plan.html` مجرد تحويل للرابط القديم إلى `index.html`. `plan.css` و`plan.js` و`feedback-zip.js` مشتركة بين الصفحتين. البيانات تُحفظ في `localStorage` تحت `yd-plan-feedback-v2` مع ترحيل تلقائي من `yd-plan-feedback-v1` والملاحظات والاقتراحات القديمة. حجم خط الموقع مكبّر. التصنيف `[عام]` مقبول للملاحظات القادمة، ولا نطلب تصنيفاً أدق.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
