package com.example.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ImageEncodingHelper
import com.example.model.Coordinates
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.TechnicianProfile
import com.example.model.TechnicianRegistrationInput
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnEmeraldContainer
import kotlinx.coroutines.launch

@Composable
fun ProRegistrationWizard(
  existingTech: TechnicianProfile?,
  existingVerify: ProviderVerificationDoc?,
  categories: List<ServiceCategory>,
  userProfile: UserProfile?,
  userCoordinates: Coordinates?,
  isLiveBroadcastActive: Boolean,
  isBusy: Boolean,
  phoneOtpVerificationId: String?,
  phoneOtpStatusMessage: String?,
  phoneVerifiedSession: Boolean,
  onRequestLocation: () -> Unit,
  onStartPhoneOtp: (phone: String, activity: Activity) -> Unit,
  onConfirmPhoneOtp: (smsCode: String) -> Unit,
  onClearPhoneOtpState: () -> Unit,
  onSaveEnrollment: (TechnicianRegistrationInput) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val state =
    rememberProWizardState(
      existingTech = existingTech,
      existingVerify = existingVerify,
      categories = categories,
      userProfile = userProfile,
      userCoordinates = userCoordinates,
    )

  val isPhoneVerified = (existingTech?.phoneVerified == true) || phoneVerifiedSession
  val selectedCategory = ServiceCategories.byId(state.categoryId, categories)

  LaunchedEffect(userCoordinates) {
    if (userCoordinates != null && ((state.lat == 0.0 && state.lng == 0.0) || isLiveBroadcastActive)) {
      state.lat = userCoordinates.latitude
      state.lng = userCoordinates.longitude
    }
  }

  val profilePhotoPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val encoded = ImageEncodingHelper.encodeUriToCompactDataUrl(context, uri)
          if (encoded != null) state.profilePhotoData = encoded
          else state.validationError = "Could not compress selected photo. Try another image."
        }
      }
    }

  val portfolioPhotoPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null && state.portfolioImages.size < 4) {
        scope.launch {
          val encoded = ImageEncodingHelper.encodeUriToCompactDataUrl(context, uri)
          if (encoded != null) state.portfolioImages = state.portfolioImages + encoded
          else state.validationError = "Could not compress portfolio image."
        }
      }
    }

  val cnicFrontPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val encoded = ImageEncodingHelper.encodeUriToCompactDataUrl(context, uri)
          if (encoded != null) state.cnicPhotoData = encoded
          else state.validationError = "Could not compress CNIC front image."
        }
      }
    }

  val cnicBackPicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val encoded = ImageEncodingHelper.encodeUriToCompactDataUrl(context, uri)
          if (encoded != null) state.cnicBackPhotoData = encoded
          else state.validationError = "Could not compress CNIC back image."
        }
      }
    }

  val certificatePicker =
    rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
      if (uri != null) {
        scope.launch {
          val encoded = ImageEncodingHelper.encodeUriToCompactDataUrl(context, uri)
          if (encoded != null) state.certificatePhotoData = encoded
          else state.validationError = "Could not compress certificate image."
        }
      }
    }

  val imageRequest = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      WizardProgressHeader(
        isEditMode = existingTech != null,
        currentStep = state.currentStep,
        highestStepReached = state.highestStepReached,
        onSelectStep = { idx ->
          state.validationError = null
          state.currentStep = idx
        },
      )

      when (state.currentStep) {
        0 ->
          ProWizardStep1Personal(
            state = state,
            isPhoneVerified = isPhoneVerified,
            onPickProfilePhoto = { profilePhotoPicker.launch(imageRequest) },
            onOpenOtpDialog = {
              onClearPhoneOtpState()
              state.showOtpDialog = true
            },
          )
        1 ->
          ProWizardStep2Professional(
            state = state,
            categories = categories,
            selectedCategory = selectedCategory,
            onPickPortfolioPhoto = { portfolioPhotoPicker.launch(imageRequest) },
          )
        2 ->
          ProWizardStep3Location(
            state = state,
            userCoordinates = userCoordinates,
            onRequestLocation = onRequestLocation,
          )
        3 -> ProWizardStep4Charges(state = state)
        4 ->
          ProWizardStep5Documents(
            state = state,
            selectedCategory = selectedCategory,
            userProfile = userProfile,
            isPhoneVerified = isPhoneVerified,
            onPickCnicFront = { cnicFrontPicker.launch(imageRequest) },
            onPickCnicBack = { cnicBackPicker.launch(imageRequest) },
            onPickCertificate = { certificatePicker.launch(imageRequest) },
          )
      }

      state.validationError?.let { errorMsg ->
        Text(
          text = errorMsg,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall,
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        if (state.currentStep > 0) {
          OutlinedButton(
            onClick = {
              state.validationError = null
              state.currentStep -= 1
            },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).height(52.dp).testTag("wizard_back_button"),
          ) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Back")
          }
        }

        if (state.currentStep < 4) {
          Button(
            onClick = {
              val err = state.validateStep(state.currentStep)
              if (err != null) {
                state.validationError = err
              } else {
                state.validationError = null
                state.currentStep += 1
                if (state.currentStep > state.highestStepReached) {
                  state.highestStepReached = state.currentStep
                }
              }
            },
            shape = RoundedCornerShape(14.dp),
            modifier =
              Modifier.weight(if (state.currentStep > 0) 1.5f else 1f)
                .height(52.dp)
                .testTag("wizard_next_button"),
          ) {
            Text("Next: ${WizardStepTitles[state.currentStep + 1]}")
            Spacer(modifier = Modifier.width(6.dp))
            Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
          }
        } else {
          Button(
            onClick = {
              val invalid = state.firstInvalidStep()
              if (invalid != null) {
                state.validationError = state.validateStep(invalid)
                state.currentStep = invalid
              } else {
                state.validationError = null
                onSaveEnrollment(
                  state.toRegistrationInput(
                    selectedCategory = selectedCategory,
                    isPhoneVerified = isPhoneVerified,
                    userCoordinates = userCoordinates,
                  )
                )
              }
            },
            enabled = !isBusy,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1.5f).height(52.dp).testTag("save_enrollment_button"),
          ) {
            if (isBusy) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp,
              )
            } else {
              Text(
                text =
                  if (existingTech == null) "Submit Provider Registration"
                  else "Save Profile Changes",
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }

  if (state.showOtpDialog) {
    PhoneOtpDialog(
      phoneNumber = state.contactPhone,
      verificationId = phoneOtpVerificationId,
      statusMessage = phoneOtpStatusMessage,
      isBusy = isBusy,
      onSendOtp = {
        val activity = context as? Activity
        if (activity != null) {
          onStartPhoneOtp(state.contactPhone, activity)
        }
      },
      onVerifyCode = onConfirmPhoneOtp,
      onDismiss = { state.showOtpDialog = false },
    )
  }
}

