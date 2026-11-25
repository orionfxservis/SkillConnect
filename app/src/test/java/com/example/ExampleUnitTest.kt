package com.example

import com.example.data.EmailAuthService
import com.example.data.LocationHelper
import com.example.model.BookingStatus
import com.example.model.ComplaintStatus
import com.example.model.Coordinates
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.ServiceCategoryEntity
import com.example.model.SubscriptionRulesCalculator
import com.example.model.SubscriptionTier
import com.example.model.TechnicianProfile
import com.example.model.VerificationStatus
import com.example.ui.screens.CategoryPromoAction
import com.example.ui.screens.CategoryPromoSlides
import com.example.ui.sortCandidatesNearToFar
import com.google.firebase.Timestamp
import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun distance_calculation_isAccurate() {
    val km = LocationHelper.distanceKm(37.7749, -122.4194, 37.7849, -122.4094)
    assertTrue("Expected ~1.4 km, got $km", km in 1.1..1.7)
    assertEquals("Area listed", LocationHelper.formatDistance(-1.0))
    assertEquals("1.4 km away", LocationHelper.formatDistance(1.4))
  }

  @Test
  fun categories_and_statuses_resolveSafely() {
    assertEquals(21, ServiceCategories.defaults.size)
    assertEquals("Electrician", ServiceCategories.byId("electrical").name)
    assertEquals("Electrician", ServiceCategories.byId("electrician").name)
    assertEquals("General Labour", ServiceCategories.byId("unknown_id").name)
    assertEquals(BookingStatus.EN_ROUTE, BookingStatus.fromRaw("EN_ROUTE"))
    assertEquals(BookingStatus.REQUESTED, BookingStatus.fromRaw("LEGACY_UNKNOWN"))
    assertEquals(VerificationStatus.APPROVED, VerificationStatus.fromRaw("APPROVED"))
    assertEquals(ComplaintStatus.INVESTIGATING, ComplaintStatus.fromRaw("INVESTIGATING"))

    val merged =
      ServiceCategories.mergeWithRemote(
        listOf(
          ServiceCategoryEntity(
            id = "drone_tech",
            categoryId = "drone_tech",
            name = "Drone Repair",
            subtitle = "Motors and gimbals",
            iconKey = "build",
            subcategories = listOf("Gimbal"),
            active = true,
            sortOrder = 22L,
          )
        )
      )
    assertEquals(22, merged.size)
    assertEquals("Drone Repair", ServiceCategories.byId("drone_tech", merged).name)
  }

  @Test
  fun nearToFar_sorting_placesClosestCandidatesFirst() {
    val user = Coordinates(latitude = 24.8607, longitude = 67.0011)
    val farTech =
      TechnicianProfile(
        userId = "far",
        fullName = "Far Pro",
        contactPhone = "+923001111111",
        categoryId = "electrician",
        availableNow = true,
        verifiedBadge = true,
        latitude = 24.9607,
        longitude = 67.1011,
      )
    val nearTech =
      TechnicianProfile(
        userId = "near",
        fullName = "Near Pro",
        contactPhone = "+923002222222",
        categoryId = "electrician",
        availableNow = false,
        verifiedBadge = false,
        latitude = 24.8650,
        longitude = 67.0050,
      )
    val noGpsTech =
      TechnicianProfile(
        userId = "nogps",
        fullName = "Area Only Pro",
        contactPhone = "+923003333333",
        categoryId = "electrician",
        latitude = 0.0,
        longitude = 0.0,
      )
    val rejectedTech =
      TechnicianProfile(
        userId = "rej",
        fullName = "Rejected Pro",
        contactPhone = "+923004444444",
        categoryId = "electrician",
        verificationStatus = VerificationStatus.REJECTED.name,
        latitude = 24.8608,
        longitude = 67.0012,
      )

    val sorted = sortCandidatesNearToFar(listOf(farTech, noGpsTech, nearTech, rejectedTech), user)
    assertEquals(listOf("near", "far", "nogps"), sorted.map { it.technician.userId })
  }

  @Test
  fun email_validation_and_gmailDetection_workCorrectly() {
    assertTrue(EmailAuthService.isValidEmail("skilled.tech@gmail.com"))
    assertTrue(EmailAuthService.isGmailAddress("skilled.tech@gmail.com"))
    assertTrue(EmailAuthService.isValidEmail("pro@company.org"))
    assertFalse(EmailAuthService.isGmailAddress("pro@company.org"))
    assertFalse(EmailAuthService.isValidEmail("not-an-email"))
    assertFalse(EmailAuthService.isValidEmail(""))
  }

  @Test
  fun subscription_freeAndProRules_andMonthlyReset_workCorrectly() {
    val novCal =
      Calendar.getInstance().apply {
        set(2026, Calendar.NOVEMBER, 15, 10, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
    val octCal =
      Calendar.getInstance().apply {
        set(2026, Calendar.OCTOBER, 20, 10, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
    val novTs = Timestamp(novCal.timeInMillis / 1000L, 0)
    val octTs = Timestamp(octCal.timeInMillis / 1000L, 0)

    val bookings =
      (1..5).map { idx ->
        ServiceBooking(
          id = "nov_$idx",
          technicianId = "pro_1",
          status = BookingStatus.ACCEPTED.name,
          createdAt = novTs,
        )
      } +
        listOf(
          ServiceBooking(
            id = "oct_old",
            technicianId = "pro_1",
            status = BookingStatus.COMPLETED.name,
            createdAt = octTs,
          )
        )

    val freeStatus =
      SubscriptionRulesCalculator.computeStatus(
        tier = SubscriptionTier.FREE,
        allBookings = bookings,
        currentUserId = "pro_1",
        nowMillis = novCal.timeInMillis,
      )
    assertEquals(5, freeStatus.confirmedThisMonth)
    assertEquals(5, freeStatus.requestsThisMonth)
    assertEquals(6, freeStatus.totalHistoryCount)
    assertFalse(freeStatus.canConfirmAnotherBooking)
    assertFalse(freeStatus.canReceiveMoreRequests)

    val proStatus =
      SubscriptionRulesCalculator.computeStatus(
        tier = SubscriptionTier.PRO,
        allBookings = bookings,
        currentUserId = "pro_1",
        nowMillis = novCal.timeInMillis,
      )
    assertTrue(proStatus.canConfirmAnotherBooking)
    assertTrue(proStatus.canReceiveMoreRequests)
    assertEquals("Rs. 499/month", SubscriptionTier.PRO.feeLabel)
    assertEquals("Automatic reset", SubscriptionTier.FREE.renewalLabel)
    assertEquals("Subscription renewal", SubscriptionTier.PRO.renewalLabel)

    // Automatic reset in December
    val decCal =
      Calendar.getInstance().apply {
        set(2026, Calendar.DECEMBER, 2, 10, 0, 0)
        set(Calendar.MILLISECOND, 0)
      }
    val decResetStatus =
      SubscriptionRulesCalculator.computeStatus(
        tier = SubscriptionTier.FREE,
        allBookings = bookings,
        currentUserId = "pro_1",
        nowMillis = decCal.timeInMillis,
      )
    assertEquals(0, decResetStatus.confirmedThisMonth)
    assertEquals(6, decResetStatus.totalHistoryCount)
    assertTrue(decResetStatus.canConfirmAnotherBooking)
  }

  @Test
  fun categoryPromoSlides_containFiveUniqueSlidesAndImages() {
    val slides = CategoryPromoSlides.defaults
    assertEquals(5, slides.size)
    assertEquals(5, slides.map { it.imageRes }.toSet().size)
    assertEquals(5, slides.map { it.action }.toSet().size)

    assertEquals("Find the Right Professional!", slides[0].headline)
    assertEquals("Explore Services", slides[0].ctaLabel)
    assertEquals(CategoryPromoAction.EXPLORE_SERVICES, slides[0].action)

    assertEquals("Your Home, Our Professionals.", slides[1].headline)
    assertEquals("Find a Professional", slides[1].ctaLabel)

    assertEquals("Turn Your Skills Into Opportunities!", slides[2].headline)
    assertEquals("Join SkillConnect", slides[2].ctaLabel)

    assertEquals("Local Services Made Easy.", slides[3].headline)
    assertEquals("Find Nearby Services", slides[3].ctaLabel)

    assertEquals("Ready for More Bookings?", slides[4].headline)
    assertEquals("Explore Pro Plan", slides[4].ctaLabel)
    assertEquals(CategoryPromoAction.EXPLORE_PRO_PLAN, slides[4].action)
  }
}
