package com.example.data

import android.app.Activity
import android.content.Context
import com.example.R
import com.example.model.BookingStatus
import com.example.model.ChatMessage
import com.example.model.ComplaintStatus
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.ServiceCategoryEntity
import com.example.model.ServiceComplaint
import com.example.model.ServiceReview
import com.example.model.TechnicianProfile
import com.example.model.TechnicianRegistrationInput
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import java.util.concurrent.TimeUnit
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FixNearRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(
    context: Context
  ) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireUserId(): String =
    auth.currentUser?.uid
      ?: throw IllegalStateException("Please sign in with Google to continue.")

  private fun <T> Flow<T>.logFirestoreErrors(
    operationType: OperationType,
    path: String,
    uid: String,
  ): Flow<T> = catch { error ->
    val firestoreError =
      generateSequence(error) { it.cause }
        .filterIsInstance<FirebaseFirestoreException>()
        .firstOrNull()
    if (firestoreError == null && error is CancellationException) throw error
    val toLog = firestoreError ?: (error as? Exception)
    if (toLog != null && (uid == "guest" || auth.currentUser?.uid == uid)) {
      handleFirestoreError(toLog, operationType, path)
    }
    throw error
  }

  // --- User Profile ---

  suspend fun ensureUserProfile(user: FirebaseUser): Result<Unit> {
    val path = "users/${user.uid}"
    return try {
      val docRef = db.collection("users").document(user.uid)
      val existing = docRef.get().await()
      if (!existing.exists()) {
        val safeName =
          (user.displayName?.trim()?.takeIf { it.isNotEmpty() }
              ?: user.email?.substringBefore("@")?.takeIf { it.isNotEmpty() }
              ?: "FixNear User")
            .take(60)
        val payload =
          mapOf(
            "userId" to user.uid,
            "displayName" to safeName,
            "email" to (user.email ?: "").take(120),
            "phoneNumber" to (user.phoneNumber ?: "").take(25),
            "defaultArea" to "",
            "latitude" to 0.0,
            "longitude" to 0.0,
            "role" to "CUSTOMER",
            "enrolledTechnician" to false,
            "suspended" to false,
            "savedTechnicianIds" to emptyList<String>(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        docRef.set(payload).await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "users/$uid"
    emitAll(
      db.collection("users")
        .document(uid)
        .snapshots()
        .map { snap ->
          snap.toObject(UserProfile::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        }
        .logFirestoreErrors(OperationType.GET, path, uid)
    )
  }

  suspend fun updateUserProfile(
    displayName: String,
    phoneNumber: String,
    defaultArea: String,
    latitude: Double,
    longitude: Double,
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "users/$uid"
    return try {
      val verifiedUid = requireUserId()
      val cleanName = displayName.trim()
      require(cleanName.length in 1..60) { "Name must be between 1 and 60 characters." }
      val cleanPhone = phoneNumber.trim().take(25)
      val cleanArea = defaultArea.trim().take(120)
      db.collection("users")
        .document(verifiedUid)
        .update(
          mapOf(
            "displayName" to cleanName,
            "phoneNumber" to cleanPhone,
            "defaultArea" to cleanArea,
            "latitude" to latitude.coerceIn(-90.0, 90.0),
            "longitude" to longitude.coerceIn(-180.0, 180.0),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun toggleSavedTechnician(techId: String): Result<Boolean> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "users/$uid"
    return try {
      val verifiedUid = requireUserId()
      require(techId.isNotBlank()) { "Invalid technician ID." }
      val docRef = db.collection("users").document(verifiedUid)
      val snap = docRef.get().await()
      val existing =
        (snap.get("savedTechnicianIds") as? List<*>)
          ?.filterIsInstance<String>()
          ?: emptyList()
      val nowSaved: Boolean
      val updatedList =
        if (techId in existing) {
          nowSaved = false
          existing.filter { it != techId }
        } else {
          nowSaved = true
          (listOf(techId) + existing).distinct().take(50)
        }
      docRef
        .update(
          mapOf(
            "savedTechnicianIds" to updatedList,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(nowSaved)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- Dynamic Categories & Subcategories ---

  fun observeActiveCategories(): Flow<List<ServiceCategoryEntity>> = flow {
    val uid = auth.currentUser?.uid ?: "guest"
    val path = "categories"
    emitAll(
      db.collection(path)
        .whereEqualTo("active", true)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceCategoryEntity::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(compareBy<ServiceCategoryEntity> { it.sortOrder }.thenBy { it.name })
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeAllCategoriesAdmin(): Flow<List<ServiceCategoryEntity>> = flow {
    val uid = requireUserId()
    val path = "categories"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceCategoryEntity::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(compareBy<ServiceCategoryEntity> { it.sortOrder }.thenBy { it.name })
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun saveCategoryAdmin(
    categoryId: String,
    name: String,
    subtitle: String,
    iconKey: String,
    subcategories: List<String>,
    active: Boolean,
    sortOrder: Long,
  ): Result<Unit> {
    val cleanId =
      categoryId.trim().lowercase().replace(Regex("[^a-z0-9_\\-]"), "_").trim('_').take(50)
    val path = "categories/$cleanId"
    return try {
      requireUserId()
      val cleanName = name.trim()
      val cleanSubtitle = subtitle.trim().take(140)
      val cleanIconKey = iconKey.trim().ifEmpty { cleanId }.take(40)
      val cleanSubs =
        subcategories.map { it.trim().take(50) }.filter { it.isNotEmpty() }.distinct().take(25)

      require(cleanId.isNotEmpty()) { "Category ID is required." }
      require(cleanName.length in 2..50) { "Category name must be 2 to 50 characters." }

      val docRef = db.collection("categories").document(cleanId)
      val existing = docRef.get().await()
      if (!existing.exists()) {
        docRef
          .set(
            mapOf(
              "categoryId" to cleanId,
              "name" to cleanName,
              "subtitle" to cleanSubtitle,
              "iconKey" to cleanIconKey,
              "subcategories" to cleanSubs,
              "active" to active,
              "sortOrder" to sortOrder.coerceIn(0L, 1000L),
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      } else {
        docRef
          .update(
            mapOf(
              "name" to cleanName,
              "subtitle" to cleanSubtitle,
              "iconKey" to cleanIconKey,
              "subcategories" to cleanSubs,
              "active" to active,
              "sortOrder" to sortOrder.coerceIn(0L, 1000L),
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun seedDefaultCategoriesAdmin(): Result<Int> {
    val path = "categories"
    return try {
      requireUserId()
      val existingIds = db.collection(path).get().await().documents.map { it.id }.toSet()
      val missing = ServiceCategories.defaults.filter { it.id !in existingIds }
      if (missing.isNotEmpty()) {
        val batch = db.batch()
        missing.forEach { cat ->
          val docRef = db.collection(path).document(cat.id)
          batch.set(
            docRef,
            mapOf(
              "categoryId" to cat.id,
              "name" to cat.name,
              "subtitle" to cat.subtitle.take(140),
              "iconKey" to cat.id,
              "subcategories" to cat.subcategories.take(25),
              "active" to true,
              "sortOrder" to cat.sortOrder.toLong(),
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        batch.commit().await()
      }
      Result.success(missing.size)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  // --- Technician Directory & Enrollment ---

  fun observeListedTechnicians(): Flow<List<TechnicianProfile>> = flow {
    val uid = auth.currentUser?.uid ?: "guest"
    val path = "technicians"
    emitAll(
      db.collection(path)
        .whereEqualTo("listed", true)
        .snapshots()
        .map { snapshot ->
          snapshot.toObjects(
            TechnicianProfile::class.java,
            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
          )
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeTechnician(techId: String): Flow<TechnicianProfile?> = flow {
    val uid = auth.currentUser?.uid ?: "guest"
    val path = "technicians/$techId"
    emitAll(
      db.collection("technicians")
        .document(techId)
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(
              TechnicianProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .logFirestoreErrors(OperationType.GET, path, uid)
    )
  }

  fun observeMyVerificationDoc(userId: String): Flow<ProviderVerificationDoc?> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "provider_verifications/$uid"
    emitAll(
      db.collection("provider_verifications")
        .document(uid)
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(
              ProviderVerificationDoc::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .logFirestoreErrors(OperationType.GET, path, uid)
    )
  }

  fun observeVerificationDocAdmin(techId: String): Flow<ProviderVerificationDoc?> = flow {
    val uid = requireUserId()
    val path = "provider_verifications/$techId"
    emitAll(
      db.collection("provider_verifications")
        .document(techId)
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(
              ProviderVerificationDoc::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .logFirestoreErrors(OperationType.GET, path, uid)
    )
  }

  suspend fun upsertTechnicianProfile(input: TechnicianRegistrationInput): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "technicians/$uid"
    return try {
      val verifiedUid = requireUserId()
      val cleanName = input.fullName.trim()
      val cleanEmail = input.email.trim().take(120)
      val cleanGender = input.gender.trim().take(30)
      val cleanCategory = input.categoryId.trim().take(60)
      val cleanExtraCats =
        input.additionalCategoryIds
          .map { it.trim().take(60) }
          .filter { it.isNotEmpty() && it != cleanCategory }
          .distinct()
          .take(12)
      val cleanSubcategory = input.subcategory.trim().take(80)
      val cleanTitle = input.professionalTitle.trim().take(80)
      val cleanExpertise = input.expertise.trim()
      val cleanBio = input.bio.trim()
      val cleanQualification = input.qualification.trim().take(100)
      val cleanPhone = input.contactPhone.trim()
      val cleanVisitCharge = input.visitCharge.trim().take(60)
      val cleanHourlyCharge = input.hourlyCharge.trim().take(40)
      val cleanDailyCharge = input.dailyCharge.trim().take(40)
      val cleanChargeNote = input.serviceChargeNote.trim().take(140)
      val cleanWorkingDays =
        input.workingDays.map { it.trim().take(12) }.filter { it.isNotEmpty() }.distinct().take(7)
      val cleanHours = input.availabilityHours.trim()
      val cleanCity = input.city.trim().take(60)
      val cleanArea = input.workingArea.trim()
      val cleanPreferredAreas =
        input.preferredAreas.map { it.trim().take(60) }.filter { it.isNotEmpty() }.distinct().take(15)
      val cleanPhoto = input.profilePhotoData.take(140_000)
      val cleanPortfolio =
        input.portfolioImages.map { it.take(140_000) }.filter { it.isNotEmpty() }.take(4)
      val cleanCnic = input.cnicNumber.trim().take(30)
      val cleanCnicPhoto = input.cnicPhotoData.take(160_000)
      val cleanCnicBackPhoto = input.cnicBackPhotoData.take(160_000)
      val cleanCertPhoto = input.certificatePhotoData.take(160_000)

      require(cleanName.length in 2..60) { "Full name must be 2 to 60 characters." }
      require(cleanCategory.length in 2..60) { "Select a valid main category." }
      require(cleanExpertise.length in 2..220) { "Skills & expertise must be 2 to 220 characters." }
      require(cleanBio.length <= 500) { "Work description must be under 500 characters." }
      require(cleanPhone.length in 5..25) { "Enter a valid mobile number (5–25 chars)." }
      require(cleanHours.length in 2..80) { "Enter your working hours (e.g. 09:00 AM - 07:00 PM)." }
      require(cleanArea.length in 2..120) { "Enter your working area or town." }
      require(input.yearsExperience in 0L..70L) { "Years of experience must be between 0 and 70." }
      require(input.serviceRadiusKm in 1L..200L) { "Service radius must be 1 to 200 km." }

      val techRef = db.collection("technicians").document(verifiedUid)
      val userRef = db.collection("users").document(verifiedUid)
      val verifyRef = db.collection("provider_verifications").document(verifiedUid)
      val existingTech = techRef.get().await()
      val hasAnyVerificationDoc =
        cleanCnic.isNotEmpty() ||
          cleanCnicPhoto.isNotEmpty() ||
          cleanCnicBackPhoto.isNotEmpty() ||
          cleanCertPhoto.isNotEmpty()
      val existingVerify = if (hasAnyVerificationDoc) verifyRef.get().await() else null

      val batch = db.batch()
      if (!existingTech.exists()) {
        val createPayload =
          mapOf(
            "userId" to verifiedUid,
            "fullName" to cleanName,
            "email" to cleanEmail,
            "gender" to cleanGender,
            "categoryId" to cleanCategory,
            "additionalCategoryIds" to cleanExtraCats,
            "subcategory" to cleanSubcategory,
            "professionalTitle" to cleanTitle,
            "expertise" to cleanExpertise,
            "bio" to cleanBio,
            "qualification" to cleanQualification,
            "yearsExperience" to input.yearsExperience,
            "contactPhone" to cleanPhone,
            "phoneVerified" to input.phoneVerified,
            "visitCharge" to cleanVisitCharge,
            "hourlyCharge" to cleanHourlyCharge,
            "dailyCharge" to cleanDailyCharge,
            "serviceChargeNote" to cleanChargeNote,
            "workingDays" to cleanWorkingDays,
            "availabilityHours" to cleanHours,
            "emergencyAvailable" to input.emergencyAvailable,
            "city" to cleanCity,
            "workingArea" to cleanArea,
            "preferredAreas" to cleanPreferredAreas,
            "travelToCustomer" to input.travelToCustomer,
            "serviceRadiusKm" to input.serviceRadiusKm,
            "availableNow" to input.availableNow,
            "listed" to input.listed,
            "termsAccepted" to input.termsAccepted,
            "verificationConsent" to input.verificationConsent,
            "verificationStatus" to VerificationStatus.PENDING.name,
            "verifiedBadge" to false,
            "adminRemarks" to "",
            "profilePhotoData" to cleanPhoto,
            "portfolioImages" to cleanPortfolio,
            "latitude" to input.latitude.coerceIn(-90.0, 90.0),
            "longitude" to input.longitude.coerceIn(-180.0, 180.0),
            "ratingSum" to 0L,
            "reviewCount" to 0L,
            "averageRating" to 0.0,
            "completedJobs" to 0L,
            "locationUpdatedAt" to FieldValue.serverTimestamp(),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        batch.set(techRef, createPayload)
        batch.update(
          userRef,
          mapOf(
            "role" to "PROVIDER",
            "enrolledTechnician" to true,
            "phoneNumber" to cleanPhone,
            "defaultArea" to if (cleanCity.isNotBlank()) "$cleanArea, $cleanCity" else cleanArea,
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      } else {
        batch.update(
          techRef,
          mapOf(
            "fullName" to cleanName,
            "email" to cleanEmail,
            "gender" to cleanGender,
            "categoryId" to cleanCategory,
            "additionalCategoryIds" to cleanExtraCats,
            "subcategory" to cleanSubcategory,
            "professionalTitle" to cleanTitle,
            "expertise" to cleanExpertise,
            "bio" to cleanBio,
            "qualification" to cleanQualification,
            "yearsExperience" to input.yearsExperience,
            "contactPhone" to cleanPhone,
            "phoneVerified" to input.phoneVerified,
            "visitCharge" to cleanVisitCharge,
            "hourlyCharge" to cleanHourlyCharge,
            "dailyCharge" to cleanDailyCharge,
            "serviceChargeNote" to cleanChargeNote,
            "workingDays" to cleanWorkingDays,
            "availabilityHours" to cleanHours,
            "emergencyAvailable" to input.emergencyAvailable,
            "city" to cleanCity,
            "workingArea" to cleanArea,
            "preferredAreas" to cleanPreferredAreas,
            "travelToCustomer" to input.travelToCustomer,
            "serviceRadiusKm" to input.serviceRadiusKm,
            "availableNow" to input.availableNow,
            "listed" to input.listed,
            "termsAccepted" to input.termsAccepted,
            "verificationConsent" to input.verificationConsent,
            "profilePhotoData" to cleanPhoto,
            "portfolioImages" to cleanPortfolio,
            "latitude" to input.latitude.coerceIn(-90.0, 90.0),
            "longitude" to input.longitude.coerceIn(-180.0, 180.0),
            "locationUpdatedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      if (hasAnyVerificationDoc) {
        if (existingVerify == null || !existingVerify.exists()) {
          batch.set(
            verifyRef,
            mapOf(
              "userId" to verifiedUid,
              "cnicNumber" to cleanCnic,
              "cnicPhotoData" to cleanCnicPhoto,
              "cnicBackPhotoData" to cleanCnicBackPhoto,
              "certificatePhotoData" to cleanCertPhoto,
              "createdAt" to FieldValue.serverTimestamp(),
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        } else {
          batch.update(
            verifyRef,
            mapOf(
              "cnicNumber" to cleanCnic,
              "cnicPhotoData" to cleanCnicPhoto,
              "cnicBackPhotoData" to cleanCnicBackPhoto,
              "certificatePhotoData" to cleanCertPhoto,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
      }

      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun updateTechnicianLiveState(
    availableNow: Boolean,
    latitude: Double,
    longitude: Double,
  ): Result<Unit> {
    val uid = auth.currentUser?.uid ?: "unauthenticated"
    val path = "technicians/$uid"
    return try {
      val verifiedUid = requireUserId()
      db.collection("technicians")
        .document(verifiedUid)
        .update(
          mapOf(
            "availableNow" to availableNow,
            "latitude" to latitude.coerceIn(-90.0, 90.0),
            "longitude" to longitude.coerceIn(-180.0, 180.0),
            "locationUpdatedAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- Firebase Phone OTP Verification (Honest SDK attempt, no fake OTP) ---

  fun startPhoneVerification(
    phoneNumber: String,
    activity: Activity,
    onCodeSent: (verificationId: String) -> Unit,
    onAutoVerified: () -> Unit,
    onError: (String) -> Unit,
  ) {
    val trimmed = phoneNumber.trim()
    if (!trimmed.startsWith("+") || trimmed.length < 8) {
      onError("Enter phone in E.164 international format (e.g. +923001234567) for SMS OTP.")
      return
    }
    val callbacks =
      object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
          onAutoVerified()
        }

        override fun onVerificationFailed(e: FirebaseException) {
          onError(
            e.localizedMessage
              ?: "Phone OTP unavailable — enable Phone provider in Firebase Console Authentication."
          )
        }

        override fun onCodeSent(
          verificationId: String,
          token: PhoneAuthProvider.ForceResendingToken,
        ) {
          onCodeSent(verificationId)
        }
      }

    try {
      val options =
        PhoneAuthOptions.newBuilder(auth)
          .setPhoneNumber(trimmed)
          .setTimeout(60L, TimeUnit.SECONDS)
          .setActivity(activity)
          .setCallbacks(callbacks)
          .build()
      PhoneAuthProvider.verifyPhoneNumber(options)
    } catch (e: Exception) {
      onError(e.localizedMessage ?: "Failed to initiate SMS OTP verification.")
    }
  }

  suspend fun confirmPhoneOtpAndMarkVerified(
    verificationId: String,
    smsCode: String,
  ): Result<Unit> {
    val path = "auth/phone_otp"
    return try {
      val uid = requireUserId()
      val cleanCode = smsCode.trim()
      require(cleanCode.length in 4..8) { "Enter the SMS verification code." }
      val credential = PhoneAuthProvider.getCredential(verificationId, cleanCode)
      val user = auth.currentUser ?: throw IllegalStateException("No signed-in user.")
      // Link or verify phone credential with Firebase Auth without replacing Google session
      user.updatePhoneNumber(credential).await()
      val techRef = db.collection("technicians").document(uid)
      if (techRef.get().await().exists()) {
        techRef
          .update(
            mapOf(
              "phoneVerified" to true,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- Bookings & Live Dispatch ---

  fun observeMyBookings(userId: String): Flow<List<ServiceBooking>> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "bookings"
    emitAll(
      db.collection(path)
        .whereArrayContains("participantIds", uid)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceBooking::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeBooking(bookingId: String): Flow<ServiceBooking?> = flow {
    val uid = requireUserId()
    val path = "bookings/$bookingId"
    emitAll(
      db.collection("bookings")
        .document(bookingId)
        .snapshots()
        .map { snap ->
          if (snap.exists()) {
            snap.toObject(
              ServiceBooking::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
          } else {
            null
          }
        }
        .logFirestoreErrors(OperationType.GET, path, uid)
    )
  }

  suspend fun createBooking(
    technician: TechnicianProfile,
    clientName: String,
    clientPhone: String,
    serviceTitle: String,
    issueDescription: String,
    serviceArea: String,
    preferredTime: String,
    clientLat: Double,
    clientLng: Double,
  ): Result<String> {
    var path = "bookings"
    return try {
      val uid = requireUserId()
      require(technician.userId.isNotBlank() && technician.userId != uid) {
        "You cannot book your own technician profile."
      }
      val cleanClientName = clientName.trim()
      val cleanClientPhone = clientPhone.trim()
      val cleanTitle = serviceTitle.trim()
      val cleanDesc = issueDescription.trim()
      val cleanArea = serviceArea.trim()
      val cleanTime = preferredTime.trim()

      require(cleanClientName.length in 1..60) { "Enter your name." }
      require(cleanClientPhone.length in 5..25) { "Enter a valid contact phone (5–25 chars)." }
      require(cleanTitle.length in 2..100) { "Enter a brief service title." }
      require(cleanDesc.length in 5..600) { "Describe the issue (at least 5 characters)." }
      require(cleanArea.length in 2..150) { "Enter your service location/address." }
      require(cleanTime.length in 2..80) { "Select or enter preferred time." }

      val docRef = db.collection("bookings").document()
      path = docRef.path
      val payload =
        mapOf(
          "clientId" to uid,
          "clientName" to cleanClientName,
          "clientPhone" to cleanClientPhone,
          "technicianId" to technician.userId,
          "technicianName" to technician.fullName.take(60),
          "technicianPhone" to technician.contactPhone.take(25),
          "categoryId" to technician.categoryId,
          "serviceTitle" to cleanTitle,
          "issueDescription" to cleanDesc,
          "serviceArea" to cleanArea,
          "preferredTime" to cleanTime,
          "clientLat" to clientLat.coerceIn(-90.0, 90.0),
          "clientLng" to clientLng.coerceIn(-180.0, 180.0),
          "status" to BookingStatus.REQUESTED.name,
          "reviewed" to false,
          "participantIds" to listOf(uid, technician.userId),
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      docRef.set(payload).await()
      Result.success(docRef.id)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun advanceBookingStatus(
    booking: ServiceBooking,
    newStatus: BookingStatus,
  ): Result<Unit> {
    val path = "bookings/${booking.id}"
    return try {
      requireUserId()
      require(booking.id.isNotBlank()) { "Invalid booking ID." }
      val bookingRef = db.collection("bookings").document(booking.id)
      if (newStatus == BookingStatus.COMPLETED) {
        val techRef = db.collection("technicians").document(booking.technicianId)
        db.runTransaction { tx ->
            val techSnap = tx.get(techRef)
            val currentJobs = techSnap.getLong("completedJobs") ?: 0L
            tx.update(
              bookingRef,
              mapOf(
                "status" to BookingStatus.COMPLETED.name,
                "updatedAt" to FieldValue.serverTimestamp(),
              ),
            )
            tx.update(
              techRef,
              mapOf(
                "completedJobs" to (currentJobs + 1L),
                "lastCompletedBookingId" to booking.id,
                "updatedAt" to FieldValue.serverTimestamp(),
              ),
            )
          }
          .await()
      } else {
        bookingRef
          .update(
            mapOf(
              "status" to newStatus.name,
              "updatedAt" to FieldValue.serverTimestamp(),
            )
          )
          .await()
      }
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  // --- Private Booking Messaging ---

  fun observeBookingMessages(bookingId: String, userId: String): Flow<List<ChatMessage>> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "bookings/$bookingId/messages"
    emitAll(
      db.collection("bookings")
        .document(bookingId)
        .collection("messages")
        .whereArrayContains("participantIds", uid)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ChatMessage::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedBy { it.createdAt?.seconds ?: Long.MAX_VALUE }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun sendBookingMessage(
    booking: ServiceBooking,
    senderName: String,
    text: String,
  ): Result<Unit> {
    var path = "bookings/${booking.id}/messages"
    return try {
      val uid = requireUserId()
      val cleanText = text.trim()
      require(cleanText.length in 1..500) { "Message must be 1 to 500 characters." }
      require(booking.participantIds.size == 2 && uid in booking.participantIds) {
        "Only booking participants can send messages."
      }
      val msgRef =
        db.collection("bookings").document(booking.id).collection("messages").document()
      path = msgRef.path
      val payload =
        mapOf(
          "bookingId" to booking.id,
          "senderId" to uid,
          "senderName" to senderName.trim().ifEmpty { "User" }.take(60),
          "text" to cleanText,
          "participantIds" to booking.participantIds,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      msgRef.set(payload).await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  // --- Reviews & Dynamic Reputation ---

  fun observeTechnicianReviews(technicianId: String): Flow<List<ServiceReview>> = flow {
    val uid = auth.currentUser?.uid ?: "guest"
    val path = "reviews"
    emitAll(
      db.collection(path)
        .whereEqualTo("technicianId", technicianId)
        .whereEqualTo("approved", true)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceReview::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun submitVerifiedReview(
    booking: ServiceBooking,
    rating: Int,
    comment: String,
    highlights: List<String>,
  ): Result<Unit> {
    val path = "reviews/${booking.id}"
    return try {
      val uid = requireUserId()
      require(uid == booking.clientId) { "Only the client can review this booking." }
      require(rating in 1..5) { "Rating must be between 1 and 5 stars." }
      val cleanComment = comment.trim()
      require(cleanComment.length in 2..500) { "Please write a short review (2–500 chars)." }
      val cleanHighlights = highlights.map { it.trim().take(40) }.filter { it.isNotEmpty() }.take(5)

      val reviewRef = db.collection("reviews").document(booking.id)
      val bookingRef = db.collection("bookings").document(booking.id)
      val techRef = db.collection("technicians").document(booking.technicianId)

      db.runTransaction { tx ->
          val bookingSnap = tx.get(bookingRef)
          val techSnap = tx.get(techRef)
          val status = bookingSnap.getString("status") ?: ""
          val alreadyReviewed = bookingSnap.getBoolean("reviewed") ?: false
          require(status == BookingStatus.COMPLETED.name) {
            "Reviews can only be submitted after job completion."
          }
          require(!alreadyReviewed) { "You have already reviewed this booking." }

          val currentSum = techSnap.getLong("ratingSum") ?: 0L
          val currentCount = techSnap.getLong("reviewCount") ?: 0L
          val newSum = currentSum + rating.toLong()
          val newCount = currentCount + 1L
          val newAvg = ((newSum.toDouble() / newCount.toDouble()) * 10.0).roundToInt() / 10.0

          tx.set(
            reviewRef,
            mapOf(
              "bookingId" to booking.id,
              "technicianId" to booking.technicianId,
              "clientId" to uid,
              "clientName" to booking.clientName.take(60),
              "rating" to rating.toLong(),
              "comment" to cleanComment,
              "highlights" to cleanHighlights,
              "approved" to true,
              "createdAt" to FieldValue.serverTimestamp(),
            ),
          )
          tx.update(
            bookingRef,
            mapOf(
              "reviewed" to true,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
          tx.update(
            techRef,
            mapOf(
              "ratingSum" to newSum,
              "reviewCount" to newCount,
              "averageRating" to newAvg,
              "lastReviewBookingId" to booking.id,
              "updatedAt" to FieldValue.serverTimestamp(),
            ),
          )
        }
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  // --- Complaints & Issue Reporting ---

  fun observeMyComplaints(userId: String): Flow<List<ServiceComplaint>> = flow {
    val uid = requireUserId()
    check(uid == userId) { "Signed-in user mismatch." }
    val path = "complaints"
    emitAll(
      db.collection(path)
        .whereEqualTo("reporterId", uid)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceComplaint::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun submitComplaint(
    booking: ServiceBooking,
    reporterName: String,
    reason: String,
    description: String,
  ): Result<Unit> {
    var path = "complaints"
    return try {
      val uid = requireUserId()
      val cleanReason = reason.trim()
      val cleanDesc = description.trim()
      require(cleanReason.length in 2..80) { "Select or enter a complaint reason." }
      require(cleanDesc.length in 5..600) { "Describe the issue (at least 5 characters)." }

      val counterpartId =
        if (uid == booking.clientId) booking.technicianId else booking.clientId
      val counterpartName =
        if (uid == booking.clientId) booking.technicianName else booking.clientName

      val docRef = db.collection("complaints").document()
      path = docRef.path
      docRef
        .set(
          mapOf(
            "bookingId" to booking.id,
            "reporterId" to uid,
            "reporterName" to reporterName.trim().ifEmpty { "User" }.take(60),
            "reportedTechId" to counterpartId,
            "reportedTechName" to counterpartName.take(60),
            "reason" to cleanReason,
            "description" to cleanDesc,
            "status" to ComplaintStatus.OPEN.name,
            "adminResolution" to "",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  // --- Super Admin Operations (Strictly invoked only when isAdmin == true) ---

  fun observeAllUsersAdmin(): Flow<List<UserProfile>> = flow {
    val uid = requireUserId()
    val path = "users"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              UserProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun updateUserAdmin(
    userId: String,
    suspended: Boolean,
    role: String,
  ): Result<Unit> {
    val path = "users/$userId"
    return try {
      requireUserId()
      require(role in listOf("CUSTOMER", "PROVIDER", "ADMIN")) { "Invalid role." }
      db.collection("users")
        .document(userId)
        .update(
          mapOf(
            "suspended" to suspended,
            "role" to role,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  fun observeAllTechniciansAdmin(): Flow<List<TechnicianProfile>> = flow {
    val uid = requireUserId()
    val path = "technicians"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              TechnicianProfile::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(
              compareBy<TechnicianProfile> {
                when (it.verificationEnum) {
                  VerificationStatus.PENDING -> 0
                  VerificationStatus.APPROVED -> 1
                  VerificationStatus.REJECTED -> 2
                }
              }.thenByDescending { it.updatedAt?.seconds ?: 0L }
            )
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun reviewTechnicianAdmin(
    techId: String,
    verificationStatus: VerificationStatus,
    verifiedBadge: Boolean,
    adminRemarks: String,
    listed: Boolean,
  ): Result<Unit> {
    val path = "technicians/$techId"
    return try {
      requireUserId()
      db.collection("technicians")
        .document(techId)
        .update(
          mapOf(
            "verificationStatus" to verificationStatus.name,
            "verifiedBadge" to verifiedBadge,
            "adminRemarks" to adminRemarks.trim().take(240),
            "listed" to listed,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  fun observeAllBookingsAdmin(): Flow<List<ServiceBooking>> = flow {
    val uid = requireUserId()
    val path = "bookings"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceBooking::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedByDescending { it.createdAt?.seconds ?: 0L }
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  fun observeAllComplaintsAdmin(): Flow<List<ServiceComplaint>> = flow {
    val uid = requireUserId()
    val path = "complaints"
    emitAll(
      db.collection(path)
        .snapshots()
        .map { snapshot ->
          snapshot
            .toObjects(
              ServiceComplaint::class.java,
              DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            .sortedWith(
              compareBy<ServiceComplaint> {
                when (it.statusEnum) {
                  ComplaintStatus.OPEN -> 0
                  ComplaintStatus.INVESTIGATING -> 1
                  ComplaintStatus.RESOLVED -> 2
                }
              }.thenByDescending { it.createdAt?.seconds ?: 0L }
            )
        }
        .logFirestoreErrors(OperationType.LIST, path, uid)
    )
  }

  suspend fun resolveComplaintAdmin(
    complaintId: String,
    status: ComplaintStatus,
    adminResolution: String,
  ): Result<Unit> {
    val path = "complaints/$complaintId"
    return try {
      requireUserId()
      db.collection("complaints")
        .document(complaintId)
        .update(
          mapOf(
            "status" to status.name,
            "adminResolution" to adminResolution.trim().take(400),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }
}