@Composable
private fun WizardProgressHeader(
  isEditMode: Boolean,
  currentStep: Int,
  highestStepReached: Int,
  onSelectStep: (Int) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = if (!isEditMode) "Professional Registration" else "Update Provider Profile",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
      Surface(shape = RoundedCornerShape(50), color = EmeraldContainer) {
        Text(
          text = "Step ${currentStep + 1} of 5",
          style = MaterialTheme.typography.labelMedium,
          color = OnEmeraldContainer,
          fontWeight = FontWeight.SemiBold,
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
        )
      }
    }
    Text(
      text = WizardStepTitles[currentStep],
      style = MaterialTheme.typography.labelLarge,
      color = MaterialTheme.colorScheme.primary,
    )
    LinearProgressIndicator(
      progress = { (currentStep + 1) / 5f },
      modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
    )
    Row(
      modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
      WizardStepTitles.forEachIndexed { idx, label ->
        val canJump = isEditMode || idx <= highestStepReached
        FilterChip(
          selected = currentStep == idx,
          enabled = canJump,
          onClick = { if (canJump) onSelectStep(idx) },
          label = { Text("${idx + 1}. $label", style = MaterialTheme.typography.labelSmall) },
          modifier = Modifier.testTag("wizard_step_${idx + 1}"),
        )
      }
    }
  }
}
