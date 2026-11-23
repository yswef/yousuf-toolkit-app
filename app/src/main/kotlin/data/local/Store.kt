package data.local

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import core.config.AppConfig
import core.security.VaultCrypto
import domain.model.AccountDevice
import domain.model.AccountDraft
import domain.model.AccountItem
import domain.model.AccountRelation
import domain.model.Book
import domain.model.Campaign
import domain.model.Debt
import domain.model.ExtraRecord
import domain.model.Habit
import domain.model.JournalEntry
import domain.model.LinkedNote
import domain.model.LearnItem
import domain.model.Ledger
import domain.model.NotifEntry
import domain.model.Priority
import domain.model.ScreenshotItem
import domain.model.SleepLog
import domain.model.SmsMoney
import domain.model.Task
import domain.model.VaultNote
import domain.model.Wallet
import domain.model.Workout
import domain.model.prayerKeys
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.util.concurrent.Executors

val LocalStore = staticCompositionLocalOf<Store> { error("Store is not provided") }

class Store(context: Context) {
    val config = AppConfig.load(context)

    private val file = File(context.applicationContext.filesDir, "state.json")
    private val io = Executors.newSingleThreadExecutor()
    private var seq = 1L
    private var energyDay = LocalDate.now().toString()

    var onboarded by mutableStateOf(false)
        private set
    var points by mutableStateOf(0)
        private set
    var xp by mutableStateOf(0)
        private set
    var energy by mutableStateOf(config.energyMax)
        private set
    var tasks by mutableStateOf<List<Task>>(emptyList())
        private set
    var ledger by mutableStateOf<List<Ledger>>(emptyList())
        private set
    var debts by mutableStateOf<List<Debt>>(emptyList())
        private set
    var workouts by mutableStateOf<List<Workout>>(emptyList())
        private set
    var journal by mutableStateOf<List<JournalEntry>>(emptyList())
        private set
    var water by mutableStateOf<Map<String, Int>>(emptyMap())
        private set
    var prayers by mutableStateOf<Map<String, Set<String>>>(emptyMap())
        private set
    var sleep by mutableStateOf<List<SleepLog>>(emptyList())
        private set
    var learn by mutableStateOf<List<LearnItem>>(emptyList())
        private set
    var books by mutableStateOf<List<Book>>(emptyList())
        private set
    var profileName by mutableStateOf("")
        private set
    var profileAge by mutableStateOf(0)
        private set
    var profileGoal by mutableStateOf("")
        private set
    var profileTeam by mutableStateOf("")
        private set
    var joinedAt by mutableStateOf(0L)
        private set
    var wallets by mutableStateOf<List<Wallet>>(emptyList())
        private set
    var notifications by mutableStateOf<List<NotifEntry>>(emptyList())
        private set
    var vault by mutableStateOf<List<VaultNote>>(emptyList())
        private set
    var blockedApps by mutableStateOf<Set<String>>(emptySet())
        private set
    var blockerOn by mutableStateOf(false)
        private set
    var blockStart by mutableStateOf(22 * 60)
        private set
    var blockEnd by mutableStateOf(6 * 60)
        private set
    var blockedDomains by mutableStateOf<List<String>>(emptyList())
        private set
    var serverUrl by mutableStateOf(config.apiBase)
        private set
    var authToken by mutableStateOf("")
        private set
    var accountId by mutableStateOf(0L)
        private set
    var accountName by mutableStateOf("")
        private set
    var accountEmail by mutableStateOf("")
        private set
    var accent by mutableStateOf("violet")
        private set
    var avatar by mutableStateOf(0)
        private set
    var premium by mutableStateOf(false)
        private set
    var licenseCode by mutableStateOf("")
        private set
    var campaigns by mutableStateOf<List<Campaign>>(emptyList())
        private set
    var accounts by mutableStateOf<List<AccountItem>>(emptyList())
        private set
    var accountDevices by mutableStateOf<List<AccountDevice>>(emptyList())
        private set
    var accountRelations by mutableStateOf<List<AccountRelation>>(emptyList())
        private set
    var screenshots by mutableStateOf<List<ScreenshotItem>>(emptyList())
        private set
    var linkedNotes by mutableStateOf<List<LinkedNote>>(emptyList())
        private set
    var extras by mutableStateOf<List<ExtraRecord>>(emptyList())
        private set
    var habits by mutableStateOf<List<Habit>>(emptyList())
        private set
    var shields by mutableStateOf(0)
        private set
    var frozenDay by mutableStateOf("")
        private set
    var starterSeeded by mutableStateOf(false)
        private set
    var smsSeen by mutableStateOf<Set<String>>(emptySet())
        private set
    private var faithDays by mutableStateOf<Set<String>>(emptySet())
    private var sleepDays by mutableStateOf<Set<String>>(emptySet())

    init {
        read()
        ensureDefaultWallet()
        ensureDay(false)
    }

    val today: String
        get() = LocalDate.now().toString()

    val level: Int
        get() = if (config.xpPerLevel <= 0) 1 else xp / config.xpPerLevel + 1

    val levelProgress: Float
        get() = if (config.xpPerLevel <= 0) 0f else (xp % config.xpPerLevel) / config.xpPerLevel.toFloat()

    val rankKey: String
        get() {
            val ranks = config.ranks.ifEmpty { listOf("novice" to 1) }
            return ranks.lastOrNull { level >= it.second }?.first ?: ranks.first().first
        }

    val nextRank: Pair<String, Int>?
        get() = config.ranks.firstOrNull { level < it.second }

    val rankProgress: Float
        get() {
            val ranks = config.ranks.ifEmpty { listOf("novice" to 1) }
            val next = ranks.firstOrNull { level < it.second } ?: return 1f
            val previous = ranks.lastOrNull { level >= it.second }?.second ?: 1
            val span = (next.second - previous).coerceAtLeast(1)
            return ((level - previous) / span.toFloat()).coerceIn(0f, 1f)
        }

    val balance: Double
        get() = ledger.sumOf { if (it.income) it.amount else -it.amount }

    val hasProfile: Boolean
        get() = profileName.isNotBlank()

    val openTasks: Int
        get() = tasks.count { !it.done }

    val learnedMinutes: Int
        get() = learn.sumOf { it.minutes }

    fun waterToday(): Int = water[today] ?: 0

