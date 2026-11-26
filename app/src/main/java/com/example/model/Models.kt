package com.example.model

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.Park
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.SolarPower
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class ServiceCategory(
  val id: String,
  val name: String,
  val subtitle: String,
  val icon: ImageVector,
  val tint: Color,
  val subcategories: List<String> = emptyList(),
  val active: Boolean = true,
  val sortOrder: Int = 0,
  val containerBg: Color = Color(0xFFDCFCE7),
)

@Keep
@IgnoreExtraProperties
data class ServiceCategoryEntity(
  @DocumentId val id: String = "",
  val categoryId: String = "",
  val name: String = "",
  val subtitle: String = "",
  val iconKey: String = "build",
  val subcategories: List<String> = emptyList(),
  val active: Boolean = true,
  val sortOrder: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

object ServiceCategories {
  val defaults: List<ServiceCategory> =
    listOf(
      ServiceCategory(
        id = "home_technical",
        name = "Home & Technical Services",
        subtitle = "Electrician, Plumber, Carpenter, AC, Welder, Generator & Lift",
        icon = Icons.Rounded.Handyman,
        tint = Color(0xFFD97706),
        containerBg = Color(0xFFFEF3C7),
        subcategories =
          listOf(
            "Electrician",
            "Plumber",
            "Carpenter",
            "Painter",
            "AC Technician",
            "Appliance Repair Technician",
            "Welder",
            "Generator Technician",
            "Lift Technician",
          ),
        sortOrder = 1,
      ),
      ServiceCategory(
        id = "solar_electrical",
        name = "Solar & Electrical Systems",
        subtitle = "Solar Panels, Installation, UPS & Hybrid Inverters",
        icon = Icons.Rounded.SolarPower,
        tint = Color(0xFFCA8A04),
        containerBg = Color(0xFFFEF9C3),
        subcategories =
          listOf(
            "Solar Technician",
            "Solar Installer",
            "UPS Technician",
            "Inverter Technician",
          ),
        sortOrder = 2,
      ),
      ServiceCategory(
        id = "it_security",
        name = "IT & Security Services",
        subtitle = "IT Support, Networking, CCTV, Computer & Mobile Repair",
        icon = Icons.Rounded.LaptopMac,
        tint = Color(0xFF7C3AED),
        containerBg = Color(0xFFEDE9FE),
        subcategories =
          listOf(
            "IT Support Expert",
            "Network Technician",
            "CCTV Technician",
            "Computer Repair Technician",
            "Mobile Repair Technician",
            "Software / Application Support",
          ),
        sortOrder = 3,
      ),
      ServiceCategory(
        id = "construction_engineering",
        name = "Construction & Engineering",
        subtitle = "Civil Engineer, Architect, Contractor, Mason, Tile & Steel Fixer",
        icon = Icons.Rounded.Construction,
        tint = Color(0xFFDC2626),
        containerBg = Color(0xFFFEE2E2),
        subcategories =
          listOf(
            "Civil Engineer",
            "Architect",
            "Construction Contractor",
            "Mason / Bricklayer",
            "Tile Fixer",
            "Steel Fixer",
            "General Labour",
            "Construction Worker",
          ),
        sortOrder = 4,
      ),
      ServiceCategory(
        id = "education_tutors",
        name = "Education & Tutors",
        subtitle = "Home Tutor, School, Quran, English, Computer & Online Tuition",
        icon = Icons.Rounded.School,
        tint = Color(0xFFC026D3),
        containerBg = Color(0xFFFAE8FF),
        subcategories =
          listOf(
            "Home Tutor",
            "School Teacher",
            "Quran Teacher",
            "English Language Tutor",
            "Computer Teacher",
            "Online Tutor",
            "Academy / Coaching Teacher",
          ),
        sortOrder = 5,
      ),
      ServiceCategory(
        id = "gardening_outdoor",
        name = "Gardening & Outdoor Services",
        subtitle = "Gardener, Landscaping, Plant Care, Lawn & Tree Trimming",
        icon = Icons.Rounded.Park,
        tint = Color(0xFF16A34A),
        containerBg = Color(0xFFDCFCE7),
        subcategories =
          listOf(
            "Gardener",
            "Landscaping Expert",
            "Plant Care Specialist",
            "Lawn Maintenance",
            "Tree Trimming",
          ),
        sortOrder = 6,
      ),
      ServiceCategory(
        id = "shops_suppliers",
        name = "Shops & Local Suppliers",
        subtitle = "Mineral Water Shops, Delivery, Tank Cleaning & RO Filter Tech",
        icon = Icons.Rounded.WaterDrop,
        tint = Color(0xFF0284C7),
        containerBg = Color(0xFFE0F2FE),
        subcategories =
          listOf(
            "Mineral Water Shop",
            "Mineral Water Delivery",
            "Drinking Water Supplier",
            "Water Tank Cleaning",
            "Water Filter / RO Technician",
          ),
        sortOrder = 7,
      ),
      ServiceCategory(
        id = "automotive_services",
        name = "Automotive Services",
        subtitle = "Car & Bike Mechanics, Auto Electrician, Car AC & Diagnostics",
        icon = Icons.Rounded.DirectionsCar,
        tint = Color(0xFFEA580C),
        containerBg = Color(0xFFFFEDD5),
        subcategories =
          listOf(
            "Car Mechanic",
            "Motorcycle Mechanic",
            "Auto Electrician",
            "Car AC Technician",
            "Vehicle Diagnostic Expert",
          ),
        sortOrder = 8,
      ),
    )

  private val LEGACY_GROUP_MAP =
    mapOf(
      "electrician" to "home_technical",
      "electrical" to "home_technical",
      "plumber" to "home_technical",
      "plumbing" to "home_technical",
      "ac_technician" to "home_technical",
      "ac_tech" to "home_technical",
      "hvac" to "home_technical",
      "carpenter" to "home_technical",
      "carpentry" to "home_technical",
      "painter" to "home_technical",
      "painting" to "home_technical",
      "welder" to "home_technical",
      "generator_technician" to "home_technical",
      "lift_technician" to "home_technical",
      "appliance_repair" to "home_technical",
      "appliance" to "home_technical",
      "cleaner" to "home_technical",
      "pest" to "home_technical",
      "tailor" to "home_technical",
      "beauty" to "home_technical",
      "photography" to "home_technical",
      "solar_technician" to "solar_electrical",
      "solar_installer" to "solar_electrical",
      "solar" to "solar_electrical",
      "ups_technician" to "solar_electrical",
      "inverter_technician" to "solar_electrical",
      "it_expert" to "it_security",
      "it_tech" to "it_security",
      "it_network" to "it_security",
      "network_technician" to "it_security",
      "cctv_expert" to "it_security",
      "cctv_installer" to "it_security",
      "cctv" to "it_security",
      "mobile_repair" to "it_security",
      "civil_engineer" to "construction_engineering",
      "architect" to "construction_engineering",
      "construction_worker" to "construction_engineering",
      "general_labour" to "construction_engineering",
      "handyman" to "construction_engineering",
      "mason" to "construction_engineering",
      "tutor" to "education_tutors",
      "gardener" to "gardening_outdoor",
      "water_supplier" to "shops_suppliers",
      "mechanic" to "automotive_services",
      "auto" to "automotive_services",
      "driver" to "automotive_services",
      "mover" to "automotive_services",
    )

  private val LEGACY_SUBCATEGORY_MAP =
    mapOf(
      "electrician" to "Electrician",
      "electrical" to "Electrician",
      "plumber" to "Plumber",
      "plumbing" to "Plumber",
      "ac_technician" to "AC Technician",
      "ac_tech" to "AC Technician",
      "hvac" to "AC Technician",
      "carpenter" to "Carpenter",
      "carpentry" to "Carpenter",
      "painter" to "Painter",
      "painting" to "Painter",
      "welder" to "Welder",
      "generator_technician" to "Generator Technician",
      "lift_technician" to "Lift Technician",
      "appliance_repair" to "Appliance Repair Technician",
      "appliance" to "Appliance Repair Technician",
      "solar_technician" to "Solar Technician",
      "solar_installer" to "Solar Installer",
      "solar" to "Solar Technician",
      "it_expert" to "IT Support Expert",
      "it_tech" to "IT Support Expert",
      "it_network" to "Network Technician",
      "network_technician" to "Network Technician",
      "cctv_expert" to "CCTV Technician",
      "cctv_installer" to "CCTV Technician",
      "cctv" to "CCTV Technician",
      "mobile_repair" to "Mobile Repair Technician",
      "civil_engineer" to "Civil Engineer",
      "architect" to "Architect",
      "construction_worker" to "Construction Worker",
      "general_labour" to "General Labour",
      "mason" to "Mason / Bricklayer",
      "tutor" to "Home Tutor",
      "gardener" to "Gardener",
      "water_supplier" to "Drinking Water Supplier",
      "mechanic" to "Car Mechanic",
      "auto" to "Car Mechanic",
    )

  fun normalizeGroupId(rawId: String): String {
    val clean = rawId.trim().lowercase()
    if (defaults.any { it.id == clean }) return clean
    return LEGACY_GROUP_MAP[clean] ?: clean.ifBlank { "home_technical" }
  }

  fun legacySubcategoryForId(rawId: String?): String? {
    val clean = rawId?.trim()?.lowercase().orEmpty()
    if (clean.isEmpty()) return null
    return LEGACY_SUBCATEGORY_MAP[clean]
  }

  fun iconForKey(key: String): ImageVector =
    when (normalizeGroupId(key)) {
      "home_technical" -> Icons.Rounded.Handyman
      "solar_electrical" -> Icons.Rounded.SolarPower
      "it_security" -> Icons.Rounded.LaptopMac
      "construction_engineering" -> Icons.Rounded.Construction
      "education_tutors" -> Icons.Rounded.School
      "gardening_outdoor" -> Icons.Rounded.Park
      "shops_suppliers" -> Icons.Rounded.WaterDrop
      "automotive_services" -> Icons.Rounded.DirectionsCar
      else -> Icons.Rounded.Build
    }

  fun tintForId(id: String): Color {
    val normalized = normalizeGroupId(id)
    return defaults.firstOrNull { it.id == normalized }?.tint ?: Color(0xFF0F4C5C)
  }

  fun bgForId(id: String): Color {
    val normalized = normalizeGroupId(id)
    return defaults.firstOrNull { it.id == normalized }?.containerBg ?: Color(0xFFDCFCE7)
  }

  fun mergeWithRemote(remote: List<ServiceCategoryEntity>, includeInactive: Boolean = false): List<ServiceCategory> {
    val defaultsById = defaults.associateBy { it.id }
    val byId = LinkedHashMap<String, ServiceCategory>()
    defaults.forEach { byId[it.id] = it }

    remote.forEach { entity ->
      val rawId = entity.categoryId.ifBlank { entity.id }.trim().lowercase()
      if (rawId.isEmpty()) return@forEach
      // Skip legacy flat categories still lingering in Firestore so only the 8 main groups (plus custom groups) render
      if (rawId in LEGACY_GROUP_MAP.keys) return@forEach

      val base = defaultsById[rawId]
      byId[rawId] =
        ServiceCategory(
          id = rawId,
          name = entity.name.ifBlank { base?.name ?: rawId },
          subtitle = entity.subtitle.ifBlank { base?.subtitle.orEmpty() },
          icon = base?.icon ?: iconForKey(entity.iconKey.ifBlank { rawId }),
          tint = base?.tint ?: tintForId(rawId),
          subcategories =
            entity.subcategories.ifEmpty { base?.subcategories.orEmpty() },
          active = entity.active,
          sortOrder = if (entity.sortOrder > 0L) entity.sortOrder.toInt() else (base?.sortOrder ?: 99),
          containerBg = base?.containerBg ?: bgForId(rawId),
        )
    }

    return byId.values
      .filter { includeInactive || it.active }
      .sortedWith(compareBy<ServiceCategory> { it.sortOrder }.thenBy { it.name })
  }

  fun byId(id: String, categories: List<ServiceCategory> = defaults): ServiceCategory {
    val clean = id.trim().lowercase()
    val normalized = normalizeGroupId(clean)
    return categories.firstOrNull { it.id == clean }
      ?: categories.firstOrNull { it.id == normalized }
      ?: defaults.firstOrNull { it.id == normalized }
      ?: defaults.first()
  }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val displayName: String = "",
  val email: String = "",
  val phoneNumber: String = "",
  val defaultArea: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val role: String = "CUSTOMER",
  val enrolledTechnician: Boolean = false,
  val suspended: Boolean = false,
  val savedTechnicianIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class VerificationStatus(val label: String) {
  PENDING("Pending Review"),
  APPROVED("Approved"),
  REJECTED("Rejected");

  companion object {
    fun fromRaw(raw: String): VerificationStatus =
      entries.firstOrNull { it.name == raw } ?: PENDING
  }
}

@Keep
@IgnoreExtraProperties
data class TechnicianProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val fullName: String = "",
  val email: String = "",
  val gender: String = "",
  val categoryId: String = "home_technical",
  val additionalCategoryIds: List<String> = emptyList(),
  val subcategory: String = "",
  val professionalTitle: String = "",
  val expertise: String = "",
  val bio: String = "",
  val qualification: String = "",
  val yearsExperience: Long = 1L,
  val contactPhone: String = "",
  val phoneVerified: Boolean = false,
  val visitCharge: String = "",
  val hourlyCharge: String = "",
  val dailyCharge: String = "",
  val serviceChargeNote: String = "",
  val workingDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
  val availabilityHours: String = "09:00 AM - 07:00 PM",
  val emergencyAvailable: Boolean = false,
  val city: String = "Karachi",
  val workingArea: String = "",
  val preferredAreas: List<String> = emptyList(),
  val travelToCustomer: Boolean = true,
  val serviceRadiusKm: Long = 10L,
  val availableNow: Boolean = true,
  val listed: Boolean = true,
  val termsAccepted: Boolean = true,
  val verificationConsent: Boolean = true,
  val verificationStatus: String = VerificationStatus.PENDING.name,
  val verifiedBadge: Boolean = false,
  val adminRemarks: String = "",
  val profilePhotoData: String = "",
  val portfolioImages: List<String> = emptyList(),
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
  val ratingSum: Long = 0L,
  val reviewCount: Long = 0L,
  val averageRating: Double = 0.0,
  val completedJobs: Long = 0L,
  val lastReviewBookingId: String? = null,
  val lastCompletedBookingId: String? = null,
  @ServerTimestamp val locationUpdatedAt: Timestamp? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val verificationEnum: VerificationStatus
    get() = VerificationStatus.fromRaw(verificationStatus)
}

@Keep
@IgnoreExtraProperties
data class ProviderVerificationDoc(
  @DocumentId val id: String = "",
  val userId: String = "",
  val cnicNumber: String = "",
  val cnicPhotoData: String = "",
  val cnicBackPhotoData: String = "",
  val certificatePhotoData: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
)

enum class BookingStatus(val label: String) {
  REQUESTED("Requested"),
  ACCEPTED("Accepted"),
  EN_ROUTE("On the Way"),
  IN_PROGRESS("Work Started"),
  COMPLETED("Completed"),
  CANCELLED("Cancelled");

  companion object {
    fun fromRaw(raw: String): BookingStatus =
      entries.firstOrNull { it.name == raw } ?: REQUESTED
  }
}

@Keep
@IgnoreExtraProperties
data class ServiceBooking(
  @DocumentId val id: String = "",
  val clientId: String = "",
  val clientName: String = "",
  val clientPhone: String = "",
  val technicianId: String = "",
  val technicianName: String = "",
  val technicianPhone: String = "",
  val categoryId: String = "home_technical",
  val serviceTitle: String = "",
  val issueDescription: String = "",
  val serviceArea: String = "",
  val preferredTime: String = "",
  val clientLat: Double = 0.0,
  val clientLng: Double = 0.0,
  val status: String = BookingStatus.REQUESTED.name,
  val reviewed: Boolean = false,
  val participantIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val bookingStatus: BookingStatus
    get() = BookingStatus.fromRaw(status)
}

@Keep
@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val bookingId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  val participantIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
)

