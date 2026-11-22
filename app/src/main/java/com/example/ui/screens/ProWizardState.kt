package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.model.Coordinates
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.TechnicianProfile
import com.example.model.TechnicianRegistrationInput
import com.example.model.UserProfile
import kotlin.math.roundToLong

val WizardStepTitles =
  listOf(
    "Personal Info",
    "Professional Details",
    "Location & Area",
    "Charges & Schedule",
    "Documents & Submit",
  )

val GenderOptions = listOf("Male", "Female", "Prefer not to say")

val QualificationPresets =
  listOf(
    "TEVTA / NAVTTC Certified",
    "DAE Diploma",
    "Technical Trade Course",
    "B.E. / BS Engineering",
    "Matric / Practical Master",
  )

val PakistanCities =
  listOf(
    "Karachi",
    "Lahore",
    "Islamabad",
    "Rawalpindi",
    "Faisalabad",
    "Multan",
    "Peshawar",
    "Quetta",
    "Hyderabad",
    "Sialkot",
    "Gujranwala",
  )

val SuggestedAreasByCity =
  mapOf(
    "Karachi" to listOf("Gulshan-e-Iqbal", "DHA", "Clifton", "PECHS", "Johar", "North Nazimabad", "Saddar"),
    "Lahore" to listOf("DHA", "Gulberg", "Model Town", "Johar Town", "Bahria Town", "Wapda Town"),
    "Islamabad" to listOf("F-6 / F-7", "F-10 / F-11", "G-9 / G-11", "E-11", "DHA / Bahria"),
    "Rawalpindi" to listOf("Saddar", "Chaklala", "Satellite Town", "Bahria Town", "Peshawar Road"),
  )

val AllWeekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

val WorkingHourPresets =
  listOf(
    "08:00 AM - 08:00 PM",
    "09:00 AM - 06:00 PM",
    "10:00 AM - 10:00 PM",
    "24 Hours Available",
  )

