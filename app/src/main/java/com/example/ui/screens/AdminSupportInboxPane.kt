package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.PictureAsPdf
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AudioPlaybackState
import com.example.data.SupportAudioHelper
import com.example.model.SupportConversation
import com.example.model.SupportConversationStatus
import com.example.model.SupportMessage
import com.example.model.SupportMessageType
import com.example.model.SupportSenderRole
import com.example.model.UiState
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OnEmeraldContainer
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun AdminSupportInboxPane(
  conversationsState: UiState<List<SupportConversation>>,
  activeConversation: SupportConversation?,
  messagesState: UiState<List<SupportMessage>>,
  currentAdminEmail: String,
  isBusy: Boolean,
  onSelectConversation: (String?) -> Unit,
  onSendAdminReply: (
    conversation: SupportConversation,
    text: String,
    messageType: SupportMessageType,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
  ) -> Unit,
  onUpdateStatusAndAssignee: (
    conversationId: String,
    status: SupportConversationStatus,
    assignedAdminId: String,
  ) -> Unit,
  onMarkConversationRead: (SupportConversation, List<SupportMessage>) -> Unit,
) {
  val context = LocalContext.current
  val audioHelper = remember(context) { SupportAudioHelper(context) }
  DisposableEffect(audioHelper) { onDispose { audioHelper.release() } }
  val playbackState by audioHelper.playbackState.collectAsStateWithLifecycle()

  var searchQuery by remember { mutableStateOf("") }
  var statusFilter by remember { mutableStateOf<SupportConversationStatus?>(null) }

  when (conversationsState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    is UiState.Error -> ErrorCard(conversationsState.message, Modifier.padding(16.dp))
    is UiState.Success -> {
      val allConversations = conversationsState.data
      val filtered =
        allConversations.filter { conv ->
          val matchesStatus = statusFilter == null || conv.statusEnum == statusFilter
          val q = searchQuery.trim().lowercase()
          val matchesQuery =
            q.isEmpty() ||
              conv.userName.lowercase().contains(q) ||
              conv.userId.lowercase().contains(q) ||
              conv.issueCategory.lowercase().contains(q) ||
              conv.subject.lowercase().contains(q)
          matchesStatus && matchesQuery
        }

      BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isTwoPane = maxWidth >= 680.dp
        if (isTwoPane) {
          Row(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.width(320.dp).fillMaxHeight()) {
              AdminInboxListPane(
                conversations = filtered,
                allConversations = allConversations,
                selectedId = activeConversation?.resolvedId,
                searchQuery = searchQuery,
                statusFilter = statusFilter,
                onSearchChange = { searchQuery = it },
                onStatusFilterChange = { statusFilter = it },
                onSelect = onSelectConversation,
              )
            }
            VerticalDivider()
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
              if (activeConversation != null) {
                AdminInboxThreadPane(
                  conversation = activeConversation,
                  messagesState = messagesState,
                  playbackState = playbackState,
                  audioHelper = audioHelper,
                  currentAdminEmail = currentAdminEmail,
                  isBusy = isBusy,
                  showBackButton = false,
                  onBack = { onSelectConversation(null) },
                  onSendReply = { text, type, bytes, name, mime ->
                    onSendAdminReply(activeConversation, text, type, bytes, name, mime)
                  },
                  onUpdateStatus = { status, assignee ->
                    onUpdateStatusAndAssignee(activeConversation.resolvedId, status, assignee)
                  },
                  onMarkRead = { msgs -> onMarkConversationRead(activeConversation, msgs) },
                )
              } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Rounded.SupportAgent, contentDescription = null, tint = DeepTeal, modifier = Modifier.size(44.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Select a conversation to reply", style = MaterialTheme.typography.titleMedium)
                  }
                }
              }
            }
          }
        } else {
          if (activeConversation != null) {
            BackHandler { onSelectConversation(null) }
            AdminInboxThreadPane(
              conversation = activeConversation,
              messagesState = messagesState,
              playbackState = playbackState,
              audioHelper = audioHelper,
              currentAdminEmail = currentAdminEmail,
              isBusy = isBusy,
              showBackButton = true,
              onBack = { onSelectConversation(null) },
              onSendReply = { text, type, bytes, name, mime ->
                onSendAdminReply(activeConversation, text, type, bytes, name, mime)
              },
              onUpdateStatus = { status, assignee ->
                onUpdateStatusAndAssignee(activeConversation.resolvedId, status, assignee)
              },
              onMarkRead = { msgs -> onMarkConversationRead(activeConversation, msgs) },
            )
          } else {
            AdminInboxListPane(
              conversations = filtered,
              allConversations = allConversations,
              selectedId = null,
              searchQuery = searchQuery,
              statusFilter = statusFilter,
              onSearchChange = { searchQuery = it },
              onStatusFilterChange = { statusFilter = it },
              onSelect = onSelectConversation,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun AdminInboxListPane(
  conversations: List<SupportConversation>,
  allConversations: List<SupportConversation>,
  selectedId: String?,
  searchQuery: String,
  statusFilter: SupportConversationStatus?,
  onSearchChange: (String) -> Unit,
  onStatusFilterChange: (SupportConversationStatus?) -> Unit,
  onSelect: (String) -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchChange,
        placeholder = { Text("Search name, UID or category") },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("admin_support_search"),
      )
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        FilterChip(
          selected = statusFilter == null,
          onClick = { onStatusFilterChange(null) },
          label = { Text("All (${allConversations.size})") },
        )
        SupportConversationStatus.entries.forEach { st ->
          val count = allConversations.count { it.statusEnum == st }
          FilterChip(
            selected = statusFilter == st,
            onClick = { onStatusFilterChange(st) },
            label = { Text("${st.label} ($count)") },
          )
        }
      }
    }
    HorizontalDivider()
    if (conversations.isEmpty()) {
      Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
        Text("No matching support conversations", color = MaterialTheme.colorScheme.onSurfaceVariant)
      }
    } else {
      LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        items(conversations, key = { it.resolvedId }) { conv ->
          val isSelected = selectedId == conv.resolvedId
          Card(
            shape = RoundedCornerShape(14.dp),
            colors =
              CardDefaults.cardColors(
                containerColor = if (isSelected) EmeraldContainer.copy(alpha = 0.45f) else Color.White
              ),
            border =
              BorderStroke(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) DeepTeal else MaterialTheme.colorScheme.outlineVariant,
              ),
            modifier = Modifier.fillMaxWidth().clickable { onSelect(conv.resolvedId) },
          ) {
            Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = conv.userName.ifBlank { conv.userId },
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                if (conv.unreadCountAdmin > 0) {
                  Surface(shape = CircleShape, color = DeepTeal) {
                    Text(
                      text = "${conv.unreadCountAdmin}",
                      style = MaterialTheme.typography.labelSmall,
                      color = Color.White,
                      modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                    )
                  }
                }
              }
              Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                  Text(
                    text = conv.accountType.ifBlank { "Customer" },
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                  )
                }
                Text(
                  text = conv.issueCategory,
                  style = MaterialTheme.typography.labelSmall,
                  color = DeepTeal,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                  modifier = Modifier.weight(1f),
                )
                SupportStatusPill(status = conv.statusEnum)
              }
              Text(
                text = conv.lastMessage.ifBlank { conv.subject },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminInboxThreadPane(
  conversation: SupportConversation,
  messagesState: UiState<List<SupportMessage>>,
  playbackState: AudioPlaybackState,
  audioHelper: SupportAudioHelper,
  currentAdminEmail: String,
  isBusy: Boolean,
  showBackButton: Boolean,
  onBack: () -> Unit,
  onSendReply: (
    text: String,
    messageType: SupportMessageType,
    attachmentBytes: ByteArray?,
    attachmentName: String,
    attachmentMime: String,
  ) -> Unit,
  onUpdateStatus: (SupportConversationStatus, String) -> Unit,
  onMarkRead: (List<SupportMessage>) -> Unit,
) {
  val context = LocalContext.current
  var replyText by remember(conversation.resolvedId) { mutableStateOf("") }
  var showAttachSheet by remember { mutableStateOf(false) }
  var attachErr by remember { mutableStateOf<String?>(null) }
  var zoomedImageUrl by remember { mutableStateOf<String?>(null) }

  val messages = (messagesState as? UiState.Success)?.data ?: emptyList()
  val listState = rememberLazyListState()

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }
  LaunchedEffect(conversation.resolvedId, messages.size) {
    if (messages.any { it.roleEnum == SupportSenderRole.USER && it.readAt == null } ||
      conversation.unreadCountAdmin > 0
    ) {
      onMarkRead(messages)
    }
  }

  val imageLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.IMAGE) { bytes, name, mime, err ->
          if (err != null) attachErr = err
          else {
            attachErr = null
            onSendReply(replyText.trim(), SupportMessageType.IMAGE, bytes, name, mime)
            replyText = ""
          }
        }
      }
    }

  val pdfLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
      if (uri != null) {
        readValidatedAttachment(context, uri, SupportMessageType.DOCUMENT) { bytes, name, mime, err ->
          if (err != null) attachErr = err
          else {
            attachErr = null
            onSendReply(replyText.trim(), SupportMessageType.DOCUMENT, bytes, name, mime)
            replyText = ""
          }
        }
      }
    }

  Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        if (showBackButton) {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to Inbox")
          }
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "${conversation.userName.ifBlank { "User" }} (${conversation.accountType})",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "${conversation.issueCategory} • UID: ${conversation.userId}${if (conversation.userEmail.isNotBlank()) " • ${conversation.userEmail}" else ""}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        SupportStatusPill(status = conversation.statusEnum)
      }

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        SupportConversationStatus.entries.forEach { st ->
          FilterChip(
            selected = conversation.statusEnum == st,
            onClick = {
              val nextAssignee = conversation.assignedAdminId.ifBlank { currentAdminEmail }
              onUpdateStatus(st, nextAssignee)
            },
            label = { Text(if (st == SupportConversationStatus.OPEN && conversation.statusEnum == SupportConversationStatus.RESOLVED) "Reopen" else st.label) },
          )
        }
        OutlinedButton(
          onClick = {
            onUpdateStatus(conversation.statusEnum, currentAdminEmail)
          },
          shape = RoundedCornerShape(10.dp),
        ) {
          Text(
            text =
              if (conversation.assignedAdminId.isBlank()) "Assign to Me"
              else "Assigned: ${conversation.assignedAdminId.substringBefore('@')}",
            style = MaterialTheme.typography.labelSmall,
          )
        }
      }
    }
    HorizontalDivider()

    Box(modifier = Modifier.weight(1f).fillMaxWidth().background(LightBackground.copy(alpha = 0.5f))) {
      when (messagesState) {
        is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
        is UiState.Error -> ErrorCard(messagesState.message, Modifier.padding(16.dp))
        is UiState.Success -> {
          LazyColumn(
            state = listState,
            contentPadding = PaddingValues(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            items(messages, key = { it.resolvedId }) { msg ->
              SupportBubble(
                message = msg,
                playbackState = playbackState,
                viewerIsAdmin = true,
                onToggleVoice = { id, url, dur ->
                  audioHelper.toggleRemoteMessagePlayback(id, url, dur)
                },
                onOpenImage = { url -> zoomedImageUrl = url },
                onOpenDocument = { url ->
                  runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
                },
              )
            }
          }
        }
      }
    }

    if (!attachErr.isNullOrBlank()) {
      Text(
        text = attachErr!!,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.error,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
      )
    }

    HorizontalDivider()
    Row(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      IconButton(onClick = { showAttachSheet = true }, enabled = !isBusy) {
        Icon(Icons.Rounded.AttachFile, contentDescription = "Attach file")
      }
      OutlinedTextField(
        value = replyText,
        onValueChange = { replyText = it },
        placeholder = { Text("Reply as SkillConnect Support...") },
        maxLines = 3,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.weight(1f).testTag("admin_support_reply_input"),
      )
      IconButton(
        onClick = {
          val txt = replyText.trim()
          if (txt.isNotEmpty()) {
            onSendReply(txt, SupportMessageType.TEXT, null, "", "")
            replyText = ""
          }
        },
        enabled = !isBusy && replyText.trim().isNotEmpty(),
        modifier = Modifier.testTag("admin_support_send_button"),
      ) {
        Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = "Send Reply", tint = DeepTeal)
      }
    }
  }

  if (showAttachSheet) {
    ModalBottomSheet(onDismissRequest = { showAttachSheet = false }) {
      Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        ListItem(
          headlineContent = { Text("Send Image") },
          leadingContent = { Icon(Icons.Rounded.Image, contentDescription = null, tint = DeepTeal) },
          modifier = Modifier.clickable {
            showAttachSheet = false
            imageLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
          },
        )
        ListItem(
          headlineContent = { Text("Send PDF Document") },
          leadingContent = { Icon(Icons.Rounded.PictureAsPdf, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
          modifier = Modifier.clickable {
            showAttachSheet = false
            pdfLauncher.launch(arrayOf("application/pdf"))
          },
        )
      }
    }
  }

  zoomedImageUrl?.let { url ->
    Dialog(onDismissRequest = { zoomedImageUrl = null }) {
      Surface(shape = RoundedCornerShape(16.dp), color = Color.Black) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.End) {
          IconButton(onClick = { zoomedImageUrl = null }) {
            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
          }
          AsyncImage(
            model = url,
            contentDescription = "Support image",
            contentScale = ContentScale.Fit,
            modifier = Modifier.fillMaxWidth().heightIn(max = 460.dp).clip(RoundedCornerShape(12.dp)),
          )
          TextButton(onClick = {
            runCatching { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url))) }
          }) {
            Text("Open in Browser", color = Color.White)
          }
        }
      }
    }
  }
}
