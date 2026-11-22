package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Flag
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LocationHelper
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.ServiceBooking
import com.example.model.ServiceComplaint
import com.example.model.TechnicianProfile
import com.example.model.UiState
import com.example.ui.theme.DispatchAmber
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold
import kotlin.math.cos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailAndChatScreen(
  bookingState: UiState<ServiceBooking?>,
  trackedTechState: UiState<TechnicianProfile?>,
  messagesState: UiState<List<ChatMessage>>,
  myComplaintsState: UiState<List<ServiceComplaint>>,
  currentUserId: String,
  currentUserName: String,
  isLiveBroadcastActive: Boolean,
  isBusy: Boolean,
  onBack: () -> Unit,
  onUpdateStatus: (ServiceBooking, BookingStatus) -> Unit,
  onToggleLiveGpsBroadcast: () -> Unit,
  onSendMessage: (ServiceBooking, String, String) -> Unit,
  onSubmitReview: (ServiceBooking, Int, String, List<String>) -> Unit,
  onSubmitComplaint: (ServiceBooking, String, String, String) -> Unit,
) {
  val context = LocalContext.current
  var messageDraft by remember { mutableStateOf("") }
  var showReviewDialog by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Dispatch & Chat") },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("booking_detail_back_button")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
        },
      )
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding).imePadding()) {
      when (bookingState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is UiState.Error -> {
          ErrorCard(message = bookingState.message, modifier = Modifier.padding(16.dp))
        }
        is UiState.Success -> {
          val booking = bookingState.data
          if (booking == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("Booking not found.")
            }
          } else {
            val isClient = booking.clientId == currentUserId
            val isTechnician = booking.technicianId == currentUserId
            val trackedTech = (trackedTechState as? UiState.Success)?.data
            val bookingComplaints =
              (myComplaintsState as? UiState.Success)?.data?.filter { it.bookingId == booking.id }
                ?: emptyList()

            Column(
              modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter)
            ) {
              LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                item {
                  DispatchStatusCard(
                    booking = booking,
                    trackedTech = trackedTech,
                    bookingComplaints = bookingComplaints,
                    isClient = isClient,
                    isTechnician = isTechnician,
                    isLiveBroadcastActive = isLiveBroadcastActive,
                    isBusy = isBusy,
                    onCallCounterpart = { phone ->
                      val intent =
                        Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:$phone") }
                      context.startActivity(intent)
                    },
                    onUpdateStatus = { next -> onUpdateStatus(booking, next) },
                    onToggleLiveGpsBroadcast = onToggleLiveGpsBroadcast,
                    onOpenReview = { showReviewDialog = true },
                    onOpenReport = { showReportDialog = true },
                  )
                }

                item {
                  Text(
                    text = "Private Booking Messages",
                    style = MaterialTheme.typography.titleMedium,
                  )
                }

                when (messagesState) {
                  is UiState.Loading -> {
                    item {
                      CircularProgressIndicator(modifier = Modifier.padding(vertical = 12.dp))
                    }
                  }
                  is UiState.Error -> {
                    item { ErrorCard(message = messagesState.message) }
                  }
                  is UiState.Success -> {
                    val msgs = messagesState.data
                    if (msgs.isEmpty()) {
                      item {
                        Surface(
                          shape = RoundedCornerShape(14.dp),
                          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                          modifier = Modifier.fillMaxWidth(),
                        ) {
                          Text(
                            text =
                              "No messages yet. Coordinate arrival time, directions, or tools needed right here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(14.dp),
                          )
                        }
                      }
                    } else {
                      items(msgs, key = { it.id }) { msg ->
                        ChatBubble(msg = msg, isMine = msg.senderId == currentUserId)
                      }
                    }
                  }
                }
              }

              // Bottom Chat Input Bar
              Surface(
                tonalElevation = 3.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  OutlinedTextField(
                    value = messageDraft,
                    onValueChange = { messageDraft = it },
                    placeholder = {
                      Text(
                        "Message ${if (isClient) booking.technicianName else booking.clientName}..."
                      )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.weight(1f).testTag("chat_message_input"),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  IconButton(
                    onClick = {
                      val text = messageDraft.trim()
                      if (text.isNotEmpty()) {
                        messageDraft = ""
                        onSendMessage(booking, currentUserName, text)
                      }
                    },
                    enabled = messageDraft.trim().isNotEmpty(),
                    modifier =
                      Modifier.size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .testTag("chat_send_button"),
                  ) {
                    Icon(
                      Icons.AutoMirrored.Rounded.Send,
                      contentDescription = "Send message",
                      tint = MaterialTheme.colorScheme.onPrimary,
                    )
                  }
                }
              }
            }

            if (showReviewDialog) {
              ReviewDialog(
                technicianName = booking.technicianName,
                onDismiss = { showReviewDialog = false },
                onSubmit = { stars, comment, tags ->
                  showReviewDialog = false
                  onSubmitReview(booking, stars, comment, tags)
                },
              )
            }

            if (showReportDialog) {
              ReportIssueDialog(
                counterpartName = if (isClient) booking.technicianName else booking.clientName,
                onDismiss = { showReportDialog = false },
                onSubmit = { reason, description ->
                  showReportDialog = false
                  onSubmitComplaint(booking, currentUserName, reason, description)
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DispatchStatusCard(
  booking: ServiceBooking,
  trackedTech: TechnicianProfile?,
  bookingComplaints: List<ServiceComplaint>,
  isClient: Boolean,
  isTechnician: Boolean,
  isLiveBroadcastActive: Boolean,
  isBusy: Boolean,
  onCallCounterpart: (String) -> Unit,
  onUpdateStatus: (BookingStatus) -> Unit,
  onToggleLiveGpsBroadcast: () -> Unit,
  onOpenReview: () -> Unit,
  onOpenReport: () -> Unit,
) {
  val status = booking.bookingStatus
  val (pillBg, pillFg) = statusColors(status)

  val distKm =
    if (trackedTech != null) {
      LocationHelper.distanceKm(
        booking.clientLat,
        booking.clientLng,
        trackedTech.latitude,
        trackedTech.longitude,
      )
    } else {
      -1.0
    }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(text = booking.serviceTitle, style = MaterialTheme.typography.titleLarge)
          Text(
            text =
              if (isClient) "Technician: ${booking.technicianName}"
              else "Client: ${booking.clientName}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Surface(shape = RoundedCornerShape(50), color = pillBg) {
          Text(
            text = status.label,
            style = MaterialTheme.typography.labelMedium,
            color = pillFg,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
      }

      Text(
        text = booking.issueDescription,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text = "Area: ${booking.serviceArea} • ${booking.preferredTime}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      // Live Location Dispatch Canvas when active
      if (status in setOf(BookingStatus.ACCEPTED, BookingStatus.EN_ROUTE, BookingStatus.IN_PROGRESS)) {
        LiveRouteTrackerCanvas(
          clientLat = booking.clientLat,
          clientLng = booking.clientLng,
          techLat = trackedTech?.latitude ?: 0.0,
          techLng = trackedTech?.longitude ?: 0.0,
          distanceKm = distKm,
          status = status,
        )
      }

      // Call & Action Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        val phoneToCall = if (isClient) booking.technicianPhone else booking.clientPhone
        OutlinedButton(
          onClick = { onCallCounterpart(phoneToCall) },
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.weight(1f).height(48.dp),
        ) {
          Icon(Icons.Rounded.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (isClient) "Call Pro" else "Call Client")
        }

        if (isClient && status in setOf(BookingStatus.REQUESTED, BookingStatus.ACCEPTED)) {
          OutlinedButton(
            onClick = { onUpdateStatus(BookingStatus.CANCELLED) },
            enabled = !isBusy,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).height(48.dp).testTag("cancel_booking_button"),
          ) {
            Text("Cancel")
          }
        }

        OutlinedButton(
          onClick = onOpenReport,
          enabled = !isBusy,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.height(48.dp).testTag("report_issue_button"),
        ) {
          Icon(Icons.Rounded.Flag, contentDescription = "Report issue", modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Report")
        }
      }

      if (bookingComplaints.isNotEmpty()) {
        bookingComplaints.forEach { complaint ->
          Surface(
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Report (${complaint.statusEnum.label}): ${complaint.reason}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
              )
              if (complaint.adminResolution.isNotBlank()) {
                Text(
                  text = "Admin Note: ${complaint.adminResolution}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onErrorContainer,
                )
              }
            }
          }
        }
      }

      // Technician Workflow Controls
      if (isTechnician && status !in setOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED)) {
        if (status in setOf(BookingStatus.ACCEPTED, BookingStatus.EN_ROUTE)) {
          Surface(
            color =
              if (isLiveBroadcastActive) EmeraldContainer
              else MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(14.dp),
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onToggleLiveGpsBroadcast)
                .testTag("toggle_live_dispatch_gps"),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  Icons.Rounded.GpsFixed,
                  contentDescription = null,
                  tint = if (isLiveBroadcastActive) OnEmeraldContainer else MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                  Text(
                    text =
                      if (isLiveBroadcastActive) "Sharing Live GPS Location"
                      else "Share Live GPS with Client",
                    style = MaterialTheme.typography.titleSmall,
                  )
                  Text(
                    text = "Tap to ${if (isLiveBroadcastActive) "pause" else "start"} real-time dispatch tracking",
                    style = MaterialTheme.typography.bodySmall,
                  )
                }
              }
            }
          }
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          when (status) {
            BookingStatus.REQUESTED -> {
              Button(
                onClick = { onUpdateStatus(BookingStatus.ACCEPTED) },
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("accept_booking_button"),
              ) {
                Text("Accept Job")
              }
              OutlinedButton(
                onClick = { onUpdateStatus(BookingStatus.CANCELLED) },
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp),
              ) {
                Text("Decline")
              }
            }
            BookingStatus.ACCEPTED -> {
              Button(
                onClick = { onUpdateStatus(BookingStatus.EN_ROUTE) },
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("en_route_button"),
              ) {
                Icon(Icons.Rounded.DirectionsCar, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Start Dispatch (On the Way)")
              }
            }
            BookingStatus.EN_ROUTE -> {
              Button(
                onClick = { onUpdateStatus(BookingStatus.IN_PROGRESS) },
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f).height(48.dp).testTag("start_work_button"),
              ) {
                Text("Arrived & Start Work")
              }
            }
            BookingStatus.IN_PROGRESS -> {
              Button(
                onClick = { onUpdateStatus(BookingStatus.COMPLETED) },
                enabled = !isBusy,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldLive),
                modifier = Modifier.weight(1f).height(48.dp).testTag("complete_job_button"),
              ) {
                Icon(Icons.Rounded.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Mark Work Completed")
              }
            }
            else -> {}
          }
        }
      }

      // Client Post-Service Review Banner
      if (isClient && status == BookingStatus.COMPLETED) {
        if (!booking.reviewed) {
          Button(
            onClick = onOpenReview,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("open_review_dialog_button"),
          ) {
            Icon(Icons.Rounded.Star, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Rate & Review ${booking.technicianName}")
          }
        } else {
          Surface(
            color = EmeraldContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                Icons.Rounded.CheckCircle,
                contentDescription = null,
                tint = OnEmeraldContainer,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Thank you! Your verified review updated this pro's rating.",
                style = MaterialTheme.typography.bodySmall,
                color = OnEmeraldContainer,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LiveRouteTrackerCanvas(
  clientLat: Double,
  clientLng: Double,
  techLat: Double,
  techLng: Double,
  distanceKm: Double,
  status: BookingStatus,
) {
  val primaryColor = MaterialTheme.colorScheme.primary
  val outlineColor = MaterialTheme.colorScheme.outlineVariant
  val hasCoords = (clientLat != 0.0 || clientLng != 0.0) && (techLat != 0.0 || techLng != 0.0)

  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text =
            when (status) {
              BookingStatus.EN_ROUTE -> "Live Dispatch • Technician En Route"
              BookingStatus.IN_PROGRESS -> "Technician On Site"
              else -> "Dispatch Location Link"
            },
          style = MaterialTheme.typography.labelLarge,
          color = MaterialTheme.colorScheme.primary,
        )
        Text(
          text =
            if (distanceKm >= 0.0)
              "${LocationHelper.formatDistance(distanceKm)} • ${LocationHelper.estimateEtaMinutes(distanceKm)}"
            else "Awaiting live GPS",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Canvas(modifier = Modifier.fillMaxWidth().height(110.dp)) {
        val left = Offset(size.width * 0.16f, size.height * 0.52f)
        val right = Offset(size.width * 0.84f, size.height * 0.52f)

        val techPos =
          if (hasCoords) {
            val dLat = (techLat - clientLat) * 111.0
            val dLng = (techLng - clientLng) * 111.0 * cos(Math.toRadians(clientLat))
            val normX = (dLng / 10.0).toFloat().coerceIn(-0.32f, 0.32f)
            val normY = (-dLat / 10.0).toFloat().coerceIn(-0.32f, 0.32f)
            Offset(size.width * (0.5f + normX), size.height * (0.52f + normY))
          } else {
            left
          }
        val clientPos = if (hasCoords) Offset(size.width * 0.5f, size.height * 0.52f) else right

        drawLine(
          color = outlineColor,
          start = techPos,
          end = clientPos,
          strokeWidth = 6f,
          pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 10f)),
        )

        drawCircle(color = Color.White, radius = 18f, center = techPos)
        drawCircle(color = DispatchAmber, radius = 12f, center = techPos)

        drawCircle(color = Color.White, radius = 18f, center = clientPos)
        drawCircle(color = primaryColor, radius = 12f, center = clientPos)
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Text(
          text = "● Technician GPS",
          style = MaterialTheme.typography.labelSmall,
          color = DispatchAmber,
        )
        Text(
          text = "● Service Location",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
        )
      }
    }
  }
}

