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
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Wc
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
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.greenThemeTextFieldColors
import kotlin.math.roundToLong

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProWizardStep1Personal(
  state: ProWizardState,
  categories: List<ServiceCategory>,
  selectedCategory: ServiceCategory,
  isPhoneVerified: Boolean,
  onPickProfilePhoto: () -> Unit,
  onCaptureProfilePhoto: () -> Unit,
  onPickCnicFront: () -> Unit,
  onCaptureCnicFront: () -> Unit,
  onPickCnicBack: () -> Unit,
  onCaptureCnicBack: () -> Unit,
  onOpenOtpDialog: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = "Section A – Personal Information",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Text(
        text = "Select your service category and complete your personal & CNIC verification details",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  var categoryExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = categoryExpanded,
    onExpandedChange = { categoryExpanded = !categoryExpanded },
  ) {
    OutlinedTextField(
      value = selectedCategory.name,
      onValueChange = {},
      readOnly = true,
      label = { Text("Service Category *") },
      leadingIcon = {
        Icon(
          imageVector = selectedCategory.icon,
          contentDescription = null,
          tint = selectedCategory.tint,
        )
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(highlighted = categoryExpanded),
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("enroll_category_dropdown"),
    )
    ExposedDropdownMenu(
      expanded = categoryExpanded,
      onDismissRequest = { categoryExpanded = false },
    ) {
      categories.forEach { cat ->
        DropdownMenuItem(
          leadingIcon = {
            Icon(
              imageVector = cat.icon,
              contentDescription = null,
              tint = cat.tint,
              modifier = Modifier.size(20.dp),
            )
          },
          text = { Text(cat.name) },
          onClick = {
            state.categoryId = cat.id
            state.additionalCategoryIds = state.additionalCategoryIds.filter { it != cat.id }
            if (state.subcategory !in cat.subcategories) {
              state.subcategory = cat.subcategories.firstOrNull() ?: ""
            }
            categoryExpanded = false
          },
          modifier = Modifier.testTag("enroll_cat_${cat.id}"),
        )
      }
    }
  }

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
          .testTag("enroll_profile_photo_preview"),
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
        text = "Upload from gallery or capture a live selfie",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Spacer(modifier = Modifier.height(6.dp))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
          onClick = onPickProfilePhoto,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f).testTag("enroll_profile_photo_picker"),
        ) {
          Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            if (state.profilePhotoData.isNotEmpty()) "Change" else "Upload",
            style = MaterialTheme.typography.labelMedium,
          )
        }
        OutlinedButton(
          onClick = onCaptureProfilePhoto,
          shape = RoundedCornerShape(10.dp),
          contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          modifier = Modifier.weight(1f).testTag("enroll_profile_photo_capture"),
        ) {
          Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text("Capture", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }

  OutlinedTextField(
    value = state.fullName,
    onValueChange = { state.fullName = it },
    label = { Text("Full Name *") },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
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
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(),
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
    label = { Text("Email Address (Saved from Sign Up • Optional)") },
    supportingText =
      if (state.email.isNotBlank()) {
        { Text("Pre-filled from your sign-up account") }
      } else null,
    singleLine = true,
    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_email_input"),
  )

  var genderExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = genderExpanded,
    onExpandedChange = { genderExpanded = !genderExpanded },
  ) {
    OutlinedTextField(
      value = state.gender,
      onValueChange = {},
      readOnly = true,
      label = { Text("Gender *") },
      placeholder = { Text("Select Male or Female") },
      leadingIcon = { Icon(Icons.Rounded.Wc, contentDescription = null) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(highlighted = genderExpanded),
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("enroll_gender_dropdown"),
    )
    ExposedDropdownMenu(
      expanded = genderExpanded,
      onDismissRequest = { genderExpanded = false },
    ) {
      GenderOptions.forEach { option ->
        DropdownMenuItem(
          text = { Text(option) },
          onClick = {
            state.gender = option
            genderExpanded = false
          },
          modifier = Modifier.testTag("enroll_gender_${option.lowercase()}"),
        )
      }
    }
  }

  OutlinedTextField(
    value = state.cnicNumber,
    onValueChange = { state.cnicNumber = it },
    label = { Text("CNIC Number (For verification, e.g. 35202-1234567-1)") },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_cnic_input"),
  )

  DocumentUploadRow(
    title = "CNIC Front Image (For verification)",
    subtitle =
      if (state.cnicPhotoData.isNotEmpty()) "Front CNIC image attached"
      else "Upload or capture front side of CNIC",
    dataUrl = state.cnicPhotoData,
    onPickClick = onPickCnicFront,
    onCaptureClick = onCaptureCnicFront,
    onClearClick = { state.cnicPhotoData = "" },
    testTag = "enroll_step1_cnic_front_button",
  )

  DocumentUploadRow(
    title = "CNIC Back Image (For verification)",
    subtitle =
      if (state.cnicBackPhotoData.isNotEmpty()) "Back CNIC image attached"
      else "Upload or capture back side of CNIC",
    dataUrl = state.cnicBackPhotoData,
    onPickClick = onPickCnicBack,
    onCaptureClick = onCaptureCnicBack,
    onClearClick = { state.cnicBackPhotoData = "" },
    testTag = "enroll_step1_cnic_back_button",
  )
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProWizardStep2Professional(
  state: ProWizardState,
  categories: List<ServiceCategory>,
  selectedCategory: ServiceCategory,
  onPickCertificate: () -> Unit,
  onCaptureCertificate: () -> Unit,
  onPickPortfolioPhotos: () -> Unit,
  onCapturePortfolioPhoto: () -> Unit,
) {
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = "Section B – Professional Information",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Text(
        text = "Confirm your category, specialty, skills, experience & work portfolio",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  var mainCategoryExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = mainCategoryExpanded,
    onExpandedChange = { mainCategoryExpanded = !mainCategoryExpanded },
  ) {
    OutlinedTextField(
      value = selectedCategory.name,
      onValueChange = {},
      readOnly = true,
      label = { Text("Main Category *") },
      leadingIcon = {
        Icon(
          imageVector = selectedCategory.icon,
          contentDescription = null,
          tint = selectedCategory.tint,
        )
      },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = mainCategoryExpanded) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(highlighted = mainCategoryExpanded),
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("enroll_step2_category_dropdown"),
    )
    ExposedDropdownMenu(
      expanded = mainCategoryExpanded,
      onDismissRequest = { mainCategoryExpanded = false },
    ) {
      categories.forEach { cat ->
        DropdownMenuItem(
          leadingIcon = {
            Icon(
              imageVector = cat.icon,
              contentDescription = null,
              tint = cat.tint,
              modifier = Modifier.size(20.dp),
            )
          },
          text = { Text(cat.name) },
          onClick = {
            state.categoryId = cat.id
            state.additionalCategoryIds = state.additionalCategoryIds.filter { it != cat.id }
            if (state.subcategory !in cat.subcategories) {
              state.subcategory = cat.subcategories.firstOrNull() ?: ""
            }
            mainCategoryExpanded = false
          },
        )
      }
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

  val subcategoryChoices =
    remember(selectedCategory) {
      selectedCategory.subcategories.ifEmpty { listOf("General ${selectedCategory.name}") }
    }
  var subcategoryExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = subcategoryExpanded,
    onExpandedChange = { subcategoryExpanded = !subcategoryExpanded },
  ) {
    OutlinedTextField(
      value = state.subcategory,
      onValueChange = {},
      readOnly = true,
      label = { Text("Subcategory *") },
      placeholder = { Text("Select subcategory") },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subcategoryExpanded) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(highlighted = subcategoryExpanded),
      modifier =
        Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable)
          .fillMaxWidth()
          .testTag("enroll_subcategory_dropdown"),
    )
    ExposedDropdownMenu(
      expanded = subcategoryExpanded,
      onDismissRequest = { subcategoryExpanded = false },
    ) {
      subcategoryChoices.forEach { sub ->
        DropdownMenuItem(
          text = { Text(sub) },
          onClick = {
            state.subcategory = sub
            subcategoryExpanded = false
          },
          modifier = Modifier.testTag("enroll_sub_${sub.lowercase().replace(" ", "_")}"),
        )
      }
    }
  }

  OutlinedTextField(
    value = state.professionalTitle,
    onValueChange = { state.professionalTitle = it },
    label = { Text("Professional Title * (e.g. Senior Master Electrician)") },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_title_input"),
  )

  if (selectedCategory.subcategories.isNotEmpty()) {
    val currentSkills =
      state.expertise.split(",").map { it.trim() }.filter { it.isNotEmpty() }
    Text(
      text = "Skills / Expertise (Multi-select) *",
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
    label = { Text("Selected Skills / Expertise *") },
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_expertise_input"),
  )

  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    OutlinedTextField(
      value = state.yearsText,
      onValueChange = { state.yearsText = it.filter { ch -> ch.isDigit() } },
      label = { Text("Experience (Years) *") },
      singleLine = true,
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(),
      modifier = Modifier.weight(0.9f).testTag("enroll_years_input"),
    )

    var qualificationExpanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
      expanded = qualificationExpanded,
      onExpandedChange = { qualificationExpanded = !qualificationExpanded },
      modifier = Modifier.weight(1.4f),
    ) {
      OutlinedTextField(
        value = state.qualification,
        onValueChange = { state.qualification = it },
        label = { Text("Qualification (Optional)") },
        placeholder = { Text("Select or type") },
        leadingIcon = { Icon(Icons.Rounded.School, contentDescription = null) },
        trailingIcon = {
          ExposedDropdownMenuDefaults.TrailingIcon(expanded = qualificationExpanded)
        },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = greenThemeTextFieldColors(highlighted = qualificationExpanded),
        modifier =
          Modifier.menuAnchor(MenuAnchorType.PrimaryEditable)
            .fillMaxWidth()
            .testTag("enroll_qualification_input"),
      )
      ExposedDropdownMenu(
        expanded = qualificationExpanded,
        onDismissRequest = { qualificationExpanded = false },
      ) {
        QualificationPresets.forEach { preset ->
          DropdownMenuItem(
            text = { Text(preset) },
            onClick = {
              state.qualification = preset
              qualificationExpanded = false
            },
          )
        }
      }
    }
  }

  DocumentUploadRow(
    title = "Certificate / Trade License (Optional)",
    subtitle =
      if (state.certificatePhotoData.isNotEmpty()) "Certificate attached"
      else "Upload or capture TEVTA, diploma or workshop license",
    dataUrl = state.certificatePhotoData,
    onPickClick = onPickCertificate,
    onCaptureClick = onCaptureCertificate,
    onClearClick = { state.certificatePhotoData = "" },
    testTag = "enroll_step2_certificate_button",
  )

  OutlinedTextField(
    value = state.bio,
    onValueChange = { state.bio = it },
    label = { Text("Work Description *") },
    placeholder = { Text("Describe your expertise, tools & service quality...") },
    minLines = 3,
    maxLines = 5,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
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
        modifier = Modifier.weight(1f),
      )
      if (state.portfolioImages.size < 4) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          OutlinedButton(
            onClick = onPickPortfolioPhotos,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("enroll_portfolio_pick_button"),
          ) {
            Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Upload", style = MaterialTheme.typography.labelMedium)
          }
          OutlinedButton(
            onClick = onCapturePortfolioPhoto,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
            modifier = Modifier.testTag("enroll_portfolio_capture_button"),
          ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Capture", style = MaterialTheme.typography.labelMedium)
          }
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
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
      Text(
        text = "Section C – Location & Service Area",
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onPrimaryContainer,
      )
      Text(
        text = "Set your city, area, live GPS pin, service radius & travel preference",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }

  var cityExpanded by remember { mutableStateOf(false) }
  ExposedDropdownMenuBox(
    expanded = cityExpanded,
    onExpandedChange = { cityExpanded = !cityExpanded },
  ) {
    OutlinedTextField(
      value = state.city,
      onValueChange = { state.city = it },
      label = { Text("Current City *") },
      placeholder = { Text("e.g. Karachi") },
      leadingIcon = { Icon(Icons.Rounded.LocationCity, contentDescription = null) },
      trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cityExpanded) },
      singleLine = true,
      shape = RoundedCornerShape(12.dp),
      colors = greenThemeTextFieldColors(highlighted = cityExpanded),
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
    label = { Text("Area / Town *") },
    placeholder = { Text("e.g. Gulshan-e-Iqbal") },
    singleLine = true,
    shape = RoundedCornerShape(12.dp),
    colors = greenThemeTextFieldColors(),
    modifier = Modifier.fillMaxWidth().testTag("enroll_area_input"),
  )

  val hasGpsPin = state.lat != 0.0 || state.lng != 0.0
  Surface(
    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f),
    shape = RoundedCornerShape(14.dp),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(
      modifier = Modifier.padding(12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = if (hasGpsPin) Icons.Rounded.CheckCircle else Icons.Rounded.MyLocation,
          contentDescription = null,
          tint = if (hasGpsPin) OnEmeraldContainer else MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
          Text("Current GPS Location *", style = MaterialTheme.typography.titleSmall)
          Text(
            text =
              if (hasGpsPin)
                "Pinned: ${"%.4f".format(state.lat)}, ${"%.4f".format(state.lng)}"
              else "Detect your current GPS location so nearby customers can find you",
            style = MaterialTheme.typography.bodySmall,
            color = if (hasGpsPin) OnEmeraldContainer else MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Button(
        onClick = {
          onRequestLocation()
          if (userCoordinates != null) {
            state.lat = userCoordinates.latitude
            state.lng = userCoordinates.longitude
            state.validationError = null
          }
        },
        shape = RoundedCornerShape(10.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier.fillMaxWidth().testTag("enroll_sync_gps_button"),
      ) {
        Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(if (hasGpsPin) "Refresh Current Location" else "Detect Current Location")
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
    Text("Preferred Working Areas (Multiple selection, Optional)", style = MaterialTheme.typography.titleSmall)
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
        shape = RoundedCornerShape(12.dp),
        colors = greenThemeTextFieldColors(),
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

  Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
    Text("Travel to Customer *", style = MaterialTheme.typography.titleSmall)
    Text(
      text =
        if (state.travelToCustomer) "Yes — willing to visit customer home or site"
        else "No — workshop / shop service only",
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      FilterChip(
        selected = state.travelToCustomer,
        onClick = { state.travelToCustomer = true },
        label = { Text("Yes") },
        modifier = Modifier.weight(1f).testTag("enroll_travel_yes"),
      )
      FilterChip(
        selected = !state.travelToCustomer,
        onClick = { state.travelToCustomer = false },
        label = { Text("No") },
        modifier = Modifier.weight(1f).testTag("enroll_travel_no"),
      )
    }
  }
}
