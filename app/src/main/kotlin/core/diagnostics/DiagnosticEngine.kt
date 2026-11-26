// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.diagnostics

import org.json.JSONArray
import org.json.JSONObject

data class DiagnosticIssue(
    val signature: String,
    val section: String,
    val title: String,
    val causes: List<String>,
    val steps: List<String>,
    val maintenance: String? = null,
    val readings: JSONObject = JSONObject()
)

object DiagnosticEngine {

    fun analyze(input: JSONObject): JSONObject {
        val issues = ArrayList<DiagnosticIssue>()
        val sections = ArrayList<String>()
        val unavailableNote = StringBuilder()

        fun available(o: JSONObject?, key: String): Boolean =
            o != null && o.has(key) && !o.isNull(key)

        fun missing(o: JSONObject?) {
            val arr = o?.optJSONArray("unavailable") ?: return
            val names = ArrayList<String>()
            for (i in 0 until arr.length()) names.add(arr.optString(i))
            if (names.isNotEmpty()) {
                unavailableNote.append("غير متاح على هذا الجهاز: ")
                    .append(names.joinToString("، ")).append("\n")
            }
        }

        val device = input.optJSONObject("device_info")
        val power = input.optJSONObject("power")
        val storage = input.optJSONObject("storage")
        val wifi = input.optJSONObject("wifi")
        val sim = input.optJSONObject("sim_network")
        val general = input.optJSONObject("system_general")
        val symptoms = input.optJSONArray("user_symptoms").stringList()
        val environment = input.optJSONObject("environment_context")

        // ---------- الطاقة ----------
        val powerLines = ArrayList<String>()
        if (power != null) {
            missing(power)
            val charging = power.optBoolean("is_charging", false)
            val type = power.optString("charge_type", "NONE")
            val level = if (available(power, "battery_level")) power.optInt("battery_level").toString() + "%" else "غير متاح على هذا الجهاز"
            val voltage = if (available(power, "voltage_mv")) power.optDouble("voltage_mv").toString() + " mV" else "غير متاح على هذا الجهاز"
            val current = if (available(power, "current_ma")) power.optDouble("current_ma").toString() + " mA" else "غير متاح على هذا الجهاز"
            val watts = if (available(power, "voltage_mv") && available(power, "current_ma")) {
                power.optDouble("voltage_mv") / 1000.0 * kotlin.math.abs(power.optDouble("current_ma")) / 1000.0
            } else null
            powerLines.add("الشحن: ${if (charging) "نشط ($type)" else "غير موصول"} · المستوى: $level")
            powerLines.add("الفولتية: $voltage · التيار: $current")
            if (watts != null) {
                val classify = when { watts >= 15 -> "طبيعية/سريعة"; watts >= 5 -> "طبيعية"; watts > 0 -> "منخفضة"; else -> "غير قابلة للقياس" }
                powerLines.add("القدرة التقريبية: %.1f W — %s".format(watts, classify))
            } else powerLines.add("القدرة التقريبية: غير متاحة على هذا الجهاز (الفولتية أو التيار مفقود).")
            if (available(power, "temperature_c")) {
                val t = power.optDouble("temperature_c")
                powerLines.add("الحرارة: %.1f°C — %s".format(t, when { t > 42 -> "مرتفعة"; t >= 38 -> "دافئة"; else -> "طبيعية" }))
                if (t > 42) {
                    issues.add(
                        DiagnosticIssue(
                            "high_battery_temp", "power", "حرارة بطارية مرتفعة",
                            listOf("شحن سريع أو شاحن غير متوافق", "غطاء/بيئة حارة", "حمل ثقيل أثناء الشحن"),
                            listOf("أوقف الشحن السريع مؤقتاً.", "أزل الغلاف وابتعد عن الشمس.", "توقف عن الشحن حتى تنخفض الحرارة تحت 38°C."),
                            readings = JSONObject().put("temperature_c", t).put("charge_type", type)
                        )
                    )
                }
            }
            val health = power.optString("health", "UNKNOWN")
            if (health != "GOOD" && health != "UNKNOWN") {
                issues.add(
                    DiagnosticIssue(
                        "battery_health_" + health.lowercase(), "power", "حالة بطارية غير سليمة: $health",
                        listOf("تلف خلية أو حساس حرارة", "شاحن غير متوافق"),
                        listOf("جرّب شاحناً أصلياً وكابلاً آخر.", "راقب الحرارة والانتفاخ."),
                        maintenance = "إن استمرت الحالة، يُحال الجهاز لمركز صيانة لفحص البطارية.",
                        readings = JSONObject().put("health", health)
                    )
                )
            }
            if (power.optString("note").isNotBlank()) powerLines.add(power.optString("note"))
            sections.add("## الطاقة والشحن (فولتية / تيار / قدرة)\n" + powerLines.joinToString("\n"))
        }

        // ---------- الأسباب المرتّبة لشكوى اللمس أثناء الشحن ----------
        val touchCharging = symptoms.any { it.contains("لمس") || it.contains("كيبورد") || it.contains("تعليق") } &&
            (power?.optBoolean("is_charging", false) == true)
        val rankedCauses = ArrayList<String>()
        if (touchCharging) {
            rankedCauses.addAll(
                listOf(
                    "تشويش/تموج تيار الشاحن (Ripple/EMI) يتسرب لخط اللمس (Touch Controller).",
                    "ضعف تأريض أو عدم استقرار مصدر الكهرباء (مقبس/مشترك قديم).",
                    "كابل أو منفذ شحن رديء/متآكل.",
                    "حرارة بطارية مرتفعة تؤثر على استجابة اللمس.",
                    "واقي شاشة غير متوافق يتفاعل مع التشويش الكهربائي عند الشحن."
                )
            )
            issues.add(
                DiagnosticIssue(
                    "touch_lag_during_ac_charging", "power", "تعليق/تكرار لمسات أثناء الشحن",
                    rankedCauses,
                    listOf("جرّب شاحناً أصلياً ومقبساً مؤرضاً.", "بدّل الكابل ونظّف المنفذ.", "أزل واقي الشاشة مؤقتاً للتحقق.", "أوقف الشحن حتى تبرد البطارية."),
                    readings = JSONObject()
                        .put("charge_type", power?.optString("charge_type", "NONE") ?: "NONE")
                        .put("voltage_mv", power?.opt("voltage_mv") ?: JSONObject.NULL)
                        .put("current_ma", power?.opt("current_ma") ?: JSONObject.NULL)
                        .put("temperature_c", power?.opt("temperature_c") ?: JSONObject.NULL)
                )
            )
        }

        // ---------- التخزين ----------
        val fixActions = JSONArray()
        if (storage != null) {
            missing(storage)
            val lines = ArrayList<String>()
            val total = if (available(storage, "internal_total_gb")) storage.optDouble("internal_total_gb") else null
            val free = if (available(storage, "internal_free_gb")) storage.optDouble("internal_free_gb") else null
            val cache = if (available(storage, "cache_size_mb")) storage.optDouble("cache_size_mb") else null
            val junk = if (available(storage, "large_junk_files_mb")) storage.optDouble("large_junk_files_mb") else null
            val read = if (available(storage, "internal_read_speed_mbps")) storage.optDouble("internal_read_speed_mbps") else null
            val write = if (available(storage, "internal_write_speed_mbps")) storage.optDouble("internal_write_speed_mbps") else null
            if (total != null && free != null) {
                val ratio = if (total > 0) free / total else 0.0
                lines.add("الداخلي: %.1f GB متاح من %.1f GB (%.0f%% فارغ)".format(free, total, ratio * 100))
                if (ratio < 0.10) {
                    issues.add(
                        DiagnosticIssue(
                            "low_internal_storage", "storage", "امتلاء التخزين الداخلي",
                            listOf("تراكم الكاش", "ملفات مؤقتة كبيرة", "تطبيقات/وسائط ثقيلة"),
                            listOf("امسح الكاش المؤقت.", "انقل الوسائط لبطاقة SD أو احذف تطبيقات ثقيلة.", "أبقِ 10٪ على الأقل فارغة."),
                            readings = JSONObject().put("internal_free_gb", free).put("internal_total_gb", total)
                        )
                    )
                }
            }
            if (read != null && write != null) {
                lines.add("سرعة القراءة/الكتابة: %.1f / %.1f MB/s".format(read, write))
                if (read < 30 || write < 20) {
                    issues.add(
                        DiagnosticIssue(
                            "slow_internal_storage", "storage", "بطء القراءة/الكتابة في التخزين الداخلي",
                            listOf("امتلاء شديد", "تدهور شريحة التخزين", "ضغط إدخال/إخراج خلفي"),
                            listOf("أغلق التطبيقات الخلفية الثقيلة.", "أعد القياس بعد مسح الكاش."),
                            maintenance = "إن تكرر البطء بعد التهيئة، يُحال لمركز صيانة لفحص شريحة التخزين.",
                            readings = JSONObject().put("read_mbps", read).put("write_mbps", write)
                        )
                    )
                }
            }
            if (cache != null) lines.add("الكاش: %.1f MB".format(cache))
            if (junk != null) lines.add("ملفات كبيرة مؤقتة: %.1f MB".format(junk))
            val sdPresent = storage.optBoolean("external_sd_present", false)
            if (sdPresent) {
                val sdTotal = if (available(storage, "external_sd_total_gb")) storage.optDouble("external_sd_total_gb") else null
                val sdFree = if (available(storage, "external_sd_free_gb")) storage.optDouble("external_sd_free_gb") else null
                lines.add("بطاقة SD مكتشفة: " + (if (sdTotal != null && sdFree != null) "%.1f GB متاح من %.1f GB".format(sdFree, sdTotal) else "غير متاحة القراءة على هذا الجهاز"))
                val sdHealth = storage.optString("external_sd_health", "unknown")
                if (sdHealth == "unreadable" || sdHealth == "errors_detected") {
                    issues.add(
                        DiagnosticIssue(
                            "sd_card_" + sdHealth, "storage", "مشكلة في بطاقة SD",
                            listOf("تلف نظام الملفات", "بطاقة غير متوافقة"),
                            listOf("انسخ بياناتك أولاً.", "تهيئة البطاقة تحذف كل محتواها."),
                            maintenance = "إن لم تزل الأخطاء بعد التهيئة، يُحال لمركز صيانة/استبدال البطاقة.",
                            readings = JSONObject().put("external_sd_health", sdHealth)
                        )
                    )
                }
            } else {
                lines.add("لا توجد ذاكرة خارجية مكتشفة؛ لا تُطلب تفاصيل عنها.")
            }
            if (storage.optString("note").isNotBlank()) lines.add(storage.optString("note"))
            sections.add("## الذاكرة والتخزين (داخلي + خارجي إن وُجد)\n" + lines.joinToString("\n"))
            if ((cache != null && cache > 300) || (free != null && total != null && total > 0 && free / total < 0.10)) {
                fixActions.put(
                    JSONObject()
                        .put("id", "clear_cache")
                        .put("target", "storage")
                        .put("description_ar", "مسح ملفات الكاش المؤقتة للتطبيق لتحرير %.1f MB".format(cache ?: 0.0))
                        .put("requires_user_confirmation", true)
                        .put("reversible", true)
                )
            }
            if (junk != null && junk > 100) {
                fixActions.put(
                    JSONObject()
                        .put("id", "trim_temp_files")
                        .put("target", "storage")
                        .put("description_ar", "حذف الملفات المؤقتة الكبيرة في كاش التطبيق فقط (%.1f MB)".format(junk))
                        .put("requires_user_confirmation", true)
                        .put("reversible", true)
                )
            }
        }

        // ---------- الواي فاي ----------
        if (wifi != null) {
            missing(wifi)
            val lines = ArrayList<String>()
            val connected = wifi.optBoolean("connected", false)
            if (!connected) {
                lines.add("غير متصل بشبكة واي فاي حالياً.")
            } else {
                val dbm = if (available(wifi, "signal_dbm")) wifi.optInt("signal_dbm") else null
                val link = if (available(wifi, "link_speed_mbps")) wifi.optInt("link_speed_mbps") else null
                val band = wifi.optString("frequency_band", "unknown")
                val dns = if (available(wifi, "dns_latency_ms")) wifi.optLong("dns_latency_ms") else null
                lines.add("النطاق: $band · الإشارة: ${dbm?.toString() ?: "غير متاح على هذا الجهاز"} dBm · سرعة الربط: ${link?.toString() ?: "غير متاح"} Mbps")
                lines.add("SSID: بصمة مجهّلة فقط · DNS: ${dns?.toString()?.plus(" ms") ?: "غير متاح على هذا الجهاز"}")
                if (dbm != null && dbm < -70) {
                    issues.add(
                        DiagnosticIssue(
                            "weak_wifi_signal", "wifi", "إشارة واي فاي ضعيفة",
                            listOf("بعد عن الراوتر/حواجز", "ازدحام القناة", "استخدام 2.4GHz رغم توفر 5GHz"),
                            listOf("اقترب من الراوتر.", "بدّل إلى 5GHz عند القرب.", "غيّر قناة الراوتر عند الازدحام."),
                            readings = JSONObject().put("signal_dbm", dbm).put("frequency_band", band)
                        )
                    )
                }
                if (dns != null && dns > 150) {
                    issues.add(
                        DiagnosticIssue(
                            "slow_dns", "wifi", "زمن استجابة DNS مرتفع",
                            listOf("DNS المزوّد بطيء", "ازدحام الشبكة"),
                            listOf("جرّب DNS عاماً مثل 1.1.1.1 أو 8.8.8.8 على الراوتر.", "أعد القياس بعد التغيير."),
                            readings = JSONObject().put("dns_latency_ms", dns)
                        )
                    )
                }
                if (link != null && link < 20) {
                    lines.add("سرعة ربط منخفضة؛ خطوات التحسين مذكورة أدناه.")
                }
            }
            if (wifi.optString("note").isNotBlank()) lines.add(wifi.optString("note"))
            sections.add("## الواي فاي والشبكة اللاسلكية\n" + lines.joinToString("\n"))
        }

        // ---------- الشريحة ----------
        if (sim != null) {
            missing(sim)
            val lines = ArrayList<String>()
            lines.add("الشريحة: ${if (sim.optBoolean("sim_present", false)) "موجودة" else "غير مكتشفة"} · النوع: ${if (available(sim, "sim_type")) sim.optString("sim_type") else "غير متاح على هذا الجهاز"}")
            lines.add("المزوّد: ${if (available(sim, "carrier_name")) sim.optString("carrier_name") else "غير متاح على هذا الجهاز"} · الشبكة: ${sim.optString("network_type", "unknown")}")
            lines.add("التجوال: ${if (sim.optBoolean("roaming", false)) "مفعّل" else "غير مفعّل"} · قفل الشبكة: ${sim.optString("network_lock_status", "unknown")}")
            val dbm = if (available(sim, "signal_strength_dbm")) sim.optInt("signal_strength_dbm") else null
            if (dbm != null) {
                lines.add("قوة الإشارة: $dbm dBm")
                if (dbm < -105) {
                    issues.add(
                        DiagnosticIssue(
                            "weak_cellular_signal", "sim_network", "ضعف إشارة الشريحة",
                            listOf("ضعف تغطية", "شريحة قديمة", "تذبذب 4G/3G"),
                            listOf("غيّر المكان أو اقترب من نافذة.", "أعد اختيار الشبكة يدوياً.", "استبدل الشريحة إن كانت قديمة."),
                            readings = JSONObject().put("signal_strength_dbm", dbm)
                        )
                    )
                }
            }
            if (sim.optString("note").isNotBlank()) lines.add(sim.optString("note"))
            sections.add("## الشريحة وشبكة الاتصال\n" + lines.joinToString("\n"))
        }

        // ---------- عام ----------
        if (general != null) {
            missing(general)
            val lines = ArrayList<String>()
            val total = if (available(general, "ram_total_mb")) general.optDouble("ram_total_mb") else null
            val free = if (available(general, "ram_free_mb")) general.optDouble("ram_free_mb") else null
            val cpu = if (available(general, "cpu_temp_c")) general.optDouble("cpu_temp_c") else null
            val background = if (available(general, "running_background_apps_count")) general.optInt("running_background_apps_count") else null
            if (total != null && free != null) {
                lines.add("RAM: %.0f MB متاح من %.0f MB (%.0f%%)".format(free, total, if (total > 0) free / total * 100 else 0.0))
                if (total > 0 && free / total < 0.15) {
                    issues.add(
                        DiagnosticIssue(
                            "low_ram", "system_general", "ذاكرة عشوائية منخفضة",
                            listOf("تطبيقات خلفية كثيرة", "ضغط ذاكرة عام"),
                            listOf("أغلق التطبيقات غير المستخدمة.", "أعد تشغيل الجهاز."),
                            readings = JSONObject().put("ram_free_mb", free).put("ram_total_mb", total)
                        )
                    )
                }
            }
            if (cpu != null) lines.add("حرارة المعالج: %.1f°C".format(cpu))
            if (background != null) lines.add("العمليات الخلفية: $background (تقريبي)")
            if (general.optBoolean("screen_touch_issue_reported", false) && !touchCharging) {
                lines.add("مشكلة لمس مُبلَّغ عنها مستقلة عن الشحن: تُصنَّف عتاداً محتملاً منفصلاً.")
            }
            if (general.optString("note").isNotBlank()) lines.add(general.optString("note"))
            sections.add("## فحوصات عامة\n" + lines.joinToString("\n"))
        }

        // ---------- التقرير ----------
        val report = StringBuilder()
        report.appendLine("## ملخص الحالة")
        report.appendLine("الجهاز: ${device?.optString("brand", "") ?: ""} ${device?.optString("model", "") ?: ""} · أندرويد ${device?.optString("android_version", "") ?: ""} · ROM ${device?.optString("rom_version", "") ?: ""}")
        report.appendLine("الأعراض المُبلَّغ عنها: ${if (symptoms.isEmpty()) "لا توجد" else symptoms.joinToString("، ")}")
        if (environment != null) {
            val env = listOf("charger_age", "power_source_notes", "screen_protector", "humidity_notes")
                .mapNotNull { k -> environment.optString(k).takeIf { it.isNotBlank() }?.let { "$k: $it" } }
            if (env.isNotEmpty()) report.appendLine("السياق: " + env.joinToString(" · "))
        }
        report.appendLine("المشاكل المكتشفة: ${issues.size} · أوامر الإصلاح المقترحة: ${fixActions.length()}")
        report.appendLine()
        sections.forEach { report.appendLine(it).appendLine() }
        report.appendLine("## الأسباب المحتملة لكل مشكلة (مرتبة حسب الاحتمالية)")
        if (issues.isEmpty()) report.appendLine("لا مشاكل حقيقية مكتشفة في القراءات المُرسلة.")
        else issues.forEach { issue ->
            report.appendLine("### ${issue.title} — `${issue.signature}`")
            issue.causes.forEachIndexed { i, c -> report.appendLine("${i + 1}. $c") }
        }
        report.appendLine()
        report.appendLine("## ما تم إصلاحه تلقائياً")
        report.appendLine("لم ينفَّذ أي إصلاح بعد؛ أوامر الإصلاح تحتاج موافقتك.")
        report.appendLine()
        report.appendLine("## خطوات مطلوبة منك")
        val steps = issues.flatMap { it.steps }.distinct()
        if (steps.isEmpty()) report.appendLine("لا خطوات مطلوبة؛ كل القراءات ضمن الطبيعي.")
        else steps.forEachIndexed { i, s -> report.appendLine("${i + 1}. $s") }
        val maintenance = issues.mapNotNull { it.maintenance }.distinct()
        if (maintenance.isNotEmpty()) {
            report.appendLine()
            report.appendLine("## توصية صيانة")
            maintenance.forEach { report.appendLine("- $it") }
        }
        if (unavailableNote.isNotBlank()) {
            report.appendLine()
            report.appendLine("## ملاحظات توافر القراءات")
            report.append(unavailableNote.toString().trim())
            report.appendLine()
            report.appendLine("بعض القراءات تقريبية أو مقيدة حسب الشركة المصنعة والـROM.")
        }

        val serverReports = JSONArray()
        val symptomsJson = JSONArray().apply { symptoms.forEach { put(it) } }
        for (issue in issues) {
            serverReports.put(
                JSONObject()
                    .put("issue_signature", issue.signature)
                    .put("symptoms", symptomsJson)
                    .put("device_model", device?.optString("model", "") ?: "")
                    .put("android_version", device?.optString("android_version", "") ?: "")
                    .put("rom_version", device?.optString("rom_version", "") ?: "")
                    .put("relevant_readings", issue.readings)
                    .put("diagnosis_ranked_causes", JSONArray().apply { issue.causes.forEach { put(it) } })
                    .put("attempted_fix_actions", JSONArray())
                    .put("resolved_locally", false)
            )
        }
        val primary = if (serverReports.length() > 0) serverReports.optJSONObject(0) else JSONObject.NULL
        return JSONObject()
            .put("user_report_markdown", report.toString())
            .put("fix_actions", fixActions)
            .put("server_report", primary)
            .put("server_reports", serverReports)
            .put("issues", JSONArray().apply { issues.forEach { put(it.signature) } })
            .put("raw_input", input)
    }

    private fun JSONArray?.stringList(): List<String> {
        if (this == null) return emptyList()
        val out = ArrayList<String>(length())
        for (i in 0 until length()) out.add(optString(i))
        return out
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
