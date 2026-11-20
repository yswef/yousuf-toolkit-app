package domain.model

data class AccountDraft(
    val title: String,
    val username: String,
    val email: String,
    val password: String,
    val totp: String,
    val layer: String,
    val category: String,
    val recoveryEmail: String,
    val recoveryPhone: String,
    val codes: String,
    val notes: String
)

data class AccountItem(
    val id: Long,
    val title: String,
    val username: String,
    val email: String,
    val password: String?,
    val totp: String?,
    val layer: String,
    val category: String,
    val recoveryEmail: String,
    val recoveryPhone: String,
    val codes: String?,
    val notes: String?,
    val updatedAt: Long
)

data class AccountDevice(
    val id: Long,
    val name: String,
    val platform: String,
    val lastSeen: Long,
    val current: Boolean
)

data class AccountRelation(
    val id: Long,
    val fromId: Long,
    val toId: Long,
    val kind: String
)

data class ScreenshotItem(
    val id: Long,
    val label: String,
    val mime: String,
    val payload: String,
    val at: Long
)

data class LinkedNote(
    val id: Long,
    val title: String,
    val body: String,
    val tags: String,
    val at: Long
)

val accountLayers = listOf("primary", "backup", "normal", "decoy")
val accountCategories = listOf("email", "social", "game", "bank", "work", "other")
val relationKinds = listOf("recovery", "alias", "linked", "same_person")