    fun prayersToday(): Set<String> = prayers[today] ?: emptySet()

    fun faithFullDays(): Int = prayers.count { it.value.size >= prayerKeys.size }

    fun sleepToday(): Double = sleep.filter { dayOf(it.at) == today }.sumOf { it.hours }

    fun start() {
        onboarded = true
        save()
    }

    fun saveProfile(name: String, age: Int, goal: String, team: String) {
        if (name.isBlank()) return
        profileName = name.trim()
        profileAge = age
        profileGoal = goal
        profileTeam = team
        if (joinedAt == 0L) joinedAt = System.currentTimeMillis()
        onboarded = true
        save()
    }

    fun rest() {
        ensureDay()
        energy = (energy + config.restGain).coerceAtMost(config.energyMax)
        save()
    }

    fun addWallet(name: String, initial: Double = 0.0) {
        if (name.isBlank() || initial < 0.0) return
        wallets = wallets + Wallet(nextId(), name.trim(), initial)
        save()
    }

    private fun ensureDefaultWallet() {
        if (wallets.any { it.name == DEFAULT_WALLET }) return
        wallets = listOf(Wallet(nextId(), DEFAULT_WALLET, balance)) + wallets
        save()
    }

    private fun adjustDefaultWallet(delta: Double) {
        if (wallets.none { it.name == DEFAULT_WALLET }) ensureDefaultWallet()
        wallets = wallets.map { if (it.name == DEFAULT_WALLET) it.copy(balance = it.balance + delta) else it }
    }

    fun isDefaultWallet(wallet: Wallet) = wallet.name == DEFAULT_WALLET

    fun deleteWallet(id: Long) {
        if (wallets.any { it.id == id && it.name == DEFAULT_WALLET }) return
        wallets = wallets.filterNot { it.id == id }
        save()
    }

    fun deposit(id: Long, amount: Double) {
        if (amount <= 0.0) return
        wallets = wallets.map { if (it.id == id) it.copy(balance = it.balance + amount) else it }
        save()
    }

    fun withdraw(id: Long, amount: Double): Boolean {
        if (amount <= 0.0) return false
        val wallet = wallets.firstOrNull { it.id == id } ?: return false
        if (wallet.balance < amount) return false
        wallets = wallets.map { if (it.id == id) it.copy(balance = it.balance - amount) else it }
        save()
        return true
    }

    fun addNotification(pkg: String, title: String, text: String) {
        if (title.isBlank() && text.isBlank()) return
        notifications = (
            listOf(
                NotifEntry(
                    nextId(),
                    pkg,
                    title.take(160),
                    text.take(400),
                    System.currentTimeMillis()
                )
            ) + notifications
            ).take(200)
        save()
    }

    fun clearNotifications() {
        notifications = emptyList()
        save()
    }

    fun addVault(title: String, body: String): Boolean {
        if (title.isBlank() && body.isBlank()) return false
        val plain = JSONObject().put("t", title).put("b", body).toString()
        val payload = VaultCrypto.encrypt(plain) ?: return false
        vault = listOf(VaultNote(nextId(), payload, System.currentTimeMillis())) + vault
        save()
        return true
    }

    fun deleteVault(id: Long) {
        vault = vault.filterNot { it.id == id }
        save()
    }

    fun vaultText(note: VaultNote): Pair<String, String>? {
        val plain = VaultCrypto.decrypt(note.payload) ?: return null
        val obj = runCatching { JSONObject(plain) }.getOrNull() ?: return null
        return obj.optString("t") to obj.optString("b")
    }

    fun setBlocker(on: Boolean) {
        blockerOn = on
        save()
    }

    fun setBlockWindow(startMinutes: Int, endMinutes: Int) {
        blockStart = startMinutes.coerceIn(0, 1439)
        blockEnd = endMinutes.coerceIn(0, 1439)
        save()
    }

    fun toggleBlockedApp(pkg: String) {
        if (pkg.isBlank()) return
        blockedApps = if (pkg in blockedApps) blockedApps - pkg else blockedApps + pkg
        save()
    }

    fun isBlockingNow(): Boolean {
        if (!blockerOn || blockedApps.isEmpty()) return false
        val minute = LocalTime.now().let { it.hour * 60 + it.minute }
        return when {
            blockStart == blockEnd -> true
            blockStart < blockEnd -> minute in blockStart until blockEnd
            else -> minute >= blockStart || minute < blockEnd
        }
    }

    fun addDomain(value: String) {
        val domain = value.trim().lowercase()
            .removePrefix("https://")
            .removePrefix("http://")
            .substringBefore('/')
        if (domain.isBlank() || domain in blockedDomains) return
        blockedDomains = blockedDomains + domain
        save()
    }

    fun deleteDomain(value: String) {
        blockedDomains = blockedDomains - value
        save()
    }

    fun saveServerUrl(value: String) {
        serverUrl = value.trim().trimEnd('/')
        save()
    }

    fun saveAccount(token: String, id: Long, name: String, email: String) {
        authToken = token
        accountId = id
        accountName = name
        accountEmail = email
        save()
    }

    fun logout() {
        authToken = ""
        accountId = 0L
        accountName = ""
        accountEmail = ""
        save()
    }

    fun addCampaign(title: String, target: Int): Boolean {
        if (title.isBlank() || target <= 0) return false
        campaigns = campaigns + Campaign(
            id = nextId(),
            title = title.trim().take(80),
            target = target.coerceIn(1, 365),
            checkins = 0,
            startedAt = System.currentTimeMillis(),
            lastCheckin = 0L
        )
        save()
        return true
    }

    fun checkinCampaign(id: Long): Boolean {
        val campaign = campaigns.firstOrNull { it.id == id } ?: return false
        val now = System.currentTimeMillis()
        if (campaign.lastCheckin > 0L && dayOf(campaign.lastCheckin) == dayOf(now)) return false
        campaigns = campaigns.map {
            if (it.id == id) it.copy(checkins = it.checkins + 1, lastCheckin = now) else it
        }
        save()
        return true
    }

    fun deleteCampaign(id: Long) {
        campaigns = campaigns.filterNot { it.id == id }
        save()
    }

    fun saveSkin(value: String) {
        accent = value
        save()
    }

    fun saveAvatar(index: Int) {
        avatar = index.coerceIn(0, 7)
        save()
    }

