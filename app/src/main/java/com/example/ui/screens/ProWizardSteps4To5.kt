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
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PowerSettingsNew
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
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
import com.example.ui.theme.greenThemeTextFieldColors
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProWizardStep4Charges(state: ProWizardState) {
  var editingStartTime by remember { mutableStateOf(false) }
  var editingEndTime by remember { mutableStateOf(false) }

  val parsedTimes =
    remember(state.availabilityHours) {
      parseWorkingHourRange(state.availabilityHours)
    }

  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = "Section D – Charges & Availability",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Text(
        text = "Set your PKR rates, working days, hours, emergency & live status",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  OutlinedTextField(
    value = state.visitCharge,
    onValueChange = { state.visitCharge = it.filter { ch -> ch.isDigit() || ch == ',' } },
    label = { Text("Visiting Charges (PKR) *") },
    placeholder = { Text("e.g. 500") },
    prefix = { Text("PKR ") },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_visit_charge_input"),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    OutlinedTextField(
      value = state.hourlyCharge,
      onValueChange = { state.hourlyCharge = it.filter { ch -> ch.isDigit() || ch == ',' } },
      label = { Text("Hourly Charges (PKR)") },
      placeholder = { Text("Optional") },
      prefix = { Text("PKR ") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(),
      modifier = Modifier.weight(1f).testTag("enroll_hourly_charge_input"),
    )
    OutlinedTextField(
      value = state.dailyCharge,
      onValueChange = { state.dailyCharge = it.filter { ch -> ch.isDigit() || ch == ',' } },
      label = { Text("Daily Charges (PKR)") },
      placeholder = { Text("Optional") },
      prefix = { Text("PKR ") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(),
      modifier = Modifier.weight(1f).testTag("enroll_daily_charge_input"),
    )
  }

  OutlinedTextField(
    value = state.serviceChargeNote,
    onValueChange = { state.serviceChargeNote = it },
    label = { Text("Charges Note (Optional)") },
    placeholder = { Text("e.g. Direct cash after job completion") },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_service_charge_input"),
  )

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text("Working Days (Multi-select) *", style = MaterialTheme.typography.titleSmall)
      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        TextButton(
          onClick = { state.workingDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat") },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        ) {
          Text("Mon–Sat", style = MaterialTheme.typography.labelMedium)
        }
        TextButton(
          onClick = { state.workingDays = AllWeekDays },
          contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        ) {
          Text("All 7 Days", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
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
          leadingIcon =
            if (active) {
              { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
          label = { Text(day) },
          modifier = Modifier.testTag("enroll_day_${day.lowercase()}"),
        )
      }
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("Working Hours (Time selection) *", style = MaterialTheme.typography.titleSmall)
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      OutlinedButton(
        onClick = { editingStartTime = true },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier.weight(1f).testTag("enroll_start_time_button"),
      ) {
        Icon(Icons.Rounded.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.Start) {
          Text("Start Time", style = MaterialTheme.typography.labelSmall)
          Text(
            text = formatTime12Hour(parsedTimes.first.first, parsedTimes.first.second),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
      OutlinedButton(
        onClick = { editingEndTime = true },
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
        modifier = Modifier.weight(1f).testTag("enroll_end_time_button"),
      ) {
        Icon(Icons.Rounded.AccessTime, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column(horizontalAlignment = Alignment.Start) {
          Text("End Time", style = MaterialTheme.typography.labelSmall)
          Text(
            text = formatTime12Hour(parsedTimes.second.first, parsedTimes.second.second),
            style = MaterialTheme.typography.labelLarge,
          )
        }
      }
    }
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
      label = { Text("Selected Working Hours *") },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(),
      modifier = Modifier.fillMaxWidth().testTag("enroll_hours_input"),
    )
  }

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Available for Emergency (Optional)", style = MaterialTheme.typography.titleSmall)
    Text(
      text =
        if (state.emergencyAvailable) "Yes — accept urgent breakdowns & after-hours requests"
        else "No — available during regular scheduled hours only",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FilterChip(
        selected = state.emergencyAvailable,
        onClick = { state.emergencyAvailable = true },
        leadingIcon =
          if (state.emergencyAvailable) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
        label = { Text("Yes") },
        modifier = Modifier.weight(1f).testTag("enroll_emergency_yes"),
      )
      FilterChip(
        selected = !state.emergencyAvailable,
        onClick = { state.emergencyAvailable = false },
        leadingIcon =
          if (!state.emergencyAvailable) {
            { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
          } else null,
        label = { Text("No") },
        modifier = Modifier.weight(1f).testTag("enroll_emergency_no"),
      )
    }
  }

  Surface(
    color =
      if (state.availableNow) EmeraldContainer.copy(alpha = 0.45f)
      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    border =
      BorderStroke(
        width = 1.dp,
        color =
          if (state.availableNow) MaterialTheme.colorScheme.primary
          else MaterialTheme.colorScheme.outlineVariant,
      ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          Icons.Rounded.PowerSettingsNew,
          contentDescription = null,
          tint = if (state.availableNow) OnEmeraldContainer else MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text("Availability Status *", style = MaterialTheme.typography.titleSmall)
          Text(
            text =
              if (state.availableNow) "ON — Showing Available Now to nearby customers"
              else "OFF — Currently marked offline / unavailable",
            style = MaterialTheme.typography.bodySmall,
            color = if (state.availableNow) OnEmeraldContainer else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        FilterChip(
          selected = state.availableNow,
          onClick = { state.availableNow = true },
          leadingIcon =
            if (state.availableNow) {
              { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
          colors =
            FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldContainer,
              selectedLabelColor = OnEmeraldContainer,
              selectedLeadingIconColor = OnEmeraldContainer,
            ),
          label = { Text("ON") },
          modifier = Modifier.weight(1f).testTag("enroll_status_on"),
        )
        FilterChip(
          selected = !state.availableNow,
          onClick = { state.availableNow = false },
          leadingIcon =
            if (!state.availableNow) {
              { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
            } else null,
          label = { Text("OFF") },
          modifier = Modifier.weight(1f).testTag("enroll_status_off"),
        )
      }
    }
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

  if (editingStartTime) {
    WorkingHourPickerDialog(
      title = "Select Start Time",
      initialHour = parsedTimes.first.first,
      initialMinute = parsedTimes.first.second,
      onConfirm = { hour, minute ->
        val startFormatted = formatTime12Hour(hour, minute)
        val endFormatted = formatTime12Hour(parsedTimes.second.first, parsedTimes.second.second)
        state.availabilityHours = "$startFormatted - $endFormatted"
        editingStartTime = false
      },
      onDismiss = { editingStartTime = false },
    )
  }

  if (editingEndTime) {
    WorkingHourPickerDialog(
      title = "Select End Time",
      initialHour = parsedTimes.second.first,
      initialMinute = parsedTimes.second.second,
      onConfirm = { hour, minute ->
        val startFormatted = formatTime12Hour(parsedTimes.first.first, parsedTimes.first.second)
        val endFormatted = formatTime12Hour(hour, minute)
        state.availabilityHours = "$startFormatted - $endFormatted"
        editingEndTime = false
      },
      onDismiss = { editingEndTime = false },
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WorkingHourPickerDialog(
  title: String,
  initialHour: Int,
  initialMinute: Int,
  onConfirm: (Int, Int) -> Unit,
  onDismiss: () -> Unit,
) {
  val pickerState =
    rememberTimePickerState(
      initialHour = initialHour.coerceIn(0, 23),
      initialMinute = initialMinute.coerceIn(0, 59),
      is24Hour = false,
    )
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        TimePicker(state = pickerState)
      }
    },
    confirmButton = {
      Button(onClick = { onConfirm(pickerState.hour, pickerState.minute) }) {
        Text("Set Time")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}

private fun formatTime12Hour(hour24: Int, minute: Int): String {
  val period = if (hour24 >= 12) "PM" else "AM"
  val hour12 =
    when (val h = hour24 % 12) {
      0 -> 12
      else -> h
    }
  return "%02d:%02d %s".format(hour12, minute, period)
}

private fun parseWorkingHourRange(raw: String): Pair<Pair<Int, Int>, Pair<Int, Int>> {
  val regex = Regex("(\\d{1,2}):(\\d{2})\\s*(AM|PM)", RegexOption.IGNORE_CASE)
  val matches = regex.findAll(raw).toList()
  if (matches.size >= 2) {
    return parseMatchTo24Hour(matches[0]) to parseMatchTo24Hour(matches[1])
  }
  return (8 to 0) to (20 to 0)
}

private fun parseMatchTo24Hour(match: MatchResult): Pair<Int, Int> {
  val rawHour = match.groupValues[1].toIntOrNull()?.coerceIn(1, 12) ?: 8
  val minute = match.groupValues[2].toIntOrNull()?.coerceIn(0, 59) ?: 0
  val isPm = match.groupValues[3].equals("PM", ignoreCase = true)
  val hour24 =
    when {
      isPm && rawHour < 12 -> rawHour + 12
      !isPm && rawHour == 12 -> 0
      else -> rawHour
    }
  return hour24 to minute
}

@Composable
fun ProWizardStep5Documents(
  state: ProWizardState,
  selectedCategory: ServiceCategory,
  userProfile: UserProfile?,
  isPhoneVerified: Boolean,
  onPickCnicFront: () -> Unit,
  onCaptureCnicFront: () -> Unit,
  onPickCnicBack: () -> Unit,
  onCaptureCnicBack: () -> Unit,
  onPickCertificate: () -> Unit,
  onCaptureCertificate: () -> Unit,
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
        shape = RoundedCornerShape(12.dp),
        colors = greenThemeTextFieldColors(),
        modifier = Modifier.fillMaxWidth(),
      )

      DocumentUploadRow(
        title = "CNIC Front Image",
        subtitle = if (state.cnicPhotoData.isNotEmpty()) "Front CNIC image attached" else "Upload or capture front side for Verified badge",
        dataUrl = state.cnicPhotoData,
        onPickClick = onPickCnicFront,
        onCaptureClick = onCaptureCnicFront,
        onClearClick = { state.cnicPhotoData = "" },
        testTag = "enroll_cnic_front_button",
      )

      DocumentUploadRow(
        title = "CNIC Back Image",
        subtitle = if (state.cnicBackPhotoData.isNotEmpty()) "Back CNIC image attached" else "Upload or capture back side of CNIC",
        dataUrl = state.cnicBackPhotoData,
        onPickClick = onPickCnicBack,
        onCaptureClick = onCaptureCnicBack,
        onClearClick = { state.cnicBackPhotoData = "" },
        testTag = "enroll_cnic_back_button",
      )

      DocumentUploadRow(
        title = "Certificate / Trade License (Optional)",
        subtitle = if (state.certificatePhotoData.isNotEmpty()) "Certificate attached" else "TEVTA, diploma or workshop license",
        dataUrl = state.certificatePhotoData,
        onPickClick = onPickCertificate,
        onCaptureClick = onCaptureCertificate,
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
              append("Secured via SkillConnect Account")
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
        text =
          "Visit Charge: ${if (state.visitCharge.isBlank()) "Not set" else "PKR ${state.visitCharge}"} • Status: ${if (state.availableNow) "ON" else "OFF"} • Days: ${state.workingDays.joinToString(", ")}",
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
  onCaptureClick: (() -> Unit)? = null,
  onClearClick: () -> Unit,
  testTag: String,
) {
  val bmp =
    remember(dataUrl) {
      if (dataUrl.isNotEmpty()) ImageEncodingHelper.decodeDataUrlToImageBitmap(dataUrl) else null
    }
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.14f),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(10.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
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
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(
          onClick = onPickClick,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
          modifier = Modifier.weight(1f).testTag(testTag),
        ) {
          Text(if (dataUrl.isNotEmpty()) "Replace Upload" else "Upload", style = MaterialTheme.typography.labelMedium)
        }
        if (onCaptureClick != null) {
          OutlinedButton(
            onClick = onCaptureClick,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.weight(1f).testTag("${testTag}_capture"),
          ) {
            Text("Capture", style = MaterialTheme.typography.labelMedium)
          }
        }
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
            shape = RoundedCornerShape(12.dp),
            colors = greenThemeTextFieldColors(),
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
