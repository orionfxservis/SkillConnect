package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Stop
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.SupportAudioHelper
import com.example.model.PendingSupportMessage
import com.example.model.SupportAttachmentLimits
import com.example.model.SupportConversation
import com.example.model.SupportIssueCategory
import com.example.model.SupportMessage
import com.example.model.SupportMessageType
import com.example.model.UiState
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OnEmeraldContainer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SupportScreen(
  isSignedIn: Boolean,
  isBusy: Boolean,
  conversationsState: UiState<List<SupportConversation>>,
  activeConversation: SupportConversation?,
  messagesState: UiState<List<SupportMessage>>,
  pendingMessages: List<PendingSupportMessage>,
  onBack: () -> Unit,
  onSignInClick: () -> Unit,
  onSelectConversation: (String?) -> Unit,
  onStartNewConversation: (
    category: SupportIssueCategory,
    subject: String,
    initialMessageType: SupportMessageType,
    text: String,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
    durationSec: Int,
  ) -> Unit,
  onSendMessage: (
    conversation: SupportConversation,
    messageType: SupportMessageType,
    text: String,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
    durationSec: Int,
  ) -> Unit,
  onRetryPending: (SupportConversation, String) -> Unit,
  onDismissPending: (String) -> Unit,
  onMarkActiveRead: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (isSignedIn && activeConversation != null) {
    SupportChatView(
      conversation = activeConversation,
      messagesState = messagesState,
      pendingMessages = pendingMessages,
      onBack = { onSelectConversation(null) },
      onMarkRead = onMarkActiveRead,
      onSendMessage = { type, text, bytes, name, mime, dur ->
        onSendMessage(activeConversation, type, text, bytes, name, mime, dur)
      },
      onRetryPending = { msgId -> onRetryPending(activeConversation, msgId) },
      onDismissPending = onDismissPending,
      modifier = modifier,
    )
    return
  }

  var showNewTicketSheet by remember { mutableStateOf(false) }
  val conversations = (conversationsState as? UiState.Success)?.data ?: emptyList()

  Box(modifier = modifier.fillMaxSize().background(LightBackground)) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("support_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = "Back",
            )
          }
          Spacer(modifier = Modifier.width(4.dp))
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
              text = "Help & Support",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Have a problem? Send us a message. Our support team is here to help.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Prominent Support Card
      item {
        Card(
          shape = RoundedCornerShape(22.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          modifier = Modifier.fillMaxWidth().testTag("support_team_card"),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(EmeraldContainer),
                contentAlignment = Alignment.Center,
              ) {
                Icon(
                  imageVector = Icons.Rounded.SupportAgent,
                  contentDescription = null,
                  tint = OnEmeraldContainer,
                  modifier = Modifier.size(26.dp),
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "SkillConnect Support Team",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(shape = RoundedCornerShape(50), color = EmeraldContainer) {
                  Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                  ) {
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(DeepTeal))
                    Text(
                      text = "Online • Direct In-App Support",
                      style = MaterialTheme.typography.labelSmall,
                      color = OnEmeraldContainer,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }
            }

            Text(
              text = "Contact our support team directly through the application.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Button(
              onClick = {
                if (isSignedIn) showNewTicketSheet = true else onSignInClick()
              },
              shape = RoundedCornerShape(14.dp),
              colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
              modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_new_message_button"),
            ) {
              Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(20.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isSignedIn) "New Message" else "Sign in to Send New Message",
                fontWeight = FontWeight.SemiBold,
              )
            }
          }
        }
      }

      // Previous Conversations
      item {
        Text(
          text = "Previous Conversations",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }

      if (!isSignedIn) {
        item {
          Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(18.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = "Sign in to view your support conversations",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "Your support messages, voice notes, and attachments stay synced across sessions under your SkillConnect account.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Button(
                onClick = onSignInClick,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_sign_in_button"),
              ) {
                Text("Sign in with Google")
              }
            }
          }
        }
      } else {
        when (conversationsState) {
          is UiState.Loading -> {
            item {
              Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = DeepTeal)
              }
            }
          }
          is UiState.Error -> {
            item { ErrorCard(message = conversationsState.message) }
          }
          is UiState.Success -> {
            if (conversations.isEmpty()) {
              item {
                Card(
                  shape = RoundedCornerShape(18.dp),
                  colors = CardDefaults.cardColors(containerColor = Color.White),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Rounded.Chat,
                      contentDescription = null,
                      tint = DeepTeal,
                      modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                      text = "No previous conversations yet. Tap New Message above to start chatting with our support team.",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                }
              }
            } else {
              items(conversations, key = { it.resolvedId }) { conv ->
                SupportConversationItemCard(
                  conversation = conv,
                  onClick = { onSelectConversation(conv.resolvedId) },
                )
              }
            }
          }
        }
      }
    }
  }

  if (showNewTicketSheet) {
    NewSupportConversationSheet(
      isBusy = isBusy,
      onDismiss = { showNewTicketSheet = false },
      onSubmit = { category, subject, msgType, text, bytes, fileName, mime, dur ->
        showNewTicketSheet = false
        onStartNewConversation(category, subject, msgType, text, bytes, fileName, mime, dur)
      },
    )
  }
}