    fun saveLicense(code: String) {
        licenseCode = code.trim()
        premium = licenseCode.isNotBlank()
        save()
    }

    fun backupText(): String = snapshot()

    fun restoreBackup(text: String): Boolean {
        val root = try {
            JSONObject(text)
        } catch (t: Throwable) {
            return false
        }
        if (!root.has("points") && !root.has("tasks")) return false
        val previous = JSONObject(snapshot())
        if (!read(root)) {
            read(previous)
            return false
        }
        ensureDefaultWallet()
        save()
        return true
    }

    fun importMoneyCsv(text: String): Int {
        var added = 0
        text.lineSequence().forEach { line ->
            val cols = csvCols(line)
            if (cols.size < 3) return@forEach
            val amount = cols[2].replace(",", "").toDoubleOrNull() ?: return@forEach
            if (amount <= 0.0) return@forEach
            if (cols[0].contains("type", true) || cols[1].contains("title", true)) return@forEach
            val income = cols[0].contains("دخل") || cols[0].equals("income", true)
            addLedger(cols[1], amount, income, "other")
            added++
        }
        return added
    }

    fun importTaskCsv(text: String): Int {
        var added = 0
        text.lineSequence().forEach { line ->
            val cols = csvCols(line)
            val title = cols.firstOrNull().orEmpty()
            if (title.isBlank()) return@forEach
            if (title.contains("title", true) || title.contains("اسم", false)) return@forEach
            val note = cols.getOrElse(1) { "" }
            val priority = when {
                cols.getOrElse(2) { "" }.contains("high", true) -> Priority.HIGH
                cols.getOrElse(2) { "" }.contains("low", true) -> Priority.LOW
                else -> Priority.NORMAL
            }
            addTask(title, note, priority)
            added++
        }
        return added
    }

    fun importSms(items: List<SmsMoney>): Int {
        var added = 0
        items.forEach { item ->
            if (item.key in smsSeen) return@forEach
            smsSeen = smsSeen + item.key
            ledger = listOf(
                Ledger(
                    id = nextId(),
                    title = item.sender.ifBlank { item.body.take(60) },
                    amount = item.amount,
                    income = item.income,
                    category = "other",
                    at = if (item.at > 0L) item.at else System.currentTimeMillis()
                )
            ) + ledger
            adjustDefaultWallet(if (item.income) item.amount else -item.amount)
            added++
        }
        if (added > 0) save()
        return added
    }

    fun addTask(title: String, note: String, priority: Priority, dueAt: Long = 0L, repeat: String = "", list: String = "") {
        if (title.isBlank()) return
        tasks = tasks + Task(nextId(), title.trim(), note.trim(), priority, false, System.currentTimeMillis(), dueAt, repeat, list.trim())
        save()
    }

    fun buyShield(): Boolean {
        if (points < config.shieldCost) return false
        takePoints(config.shieldCost)
        shields += 1
        save()
        return true
    }

    fun freezeToday(): Boolean {
        if (frozenDay == today || points < config.freezeCost) return false
        takePoints(config.freezeCost)
        frozenDay = today
        save()
        return true
    }

    fun seedStarterTasks(titles: List<String>) {
        if (starterSeeded) return
        if (tasks.isEmpty()) {
            val now = System.currentTimeMillis()
            tasks = titles.filter { it.isNotBlank() }.mapIndexed { index, title ->
                Task(nextId(), title, "", if (index < 2) Priority.HIGH else Priority.NORMAL, false, now + index, 0L, "", "")
            }
        }
        starterSeeded = true
        save()
    }

    fun addHabit(title: String) {
        if (title.isBlank()) return
        habits = habits + Habit(nextId(), title.trim(), 0, 0, "")
        save()
    }

    fun checkHabit(id: Long) {
        ensureDay()
        val day = today
        val habit = habits.firstOrNull { it.id == id } ?: return
        if (habit.lastDay == day) return
        val streak = if (habit.lastDay == LocalDate.now().minusDays(1).toString()) habit.streak + 1 else 1
        habits = habits.map {
            if (it.id == id) it.copy(streak = streak, best = maxOf(it.best, streak), lastDay = day) else it
        }
        addPoints(config.pointsTask / 2)
        save()
    }

    fun deleteHabit(id: Long) {
        habits = habits.filterNot { it.id == id }
        save()
    }

    fun toggleTask(id: Long): Boolean {
        ensureDay()
        val task = tasks.firstOrNull { it.id == id } ?: return false
        if (!task.done) {
            if (energy < config.energyCost) return false
            energy -= config.energyCost
            addPoints(config.pointsTask)
            tasks = tasks.map { if (it.id == id) it.copy(done = true) else it }
            if (task.repeat.isNotBlank()) {
                val next = when (task.repeat) {
                    "daily" -> LocalDate.now().plusDays(1)
                    "weekly" -> LocalDate.now().plusWeeks(1)
                    "monthly" -> LocalDate.now().plusMonths(1)
                    else -> null
                }
                if (next != null) {
                    val due = next.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    tasks = tasks + Task(nextId(), task.title, task.note, task.priority, false, System.currentTimeMillis(), due, task.repeat, task.list)
                }
            }
        } else {
            energy = (energy + config.energyCost).coerceAtMost(config.energyMax)
            takePoints(config.pointsTask)
            tasks = tasks.map { if (it.id == id) it.copy(done = false) else it }
            if (task.repeat.isNotBlank()) {
                val spawned = tasks.firstOrNull {
                    it.id > task.id && !it.done && it.title == task.title && it.repeat == task.repeat && it.list == task.list
                }
                if (spawned != null) tasks = tasks.filterNot { it.id == spawned.id }
            }
        }
        save()
        return true
    }

    fun restoreTask(task: Task) {
        if (tasks.any { it.id == task.id }) return
        tasks = tasks + task
        save()
    }

    fun deleteTask(id: Long) {
        tasks = tasks.filterNot { it.id == id }
        save()
    }

    fun addLedger(title: String, amount: Double, income: Boolean, category: String, at: Long = System.currentTimeMillis()) {
        if (title.isBlank() || !amount.isFinite() || amount <= 0.0) return
        ledger = ledger + Ledger(nextId(), title.trim(), amount, income, category, at)
        adjustDefaultWallet(if (income) amount else -amount)
        save()
    }

