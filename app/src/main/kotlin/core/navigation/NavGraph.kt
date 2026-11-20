package core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import core.design.UiState
import data.local.LocalStore
import feature.accounts.AccountsScreen
import feature.alarm.AlarmScreen
import feature.blocker.BlockerScreen
import feature.cleaner.CleanerScreen
import feature.contacts.ContactsScreen
import feature.device.DeviceScreen
import feature.devicepro.DeviceProScreen
import feature.devmode.DevmodeScreen
import feature.faith.FaithScreen
import feature.firewall.FirewallScreen
import feature.forum.ForumScreen
import feature.game.GameScreen
import feature.home.HomeScreen
import feature.intro.IntroScreen
import feature.journal.JournalScreen
import feature.learn.LearnScreen
import feature.life.LifeScreen
import feature.library.LibraryScreen
import feature.license.LicenseScreen
import feature.money.MoneyScreen
import feature.moneypro.MoneyProScreen
import feature.notiflog.NotiflogScreen
import feature.onboarding.OnboardingScreen
import feature.onboarding.RegisterScreen
import feature.profile.ProfileScreen
import feature.raids.RaidsScreen
import feature.settings.SettingsScreen
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
    NavHost(navController = nav, startDestination = Routes.INTRO) {
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
                ui = ui
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
                onTests = { nav.navigate(Routes.TESTS) }
            )
        }
        composable(Routes.ACCOUNTS) { AccountsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.ALARM) { AlarmScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.BLOCKER) { BlockerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CLEANER) { CleanerScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.CONTACTS) { ContactsScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEVICE) { DeviceScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEVICEPRO) { DeviceProScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.DEVMODE) { DevmodeScreen(onBack = { nav.popBackStack() }) }
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
        composable(Routes.LIBRARY) { LibraryScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.LICENSE) { LicenseScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.MONEY) { MoneyScreen(onBack = { nav.popBackStack() }, onWallets = { nav.navigate(Routes.WALLETS) }, onMore = { nav.navigate(Routes.MONEYPRO) }) }
        composable(Routes.MONEYPRO) { MoneyProScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.NOTIFLOG) { NotiflogScreen(onBack = { nav.popBackStack() }) }
        composable(Routes.PROFILE) { ProfileScreen(onBack = { nav.popBackStack() }) }
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
        composable(Routes.WORKOUTS) { WorkoutsScreen(onBack = { nav.popBackStack() }) }
    }
}
