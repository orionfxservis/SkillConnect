package com.example.ui

import android.app.Activity
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FixNearRepository
import com.example.data.LocationHelper
import com.example.data.SubscriptionPreferencesStore
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.ComplaintStatus
import com.example.model.Coordinates
import com.example.model.ProviderSubscriptionStatus
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.ServiceCategoryEntity
import com.example.model.ServiceComplaint
import com.example.model.ServiceReview
import com.example.model.SubscriptionRulesCalculator
import com.example.model.SubscriptionTier
import com.example.model.TechnicianProfile
import com.example.model.TechnicianRegistrationInput
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestoreException
import java.util.Calendar
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TechnicianWithDistance(
  val technician: TechnicianProfile,
  val distanceKm: Double,
)

internal fun sortCandidatesNearToFar(
  techs: List<TechnicianProfile>,
  userCoordinates: Coordinates?,
): List<TechnicianWithDistance> =
  techs
    .filter { it.verificationEnum != VerificationStatus.REJECTED }
    .map { tech ->
      val dist =
        if (userCoordinates != null) {
          LocationHelper.distanceKm(
            userCoordinates.latitude,
            userCoordinates.longitude,
            tech.latitude,
            tech.longitude,
          )
        } else {
          -1.0
        }
      TechnicianWithDistance(tech, dist)
    }
    .sortedWith(
      compareBy<TechnicianWithDistance> {
          if (it.distanceKm >= 0.0) it.distanceKm else Double.MAX_VALUE
        }
        .thenByDescending { it.technician.availableNow }
        .thenByDescending { it.technician.verifiedBadge }
        .thenByDescending { it.technician.averageRating }
    )

