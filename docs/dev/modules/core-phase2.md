# النواة (المرحلة 2)

- الحالة: تم، بانتظار التحقق اليدوي والتاج
- الحزمة: data.local وdomain.model وfeature (tasks وmoney وworkouts وjournal)
- المسؤولية: المهام والمال والتمارين والمذكرات مع حفظ محلي ونقاط وطاقة
- الشاشات: TasksScreen وMoneyScreen وWorkoutsScreen وJournalScreen
- التبعيات: org.json من أندرويد فقط، بلا Room أو مكتبات إضافية
- الإعدادات: config/app.json (طاقة ونقاط)، والتخزين في filesDir/state.json
- الاختبارات: قسم core في docs/site/dev/tests-data.js
- مشاكل مفتوحة: لا حذف متعدد ولا بحث؛ إزالة المهمة المكتملة لا تعيد النقاط؛ لا مزامنة مع الخادم
