package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.ImageEncodingHelper
import com.example.model.ServiceCategory
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnEmeraldContainer
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProWizardStep4Charges(state: ProWizardState) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    OutlinedTextField(
      value = state.visitCharge,
      onValueChange = { state.visitCharge = it },
      label = { Text("Visit Fee (PKR) *") },
      singleLine = true,
      modifier = Modifier.weight(1f).testTag("enroll_visit_charge_input"),
    )
    OutlinedTextField(
      value = state.hourlyCharge,
      onValueChange = { state.hourlyCharge = it },
      label = { Text("Hourly Rate (Opt.)") },
      singleLine = true,
      modifier = Modifier.weight(1f).testTag("enroll_hourly_charge_input"),
    )
  }

  OutlinedTextField(
    value = state.dailyCharge,
    onValueChange = { state.dailyCharge = it },
    label = { Text("Daily Charges PKR (Optional, e.g. Rs. 3,500/day)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_daily_charge_input"),
  )

  OutlinedTextField(
    value = state.serviceChargeNote,
    onValueChange = { state.serviceChargeNote = it },
    label = { Text("Charges Note (e.g. Direct cash after job completion)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_service_charge_input"),
  )

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Working Days *", style = MaterialTheme.typography.titleSmall)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      AllWeekDays.forEach { day ->
        val active = day in state.workingDays
        FilterChip(
          selected = active,
          onClick = {
            state.workingDays = if (active) state.workingDays - day else state.workingDays + day
          },
          label = { Text(day) },
        )
      }
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Working Hours *", style = MaterialTheme.typography.titleSmall)
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      WorkingHourPresets.forEach { preset ->
        FilterChip(
          selected = state.availabilityHours == preset,
          onClick = { state.availabilityHours = preset },
          label = { Text(preset) },
        )
      }
    }
    OutlinedTextField(
      value = state.availabilityHours,
      onValueChange = { state.availabilityHours = it },
      label = { Text("Custom Working Hours *") },
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("enroll_hours_input"),
    )
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text("Available for Emergency Calls", style = MaterialTheme.typography.titleSmall)
      Text(
        "Accept urgent breakdowns & after-hours requests",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(checked = state.emergencyAvailable, onCheckedChange = { state.emergencyAvailable = it })
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text("Available Now Status *", style = MaterialTheme.typography.titleSmall)
      Text(
        "Show green Available Now indicator immediately",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(checked = state.availableNow, onCheckedChange = { state.availableNow = it })
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text("Show in Public Directory", style = MaterialTheme.typography.titleSmall)
      Text(
        "Clients can discover & hire you directly",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(checked = state.listed, onCheckedChange = { state.listed = it })
  }
}

@Composable
fun ProWizardStep5Documents(
  state: ProWizardState,
  selectedCategory: ServiceCategory,
  userProfile: UserProfile?,
  isPhoneVerified: Boolean,
  onPickCnicFront: () -> Unit,
  onPickCnicBack: () -> Unit,
  onPickCertificate: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.Badge, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Identity & Trade Verification (Admin-Only Private)", style = MaterialTheme.typography.titleSmall)
      }

      OutlinedTextField(
        value = state.cnicNumber,
        onValueChange = { state.cnicNumber = it },
        label = { Text("CNIC Number (e.g. 35202-1234567-1)") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
      )

      DocumentUploadRow(
        title = "CNIC Front Image",
        subtitle = if (state.cnicPhotoData.isNotEmpty()) "Front CNIC image attached" else "Upload front side for Verified badge",
        dataUrl = state.cnicPhotoData,
        onPickClick = onPickCnicFront,
        onClearClick = { state.cnicPhotoData = "" },
        testTag = "enroll_cnic_front_button",
      )

      DocumentUploadRow(
        title = "CNIC Back Image",
        subtitle = if (state.cnicBackPhotoData.isNotEmpty()) "Back CNIC image attached" else "Upload back side of CNIC",
        dataUrl = state.cnicBackPhotoData,
        onPickClick = onPickCnicBack,
        onClearClick = { state.cnicBackPhotoData = "" },
        testTag = "enroll_cnic_back_button",
      )

      DocumentUploadRow(
        title = "Certificate / Trade License (Optional)",
        subtitle = if (state.certificatePhotoData.isNotEmpty()) "Certificate attached" else "TEVTA, diploma or workshop license",
        dataUrl = state.certificatePhotoData,
        onPickClick = onPickCertificate,
        onClearClick = { state.certificatePhotoData = "" },
        testTag = "enroll_certificate_button",
      )
    }
  }

  Surface(
    color = EmeraldContainer.copy(alpha = 0.55f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
      Icon(Icons.Rounded.Security, contentDescription = null, tint = OnEmeraldContainer)
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "Account Security Verified",
          style = MaterialTheme.typography.titleSmall,
          color = OnEmeraldContainer,
        )
        Text(
          text =
            buildString {
              append("Secured via Google Sign-In")
              if (!userProfile?.email.isNullOrBlank()) append(" (${userProfile?.email})")
              append(if (isPhoneVerified) " • Mobile OTP Verified" else " • Mobile OTP optional")
            },
          style = MaterialTheme.typography.bodySmall,
          color = OnEmeraldContainer.copy(alpha = 0.88f),
        )
      }
    }
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Text("Registration Summary", style = MaterialTheme.typography.titleSmall)
      Text(
        text = "${state.fullName.ifBlank { "Unnamed" }} • ${selectedCategory.name} (${state.yearsText.ifBlank { "1" }} yrs exp)",
        style = MaterialTheme.typography.bodySmall,
      )
      Text(
        text = "Location: ${state.workingArea.ifBlank { "-" }}, ${state.city} (${state.radiusKm.roundToLong()} km radius)",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = "Visit Charge: ${state.visitCharge.ifBlank { "Not set" }} • Days: ${state.workingDays.joinToString(", ")}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  Row(
    modifier = Modifier.fillMaxWidth().clickable { state.termsAccepted = !state.termsAccepted },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      checked = state.termsAccepted,
      onCheckedChange = { state.termsAccepted = it },
      modifier = Modifier.testTag("enroll_terms_checkbox"),
    )
    Text(
      text = "I accept SkillConnect Terms & Conditions (free service connection platform with zero payment gate) *",
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f),
    )
  }

  Row(
    modifier = Modifier.fillMaxWidth().clickable { state.verificationConsent = !state.verificationConsent },
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Checkbox(
      checked = state.verificationConsent,
      onCheckedChange = { state.verificationConsent = it },
      modifier = Modifier.testTag("enroll_consent_checkbox"),
    )
    Text(
      text = "I consent to identity & document review by Super Admin for Verified badge approval *",
      style = MaterialTheme.typography.bodySmall,
      modifier = Modifier.weight(1f),
    )
  }
}

