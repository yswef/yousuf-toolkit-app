<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# النواة (المرحلة 2)

- الحالة: تم، بانتظار التحقق اليدوي والتاج
- الحزمة: data.local وdomain.model وfeature (tasks وmoney وworkouts وjournal)
- المسؤولية: المهام والمال والتمارين والمذكرات مع حفظ محلي ونقاط وطاقة
- الشاشات: TasksScreen وMoneyScreen وWorkoutsScreen وJournalScreen
- التبعيات: org.json من أندرويد فقط، بلا Room أو مكتبات إضافية
- الإعدادات: config/app.json (طاقة ونقاط)، والتخزين في filesDir/state.json
- الاختبارات: قسم core في docs/site/dev/tests-data.js
- مشاكل مفتوحة: لا حذف متعدد ولا بحث؛ إزالة المهمة المكتملة لا تعيد النقاط؛ لا مزامنة مع الخادم
