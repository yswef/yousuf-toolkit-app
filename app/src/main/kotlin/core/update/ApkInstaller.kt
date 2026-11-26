// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
package core.update

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import core.net.Api
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object ApkInstaller {

    fun canInstall(context: Context): Boolean =
        context.packageManager.canRequestPackageInstalls()

    suspend fun download(context: Context, url: String): File = withContext(Dispatchers.IO) {
        val bytes = Api.download(url)
        val dir = File(context.filesDir, "updates").apply { mkdirs() }
        dir.listFiles()?.forEach { if (it.name.endsWith(".apk")) it.delete() }
        val target = File(dir, "y-dimensions-update.apk")
        target.writeBytes(bytes)
        target
    }

    fun install(context: Context, apk: File) {
        val installer = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
        val sessionId = installer.createSession(params)
        val session = installer.openSession(sessionId)
        session.use {
            it.openWrite("package", 0, apk.length()).use { out ->
                apk.inputStream().use { input -> input.copyTo(out) }
                it.fsync(out)
            }
            val intent = Intent("com.yousuf.toolkit.APK_INSTALLED").setPackage(context.packageName)
            val pending = PendingIntent.getBroadcast(
                context, 1, intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            it.commit(pending.intentSender)
        }
    }
}
// Developer signature: Yousuf Alhamzi — yswefalhmzy@gmail.com — Y-Dimension
