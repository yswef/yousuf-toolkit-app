package core.design

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.yousuf.toolkit.R

/** مؤثرات صوتية قصيرة للواجهة. تُهيَّأ مرة واحدة من MainActivity. */
object SoundFx {
    enum class Fx(val res: Int) {
        TAP(R.raw.sfx_tap),
        SUCCESS(R.raw.sfx_success),
        COIN(R.raw.sfx_coin),
        LEVEL_UP(R.raw.sfx_levelup),
        ERROR(R.raw.sfx_error),
        OPEN(R.raw.sfx_open),
        SWIPE(R.raw.sfx_swipe),
        DELETE(R.raw.sfx_delete),
        CLEAN(R.raw.sfx_clean)
    }

    private const val PREFS = "ydim_ui"
    private const val KEY = "sound_fx"
    private var pool: SoundPool? = null
    private val ids = HashMap<Fx, Int>()
    private val ready = HashSet<Int>()

    @Volatile
    var enabled: Boolean = true
        private set

    fun init(context: Context) {
        if (pool != null) return
        val app = context.applicationContext
        enabled = app.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY, true)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        val p = SoundPool.Builder().setMaxStreams(4).setAudioAttributes(attrs).build()
        p.setOnLoadCompleteListener { _, id, status -> if (status == 0) ready.add(id) }
        Fx.entries.forEach { ids[it] = p.load(app, it.res, 1) }
        pool = p
    }

    fun setEnabled(context: Context, value: Boolean) {
        enabled = value
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY, value).apply()
    }

    fun play(fx: Fx) {
        if (!enabled) return
        val id = ids[fx] ?: return
        if (id in ready) pool?.play(id, 1f, 1f, 1, 0, 1f)
    }
}
