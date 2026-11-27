<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# قواعد العمل

## 1. قبل أي تعديل

- اقرأ `handover_state.md` أولاً.
- لا تخمّن قيمة ناقصة؛ اسأل المالك.
- ميزة واحدة واضحة في كل جلسة.
- الرد قصير ومباشر، وفي آخره سطر «التحقق:».

## 2. الكود

- الطبقات: `core`, `data`, `domain`, `feature`.
- لا روابط ولا مفاتيح داخل الكود؛ كل إعداد من `config`.
- النصوص التي تظهر للمستخدم في ملفات الموارد.
- لا مكتبة كبيرة من أجل دالة صغيرة.
- لا تعليقات داخل الكود، **إلا توقيع المطوّر في بداية ونهاية كل ملف**؛ هذا استثناء صريح.
- رسائل السجل والأخطاء بالإنجليزية.

## 3. الواجهة

- عربية وRTL أولاً.
- بلا نصوص وهمية ولا بيانات وهمية في الإصدار النهائي.
- بلا تدرجات نيون مفرطة ولا بطاقات متطابقة بلا معنى.

## 4. Git والإصدارات

- `main` مستقر، و`dev` لكل العمل.
- البناء عند دفع tag فقط.
- `tester` يسمح بوضع المطور؛ `release` للمستخدمين.
- حدّث `docs/site/dev/status-data.js` عند كل إصدار مهم.

## 5. التوثيق

- الوثائق الفعّالة فقط: `README.md`, `handover_state.md`, `docs/dev/RULES.md`, `BUILD_LOCAL.md`, `ASSETS.md`, `THIRD_PARTY.md`, `docs/plan/`, `docs/site/`, `docs/diagnostics/`, `docs/mother-panel/`.
- لا سجلات تعديل قديمة ولا ملفات وحدات قديمة ولا معاينات تصميم قديمة.
- `docs/word/` للمراجعة فقط ولا يدخل Git.

## 6. الخادم والبيانات

- قاعدة بيانات واحدة، وجدول `users` واحد لكل التطبيقات.
- لا جدول مستخدمين خاص بتطبيق، ولا تكرار للجداول الموجودة.
- الجداول الجديدة غير المكررة مسموح بها.
- الموقع PHP على Hostinger؛ مبدأ «أوفلاين أولاً» ملغى.
- الروابط من `config/environments.*.json` وإعداد الخادم من `server/config/config.php`.
<!-- توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com -->
