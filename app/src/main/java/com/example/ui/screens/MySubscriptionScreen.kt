package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.model.ProviderSubscriptionStatus
import com.example.model.SubscriptionFeatureFlags
import com.example.model.SubscriptionTier
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MySubscriptionScreen(
  isSignedIn: Boolean,
  status: ProviderSubscriptionStatus,
  onBack: () -> Unit,
  onOpenBookings: () -> Unit,
  onUpgradeToPro: () -> Unit,
  onRenewPro: () -> Unit,
  onSwitchToFree: () -> Unit,
  onSignInPrompt: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var pendingConfirmAction by remember { mutableStateOf<SubscriptionConfirmAction?>(null) }

  Scaffold(
    modifier = modifier.fillMaxSize(),
    topBar = {
      TopAppBar(
        title = { Text("My Subscription") },
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("subscription_back_button"),
          ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
        },
      )
    },
  ) { innerPadding ->
    LazyColumn(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      item {
        Column(
          modifier = Modifier.widthIn(max = 680.dp).fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          CurrentPlanOverviewCard(
            isSignedIn = isSignedIn,
            status = status,
            onOpenBookings = onOpenBookings,
          )

          Text(
            text = "SkillConnect – Service Booking Plans",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )

          val freeUnlimited = SubscriptionTier.FREE.monthlyConfirmedLimit == null
          ServicePlanCard(
            planName = "Free",
            badgeLabel =
              if (isSignedIn && status.tier == SubscriptionTier.FREE) "Get Started • Active"
              else "Get Started",
            priceAmount = "Rs. 0",
            priceSuffix = "/ month",
            subtitle =
              if (freeUnlimited) "Receive and record unlimited service bookings without a subscription fee."
              else "Start receiving service bookings without a subscription fee.",
            includedHeading = "What's included",
            includedItems =
              listOf(
                buildAnnotatedString {
                  if (freeUnlimited) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                      append("Unlimited confirmed service bookings")
                    }
                  } else {
                    append("Up to ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                      append("5 confirmed bookings per month")
                    }
                  }
                },
                AnnotatedString("Full booking history & automatic recording"),
                AnnotatedString("Professional profile listing"),
                AnnotatedString("Receive customer booking requests"),
                AnnotatedString("Accept or reject bookings"),
                AnnotatedString("Customer contact details"),
              ),
            footerPillText =
              if (freeUnlimited) "Unlimited free bookings active"
              else "5 bookings reset every month",
            isActive = isSignedIn && status.tier == SubscriptionTier.FREE,
            isProHighlight = false,
            buttonLabel =
              if (isSignedIn && status.tier == SubscriptionTier.FREE) "Active Free Plan"
              else "Start Free",
            buttonEnabled = !isSignedIn || status.tier != SubscriptionTier.FREE,
            onActionClick = {
              if (!isSignedIn) {
                onSignInPrompt()
              } else {
                pendingConfirmAction = SubscriptionConfirmAction.SWITCH_FREE
              }
            },
            testTag = "plan_free_action_button",
          )

          if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED) {
            ServicePlanCard(
              planName = "SkillConnect Pro",
              badgeLabel =
                if (isSignedIn && status.tier == SubscriptionTier.PRO) "Most Popular • Active"
                else "Most Popular",
              priceAmount = "Rs. 499",
              priceSuffix = "/ month",
              subtitle = "Grow your service business with unlimited bookings.",
              includedHeading = "Everything in Free, plus",
              includedItems =
                listOf(
                  buildAnnotatedString {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                      append("Unlimited service bookings")
                    }
                  },
                  AnnotatedString("Receive unlimited booking requests"),
                  AnnotatedString("Accept or reject bookings"),
                  AnnotatedString("Manage all customer bookings"),
                  AnnotatedString("Full booking history"),
                  AnnotatedString("No monthly booking limit"),
                ),
              footerPillText = "Unlimited bookings while subscription is active",
              isActive = isSignedIn && status.tier == SubscriptionTier.PRO,
              isProHighlight = true,
              buttonLabel =
                if (isSignedIn && status.tier == SubscriptionTier.PRO) "Renew Pro"
                else "Upgrade to Pro",
              buttonEnabled = true,
              onActionClick = {
                if (!isSignedIn) {
                  onSignInPrompt()
                } else if (status.tier == SubscriptionTier.PRO) {
                  pendingConfirmAction = SubscriptionConfirmAction.RENEW_PRO
                } else {
                  pendingConfirmAction = SubscriptionConfirmAction.UPGRADE_PRO
                }
              },
              testTag = "plan_pro_action_button",
            )
          }
        }
      }
    }
  }

  pendingConfirmAction?.let { action ->
    val title =
      when (action) {
        SubscriptionConfirmAction.UPGRADE_PRO -> "Activate Pro Plan?"
        SubscriptionConfirmAction.RENEW_PRO -> "Renew Pro Subscription?"
        SubscriptionConfirmAction.SWITCH_FREE -> "Switch to Free Plan?"
      }
    val body =
      when (action) {
        SubscriptionConfirmAction.UPGRADE_PRO ->
          "Switch to the Service Provider Pro Plan (Rs. 499/month) to unlock unlimited monthly confirmed bookings, unlimited booking requests, and full booking history."
        SubscriptionConfirmAction.RENEW_PRO ->
          "Extend your Pro Plan subscription (Rs. 499/month) for another 30-day cycle with unlimited monthly confirmed bookings and requests."
        SubscriptionConfirmAction.SWITCH_FREE ->
          "Switch back to the Free Plan (5 monthly confirmed bookings and up to 5 booking requests, automatically reset every month). Your booking history remains available."
      }
    val confirmLabel =
      when (action) {
        SubscriptionConfirmAction.UPGRADE_PRO -> "Activate Pro"
        SubscriptionConfirmAction.RENEW_PRO -> "Renew Pro"
        SubscriptionConfirmAction.SWITCH_FREE -> "Switch to Free"
      }

    AlertDialog(
      onDismissRequest = { pendingConfirmAction = null },
      title = { Text(title) },
      text = { Text(body, style = MaterialTheme.typography.bodyMedium) },
      confirmButton = {
        Button(
          onClick = {
            when (action) {
              SubscriptionConfirmAction.UPGRADE_PRO -> onUpgradeToPro()
              SubscriptionConfirmAction.RENEW_PRO -> onRenewPro()
              SubscriptionConfirmAction.SWITCH_FREE -> onSwitchToFree()
            }
            pendingConfirmAction = null
          },
          modifier = Modifier.testTag("confirm_subscription_action_button"),
        ) {
          Text(confirmLabel)
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingConfirmAction = null }) {
          Text("Cancel")
        }
      },
    )
  }
}

