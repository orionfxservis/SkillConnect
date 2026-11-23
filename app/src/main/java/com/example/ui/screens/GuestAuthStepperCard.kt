package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.EmailAuthResult
import com.example.data.EmailAuthService
import com.example.data.VerificationSendResult
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.blueLoginTextFieldColors
import com.example.ui.theme.greenThemeTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun GuestAuthStepperCard(
  categories: List<ServiceCategory> = ServiceCategories.defaults,
) {
  val themeGreen = MaterialTheme.colorScheme.primary
  val pillTrackColor = MaterialTheme.colorScheme.surfaceVariant
  val inactiveCircleBg = MaterialTheme.colorScheme.outlineVariant
  val inactiveText = MaterialTheme.colorScheme.onSurfaceVariant
  val fieldLabelColor = MaterialTheme.colorScheme.onSurface
  val greenFieldColors = greenThemeTextFieldColors()
  val blueLoginColors = blueLoginTextFieldColors()

  val scope = rememberCoroutineScope()
  val allCategories = remember(categories) { categories.ifEmpty { ServiceCategories.defaults } }

  var isSignUpMode by remember { mutableStateOf(true) }
  var activeStep by remember { mutableIntStateOf(0) }
  var selectedCategoryId by remember(allCategories) {
    mutableStateOf(allCategories.firstOrNull()?.id ?: "electrician")
  }
  var emailInput by remember { mutableStateOf("") }
  var verificationCodeInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var confirmPasswordInput by remember { mutableStateOf("") }
  var showPassword by remember { mutableStateOf(false) }

  var issuedCode by remember { mutableStateOf<String?>(null) }
  var codeExpiresAtMillis by remember { mutableLongStateOf(0L) }
  var verifyAttempts by remember { mutableIntStateOf(0) }
  var codeVerified by remember { mutableStateOf(false) }

  var isSubmitting by remember { mutableStateOf(false) }
  var statusMessage by remember { mutableStateOf<String?>(null) }
  var isErrorBanner by remember { mutableStateOf(false) }

  val selectedCategory =
    remember(selectedCategoryId, allCategories) {
      ServiceCategories.byId(selectedCategoryId, allCategories)
    }

  fun resetMessages() {
    statusMessage = null
    isErrorBanner = false
  }

  fun sendCodeForCurrentRole() {
    val cleanEmail = emailInput.trim()
    if (!EmailAuthService.isValidEmail(cleanEmail)) {
      statusMessage = "Please enter an active email address (Gmail preferred)."
      isErrorBanner = true
      return
    }
    resetMessages()
    isSubmitting = true
    scope.launch {
      when (val result = EmailAuthService.sendVerificationCode(cleanEmail, selectedCategory.name)) {
        is VerificationSendResult.Sent -> {
          issuedCode = result.issuedCode
          codeExpiresAtMillis = result.expiresAtMillis
          verifyAttempts = 0
          codeVerified = false
          verificationCodeInput = ""
          statusMessage = "Verification code sent to $cleanEmail. Check your inbox."
          isErrorBanner = false
          activeStep = 1
        }
        is VerificationSendResult.UseFirebaseEmailLink -> {
          issuedCode = null
          codeExpiresAtMillis = 0L
          verifyAttempts = 0
          codeVerified = true
          verificationCodeInput = ""
          statusMessage = result.message
          isErrorBanner = false
          activeStep = 2
        }
        is VerificationSendResult.NeedsConfiguration -> {
          statusMessage = result.message
          isErrorBanner = true
        }
        is VerificationSendResult.Failed -> {
          statusMessage = result.message
          isErrorBanner = true
        }
      }
      isSubmitting = false
    }
  }

  Card(
    shape = RoundedCornerShape(22.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth().testTag("guest_auth_stepper_card"),
  ) {
    Column(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      // Segmented Login / Sign Up Switch
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = pillTrackColor,
        modifier = Modifier.fillMaxWidth(),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(3.dp),
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Surface(
            onClick = {
              isSignUpMode = false
              resetMessages()
            },
            shape = RoundedCornerShape(11.dp),
            color = if (!isSignUpMode) MaterialTheme.colorScheme.surface else Color.Transparent,
            shadowElevation = if (!isSignUpMode) 2.dp else 0.dp,
            modifier = Modifier.weight(1f).height(40.dp).testTag("auth_tab_login"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "Login",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (!isSignUpMode) FontWeight.SemiBold else FontWeight.Medium,
                color = if (!isSignUpMode) themeGreen else inactiveText,
              )
            }
          }

          Surface(
            onClick = {
              isSignUpMode = true
              resetMessages()
            },
            shape = RoundedCornerShape(11.dp),
            color = if (isSignUpMode) MaterialTheme.colorScheme.surface else Color.Transparent,
            shadowElevation = if (isSignUpMode) 2.dp else 0.dp,
            modifier = Modifier.weight(1f).height(40.dp).testTag("auth_tab_signup"),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "Sign Up",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (isSignUpMode) FontWeight.SemiBold else FontWeight.Medium,
                color = if (isSignUpMode) themeGreen else inactiveText,
              )
            }
          }
        }
      }

      if (isSignUpMode) {
        // 3-Step Indicator: 1 Role — 2 Verify — 3 Password
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          AuthStepBadge(
            stepNumber = 1,
            label = "Role",
            active = activeStep == 0,
            completed = activeStep > 0,
            accentColor = themeGreen,
            inactiveBg = inactiveCircleBg,
            inactiveText = inactiveText,
            onClick = {
              activeStep = 0
              resetMessages()
            },
          )
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
          )
          AuthStepBadge(
            stepNumber = 2,
            label = "Verify",
            active = activeStep == 1,
            completed = activeStep > 1 || codeVerified,
            accentColor = themeGreen,
            inactiveBg = inactiveCircleBg,
            inactiveText = inactiveText,
            onClick = {
              if (issuedCode != null) {
                activeStep = 1
                resetMessages()
              }
            },
          )
          HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp,
            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
          )
          AuthStepBadge(
            stepNumber = 3,
            label = "Password",
            active = activeStep == 2,
            completed = false,
            accentColor = themeGreen,
            inactiveBg = inactiveCircleBg,
            inactiveText = inactiveText,
            onClick = {
              if (codeVerified) {
                activeStep = 2
                resetMessages()
              }
            },
          )
        }

        if (statusMessage != null) {
          AuthStatusBanner(message = statusMessage!!, isError = isErrorBanner)
        }

        when (activeStep) {
          // STEP 1: ROLE (Email + Left-to-Right Scrolling Category Picker + Send Verification Code)
          0 -> {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "WORK EMAIL ADDRESS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = fieldLabelColor,
              )
              OutlinedTextField(
                value = emailInput,
                onValueChange = {
                  emailInput = it
                  resetMessages()
                },
                placeholder = { Text("you@gmail.com (Gmail preferred)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                shape = RoundedCornerShape(14.dp),
                colors = greenFieldColors,
                modifier = Modifier.fillMaxWidth().testTag("auth_work_email_input"),
              )

              Text(
                text = "SELECT YOUR CATEGORY",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = fieldLabelColor,
              )

              LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 2.dp),
                modifier = Modifier.fillMaxWidth().testTag("auth_category_carousel"),
              ) {
                items(allCategories, key = { it.id }) { category ->
                  val selected = selectedCategoryId == category.id
                  AuthRoleCategoryCard(
                    category = category,
                    selected = selected,
                    accentColor = themeGreen,
                    defaultBorder = MaterialTheme.colorScheme.outlineVariant,
                    onClick = {
                      selectedCategoryId = category.id
                      resetMessages()
                    },
                  )
                }
              }

              Button(
                onClick = { sendCodeForCurrentRole() },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = themeGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ),
                modifier =
                  Modifier.fillMaxWidth().height(48.dp).testTag("send_verification_code_button"),
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.2.dp,
                  )
                } else {
                  Text(
                    text = "Send Verification Code",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                  )
                }
              }
            }
          }

          // STEP 2: VERIFY (6-Digit Code Entry)
          1 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "ENTER VERIFICATION CODE",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.8.sp,
                    color = fieldLabelColor,
                  )
                  Text(
                    text = "${emailInput.trim()} • ${selectedCategory.name}",
                    style = MaterialTheme.typography.bodySmall,
                    color = inactiveText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
                TextButton(
                  onClick = {
                    activeStep = 0
                    resetMessages()
                  }
                ) {
                  Text("Change", color = themeGreen)
                }
              }

              OutlinedTextField(
                value = verificationCodeInput,
                onValueChange = { raw ->
                  val digits = raw.filter { it.isDigit() }.take(6)
                  verificationCodeInput = digits
                  resetMessages()
                },
                placeholder = { Text("6-digit code") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(14.dp),
                colors = greenFieldColors,
                modifier = Modifier.fillMaxWidth().testTag("auth_verification_code_input"),
              )

              Button(
                onClick = {
                  val expected = issuedCode
                  val entered = verificationCodeInput.trim()
                  when {
                    expected.isNullOrBlank() -> {
                      statusMessage = "Please request a verification code first."
                      isErrorBanner = true
                    }
                    System.currentTimeMillis() > codeExpiresAtMillis -> {
                      statusMessage = "Verification code expired. Tap Resend Code below."
                      isErrorBanner = true
                    }
                    verifyAttempts >= 5 -> {
                      statusMessage = "Too many incorrect attempts. Please request a new code."
                      isErrorBanner = true
                    }
                    entered == expected -> {
                      codeVerified = true
                      resetMessages()
                      activeStep = 2
                    }
                    else -> {
                      verifyAttempts += 1
                      statusMessage = "Incorrect verification code. Please check your email."
                      isErrorBanner = true
                    }
                  }
                },
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = themeGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ),
                modifier =
                  Modifier.fillMaxWidth().height(50.dp).testTag("verify_code_continue_button"),
              ) {
                Text("Verify Code", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
              }

              TextButton(
                onClick = { sendCodeForCurrentRole() },
                enabled = !isSubmitting,
                modifier = Modifier.align(Alignment.CenterHorizontally),
              ) {
                Text("Resend Verification Code", color = themeGreen)
              }
            }
          }

          // STEP 3: PASSWORD (Create Account)
          2 -> {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              Text(
                text = "CREATE PASSWORD",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.8.sp,
                color = fieldLabelColor,
              )
              Text(
                text =
                  if (issuedCode != null) "Verified: ${emailInput.trim()} (${selectedCategory.name})"
                  else "Account: ${emailInput.trim()} (${selectedCategory.name})",
                style = MaterialTheme.typography.bodySmall,
                color = OnEmeraldContainer,
                fontWeight = FontWeight.Medium,
              )

              OutlinedTextField(
                value = passwordInput,
                onValueChange = {
                  passwordInput = it
                  resetMessages()
                },
                placeholder = { Text("Minimum 6 characters") },
                singleLine = true,
                visualTransformation =
                  if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                  IconButton(onClick = { showPassword = !showPassword }) {
                    Icon(
                      imageVector =
                        if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                      contentDescription = "Toggle password visibility",
                    )
                  }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = greenFieldColors,
                modifier = Modifier.fillMaxWidth().testTag("auth_signup_password_input"),
              )

              OutlinedTextField(
                value = confirmPasswordInput,
                onValueChange = {
                  confirmPasswordInput = it
                  resetMessages()
                },
                placeholder = { Text("Confirm password") },
                singleLine = true,
                visualTransformation =
                  if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                shape = RoundedCornerShape(14.dp),
                colors = greenFieldColors,
                modifier = Modifier.fillMaxWidth().testTag("auth_signup_confirm_password_input"),
              )

              Button(
                onClick = {
                  if (passwordInput.length < 6) {
                    statusMessage = "Password must be at least 6 characters."
                    isErrorBanner = true
                    return@Button
                  }
                  if (passwordInput != confirmPasswordInput) {
                    statusMessage = "Passwords do not match."
                    isErrorBanner = true
                    return@Button
                  }
                  resetMessages()
                  isSubmitting = true
                  scope.launch {
                    when (
                      val result =
                        EmailAuthService.createAccountWithEmail(
                          email = emailInput,
                          password = passwordInput,
                          categoryName = selectedCategory.name,
                          categoryId = selectedCategory.id,
                        )
                    ) {
                      is EmailAuthResult.Success -> {
                        statusMessage = "Account created! Completing setup..."
                        isErrorBanner = false
                      }
                      is EmailAuthResult.Error -> {
                        statusMessage = result.message
                        isErrorBanner = true
                      }
                    }
                    isSubmitting = false
                  }
                },
                enabled = !isSubmitting,
                shape = RoundedCornerShape(14.dp),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = themeGreen,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                  ),
                modifier =
                  Modifier.fillMaxWidth().height(50.dp).testTag("create_account_submit_button"),
              ) {
                if (isSubmitting) {
                  CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.2.dp,
                  )
                } else {
                  Text("Create Account", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }
              }
            }
          }
        }
      } else {
        // LOGIN TAB (Blue outline input fields per user preference + green default theme button)
        if (statusMessage != null) {
          AuthStatusBanner(message = statusMessage!!, isError = isErrorBanner)
        }

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "WORK EMAIL ADDRESS",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = fieldLabelColor,
          )
          OutlinedTextField(
            value = emailInput,
            onValueChange = {
              emailInput = it
              resetMessages()
            },
            placeholder = { Text("you@gmail.com") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            shape = RoundedCornerShape(14.dp),
            colors = blueLoginColors,
            modifier = Modifier.fillMaxWidth().testTag("auth_login_email_input"),
          )

          Text(
            text = "PASSWORD",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.8.sp,
            color = fieldLabelColor,
          )
          OutlinedTextField(
            value = passwordInput,
            onValueChange = {
              passwordInput = it
              resetMessages()
            },
            placeholder = { Text("Enter password") },
            singleLine = true,
            visualTransformation =
              if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
              IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                  imageVector =
                    if (showPassword) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                  contentDescription = "Toggle password visibility",
                )
              }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            shape = RoundedCornerShape(14.dp),
            colors = blueLoginColors,
            modifier = Modifier.fillMaxWidth().testTag("auth_login_password_input"),
          )

          Button(
            onClick = {
              resetMessages()
              isSubmitting = true
              scope.launch {
                when (val result = EmailAuthService.signInWithEmail(emailInput, passwordInput)) {
                  is EmailAuthResult.Success -> {
                    statusMessage = "Signed in successfully."
                    isErrorBanner = false
                  }
                  is EmailAuthResult.Error -> {
                    statusMessage = result.message
                    isErrorBanner = true
                  }
                }
                isSubmitting = false
              }
            },
            enabled = !isSubmitting,
            shape = RoundedCornerShape(14.dp),
            colors =
              ButtonDefaults.buttonColors(
                containerColor = themeGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary,
              ),
            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("auth_login_submit_button"),
          ) {
            if (isSubmitting) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.2.dp,
              )
            } else {
              Text("Login", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AuthRoleCategoryCard(
  category: ServiceCategory,
  selected: Boolean,
  accentColor: Color,
  defaultBorder: Color,
  onClick: () -> Unit,
) {
  val shortSub =
    remember(category.id) {
      category.subcategories.firstOrNull() ?: category.subtitle.substringBefore(",")
    }
  Surface(
    onClick = onClick,
    shape = RoundedCornerShape(14.dp),
    color = if (selected) EmeraldContainer.copy(alpha = 0.45f) else MaterialTheme.colorScheme.surface,
    border =
      BorderStroke(
        width = if (selected) 2.dp else 1.dp,
        color = if (selected) accentColor else defaultBorder,
      ),
    modifier = Modifier.width(106.dp).height(78.dp).testTag("auth_category_${category.id}"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = category.icon,
        contentDescription = category.name,
        tint = if (selected) accentColor else category.tint,
        modifier = Modifier.size(24.dp),
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = category.name,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = if (selected) accentColor else MaterialTheme.colorScheme.onSurface,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
      Text(
        text = shortSub,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        color = if (selected) accentColor.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}

@Composable
private fun AuthStatusBanner(
  message: String,
  isError: Boolean,
) {
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = if (isError) MaterialTheme.colorScheme.errorContainer else EmeraldContainer,
    border =
      BorderStroke(
        1.dp,
        if (isError) MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
        else MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
      ),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Text(
      text = message,
      style = MaterialTheme.typography.bodySmall,
      color = if (isError) MaterialTheme.colorScheme.onErrorContainer else OnEmeraldContainer,
      modifier = Modifier.padding(12.dp),
    )
  }
}

@Composable
private fun AuthStepBadge(
  stepNumber: Int,
  label: String,
  active: Boolean,
  completed: Boolean,
  accentColor: Color,
  inactiveBg: Color,
  inactiveText: Color,
  onClick: () -> Unit,
) {
  Row(
    modifier = Modifier.clickable(onClick = onClick),
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
  ) {
    Box(
      modifier =
        Modifier.size(30.dp)
          .clip(CircleShape)
          .background(if (active) accentColor.copy(alpha = 0.16f) else Color.Transparent),
      contentAlignment = Alignment.Center,
    ) {
      Box(
        modifier =
          Modifier.size(24.dp)
            .clip(CircleShape)
            .background(if (active || completed) accentColor else inactiveBg),
        contentAlignment = Alignment.Center,
      ) {
        if (completed) {
          Icon(
            imageVector = Icons.Rounded.Check,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(14.dp),
          )
        } else {
          Text(
            text = stepNumber.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (active) MaterialTheme.colorScheme.onPrimary else inactiveText,
          )
        }
      }
    }
    Text(
      text = label,
      style = MaterialTheme.typography.labelLarge,
      fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
      color =
        if (active) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
      maxLines = 1,
      softWrap = false,
    )
  }
}
