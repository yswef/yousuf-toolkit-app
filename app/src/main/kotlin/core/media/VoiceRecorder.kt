// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package core.media

import android.content.Context
import android.media.MediaPlayer
import android.media.MediaRecorder
import java.io.File

object VoiceRecorder {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var activeFile: File? = null

    fun start(context: Context, prefix: String): String? {
        stop()
        val target = File(context.filesDir, prefix + System.currentTimeMillis() + ".m4a")
        return try {
            recorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(target.absolutePath)
                prepare()
                start()
            }
            activeFile = target
            target.absolutePath
        } catch (error: Throwable) {
            runCatching { recorder?.release() }
            recorder = null
            activeFile = null
            null
        }
    }

    fun stop(): String? {
        val target = activeFile
        runCatching { recorder?.stop() }
        runCatching { recorder?.release() }
        recorder = null
        activeFile = null
        return target?.takeIf { it.exists() && it.length() > 0 }?.absolutePath
    }

    fun play(path: String) {
        runCatching { player?.release() }
        player = null
        runCatching {
            player = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                start()
            }
        }
    }

    fun release() {
        stop()
        runCatching { player?.release() }
        player = null
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
