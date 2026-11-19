# تعديل

- الموديل: arena-agent
- الميزة: النواة (المرحلة 2) والحياة اليومية (المرحلة 3) ومقدمة الأنمي
- التاريخ: 2026-11-19
- الملفات المتأثرة: app/build.gradle.kts، config/app.json، AndroidManifest.xml، MainActivity.kt، core/config/AppConfig.kt، core/design (ScreenScaffold وFormDialog)، core/navigation (Routes وNavGraph)، data/local/Store.kt، domain/model (Task وLedger وWorkout وJournalEntry وDaily)، feature (intro وhome وonboarding وsettings وtasks وmoney وworkouts وjournal وwater وsleep وfaith وlearn وlibrary)، res/values/strings.xml، docs (هذا الملف وCHANGELOG وmodules الثلاثة وSYMBOLS وtests-data وstatus-data وREADME وhandover_state)

## ما تم

- مقدمة أنمي عند الإقلاع: خلفية كونية، دائرة سحرية دوارة، نجوم، ظهور متدرج للعنوان، شريط تقدم وتخطي، ثم البداية أو الرئيسية. يمكن إعادتها من الإعدادات.
- طبقة بيانات محلية: `data/local/Store.kt` يحفظ كل الحالة في `filesDir/state.json` كتابة ذرية على خيط منفصل، ويقرأ الإعداد من `config/app.json` عبر assets.
- المرحلة 2: المهام (أولوية، إنجاز، طاقة، نقاط)، المال (دخل ومصروف وتصنيفات ورصيد)، التمارين (نوع ومدة)، المذكرات.
- المرحلة 3: الماء (هدف يومي وسجل أسبوعي)، النوم (تسجيل وسجل ومعدل)، الإيمان (الصلوات الخمس وأيام كاملة)، التعلم (مواضيع ودقائق)، المكتبة (كتب وتقدم صفحات).
- الرئيسية: بطاقة مستوى ونقاط وطاقة، ملخص اليوم، وميزات مجمّعة في أقسام.
- القيم الجديدة في config/app.json (نقاط الإيمان والتعلم والقراءة والنوم، وهدفا الماء والنوم) بانتظار موافقة المالك.
