package com.example.data

import android.content.Context
import android.util.Log
import com.example.R
import com.example.model.SupportAttachmentLimits
import com.example.model.SupportConversation
import com.example.model.SupportConversationStatus
import com.example.model.SupportIssueCategory
import com.example.model.SupportMessage
import com.example.model.SupportMessageType
import com.example.model.SupportSenderRole
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SupportRepository(
  private val db: FirebaseFirestore,
  private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    db =
      FirebaseFirestore.getInstance(
        context.applicationContext.getString(R.string.firestore_database_id)
      )
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Please sign in to contact SkillConnect Support.")

  private fun <T> Flow<T>.logFirestoreErrors(
    operationType: OperationType,
    path: String,
    uid: String,
  ): Flow<T> = catch { error ->
    val firestoreError =
      generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val toLog = firestoreError ?: (error as? Exception)
    if (toLog != null && auth.currentUser?.uid == uid) {
      handleFirestoreError(toLog, operationType, path)
    }
    throw error
  }

  fun newConversationId(userId: String): String {
    val suffix = UUID.randomUUID().toString().replace("-", "").take(6)
    return "sc_${userId}--${System.currentTimeMillis()}_$suffix"
  }

  fun newMessageId(): String {
    val suffix = UUID.randomUUID().toString().replace("-", "").take(6)
    return "msg_${System.currentTimeMillis()}_$suffix"
  }

  fun observeUserConversations(userId: String): Flow<List<SupportConversation>> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "support_conversations"
    emitAll(
      db.collection(path)
        .whereEqualTo("userId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              SupportConversation::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.lastMessageAt?.seconds ?: it.updatedAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeAllConversationsAdmin(): Flow<List<SupportConversation>> = flow {
    val uid = requireUserId()
    val path = "support_conversations"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              SupportConversation::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(
              compareBy<SupportConversation> {
                when (it.statusEnum) {
                  SupportConversationStatus.OPEN -> 0
                  SupportConversationStatus.PENDING -> 1
                  SupportConversationStatus.RESOLVED -> 2
                }
              }
                .thenByDescending { it.unreadCountAdmin > 0 }
                .thenByDescending { it.lastMessageAt?.seconds ?: it.updatedAt?.seconds ?: 0L }
            )
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeMessages(
    conversationId: String,
    conversationOwnerId: String,
    isAdmin: Boolean,
  ): Flow<List<SupportMessage>> = flow {
    val uid = requireUserId()
    val path = "support_messages"
    val baseQuery =
      if (isAdmin) {
        db.collection(path).whereEqualTo("conversationId", conversationId)
      } else {
        db.collection(path)
          .whereEqualTo("conversationId", conversationId)
          .whereEqualTo("conversationOwnerId", conversationOwnerId)
      }
    emitAll(
      baseQuery
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              SupportMessage::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(
              compareBy<SupportMessage> { it.createdAt?.seconds ?: Long.MAX_VALUE }
                .thenBy { it.createdAt?.nanoseconds ?: Int.MAX_VALUE }
            )
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun uploadAttachment(
    conversationId: String,
    ownerUid: String,
    fileName: String,
    mimeType: String,
    bytes: ByteArray,
  ): Result<Pair<String, String>> {
    return try {
      val uploaderUid = requireUserId()
      val cleanMime = mimeType.substringBefore(';').trim().lowercase()
      val validationError = SupportAttachmentLimits.validate(cleanMime, bytes.size)
      require(validationError == null) { validationError ?: "Invalid attachment." }

      val sanitizedName =
        fileName
          .trim()
          .replace(Regex("[^a-zA-Z0-9._-]"), "_")
          .take(80)
          .ifBlank { "attachment_${System.currentTimeMillis()}" }
      val uniqueFileName = "${System.currentTimeMillis()}_$sanitizedName"
      val storagePath = "support-attachments/$conversationId/$uniqueFileName"

      val metadata =
        StorageMetadata.Builder()
          .setContentType(cleanMime)
          .setCustomMetadata("ownerUid", ownerUid)
          .setCustomMetadata("conversationId", conversationId)
          .setCustomMetadata("uploadedBy", uploaderUid)
          .build()

      val ref = storage.reference.child(storagePath)
      ref.putBytes(bytes, metadata).await()
      val downloadUrl = ref.downloadUrl.await().toString()
      Result.success(downloadUrl to storagePath)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      Log.e(TAG, "Support attachment upload failed", e)
      Result.failure(e)
    }
  }

  suspend fun createConversationAndInitialMessage(
    conversationId: String,
    messageId: String,
    userName: String,
    userEmail: String,
    accountType: String,
    issueCategory: SupportIssueCategory,
    subject: String,
    messageType: SupportMessageType,
    text: String,
    attachmentUrl: String = "",
    attachmentPath: String = "",
    attachmentName: String = "",
    attachmentType: String = "",
    durationSec: Int = 0,
  ): Result<Unit> {
    val path = "support_conversations/$conversationId"
    return try {
      val uid = requireUserId()
      val cleanName = userName.trim().ifEmpty { "SkillConnect User" }.take(80)
      val cleanEmail = userEmail.trim().take(140)
      val cleanAccountType =
        if (accountType.equals("Service Provider", ignoreCase = true)) "Service Provider"
        else "Customer"
      val cleanSubject = subject.trim().ifEmpty { issueCategory.label }.take(160)
      val cleanText = text.trim().take(2000)
      val preview =
        when (messageType) {
          SupportMessageType.TEXT -> cleanText
          SupportMessageType.VOICE -> "Voice message (${durationSec}s)"
          SupportMessageType.IMAGE -> cleanText.ifBlank { "Image: ${attachmentName.ifBlank { "Screenshot" }}" }
          SupportMessageType.DOCUMENT -> "PDF: ${attachmentName.ifBlank { "Document.pdf" }}"
        }.take(500)

      val convRef = db.collection("support_conversations").document(conversationId)
      val msgRef = db.collection("support_messages").document(messageId)

      val batch = db.batch()
      batch.set(
        convRef,
        mapOf(
          "conversationId" to conversationId,
          "userId" to uid,
          "userName" to cleanName,
          "userEmail" to cleanEmail,
          "accountType" to cleanAccountType,
          "issueCategory" to issueCategory.label,
          "subject" to cleanSubject,
          "status" to SupportConversationStatus.OPEN.label,
          "assignedAdminId" to "",
          "lastMessage" to preview,
          "lastMessageAt" to FieldValue.serverTimestamp(),
          "unreadCountUser" to 0L,
          "unreadCountAdmin" to 1L,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )

      batch.set(
        msgRef,
        mapOf(
          "messageId" to messageId,
          "conversationId" to conversationId,
          "conversationOwnerId" to uid,
          "senderId" to uid,
          "senderName" to cleanName,
          "senderRole" to SupportSenderRole.USER.label,
          "messageType" to messageType.wireValue,
          "text" to cleanText,
          "attachmentUrl" to attachmentUrl,
          "attachmentPath" to attachmentPath,
          "attachmentName" to attachmentName.take(180),
          "attachmentType" to attachmentType.take(80),
          "duration" to durationSec.coerceIn(0, 600),
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )

      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun sendMessage(
    conversation: SupportConversation,
    messageId: String,
    senderName: String,
    senderRole: SupportSenderRole,
    messageType: SupportMessageType,
    text: String,
    attachmentUrl: String = "",
    attachmentPath: String = "",
    attachmentName: String = "",
    attachmentType: String = "",
    durationSec: Int = 0,
  ): Result<Unit> {
    val convId = conversation.resolvedId
    val path = "support_messages/$messageId"
    return try {
      val uid = requireUserId()
      val cleanText = text.trim().take(2000)
      if (messageType == SupportMessageType.TEXT) {
        require(cleanText.isNotEmpty()) { "Message cannot be empty." }
      } else {
        require(attachmentUrl.isNotBlank() && attachmentPath.isNotBlank()) {
          "Attachment upload did not complete."
        }
      }

      val convRef = db.collection("support_conversations").document(convId)
      val msgRef = db.collection("support_messages").document(messageId)
      val existingMsg = msgRef.get().await()
      if (existingMsg.exists()) {
        // Idempotent retry protection: already committed
        return Result.success(Unit)
      }

      val preview =
        when (messageType) {
          SupportMessageType.TEXT -> cleanText
          SupportMessageType.VOICE -> "Voice message (${durationSec}s)"
          SupportMessageType.IMAGE -> cleanText.ifBlank { "Image: ${attachmentName.ifBlank { "Photo" }}" }
          SupportMessageType.DOCUMENT -> "PDF: ${attachmentName.ifBlank { "Document.pdf" }}"
        }.take(500)

      val batch = db.batch()
      batch.set(
        msgRef,
        mapOf(
          "messageId" to messageId,
          "conversationId" to convId,
          "conversationOwnerId" to conversation.userId,
          "senderId" to uid,
          "senderName" to senderName.trim().ifEmpty { "SkillConnect User" }.take(80),
          "senderRole" to senderRole.label,
          "messageType" to messageType.wireValue,
          "text" to cleanText,
          "attachmentUrl" to attachmentUrl,
          "attachmentPath" to attachmentPath,
          "attachmentName" to attachmentName.take(180),
          "attachmentType" to attachmentType.take(80),
          "duration" to durationSec.coerceIn(0, 600),
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )

      if (senderRole == SupportSenderRole.ADMIN) {
        val nextStatus =
          if (conversation.statusEnum == SupportConversationStatus.RESOLVED) {
            SupportConversationStatus.PENDING.label
          } else {
            conversation.statusEnum.label
          }
        batch.update(
          convRef,
          mapOf(
            "assignedAdminId" to conversation.assignedAdminId.ifBlank { uid },
            "status" to nextStatus,
            "lastMessage" to preview,
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "unreadCountUser" to FieldValue.increment(1L),
            "unreadCountAdmin" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      } else {
        val nextStatus =
          if (conversation.statusEnum == SupportConversationStatus.RESOLVED) {
            SupportConversationStatus.OPEN.label
          } else {
            conversation.statusEnum.label
          }
        batch.update(
          convRef,
          mapOf(
            "status" to nextStatus,
            "lastMessage" to preview,
            "lastMessageAt" to FieldValue.serverTimestamp(),
            "unreadCountAdmin" to FieldValue.increment(1L),
            "unreadCountUser" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun markConversationReadByUser(
    conversation: SupportConversation,
    messages: List<SupportMessage>,
  ) {
    val uid = auth.currentUser?.uid ?: return
    if (uid != conversation.userId) return
    val unreadAdminMsgs =
      messages.filter { it.roleEnum == SupportSenderRole.ADMIN && it.readAt == null }
    if (conversation.unreadCountUser == 0 && unreadAdminMsgs.isEmpty()) return

    runCatching {
      val batch = db.batch()
      unreadAdminMsgs.take(40).forEach { msg ->
        batch.update(
          db.collection("support_messages").document(msg.resolvedId),
          mapOf("readAt" to FieldValue.serverTimestamp()),
        )
      }
      if (conversation.unreadCountUser > 0) {
        batch.update(
          db.collection("support_conversations").document(conversation.resolvedId),
          mapOf(
            "unreadCountUser" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
    }
  }

  suspend fun markConversationReadByAdmin(
    conversation: SupportConversation,
    messages: List<SupportMessage>,
  ) {
    auth.currentUser?.uid ?: return
    val unreadUserMsgs =
      messages.filter { it.roleEnum == SupportSenderRole.USER && it.readAt == null }
    if (conversation.unreadCountAdmin == 0 && unreadUserMsgs.isEmpty()) return

    runCatching {
      val batch = db.batch()
      unreadUserMsgs.take(40).forEach { msg ->
        batch.update(
          db.collection("support_messages").document(msg.resolvedId),
          mapOf("readAt" to FieldValue.serverTimestamp()),
        )
      }
      if (conversation.unreadCountAdmin > 0) {
        batch.update(
          db.collection("support_conversations").document(conversation.resolvedId),
          mapOf(
            "unreadCountAdmin" to 0L,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }
      batch.commit().await()
    }
  }

  suspend fun updateConversationAdmin(
    conversationId: String,
    status: SupportConversationStatus,
    assignedAdminId: String,
  ): Result<Unit> {
    val path = "support_conversations/$conversationId"
    return try {
      requireUserId()
      db.collection("support_conversations")
        .document(conversationId)
        .update(
          mapOf(
            "status" to status.label,
            "assignedAdminId" to assignedAdminId.trim().take(128),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun registerFcmToken(userId: String, token: String) {
    if (token.length < 10) return
    val safeTokenId = token.take(64).replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "android_fcm" }
    runCatching {
      db.collection("users")
        .document(userId)
        .collection("fcm_tokens")
        .document(safeTokenId)
        .set(
          mapOf(
            "token" to token,
            "platform" to "android",
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
    }
  }

  private companion object {
    const val TAG = "SupportRepo"
  }
}
