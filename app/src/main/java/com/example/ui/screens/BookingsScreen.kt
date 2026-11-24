package com.example.ui.screens

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Chat
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.BookingStatus
import com.example.model.ProviderSubscriptionStatus
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.SubscriptionTier
import com.example.model.UiState
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer

@Composable
fun BookingsScreen(
  bookingsState: UiState<List<ServiceBooking>>,
  currentUserId: String?,
  subscriptionStatus: ProviderSubscriptionStatus = ProviderSubscriptionStatus(),
  onSignInClick: () -> Unit,
  onOpenBooking: (String) -> Unit,
  onOpenSubscription: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var selectedTab by remember { mutableIntStateOf(0) }

  Box(modifier = modifier.fillMaxSize()) {
    if (currentUserId == null) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .widthIn(max = 640.dp)
            .align(Alignment.Center)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.CalendarToday,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "Bookings & Live Dispatch",
          style = MaterialTheme.typography.headlineSmall,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text =
            "You can browse and call technicians without registering. Sign in with Google when you want to track in-app bookings, message pros live, or submit verified reviews.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(18.dp))
        androidx.compose.material3.Button(
          onClick = onSignInClick,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("bookings_google_sign_in_button"),
        ) {
          Text("Sign in with Google to Sync Bookings")
        }
      }
      return@Box
    }

    Column(
      modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter)
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = "Bookings & Dispatch",
          style = MaterialTheme.typography.headlineMedium,
        )
        Text(
          text = "Track live arrivals, chat privately, and review completed jobs",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(12.dp))

        val incomingCount =
          (bookingsState as? UiState.Success)?.data?.count {
            it.technicianId == currentUserId &&
              it.bookingStatus in
                setOf(
                  BookingStatus.REQUESTED,
                  BookingStatus.ACCEPTED,
                  BookingStatus.EN_ROUTE,
                  BookingStatus.IN_PROGRESS,
                )
          } ?: 0

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
          SegmentedButton(
            selected = selectedTab == 0,
            onClick = { selectedTab = 0 },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            modifier = Modifier.testTag("tab_client_requests"),
          ) {
            Text("My Requests")
          }
          SegmentedButton(
            selected = selectedTab == 1,
            onClick = { selectedTab = 1 },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            modifier = Modifier.testTag("tab_incoming_jobs"),
          ) {
            Text(if (incomingCount > 0) "Incoming Jobs ($incomingCount)" else "Incoming Jobs")
          }
        }

        if (selectedTab == 1) {
          Spacer(modifier = Modifier.height(10.dp))
          val isPro = subscriptionStatus.tier == SubscriptionTier.PRO
          val limitReached = !subscriptionStatus.canConfirmAnotherBooking
          Surface(
            onClick = onOpenSubscription,
            shape = RoundedCornerShape(12.dp),
            color =
              when {
                isPro -> EmeraldContainer.copy(alpha = 0.7f)
                limitReached -> AmberContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
              },
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth().testTag("bookings_subscription_banner"),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text =
                    if (isPro) "Pro Plan • Unlimited bookings active"
                    else if (limitReached) "Free Plan Limit Reached (5/5 confirmed)"
                    else "Free Plan • ${subscriptionStatus.confirmedThisMonth}/5 confirmed • ${subscriptionStatus.requestsThisMonth}/5 requests",
                  style = MaterialTheme.typography.labelMedium,
                  color =
                    when {
                      isPro -> OnEmeraldContainer
                      limitReached -> OnAmberContainer
                      else -> MaterialTheme.colorScheme.onSurface
                    },
                )
                Text(
                  text =
                    if (isPro) "Booking history available • Subscription renewal active"
                    else "Automatic reset on ${subscriptionStatus.nextResetDateLabel} • Tap for Pro (Rs. 499/mo)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = if (isPro) "Plans" else "Upgrade",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }

      when (bookingsState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is UiState.Error -> {
          ErrorCard(
            message = bookingsState.message,
            modifier = Modifier.padding(16.dp),
          )
        }
        is UiState.Success -> {
          val filtered =
            bookingsState.data.filter { booking ->
              if (selectedTab == 0) booking.clientId == currentUserId
              else booking.technicianId == currentUserId
            }

          if (filtered.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize().padding(24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Rounded.CalendarToday,
                  contentDescription = null,
                  tint = MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(44.dp),
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                  text =
                    if (selectedTab == 0) "No service requests yet"
                    else "No incoming technician jobs yet",
                  style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text =
                    if (selectedTab == 0)
                      "Browse nearby categories in Discover to book a technician directly."
                    else "Keep your Pro Hub profile online so nearby clients can hire you.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
            }
          } else {
            LazyColumn(
              contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              items(filtered, key = { it.id }) { booking ->
                BookingSummaryCard(
                  booking = booking,
                  isClientView = selectedTab == 0,
                  onClick = { onOpenBooking(booking.id) },
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun BookingSummaryCard(
  booking: ServiceBooking,
  isClientView: Boolean,
  onClick: () -> Unit,
) {
  val category = ServiceCategories.byId(booking.categoryId)
  val status = booking.bookingStatus
  val (pillBg, pillFg) = statusColors(status)

  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("booking_card_${booking.id}"),
  ) {
    Column(modifier = Modifier.padding(16.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier =
              Modifier.size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(category.tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = category.icon,
              contentDescription = category.name,
              tint = category.tint,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = booking.serviceTitle,
              style = MaterialTheme.typography.titleMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                if (isClientView) "Pro: ${booking.technicianName}"
                else "Client: ${booking.clientName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Surface(shape = RoundedCornerShape(50), color = pillBg) {
          Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = pillFg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Rounded.LocationOn,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "${booking.serviceArea} • ${booking.preferredTime}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.AutoMirrored.Rounded.Chat,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "Open Live Tracker & Chat",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        if (isClientView && status == BookingStatus.COMPLETED && !booking.reviewed) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = AmberContainer,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                Icons.Rounded.RateReview,
                contentDescription = "Leave Review",
                tint = OnAmberContainer,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = "Rate Pro",
                style = MaterialTheme.typography.labelSmall,
                color = OnAmberContainer,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun statusColors(status: BookingStatus): Pair<Color, Color> =
  when (status) {
    BookingStatus.REQUESTED -> AmberContainer to OnAmberContainer
    BookingStatus.ACCEPTED ->
      MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    BookingStatus.EN_ROUTE -> EmeraldContainer to OnEmeraldContainer
    BookingStatus.IN_PROGRESS -> EmeraldLive.copy(alpha = 0.22f) to OnEmeraldContainer
    BookingStatus.COMPLETED ->
      MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
    BookingStatus.CANCELLED ->
      MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
  }
