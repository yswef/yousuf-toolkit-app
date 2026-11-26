<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# خطة التطبيق

صفحتان فقط:

- `index.html`: شرح بسيط كأنه لطفل، وملخص آخر تحديثات `0.18.0`.
- `feedback.html`: صفحة واحدة للاقتراحات والملاحظات. الاقتراح له «اسم» و«وصف» فقط. الملاحظة لها «مكان المشكلة» و«النص» فقط.

`plan.html` مجرد تحويل للرابط القديم إلى `index.html`. `plan.css` و`plan.js` مشتركان بين الصفحتين. البيانات تُحفظ في `localStorage` تحت `yd-plan-feedback-v1`، مع ترحيل تلقائي للملاحظات والاقتراحات القديمة دون الاحتفاظ بالقسم القديم.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
