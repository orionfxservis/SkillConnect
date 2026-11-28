package com.example.model

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties

enum class SupportIssueCategory(val label: String) {
  BOOKING_PROBLEM("Booking Problem"),
  PROFILE_ACCOUNT("Profile or Account Issue"),
  PROVIDER_VERIFICATION("Provider Verification"),
  SUBSCRIPTION_PAYMENT("Subscription or Payment Issue"),
  LOCATION_GPS("Location or GPS Problem"),
  TECHNICAL_PROBLEM("Technical Problem"),
  OTHER_ISSUE("Other Issue");

  companion object {
    val allLabels: List<String> = entries.map { it.label }

    fun fromLabel(raw: String): SupportIssueCategory =
      entries.firstOrNull { it.label.equals(raw.trim(), ignoreCase = true) } ?: OTHER_ISSUE
  }
}

enum class SupportConversationStatus(val label: String) {
  OPEN("Open"),
  PENDING("Pending"),
  RESOLVED("Resolved");

  companion object {
    fun fromRaw(raw: String): SupportConversationStatus =
      entries.firstOrNull { it.label.equals(raw.trim(), ignoreCase = true) || it.name.equals(raw.trim(), ignoreCase = true) }
        ?: OPEN
  }
}

enum class SupportSenderRole(val label: String) {
  USER("User"),
  ADMIN("Admin");

  companion object {
    fun fromRaw(raw: String): SupportSenderRole =
      if (raw.trim().equals("Admin", ignoreCase = true)) ADMIN else USER
  }
}

enum class SupportMessageType(val wireValue: String) {
  TEXT("text"),
  VOICE("voice"),
  IMAGE("image"),
  DOCUMENT("document");

  companion object {
    fun fromRaw(raw: String): SupportMessageType =
      entries.firstOrNull { it.wireValue.equals(raw.trim(), ignoreCase = true) } ?: TEXT
  }
}

@Keep
@IgnoreExtraProperties
data class SupportConversation(
  @DocumentId val id: String = "",
  val conversationId: String = "",
  val userId: String = "",
  val userName: String = "",
  val userEmail: String = "",
  val accountType: String = "Customer",
  val issueCategory: String = SupportIssueCategory.OTHER_ISSUE.label,
  val subject: String = "",
  val status: String = SupportConversationStatus.OPEN.label,
  val assignedAdminId: String = "",
  val lastMessage: String = "",
  val lastMessageAt: Timestamp? = null,
  val unreadCountUser: Int = 0,
  val unreadCountAdmin: Int = 0,
  val createdAt: Timestamp? = null,
  val updatedAt: Timestamp? = null,
) {
  val statusEnum: SupportConversationStatus
    get() = SupportConversationStatus.fromRaw(status)

  val resolvedId: String
    get() = conversationId.ifBlank { id }
}

@Keep
@IgnoreExtraProperties
data class SupportMessage(
  @DocumentId val id: String = "",
  val messageId: String = "",
  val conversationId: String = "",
  val conversationOwnerId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val senderRole: String = SupportSenderRole.USER.label,
  val messageType: String = SupportMessageType.TEXT.wireValue,
  val text: String = "",
  val attachmentUrl: String = "",
  val attachmentPath: String = "",
  val attachmentName: String = "",
  val attachmentType: String = "",
  val duration: Int = 0,
  val createdAt: Timestamp? = null,
  val readAt: Timestamp? = null,
) {
  val roleEnum: SupportSenderRole
    get() = SupportSenderRole.fromRaw(senderRole)

  val typeEnum: SupportMessageType
    get() = SupportMessageType.fromRaw(messageType)

  val resolvedId: String
    get() = messageId.ifBlank { id }
}

data class PendingSupportMessage(
  val messageId: String,
  val conversationId: String,
  val messageType: SupportMessageType,
  val text: String = "",
  val localFileBytes: ByteArray? = null,
  val uploadedUrl: String = "",
  val uploadedPath: String = "",
  val attachmentName: String = "",
  val attachmentType: String = "",
  val durationSec: Int = 0,
  val isSending: Boolean = true,
  val errorMessage: String? = null,
) {
  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is PendingSupportMessage) return false
    return messageId == other.messageId &&
      conversationId == other.conversationId &&
      messageType == other.messageType &&
      text == other.text &&
      uploadedUrl == other.uploadedUrl &&
      uploadedPath == other.uploadedPath &&
      isSending == other.isSending &&
      errorMessage == other.errorMessage
  }

  override fun hashCode(): Int {
    var result = messageId.hashCode()
    result = 31 * result + conversationId.hashCode()
    result = 31 * result + messageType.hashCode()
    result = 31 * result + isSending.hashCode()
    result = 31 * result + (errorMessage?.hashCode() ?: 0)
    return result
  }
}

object SupportAttachmentLimits {
  const val MAX_IMAGE_OR_VOICE_BYTES = 5 * 1024 * 1024 // 5 MB
  const val MAX_PDF_BYTES = 10 * 1024 * 1024 // 10 MB

  val ALLOWED_IMAGE_MIME_TYPES = setOf("image/jpeg", "image/jpg", "image/png", "image/webp")
  val ALLOWED_AUDIO_MIME_TYPES =
    setOf("audio/mp4", "audio/m4a", "audio/aac", "audio/mpeg", "audio/ogg", "audio/webm", "audio/3gpp")
  val ALLOWED_DOCUMENT_MIME_TYPES = setOf("application/pdf")

  fun validate(mimeType: String, sizeBytes: Int): String? {
    val cleanMime = mimeType.substringBefore(';').trim().lowercase()
    return when {
      cleanMime in ALLOWED_IMAGE_MIME_TYPES ->
        if (sizeBytes > MAX_IMAGE_OR_VOICE_BYTES) "Image must be 5 MB or smaller." else null
      cleanMime in ALLOWED_AUDIO_MIME_TYPES ->
        if (sizeBytes > MAX_IMAGE_OR_VOICE_BYTES) "Voice message must be 5 MB or smaller." else null
      cleanMime in ALLOWED_DOCUMENT_MIME_TYPES ->
        if (sizeBytes > MAX_PDF_BYTES) "PDF document must be 10 MB or smaller." else null
      else -> "Only JPG/PNG/WEBP images, audio recordings, and PDF documents are supported."
    }
  }
}
