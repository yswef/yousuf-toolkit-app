package core.navigation

import androidx.compose.animation.fadeOut
import androidx.compose.animation.fadeIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import core.design.UiState
import data.local.LocalStore
import feature.accounts.AccountsScreen
import feature.adhkar.AdhkarScreen
import feature.alarm.AlarmScreen
import feature.blocker.BlockerScreen
import feature.cleaner.CleanerScreen
import feature.contacts.ContactsScreen
import feature.device.DeviceScreen
import feature.diagnostics.DiagnosticsScreen
import feature.devicepro.DeviceProScreen
import feature.devmode.DevmodeScreen
import feature.faith.FaithScreen
import feature.firewall.FirewallScreen
import feature.forum.ForumScreen
import feature.apps.AppsScreen
import feature.auth.AuthGateScreen
import feature.game.GameScreen
import feature.git.GitScreen
import feature.home.AccountScreen
import feature.home.HomeScreen
import feature.home.PathScreen
import feature.home.StatsScreen
import feature.intro.IntroScreen
import feature.journal.JournalScreen
import feature.learn.LearnScreen
import feature.life.LifeScreen
import feature.library.LibraryScreen
import feature.license.LicenseScreen
import feature.media.MediaVaultScreen
import feature.money.MoneyScreen
import feature.moneypro.MoneyProScreen
import feature.notiflog.NotiflogScreen
import feature.onboarding.OnboardingScreen
import feature.onboarding.RegisterScreen
import feature.profile.DevicesScreen
import feature.profile.ProfileScreen
import feature.reader.FileManagerScreen
import feature.reader.PdfReaderScreen
import feature.raids.RaidsScreen
import feature.settings.SettingsScreen
import feature.settings.VisibilityScreen
import feature.shop.ShopScreen
import feature.skin.SkinScreen
import feature.sleep.SleepScreen
import feature.tasks.TasksScreen
import feature.taskspro.TasksProScreen
import feature.tests.TestsScreen
import feature.updater.UpdaterScreen
import feature.vault.VaultScreen
import feature.wallets.WalletsScreen
import feature.water.WaterScreen
import feature.workouts.WorkoutsScreen

