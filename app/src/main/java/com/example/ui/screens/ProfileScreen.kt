package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PersonOutline
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.model.ServiceCategory
import com.example.model.ServiceComplaint
import com.example.model.TechnicianProfile
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import com.example.ui.TechnicianWithDistance
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ProfileScreen(
  isSignedIn: Boolean,
  guestArea: String,
  userProfileState: UiState<UserProfile?>,
  myTechnicianProfile: TechnicianProfile?,
  savedTechnicians: List<TechnicianWithDistance>,
  categories: List<ServiceCategory>,
  myComplaintsState: UiState<List<ServiceComplaint>>,
  isAdmin: Boolean,
  isBusy: Boolean,
  onUpdateGuestArea: (String) -> Unit,
  onRequestLocation: () -> Unit,
  onSignInWithGoogle: () -> Unit,
  onEnrollTechnicianClick: () -> Unit,
  onSaveProfile: (displayName: String, phoneNumber: String, defaultArea: String) -> Unit,
  onOpenBookings: () -> Unit,
  onOpenTechnician: (String) -> Unit,
  onToggleSavedTechnician: (String) -> Unit,
  onOpenProHub: () -> Unit,
  onOpenSuperAdmin: () -> Unit,
  onSignOut: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showSavedSheet by remember { mutableStateOf(false) }
  var showSettingsSheet by remember { mutableStateOf(false) }
  var showHelpSheet by remember { mutableStateOf(false) }
  val scope = rememberCoroutineScope()
  val authCardBringIntoView = remember { BringIntoViewRequester() }
  val profile = (userProfileState as? UiState.Success)?.data

  Box(modifier = modifier.fillMaxSize()) {
    if (isSignedIn && userProfileState is UiState.Loading) {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
      }
      return@Box
    }

    if (isSignedIn && userProfileState is UiState.Error) {
      ErrorCard(message = userProfileState.message, modifier = Modifier.padding(16.dp))
      return@Box
    }

    val isEnrolledProvider = myTechnicianProfile != null || profile?.enrolledTechnician == true
    val roleLabel =
      when {
        !isSignedIn -> "Customer"
        isAdmin -> "Super Admin"
        isEnrolledProvider -> "Service Provider"
        else -> "Customer"
      }

    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 680.dp)
          .align(Alignment.TopCenter)
          .imePadding()
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 16.dp, vertical = 10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "Profile & Account",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )

      if (isSignedIn && profile?.suspended == true) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = "Your account has been flagged/suspended by Super Admin. Contact support.",
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
          )
        }
      }

      IdentitySummaryCard(isSignedIn = isSignedIn, isAdmin = isAdmin, roleLabel = roleLabel, profile = profile)

      Text(
        text = "Account Settings",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          AccountMenuRow(
            icon = Icons.Rounded.PersonOutline,
            title = "My Profile",
            subtitle =
              if (!isSignedIn) "Sign in to save name, phone & neighborhood"
              else "Update multi-step profile, photo, CNIC & skills",
            onClick = {
              if (!isSignedIn) {
                scope.launch { authCardBringIntoView.bringIntoView() }
              } else if (isEnrolledProvider) {
                onOpenProHub()
              } else {
                onEnrollTechnicianClick()
              }
            },
            modifier = Modifier.testTag("menu_my_profile"),
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          AccountMenuRow(
            icon = Icons.Rounded.CalendarMonth,
            title = "My Bookings",
            subtitle = "Track requested, active & completed service jobs",
            onClick = onOpenBookings,
            modifier = Modifier.testTag("menu_my_bookings"),
          )

          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          AccountMenuRow(
            icon = Icons.Rounded.Bookmark,
            title = "Saved Professionals",
            subtitle =
              if (!isSignedIn) "Sign in to bookmark reliable technicians"
              else "${savedTechnicians.size} saved technician(s)",
            onClick = { showSavedSheet = true },
            modifier = Modifier.testTag("menu_saved_professionals"),
          )
        }
      }

      if (!isSignedIn) {
        Box(modifier = Modifier.bringIntoViewRequester(authCardBringIntoView)) {
          GuestAuthStepperCard(categories = categories)
        }
      }

      ProviderEnrolBannerCard(
        isEnrolledProvider = isEnrolledProvider,
        myTechnicianProfile = myTechnicianProfile,
        onClick = {
          when {
            !isSignedIn -> scope.launch { authCardBringIntoView.bringIntoView() }
            isEnrolledProvider -> onOpenProHub()
            else -> onEnrollTechnicianClick()
          }
        },
      )

      if (isSignedIn && isAdmin) {
        Button(
          onClick = onOpenSuperAdmin,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("open_super_admin_button"),
        ) {
          Icon(Icons.Rounded.AdminPanelSettings, contentDescription = null, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Open Super Admin Console")
        }
      }

      Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          AccountMenuRow(
            icon = Icons.Rounded.Settings,
            title = "Settings",
            subtitle = "Preferred area (${if (isSignedIn) profile?.defaultArea?.ifBlank { guestArea } ?: guestArea else guestArea}) & GPS permissions",
            onClick = { showSettingsSheet = true },
            modifier = Modifier.testTag("menu_settings"),
          )
          HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
          AccountMenuRow(
            icon = Icons.AutoMirrored.Rounded.HelpOutline,
            title = "Help & Support",
            subtitle = "Zero payment gate, direct pay & rating accountability FAQ",
            onClick = { showHelpSheet = true },
            modifier = Modifier.testTag("menu_help_support"),
          )
        }
      }

      val myComplaints = (myComplaintsState as? UiState.Success)?.data ?: emptyList()
      if (isSignedIn && myComplaints.isNotEmpty()) {
        FiledReportsCard(myComplaints = myComplaints)
      }

      if (isSignedIn) {
        OutlinedButton(
          onClick = onSignOut,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(50.dp).testTag("sign_out_button"),
        ) {
          Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = "Sign Out", modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Sign Out")
        }
      }

      Spacer(modifier = Modifier.height(12.dp))
    }
  }

  if (showSavedSheet) {
    SavedProfessionalsSheet(
      isSignedIn = isSignedIn,
      savedTechnicians = savedTechnicians,
      categories = categories,
      onSignInWithGoogle = {
        scope.launch { authCardBringIntoView.bringIntoView() }
      },
      onOpenTechnician = onOpenTechnician,
      onToggleSavedTechnician = onToggleSavedTechnician,
      onDismiss = { showSavedSheet = false },
    )
  }

  if (showSettingsSheet) {
    val initialArea =
      if (isSignedIn) profile?.defaultArea?.ifBlank { guestArea } ?: guestArea
      else guestArea
    ProfileSettingsSheet(
      initialArea = initialArea,
      onSaveArea = { areaInput ->
        if (isSignedIn) {
          onSaveProfile(profile?.displayName ?: "", profile?.phoneNumber ?: "", areaInput)
        } else {
          onUpdateGuestArea(areaInput)
        }
      },
      onRequestLocation = onRequestLocation,
      onDismiss = { showSettingsSheet = false },
    )
  }

  if (showHelpSheet) {
    HelpAndSupportSheet(onDismiss = { showHelpSheet = false })
  }
}

