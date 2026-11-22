package com.example.model

import androidx.annotation.Keep
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Architecture
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.Carpenter
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.ElectricalServices
import androidx.compose.material.icons.rounded.Elevator
import androidx.compose.material.icons.rounded.Engineering
import androidx.compose.material.icons.rounded.FormatPaint
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Hardware
import androidx.compose.material.icons.rounded.Kitchen
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Plumbing
import androidx.compose.material.icons.rounded.Power
import androidx.compose.material.icons.rounded.Router
import androidx.compose.material.icons.rounded.SolarPower
import androidx.compose.material.icons.rounded.Videocam
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
        id = "electrician",
        name = "Electrician",
        subtitle = "Wiring, breaker panels, UPS & lighting",
        icon = Icons.Rounded.Bolt,
        tint = Color(0xFFFFB800),
        subcategories = listOf("Home Wiring", "Breaker Panel", "UPS & Inverter", "Smart Switches"),
        sortOrder = 1,
      ),
      ServiceCategory(
        id = "plumber",
        name = "Plumber",
        subtitle = "Leaks, pipes, drains & water motors",
        icon = Icons.Rounded.Plumbing,
        tint = Color(0xFF2196F3),
        subcategories = listOf("Pipe Leak", "Drain Unclog", "Water Pump", "Bathroom Fitting"),
        sortOrder = 2,
      ),
      ServiceCategory(
        id = "ac_technician",
        name = "AC Technician",
        subtitle = "Split/inverter AC service, gas & PCB",
        icon = Icons.Rounded.AcUnit,
        tint = Color(0xFF00B0FF),
        subcategories = listOf("AC Servicing", "Gas Refill", "Installation", "PCB Repair"),
        sortOrder = 3,
      ),
      ServiceCategory(
        id = "mechanic",
        name = "Mechanic",
        subtitle = "Engine diagnostics, brakes & roadside help",
        icon = Icons.Rounded.DirectionsCar,
        tint = Color(0xFF2962FF),
        subcategories = listOf("Engine Tune-Up", "Battery Jumpstart", "Brakes & Clutch", "AC Car"),
        sortOrder = 4,
      ),
      ServiceCategory(
        id = "carpenter",
        name = "Carpenter",
        subtitle = "Doors, cabinets, locks & custom wood",
        icon = Icons.Rounded.Carpenter,
        tint = Color(0xFFFF7A00),
        subcategories = listOf("Door & Hinge", "Kitchen Cabinets", "Furniture Fix", "Wood Polish"),
        sortOrder = 5,
      ),
      ServiceCategory(
        id = "it_expert",
        name = "IT Expert",
        subtitle = "Laptops, PCs, OS setup & data recovery",
        icon = Icons.Rounded.LaptopMac,
        tint = Color(0xFF3B82F6),
        subcategories = listOf("Laptop Repair", "OS & Software", "Data Recovery", "Office Setup"),
        sortOrder = 6,
      ),
      ServiceCategory(
        id = "solar_technician",
        name = "Solar Technician",
        subtitle = "Inverter faults, batteries & MPPT tuning",
        icon = Icons.Rounded.SolarPower,
        tint = Color(0xFFFF6D00),
        subcategories = listOf("Inverter Fault", "Battery Bank", "Panel Cleaning", "Wiring Audit"),
        sortOrder = 7,
      ),
      ServiceCategory(
        id = "cctv_expert",
        name = "CCTV Expert",
        subtitle = "DVR/NVR config, mobile view & repair",
        icon = Icons.Rounded.Videocam,
        tint = Color(0xFF00BFA5),
        subcategories = listOf("DVR/NVR Config", "Mobile App View", "Faulty Camera", "Night Vision"),
        sortOrder = 8,
      ),
      ServiceCategory(
        id = "mobile_repair",
        name = "Mobile Repair",
        subtitle = "Screens, batteries, charging & board fix",
        icon = Icons.Rounded.PhoneAndroid,
        tint = Color(0xFF00BCD4),
        subcategories = listOf("Screen Replacement", "Battery Swap", "Charging Port", "Water Damage"),
        sortOrder = 9,
      ),
      ServiceCategory(
        id = "painter",
        name = "Painter",
        subtitle = "Interior/exterior walls & waterproofing",
        icon = Icons.Rounded.FormatPaint,
        tint = Color(0xFFF50057),
        subcategories = listOf("Interior Paint", "Exterior Coat", "Waterproofing", "Texture Wall"),
        sortOrder = 10,
      ),
      ServiceCategory(
        id = "welder",
        name = "Welder",
        subtitle = "Gates, grills, steel & aluminum fabrication",
        icon = Icons.Rounded.Hardware,
        tint = Color(0xFFAA00FF),
        subcategories = listOf("Gate Repair", "Safety Grills", "Stainless Steel", "Roof Shed"),
        sortOrder = 11,
      ),
      ServiceCategory(
        id = "generator_technician",
        name = "Generator Technician",
        subtitle = "Diesel/gas generators, ATS & alternators",
        icon = Icons.Rounded.Power,
        tint = Color(0xFFFF3D00),
        subcategories = listOf("Oil & Filter Service", "ATS Panel", "Self-Starter", "Alternator"),
        sortOrder = 12,
      ),
      ServiceCategory(
        id = "solar_installer",
        name = "Solar Installer",
        subtitle = "Rooftop mounting, hybrid & on-grid systems",
        icon = Icons.Rounded.SolarPower,
        tint = Color(0xFFFF9100),
        subcategories = listOf("Hybrid System", "On-Grid Setup", "Rooftop Structure", "Earthing"),
        sortOrder = 13,
      ),
      ServiceCategory(
        id = "lift_technician",
        name = "Lift Technician",
        subtitle = "Passenger & cargo elevator servicing",
        icon = Icons.Rounded.Elevator,
        tint = Color(0xFF00ACC1),
        subcategories = listOf("Routine Service", "Door Sensor", "Control Board", "Emergency Call"),
        sortOrder = 14,
      ),
      ServiceCategory(
        id = "civil_engineer",
        name = "Civil Engineer",
        subtitle = "Structural check, supervision & BOQ",
        icon = Icons.Rounded.Engineering,
        tint = Color(0xFF10B981),
        subcategories = listOf("Site Inspection", "Structural Audit", "BOQ Estimation", "Grey Structure"),
        sortOrder = 15,
      ),
      ServiceCategory(
        id = "architect",
        name = "Architect",
        subtitle = "Floor plans, 3D elevation & interiors",
        icon = Icons.Rounded.Architecture,
        tint = Color(0xFF6366F1),
        subcategories = listOf("2D Floor Plan", "3D Elevation", "Interior Design", "Renovation Plan"),
        sortOrder = 16,
      ),
      ServiceCategory(
        id = "cctv_installer",
        name = "CCTV Installer",
        subtitle = "New IP/HD camera cabling & coverage",
        icon = Icons.Rounded.Videocam,
        tint = Color(0xFF0EA5E9),
        subcategories = listOf("Home 4-Cam Kit", "PoE IP Cameras", "Office Coverage", "Outdoor Pole"),
        sortOrder = 17,
      ),
      ServiceCategory(
        id = "network_technician",
        name = "Network Technician",
        subtitle = "Wi-Fi mesh, fiber, switches & LAN cabling",
        icon = Icons.Rounded.Router,
        tint = Color(0xFF2563EB),
        subcategories = listOf("Wi-Fi Mesh", "Cat6 Cabling", "Router Config", "Server Rack"),
        sortOrder = 18,
      ),
      ServiceCategory(
        id = "appliance_repair",
        name = "Appliance Repair",
        subtitle = "Fridges, washers, ovens & dispensers",
        icon = Icons.Rounded.Kitchen,
        tint = Color(0xFF8B5CF6),
        subcategories = listOf("Refrigerator", "Washing Machine", "Microwave Oven", "Water Dispenser"),
        sortOrder = 19,
      ),
      ServiceCategory(
        id = "construction_worker",
        name = "Construction Workers",
        subtitle = "Masonry, tiling, plaster & concrete work",
        icon = Icons.Rounded.Construction,
        tint = Color(0xFFF97316),
        subcategories = listOf("Tile Fixing", "Wall Plaster", "Brick Masonry", "Roof Screed"),
        sortOrder = 20,
      ),
      ServiceCategory(
        id = "general_labour",
        name = "General Labour",
        subtitle = "Shifting, loading, digging & site helpers",
        icon = Icons.Rounded.Handyman,
        tint = Color(0xFF14B8A6),
        subcategories = listOf("House Shifting", "Site Cleanup", "Material Loading", "Helper Work"),
        sortOrder = 21,
      ),
    )

  fun iconForKey(key: String): ImageVector =
    when (key.lowercase()) {
      "electrician", "electrical" -> Icons.Rounded.ElectricalServices
      "plumber", "plumbing" -> Icons.Rounded.Plumbing
      "ac_technician", "hvac" -> Icons.Rounded.AcUnit
      "mechanic", "auto" -> Icons.Rounded.DirectionsCar
      "carpenter", "carpentry" -> Icons.Rounded.Carpenter
      "it_expert" -> Icons.Rounded.LaptopMac
      "solar_technician", "solar_installer", "solar" -> Icons.Rounded.SolarPower
      "cctv_expert", "cctv_installer", "cctv" -> Icons.Rounded.Videocam
      "mobile_repair" -> Icons.Rounded.PhoneAndroid
      "painter", "painting" -> Icons.Rounded.FormatPaint
      "welder" -> Icons.Rounded.Hardware
      "generator_technician" -> Icons.Rounded.Power
      "lift_technician" -> Icons.Rounded.Elevator
      "civil_engineer" -> Icons.Rounded.Engineering
      "architect" -> Icons.Rounded.Architecture
      "network_technician", "it_network" -> Icons.Rounded.Router
      "appliance_repair", "appliance" -> Icons.Rounded.Kitchen
      "construction_worker" -> Icons.Rounded.Construction
      "general_labour", "handyman" -> Icons.Rounded.Handyman
      else -> Icons.Rounded.Build
    }

  fun tintForId(id: String): Color =
    defaults.firstOrNull { it.id == id }?.tint ?: Color(0xFF0F4C5C)

  fun mergeWithRemote(remote: List<ServiceCategoryEntity>, includeInactive: Boolean = false): List<ServiceCategory> {
    val mappedRemote =
      remote.map { entity ->
        ServiceCategory(
          id = entity.categoryId.ifBlank { entity.id },
          name = entity.name,
          subtitle = entity.subtitle,
          icon = iconForKey(entity.iconKey.ifBlank { entity.categoryId }),
          tint = tintForId(entity.categoryId),
          subcategories = entity.subcategories,
          active = entity.active,
          sortOrder = entity.sortOrder.toInt(),
        )
      }
    val byId = LinkedHashMap<String, ServiceCategory>()
    defaults.forEach { byId[it.id] = it }
    mappedRemote.forEach { byId[it.id] = it }
    return byId.values
      .filter { includeInactive || it.active }
      .sortedWith(compareBy<ServiceCategory> { it.sortOrder }.thenBy { it.name })
  }

  fun byId(id: String, categories: List<ServiceCategory> = defaults): ServiceCategory {
    val normalized =
      when (id) {
        "electrical" -> "electrician"
        "plumbing" -> "plumber"
        "hvac" -> "ac_technician"
        "auto" -> "mechanic"
        "carpentry" -> "carpenter"
        "it_network" -> "network_technician"
        "appliance" -> "appliance_repair"
        "painting" -> "painter"
        "handyman" -> "general_labour"
        else -> id
      }
    return categories.firstOrNull { it.id == id || it.id == normalized }
      ?: defaults.firstOrNull { it.id == normalized }
      ?: defaults.last()
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
  val categoryId: String = "electrician",
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
  val categoryId: String = "electrician",
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