private enum class SubscriptionConfirmAction {
  UPGRADE_PRO,
  RENEW_PRO,
  SWITCH_FREE,
}

@Composable
private fun CurrentPlanOverviewCard(
  isSignedIn: Boolean,
  status: ProviderSubscriptionStatus,
  onOpenBookings: () -> Unit,
) {
  val isPro = status.tier == SubscriptionTier.PRO
  Card(
    shape = RoundedCornerShape(20.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = if (isPro) EmeraldContainer.copy(alpha = 0.72f) else MaterialTheme.colorScheme.surface,
      ),
    border =
      BorderStroke(
        width = 1.dp,
        color = if (isPro) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
      ),
    modifier = Modifier.fillMaxWidth().testTag("subscription_overview_card"),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier =
              Modifier.size(44.dp)
                .clip(CircleShape)
                .background(
                  if (isPro) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.primaryContainer,
                ),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = if (isPro) Icons.Rounded.WorkspacePremium else Icons.Rounded.Refresh,
              contentDescription = null,
              tint = if (isPro) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
              modifier = Modifier.size(22.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "Service Booking Plans",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                if (status.currentMonthLabel.isNotBlank()) "Booking record • ${status.currentMonthLabel}"
                else "Monthly booking activity",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Surface(
          shape = RoundedCornerShape(50),
          color = if (isPro) MaterialTheme.colorScheme.primary else EmeraldContainer,
        ) {
          Text(
            text = "${status.tier.title} Plan",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isPro) MaterialTheme.colorScheme.onPrimary else OnEmeraldContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
          )
        }
      }

      if (isSignedIn) {
        QuotaProgressRow(
          label = "Monthly confirmed bookings",
          used = status.confirmedThisMonth,
          limit = status.tier.monthlyConfirmedLimit,
        )
        QuotaProgressRow(
          label = "Booking requests",
          used = status.requestsThisMonth,
          limit = status.tier.monthlyRequestLimit,
        )

        if (!status.canConfirmAnotherBooking || !status.canReceiveMoreRequests) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = AmberContainer,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text =
                "Monthly Free limit reached (5 bookings). Upgrade to Pro (Rs. 499/month) for unlimited bookings or wait until ${status.nextResetDateLabel} for automatic reset.",
              style = MaterialTheme.typography.bodySmall,
              color = OnAmberContainer,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text =
              when {
                isPro -> "Subscription renewal: ${status.proRenewalDateLabel ?: status.nextResetDateLabel}"
                status.tier.monthlyConfirmedLimit == null -> "Plan status: Free & Unlimited (all bookings recorded)"
                else -> "Monthly renewal: Automatic reset (${status.nextResetDateLabel})"
              },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
          )
          Text(
            text = "Booking history: Available (${status.totalHistoryCount} total)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        TextButton(
          onClick = onOpenBookings,
          modifier = Modifier.testTag("subscription_view_history_button"),
        ) {
          Icon(Icons.Rounded.History, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("History")
        }
      }
    }
  }
}

