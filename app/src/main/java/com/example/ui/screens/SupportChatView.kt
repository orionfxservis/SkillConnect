package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Done
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AudioPlaybackState
import com.example.data.SupportAudioHelper
import com.example.model.PendingSupportMessage
import com.example.model.SupportAttachmentLimits
import com.example.model.SupportConversation
import com.example.model.SupportConversationStatus
import com.example.model.SupportMessage
import com.example.model.SupportMessageType
import com.example.model.SupportSenderRole
import com.example.model.UiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val UserChatBubbleBg = Color(0xFFDCFCE7)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupportChatView(
  conversation: SupportConversation,
  messagesState: UiState<List<SupportMessage>>,
  pendingMessages: List<PendingSupportMessage>,
  onBack: () -> Unit,
  onMarkRead: () -> Unit,
  onSendMessage: (
    messageType: SupportMessageType,
    text: String,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
    durationSec: Int,
  ) -> Unit,
  onRetryPending: (String) -> Unit,
  onDismissPending: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  BackHandler(onBack = onBack)

  val context = LocalContext.current
  val audioHelper = remember(context) { SupportAudioHelper(context) }
  DisposableEffect(audioHelper) { onDispose { audioHelper.release() } }

  val recorderState by audioHelper.recorderState.collectAsStateWithLifecycle()
  val playbackState by audioHelper.playbackState.collectAsStateWithLifecycle()

  var messageInput by remember(conversation.resolvedId) { mutableStateOf("") }
  var showAttachmentPicker by remember { mutableStateOf(false) }
  var validationError by remember { mutableStateOf<String?>(null) }
  var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

  val messages = (messagesState as? UiState.Success)?.data ?: emptyList()
  val convPending = pendingMessages.filter { it.conversationId == conversation.resolvedId }

  val listState = rememberLazyListState()
  LaunchedEffect(messages.size, convPending.size) {
    val total = messages.size + convPending.size
    if (total > 0) {
      listState.animateScrollToItem(total - 1)
    }
  }

  LaunchedEffect(conversation.resolvedId, messages.size) {
    if (messages.any { it.roleEnum == SupportSenderRole.ADMIN && it.readAt == null } ||
      conversation.unreadCountUser > 0
    ) {
      onMarkRead()
    }
  }

  val recordAudioPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        validationError = null
        audioHelper.startRecording()
      } else {
        validationError = "Microphone permission is required to record voice messages."
      }
    }

  val imagePickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.IMAGE) { bytes, name, mime, err ->
          if (err != null) {
            validationError = err
          } else if (bytes != null) {
            validationError = null
            onSendMessage(
              SupportMessageType.IMAGE,
              messageInput.trim().ifBlank { "Shared a screenshot" },
              bytes,
              name,
              mime,
              0,
            )
            messageInput = ""
          }
        }
      }
    }

  val pdfPickerLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.DOCUMENT) { bytes, name, mime, err ->
          if (err != null) {
            validationError = err
          } else if (bytes != null) {
            validationError = null
            onSendMessage(
              SupportMessageType.DOCUMENT,
              messageInput.trim().ifBlank { "Shared a PDF document" },
              bytes,
              name,
              mime,
              0,
            )
            messageInput = ""
          }
        }
      }
    }

  Column(
    modifier = modifier.fillMaxSize().background(Color.White).imePadding(),
  ) {
    // Top Header matching illustrative layout
    Surface(color = Color.White, shadowElevation = 1.dp) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(
          onClick = onBack,
          modifier = Modifier.testTag("support_chat_back_button"),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = "Back to Support",
          )
        }
        Box(
          modifier =
            Modifier.size(40.dp).clip(CircleShape).background(EmeraldContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.SupportAgent,
            contentDescription = null,
            tint = OnEmeraldContainer,
            modifier = Modifier.size(22.dp),
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "SkillConnect Support Team",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
          Text(
            text = "${conversation.issueCategory} • Support conversation",
            style = MaterialTheme.typography.labelSmall,
            color = DeepTeal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }
        SupportStatusPill(status = conversation.statusEnum)
      }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    // Messages stream
    Box(
      modifier = Modifier.weight(1f).fillMaxWidth().background(LightBackground.copy(alpha = 0.45f)),
    ) {
      when (messagesState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepTeal)
          }
        }
        is UiState.Error -> {
          ErrorCard(message = messagesState.message, modifier = Modifier.padding(16.dp))
        }
        is UiState.Success -> {
          LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            if (messages.isEmpty() && convPending.isEmpty()) {
              item {
                SupportBubble(
                  message =
                    SupportMessage(
                      messageId = "welcome_hint",
                      senderName = "Support Team",
                      senderRole = SupportSenderRole.ADMIN.label,
                      messageType = SupportMessageType.TEXT.wireValue,
                      text = "Assalam-o-Alaikum! How can we help you today?",
                    ),
                  playbackState = playbackState,
                  onToggleVoice = { _, _, _ -> },
                  onOpenImage = {},
                  onOpenDocument = {},
                )
              }
            }

            items(messages, key = { it.resolvedId }) { msg ->
              SupportBubble(
                message = msg,
                playbackState = playbackState,
                onToggleVoice = { id, url, dur ->
                  audioHelper.toggleRemoteMessagePlayback(id, url, dur)
                },
                onOpenImage = { url -> zoomedImageUrl = url },
                onOpenDocument = { url ->
                  runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                  }
                },
              )
            }

            items(convPending, key = { it.messageId }) { pending ->
              PendingSupportBubble(
                pending = pending,
                onRetry = { onRetryPending(pending.messageId) },
                onDismiss = { onDismissPending(pending.messageId) },
              )
            }
          }
        }
      }
    }

    // Error strip
    val activeError = validationError ?: recorderState.errorMessage
    if (!activeError.isNullOrBlank()) {
      Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = activeError,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.weight(1f),
          )
          IconButton(
            onClick = {
              validationError = null
              audioHelper.clearError()
            },
            modifier = Modifier.size(28.dp),
          ) {
            Icon(
              Icons.Rounded.Close,
              contentDescription = "Dismiss",
              tint = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.size(16.dp),
            )
          }
        }
      }
    }

    // Voice Recording or Recorded Draft Preview Bar
    if (recorderState.isRecording || recorderState.recordedFilePath != null) {
      VoiceRecorderBar(
        isRecording = recorderState.isRecording,
        recordingSeconds = recorderState.recordingSeconds,
        recordedDurationSec = recorderState.recordedDurationSec,
        isPreviewPlaying =
          playbackState.playingKey == SupportAudioHelper.DRAFT_PREVIEW_KEY &&
            playbackState.isPlaying,
        onStopRecording = { audioHelper.stopRecording() },
        onTogglePreviewPlay = { audioHelper.toggleDraftPreview() },
        onDeleteDraft = { audioHelper.deleteDraftRecording() },
        onSendVoice = {
          val bytes = audioHelper.readRecordedBytes()
          val dur = recorderState.recordedDurationSec.coerceAtLeast(1)
          if (bytes != null) {
            val err = SupportAttachmentLimits.validate("audio/mp4", bytes.size)
            if (err != null) {
              validationError = err
            } else {
              onSendMessage(
                SupportMessageType.VOICE,
                "",
                bytes,
                "voice_${System.currentTimeMillis()}.m4a",
                "audio/mp4",
                dur,
              )
              audioHelper.deleteDraftRecording()
            }
          }
        },
      )
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

    // Bottom Pill Composer matching screenshot layout
    Row(
      modifier = Modifier.fillMaxWidth().background(Color.White).padding(horizontal = 10.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      IconButton(
        onClick = { showAttachmentPicker = true },
        enabled = !recorderState.isRecording,
        modifier = Modifier.size(44.dp).testTag("support_attach_button"),
      ) {
        Icon(
          imageVector = Icons.Rounded.AttachFile,
          contentDescription = "Attach image or PDF",
          tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.weight(1f).heightIn(min = 44.dp),
      ) {
        Box(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (messageInput.isEmpty()) {
            Text(
              text = "Type your message...",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          BasicTextField(
            value = messageInput,
            onValueChange = { messageInput = it },
            maxLines = 4,
            textStyle = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurface,
            ),
            cursorBrush = SolidColor(DeepTeal),
            modifier = Modifier.fillMaxWidth().testTag("support_message_input"),
          )
        }
      }

      IconButton(
        onClick = {
          if (recorderState.isRecording) {
            audioHelper.stopRecording()
          } else {
            val granted =
              ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                PackageManager.PERMISSION_GRANTED
            if (granted) {
              validationError = null
              audioHelper.startRecording()
            } else {
              recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
          }
        },
        modifier = Modifier.size(44.dp).testTag("support_mic_button"),
      ) {
        Icon(
          imageVector = if (recorderState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
          contentDescription = if (recorderState.isRecording) "Stop recording" else "Record voice message",
          tint = if (recorderState.isRecording) MaterialTheme.colorScheme.error else DeepTeal,
        )
      }

      IconButton(
        onClick = {
          val text = messageInput.trim()
          if (text.isNotEmpty()) {
            validationError = null
            onSendMessage(SupportMessageType.TEXT, text, null, "", "", 0)
            messageInput = ""
          }
        },
        enabled = messageInput.trim().isNotEmpty(),
        modifier = Modifier.size(44.dp).testTag("support_send_button"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.Send,
          contentDescription = "Send message",
          tint =
            if (messageInput.trim().isNotEmpty()) DeepTeal
            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
        )
      }
    }
  }

  if (showAttachmentPicker) {
    ModalBottomSheet(onDismissRequest = { showAttachmentPicker = false }) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
      ) {
        Text(
          text = "Attach File",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        ListItem(
          headlineContent = { Text("Image / Screenshot") },
          supportingContent = { Text("JPG, PNG or WEBP up to 5 MB") },
          leadingContent = {
            Icon(Icons.Rounded.Image, contentDescription = null, tint = DeepTeal)
          },
          modifier =
            Modifier.clickable {
              showAttachmentPicker = false
              imagePickerLauncher.launch(
                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
              )
            }
              .testTag("support_pick_image_option"),
        )
        ListItem(
          headlineContent = { Text("PDF Document") },
          supportingContent = { Text("PDF files up to 10 MB") },
          leadingContent = {
            Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error)
          },
          modifier =
            Modifier.clickable {
              showAttachmentPicker = false
              pdfPickerLauncher.launch(arrayOf("application/pdf"))
            }
              .testTag("support_pick_pdf_option"),
        )
      }
    }
  }

  zoomedImageUrl?.let { url ->
    Dialog(onDismissRequest = { zoomedImageUrl = null }) {
      Surface(shape = RoundedCornerShape(16.dp), color = Color.Black) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
          IconButton(onClick = { zoomedImageUrl = null }) {
            Icon(Icons.Rounded.Close, contentDescription = "Close preview", tint = Color.White)
          }
          AsyncImage(
            model = url,
            contentDescription = "Support image attachment",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).clip(RoundedCornerShape(12.dp)),
          )
          TextButton(
            onClick = {
              runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
            }
          ) {
            Text("Open in Browser", color = Color.White)
          }
        }
      }
    }
  }
}