    fun addDebt(person: String, amount: Double, owedByMe: Boolean, note: String) {
        if (person.isBlank() || !amount.isFinite() || amount <= 0.0) return
        debts = debts + Debt(nextId(), person.trim(), amount, owedByMe, note.trim(), false, System.currentTimeMillis())
        save()
    }

    fun setDebtSettled(id: Long, settled: Boolean) {
        debts = debts.map { if (it.id == id) it.copy(settled = settled) else it }
        save()
    }

    fun payDebt(id: Long, amount: Double): Boolean {
        if (amount <= 0.0) return false
        val debt = debts.firstOrNull { it.id == id } ?: return false
        val paid = amount.coerceAtMost(debt.amount)
        val left = debt.amount - paid
        debts = debts.map { if (it.id == id) it.copy(amount = left, settled = left <= 0.0001) else it }
        addLedger(debt.person, paid, !debt.owedByMe, "other")
        save()
        return true
    }

    fun deleteDebt(id: Long) {
        debts = debts.filterNot { it.id == id }
        save()
    }

    fun missionClaimed(key: String): Boolean = extras.any { it.kind == "mission" && it.title == key }

    fun claimMission(key: String, reward: Int): Boolean {
        if (missionClaimed(key)) return false
        extras = listOf(ExtraRecord(nextId(), "mission", key, "", reward.toDouble(), "done", 0L, System.currentTimeMillis())) + extras
        addPoints(reward)
        save()
        return true
    }

    fun lessonDone(key: String): Boolean = extras.any { it.kind == "lesson" && it.title == key }

    fun toggleLesson(key: String): Boolean {
        val existing = extras.firstOrNull { it.kind == "lesson" && it.title == key }
        if (existing != null) {
            extras = extras.filterNot { it.id == existing.id }
            takePoints(LESSON_POINTS)
            save()
            return false
        }
        extras = listOf(ExtraRecord(nextId(), "lesson", key, "", 0.0, "done", 0L, System.currentTimeMillis())) + extras
        addPoints(LESSON_POINTS)
        save()
        return true
    }

    fun restoreLedger(item: Ledger) {
        if (ledger.any { it.id == item.id }) return
        ledger = ledger + item
        adjustDefaultWallet(if (item.income) item.amount else -item.amount)
        save()
    }

    fun restoreWorkout(item: Workout) {
        if (workouts.any { it.id == item.id }) return
        workouts = workouts + item
        addPoints(config.pointsWorkout)
        save()
    }

    fun restoreEntry(item: JournalEntry) {
        if (journal.any { it.id == item.id }) return
        journal = journal + item
        save()
    }

    fun restoreSleep(item: SleepLog) {
        if (sleep.any { it.id == item.id }) return
        sleep = sleep + item
        save()
    }

    fun restoreLearn(item: LearnItem) {
        if (learn.any { it.id == item.id }) return
        learn = learn + item
        save()
    }

    fun restoreBook(item: Book) {
        if (books.any { it.id == item.id }) return
        books = books + item
        save()
    }

    fun deleteLedger(id: Long) {
        ledger.firstOrNull { it.id == id }?.let { adjustDefaultWallet(if (it.income) -it.amount else it.amount) }
        ledger = ledger.filterNot { it.id == id }
        save()
    }

    fun addWorkout(kind: String, minutes: Int) {
        if (minutes <= 0) return
        workouts = workouts + Workout(nextId(), kind, minutes, System.currentTimeMillis())
        addPoints(config.pointsWorkout)
        save()
    }

    fun deleteWorkout(id: Long) {
        val found = workouts.any { it.id == id }
        workouts = workouts.filterNot { it.id == id }
        if (found) takePoints(config.pointsWorkout)
        save()
    }

    fun addEntry(title: String, body: String) {
        if (title.isBlank() && body.isBlank()) return
        journal = journal + JournalEntry(nextId(), title.trim(), body.trim(), System.currentTimeMillis())
        save()
    }

    fun deleteEntry(id: Long) {
        journal = journal.filterNot { it.id == id }
        save()
    }

    fun addWater(delta: Int) {
        ensureDay()
        if (delta == 0) return
        val day = today
        val current = water[day] ?: 0
        val cap = if (config.waterGoal > 0) config.waterGoal else Int.MAX_VALUE
        val next = (current + delta).coerceIn(0, cap)
        if (next == current) return
        water = water + (day to next)
        if (delta > 0) {
            if (config.waterGoal <= 0 || current < config.waterGoal) addPoints(config.pointsWater)
        } else {
            takePoints(config.pointsWater)
        }
        save()
    }

    fun togglePrayer(id: String) {
        if (id !in prayerKeys) return
        ensureDay()
        val day = today
        val current = prayers[day] ?: emptySet()
        val next = if (id in current) current - id else current + id
        prayers = prayers + (day to next)
        val full = next.size >= prayerKeys.size
        val rewarded = day in faithDays
        if (full && !rewarded) {
            faithDays = faithDays + day
            addPoints(config.pointsFaith)
        }
        if (!full && rewarded) {
            faithDays = faithDays - day
            takePoints(config.pointsFaith)
        }
        save()
    }

    fun addSleep(hours: Double) {
        if (hours <= 0.0) return
        ensureDay()
        sleep = (listOf(SleepLog(nextId(), hours.coerceAtMost(24.0), System.currentTimeMillis())) + sleep).take(400)
        val day = today
        if (day !in sleepDays) {
            sleepDays = sleepDays + day
            addPoints(config.pointsSleep)
        }
        save()
    }

    fun deleteSleep(id: Long) {
        sleep = sleep.filterNot { it.id == id }
        save()
    }

    fun addLearn(title: String, target: Int) {
        if (title.isBlank() || target <= 0) return
        learn = learn + LearnItem(nextId(), title.trim(), 0, target, false)
        save()
    }

    fun learnMore(id: Long, minutes: Int) {
        if (minutes <= 0) return
        val item = learn.firstOrNull { it.id == id } ?: return
        val next = (item.minutes + minutes).coerceAtMost(item.target)
        val done = next >= item.target
        val rewarded = item.rewarded || (done && config.pointsLearn > 0)
        if (done && !item.rewarded) addPoints(config.pointsLearn)
        learn = learn.map { if (it.id == id) it.copy(minutes = next, rewarded = rewarded) else it }
        save()
    }

