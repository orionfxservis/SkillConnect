package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  var isLoading by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = onAuthSuccess,
      onUnauthenticated = {},
      scope = scope,
    )
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            colors =
              listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                MaterialTheme.colorScheme.background,
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.06f),
              )
          )
        )
        .safeDrawingPadding()
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 560.dp)
          .align(Alignment.Center)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 24.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween,
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.Center,
        ) {
          Image(
            painter = painterResource(id = R.drawable.skillconnect_logo),
            contentDescription = "SkillConnect logo",
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(54.dp).clip(RoundedCornerShape(13.dp)),
          )
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = "SkillConnect",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
            )
            Text(
              text = "Find • Hire • Get It Done",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Card(
          shape = RoundedCornerShape(24.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Image(
            painter = painterResource(id = R.drawable.img_hero_technician),
            contentDescription = "Verified SkillConnect technician",
            contentScale = ContentScale.Crop,
            alignment = Alignment.TopCenter,
            modifier = Modifier.fillMaxWidth().height(210.dp),
          )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
          text = "Skilled Help. Just a Tap Away!",
          style = MaterialTheme.typography.headlineMedium,
          color = MaterialTheme.colorScheme.onBackground,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
          text =
            "Find available technicians in your area, track live dispatch, message in-app, and pay directly after work — 100% free platform.",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(20.dp))

        FeaturePillRow(
          icon = Icons.Rounded.LocationOn,
          title = "Real-Time Dispatch Tracking",
          subtitle = "See nearby available pros & live arrival location",
        )
        Spacer(modifier = Modifier.height(10.dp))
        FeaturePillRow(
          icon = Icons.Rounded.Star,
          title = "Accountable Community Ratings",
          subtitle = "Verified post-service reviews update pro scores",
        )
        Spacer(modifier = Modifier.height(10.dp))
        FeaturePillRow(
          icon = Icons.Rounded.VerifiedUser,
          title = "Zero Payment Gate",
          subtitle = "Free to enroll, free to book — settle directly with pros",
        )
      }

      Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        if (errorMessage != null) {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
          ) {
            Text(
              text = errorMessage!!,
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(12.dp),
            )
          }
        }

        Button(
          onClick = {
            errorMessage = null
            isLoading = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isLoading = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isLoading = false
                errorMessage = msg
              },
              scope = scope,
              onAuthCancelled = { isLoading = false },
            )
          },
          enabled = !isLoading,
          shape = RoundedCornerShape(16.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
          modifier =
            Modifier.fillMaxWidth().height(56.dp).testTag("google_sign_in_button"),
        ) {
          if (isLoading) {
            CircularProgressIndicator(
              modifier = Modifier.size(22.dp),
              color = MaterialTheme.colorScheme.onPrimary,
              strokeWidth = 2.5.dp,
            )
          } else {
            Text(
              text = "Sign in with Google",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "Your profile, technician listing, and bookings stay safely synced.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center,
        )
      }
    }
  }
}

@Composable
private fun FeaturePillRow(icon: ImageVector, title: String, subtitle: String) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 1.dp,
    modifier = Modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = icon,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.onPrimaryContainer,
          modifier = Modifier.size(20.dp),
        )
      }
      Spacer(modifier = Modifier.width(12.dp))
      Column {
        Text(
          text = title,
          style = MaterialTheme.typography.titleSmall,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

private fun resolveDefaultWebClientId(context: Context): String? {
  val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
  if (resId == 0) return null
  return runCatching { context.getString(resId) }.getOrNull()?.takeIf { it.isNotBlank() }
}

fun attemptAutoSignIn(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onUnauthenticated: () -> Unit,
  scope: CoroutineScope,
) {
  if (Firebase.auth.currentUser != null) {
    onAuthSuccess()
    return
  }
  val clientId =
    resolveDefaultWebClientId(context)
      ?: run {
        onUnauthenticated()
        return
      }
  val googleIdOption =
    GetGoogleIdOption.Builder()
      .setFilterByAuthorizedAccounts(true)
      .setServerClientId(clientId)
      .setAutoSelectEnabled(true)
      .build()
  val request = GetCredentialRequest.Builder().addCredentialOption(googleIdOption).build()
  scope.launch {
    try {
      val cred = credentialManager.getCredential(context, request).credential
      if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
        Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
        onAuthSuccess()
      } else {
        onUnauthenticated()
      }
    } catch (_: Exception) {
      onUnauthenticated()
    }
  }
}

fun onGoogleSignInClicked(
  context: Context,
  credentialManager: CredentialManager,
  onAuthSuccess: () -> Unit,
  onAuthError: (String) -> Unit,
  scope: CoroutineScope,
  onAuthCancelled: () -> Unit = {},
) {
  val clientId =
    resolveDefaultWebClientId(context)
      ?: run {
        onAuthError("Google Sign-In configuration missing: default_web_client_id not found")
        return
      }
  val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
  val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()
  scope.launch {
    try {
      val activity = context as? Activity
      if (activity == null) {
        onAuthError("Unable to launch Google Sign-In from current context")
        return@launch
      }
      val cred = credentialManager.getCredential(activity, request).credential
      if (cred is CustomCredential && cred.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        val token = GoogleIdTokenCredential.createFrom(cred.data).idToken
        Firebase.auth.signInWithCredential(GoogleAuthProvider.getCredential(token, null)).await()
        onAuthSuccess()
      } else {
        onAuthError("Unexpected credential type")
      }
    } catch (e: GetCredentialCancellationException) {
      Log.w("Auth", "Google Sign-In flow cancelled or dismissed: ${e.message}", e)
      onAuthCancelled()
    } catch (e: Exception) {
      Log.e("Auth", "Google Sign-In failed", e)
      onAuthError(e.localizedMessage ?: "Sign in failed")
    }
  }
}

fun signOutUser(
  credentialManager: CredentialManager,
  scope: CoroutineScope,
  onSignOutComplete: () -> Unit = {},
) {
  scope.launch {
    withContext(NonCancellable) {
      try {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
      } catch (e: Exception) {
        Log.w("Auth", "Failed to clear credential state: ${e.message}")
      } finally {
        Firebase.auth.signOut()
        onSignOutComplete()
      }
    }
  }
}