@Composable
fun DocumentUploadRow(
  title: String,
  subtitle: String,
  dataUrl: String,
  onPickClick: () -> Unit,
  onClearClick: () -> Unit,
  testTag: String,
) {
  val bmp =
    remember(dataUrl) {
      if (dataUrl.isNotEmpty()) ImageEncodingHelper.decodeDataUrlToImageBitmap(dataUrl) else null
    }
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(10.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      if (bmp != null) {
        Image(
          bitmap = bmp,
          contentDescription = title,
          contentScale = ContentScale.Crop,
          modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)),
        )
      } else {
        Box(
          modifier =
            Modifier.size(48.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant),
          contentAlignment = Alignment.Center,
        ) {
          Icon(
            Icons.Rounded.UploadFile,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge)
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      if (dataUrl.isNotEmpty()) {
        IconButton(onClick = onClearClick, modifier = Modifier.size(36.dp)) {
          Icon(Icons.Rounded.Close, contentDescription = "Remove $title", modifier = Modifier.size(18.dp))
        }
      }
      OutlinedButton(
        onClick = onPickClick,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
        modifier = Modifier.testTag(testTag),
      ) {
        Text(if (dataUrl.isNotEmpty()) "Replace" else "Upload", style = MaterialTheme.typography.labelMedium)
      }
    }
  }
}

@Composable
fun PhoneOtpDialog(
  phoneNumber: String,
  verificationId: String?,
  statusMessage: String?,
  isBusy: Boolean,
  onSendOtp: () -> Unit,
  onVerifyCode: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  var otpCode by remember { mutableStateOf("") }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Verify Mobile Number (OTP)") },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Phone: $phoneNumber",
          style = MaterialTheme.typography.titleSmall,
        )
        Text(
          text =
            "Uses Firebase Phone Authentication (international E.164 format required, e.g. +923001234567).",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!statusMessage.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = statusMessage,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSecondaryContainer,
              modifier = Modifier.padding(10.dp),
            )
          }
        }
        if (verificationId != null) {
          OutlinedTextField(
            value = otpCode,
            onValueChange = { otpCode = it.filter { ch -> ch.isDigit() }.take(8) },
            label = { Text("Enter 6-digit SMS Code") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth().testTag("otp_code_input"),
          )
        }
      }
    },
    confirmButton = {
      if (verificationId == null) {
        Button(onClick = onSendOtp, enabled = !isBusy) {
          Text("Send SMS OTP")
        }
      } else {
        Button(
          onClick = { onVerifyCode(otpCode) },
          enabled = !isBusy && otpCode.length >= 4,
        ) {
          Text("Confirm Code")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    },
  )
}