    fun deleteLearn(id: Long) {
        learn = learn.filterNot { it.id == id }
        save()
    }

    fun addBook(title: String, pages: Int) {
        if (title.isBlank() || pages <= 0) return
        books = books + Book(nextId(), title.trim(), 0, pages, false)
        save()
    }

    fun readMore(id: Long, pages: Int) {
        if (pages <= 0) return
        val book = books.firstOrNull { it.id == id } ?: return
        val next = (book.page + pages).coerceAtMost(book.pages)
        if (next == book.page) return
        val done = next >= book.pages
        val rewarded = book.rewarded || (done && config.pointsReading > 0)
        if (done && !book.rewarded) addPoints(config.pointsReading)
        books = books.map { if (it.id == id) it.copy(page = next, rewarded = rewarded) else it }
        save()
    }

    fun deleteBook(id: Long) {
        books = books.filterNot { it.id == id }
        save()
    }

    fun saveAccount(id: Long, draft: AccountDraft): Boolean {
        if (draft.title.isBlank() && draft.email.isBlank() && draft.username.isBlank()) return false
        val item = AccountItem(
            id = if (id > 0L) id else nextId(),
            title = draft.title.trim(),
            username = draft.username.trim(),
            email = draft.email.trim(),
            password = enc(draft.password),
            totp = enc(draft.totp),
            layer = draft.layer,
            category = draft.category,
            recoveryEmail = draft.recoveryEmail.trim(),
            recoveryPhone = draft.recoveryPhone.trim(),
            codes = enc(draft.codes),
            notes = enc(draft.notes),
            updatedAt = System.currentTimeMillis()
        )
        accounts = if (id > 0L) accounts.map { if (it.id == id) item else it } else listOf(item) + accounts
        save()
        return true
    }

    fun touchAccount(id: Long) {
        accounts = accounts.map { if (it.id == id) it.copy(updatedAt = System.currentTimeMillis()) else it }
        save()
    }

    fun deleteAccount(id: Long) {
        accounts = accounts.filterNot { it.id == id }
        accountRelations = accountRelations.filterNot { it.fromId == id || it.toId == id }
        save()
    }

    fun accountPassword(item: AccountItem): String = dec(item.password)
    fun accountTotp(item: AccountItem): String = dec(item.totp).replace(" ", "")
    fun accountCodes(item: AccountItem): String = dec(item.codes)
    fun accountNotes(item: AccountItem): String = dec(item.notes)

    fun addAccountDevice(name: String, platform: String) {
        if (name.isBlank()) return
        accountDevices = listOf(AccountDevice(nextId(), name.trim(), platform.ifBlank { "android" }, System.currentTimeMillis(), false)) + accountDevices
        save()
    }

    fun ensureCurrentDevice(name: String) {
        if (accountDevices.any { it.current }) return
        accountDevices = listOf(AccountDevice(nextId(), name, "android", System.currentTimeMillis(), true)) + accountDevices
        save()
    }

    fun removeAccountDevice(id: Long) {
        accountDevices = accountDevices.filterNot { it.id == id }
        save()
    }

    fun addAccountRelation(fromId: Long, toId: Long, kind: String) {
        if (fromId == toId || fromId <= 0L || toId <= 0L) return
        if (accountRelations.any { it.fromId == fromId && it.toId == toId && it.kind == kind }) return
        accountRelations = accountRelations + AccountRelation(nextId(), fromId, toId, kind)
        save()
    }

    fun deleteAccountRelation(id: Long) {
        accountRelations = accountRelations.filterNot { it.id == id }
        save()
    }

    fun addScreenshot(label: String, mime: String, bytes: ByteArray): Boolean {
        if (bytes.isEmpty()) return false
        val plain = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
        val payload = VaultCrypto.encrypt(plain) ?: return false
        screenshots = listOf(ScreenshotItem(nextId(), label.ifBlank { "screenshot" }, mime, payload, System.currentTimeMillis())) + screenshots
        save()
        return true
    }

    fun screenshotBytes(item: ScreenshotItem): ByteArray? {
        val plain = VaultCrypto.decrypt(item.payload) ?: return null
        return runCatching { android.util.Base64.decode(plain, android.util.Base64.NO_WRAP) }.getOrNull()
    }

    fun deleteScreenshot(id: Long) {
        screenshots = screenshots.filterNot { it.id == id }
        save()
    }

    fun addLinkedNote(title: String, body: String, tags: String): Boolean {
        if (title.isBlank() && body.isBlank()) return false
        linkedNotes = listOf(LinkedNote(nextId(), title.trim(), body, tags.trim(), System.currentTimeMillis())) + linkedNotes
        save()
        return true
    }

    fun deleteLinkedNote(id: Long) {
        linkedNotes = linkedNotes.filterNot { it.id == id }
        save()
    }

    fun addExtra(kind: String, title: String, detail: String, amount: Double, dueAt: Long): Boolean {
        if (kind.isBlank() || title.isBlank()) return false
        extras = listOf(ExtraRecord(nextId(), kind, title.trim(), detail.trim(), amount, "open", dueAt, System.currentTimeMillis())) + extras
        save()
        return true
    }

    fun addAlarm(title: String, text: String, at: Long): Long {
        val id = nextId()
        extras = listOf(ExtraRecord(id, "alarm", title.trim(), text.trim(), 0.0, "open", at, System.currentTimeMillis())) + extras
        save()
        return id
    }

    fun updateExtra(id: Long, status: String, amount: Double? = null) {
        extras = extras.map {
            if (it.id == id) it.copy(status = status, amount = amount ?: it.amount) else it
        }
        save()
    }

    fun addExtraAmount(id: Long, delta: Double) {
        extras = extras.map {
            if (it.id == id) it.copy(amount = (it.amount + delta).coerceAtLeast(0.0)) else it
        }
        save()
    }

    fun deleteExtra(id: Long) {
        extras = extras.filterNot { it.id == id }
        save()
    }

