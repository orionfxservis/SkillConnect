package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddAPhoto
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.model.Coordinates
import com.example.model.ServiceCategory
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnEmeraldContainer
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProWizardStep1Personal(
  state: ProWizardState,
  isPhoneVerified: Boolean,
  onPickProfilePhoto: () -> Unit,
  onOpenOtpDialog: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    val avatarBmp =
      remember(state.profilePhotoData) {
        ImageEncodingHelper.decodeDataUrlToImageBitmap(state.profilePhotoData)
      }
    Box(
      modifier =
        Modifier.size(74.dp)
          .clip(CircleShape)
          .background(MaterialTheme.colorScheme.primaryContainer)
          .clickable(onClick = onPickProfilePhoto)
          .testTag("enroll_profile_photo_picker"),
      contentAlignment = Alignment.Center,
    ) {
      if (avatarBmp != null) {
        Image(
          bitmap = avatarBmp,
          contentDescription = "Profile Photo",
          contentScale = ContentScale.Crop,
          modifier = Modifier.fillMaxSize(),
        )
      } else {
        Icon(
          imageVector = Icons.Rounded.Person,
          contentDescription = "Upload profile photo",
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(34.dp),
        )
      }
    }
    Column(modifier = Modifier.weight(1f)) {
      Text("Profile Photograph *", style = MaterialTheme.typography.titleSmall)
      Text(
        text = "Clear photo builds trust when visiting customers",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(6.dp))
      OutlinedButton(
        onClick = onPickProfilePhoto,
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
      ) {
        Icon(Icons.Rounded.AddAPhoto, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          if (state.profilePhotoData.isNotEmpty()) "Change Photo" else "Upload Photo",
          style = MaterialTheme.typography.labelMedium,
        )
      }
    }
  }

  OutlinedTextField(
    value = state.fullName,
    onValueChange = { state.fullName = it },
    label = { Text("Full Name *") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_name_input"),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    OutlinedTextField(
      value = state.contactPhone,
      onValueChange = { state.contactPhone = it },
      label = { Text("Mobile Number (e.g. +92300...) *") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
      trailingIcon = {
        if (isPhoneVerified) {
          Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = "Phone Verified",
            tint = OnEmeraldContainer,
          )
        }
      },
      modifier = Modifier.weight(1f).testTag("enroll_phone_input"),
    )
    OutlinedButton(
      onClick = onOpenOtpDialog,
      enabled = state.contactPhone.trim().length >= 5,
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier.height(54.dp).testTag("verify_phone_otp_button"),
    ) {
      Text(if (isPhoneVerified) "Verified" else "Verify OTP")
    }
  }

  OutlinedTextField(
    value = state.email,
    onValueChange = { state.email = it },
    label = { Text("Email Address (Optional)") },
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    modifier = Modifier.fillMaxWidth().testTag("enroll_email_input"),
  )

  OutlinedTextField(
    value = state.cnicNumber,
    onValueChange = { state.cnicNumber = it },
    label = { Text("CNIC Number (For Verification, e.g. 35202-1234567-1)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_cnic_input"),
  )

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Gender (Optional)", style = MaterialTheme.typography.titleSmall)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      GenderOptions.forEach { option ->
        FilterChip(
          selected = state.gender == option,
          onClick = { state.gender = if (state.gender == option) "" else option },
          label = { Text(option) },
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProWizardStep2Professional(
  state: ProWizardState,
  categories: List<ServiceCategory>,
  selectedCategory: ServiceCategory,
  onPickPortfolioPhoto: () -> Unit,
) {
  Text("Main Category *", style = MaterialTheme.typography.titleSmall)
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    categories.forEach { cat ->
      FilterChip(
        selected = state.categoryId == cat.id,
        onClick = {
          state.categoryId = cat.id
          state.additionalCategoryIds = state.additionalCategoryIds.filter { it != cat.id }
          if (state.subcategory !in cat.subcategories) {
            state.subcategory = cat.subcategories.firstOrNull() ?: ""
          }
        },
        label = { Text(cat.name) },
        modifier = Modifier.testTag("enroll_cat_${cat.id}"),
      )
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = "Additional Categories (Multi-select, Optional)",
      style = MaterialTheme.typography.titleSmall,
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      categories
        .filter { it.id != state.categoryId }
        .forEach { cat ->
          val checked = cat.id in state.additionalCategoryIds
          FilterChip(
            selected = checked,
            onClick = {
              state.additionalCategoryIds =
                if (checked) state.additionalCategoryIds - cat.id
                else (state.additionalCategoryIds + cat.id).take(6)
            },
            label = { Text(cat.name) },
          )
        }
    }
  }

  if (selectedCategory.subcategories.isNotEmpty()) {
    Text("Subcategory *", style = MaterialTheme.typography.titleSmall)
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      selectedCategory.subcategories.forEach { sub ->
        FilterChip(
          selected = state.subcategory == sub,
          onClick = { state.subcategory = sub },
          label = { Text(sub) },
        )
      }
    }
  }
  OutlinedTextField(
    value = state.subcategory,
    onValueChange = { state.subcategory = it },
    label = { Text("Specific Subcategory *") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_subcategory_input"),
  )

  OutlinedTextField(
    value = state.professionalTitle,
    onValueChange = { state.professionalTitle = it },
    label = { Text("Professional Title * (e.g. Senior Master Electrician)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_title_input"),
  )

  if (selectedCategory.subcategories.isNotEmpty()) {
    val currentSkills =
      state.expertise.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    Text(
      text = "Tap to Add Skills / Expertise *",
      style = MaterialTheme.typography.titleSmall,
    )
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      selectedCategory.subcategories.forEach { skill ->
        val isAdded = currentSkills.any { it.equals(skill, ignoreCase = true) }
        FilterChip(
          selected = isAdded,
          onClick = {
            val updated =
              if (isAdded) currentSkills.filterNot { it.equals(skill, ignoreCase = true) }
              else currentSkills + skill
            state.expertise = updated.joinToString(", ")
          },
          label = { Text(skill) },
        )
      }
    }
  }
  OutlinedTextField(
    value = state.expertise,
    onValueChange = { state.expertise = it },
    label = { Text("Skills & Expertise (comma separated) *") },
    modifier = Modifier.fillMaxWidth().testTag("enroll_expertise_input"),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    OutlinedTextField(
      value = state.yearsText,
      onValueChange = { state.yearsText = it.filter { ch -> ch.isDigit() } },
      label = { Text("Years Exp *") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      modifier = Modifier.weight(0.9f).testTag("enroll_years_input"),
    )
    OutlinedTextField(
      value = state.qualification,
      onValueChange = { state.qualification = it },
      label = { Text("Qualification (Optional)") },
      singleLine = true,
      leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null) },
      modifier = Modifier.weight(1.4f).testTag("enroll_qualification_input"),
    )
  }
  FlowRow(
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    QualificationPresets.forEach { preset ->
      FilterChip(
        selected = state.qualification == preset,
        onClick = { state.qualification = if (state.qualification == preset) "" else preset },
        label = { Text(preset, style = MaterialTheme.typography.labelSmall) },
      )
    }
  }

  OutlinedTextField(
    value = state.bio,
    onValueChange = { state.bio = it },
    label = { Text("Work Description & Experience Summary *") },
    minLines = 3,
    maxLines = 5,
    modifier = Modifier.fillMaxWidth().testTag("enroll_bio_input"),
  )

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = "Previous Work Images (${state.portfolioImages.size}/4 Optional)",
        style = MaterialTheme.typography.titleSmall,
      )
      if (state.portfolioImages.size < 4) {
        OutlinedButton(
          onClick = onPickPortfolioPhoto,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
        ) {
          Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Add Photo")
        }
      }
    }
    if (state.portfolioImages.isNotEmpty()) {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        itemsIndexed(state.portfolioImages) { idx, dataUrl ->
          val bmp =
            remember(dataUrl) {
              ImageEncodingHelper.decodeDataUrlToImageBitmap(dataUrl)
            }
          Box(modifier = Modifier.size(100.dp)) {
            if (bmp != null) {
              Image(
                bitmap = bmp,
                contentDescription = "Portfolio photo ${idx + 1}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)),
              )
            }
            IconButton(
              onClick = {
                state.portfolioImages = state.portfolioImages.filterIndexed { i, _ -> i != idx }
              },
              modifier =
                Modifier.align(Alignment.TopEnd)
                  .padding(4.dp)
                  .size(26.dp)
                  .clip(CircleShape)
                  .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
            ) {
              Icon(Icons.Rounded.Close, contentDescription = "Remove photo", modifier = Modifier.size(16.dp))
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProWizardStep3Location(
  state: ProWizardState,
  userCoordinates: Coordinates?,
  onRequestLocation: () -> Unit,
) {
  var cityExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = cityExpanded,
    onExpandedChange = { cityExpanded = !cityExpanded },
  ) {
    OutlinedTextField(
      value = state.city,
      onValueChange = { state.city = it },
      label = { Text("Current City *") },
      leadingIcon = { Icon(Icons.Rounded.LocationCity, contentDescription = null) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
      singleLine = true,
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
          .fillMaxWidth()
          .testTag("enroll_city_input"),
    )
    ExposedDropdownMenu(
      expanded = cityExpanded,
      onDismissRequest = { cityExpanded = false },
    ) {
      PakistanCities.forEach { option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            state.city = option
            cityExpanded = false
          },
        )
      }
    }
  }

  OutlinedTextField(
    value = state.workingArea,
    onValueChange = { state.workingArea = it },
    label = { Text("Primary Area / Town * (e.g. Gulshan-e-Iqbal)") },
    singleLine = true,
    modifier = Modifier.fillMaxWidth().testTag("enroll_area_input"),
  )

  Surface(
    color = EmeraldContainer.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text("Current GPS Location *", style = MaterialTheme.typography.titleSmall)
        Text(
          text =
            if (state.lat != 0.0 || state.lng != 0.0)
              "Pinned: ${"%.4f".format(state.lat)}, ${"%.4f".format(state.lng)}"
            else "Tap Detect GPS so nearby customers can track you",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
      Button(
        onClick = {
          onRequestLocation()
          if (userCoordinates != null) {
            state.lat = userCoordinates.latitude
            state.lng = userCoordinates.longitude
          }
        },
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        modifier = Modifier.testTag("enroll_sync_gps_button"),
      ) {
        Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Detect GPS")
      }
    }
  }

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text(
      text = "Service Coverage Radius: ${state.radiusKm.roundToLong()} km *",
      style = MaterialTheme.typography.titleSmall,
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      listOf(5, 10, 15, 25, 50).forEach { km ->
        FilterChip(
          selected = state.radiusKm.roundToLong() == km.toLong(),
          onClick = { state.radiusKm = km.toFloat() },
          label = { Text("$km km") },
        )
      }
    }
    Slider(
      value = state.radiusKm,
      onValueChange = { state.radiusKm = it },
      valueRange = 2f..80f,
    )
  }

  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Text("Preferred Working Areas (Optional)", style = MaterialTheme.typography.titleSmall)
    val suggested =
      SuggestedAreasByCity[state.city]
        ?: listOf("City Center", "North Zone", "South Zone", "Cantt", "Industrial Area")
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      (suggested + state.preferredAreas).distinct().forEach { area ->
        val selected = area in state.preferredAreas
        FilterChip(
          selected = selected,
          onClick = {
            state.preferredAreas =
              if (selected) state.preferredAreas - area else (state.preferredAreas + area).take(12)
          },
          label = { Text(area) },
        )
      }
    }
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      OutlinedTextField(
        value = state.customAreaInput,
        onValueChange = { state.customAreaInput = it },
        label = { Text("Add another area/town") },
        singleLine = true,
        modifier = Modifier.weight(1f),
      )
      OutlinedButton(
        onClick = {
          val cleaned = state.customAreaInput.trim()
          if (cleaned.isNotEmpty() && cleaned !in state.preferredAreas && state.preferredAreas.size < 12) {
            state.preferredAreas = state.preferredAreas + cleaned
            state.customAreaInput = ""
          }
        },
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.height(54.dp),
      ) {
        Icon(Icons.Rounded.Add, contentDescription = "Add area")
      }
    }
  }

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text("Willing to Travel to Customer Location *", style = MaterialTheme.typography.titleSmall)
      Text(
        text =
          if (state.travelToCustomer) "Yes — home/on-site visits enabled"
          else "Workshop / shop service only",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
    Switch(checked = state.travelToCustomer, onCheckedChange = { state.travelToCustomer = it })
  }
}
