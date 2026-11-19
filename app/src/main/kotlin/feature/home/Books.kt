package feature.home

import androidx.compose.ui.graphics.Color
import com.yousuf.toolkit.R
import core.design.BookSymbol
import core.navigation.Routes

data class MagicBook(
    val route: String,
    val title: Int,
    val start: Color,
    val end: Color,
    val glow: Color,
    val symbol: BookSymbol
)

data class BookSection(val title: Int, val books: List<MagicBook>)

val bookSections = listOf(
    BookSection(
        R.string.section_core,
        listOf(
            MagicBook(Routes.TASKS, R.string.feature_tasks, Color(0xFF7B2FF7), Color(0xFF3B0F86), Color(0xFFD9C4FF), BookSymbol.STAR),
            MagicBook(Routes.MONEY, R.string.feature_money, Color(0xFFF7B733), Color(0xFF9C5C00), Color(0xFFFFF0C2), BookSymbol.DIAMOND),
            MagicBook(Routes.WORKOUTS, R.string.feature_workouts, Color(0xFFFF5F6D), Color(0xFF8E0F28), Color(0xFFFFD0D4), BookSymbol.FLAME),
            MagicBook(Routes.JOURNAL, R.string.feature_journal, Color(0xFF00C6FF), Color(0xFF005E9E), Color(0xFFC2F0FF), BookSymbol.RUNE)
        )
    ),
    BookSection(
        R.string.section_daily,
        listOf(
            MagicBook(Routes.WATER, R.string.feature_water, Color(0xFF22D3EE), Color(0xFF0B5C86), Color(0xFFC5F6FF), BookSymbol.DROP),
            MagicBook(Routes.SLEEP, R.string.feature_sleep, Color(0xFF6A5CFF), Color(0xFF2A1B7A), Color(0xFFD7D0FF), BookSymbol.MOON),
            MagicBook(Routes.FAITH, R.string.feature_faith, Color(0xFF10B981), Color(0xFF046B4A), Color(0xFFC7F9E5), BookSymbol.STAR),
            MagicBook(Routes.LEARN, R.string.feature_learn, Color(0xFF84CC16), Color(0xFF3F6212), Color(0xFFE7F9B8), BookSymbol.LEAF),
            MagicBook(Routes.LIBRARY, R.string.feature_library, Color(0xFFD97706), Color(0xFF7C2D12), Color(0xFFFFE0B2), BookSymbol.ORB)
        )
    ),
    BookSection(
        R.string.section_game,
        listOf(
            MagicBook(Routes.GAME, R.string.feature_game, Color(0xFFEC4899), Color(0xFF7A1040), Color(0xFFFFD1E6), BookSymbol.STAR),
            MagicBook(Routes.SHOP, R.string.feature_shop, Color(0xFFF59E0B), Color(0xFF8A3D05), Color(0xFFFFE8B8), BookSymbol.DIAMOND),
            MagicBook(Routes.RAIDS, R.string.feature_raids, Color(0xFFEF4444), Color(0xFF6E1414), Color(0xFFFFC9C9), BookSymbol.FLAME),
            MagicBook(Routes.PROFILE, R.string.feature_profile, Color(0xFF8B5CF6), Color(0xFF43188C), Color(0xFFE3D4FF), BookSymbol.ORB),
            MagicBook(Routes.SKIN, R.string.feature_skin, Color(0xFFF472B6), Color(0xFF8C1145), Color(0xFFFFD6EA), BookSymbol.LEAF)
        )
    ),
    BookSection(
        R.string.section_device,
        listOf(
            MagicBook(Routes.BLOCKER, R.string.feature_blocker, Color(0xFF64748B), Color(0xFF1E293B), Color(0xFFD7E2F0), BookSymbol.RUNE),
            MagicBook(Routes.CLEANER, R.string.feature_cleaner, Color(0xFF14B8A6), Color(0xFF115E59), Color(0xFFC7F5EF), BookSymbol.ORB),
            MagicBook(Routes.CONTACTS, R.string.feature_contacts, Color(0xFF22C55E), Color(0xFF14532D), Color(0xFFC8F7D4), BookSymbol.STAR),
            MagicBook(Routes.DEVICE, R.string.feature_device, Color(0xFF475569), Color(0xFF0F172A), Color(0xFFCBD8E8), BookSymbol.DIAMOND),
            MagicBook(Routes.FIREWALL, R.string.feature_firewall, Color(0xFFF97316), Color(0xFF7C2D12), Color(0xFFFFD9B8), BookSymbol.FLAME),
            MagicBook(Routes.NOTIFLOG, R.string.feature_notiflog, Color(0xFF3B82F6), Color(0xFF1E3A8A), Color(0xFFCFE0FF), BookSymbol.RUNE),
            MagicBook(Routes.UPDATER, R.string.feature_updater, Color(0xFF38BDF8), Color(0xFF075985), Color(0xFFC9EFFF), BookSymbol.STAR),
            MagicBook(Routes.VAULT, R.string.feature_vault, Color(0xFFA16207), Color(0xFF422006), Color(0xFFFFE7A3), BookSymbol.DIAMOND),
            MagicBook(Routes.WALLETS, R.string.feature_wallets, Color(0xFF16A34A), Color(0xFF064E3B), Color(0xFFC9F5DA), BookSymbol.ORB)
        )
    ),
    BookSection(
        R.string.section_more,
        listOf(
            MagicBook(Routes.FORUM, R.string.feature_forum, Color(0xFF6366F1), Color(0xFF312E81), Color(0xFFD5D4FF), BookSymbol.RUNE),
            MagicBook(Routes.LICENSE, R.string.feature_license, Color(0xFFA8A29E), Color(0xFF44403C), Color(0xFFEDE9E4), BookSymbol.STAR),
            MagicBook(Routes.DEVMODE, R.string.feature_devmode, Color(0xFF334155), Color(0xFF020617), Color(0xFFC8D4E6), BookSymbol.RUNE),
            MagicBook(Routes.TESTS, R.string.feature_tests, Color(0xFFA855F7), Color(0xFF581C87), Color(0xFFE9D0FF), BookSymbol.STAR)
        )
    )
)