    fun transferBetweenWallets(fromId: Long, toId: Long, amount: Double): Boolean {
        if (amount <= 0.0 || fromId == toId) return false
        val from = wallets.firstOrNull { it.id == fromId } ?: return false
        val to = wallets.firstOrNull { it.id == toId } ?: return false
        if (from.balance < amount) return false
        wallets = wallets.map {
            when (it.id) {
                fromId -> it.copy(balance = it.balance - amount)
                toId -> it.copy(balance = it.balance + amount)
                else -> it
            }
        }
        extras = listOf(ExtraRecord(nextId(), "transfer", from.name + " > " + to.name, "", amount, "done", 0L, System.currentTimeMillis())) + extras
        save()
        return true
    }

    private fun enc(value: String): String? =
        value.takeIf { it.isNotBlank() }?.let { VaultCrypto.encrypt(it) }

    private fun dec(value: String?): String =
        value?.takeIf { it.isNotBlank() }?.let { VaultCrypto.decrypt(it) }.orEmpty()

    private fun addPoints(value: Int) {
        if (value <= 0) return
        points += value
        xp += value
    }

    private fun takePoints(value: Int) {
        if (value <= 0) return
        points = (points - value).coerceAtLeast(0)
        xp = (xp - value).coerceAtLeast(0)
    }

    private fun ensureDay(saveIfChanged: Boolean = true) {
        val day = LocalDate.now().toString()
        if (day != energyDay) {
            energyDay = day
            energy = config.energyMax
            if (saveIfChanged) save()
        }
    }

    @Synchronized
    private fun nextId(): Long = seq++

    private fun dayOf(at: Long): String =
        Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()