@Composable
private fun SupportConversationItemCard(
  conversation: SupportConversation,
  onClick: () -> Unit,
) {
  val dateStr =
    remember(conversation.lastMessageAt) {
      conversation.lastMessageAt?.toDate()?.let {
        SimpleDateFormat("dd MMM • h:mm a", Locale.getDefault()).format(it)
      } ?: ""
    }

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("support_conversation_${conversation.resolvedId}"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = EmeraldContainer.copy(alpha = 0.65f),
        ) {
          Text(
            text = conversation.issueCategory,
            style = MaterialTheme.typography.labelSmall,
            color = OnEmeraldContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
          )
        }
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          if (conversation.unreadCountUser > 0) {
            Surface(shape = CircleShape, color = DeepTeal) {
              Text(
                text = "${conversation.unreadCountUser}",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
              )
            }
          }
          SupportStatusPill(status = conversation.statusEnum)
        }
      }

      if (conversation.subject.isNotBlank() && conversation.subject != conversation.issueCategory) {
        Text(
          text = conversation.subject,
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Text(
        text = conversation.lastMessage.ifBlank { "Conversation opened" },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
      )

      if (dateStr.isNotBlank()) {
        Text(
          text = dateStr,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun NewSupportConversationSheet(
  isBusy: Boolean,
  onDismiss: () -> Unit,
  onSubmit: (
    category: SupportIssueCategory,
    subject: String,
    initialMessageType: SupportMessageType,
    text: String,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
    durationSec: Int,
  ) -> Unit,
) {
  val context = LocalContext.current
  val audioHelper = remember(context) { SupportAudioHelper(context) }
  DisposableEffect(audioHelper) { onDispose { audioHelper.release() } }
  val recorderState by audioHelper.recorderState.collectAsStateWithLifecycle()

  var selectedCategory by remember { mutableStateOf(SupportIssueCategory.BOOKING_PROBLEM) }
  var description by remember { mutableStateOf("") }
  var attachedBytes by remember { mutableStateOf<ByteArray?>(null) }
  var attachedName by remember { mutableStateOf("") }
  var attachedMime by remember { mutableStateOf("") }
  var attachedType by remember { mutableStateOf<SupportMessageType?>(null) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val recordAudioPermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      if (granted) {
        errorMsg = null
        attachedBytes = null
        attachedType = null
        audioHelper.startRecording()
      } else {
        errorMsg = "Microphone permission is required to record voice messages."
      }
    }

  val imageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.IMAGE) { bytes, name, mime, err ->
          if (err != null) errorMsg = err
          else {
            audioHelper.deleteDraftRecording()
            attachedBytes = bytes
            attachedName = name
            attachedMime = mime
            attachedType = SupportMessageType.IMAGE
            errorMsg = null
          }
        }
      }
    }

  val pdfLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.DOCUMENT) { bytes, name, mime, err ->
          if (err != null) errorMsg = err
          else {
            audioHelper.deleteDraftRecording()
            attachedBytes = bytes
            attachedName = name
            attachedMime = mime
            attachedType = SupportMessageType.DOCUMENT
            errorMsg = null
          }
        }
      }
    }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 20.dp)
          .padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(
        text = "Start Support Conversation",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Select an issue category",
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
      )

      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        SupportIssueCategory.entries.forEach { cat ->
          val selected = selectedCategory == cat
          FilterChip(
            selected = selected,
            onClick = { selectedCategory = cat },
            label = { Text(cat.label) },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldContainer,
                selectedLabelColor = OnEmeraldContainer,
              ),
            modifier = Modifier.testTag("support_cat_${cat.name.lowercase()}"),
          )
        }
      }

      OutlinedTextField(
        value = description,
        onValueChange = { description = it },
        label = { Text("Describe your issue") },
        placeholder = { Text("Share booking details, account info, or what happened...") },
        minLines = 3,
        maxLines = 5,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().testTag("support_new_description_input"),
      )

      Text(
        text = "Optional screenshot, PDF, or voice message",
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = {
            imageLauncher.launch(
              PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
            )
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(44.dp),
        ) {
          Icon(Icons.Rounded.Image, contentDescription = null, modifier = Modifier.size(17.dp))
          Spacer(Modifier.width(4.dp))
          Text("Photo", maxLines = 1)
        }
        OutlinedButton(
          onClick = { pdfLauncher.launch(arrayOf("application/pdf")) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(44.dp),
        ) {
          Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, modifier = Modifier.size(17.dp))
          Spacer(Modifier.width(4.dp))
          Text("PDF", maxLines = 1)
        }
        OutlinedButton(
          onClick = {
            if (recorderState.isRecording) {
              audioHelper.stopRecording()
            } else {
              val granted =
                ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
                  PackageManager.PERMISSION_GRANTED
              if (granted) {
                errorMsg = null
                attachedBytes = null
                attachedType = null
                audioHelper.startRecording()
              } else {
                recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
              }
            }
          },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(44.dp),
        ) {
          Icon(
            if (recorderState.isRecording) Icons.Rounded.Stop else Icons.Rounded.Mic,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = if (recorderState.isRecording) MaterialTheme.colorScheme.error else DeepTeal,
          )
          Spacer(Modifier.width(4.dp))
          Text(if (recorderState.isRecording) "${recorderState.recordingSeconds}s" else "Voice", maxLines = 1)
        }
      }

      if (attachedBytes != null && attachedType != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldContainer.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Attached: $attachedName",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.weight(1f),
            )
            IconButton(
              onClick = {
                attachedBytes = null
                attachedType = null
                attachedName = ""
                attachedMime = ""
              },
              modifier = Modifier.size(28.dp),
            ) {
              Icon(Icons.Rounded.Close, contentDescription = "Remove attachment", modifier = Modifier.size(16.dp))
            }
          }
        }
      } else if (recorderState.recordedFilePath != null) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldContainer.copy(alpha = 0.6f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Text(
              text = "Voice note recorded (${formatSeconds(recorderState.recordedDurationSec)})",
              style = MaterialTheme.typography.bodySmall,
              modifier = Modifier.weight(1f),
            )
            IconButton(
              onClick = { audioHelper.deleteDraftRecording() },
              modifier = Modifier.size(28.dp),
            ) {
              Icon(Icons.Rounded.Close, contentDescription = "Remove voice", modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      val displayErr = errorMsg ?: recorderState.errorMessage
      if (!displayErr.isNullOrBlank()) {
        Text(
          text = displayErr,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error,
        )
      }

      val hasVoiceDraft = recorderState.recordedFilePath != null && !recorderState.isRecording
      val canSubmit =
        !isBusy &&
          !recorderState.isRecording &&
          (description.trim().isNotEmpty() || attachedBytes != null || hasVoiceDraft)

      Button(
        onClick = {
          val trimmed = description.trim()
          val subject = trimmed.lineSequence().firstOrNull()?.take(70).orEmpty().ifBlank { selectedCategory.label }
          when {
            hasVoiceDraft -> {
              val vBytes = audioHelper.readRecordedBytes()
              val dur = recorderState.recordedDurationSec.coerceAtLeast(1)
              if (vBytes == null || SupportAttachmentLimits.validate("audio/mp4", vBytes.size) != null) {
                errorMsg = "Recorded voice file could not be validated."
                return@Button
              }
              onSubmit(
                selectedCategory,
                subject,
                SupportMessageType.VOICE,
                trimmed,
                vBytes,
                "voice_${System.currentTimeMillis()}.m4a",
                "audio/mp4",
                dur,
              )
            }
            attachedBytes != null && attachedType != null -> {
              onSubmit(
                selectedCategory,
                subject,
                attachedType!!,
                trimmed,
                attachedBytes,
                attachedName,
                attachedMime,
                0,
              )
            }
            else -> {
              onSubmit(
                selectedCategory,
                subject,
                SupportMessageType.TEXT,
                trimmed,
                null,
                "",
                "",
                0,
              )
            }
          }
        },
        enabled = canSubmit,
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("support_submit_new_conversation"),
      ) {
        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text("Start Support Chat", fontWeight = FontWeight.SemiBold)
      }
    }
  }
}
