// توقيع المطوّر: يوسف الحمزي — Y- Dimensions — yswefalhmzy@gmail.com
package feature.journal

import core.net.Api
import data.local.Store

object JournalSync {
    suspend fun push(store: Store): Int {
        if (store.authToken.isBlank()) return 0
        val response = Api.sync(Api.normalizeBase(store.serverUrl), store.authToken, store.journalSyncPayload())
        val count = response.optJSONObject("synced")?.optInt("journal", store.journal.size) ?: store.journal.size
        store.markJournalSynced()
        return count
    }
}
