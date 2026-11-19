package data.local

import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import core.config.AppConfig
import core.security.VaultCrypto
import domain.model.Book
import domain.model.JournalEntry
import domain.model.LearnItem
import domain.model.Ledger
import domain.model.NotifEntry
import domain.model.Priority
import domain.model.SleepLog
import domain.model.Task
import domain.model.VaultNote
import domain.model.Wallet
import domain.model.Workout
import domain.model.prayerKeys
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
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
    private var faithDays by mutableStateOf<Set<String>>(emptySet())
    private var sleepDays by mutableStateOf<Set<String>>(emptySet())

    init {
        read()
        ensureDay(false)
    }

    val today: String
        get() = LocalDate.now().toString()

    val level: Int
        get() = if (config.xpPerLevel <= 0) 1 else xp / config.xpPerLevel + 1

    val levelProgress: Float
        get() = if (config.xpPerLevel <= 0) 0f else (xp % config.xpPerLevel) / config.xpPerLevel.toFloat()

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

    fun deleteWallet(id: Long) {
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

    fun addTask(title: String, note: String, priority: Priority) {
        if (title.isBlank()) return
        tasks = tasks + Task(nextId(), title.trim(), note.trim(), priority, false, System.currentTimeMillis())
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
        } else {
            energy = (energy + config.energyCost).coerceAtMost(config.energyMax)
            takePoints(config.pointsTask)
            tasks = tasks.map { if (it.id == id) it.copy(done = false) else it }
        }
        save()
        return true
    }

    fun deleteTask(id: Long) {
        tasks = tasks.filterNot { it.id == id }
        save()
    }

    fun addLedger(title: String, amount: Double, income: Boolean, category: String) {
        if (title.isBlank() || amount <= 0.0) return
        ledger = ledger + Ledger(nextId(), title.trim(), amount, income, category, System.currentTimeMillis())
        save()
    }

    fun deleteLedger(id: Long) {
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

    private fun nextId(): Long = seq++

    private fun dayOf(at: Long): String =
        Instant.ofEpochMilli(at).atZone(ZoneId.systemDefault()).toLocalDate().toString()

    private fun save() {
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

        val text = root.toString()
        io.execute {
            try {
                val temp = File(file.parentFile, "state.tmp")
                temp.writeText(text)
                if (!temp.renameTo(file)) {
                    file.delete()
                    temp.renameTo(file)
                }
            } catch (t: Throwable) {
                Log.e("Store", "Failed to save state", t)
            }
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

    private fun read() {
        if (!file.exists()) return
        try {
            val root = JSONObject(file.readText())
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
                    createdAt = it.optLong("createdAt")
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
            faithDays = root.stringSet("faithDays")
            sleepDays = root.stringSet("sleepDays")
        } catch (t: Throwable) {
            Log.e("Store", "Failed to read state", t)
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