    private fun snapshot(): String {
        val root = JSONObject()
        root.put("onboarded", onboarded)
        root.put("points", points)
        root.put("xp", xp)
        root.put("energy", energy)
        root.put("energyDay", energyDay)
        root.put("seq", seq)
        root.put("profileName", profileName)
        root.put("profileAge", profileAge)
        root.put("profileGoal", profileGoal)
        root.put("profileTeam", profileTeam)
        root.put("joinedAt", joinedAt)
        root.put("serverUrl", serverUrl)
        root.put("authToken", authToken)
        root.put("accountId", accountId)
        root.put("accountName", accountName)
        root.put("accountEmail", accountEmail)
        root.put("accent", accent)
        root.put("avatar", avatar)
        root.put("premium", premium)
        root.put("licenseCode", licenseCode)
        root.put("campaigns", JSONArray().apply {
            campaigns.forEach { c ->
                put(JSONObject().apply {
                    put("id", c.id)
                    put("title", c.title)
                    put("target", c.target)
                    put("checkins", c.checkins)
                    put("startedAt", c.startedAt)
                    put("lastCheckin", c.lastCheckin)
                })
            }
        })
        root.put("accounts", JSONArray().apply { accounts.forEach { put(it.toJson()) } })
        root.put("accountDevices", JSONArray().apply { accountDevices.forEach { put(it.toJson()) } })
        root.put("accountRelations", JSONArray().apply { accountRelations.forEach { put(it.toJson()) } })
        root.put("screenshots", JSONArray().apply { screenshots.forEach { put(it.toJson()) } })
        root.put("linkedNotes", JSONArray().apply { linkedNotes.forEach { put(it.toJson()) } })
        root.put("extras", JSONArray().apply { extras.forEach { put(it.toJson()) } })
        root.put("shields", shields)
        root.put("frozenDay", frozenDay)
        root.put("starterSeeded", starterSeeded)
        root.put("habits", JSONArray().apply {
            habits.forEach { h ->
                put(JSONObject().apply {
                    put("id", h.id)
                    put("title", h.title)
                    put("streak", h.streak)
                    put("best", h.best)
                    put("lastDay", h.lastDay)
                })
            }
        })
        root.put("smsSeen", JSONArray().apply { smsSeen.forEach { put(it) } })
        root.put("blockerOn", blockerOn)
        root.put("blockStart", blockStart)
        root.put("blockEnd", blockEnd)
        root.put("wallets", JSONArray().apply {
            wallets.forEach { w ->
                put(JSONObject().apply {
                    put("id", w.id)
                    put("name", w.name)
                    put("balance", w.balance)
                })
            }
        })
        root.put("notifications", JSONArray().apply {
            notifications.forEach { n ->
                put(JSONObject().apply {
                    put("id", n.id)
                    put("pkg", n.pkg)
                    put("title", n.title)
                    put("text", n.text)
                    put("at", n.at)
                })
            }
        })
        root.put("vault", JSONArray().apply {
            vault.forEach { v ->
                put(JSONObject().apply {
                    put("id", v.id)
                    put("payload", v.payload)
                    put("at", v.at)
                })
            }
        })
        root.put("blockedApps", JSONArray().apply { blockedApps.forEach { put(it) } })
        root.put("blockedDomains", JSONArray().apply { blockedDomains.forEach { put(it) } })

        root.put("tasks", JSONArray().apply {
            tasks.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("note", t.note)
                    put("priority", t.priority.name)
                    put("done", t.done)
                    put("createdAt", t.createdAt)
                    put("dueAt", t.dueAt)
                    put("repeat", t.repeat)
                    put("list", t.list)
                })
            }
        })
        root.put("debts", JSONArray().apply {
            debts.forEach { debt ->
                put(JSONObject().apply {
                    put("id", debt.id)
                    put("person", debt.person)
                    put("amount", debt.amount)
                    put("owedByMe", debt.owedByMe)
                    put("note", debt.note)
                    put("settled", debt.settled)
                    put("at", debt.at)
                })
            }
        })
        root.put("ledger", JSONArray().apply {
            ledger.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("amount", t.amount)
                    put("income", t.income)
                    put("category", t.category)
                    put("at", t.at)
                })
            }
        })
        root.put("workouts", JSONArray().apply {
            workouts.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("kind", t.kind)
                    put("minutes", t.minutes)
                    put("at", t.at)
                })
            }
        })
        root.put("journal", JSONArray().apply {
            journal.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("body", t.body)
                    put("at", t.at)
                })
            }
        })
        root.put("water", JSONObject().apply {
            water.forEach { (day, cups) -> put(day, cups) }
        })
        root.put("prayers", JSONObject().apply {
            prayers.forEach { (day, ids) ->
                put(day, JSONArray().apply { ids.forEach { put(it) } })
            }
        })
        root.put("sleep", JSONArray().apply {
            sleep.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("hours", t.hours)
                    put("at", t.at)
                })
            }
        })
        root.put("learn", JSONArray().apply {
            learn.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("minutes", t.minutes)
                    put("target", t.target)
                    put("rewarded", t.rewarded)
                })
            }
        })
        root.put("books", JSONArray().apply {
            books.forEach { t ->
                put(JSONObject().apply {
                    put("id", t.id)
                    put("title", t.title)
                    put("page", t.page)
                    put("pages", t.pages)
                    put("rewarded", t.rewarded)
                })
            }
        })
        root.put("faithDays", JSONArray().apply { faithDays.forEach { put(it) } })
        root.put("sleepDays", JSONArray().apply { sleepDays.forEach { put(it) } })

        return root.toString()
    }

    private fun save() {
        val text = snapshot()
        io.execute {
            try {
                val temp = File(file.parentFile, "state.tmp")
                FileOutputStream(temp).use { out ->
                    out.write(text.toByteArray(Charsets.UTF_8))
                    out.fd.sync()
                }
                try {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
                } catch (e: AtomicMoveNotSupportedException) {
                    Files.move(temp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING)
                }
            } catch (t: Throwable) {
                Log.e("Store", "Failed to save state", t)
            }
        }
    }

    private fun preserveCorrupt() {
        runCatching {
            file.copyTo(File(file.parentFile, "state.corrupt.json"), overwrite = true)
        }
    }

    companion object {
        @Volatile
        private var shared: Store? = null

        fun of(context: Context): Store {
            shared?.let { return it }
            return synchronized(this) {
                shared ?: Store(context.applicationContext).also { shared = it }
            }
        }
    }

    private fun read(restored: JSONObject? = null): Boolean {
        if (restored == null && !file.exists()) return true
        return try {
            val root = restored ?: JSONObject(file.readText())
            onboarded = root.optBoolean("onboarded", false)
            points = root.optInt("points", 0)
            xp = root.optInt("xp", 0)
            energy = root.optInt("energy", config.energyMax).coerceIn(0, config.energyMax)
            energyDay = root.optString("energyDay", energyDay)
            seq = root.optLong("seq", 1L).coerceAtLeast(1L)
            profileName = root.optString("profileName", "")
            profileAge = root.optInt("profileAge", 0)
            profileGoal = root.optString("profileGoal", "")
            profileTeam = root.optString("profileTeam", "")
            joinedAt = root.optLong("joinedAt", 0L)
            serverUrl = root.optString("serverUrl", config.apiBase)
            authToken = root.optString("authToken", "")
            accountId = root.optLong("accountId", 0L)
            accountName = root.optString("accountName", "")
            accountEmail = root.optString("accountEmail", "")
            accent = root.optString("accent", "violet")
            avatar = root.optInt("avatar", 0).coerceIn(0, 7)
            premium = root.optBoolean("premium", false)
            licenseCode = root.optString("licenseCode", "")
            campaigns = root.optJSONArray("campaigns").mapObjects {
                Campaign(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    target = it.optInt("target", 1),
                    checkins = it.optInt("checkins", 0),
                    startedAt = it.optLong("startedAt"),
                    lastCheckin = it.optLong("lastCheckin")
                )
            }
            smsSeen = root.stringSet("smsSeen")
            blockerOn = root.optBoolean("blockerOn", false)
            blockStart = root.optInt("blockStart", 22 * 60).coerceIn(0, 1439)
            blockEnd = root.optInt("blockEnd", 6 * 60).coerceIn(0, 1439)
            wallets = root.optJSONArray("wallets").mapObjects {
                Wallet(
                    id = it.optLong("id"),
                    name = it.optString("name"),
                    balance = it.optDouble("balance", 0.0)
                )
            }
            notifications = root.optJSONArray("notifications").mapObjects {
                NotifEntry(
                    id = it.optLong("id"),
                    pkg = it.optString("pkg"),
                    title = it.optString("title"),
                    text = it.optString("text"),
                    at = it.optLong("at")
                )
            }
            vault = root.optJSONArray("vault").mapObjects {
                VaultNote(
                    id = it.optLong("id"),
                    payload = it.optString("payload"),
                    at = it.optLong("at")
                )
            }
            blockedApps = root.stringSet("blockedApps")
            blockedDomains = root.optJSONArray("blockedDomains").strings()

            tasks = root.optJSONArray("tasks").mapObjects {
                Task(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    note = it.optString("note"),
                    priority = priorityOf(it.optString("priority")),
                    done = it.optBoolean("done"),
                    createdAt = it.optLong("createdAt"),
                    dueAt = it.optLong("dueAt"),
                    repeat = it.optString("repeat"),
                    list = it.optString("list")
                )
            }
            debts = root.optJSONArray("debts").mapObjects {
                Debt(
                    id = it.optLong("id"),
                    person = it.optString("person"),
                    amount = it.optDouble("amount", 0.0),
                    owedByMe = it.optBoolean("owedByMe"),
                    note = it.optString("note"),
                    settled = it.optBoolean("settled"),
                    at = it.optLong("at")
                )
            }
            ledger = root.optJSONArray("ledger").mapObjects {
                Ledger(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    amount = it.optDouble("amount", 0.0),
                    income = it.optBoolean("income"),
                    category = it.optString("category", "other"),
                    at = it.optLong("at")
                )
            }
            workouts = root.optJSONArray("workouts").mapObjects {
                Workout(
                    id = it.optLong("id"),
                    kind = it.optString("kind", "strength"),
                    minutes = it.optInt("minutes"),
                    at = it.optLong("at")
                )
            }
            journal = root.optJSONArray("journal").mapObjects {
                JournalEntry(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    body = it.optString("body"),
                    at = it.optLong("at")
                )
            }
            water = root.optJSONObject("water").toIntMap()
            prayers = root.optJSONObject("prayers").toSetMap()
            sleep = root.optJSONArray("sleep").mapObjects {
                SleepLog(
                    id = it.optLong("id"),
                    hours = it.optDouble("hours", 0.0),
                    at = it.optLong("at")
                )
            }
            learn = root.optJSONArray("learn").mapObjects {
                LearnItem(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    minutes = it.optInt("minutes"),
                    target = it.optInt("target"),
                    rewarded = it.optBoolean("rewarded")
                )
            }
            books = root.optJSONArray("books").mapObjects {
                Book(
                    id = it.optLong("id"),
                    title = it.optString("title"),
                    page = it.optInt("page"),
                    pages = it.optInt("pages"),
                    rewarded = it.optBoolean("rewarded")
                )
            }
            accounts = root.optJSONArray("accounts").mapObjects { it.toAccount() }
            accountDevices = root.optJSONArray("accountDevices").mapObjects { it.toAccountDevice() }
            accountRelations = root.optJSONArray("accountRelations").mapObjects { it.toAccountRelation() }
            screenshots = root.optJSONArray("screenshots").mapObjects { it.toScreenshot() }
            linkedNotes = root.optJSONArray("linkedNotes").mapObjects { it.toLinkedNote() }
            extras = root.optJSONArray("extras").mapObjects { it.toExtra() }
            shields = root.optInt("shields", 0)
            frozenDay = root.optString("frozenDay", "")
            starterSeeded = root.optBoolean("starterSeeded", false)
            habits = root.optJSONArray("habits").mapObjects {
                Habit(it.optLong("id"), it.optString("title"), it.optInt("streak"), it.optInt("best"), it.optString("lastDay"))
            }
            faithDays = root.stringSet("faithDays")
            sleepDays = root.stringSet("sleepDays")
            true
        } catch (t: Throwable) {
            Log.e("Store", "Failed to read state", t)
            if (restored == null) preserveCorrupt()
            false
        }
    }
}

