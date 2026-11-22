package com.example.ui.screens

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.HourglassTop
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Coordinates
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceCategory
import com.example.model.TechnicianProfile
import com.example.model.TechnicianRegistrationInput
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold

@Composable
fun ProHubScreen(
  isSignedIn: Boolean,
  myTechnicianState: UiState<TechnicianProfile?>,
  myVerificationDocState: UiState<ProviderVerificationDoc?>,
  categories: List<ServiceCategory>,
  userProfile: UserProfile?,
  userCoordinates: Coordinates?,
  isLiveBroadcastActive: Boolean,
  isBusy: Boolean,
  phoneOtpVerificationId: String?,
  phoneOtpStatusMessage: String?,
  phoneVerifiedSession: Boolean,
  onSignInToEnroll: () -> Unit,
  onRequestLocation: () -> Unit,
  onToggleLiveGpsBroadcast: () -> Unit,
  onToggleAvailability: (Boolean, Double, Double) -> Unit,
  onStartPhoneOtp: (phone: String, activity: Activity) -> Unit,
  onConfirmPhoneOtp: (smsCode: String) -> Unit,
  onClearPhoneOtpState: () -> Unit,
  onSaveEnrollment: (TechnicianRegistrationInput) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier.fillMaxSize()) {
    if (!isSignedIn) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .widthIn(max = 640.dp)
            .align(Alignment.Center)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
      ) {
        Box(
          modifier =
            Modifier.size(72.dp)
              .clip(CircleShape)
              .background(EmeraldContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            imageVector = Icons.Rounded.Verified,
            contentDescription = null,
            tint = OnEmeraldContainer,
            modifier = Modifier.size(38.dp),
          )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
          text = "Technical Person Registration",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text =
            "Normal customers browse without signing in. Sign in with Google once to complete your 5-step technical registration, upload CNIC & certificates, share live GPS, and receive client bookings.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
          onClick = onSignInToEnroll,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(54.dp).testTag("pro_hub_google_sign_in_button"),
        ) {
          Text("Sign in with Google & Register")
        }
      }
      return@Box
    }

    when (myTechnicianState) {
      is UiState.Loading -> {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          CircularProgressIndicator()
        }
      }
      is UiState.Error -> {
        ErrorCard(
          message = myTechnicianState.message,
          modifier = Modifier.padding(16.dp),
        )
      }
      is UiState.Success -> {
        val existingTech = myTechnicianState.data
        val existingVerify = (myVerificationDocState as? UiState.Success)?.data

        Column(
          modifier =
            Modifier.fillMaxSize()
              .widthIn(max = 720.dp)
              .align(Alignment.TopCenter)
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
          Text(
            text = if (existingTech == null) "Enrol as Technical Person" else "Service Provider Hub",
            style = MaterialTheme.typography.headlineMedium,
          )
          Text(
            text =
              "Free 5-step registration — showcase your skills, rates & live GPS to get discovered directly by nearby customers.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          if (existingTech != null) {
            // 4-Stage Post-Submission Verification & Publishing Workflow Tracker
            PostSubmissionWorkflowTracker(tech = existingTech)

            // Live Dispatch Status + Reputation Card
            Card(
              shape = RoundedCornerShape(20.dp),
              colors =
                CardDefaults.cardColors(
                  containerColor =
                    if (existingTech.availableNow) EmeraldContainer.copy(alpha = 0.65f)
                    else MaterialTheme.colorScheme.surface
                ),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
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
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text =
                        if (existingTech.availableNow) "Availability: ON (Available Now)"
                        else "Availability: OFF (Offline)",
                      style = MaterialTheme.typography.titleMedium,
                      color =
                        if (existingTech.availableNow) OnEmeraldContainer
                        else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                      text = "Toggle ON to appear as Available Now in nearby customer searches",
                      style = MaterialTheme.typography.bodySmall,
                    )
                  }
                  Switch(
                    checked = existingTech.availableNow,
                    onCheckedChange = { next ->
                      val useLat = userCoordinates?.latitude ?: existingTech.latitude
                      val useLng = userCoordinates?.longitude ?: existingTech.longitude
                      onToggleAvailability(next, useLat, useLng)
                    },
                    modifier = Modifier.testTag("pro_available_switch"),
                  )
                }

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                  ) {
                    Row(
                      modifier = Modifier.padding(10.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Icon(
                        Icons.Rounded.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(18.dp),
                      )
                      Spacer(modifier = Modifier.width(6.dp))
                      Text(
                        text =
                          if (existingTech.reviewCount > 0)
                            "${existingTech.averageRating} (${existingTech.reviewCount})"
                          else "No reviews yet",
                        style = MaterialTheme.typography.labelLarge,
                      )
                    }
                  }
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f),
                  ) {
                    Text(
                      text = "${existingTech.completedJobs} Jobs Done",
                      style = MaterialTheme.typography.labelLarge,
                      modifier = Modifier.padding(10.dp),
                    )
                  }
                }

                OutlinedButton(
                  onClick = {
                    onRequestLocation()
                    onToggleLiveGpsBroadcast()
                  },
                  shape = RoundedCornerShape(12.dp),
                  modifier = Modifier.fillMaxWidth().testTag("pro_live_gps_broadcast_button"),
                ) {
                  Icon(
                    if (isLiveBroadcastActive) Icons.Rounded.GpsFixed else Icons.Rounded.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                  )
                  Spacer(modifier = Modifier.width(8.dp))
                  Text(
                    if (isLiveBroadcastActive) "Live GPS Broadcasting Active (Tap to Pause)"
                    else "Start Live GPS Location Sharing"
                  )
                }
              }
            }
          }

          ProRegistrationWizard(
            existingTech = existingTech,
            existingVerify = existingVerify,
            categories = categories,
            userProfile = userProfile,
            userCoordinates = userCoordinates,
            isLiveBroadcastActive = isLiveBroadcastActive,
            isBusy = isBusy,
            phoneOtpVerificationId = phoneOtpVerificationId,
            phoneOtpStatusMessage = phoneOtpStatusMessage,
            phoneVerifiedSession = phoneVerifiedSession,
            onRequestLocation = onRequestLocation,
            onStartPhoneOtp = onStartPhoneOtp,
            onConfirmPhoneOtp = onConfirmPhoneOtp,
            onClearPhoneOtpState = onClearPhoneOtpState,
            onSaveEnrollment = onSaveEnrollment,
          )

          Spacer(modifier = Modifier.height(16.dp))
        }
      }
    }
  }
}

