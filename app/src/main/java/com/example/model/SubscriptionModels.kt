package com.example.model

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class SubscriptionTier(
  val title: String,
  val feeLabel: String,
  val monthlyConfirmedLimit: Int?,
  val monthlyRequestLimit: Int?,
  val renewalLabel: String,
) {
  FREE(
    title = "Free",
    feeLabel = "Free",
    monthlyConfirmedLimit = 5,
    monthlyRequestLimit = 5,
    renewalLabel = "Automatic reset",
  ),
  PRO(
    title = "Pro",
    feeLabel = "Rs. 499/month",
    monthlyConfirmedLimit = null,
    monthlyRequestLimit = null,
    renewalLabel = "Subscription renewal",
  );

  companion object {
    fun fromRaw(raw: String?): SubscriptionTier =
      entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: FREE
  }
}

data class ProviderSubscriptionStatus(
  val tier: SubscriptionTier = SubscriptionTier.FREE,
  val confirmedThisMonth: Int = 0,
  val requestsThisMonth: Int = 0,
  val totalHistoryCount: Int = 0,
  val currentMonthLabel: String = "",
  val nextResetDateLabel: String = "",
  val proRenewalDateLabel: String? = null,
) {
  val canConfirmAnotherBooking: Boolean
    get() =
      tier.monthlyConfirmedLimit == null || confirmedThisMonth < tier.monthlyConfirmedLimit

  val canReceiveMoreRequests: Boolean
    get() =
      tier.monthlyRequestLimit == null || requestsThisMonth < tier.monthlyRequestLimit

  val remainingConfirmed: Int?
    get() = tier.monthlyConfirmedLimit?.let { (it - confirmedThisMonth).coerceAtLeast(0) }

  val remainingRequests: Int?
    get() = tier.monthlyRequestLimit?.let { (it - requestsThisMonth).coerceAtLeast(0) }
}

object SubscriptionRulesCalculator {
  private val confirmedStatuses =
    setOf(
      BookingStatus.ACCEPTED,
      BookingStatus.EN_ROUTE,
      BookingStatus.IN_PROGRESS,
      BookingStatus.COMPLETED,
    )

  fun computeStatus(
    tier: SubscriptionTier,
    allBookings: List<ServiceBooking>,
    currentUserId: String?,
    nowMillis: Long = System.currentTimeMillis(),
    proRenewalAtMillis: Long? = null,
  ): ProviderSubscriptionStatus {
    val monthCal =
      Calendar.getInstance().apply {
        timeInMillis = nowMillis
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
      }
    val monthStartMillis = monthCal.timeInMillis

    val nextMonthCal =
      (monthCal.clone() as Calendar).apply {
        add(Calendar.MONTH, 1)
      }

    val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    val dateFormat = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    val providerBookings =
      if (currentUserId.isNullOrBlank()) emptyList()
      else allBookings.filter { it.technicianId == currentUserId }

    val thisMonthBookings =
      providerBookings.filter { booking ->
        val createdSec = booking.createdAt?.seconds
        if (createdSec == null) {
          true
        } else {
          createdSec * 1000L >= monthStartMillis
        }
      }

    val confirmedThisMonth = thisMonthBookings.count { it.bookingStatus in confirmedStatuses }
    val requestsThisMonth = thisMonthBookings.count { it.bookingStatus != BookingStatus.CANCELLED }

    val proRenewalLabel =
      if (tier == SubscriptionTier.PRO && proRenewalAtMillis != null && proRenewalAtMillis > 0L) {
        dateFormat.format(Date(proRenewalAtMillis))
      } else {
        null
      }

    return ProviderSubscriptionStatus(
      tier = tier,
      confirmedThisMonth = confirmedThisMonth,
      requestsThisMonth = requestsThisMonth,
      totalHistoryCount = providerBookings.size,
      currentMonthLabel = monthFormat.format(Date(nowMillis)),
      nextResetDateLabel = dateFormat.format(nextMonthCal.time),
      proRenewalDateLabel = proRenewalLabel,
    )
  }
}