@Stable
class ProWizardState(
  existingTech: TechnicianProfile?,
  existingVerify: ProviderVerificationDoc?,
  categories: List<ServiceCategory>,
  userProfile: UserProfile?,
  userCoordinates: Coordinates?,
) {
  var currentStep by mutableIntStateOf(0)
  var highestStepReached by mutableIntStateOf(if (existingTech != null) 4 else 0)
  var showOtpDialog by mutableStateOf(false)
  var validationError by mutableStateOf<String?>(null)

  // Step 1: Personal Information
  var fullName by mutableStateOf(existingTech?.fullName ?: userProfile?.displayName ?: "")
  var contactPhone by mutableStateOf(existingTech?.contactPhone ?: userProfile?.phoneNumber ?: "")
  var email by mutableStateOf(
    existingTech?.email?.ifBlank { userProfile?.email ?: "" } ?: userProfile?.email ?: ""
  )
  var gender by mutableStateOf(existingTech?.gender ?: "")
  var profilePhotoData by mutableStateOf(existingTech?.profilePhotoData ?: "")
  var cnicNumber by mutableStateOf(existingVerify?.cnicNumber ?: "")

  // Step 2: Professional Details
  var categoryId by mutableStateOf(
    ServiceCategories.byId(existingTech?.categoryId ?: "electrician", categories).id
  )
  var additionalCategoryIds by mutableStateOf(existingTech?.additionalCategoryIds ?: emptyList())
  var subcategory by mutableStateOf(existingTech?.subcategory ?: "")
  var professionalTitle by mutableStateOf(existingTech?.professionalTitle ?: "")
  var expertise by mutableStateOf(existingTech?.expertise ?: "")
  var yearsText by mutableStateOf((existingTech?.yearsExperience ?: 3L).toString())
  var qualification by mutableStateOf(existingTech?.qualification ?: "")
  var bio by mutableStateOf(existingTech?.bio ?: "")
  var portfolioImages by mutableStateOf(existingTech?.portfolioImages ?: emptyList())

  // Step 3: Location & Service Area
  var city by mutableStateOf(existingTech?.city?.ifBlank { "Karachi" } ?: "Karachi")
  var workingArea by mutableStateOf(
    existingTech?.workingArea ?: userProfile?.defaultArea ?: ""
  )
  var radiusKm by mutableFloatStateOf((existingTech?.serviceRadiusKm ?: 15L).toFloat())
  var preferredAreas by mutableStateOf(existingTech?.preferredAreas ?: emptyList())
  var customAreaInput by mutableStateOf("")
  var travelToCustomer by mutableStateOf(existingTech?.travelToCustomer ?: true)
  var lat by mutableDoubleStateOf(existingTech?.latitude ?: userCoordinates?.latitude ?: 0.0)
  var lng by mutableDoubleStateOf(existingTech?.longitude ?: userCoordinates?.longitude ?: 0.0)

  // Step 4: Charges & Availability
  var visitCharge by mutableStateOf(existingTech?.visitCharge ?: "")
  var hourlyCharge by mutableStateOf(existingTech?.hourlyCharge ?: "")
  var dailyCharge by mutableStateOf(existingTech?.dailyCharge ?: "")
  var serviceChargeNote by mutableStateOf(existingTech?.serviceChargeNote ?: "")
  var workingDays by mutableStateOf(
    existingTech?.workingDays ?: listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
  )
  var availabilityHours by mutableStateOf(
    existingTech?.availabilityHours ?: "08:00 AM - 08:00 PM"
  )
  var emergencyAvailable by mutableStateOf(existingTech?.emergencyAvailable ?: false)
  var availableNow by mutableStateOf(existingTech?.availableNow ?: true)
  var listed by mutableStateOf(existingTech?.listed ?: true)

  // Step 5: Documents & Consent
  var cnicPhotoData by mutableStateOf(existingVerify?.cnicPhotoData ?: "")
  var cnicBackPhotoData by mutableStateOf(existingVerify?.cnicBackPhotoData ?: "")
  var certificatePhotoData by mutableStateOf(existingVerify?.certificatePhotoData ?: "")
  var termsAccepted by mutableStateOf(existingTech?.termsAccepted ?: false)
  var verificationConsent by mutableStateOf(existingTech?.verificationConsent ?: false)

  fun validateStep(step: Int): String? =
    when (step) {
      0 ->
        when {
          fullName.trim().length < 2 -> "Enter your full name (at least 2 characters)."
          contactPhone.trim().length < 5 -> "Enter a valid mobile number (e.g. +92300...)."
          else -> null
        }
      1 ->
        when {
          expertise.trim().length < 2 -> "Select or enter your technical skills & expertise."
          yearsText.toLongOrNull() == null -> "Enter your years of experience."
          else -> null
        }
      2 ->
        when {
          city.trim().length < 2 -> "Select or enter your current city."
          workingArea.trim().length < 2 -> "Enter your working area or town."
          else -> null
        }
      3 ->
        when {
          visitCharge.trim().isEmpty() -> "Enter your visiting fee (e.g. Rs. 500 or Free Visit)."
          workingDays.isEmpty() -> "Select at least one working day."
          availabilityHours.trim().length < 2 -> "Enter your working hours."
          else -> null
        }
      4 ->
        when {
          !termsAccepted -> "Please accept Terms & Conditions to register."
          !verificationConsent -> "Please confirm identity verification consent."
          else -> null
        }
      else -> null
    }

  fun firstInvalidStep(): Int? {
    for (s in 0..4) {
      if (validateStep(s) != null) return s
    }
    return null
  }

  fun toRegistrationInput(
    selectedCategory: ServiceCategory,
    isPhoneVerified: Boolean,
    userCoordinates: Coordinates?,
  ): TechnicianRegistrationInput {
    val finalLat = if (lat != 0.0) lat else (userCoordinates?.latitude ?: 0.0)
    val finalLng = if (lng != 0.0) lng else (userCoordinates?.longitude ?: 0.0)
    return TechnicianRegistrationInput(
      fullName = fullName,
      email = email,
      gender = gender,
      categoryId = categoryId,
      additionalCategoryIds = additionalCategoryIds,
      subcategory = subcategory,
      professionalTitle = professionalTitle.trim().ifBlank { selectedCategory.name },
      expertise = expertise,
      bio = bio,
      qualification = qualification,
      yearsExperience = (yearsText.toLongOrNull() ?: 1L).coerceIn(0L, 70L),
      contactPhone = contactPhone,
      phoneVerified = isPhoneVerified,
      visitCharge = visitCharge,
      hourlyCharge = hourlyCharge,
      dailyCharge = dailyCharge,
      serviceChargeNote = serviceChargeNote,
      workingDays = workingDays,
      availabilityHours = availabilityHours,
      emergencyAvailable = emergencyAvailable,
      city = city,
      workingArea = workingArea,
      preferredAreas = preferredAreas,
      travelToCustomer = travelToCustomer,
      serviceRadiusKm = radiusKm.roundToLong().coerceIn(1L, 200L),
      availableNow = availableNow,
      listed = listed,
      termsAccepted = termsAccepted,
      verificationConsent = verificationConsent,
      profilePhotoData = profilePhotoData,
      portfolioImages = portfolioImages,
      cnicNumber = cnicNumber,
      cnicPhotoData = cnicPhotoData,
      cnicBackPhotoData = cnicBackPhotoData,
      certificatePhotoData = certificatePhotoData,
      latitude = finalLat,
      longitude = finalLng,
    )
  }
}

@Composable
fun rememberProWizardState(
  existingTech: TechnicianProfile?,
  existingVerify: ProviderVerificationDoc?,
  categories: List<ServiceCategory>,
  userProfile: UserProfile?,
  userCoordinates: Coordinates?,
): ProWizardState {
  val state =
    remember(existingTech?.userId, existingVerify?.userId) {
      ProWizardState(
        existingTech = existingTech,
        existingVerify = existingVerify,
        categories = categories,
        userProfile = userProfile,
        userCoordinates = userCoordinates,
      )
    }
  return state
}
