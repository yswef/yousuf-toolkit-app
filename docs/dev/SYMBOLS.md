# SYMBOLS

| الرمز | الملف | الوظيفة | يستخدمه |
|---|---|---|---|
| MainActivity | app/src/main/kotlin/MainActivity.kt | نقطة الدخول، ينشئ UiState ويعرض AppNav داخل AppTheme | النظام |
| UiState | app/src/main/kotlin/core/design/UiState.kt | حالة الواجهة في الذاكرة: mode وdark | MainActivity وSettingsScreen وAppTheme |
| UiMode | app/src/main/kotlin/core/design/Theme.kt | نوع الواجهة: PROFESSIONAL أو SIMPLE | UiState وSettingsScreen وAppTheme |
| AppTheme | app/src/main/kotlin/core/design/Theme.kt | ثيم Material الافتراضي (فاتح وداكن) حسب mode | MainActivity |
| professionalType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة الاحترافية (Material الافتراضية) | AppTheme |
| simpleType | app/src/main/kotlin/core/design/Type.kt | خطوط الواجهة البسيطة (أكبر حجماً) | AppTheme |
| Routes | app/src/main/kotlin/core/navigation/Routes.kt | ثوابت مسارات التنقل | NavGraph وHomeScreen |
| AppNav | app/src/main/kotlin/core/navigation/NavGraph.kt | NavHost بين كل الشاشات | MainActivity |
| HomeScreen | app/src/main/kotlin/feature/home/HomeScreen.kt | قائمة الميزات والانتقال إليها | AppNav |
| OnboardingScreen | app/src/main/kotlin/feature/onboarding/OnboardingScreen.kt | شاشة البداية وزر ابدأ | AppNav |
| SettingsScreen | app/src/main/kotlin/feature/settings/SettingsScreen.kt | تبديل الواجهة والوضع الداكن | AppNav |
| GameScreen | app/src/main/kotlin/feature/game/GameScreen.kt | عرض فرق السحر العشرة | AppNav |
| *Screen (26) | app/src/main/kotlin/feature/*/ | شاشة ميزة بحالة فارغة (عنوان من الموارد) | AppNav |