@Composable
private fun ChatBubble(msg: ChatMessage, isMine: Boolean) {
  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
  ) {
    Surface(
      shape =
        RoundedCornerShape(
          topStart = 16.dp,
          topEnd = 16.dp,
          bottomStart = if (isMine) 16.dp else 4.dp,
          bottomEnd = if (isMine) 4.dp else 16.dp,
        ),
      color =
        if (isMine) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.widthIn(max = 300.dp),
    ) {
      Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
        if (!isMine) {
          Text(
            text = msg.senderName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.height(2.dp))
        }
        Text(
          text = msg.text,
          style = MaterialTheme.typography.bodyMedium,
          color =
            if (isMine) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewDialog(
  technicianName: String,
  onDismiss: () -> Unit,
  onSubmit: (rating: Int, comment: String, highlights: List<String>) -> Unit,
) {
  var rating by remember { mutableIntStateOf(5) }
  var comment by remember { mutableStateOf("") }
  val allTags = listOf("Punctual", "Fair Pricing", "Clean Work", "Expert Diagnostics", "Friendly")
  var selectedTags by remember { mutableStateOf(setOf("Punctual")) }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Rate $technicianName") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Your review updates the technician's community score and accountability.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.Center,
        ) {
          (1..5).forEach { star ->
            IconButton(
              onClick = { rating = star },
              modifier = Modifier.testTag("review_star_$star"),
            ) {
              Icon(
                imageVector = if (star <= rating) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                contentDescription = "$star stars",
                tint = if (star <= rating) StarGold else MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(34.dp),
              )
            }
          }
        }
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          allTags.forEach { tag ->
            val selected = tag in selectedTags
            FilterChip(
              selected = selected,
              onClick = {
                selectedTags = if (selected) selectedTags - tag else selectedTags + tag
              },
              label = { Text(tag) },
            )
          }
        }
        OutlinedTextField(
          value = comment,
          onValueChange = { comment = it },
          label = { Text("Share your experience") },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("review_comment_input"),
        )
        if (errorText != null) {
          Text(
            text = errorText!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (comment.trim().length < 2) {
            errorText = "Please enter a brief review comment."
          } else {
            onSubmit(rating, comment, selectedTags.toList())
          }
        },
        modifier = Modifier.testTag("submit_review_button"),
      ) {
        Text("Publish Review")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReportIssueDialog(
  counterpartName: String,
  onDismiss: () -> Unit,
  onSubmit: (reason: String, description: String) -> Unit,
) {
  val presetReasons =
    listOf("No Show / Late", "Overcharging", "Incomplete Work", "Unprofessional", "Other")
  var reason by remember { mutableStateOf(presetReasons.first()) }
  var description by remember { mutableStateOf("") }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Report Issue ($counterpartName)") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Reports are reviewed by the Super Admin team for verification & quality.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          presetReasons.forEach { r ->
            FilterChip(
              selected = reason == r,
              onClick = { reason = r },
              label = { Text(r) },
            )
          }
        }
        OutlinedTextField(
          value = description,
          onValueChange = { description = it },
          label = { Text("Describe what happened") },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("complaint_description_input"),
        )
        if (errorText != null) {
          Text(
            text = errorText!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (description.trim().length < 5) {
            errorText = "Please enter at least 5 characters."
          } else {
            onSubmit(reason, description)
          }
        },
        modifier = Modifier.testTag("submit_complaint_button"),
      ) {
        Text("Submit Report")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}
