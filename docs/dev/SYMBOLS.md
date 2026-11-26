<div id="yd-home-fab" style="position:fixed;bottom:18px;left:18px;z-index:9999;display:flex;gap:6px"><a href="https://yousef.romoozbasmah.com/" style="background:#6c4cf1;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">🏠 الموقع الأساسي</a><a href="https://yousef.romoozbasmah.com/panel/" style="background:#1d1b29;color:#fff;padding:10px 14px;border-radius:999px;text-decoration:none;font:700 13px system-ui">⚙ لوحة التحكم الأساسية</a></div>

# SYMBOLS

| الرمز | الملف | الوظيفة | يستخدمه |
|---|---|---|---|
| MainActivity | app/src/main/kotlin/com/yousuf/toolkit/MainActivity.kt | نقطة الدخول، ينشئ Store وUiState ويوفر LocalStore ثم يعرض AppNav داخل AppTheme | النظام |
| UiState | app/src/main/kotlin/core/design/UiState.kt | حالة الواجهة في الذاكرة: mode وdark | MainActivity وSettingsScreen وAppTheme |
| UiMode | app/src/main/kotlin/core/design/Theme.kt | نوع الواجهة: PROFESSIONAL أو SIMPLE | UiState وSettingsScreen وAppTheme |
| AppTheme | app/src/main/kotlin/core/design/Theme.kt | ثيم Material فاتح وداكن حسب mode مع لون accent | MainActivity |
| Accent وaccentOf | app/src/main/kotlin/core/design/Skin.kt | ألوان الواجهة الأربعة الفاتحة والداكنة | AppTheme وSkinScreen |
| SmsReader | app/src/main/kotlin/core/device/SmsReader.kt | قراءة رسائل المال بعد الصلاحية واستخراج المبلغ | MoneyScreen |
| Campaign | app/src/main/kotlin/domain/model/Campaign.kt | حملة باسم وهدف أيام وآخر تسجيل | Store وGameScreen |
| SmsMoney | app/src/main/kotlin/domain/model/SmsMoney.kt | حركة مستخرجة من رسالة بمفتاح فريد | SmsReader وStore |
| professionalType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة الاحترافية (Material الافتراضية) | AppTheme |
| simpleType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة البسيطة (أكبر حجماً) | AppTheme |
| Routes | app/src/main/kotlin/core/navigation/Routes.kt | ثوابت مسارات التنقل | NavGraph وHomeScreen |
| AppNav | app/src/main/kotlin/core/navigation/NavGraph.kt | NavHost بين كل الشاشات | MainActivity |
| HomeScreen | app/src/main/kotlin/feature/home/HomeScreen.kt | حاوية بأربعة أقسام وشريط سفلي وزر إضافة سريعة | AppNav |
| PathTab | app/src/main/kotlin/feature/home/PathTab.kt | خريطة الرتب المتحركة وشخصية البطل والحملات | HomeScreen |
| StatsTab | app/src/main/kotlin/feature/home/StatsTab.kt | حلقات وأشرطة ودوائر إحصائية لكل نشاط | HomeScreen |
| ToolsTab | app/src/main/kotlin/feature/home/ToolsTab.kt | كل الكتب السحرية ونسخ البيانات واستيراد CSV | HomeScreen |
| AccountTab | app/src/main/kotlin/feature/home/AccountTab.kt | الملف والرتبة وألوان الواجهة والرموز والإعدادات | HomeScreen |
| QuickAddSheet | app/src/main/kotlin/feature/home/QuickAdd.kt | إضافة سريعة بمخططات ورموز وحقول مختصرة | HomeScreen |
| OnboardingScreen | app/src/main/kotlin/feature/onboarding/OnboardingScreen.kt | شاشة البداية وزر ابدأ، ويثبت onboarded | AppNav |
| SettingsScreen | app/src/main/kotlin/feature/settings/SettingsScreen.kt | تبديل الواجهة والوضع الداكن وإعادة مقدمة الأنمي | AppNav |
| GameScreen | app/src/main/kotlin/feature/game/GameScreen.kt | الرتب والتقدم والحملات اليومية | AppNav |
| SkinScreen | app/src/main/kotlin/feature/skin/SkinScreen.kt | اختيار لون الواجهة ورمز البطل | AppNav |
| LicenseScreen | app/src/main/kotlin/feature/license/LicenseScreen.kt | تفعيل كود الترخيص وإزالته | AppNav |
| DevmodeScreen | app/src/main/kotlin/feature/devmode/DevmodeScreen.kt | تشخيص محلي ونسخ النص | AppNav |
| TestsScreen | app/src/main/kotlin/feature/tests/TestsScreen.kt | فحص الإعدادات والصلاحيات محلياً | AppNav |
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
| FormDialog | app/src/main/kotlin/core/design/FormDialog.kt | لوحة سفلية موحّدة للحقول وزر تأكيد معطّل عند النقص | شاشات الإضافة |
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
| Wallet | app/src/main/kotlin/domain/model/Wallet.kt | محفظة باسم ورصيد | Store وWalletsScreen |
| NotifEntry | app/src/main/kotlin/domain/model/NotifEntry.kt | إشعار محفوظ بعنوان ونص وتاريخ | Store وNotiflogScreen |
| VaultNote | app/src/main/kotlin/domain/model/VaultNote.kt | ملاحظة مشفّرة بحمولة وتاريخ | Store وVaultScreen |
| VaultCrypto | app/src/main/kotlin/core/security/VaultCrypto.kt | تشفير AES-GCM بمفتاح Keystore | Store |
| AppUsage | app/src/main/kotlin/core/device/AppUsage.kt | صلاحية الاستخدام وملخص وقت اليوم | DeviceScreen |
| Access | app/src/main/kotlin/core/device/Access.kt | فحص خدمات الوصول والإشعارات وقائمة التطبيقات | DeviceScreen |
| BlockingAccessibilityService | app/src/main/kotlin/core/device/BlockingAccessibilityService.kt | إعادة التطبيقات المحجوبة للرئيسية | البيان |
| NotificationCollectorService | app/src/main/kotlin/core/device/NotificationCollectorService.kt | حفظ الإشعارات في Store | البيان |
| Api | app/src/main/kotlin/core/net/Api.kt | عميل HTTP لكل نقاط الخادم | DeviceScreen وForum وShop وRaids |
| Platform | app/src/main/kotlin/core/net/Platform.kt | قراءة آخر إصدار من GitHub | UpdaterScreen |
| DeviceScreen | app/src/main/kotlin/feature/device/DeviceScreen.kt | ستة تبويبات أدوات الجهاز والمزامنة | AppNav |
| WalletsScreen | app/src/main/kotlin/feature/wallets/WalletsScreen.kt | محافظ وإيداع وسحب | AppNav |
| CleanerScreen | app/src/main/kotlin/feature/cleaner/CleanerScreen.kt | فحص وتنظيف ذاكرة التطبيق | AppNav |
| ForumScreen | app/src/main/kotlin/feature/forum/ForumScreen.kt | مواضيع وردود المنتدى | AppNav |
| ShopScreen | app/src/main/kotlin/feature/shop/ShopScreen.kt | متجر يخصم رصيد الخادم | AppNav |
| RaidsScreen | app/src/main/kotlin/feature/raids/RaidsScreen.kt | تقدم الغارة وتسجيل الحضور | AppNav |
| NotiflogScreen | app/src/main/kotlin/feature/notiflog/NotiflogScreen.kt | سجل الإشعارات | AppNav |
| UpdaterScreen | app/src/main/kotlin/feature/updater/UpdaterScreen.kt | التحقق من إصدار GitHub | AppNav |
| MagicTabs | app/src/main/kotlin/core/design/Sections.kt | تبويبات موحدة | DeviceScreen |
| InfoCard | app/src/main/kotlin/core/design/Sections.kt | بطاقة تنبيه بمستويات | الشاشات |
| ToggleRow | app/src/main/kotlin/core/design/Sections.kt | صف مفتاح تشغيل | DeviceScreen |
| RowItem | app/src/main/kotlin/core/design/Sections.kt | صف عنوان ووصف وإجراء | DeviceScreen وNotiflog |
| sync_rows | server/src/sync_rows.php | إدخال صفوف العميل دفعة واحدة | sync.php |
| current_user | server/src/auth.php | التحقق من Bearer وإرجاع المستخدم | كل نقاط API المحمية |
