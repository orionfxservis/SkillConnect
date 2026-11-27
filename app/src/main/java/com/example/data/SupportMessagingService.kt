package com.example.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SupportMessagingService : FirebaseMessagingService() {
  private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

  override fun onNewToken(token: String) {
    super.onNewToken(token)
    val uid = Firebase.auth.currentUser?.uid ?: return
    val databaseId = applicationContext.getString(R.string.firestore_database_id)
    val db = FirebaseFirestore.getInstance(databaseId)
    val safeTokenId = token.take(64).replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "android_default" }
    serviceScope.launch {
      runCatching {
        db.collection("users")
          .document(uid)
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
  }

  override fun onMessageReceived(remoteMessage: RemoteMessage) {
    super.onMessageReceived(remoteMessage)
    val title = remoteMessage.notification?.title ?: remoteMessage.data["senderName"] ?: "SkillConnect Support Team"
    val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "You received a new support reply."
    val conversationId = remoteMessage.data["conversationId"].orEmpty()

    showSupportNotification(title = title, body = body, conversationId = conversationId)
  }

  private fun showSupportNotification(title: String, body: String, conversationId: String) {
    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel =
        NotificationChannel(
          CHANNEL_ID,
          "Support Team Replies",
          NotificationManager.IMPORTANCE_HIGH,
        ).apply {
          description = "Real-time notifications when SkillConnect Support Team replies."
        }
      manager.createNotificationChannel(channel)
    }

    val intent =
      Intent(this, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (conversationId.isNotBlank()) {
          putExtra(EXTRA_SUPPORT_CONVERSATION_ID, conversationId)
        }
      }

    val pendingIntent =
      PendingIntent.getActivity(
        this,
        conversationId.hashCode(),
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
      )

    val notification =
      NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.mipmap.ic_launcher)
        .setContentTitle(title)
        .setContentText(body)
        .setStyle(NotificationCompat.BigTextStyle().bigText(body))
        .setAutoCancel(true)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setContentIntent(pendingIntent)
        .build()

    manager.notify(conversationId.ifBlank { "support_reply" }.hashCode(), notification)
  }

  companion object {
    const val CHANNEL_ID = "skillconnect_support_replies"
    const val EXTRA_SUPPORT_CONVERSATION_ID = "extra_support_conversation_id"
  }
}
