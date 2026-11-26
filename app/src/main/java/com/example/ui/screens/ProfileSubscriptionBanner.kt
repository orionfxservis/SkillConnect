package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ProviderSubscriptionStatus
import com.example.model.SubscriptionFeatureFlags
import com.example.model.SubscriptionTier
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.DispatchAmber
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold

@Composable
fun ProfileSubscriptionBannerSection(
  subscriptionStatus: ProviderSubscriptionStatus,
  onOpenSubscription: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED && subscriptionStatus.tier == SubscriptionTier.PRO) {
    ProPlanMembershipBannerCard(
      status = subscriptionStatus,
      onOpenSubscription = onOpenSubscription,
      modifier = modifier,
    )
  } else {
    FreePlanPromoBannerCard(
      status = subscriptionStatus,
      onOpenSubscription = onOpenSubscription,
      modifier = modifier,
    )
  }
}

@Composable
private fun FreePlanPromoBannerCard(
  status: ProviderSubscriptionStatus,
  onOpenSubscription: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val limit = status.tier.monthlyConfirmedLimit
  val used = status.confirmedThisMonth.coerceAtLeast(0)
  val limitReached = limit != null && used >= limit

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (limitReached) MaterialTheme.colorScheme.error.copy(alpha = 0.45f)
          else MaterialTheme.colorScheme.outlineVariant,
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth().testTag("profile_free_plan_banner"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Outlined.CardGiftcard,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Welcome to SkillConnect!",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(50),
          color = EmeraldContainer,
        ) {
          Text(
            text = "FREE PLAN",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OnEmeraldContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = "Your Free Account is Active",
        style = MaterialTheme.typography.titleMedium,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      if (limit == null) {
        Text(
          text =
            buildAnnotatedString {
              append("Enjoy ")
              withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                append("unlimited service bookings")
              }
              append(" with your Free Plan. All bookings are automatically recorded.")
            },
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface,
        )

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = EmeraldContainer.copy(alpha = 0.72f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(
                imageVector = Icons.Rounded.AllInclusive,
                contentDescription = null,
                tint = OnEmeraldContainer,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text =
                  "$used confirmed this month • ${status.totalHistoryCount} total recorded",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnEmeraldContainer,
                modifier = Modifier.testTag("free_plan_usage_text"),
              )
            }
            Text(
              text = "Unlimited",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.testTag("free_plan_remaining_text"),
            )
          }
        }
      } else {
        val remaining = (limit - used).coerceAtLeast(0)
        val progress = (used.toFloat() / limit.toFloat()).coerceIn(0f, 1f)

        if (limitReached) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "You've reached your monthly Free Plan limit. Upgrade to Pro for unlimited bookings.",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Medium,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            )
          }
        } else {
          Text(
            text =
              buildAnnotatedString {
                append("Enjoy up to ")
                withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                  append("$limit confirmed service bookings per month")
                }
                append(" with your Free Plan.")
              },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        LinearProgressIndicator(
          progress = { progress },
          color =
            if (limitReached) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.primary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant,
          strokeCap = StrokeCap.Round,
          modifier =
            Modifier.fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(50))
              .testTag("free_plan_usage_progress"),
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = "$used of $limit bookings used",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("free_plan_usage_text"),
          )
          Text(
            text = "$remaining bookings remaining",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color =
              if (limitReached) MaterialTheme.colorScheme.error
              else MaterialTheme.colorScheme.primary,
            modifier = Modifier.testTag("free_plan_remaining_text"),
          )
        }
      }

      if (SubscriptionFeatureFlags.PRO_PLAN_ENABLED) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = DispatchAmber,
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "Want unlimited bookings?",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Text(
          text = "Upgrade to SkillConnect Pro and receive unlimited service bookings.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Button(
          onClick = onOpenSubscription,
          shape = RoundedCornerShape(50),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier =
            Modifier.fillMaxWidth()
              .height(48.dp)
              .testTag("profile_upgrade_pro_button"),
        ) {
          Text(
            text = "Upgrade to Pro – Rs. 499/month",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
          )
        }

        Text(
          text = "Upgrade anytime. Your Free Plan remains available until then.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth(),
        )
      }
    }
  }
}

@Composable
private fun ProPlanMembershipBannerCard(
  status: ProviderSubscriptionStatus,
  onOpenSubscription: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.5.dp, StarGold),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth().testTag("profile_pro_plan_banner"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Icon(
            imageVector = Icons.Rounded.WorkspacePremium,
            contentDescription = null,
            tint = DispatchAmber,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "SkillConnect Pro Membership",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
          )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Surface(
          shape = RoundedCornerShape(50),
          color = AmberContainer,
        ) {
          Text(
            text = "PRO PLAN",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = OnAmberContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }

      Text(
        text = "Your Pro Account is Active",
        style = MaterialTheme.typography.titleMedium,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Text(
        text =
          buildAnnotatedString {
            append("Enjoy ")
            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
              append("unlimited confirmed service bookings")
            }
            append(" every month with full booking history and priority visibility.")
          },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = EmeraldContainer.copy(alpha = 0.72f),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.AllInclusive,
              contentDescription = null,
              tint = OnEmeraldContainer,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${status.confirmedThisMonth} confirmed this month • Unlimited",
              style = MaterialTheme.typography.labelMedium,
              fontWeight = FontWeight.SemiBold,
              color = OnEmeraldContainer,
            )
          }
          Icon(
            imageVector = Icons.Rounded.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp),
          )
        }
      }

      if (!status.proRenewalDateLabel.isNullOrBlank()) {
        Text(
          text = "Subscription fee: Rs. 499/month • Next renewal: ${status.proRenewalDateLabel}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      OutlinedButton(
        onClick = onOpenSubscription,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        modifier =
          Modifier.fillMaxWidth()
            .height(48.dp)
            .testTag("profile_manage_pro_button"),
      ) {
        Text(
          text = "Manage Pro Subscription",
          style = MaterialTheme.typography.labelLarge,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = null,
          modifier = Modifier.size(18.dp),
        )
      }
    }
  }
}