@Keep
@IgnoreExtraProperties
data class ServiceReview(
  @DocumentId val id: String = "",
  val bookingId: String = "",
  val technicianId: String = "",
  val clientId: String = "",
  val clientName: String = "",
  val rating: Long = 5L,
  val comment: String = "",
  val highlights: List<String> = emptyList(),
  val approved: Boolean = true,
  @ServerTimestamp val createdAt: Timestamp? = null,
)

enum class ComplaintStatus(val label: String) {
  OPEN("Open"),
  INVESTIGATING("Investigating"),
  RESOLVED("Resolved");

  companion object {
    fun fromRaw(raw: String): ComplaintStatus =
      entries.firstOrNull { it.name == raw } ?: OPEN
  }
}

@Keep
@IgnoreExtraProperties
data class ServiceComplaint(
  @DocumentId val id: String = "",
  val bookingId: String = "",
  val reporterId: String = "",
  val reporterName: String = "",
  val reportedTechId: String = "",
  val reportedTechName: String = "",
  val reason: String = "",
  val description: String = "",
  val status: String = ComplaintStatus.OPEN.name,
  val adminResolution: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null,
) {
  val statusEnum: ComplaintStatus
    get() = ComplaintStatus.fromRaw(status)
}

sealed interface UiState<out T> {
  data object Loading : UiState<Nothing>