@Composable
private fun IdentitySummaryCard(
  isSignedIn: Boolean,
  isAdmin: Boolean,
  roleLabel: String,
  profile: UserProfile?,
) {
  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
        Box(
          modifier =
            Modifier.size(44.dp)
              .clip(CircleShape)
              .background(MaterialTheme.colorScheme.primaryContainer),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.Rounded.Person,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(24.dp),
          )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
          Text(
            text =
              if (!isSignedIn) "Guest User"
              else profile?.displayName?.ifBlank { "SkillConnect Member" } ?: "SkillConnect Member",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text =
              if (!isSignedIn) "Login or create your account"
              else profile?.email?.ifBlank { profile.defaultArea } ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      Surface(
        shape = RoundedCornerShape(50),
        color = if (isAdmin) MaterialTheme.colorScheme.primaryContainer else EmeraldContainer,
      ) {
        Text(
          text = roleLabel,
          style = MaterialTheme.typography.labelMedium,
          color = if (isAdmin) MaterialTheme.colorScheme.onPrimaryContainer else OnEmeraldContainer,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
  }
}

@Composable
private fun ProviderEnrolBannerCard(
  isEnrolledProvider: Boolean,
  myTechnicianProfile: TechnicianProfile?,
  onClick: () -> Unit,
) {
  Card(
    onClick = onClick,
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = EmeraldContainer.copy(alpha = 0.78f)),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
    modifier = Modifier.fillMaxWidth().testTag("open_pro_hub_card"),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          Box(
            modifier =
              Modifier.size(46.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Build,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onPrimary,
              modifier = Modifier.size(24.dp),
            )
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = if (isEnrolledProvider) "Service Provider Hub" else "Become a Service Provider",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = OnEmeraldContainer,
            )
            if (myTechnicianProfile != null) {
              Spacer(modifier = Modifier.height(2.dp))
              val statusText =
                when (myTechnicianProfile.verificationEnum) {
                  VerificationStatus.APPROVED ->
                    if (myTechnicianProfile.verifiedBadge) "Approved & Verified" else "Approved"
                  VerificationStatus.PENDING -> "Verification Pending"
                  VerificationStatus.REJECTED -> "Needs Attention"
                }
              val statusBg =
                if (myTechnicianProfile.verificationEnum == VerificationStatus.PENDING) AmberContainer
                else MaterialTheme.colorScheme.surface
              Surface(shape = RoundedCornerShape(50), color = statusBg) {
                Text(
                  text = statusText,
                  style = MaterialTheme.typography.labelSmall,
                  color =
                    if (myTechnicianProfile.verificationEnum == VerificationStatus.PENDING)
                      OnAmberContainer
                    else OnEmeraldContainer,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                )
              }
            }
          }
        }
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
          contentDescription = "Open Technical Registration",
          tint = OnEmeraldContainer,
        )
      }

      Text(
        text =
          if (isEnrolledProvider)
            "Manage your 4-stage verification status, Availability ON/OFF switch, live GPS location sharing & rates."
          else
            "Are you an electrician, plumber, mechanic or other skilled professional? Register your skills and get discovered by customers near you.",
        style = MaterialTheme.typography.bodySmall,
        color = OnEmeraldContainer.copy(alpha = 0.9f),
      )

      Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(50.dp).testTag("enroll_technician_button"),
      ) {
        Text(
          text =
            if (isEnrolledProvider) "Manage Provider Profile & GPS"
            else "Enrol as Technical Person",
          fontWeight = FontWeight.SemiBold,
        )
      }
    }
  }
}

@Composable
private fun FiledReportsCard(myComplaints: List<ServiceComplaint>) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Rounded.ReportProblem,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = "My Filed Reports (${myComplaints.size})", style = MaterialTheme.typography.titleSmall)
      }
      myComplaints.forEach { complaint ->
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              Text(
                text = "${complaint.reportedTechName} • ${complaint.reason}",
                style = MaterialTheme.typography.labelLarge,
              )
              Text(
                text = complaint.statusEnum.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Text(
              text = complaint.description,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (complaint.adminResolution.isNotBlank()) {
              Text(
                text = "Resolution: ${complaint.adminResolution}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AccountMenuRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 14.dp, vertical = 9.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier.size(34.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(18.dp),
      )
    }
    Spacer(modifier = Modifier.width(12.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
      Text(
        text = subtitle,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
    Icon(
      imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.onSurfaceVariant,
    )
  }
}
