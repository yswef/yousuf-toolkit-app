<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# مساحة مراجعة داخل صفحة الخطة

## ما تغيّر

- إضافة قائمة تحقق لكل تطبيق مرجعي: زر «حمّلت وجرّبت»، زر «قارنت»، ومربع لكل ميزة استوحيناها.
- إضافة قائمة تحقق لمميزات التطبيق الأخرى مجمعة حسب القسم.
- إضافة تبويب المدفوعة: الميزات المرتبطة بالدفع أو الشراء أو النقاط، واختيار ما يبقى مدفوعاً، وملاحظة أسعار.
- إضافة تبويب الاقتراحات: عنوان ووصف وقسم، وحفظ ونسخ وحذف.
- إضافة تبويب ملاحظاتي: تصنيف وقسم ونص لكل ملاحظة، ترتيب تلقائي (واجهة، مشكلة، ناقص، مدفوع، اقتراح)، ونسخ الكل.
- حذف تبويبي الأسئلة والمتطلبات من التنقل وإخفاء محتواهما.
- شطب الخطوات المكتملة في قسم الخطوات.
- إضافة `plan.html` كرابط قصير، وربط الصفحة بمؤشر تقدم قائمة التحقق.

## الحفظ

`localStorage` في المتصفح: `yousef-plan-checklist-v1` و`yousef-plan-paid-v1` و`yousef-plan-notes-v1` و`yousef-plan-suggestions-v1`.

## الاختبار

فتح `docs/plan/plan.html`، تبديل الأقسام، تعليم عناصر، إضافة ملاحظة واقتراح، ثم إعادة فتح الصفحة للتأكد من بقاء البيانات.