  data class Success<T>(val data: T) : UiState<T>

  data class Error(val message: String) : UiState<Nothing>
}

data class Coordinates(
  val latitude: Double,
  val longitude: Double,
  val accuracyMeters: Float? = null,
  val isDeviceGps: Boolean = false,
)

data class TechnicianRegistrationInput(
  val fullName: String,
  val email: String = "",
  val gender: String = "",
  val categoryId: String,
  val additionalCategoryIds: List<String> = emptyList(),
  val subcategory: String = "",
  val professionalTitle: String = "",
  val expertise: String,
  val bio: String = "",
  val qualification: String = "",
  val yearsExperience: Long,
  val contactPhone: String,
  val phoneVerified: Boolean = false,
  val visitCharge: String = "",
  val hourlyCharge: String = "",
  val dailyCharge: String = "",
  val serviceChargeNote: String = "",
  val workingDays: List<String> = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat"),
  val availabilityHours: String = "09:00 AM - 07:00 PM",
  val emergencyAvailable: Boolean = false,
  val city: String = "Karachi",
  val workingArea: String,
  val preferredAreas: List<String> = emptyList(),
  val travelToCustomer: Boolean = true,
  val serviceRadiusKm: Long = 10L,
  val availableNow: Boolean = true,
  val listed: Boolean = true,
  val termsAccepted: Boolean = true,
  val verificationConsent: Boolean = true,
  val profilePhotoData: String = "",
  val portfolioImages: List<String> = emptyList(),
  val cnicNumber: String = "",
  val cnicPhotoData: String = "",
  val cnicBackPhotoData: String = "",
  val certificatePhotoData: String = "",
  val latitude: Double = 0.0,
  val longitude: Double = 0.0,
)