@Composable
fun AppNav(ui: UiState, nav: NavHostController = rememberNavController()) {
    val store = LocalStore.current
    val spec = tween<Float>(300)
    NavHost(
        navController = nav,
        startDestination = if (store.authToken.isBlank() || !store.onboarded) Routes.AUTH else Routes.INTRO,
        enterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(320), initialOffset = { it / 3 }) + fadeIn(spec)
        },
        exitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(320), targetOffset = { it / 6 }) + fadeOut(tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(320), initialOffset = { it / 6 }) + fadeIn(spec)
        },
        popExitTransition = {
            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(320), targetOffset = { it / 3 }) + fadeOut(tween(200))
        }
    ) {
        composable(Routes.INTRO) {
            IntroScreen(onFinish = {
                val target = if (store.onboarded && store.hasProfile) Routes.HOME else Routes.ONBOARDING
                nav.navigate(target) {
                    popUpTo(Routes.INTRO) { inclusive = true }
                }
            })
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(onRegister = { nav.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(
                onBack = { nav.popBackStack() },
                onDone = {
                    nav.navigate(Routes.HOME) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onFeature = { nav.navigate(it) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                onAccount = { nav.navigate(Routes.ACCOUNT) }
            )
        }
        composable(Routes.STATS) {
            StatsScreen(onBack = { nav.popBackStack() })
        }
        composable(Routes.PATH) {
            PathScreen(onBack = { nav.popBackStack() }, onFeature = { nav.navigate(it) })
        }
        composable(Routes.ACCOUNT) {
            AccountScreen(
                onBack = { nav.popBackStack() },
                onFeature = { nav.navigate(it) },
                onSettings = { nav.navigate(Routes.SETTINGS) },
                ui = ui,
                onLogout = {
                    nav.navigate(Routes.AUTH) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                ui = ui,
                onBack = { nav.popBackStack() },
                onReplayIntro = {
                    nav.navigate(Routes.INTRO) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                },
                onProfile = { nav.navigate(Routes.PROFILE) },
                onSkin = { nav.navigate(Routes.SKIN) },
                onLicense = { nav.navigate(Routes.LICENSE) },
                onTests = { nav.navigate(Routes.TESTS) },
                onFeature = { nav.navigate(it) }
            )
        }
        composable(Routes.ACCOUNTS) { AccountsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ADHKAR) { AdhkarScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ALARM) { AlarmScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.BLOCKER) { BlockerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CLEANER) { CleanerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CONTACTS) { ContactsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEVICE) { DeviceScreen(onBack = { nav.popBackStack() }, onMore = { nav.navigate(Routes.DEVICEPRO) }) }
        composable(Routes.DEVICEPRO) { DeviceProScreen(onBack = { nav.popBackStack() }, onOpen = { nav.navigate(it) }) }
        composable(Routes.DEVMODE) {
            DevmodeScreen(onBack = { nav.popBackStack() }, onFeature = { nav.navigate(it) })
        }
        composable(Routes.DIAGNOSTICS) { DiagnosticsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FAITH) { FaithScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.HEALTHPRO) { LifeScreen(initialTab = 0, onBack = { nav.popBackStack() }) }
        composable(Routes.LEARNPRO) { LifeScreen(initialTab = 1, onBack = { nav.popBackStack() }) }
        composable(Routes.FAITHPRO) { LifeScreen(initialTab = 2, onBack = { nav.popBackStack() }) }
        composable(Routes.LIBRARYPRO) { LifeScreen(initialTab = 3, onBack = { nav.popBackStack() }) }
        composable(Routes.FIREWALL) { FirewallScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FORUM) { ForumScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.GAME) { GameScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.JOURNAL) { JournalScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LEARN) { LearnScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LIBRARY) { LibraryScreen(onBack = { nav.popBackStack() }, onPdf = { nav.navigate(Routes.PDFREADER) }) }
        composable(Routes.LICENSE) { LicenseScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MONEY) { MoneyScreen(onBack = { nav.popBackStack() }, onWallets = { nav.navigate(Routes.WALLETS) }, onMore = { nav.navigate(Routes.MONEYPRO) }) }
        composable(Routes.MONEYPRO) { MoneyProScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.NOTIFLOG) { NotiflogScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.PROFILE) { ProfileScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.PDFREADER) { PdfReaderScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.RAIDS) { RaidsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SHOP) { ShopScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SKIN) { SkinScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.SLEEP) { SleepScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.TASKS) { TasksScreen(onBack = { nav.popBackStack() }, onMore = { nav.navigate(Routes.TASKSPRO) }) }
        composable(Routes.TASKSPRO) { TasksProScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.TESTS) { TestsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.UPDATER) { UpdaterScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.VAULT) { VaultScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.WALLETS) { WalletsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.WATER) { WaterScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.WATCH) { feature.watch.WatchScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.WORKOUTS) { WorkoutsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.AUTH) {
            AuthGateScreen()
            LaunchedEffect(store.authToken) {
                if (store.authToken.isNotBlank()) {
                    nav.navigate(if (store.onboarded) Routes.HOME else Routes.INTRO) {
                        popUpTo(Routes.AUTH) { inclusive = true }
                    }
                }
            }
        }
        composable(Routes.DEVICES) {
            DevicesScreen(
                onBack = { nav.popBackStack() },
                onLoggedOut = {
                    nav.navigate(Routes.AUTH) { popUpTo(Routes.HOME) { inclusive = true } }
                }
            )
        }
        composable(Routes.APPS) { AppsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.VISIBILITY) { VisibilityScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MEDIA) { MediaVaultScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.FILES) { FileManagerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.GIT) { GitScreen(onBack = { nav.popBackStack() }) }
    }
}
