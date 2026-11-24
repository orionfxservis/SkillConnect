package com.example.data

import android.content.Context
import com.example.model.SubscriptionTier

class SubscriptionPreferencesStore(context: Context) {
  private val prefs =
    context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

  fun loadTier(userId: String?): SubscriptionTier {
    if (userId.isNullOrBlank()) return SubscriptionTier.FREE
    return SubscriptionTier.fromRaw(prefs.getString(tierKey(userId), SubscriptionTier.FREE.name))
  }

  fun loadProRenewalMillis(userId: String?): Long? {
    if (userId.isNullOrBlank()) return null
    val value = prefs.getLong(renewalKey(userId), 0L)
    return if (value > 0L) value else null
  }

  fun savePlan(userId: String?, tier: SubscriptionTier, proRenewalAtMillis: Long? = null) {
    if (userId.isNullOrBlank()) return
    val editor = prefs.edit().putString(tierKey(userId), tier.name)
    if (tier == SubscriptionTier.PRO && proRenewalAtMillis != null && proRenewalAtMillis > 0L) {
      editor.putLong(renewalKey(userId), proRenewalAtMillis)
    } else if (tier == SubscriptionTier.FREE) {
      editor.remove(renewalKey(userId))
    }
    editor.apply()
  }

  private fun tierKey(userId: String) = "sub_tier_$userId"

  private fun renewalKey(userId: String) = "sub_renewal_$userId"

  companion object {
    private const val PREFS_NAME = "skillconnect_subscription_prefs"
  }
}
