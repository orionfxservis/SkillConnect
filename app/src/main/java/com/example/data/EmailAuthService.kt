package com.example.data

import com.example.BuildConfig
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.security.SecureRandom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

sealed interface VerificationSendResult {
  data class Sent(val issuedCode: String, val expiresAtMillis: Long) : VerificationSendResult

  data class NeedsConfiguration(val message: String) : VerificationSendResult

  data class Failed(val message: String) : VerificationSendResult
}

sealed interface EmailAuthResult {
  data object Success : EmailAuthResult

  data class Error(val message: String) : EmailAuthResult
}

object EmailAuthService {
  private val secureRandom = SecureRandom()
  private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
  private const val CODE_TTL_MS = 10 * 60 * 1000L

  fun isValidEmail(raw: String): Boolean {
    val trimmed = raw.trim()
    return trimmed.isNotEmpty() && EMAIL_REGEX.matches(trimmed)
  }

  fun isGmailAddress(raw: String): Boolean {
    val domain = raw.trim().substringAfter("@", "").lowercase()
    return domain == "gmail.com" || domain == "googlemail.com"
  }

  private fun hasConfiguredResendKey(): Boolean {
    val key = BuildConfig.RESEND_API_KEY.trim()
    return key.isNotEmpty() && !key.startsWith("YOUR_")
  }

  suspend fun sendVerificationCode(
    email: String,
    categoryName: String,
  ): VerificationSendResult = withContext(Dispatchers.IO) {
    val cleanEmail = email.trim()
    if (!isValidEmail(cleanEmail)) {
      return@withContext VerificationSendResult.Failed(
        "Please enter a valid active email address (Gmail preferred)."
      )
    }

    if (!hasConfiguredResendKey()) {
      return@withContext VerificationSendResult.NeedsConfiguration(
        "Live email code delivery is not configured yet. Add RESEND_API_KEY in AI Studio Secrets to mail verification codes to Gmail."
      )
    }

    val code = (100000 + secureRandom.nextInt(900000)).toString()
    val expiresAt = System.currentTimeMillis() + CODE_TTL_MS

    try {
      val url = URL("https://api.resend.com/emails")
      val connection = (url.openConnection() as HttpURLConnection).apply {
        requestMethod = "POST"
        connectTimeout = 12_000
        readTimeout = 12_000
        doOutput = true
        setRequestProperty("Authorization", "Bearer ${BuildConfig.RESEND_API_KEY.trim()}")
        setRequestProperty("Content-Type", "application/json")
      }

      val body =
        JSONObject()
          .put("from", "SkillConnect <onboarding@resend.dev>")
          .put("to", JSONArray().put(cleanEmail))
          .put("subject", "Your SkillConnect Verification Code: $code")
          .put(
            "text",
            "Hello,\n\nYour SkillConnect verification code for $categoryName registration is: $code\n\nThis code expires in 10 minutes.\n\nIf you did not request this, you can safely ignore this email.",
          )
          .toString()

      OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
        writer.write(body)
        writer.flush()
      }

      val statusCode = connection.responseCode
      if (statusCode in 200..299) {
        VerificationSendResult.Sent(issuedCode = code, expiresAtMillis = expiresAt)
      } else {
        val errorBody =
          runCatching { connection.errorStream?.bufferedReader()?.readText() }.getOrNull().orEmpty()
        val parsedMsg =
          runCatching { JSONObject(errorBody).optString("message") }.getOrNull()
            ?.takeIf { it.isNotBlank() }
        VerificationSendResult.Failed(
          parsedMsg ?: "Failed to send verification email (HTTP $statusCode)."
        )
      }
    } catch (e: Exception) {
      VerificationSendResult.Failed(
        e.localizedMessage ?: "Network error while sending verification email."
      )
    }
  }

  suspend fun createAccountWithEmail(
    email: String,
    password: String,
    categoryName: String,
  ): EmailAuthResult {
    val cleanEmail = email.trim()
    if (!isValidEmail(cleanEmail)) {
      return EmailAuthResult.Error("Please enter a valid email address.")
    }
    if (password.length < 6) {
      return EmailAuthResult.Error("Password must be at least 6 characters.")
    }
    return try {
      val result = Firebase.auth.createUserWithEmailAndPassword(cleanEmail, password).await()
      val user = result.user
      if (user != null) {
        val displayPrefix = cleanEmail.substringBefore("@").take(40).ifBlank { categoryName }
        runCatching {
          user.updateProfile(
            UserProfileChangeRequest.Builder().setDisplayName(displayPrefix).build()
          ).await()
        }
        runCatching { user.sendEmailVerification().await() }
      }
      EmailAuthResult.Success
    } catch (e: FirebaseAuthException) {
      EmailAuthResult.Error(mapFirebaseAuthError(e))
    } catch (e: Exception) {
      EmailAuthResult.Error(e.localizedMessage ?: "Account creation failed.")
    }
  }

  suspend fun signInWithEmail(
    email: String,
    password: String,
  ): EmailAuthResult {
    val cleanEmail = email.trim()
    if (!isValidEmail(cleanEmail)) {
      return EmailAuthResult.Error("Please enter a valid email address.")
    }
    if (password.isEmpty()) {
      return EmailAuthResult.Error("Please enter your password.")
    }
    return try {
      Firebase.auth.signInWithEmailAndPassword(cleanEmail, password).await()
      EmailAuthResult.Success
    } catch (e: FirebaseAuthException) {
      EmailAuthResult.Error(mapFirebaseAuthError(e))
    } catch (e: Exception) {
      EmailAuthResult.Error(e.localizedMessage ?: "Login failed.")
    }
  }

  private fun mapFirebaseAuthError(e: FirebaseAuthException): String =
    when (e.errorCode) {
      "ERROR_OPERATION_NOT_ALLOWED",
      "CONFIGURATION_NOT_FOUND" ->
        "Email/Password login is not enabled in Firebase Authentication yet. Enable Email/Password in Firebase Console → Authentication → Sign-in method."
      "ERROR_EMAIL_ALREADY_IN_USE" ->
        "An account with this email already exists. Switch to the Login tab above to sign in."
      "ERROR_WRONG_PASSWORD",
      "ERROR_INVALID_CREDENTIAL",
      "ERROR_USER_NOT_FOUND" -> "Incorrect email or password. Please check and try again."
      "ERROR_WEAK_PASSWORD" -> "Please choose a stronger password (at least 6 characters)."
      else -> e.localizedMessage ?: "Authentication error (${e.errorCode})."
    }
}
