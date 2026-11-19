# SYMBOLS

| الرمز | الملف | الوظيفة | يستخدمه |
|---|---|---|---|
| MainActivity | app/src/main/kotlin/com/yousuf/toolkit/MainActivity.kt | نقطة الدخول، ينشئ Store وUiState ويوفر LocalStore ثم يعرض AppNav داخل AppTheme | النظام |
| UiState | app/src/main/kotlin/core/design/UiState.kt | حالة الواجهة في الذاكرة: mode وdark | MainActivity وSettingsScreen وAppTheme |
| UiMode | app/src/main/kotlin/core/design/Theme.kt | نوع الواجهة: PROFESSIONAL أو SIMPLE | UiState وSettingsScreen وAppTheme |
| AppTheme | app/src/main/kotlin/core/design/Theme.kt | ثيم Material الافتراضي (فاتح وداكن) حسب mode | MainActivity |
| professionalType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة الاحترافية (Material الافتراضية) | AppTheme |
| simpleType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة البسيطة (أكبر حجماً) | AppTheme |
| Routes | app/src/main/kotlin/core/navigation/Routes.kt | ثوابت مسارات التنقل | NavGraph وHomeScreen |
| AppNav | app/src/main/kotlin/core/navigation/NavGraph.kt | NavHost بين كل الشاشات | MainActivity |
| HomeScreen | app/src/main/kotlin/feature/home/HomeScreen.kt | بطاقة المستوى والنقاط والطاقة وملخص اليوم وقائمة الميزات المجمّعة | AppNav |
| OnboardingScreen | app/src/main/kotlin/feature/onboarding/OnboardingScreen.kt | شاشة البداية وزر ابدأ، ويثبت onboarded | AppNav |
| SettingsScreen | app/src/main/kotlin/feature/settings/SettingsScreen.kt | تبديل الواجهة والوضع الداكن وإعادة مقدمة الأنمي | AppNav |
| GameScreen | app/src/main/kotlin/feature/game/GameScreen.kt | عرض فرق السحر العشرة | AppNav |
| IntroScreen | app/src/main/kotlin/feature/intro/IntroScreen.kt | مقدمة أنمي مرسومة بالكود مع انتقال تلقائي وتخطي | AppNav |
| Store | app/src/main/kotlin/data/local/Store.kt | حالة التطبيق كاملة مع حفظ JSON محلي ونقاط وطاقة وسجلات | كل شاشات feature |
| LocalStore | app/src/main/kotlin/data/local/Store.kt | CompositionLocal لتوفير Store | MainActivity وAppNav والشاشات |
| AppConfig | app/src/main/kotlin/core/config/AppConfig.kt | قراءة الطاقة والنقاط والأهداف من assets/app.json | Store |
| Task | app/src/main/kotlin/domain/model/Task.kt | مهمة بعنوان وملاحظة وأولوية وحالة | Store وTasksScreen |
| Ledger | app/src/main/kotlin/domain/model/Ledger.kt | حركة مالية دخل أو مصروف بتصنيف | Store وMoneyScreen |
| Workout | app/src/main/kotlin/domain/model/Workout.kt | تمرين بنوع ومدة | Store وWorkoutsScreen |
| JournalEntry | app/src/main/kotlin/domain/model/JournalEntry.kt | مذكرة بعنوان ونص وتاريخ | Store وJournalScreen |
| SleepLog | app/src/main/kotlin/domain/model/Daily.kt | تسجيل ساعات نوم | Store وSleepScreen |
| LearnItem | app/src/main/kotlin/domain/model/Daily.kt | موضوع تعلم بدقائق مستهدفة ومنجزة | Store وLearnScreen |
| Book | app/src/main/kotlin/domain/model/Daily.kt | كتاب بتقدم صفحات | Store وLibraryScreen |
| prayerKeys | app/src/main/kotlin/domain/model/Daily.kt | معرفات الصلوات الخمس | Store وFaithScreen |
| ScreenScaffold | app/src/main/kotlin/core/design/ScreenScaffold.kt | شريط عنوان وزر رجوع موحد لكل الشاشات | شاشات feature |
| EmptyState | app/src/main/kotlin/core/design/ScreenScaffold.kt | نص حالة فارغة في الوسط | الشاشات |
| FormDialog | app/src/main/kotlin/core/design/FormDialog.kt | نافذة نموذج بحقول وزر تأكيد معطّل عند النقص | شاشات الإضافة |
| TasksScreen | app/src/main/kotlin/feature/tasks/TasksScreen.kt | إضافة المهام وإنجازها وحذفها مع طاقة ونقاط | AppNav |
| MoneyScreen | app/src/main/kotlin/feature/money/MoneyScreen.kt | رصيد وحركات دخل ومصروف بتصنيفات | AppNav |
| WorkoutsScreen | app/src/main/kotlin/feature/workouts/WorkoutsScreen.kt | تسجيل التمارين وإجمالي الدقائق | AppNav |
| JournalScreen | app/src/main/kotlin/feature/journal/JournalScreen.kt | كتابة المذكرات وعرضها وحذفها | AppNav |
| WaterScreen | app/src/main/kotlin/feature/water/WaterScreen.kt | أكواب الماء اليومية والهدف وآخر 7 أيام | AppNav |
| SleepScreen | app/src/main/kotlin/feature/sleep/SleepScreen.kt | تسجيل النوم والهدف والمعدل | AppNav |
| FaithScreen | app/src/main/kotlin/feature/faith/FaithScreen.kt | صلوات اليوم الخمس والأيام الكاملة | AppNav |
| LearnScreen | app/src/main/kotlin/feature/learn/LearnScreen.kt | مواضيع تعلم ودقائق وأهداف | AppNav |
| LibraryScreen | app/src/main/kotlin/feature/library/LibraryScreen.kt | كتب وتقدم قراءة الصفحات | AppNav |
| *Screen (17) | app/src/main/kotlin/feature/*/ | شاشة ميزة بحالة فارغة (عنوان من الموارد) | AppNav |
| MagicBackground | app/src/main/kotlin/core/design/MagicBackground.kt | خلفية متدرجة بنفسجية فيروزية مع نجوم لكل الشاشات | ScreenScaffold وHomeScreen |
| BookSymbol | app/src/main/kotlin/core/design/MagicSymbol.kt | رموز أغلفة الكتب: نجمة وقمر وقطرة وشعلة وورقة وكرة وماسة ورونة | Books وMagicBooks وOnboardingScreen |
| drawBookSymbol | app/src/main/kotlin/core/design/MagicSymbol.kt | رسم رمز الكتاب داخل Canvas | MagicBookCard وMagicBookOpen وأغلفة الملف |
| ProfileFields | app/src/main/kotlin/core/design/ProfileFields.kt | حقول الاسم والعمر والهدف والفريق | RegisterScreen وProfileScreen |
| teamKeys وteamLabel | app/src/main/kotlin/core/design/Choices.kt | معرفات فرق السحر وتسمياتها | RegisterScreen وProfileScreen |
| goalKeys وgoalLabel | app/src/main/kotlin/core/design/Choices.kt | أهداف البطل وتسمياتها | RegisterScreen وProfileScreen |
| MagicBook | app/src/main/kotlin/feature/home/Books.kt | كتاب ميزة: المسار والعنوان والألوان والرمز | HomeScreen وMagicBookCard |
| bookSections | app/src/main/kotlin/feature/home/Books.kt | أقسام الكتب الـ27 وألوانها | HomeScreen |
| MagicBookCard | app/src/main/kotlin/feature/home/MagicBooks.kt | غلاف كتاب ثلاثي الأبعاد في الشبكة | HomeScreen |
| MagicBookOpen | app/src/main/kotlin/feature/home/MagicBooks.kt | أنيميشن فتح الكتاب والبوابة قبل الانتقال | HomeScreen |
| RegisterScreen | app/src/main/kotlin/feature/onboarding/RegisterScreen.kt | تسجيل بيانات البطل وحفظها | AppNav |
| ProfileScreen | app/src/main/kotlin/feature/profile/ProfileScreen.kt | بطاقة البطل وتعديل البيانات | AppNav وSettingsScreen |
| saveProfile | app/src/main/kotlin/data/local/Store.kt | حفظ الاسم والعمر والهدف والفريق وتاريخ الانضمام | RegisterScreen وProfileScreen |
| hasProfile | app/src/main/kotlin/data/local/Store.kt | هل بيانات البطل موجودة | AppNav |
| mentor.webp | app/src/main/res/drawable-nodpi/mentor.webp | صورة المعلمة نور | OnboardingScreen وRegisterScreen وProfileScreen |