@Composable
private fun QuotaProgressRow(
  label: String,
  used: Int,
  limit: Int?,
) {
  Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(text = label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
      Text(
        text = if (limit == null) "$used / Unlimited" else "$used / $limit",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color =
          if (limit != null && used >= limit) MaterialTheme.colorScheme.error
          else MaterialTheme.colorScheme.primary,
      )
    }
    if (limit != null) {
      LinearProgressIndicator(
        progress = { (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f) },
        modifier = Modifier.fillMaxWidth().height(7.dp).clip(RoundedCornerShape(50)),
        color =
          if (used >= limit) MaterialTheme.colorScheme.error
          else MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
      )
    } else {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(
          Icons.Rounded.AllInclusive,
          contentDescription = null,
          tint = OnEmeraldContainer,
          modifier = Modifier.size(16.dp),
        )
        Text(
          text = "Unlimited • Recorded automatically",
          style = MaterialTheme.typography.labelSmall,
          color = OnEmeraldContainer,
        )
      }
    }
  }
}

@Composable
private fun ServicePlanCard(
  planName: String,
  badgeLabel: String,
  priceAmount: String,
  priceSuffix: String,
  subtitle: String,
  includedHeading: String,
  includedItems: List<AnnotatedString>,
  footerPillText: String,
  isActive: Boolean,
  isProHighlight: Boolean,
  buttonLabel: String,
  buttonEnabled: Boolean,
  onActionClick: () -> Unit,
  testTag: String,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border =
      BorderStroke(
        width = if (isActive || isProHighlight) 1.5.dp else 1.dp,
        color =
          when {
            isProHighlight -> StarGold
            isActive -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outlineVariant
          },
      ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = planName,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        Surface(
          shape = RoundedCornerShape(50),
          color = if (isProHighlight) AmberContainer else EmeraldContainer,
        ) {
          Text(
            text = badgeLabel,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = if (isProHighlight) OnAmberContainer else OnEmeraldContainer,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
          )
        }
      }

      Row(verticalAlignment = Alignment.Bottom) {
        Text(
          text = priceAmount,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color =
            if (isProHighlight) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.primary,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = priceSuffix,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(bottom = 3.dp),
        )
      }

      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

      Text(
        text = includedHeading,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold,
      )

      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        includedItems.forEach { itemText ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector = Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(18.dp),
            )
            Text(
              text = itemText,
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

      Surface(
        shape = RoundedCornerShape(50),
        color =
          if (isProHighlight) EmeraldContainer
          else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
      ) {
        Text(
          text = footerPillText,
          style = MaterialTheme.typography.labelMedium,
          fontWeight = FontWeight.Medium,
          color =
            if (isProHighlight) OnEmeraldContainer
            else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
        )
      }

      if (isProHighlight) {
        Button(
          onClick = onActionClick,
          enabled = buttonEnabled,
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag(testTag),
        ) {
          Text(buttonLabel, fontWeight = FontWeight.SemiBold)
        }
      } else {
        OutlinedButton(
          onClick = onActionClick,
          enabled = buttonEnabled,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag(testTag),
        ) {
          Text(buttonLabel, fontWeight = FontWeight.SemiBold)
        }
      }
    }
  }
}