class FixNearViewModel(
  private val repository: FixNearRepository,
  val currentUserId: String?,
  private val firebaseUser: FirebaseUser?,
  private val subscriptionStore: SubscriptionPreferencesStore? = null,
) : ViewModel() {

  val isSignedIn: Boolean = !currentUserId.isNullOrBlank() && firebaseUser != null

  val isAdmin: Boolean =
    firebaseUser != null &&
      firebaseUser.email.equals(SUPER_ADMIN_EMAIL, ignoreCase = true) &&
      firebaseUser.isEmailVerified

  private val _guestArea = MutableStateFlow("Karachi, Sindh")
  val guestArea: StateFlow<String> = _guestArea.asStateFlow()

  private val _userCoordinates = MutableStateFlow<Coordinates?>(null)
  val userCoordinates: StateFlow<Coordinates?> = _userCoordinates.asStateFlow()

  private val _hasLocationPermission = MutableStateFlow(false)
  val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

  private val _selectedCategoryId = MutableStateFlow<String?>(null)
  val selectedCategoryId: StateFlow<String?> = _selectedCategoryId.asStateFlow()

  private val _selectedSubcategory = MutableStateFlow<String?>(null)
  val selectedSubcategory: StateFlow<String?> = _selectedSubcategory.asStateFlow()

  private val _availableOnly = MutableStateFlow(false)
  val availableOnly: StateFlow<Boolean> = _availableOnly.asStateFlow()

  private val _verifiedOnly = MutableStateFlow(false)
  val verifiedOnly: StateFlow<Boolean> = _verifiedOnly.asStateFlow()

  private val _searchQuery = MutableStateFlow("")
  val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

  private val _actionMessage = MutableStateFlow<String?>(null)
  val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

  private val _isBusy = MutableStateFlow(false)
  val isBusy: StateFlow<Boolean> = _isBusy.asStateFlow()

  private val _isLiveBroadcastActive = MutableStateFlow(false)
  val isLiveBroadcastActive: StateFlow<Boolean> = _isLiveBroadcastActive.asStateFlow()

  // Phone OTP verification session state
  private val _phoneOtpVerificationId = MutableStateFlow<String?>(null)
  val phoneOtpVerificationId: StateFlow<String?> = _phoneOtpVerificationId.asStateFlow()

  private val _phoneOtpStatusMessage = MutableStateFlow<String?>(null)
  val phoneOtpStatusMessage: StateFlow<String?> = _phoneOtpStatusMessage.asStateFlow()

  private val _phoneVerifiedSession = MutableStateFlow(false)
  val phoneVerifiedSession: StateFlow<Boolean> = _phoneVerifiedSession.asStateFlow()

  private val _subscriptionTier =
    MutableStateFlow(subscriptionStore?.loadTier(currentUserId) ?: SubscriptionTier.FREE)
  private val _proRenewalAtMillis =
    MutableStateFlow(subscriptionStore?.loadProRenewalMillis(currentUserId))

  init {
    if (firebaseUser != null) {
      viewModelScope.launch {
        repository.ensureUserProfile(firebaseUser)
      }
    }
  }

  private fun <T> Flow<T>.toUiStateFlow(
    tagLabel: String,
    initial: UiState<T> = UiState.Loading,
  ): StateFlow<UiState<T>> =
    map<T, UiState<T>> { UiState.Success(it) }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing $tagLabel", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load $tagLabel"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = initial,
      )

  val userProfileState: StateFlow<UiState<UserProfile?>> =
    if (currentUserId != null) repository.observeUserProfile(currentUserId).toUiStateFlow("profile")
    else MutableStateFlow(UiState.Success(null))

  val myTechnicianState: StateFlow<UiState<TechnicianProfile?>> =
    if (currentUserId != null)
      repository.observeTechnician(currentUserId).toUiStateFlow("technician profile")
    else MutableStateFlow(UiState.Success(null))

  val myVerificationDocState: StateFlow<UiState<ProviderVerificationDoc?>> =
    if (currentUserId != null)
      repository.observeMyVerificationDoc(currentUserId).toUiStateFlow("verification document")
    else MutableStateFlow(UiState.Success(null))

  val categoriesState: StateFlow<List<ServiceCategory>> =
    repository
      .observeActiveCategories()
      .map { remote -> ServiceCategories.mergeWithRemote(remote, includeInactive = false) }
      .catch { error ->
        if (error is CancellationException) throw error
        Log.w(TAG, "Falling back to default categories", error)
        emit(ServiceCategories.defaults)
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = ServiceCategories.defaults,
      )

  private data class DirectoryFilters(
    val coords: Coordinates?,
    val catId: String?,
    val subCat: String?,
    val availOnly: Boolean,
    val verifiedOnly: Boolean,
    val query: String,
  )

  private val filterFlow =
    combine(
      _userCoordinates,
      _selectedCategoryId,
      _selectedSubcategory,
      _availableOnly,
      _verifiedOnly,
      _searchQuery,
    ) { values ->
      DirectoryFilters(
        coords = values[0] as Coordinates?,
        catId = values[1] as String?,
        subCat = values[2] as String?,
        availOnly = values[3] as Boolean,
        verifiedOnly = values[4] as Boolean,
        query = values[5] as String,
      )
    }

  val techniciansState: StateFlow<UiState<List<TechnicianWithDistance>>> =
    combine(
        repository.observeListedTechnicians(),
        categoriesState,
        filterFlow,
      ) { techs, categories, filters ->
        val trimmed = filters.query.trim().lowercase()
        techs
          .filter { tech ->
            val resolvedCat = ServiceCategories.byId(tech.categoryId, categories)
            val matchesAdditionalCat =
              filters.catId != null &&
                tech.additionalCategoryIds.any {
                  ServiceCategories.byId(it, categories).id == filters.catId
                }
            tech.verificationEnum != VerificationStatus.REJECTED &&
              (filters.catId == null || resolvedCat.id == filters.catId || matchesAdditionalCat) &&
              (filters.subCat == null ||
                tech.subcategory.equals(filters.subCat, ignoreCase = true) ||
                tech.expertise.contains(filters.subCat, ignoreCase = true)) &&
              (!filters.availOnly || tech.availableNow) &&
              (!filters.verifiedOnly || tech.verifiedBadge) &&
              (trimmed.isEmpty() ||
                tech.fullName.lowercase().contains(trimmed) ||
                tech.professionalTitle.lowercase().contains(trimmed) ||
                tech.expertise.lowercase().contains(trimmed) ||
                tech.subcategory.lowercase().contains(trimmed) ||
                tech.workingArea.lowercase().contains(trimmed) ||
                tech.city.lowercase().contains(trimmed) ||
                tech.qualification.lowercase().contains(trimmed) ||
                tech.preferredAreas.any { it.lowercase().contains(trimmed) } ||
                resolvedCat.name.lowercase().contains(trimmed))
          }
          .map { tech ->
            val dist =
              if (filters.coords != null) {
                LocationHelper.distanceKm(
                  filters.coords.latitude,
                  filters.coords.longitude,
                  tech.latitude,
                  tech.longitude,
                )
              } else {
                -1.0
              }
            TechnicianWithDistance(tech, dist)
          }
          .sortedWith(
            compareByDescending<TechnicianWithDistance> { it.technician.availableNow }
              .thenByDescending { it.technician.verifiedBadge }
              .thenBy { if (it.distanceKm >= 0.0) it.distanceKm else 9999.0 }
              .thenByDescending { it.technician.averageRating }
          )
      }
      .toUiStateFlow("technicians")

  val allTechniciansSortedByDistanceState: StateFlow<UiState<List<TechnicianWithDistance>>> =
    combine(
        repository.observeListedTechnicians(),
        _userCoordinates,
        userProfileState,
      ) { techs, liveCoords, profileState ->
        val profile = (profileState as? UiState.Success)?.data
        val coords =
          liveCoords
            ?: profile
              ?.takeIf { it.latitude != 0.0 || it.longitude != 0.0 }
              ?.let { Coordinates(latitude = it.latitude, longitude = it.longitude) }
        sortCandidatesNearToFar(techs = techs, userCoordinates = coords)
      }
      .toUiStateFlow("category candidates")

  val savedTechniciansState: StateFlow<List<TechnicianWithDistance>> =
    combine(
        userProfileState,
        repository.observeListedTechnicians(),
        _userCoordinates,
      ) { profileState, techs, coords ->
        val savedIds =
          (profileState as? UiState.Success)?.data?.savedTechnicianIds?.toSet() ?: emptySet()
        if (savedIds.isEmpty()) emptyList()
        else {
          techs
            .filter { it.userId in savedIds && it.verificationEnum != VerificationStatus.REJECTED }
            .map { tech ->
              val dist =
                if (coords != null) {
                  LocationHelper.distanceKm(
                    coords.latitude,
                    coords.longitude,
                    tech.latitude,
                    tech.longitude,
                  )
                } else {
                  -1.0
                }
              TechnicianWithDistance(tech, dist)
            }
        }
      }
      .catch { error ->
        if (error is CancellationException) throw error
        Log.w(TAG, "Error observing saved technicians", error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  val bookingsState: StateFlow<UiState<List<ServiceBooking>>> =
    if (currentUserId != null)
      repository.observeMyBookings(currentUserId).toUiStateFlow("bookings")
    else MutableStateFlow(UiState.Success(emptyList()))

  val subscriptionStatusState: StateFlow<ProviderSubscriptionStatus> =
    combine(_subscriptionTier, _proRenewalAtMillis, bookingsState) { tier, renewalMillis, bState ->
        val bookings = (bState as? UiState.Success)?.data ?: emptyList()
        SubscriptionRulesCalculator.computeStatus(
          tier = tier,
          allBookings = bookings,
          currentUserId = currentUserId,
          proRenewalAtMillis = renewalMillis,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue =
          SubscriptionRulesCalculator.computeStatus(
            tier = _subscriptionTier.value,
            allBookings = emptyList(),
            currentUserId = currentUserId,
            proRenewalAtMillis = _proRenewalAtMillis.value,
          ),
      )

  val myComplaintsState: StateFlow<UiState<List<ServiceComplaint>>> =
    if (currentUserId != null)
      repository.observeMyComplaints(currentUserId).toUiStateFlow("complaints")
    else MutableStateFlow(UiState.Success(emptyList()))

  // Active technician inspection
  private val _inspectedTechId = MutableStateFlow<String?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val inspectedTechnicianState: StateFlow<UiState<TechnicianProfile?>> =
    _inspectedTechId
      .flatMapLatest { techId ->
        if (techId.isNullOrBlank()) flowOf(null) else repository.observeTechnician(techId)
      }
      .toUiStateFlow("technician details")

  @OptIn(ExperimentalCoroutinesApi::class)
  val inspectedTechnicianReviewsState: StateFlow<UiState<List<ServiceReview>>> =
    _inspectedTechId
      .flatMapLatest { techId ->
        if (techId.isNullOrBlank()) flowOf(emptyList())
        else repository.observeTechnicianReviews(techId)
      }
      .toUiStateFlow("reviews")

  // Active booking + live technician tracking + private chat
  private val _inspectedBookingId = MutableStateFlow<String?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val inspectedBookingState: StateFlow<UiState<ServiceBooking?>> =
    _inspectedBookingId
      .flatMapLatest { bookingId ->
        if (bookingId.isNullOrBlank() || currentUserId == null) flowOf(null)
        else repository.observeBooking(bookingId)
      }
      .toUiStateFlow("booking")

  @OptIn(ExperimentalCoroutinesApi::class)
  val trackedBookingTechnicianState: StateFlow<UiState<TechnicianProfile?>> =
    inspectedBookingState
      .flatMapLatest { bookingState ->
        val techId = (bookingState as? UiState.Success)?.data?.technicianId
        if (techId.isNullOrBlank()) flowOf(null) else repository.observeTechnician(techId)
      }
      .toUiStateFlow("live technician location")

  @OptIn(ExperimentalCoroutinesApi::class)
  val bookingMessagesState: StateFlow<UiState<List<ChatMessage>>> =
    _inspectedBookingId
      .flatMapLatest { bookingId ->
        if (bookingId.isNullOrBlank() || currentUserId == null) flowOf(emptyList())
        else repository.observeBookingMessages(bookingId, currentUserId)
      }
      .toUiStateFlow("messages")

  // --- Super Admin StateFlows (strictly guarded by isAdmin) ---

  val adminUsersState: StateFlow<UiState<List<UserProfile>>> =
    if (isAdmin) repository.observeAllUsersAdmin().toUiStateFlow("admin users")
    else MutableStateFlow(UiState.Success(emptyList()))

  val adminTechniciansState: StateFlow<UiState<List<TechnicianProfile>>> =
    if (isAdmin) repository.observeAllTechniciansAdmin().toUiStateFlow("admin technicians")
    else MutableStateFlow(UiState.Success(emptyList()))

  val adminCategoriesState: StateFlow<UiState<List<ServiceCategoryEntity>>> =
    if (isAdmin) repository.observeAllCategoriesAdmin().toUiStateFlow("admin categories")
    else MutableStateFlow(UiState.Success(emptyList()))

  val adminBookingsState: StateFlow<UiState<List<ServiceBooking>>> =
    if (isAdmin) repository.observeAllBookingsAdmin().toUiStateFlow("admin bookings")
    else MutableStateFlow(UiState.Success(emptyList()))

  val adminComplaintsState: StateFlow<UiState<List<ServiceComplaint>>> =
    if (isAdmin) repository.observeAllComplaintsAdmin().toUiStateFlow("admin complaints")
    else MutableStateFlow(UiState.Success(emptyList()))

  private val _adminInspectedTechId = MutableStateFlow<String?>(null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val adminInspectedVerificationDocState: StateFlow<UiState<ProviderVerificationDoc?>> =
    if (isAdmin) {
      _adminInspectedTechId
        .flatMapLatest { techId ->
          if (techId.isNullOrBlank()) flowOf(null)
          else repository.observeVerificationDocAdmin(techId)
        }
        .toUiStateFlow("admin verification doc")
    } else {
      MutableStateFlow(UiState.Success(null))
    }

  // --- User & Filter Actions ---

  fun setLocationPermissionGranted(granted: Boolean) {
    _hasLocationPermission.value = granted
  }

  fun updateCoordinates(coords: Coordinates) {
    _userCoordinates.value = coords
  }

  fun updateGuestArea(area: String) {
    val cleaned = area.trim().take(120)
    if (cleaned.isNotEmpty()) {
      _guestArea.value = cleaned
      _actionMessage.value = "Area set to $cleaned"
    }
  }

  fun setLiveBroadcastActive(active: Boolean) {
    _isLiveBroadcastActive.value = active
  }

  fun selectCategory(categoryId: String?) {
    val next = if (_selectedCategoryId.value == categoryId) null else categoryId
    _selectedCategoryId.value = next
    _selectedSubcategory.value = null
  }

  fun selectSubcategory(subcategory: String?) {
    _selectedSubcategory.value =
      if (_selectedSubcategory.value == subcategory) null else subcategory
  }

  fun toggleAvailableOnly() {
    _availableOnly.value = !_availableOnly.value
  }

  fun toggleVerifiedOnly() {
    _verifiedOnly.value = !_verifiedOnly.value
  }

  fun updateSearchQuery(query: String) {
    _searchQuery.value = query
  }

  fun inspectTechnician(techId: String) {
    _inspectedTechId.value = techId
  }

  fun inspectBooking(bookingId: String) {
    _inspectedBookingId.value = bookingId
  }

  fun inspectVerificationDocAdmin(techId: String?) {
    _adminInspectedTechId.value = techId
  }

  fun clearMessage() {
    _actionMessage.value = null
  }

  fun clearPhoneOtpState() {
    _phoneOtpVerificationId.value = null
    _phoneOtpStatusMessage.value = null
  }

  fun startPhoneOtp(phoneNumber: String, activity: Activity) {
    _phoneOtpStatusMessage.value = "Sending SMS verification code..."
    repository.startPhoneVerification(
      phoneNumber = phoneNumber,
      activity = activity,
      onCodeSent = { verificationId ->
        _phoneOtpVerificationId.value = verificationId
        _phoneOtpStatusMessage.value = "OTP sent via SMS. Enter the 6-digit code below."
      },
      onAutoVerified = {
        _phoneVerifiedSession.value = true
        _phoneOtpVerificationId.value = null
        _phoneOtpStatusMessage.value = "Phone verified automatically!"
        _actionMessage.value = "Mobile number verified"
      },
      onError = { errorMsg ->
        _phoneOtpVerificationId.value = null
        _phoneOtpStatusMessage.value = errorMsg
      },
    )
  }

  fun confirmPhoneOtp(smsCode: String) {
    val verificationId = _phoneOtpVerificationId.value ?: return
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.confirmPhoneOtpAndMarkVerified(verificationId, smsCode)
      _isBusy.value = false
      result
        .onSuccess {
          _phoneVerifiedSession.value = true
          _phoneOtpVerificationId.value = null
          _phoneOtpStatusMessage.value = "Mobile number verified via SMS OTP!"
          _actionMessage.value = "Mobile number verified"
        }
        .onFailure {
          _phoneOtpStatusMessage.value = it.localizedMessage ?: "Invalid verification code."
        }
    }
  }

  fun saveUserProfile(
    displayName: String,
    phoneNumber: String,
    defaultArea: String,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val coords = _userCoordinates.value
      val result =
        repository.updateUserProfile(
          displayName = displayName,
          phoneNumber = phoneNumber,
          defaultArea = defaultArea,
          latitude = coords?.latitude ?: 0.0,
          longitude = coords?.longitude ?: 0.0,
        )
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Profile saved"
          onSuccess()
        }
        .onFailure { _actionMessage.value = it.localizedMessage ?: "Failed to save profile" }
    }
  }

  fun toggleSavedTechnician(techId: String) {
    if (!isSignedIn) return
    viewModelScope.launch {
      val result = repository.toggleSavedTechnician(techId)
      result
        .onSuccess { saved ->
          _actionMessage.value =
            if (saved) "Saved to your professionals" else "Removed from saved professionals"
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to update saved professionals"
        }
    }
  }

  fun saveTechnicianEnrollment(
    input: TechnicianRegistrationInput,
    onSuccess: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val normalized =
        input.copy(phoneVerified = input.phoneVerified || _phoneVerifiedSession.value)
      val result = repository.upsertTechnicianProfile(normalized)
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Service Provider registration submitted & saved"
          onSuccess()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Could not save provider profile"
        }
    }
  }

  fun updateLiveTechnicianStatusAndLocation(
    availableNow: Boolean,
    latitude: Double,
    longitude: Double,
    showToast: Boolean = true,
  ) {
    viewModelScope.launch {
      val result = repository.updateTechnicianLiveState(availableNow, latitude, longitude)
      if (showToast) {
        result
          .onSuccess {
            _actionMessage.value =
              if (availableNow) "Live availability & GPS updated" else "Status set to Offline"
          }
          .onFailure {
            _actionMessage.value = it.localizedMessage ?: "Failed to update live location"
          }
      }
    }
  }

  fun bookTechnician(
    technician: TechnicianProfile,
    clientName: String,
    clientPhone: String,
    serviceTitle: String,
    issueDescription: String,
    serviceArea: String,
    preferredTime: String,
    onBooked: (String) -> Unit,
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val coords = _userCoordinates.value
      val result =
        repository.createBooking(
          technician = technician,
          clientName = clientName,
          clientPhone = clientPhone,
          serviceTitle = serviceTitle,
          issueDescription = issueDescription,
          serviceArea = serviceArea,
          preferredTime = preferredTime,
          clientLat = coords?.latitude ?: 0.0,
          clientLng = coords?.longitude ?: 0.0,
        )
      _isBusy.value = false
      result
        .onSuccess { bookingId ->
          _actionMessage.value = "Booking sent to ${technician.fullName}"
          onBooked(bookingId)
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to create booking"
        }
    }
  }

  fun upgradeToProPlan() {
    val nextRenewal =
      Calendar.getInstance().apply { add(Calendar.MONTH, 1) }.timeInMillis
    _subscriptionTier.value = SubscriptionTier.PRO
    _proRenewalAtMillis.value = nextRenewal
    subscriptionStore?.savePlan(currentUserId, SubscriptionTier.PRO, nextRenewal)
    _actionMessage.value = "Pro Plan active (Rs. 499/month) — Unlimited bookings unlocked!"
  }

  fun renewProPlan() {
    val base = maxOf(System.currentTimeMillis(), _proRenewalAtMillis.value ?: 0L)
    val nextRenewal =
      Calendar.getInstance().apply {
        timeInMillis = base
        add(Calendar.MONTH, 1)
      }.timeInMillis
    _subscriptionTier.value = SubscriptionTier.PRO
    _proRenewalAtMillis.value = nextRenewal
    subscriptionStore?.savePlan(currentUserId, SubscriptionTier.PRO, nextRenewal)
    _actionMessage.value = "Pro subscription renewed for next month."
  }

  fun switchToFreePlan() {
    _subscriptionTier.value = SubscriptionTier.FREE
    _proRenewalAtMillis.value = null
    subscriptionStore?.savePlan(currentUserId, SubscriptionTier.FREE, null)
    _actionMessage.value = "Switched to Free Plan (5 monthly confirmed bookings)."
  }

  fun updateBookingStatus(booking: ServiceBooking, status: BookingStatus) {
    if (
      status == BookingStatus.ACCEPTED &&
        booking.technicianId == currentUserId &&
        !subscriptionStatusState.value.canConfirmAnotherBooking
    ) {
      _actionMessage.value =
        "Free plan limit reached (5/5 confirmed bookings this month). Upgrade to Pro in Profile → My Subscription for unlimited bookings."
      return
    }
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.advanceBookingStatus(booking, status)
      _isBusy.value = false
      result
        .onSuccess { _actionMessage.value = "Status updated: ${status.label}" }
        .onFailure { _actionMessage.value = it.localizedMessage ?: "Failed to update status" }
    }
  }

  fun sendMessage(booking: ServiceBooking, senderName: String, text: String) {
    viewModelScope.launch {
      val result = repository.sendBookingMessage(booking, senderName, text)
      result.onFailure {
        _actionMessage.value = it.localizedMessage ?: "Failed to send message"
      }
    }
  }

  fun submitReview(
    booking: ServiceBooking,
    rating: Int,
    comment: String,
    highlights: List<String>,
    onSubmitted: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.submitVerifiedReview(booking, rating, comment, highlights)
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Review published! Technician rating updated."
          onSubmitted()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to submit review"
        }
    }
  }

  fun submitComplaint(
    booking: ServiceBooking,
    reporterName: String,
    reason: String,
    description: String,
    onSubmitted: () -> Unit = {},
  ) {
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.submitComplaint(booking, reporterName, reason, description)
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Report submitted to Super Admin."
          onSubmitted()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to submit report"
        }
    }
  }

  // --- Super Admin Actions ---

  fun seedDefaultCategoriesAdmin() {
    if (!isAdmin) return
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.seedDefaultCategoriesAdmin()
      _isBusy.value = false
      result
        .onSuccess { count ->
          _actionMessage.value =
            if (count > 0) "Seeded $count categories to Firestore"
            else "All 21 default categories are already synced"
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to seed categories"
        }
    }
  }

  fun saveCategoryAdmin(
    categoryId: String,
    name: String,
    subtitle: String,
    iconKey: String,
    subcategories: List<String>,
    active: Boolean,
    sortOrder: Long,
    onSuccess: () -> Unit = {},
  ) {
    if (!isAdmin) return
    viewModelScope.launch {
      _isBusy.value = true
      val result =
        repository.saveCategoryAdmin(
          categoryId = categoryId,
          name = name,
          subtitle = subtitle,
          iconKey = iconKey,
          subcategories = subcategories,
          active = active,
          sortOrder = sortOrder,
        )
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Category \"$name\" saved"
          onSuccess()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to save category"
        }
    }
  }

  fun reviewTechnicianAdmin(
    techId: String,
    verificationStatus: VerificationStatus,
    verifiedBadge: Boolean,
    adminRemarks: String,
    listed: Boolean,
    onSuccess: () -> Unit = {},
  ) {
    if (!isAdmin) return
    viewModelScope.launch {
      _isBusy.value = true
      val result =
        repository.reviewTechnicianAdmin(
          techId = techId,
          verificationStatus = verificationStatus,
          verifiedBadge = verifiedBadge,
          adminRemarks = adminRemarks,
          listed = listed,
        )
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Provider status updated to ${verificationStatus.label}"
          onSuccess()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to update provider verification"
        }
    }
  }

  fun updateUserAdmin(userId: String, suspended: Boolean, role: String) {
    if (!isAdmin) return
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.updateUserAdmin(userId, suspended, role)
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = if (suspended) "User suspended" else "User updated"
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to update user"
        }
    }
  }

  fun resolveComplaintAdmin(
    complaintId: String,
    status: ComplaintStatus,
    adminResolution: String,
    onSuccess: () -> Unit = {},
  ) {
    if (!isAdmin) return
    viewModelScope.launch {
      _isBusy.value = true
      val result = repository.resolveComplaintAdmin(complaintId, status, adminResolution)
      _isBusy.value = false
      result
        .onSuccess {
          _actionMessage.value = "Complaint marked ${status.label}"
          onSuccess()
        }
        .onFailure {
          _actionMessage.value = it.localizedMessage ?: "Failed to update complaint"
        }
    }
  }

  private companion object {
    const val TAG = "FixNearVM"
    const val SUPER_ADMIN_EMAIL = "admin-skillconnect@orionfx.net"
  }
}