@Composable
private fun PostSubmissionWorkflowTracker(tech: TechnicianProfile) {
  val status = tech.verificationEnum
  val (bannerBg, bannerFg, bannerIcon, bannerTitle, bannerSubtitle) =
    when (status) {
      VerificationStatus.APPROVED ->
        WorkflowBannerStyle(
          EmeraldContainer,
          OnEmeraldContainer,
          if (tech.verifiedBadge) Icons.Rounded.Verified else Icons.Rounded.CheckCircle,
          if (tech.verifiedBadge) "Approved & Verified Professional" else "Approved Service Provider",
          tech.adminRemarks.ifBlank { "Your profile is approved and live in the directory." },
        )
      VerificationStatus.REJECTED ->
        WorkflowBannerStyle(
          MaterialTheme.colorScheme.errorContainer,
          MaterialTheme.colorScheme.onErrorContainer,
          Icons.Rounded.WarningAmber,
          "Verification Needs Attention",
          tech.adminRemarks.ifBlank { "Super Admin flagged this profile. Update details and resubmit." },
        )
      VerificationStatus.PENDING ->
        WorkflowBannerStyle(
          AmberContainer,
          OnAmberContainer,
          Icons.Rounded.HourglassTop,
          "Verification Pending Review",
          "Super Admin is checking your identity, CNIC & professional info.",
        )
    }

  val isApproved = status == VerificationStatus.APPROVED
  val isRejected = status == VerificationStatus.REJECTED
  val isPublished = tech.listed && !isRejected

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("post_submission_workflow_card"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(color = bannerBg, shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(bannerIcon, contentDescription = null, tint = bannerFg, modifier = Modifier.size(22.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(text = bannerTitle, style = MaterialTheme.typography.titleSmall, color = bannerFg)
            Text(
              text = bannerSubtitle,
              style = MaterialTheme.typography.bodySmall,
              color = bannerFg.copy(alpha = 0.9f),
            )
          }
        }
      }

      Text(
        text = "Enrolment Progress",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )

      WorkflowStepRow(
        step = "1. Registration Submitted",
        detail = "Personal info, skills, location & documents saved",
        done = true,
        active = false,
        error = false,
      )
      WorkflowStepRow(
        step = "2. Verification Pending",
        detail = "Admin checks identity, CNIC & trade details",
        done = isApproved,
        active = status == VerificationStatus.PENDING,
        error = isRejected,
      )
      WorkflowStepRow(
        step = "3. Approved",
        detail = if (isRejected) "Update details & resubmit for approval" else "Verified badge & approval granted by Super Admin",
        done = isApproved,
        active = false,
        error = isRejected,
      )
      WorkflowStepRow(
        step = "4. Profile Published",
        detail =
          when {
            !isPublished -> "Hidden until listed & approved"
            tech.availableNow -> "Live & searchable right now as Available Now"
            else -> "Published in directory (switch Availability ON when ready)"
          },
        done = isPublished && isApproved,
        active = isPublished && !isApproved,
        error = isRejected,
      )
    }
  }
}

@Composable
private fun WorkflowStepRow(
  step: String,
  detail: String,
  done: Boolean,
  active: Boolean,
  error: Boolean,
) {
  val icon =
    when {
      error -> Icons.Rounded.WarningAmber
      done -> Icons.Rounded.CheckCircle
      active -> Icons.Rounded.HourglassTop
      else -> Icons.Rounded.RadioButtonUnchecked
    }
  val tint =
    when {
      error -> MaterialTheme.colorScheme.error
      done -> EmeraldLive
      active -> OnAmberContainer
      else -> Color.Gray
    }
  Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = tint,
      modifier = Modifier.size(18.dp).padding(top = 2.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = step,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = if (active || done) FontWeight.SemiBold else FontWeight.Normal,
      )
      Text(
        text = detail,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

private data class WorkflowBannerStyle<A, B, C, D, E>(
  val a: A,
  val b: B,
  val c: C,
  val d: D,
  val e: E,
)