private fun priorityOf(value: String): Priority =
    runCatching { Priority.valueOf(value) }.getOrDefault(Priority.NORMAL)

private inline fun <T> JSONArray?.mapObjects(block: (JSONObject) -> T): List<T> {
    if (this == null) return emptyList()
    val out = ArrayList<T>(length())
    for (i in 0 until length()) out.add(block(optJSONObject(i) ?: JSONObject()))
    return out
}

private fun JSONObject?.toIntMap(): Map<String, Int> {
    if (this == null) return emptyMap()
    val out = mutableMapOf<String, Int>()
    val keys = keys()
    while (keys.hasNext()) {
        val key = keys.next()
        out[key] = optInt(key, 0)
    }
    return out
}

private fun JSONObject?.toSetMap(): Map<String, Set<String>> {
    if (this == null) return emptyMap()
    val out = mutableMapOf<String, Set<String>>()
    val keys = keys()
    while (keys.hasNext()) {
        val key = keys.next()
        val arr = optJSONArray(key) ?: continue
        val set = mutableSetOf<String>()
        for (i in 0 until arr.length()) set.add(arr.optString(i))
        out[key] = set
    }
    return out
}

private fun JSONArray?.strings(): List<String> {
    if (this == null) return emptyList()
    val out = ArrayList<String>(length())
    for (i in 0 until length()) out.add(optString(i))
    return out
}

private fun JSONObject.stringSet(key: String): Set<String> {
    val arr = optJSONArray(key) ?: return emptySet()
    val out = mutableSetOf<String>()
    for (i in 0 until arr.length()) out.add(arr.optString(i))
    return out
}


private fun AccountItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("username", username)
    put("email", email)
    put("password", password ?: "")
    put("totp", totp ?: "")
    put("layer", layer)
    put("category", category)
    put("recoveryEmail", recoveryEmail)
    put("recoveryPhone", recoveryPhone)
    put("codes", codes ?: "")
    put("notes", notes ?: "")
    put("updatedAt", updatedAt)
}

private fun JSONObject.toAccount(): AccountItem = AccountItem(
    optLong("id"), optString("title"), optString("username"), optString("email"),
    optString("password").ifBlank { null }, optString("totp").ifBlank { null },
    optString("layer", "normal"), optString("category", "other"),
    optString("recoveryEmail"), optString("recoveryPhone"),
    optString("codes").ifBlank { null }, optString("notes").ifBlank { null },
    optLong("updatedAt")
)

private fun AccountDevice.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("name", name)
    put("platform", platform)
    put("lastSeen", lastSeen)
    put("current", current)
}

private fun JSONObject.toAccountDevice(): AccountDevice = AccountDevice(
    optLong("id"), optString("name"), optString("platform", "android"),
    optLong("lastSeen"), optBoolean("current")
)

private fun AccountRelation.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("fromId", fromId)
    put("toId", toId)
    put("kind", kind)
}

private fun JSONObject.toAccountRelation(): AccountRelation =
    AccountRelation(optLong("id"), optLong("fromId"), optLong("toId"), optString("kind"))

private fun ScreenshotItem.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("label", label)
    put("mime", mime)
    put("payload", payload)
    put("at", at)
}

private fun JSONObject.toScreenshot(): ScreenshotItem =
    ScreenshotItem(optLong("id"), optString("label"), optString("mime", "image/jpeg"), optString("payload"), optLong("at"))

private fun LinkedNote.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("title", title)
    put("body", body)
    put("tags", tags)
    put("at", at)
}

private fun JSONObject.toLinkedNote(): LinkedNote =
    LinkedNote(optLong("id"), optString("title"), optString("body"), optString("tags"), optLong("at"))

private fun ExtraRecord.toJson(): JSONObject = JSONObject().apply {
    put("id", id)
    put("kind", kind)
    put("title", title)
    put("detail", detail)
    put("amount", amount)
    put("status", status)
    put("dueAt", dueAt)
    put("createdAt", createdAt)
}

private fun JSONObject.toExtra(): ExtraRecord = ExtraRecord(
    optLong("id"), optString("kind"), optString("title"), optString("detail"),
    optDouble("amount", 0.0), optString("status", "open"), optLong("dueAt"), optLong("createdAt")
)

const val DEFAULT_WALLET = "الحساب الافتراضي"

private const val LESSON_POINTS = 5

private fun csvCols(line: String): List<String> {
    val out = ArrayList<String>()
    val cur = StringBuilder()
    var quoted = false
    var i = 0
    while (i < line.length) {
        val c = line[i]
        when {
            c == '"' && quoted && i + 1 < line.length && line[i + 1] == '"' -> {
                cur.append('"')
                i++
            }
            c == '"' -> quoted = !quoted
            c == ',' && !quoted -> {
                out.add(cur.toString().trim())
                cur.setLength(0)
            }
            else -> cur.append(c)
        }
        i++
    }
    out.add(cur.toString().trim())
    return out
}
