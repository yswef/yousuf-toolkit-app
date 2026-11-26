// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package core.security

import android.content.Context
import android.content.DialogInterface
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal
import android.os.Handler
import java.util.concurrent.Executor

class MainExecutor(context: Context) : Executor {
    private val handler = Handler(context.mainLooper)
    override fun execute(command: Runnable) {
        handler.post(command)
    }
}

object BiometricAuth {
    private val authenticators =
        BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK

    fun available(context: Context): Boolean =
        BiometricManager.from(context).canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS

    fun prompt(
        context: Context,
        title: CharSequence,
        subtitle: CharSequence,
        negative: CharSequence,
        executor: Executor,
        onSuccess: () -> Unit,
        onFail: () -> Unit
    ) {
        val prompt = BiometricPrompt.Builder(context)
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(authenticators)
            .setNegativeButton(negative, executor, DialogInterface.OnClickListener { _, _ -> onFail() })
            .build()
        prompt.authenticate(CancellationSignal(), executor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
                onFail()
            }

            override fun onAuthenticationFailed() {
                onFail()
            }
        })
    }
}
// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