@Composable
fun SupportStatusPill(status: SupportConversationStatus) {
  val (bg, fg) =
    when (status) {
      SupportConversationStatus.OPEN -> EmeraldContainer to OnEmeraldContainer
      SupportConversationStatus.PENDING -> AmberContainer to OnAmberContainer
      SupportConversationStatus.RESOLVED ->
        MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
  Surface(shape = RoundedCornerShape(50), color = bg) {
    Text(
      text = status.label,
      style = MaterialTheme.typography.labelSmall,
      fontWeight = FontWeight.SemiBold,
      color = fg,
      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
    )
  }
}

@Composable
fun SupportBubble(
  message: SupportMessage,
  playbackState: AudioPlaybackState,
  onToggleVoice: (messageId: String, url: String, durationSec: Int) -> Unit,
  onOpenImage: (String) -> Unit,
  onOpenDocument: (String) -> Unit,
  viewerIsAdmin: Boolean = false,
) {
  val isOutgoing =
    if (viewerIsAdmin) message.roleEnum == SupportSenderRole.ADMIN
    else message.roleEnum == SupportSenderRole.USER

  val bubbleBg = if (isOutgoing) UserChatBubbleBg else Color.White
  val border = if (isOutgoing) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
  val timeLabel = formatMessageTime(message.createdAt?.toDate())
  val senderLabel =
    if (isOutgoing) "You"
    else if (message.roleEnum == SupportSenderRole.ADMIN) "Support Team"
    else message.senderName.ifBlank { "User" }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start,
  ) {
    Surface(
      shape =
        RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isOutgoing) 16.dp else 4.dp,
          bottomEnd = if (isOutgoing) 4.dp else 16.dp,
        ),
      color = bubbleBg,
      border = border,
      modifier = Modifier.widthIn(max = 310.dp),
    ) {
      Column(
        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        when (message.typeEnum) {
          SupportMessageType.VOICE -> {
            val isThis = playbackState.playingKey == message.resolvedId
            val isPlaying = isThis && playbackState.isPlaying
            val totalSec = if (isThis && playbackState.durationSec > 0) playbackState.durationSec else message.duration.coerceAtLeast(1)
            val progress = if (isThis) (playbackState.positionSec.toFloat() / totalSec.toFloat()).coerceIn(0f, 1f) else 0f

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              IconButton(
                onClick = { onToggleVoice(message.resolvedId, message.attachmentUrl, message.duration) },
                modifier = Modifier.size(36.dp).clip(CircleShape).background(DeepTeal.copy(alpha = 0.14f)),
              ) {
                Icon(
                  imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                  contentDescription = if (isPlaying) "Pause voice message" else "Play voice message",
                  tint = DeepTeal,
                )
              }
              Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                  progress = { progress },
                  color = DeepTeal,
                  trackColor = DeepTeal.copy(alpha = 0.2f),
                  modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(50)),
                )
                Text(
                  text = "Voice • ${formatSeconds(if (isThis && isPlaying) playbackState.positionSec else totalSec)}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          }

          SupportMessageType.IMAGE -> {
            if (message.attachmentUrl.isNotBlank()) {
              AsyncImage(
                model = message.attachmentUrl,
                contentDescription = message.attachmentName.ifBlank { "Image attachment" },
                contentScale = ContentScale.Crop,
                modifier =
                  Modifier.fillMaxWidth()
                    .height(170.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onOpenImage(message.attachmentUrl) },
              )
            }
            if (message.text.isNotBlank()) {
              Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            }
          }

          SupportMessageType.DOCUMENT -> {
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier = Modifier.fillMaxWidth().clickable { onOpenDocument(message.attachmentUrl) },
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  Icons.Rounded.PictureAsPdf,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.error,
                  modifier = Modifier.size(26.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = message.attachmentName.ifBlank { "Document.pdf" },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                  Text(
                    text = "Tap to open PDF",
                    style = MaterialTheme.typography.labelSmall,
                    color = DeepTeal,
                  )
                }
              }
            }
            if (message.text.isNotBlank()) {
              Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
            }
          }

          SupportMessageType.TEXT -> {
            Text(text = message.text, style = MaterialTheme.typography.bodyMedium)
          }
        }

        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = if (timeLabel.isNotBlank()) "$senderLabel • $timeLabel" else senderLabel,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (isOutgoing && message.messageId != "welcome_hint") {
            Icon(
              imageVector = if (message.readAt != null) Icons.Rounded.DoneAll else Icons.Rounded.Done,
              contentDescription = if (message.readAt != null) "Read" else "Sent",
              tint = if (message.readAt != null) DeepTeal else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun PendingSupportBubble(
  pending: PendingSupportMessage,
  onRetry: () -> Unit,
  onDismiss: () -> Unit,
) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.End,
  ) {
    Surface(
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp),
      color = if (pending.errorMessage != null) MaterialTheme.colorScheme.errorContainer else UserChatBubbleBg.copy(alpha = 0.7f),
      modifier = Modifier.widthIn(max = 300.dp),
    ) {
      Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        Text(
          text =
            when (pending.messageType) {
              SupportMessageType.VOICE -> "Voice message (${formatSeconds(pending.durationSec)})"
              SupportMessageType.IMAGE -> "Image: ${pending.attachmentName}"
              SupportMessageType.DOCUMENT -> "PDF: ${pending.attachmentName}"
              SupportMessageType.TEXT -> pending.text
            },
          style = MaterialTheme.typography.bodyMedium,
        )
        if (pending.isSending) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = DeepTeal)
            Text("Sending...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        } else if (pending.errorMessage != null) {
          Text(
            text = pending.errorMessage,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onErrorContainer,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onRetry, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
              Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
              Spacer(Modifier.width(4.dp))
              Text("Retry")
            }
            TextButton(onClick = onDismiss, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)) {
              Text("Remove", color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}

@Composable
fun VoiceRecorderBar(
  isRecording: Boolean,
  recordingSeconds: Int,
  recordedDurationSec: Int,
  isPreviewPlaying: Boolean,
  onStopRecording: () -> Unit,
  onTogglePreviewPlay: () -> Unit,
  onDeleteDraft: () -> Unit,
  onSendVoice: () -> Unit,
) {
  Surface(
    color = if (isRecording) Color(0xFFFEF2F2) else Color(0xFFECFDF5),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      if (isRecording) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFDC2626)))
          Text(
            text = "Recording ${formatSeconds(recordingSeconds)}",
            style = MaterialTheme.typography.titleSmall,
            color = Color(0xFF991B1B),
            fontWeight = FontWeight.SemiBold,
          )
        }
        TextButton(onClick = onStopRecording, modifier = Modifier.testTag("support_voice_stop_button")) {
          Icon(Icons.Rounded.Stop, contentDescription = "Stop recording", tint = Color(0xFFDC2626), modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text("Stop", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
        }
      } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          IconButton(onClick = onTogglePreviewPlay, modifier = Modifier.testTag("support_voice_preview_play_button")) {
            Icon(
              imageVector = if (isPreviewPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
              contentDescription = "Preview recording",
              tint = DeepTeal,
            )
          }
          Text(
            text = "Voice note (${formatSeconds(recordedDurationSec)})",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onDeleteDraft, modifier = Modifier.testTag("support_voice_delete_button")) {
            Icon(Icons.Rounded.Delete, contentDescription = "Delete recording", tint = MaterialTheme.colorScheme.error)
          }
          TextButton(onClick = onSendVoice, modifier = Modifier.testTag("support_voice_send_button")) {
            Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send voice message", tint = DeepTeal, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(4.dp))
            Text("Send", color = DeepTeal, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

fun readValidatedAttachment(
  context: Context,
  uri: Uri,
  expectedType: SupportMessageType,
  onResult: (bytes: ByteArray?, name: String, mime: String, error: String?) -> Unit,
) {
  val resolver = context.contentResolver
  val detectedMime = resolver.getType(uri)?.substringBefore(';')?.trim()?.lowercase().orEmpty()
  val fallbackMime =
    when (expectedType) {
      SupportMessageType.IMAGE -> "image/jpeg"
      SupportMessageType.DOCUMENT -> "application/pdf"
      else -> detectedMime
    }
  val mime = detectedMime.ifBlank { fallbackMime }

  var fileName = ""
  resolver.query(uri, null, null, null, null)?.use { cursor ->
    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
    if (nameIdx >= 0 && cursor.moveToFirst()) {
      fileName = cursor.getString(nameIdx).orEmpty()
    }
  }
  if (fileName.isBlank()) {
    fileName = if (expectedType == SupportMessageType.DOCUMENT) "document.pdf" else "screenshot.jpg"
  }

  val bytes = runCatching { resolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
  if (bytes == null || bytes.isEmpty()) {
    onResult(null, fileName, mime, "Unable to read the selected file.")
    return
  }

  val validationErr = SupportAttachmentLimits.validate(mime, bytes.size)
  if (validationErr != null) {
    onResult(null, fileName, mime, validationErr)
    return
  }

  onResult(bytes, fileName, mime, null)
}

fun formatSeconds(totalSeconds: Int): String {
  val s = totalSeconds.coerceAtLeast(0)
  return "%d:%02d".format(s / 60, s % 60)
}

fun formatMessageTime(date: Date?): String {
  if (date == null) return ""
  return SimpleDateFormat("h:mm a", Locale.getDefault()).format(date)
}
