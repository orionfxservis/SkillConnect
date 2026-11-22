package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.ImageEncodingHelper
import com.example.data.LocationHelper
import com.example.model.Coordinates
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.ServiceReview
import com.example.model.TechnicianProfile
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TechnicianDetailScreen(
  technicianState: UiState<TechnicianProfile?>,
  reviewsState: UiState<List<ServiceReview>>,
  categories: List<ServiceCategory>,
  userProfile: UserProfile?,
  userCoordinates: Coordinates?,
  currentUserId: String?,
  isBusy: Boolean,
  onSignInToBook: () -> Unit,
  onBack: () -> Unit,
  onBookSubmit: (
    technician: TechnicianProfile,
    clientName: String,
    clientPhone: String,
    serviceTitle: String,
    issueDescription: String,
    serviceArea: String,
    preferredTime: String,
  ) -> Unit,
  onToggleSaveTechnician: (String) -> Unit,
  onEditOwnProfile: () -> Unit,
) {
  val context = LocalContext.current
  var showBookingSheet by remember { mutableStateOf(false) }
  val loadedTech = (technicianState as? UiState.Success)?.data
  val isSaved =
    loadedTech != null && userProfile?.savedTechnicianIds?.contains(loadedTech.userId) == true

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Technician Profile") },
        navigationIcon = {
          IconButton(onClick = onBack, modifier = Modifier.testTag("tech_detail_back_button")) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (loadedTech != null && loadedTech.userId != currentUserId) {
            IconButton(
              onClick = {
                if (currentUserId == null) onSignInToBook()
                else onToggleSaveTechnician(loadedTech.userId)
              },
              modifier = Modifier.testTag("save_tech_bookmark_button"),
            ) {
              Icon(
                imageVector =
                  if (isSaved) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = if (isSaved) "Remove saved technician" else "Save technician",
                tint =
                  if (isSaved) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        },
      )
    }
  ) { innerPadding ->
    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
      when (technicianState) {
        is UiState.Loading -> {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
          }
        }
        is UiState.Error -> {
          ErrorCard(
            message = technicianState.message,
            modifier = Modifier.padding(16.dp),
          )
        }
        is UiState.Success -> {
          val tech = technicianState.data
          if (tech == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("Technician profile not found.")
            }
          } else {
            val category = ServiceCategories.byId(tech.categoryId, categories)
            val distKm =
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
            val isOwnProfile = currentUserId != null && tech.userId == currentUserId
            val avatarBitmap =
              remember(tech.profilePhotoData) {
                ImageEncodingHelper.decodeDataUrlToImageBitmap(tech.profilePhotoData)
              }

            LazyColumn(
              modifier = Modifier.fillMaxSize().widthIn(max = 720.dp).align(Alignment.TopCenter),
              contentPadding = PaddingValues(16.dp),
              verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
              // Hero Header Card
              item {
                Card(
                  shape = RoundedCornerShape(22.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                  Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      if (avatarBitmap != null) {
                        Image(
                          bitmap = avatarBitmap,
                          contentDescription = tech.fullName,
                          contentScale = ContentScale.Crop,
                          modifier = Modifier.size(68.dp).clip(RoundedCornerShape(18.dp)),
                        )
                      } else {
                        Box(
                          modifier =
                            Modifier.size(68.dp)
                              .clip(RoundedCornerShape(18.dp))
                              .background(category.tint.copy(alpha = 0.14f)),
                          contentAlignment = Alignment.Center,
                        ) {
                          Icon(
                            imageVector = category.icon,
                            contentDescription = category.name,
                            tint = category.tint,
                            modifier = Modifier.size(34.dp),
                          )
                        }
                      }
                      Spacer(modifier = Modifier.width(14.dp))
                      Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                          Text(
                            text = tech.fullName,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f, fill = false),
                          )
                          if (tech.verifiedBadge) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                              imageVector = Icons.Rounded.Verified,
                              contentDescription = "Verified Professional",
                              tint = MaterialTheme.colorScheme.primary,
                              modifier = Modifier.size(20.dp),
                            )
                          }
                        }
                        Text(
                          text =
                            buildString {
                              if (tech.professionalTitle.isNotBlank()) {
                                append(tech.professionalTitle)
                                append(" • ")
                              }
                              append(category.name)
                              if (tech.subcategory.isNotBlank()) append(" • ${tech.subcategory}")
                              append(" • ${tech.yearsExperience} yrs exp")
                            },
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        if (tech.additionalCategoryIds.isNotEmpty()) {
                          Spacer(modifier = Modifier.height(4.dp))
                          val extraNames =
                            tech.additionalCategoryIds.map {
                              ServiceCategories.byId(it, categories).name
                            }
                          Text(
                            text = "Also: ${extraNames.joinToString(", ")}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                          )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        FlowRow(
                          horizontalArrangement = Arrangement.spacedBy(6.dp),
                          verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                          Surface(
                            shape = RoundedCornerShape(50),
                            color =
                              if (tech.availableNow) EmeraldContainer
                              else MaterialTheme.colorScheme.surfaceVariant,
                          ) {
                            Row(
                              modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                              verticalAlignment = Alignment.CenterVertically,
                            ) {
                              Box(
                                modifier =
                                  Modifier.size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (tech.availableNow) EmeraldLive else Color.Gray)
                              )
                              Spacer(modifier = Modifier.width(6.dp))
                              Text(
                                text =
                                  if (tech.availableNow) "Available Now • Live GPS"
                                  else "Currently Offline",
                                style = MaterialTheme.typography.labelMedium,
                                color =
                                  if (tech.availableNow) OnEmeraldContainer
                                  else MaterialTheme.colorScheme.onSurfaceVariant,
                              )
                            }
                          }
                          if (tech.verifiedBadge) {
                            Surface(
                              shape = RoundedCornerShape(50),
                              color = MaterialTheme.colorScheme.primaryContainer,
                            ) {
                              Text(
                                text = "ID Verified Pro",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                              )
                            }
                          }
                          if (tech.emergencyAvailable) {
                            Surface(
                              shape = RoundedCornerShape(50),
                              color = Color(0xFFFFE4D1),
                            ) {
                              Text(
                                text = "Emergency Calls",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF431900),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                              )
                            }
                          }
                        }
                      }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Reputation & Stats Strip
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      StatBox(
                        label = "Rating",
                        value =
                          if (tech.reviewCount > 0) "${tech.averageRating} ★"
                          else "New",
                        modifier = Modifier.weight(1f),
                      )
                      StatBox(
                        label = "Reviews",
                        value = tech.reviewCount.toString(),
                        modifier = Modifier.weight(1f),
                      )
                      StatBox(
                        label = "Jobs Done",
                        value = tech.completedJobs.toString(),
                        modifier = Modifier.weight(1f),
                      )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      OutlinedButton(
                        onClick = {
                          val dialIntent =
                            Intent(Intent.ACTION_DIAL).apply {
                              data = Uri.parse("tel:${tech.contactPhone}")
                            }
                          context.startActivity(dialIntent)
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.weight(1f).height(50.dp).testTag("call_tech_button"),
                      ) {
                        Icon(
                          Icons.Rounded.Call,
                          contentDescription = "Call",
                          modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Call Pro")
                      }

                      if (isOwnProfile) {
                        Button(
                          onClick = onEditOwnProfile,
                          shape = RoundedCornerShape(14.dp),
                          modifier = Modifier.weight(1.4f).height(50.dp),
                        ) {
                          Text("Edit My Pro Profile")
                        }
                      } else {
                        Button(
                          onClick = { showBookingSheet = true },
                          shape = RoundedCornerShape(14.dp),
                          colors =
                            ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.primary
                            ),
                          modifier =
                            Modifier.weight(1.4f).height(50.dp).testTag("book_now_button"),
                        ) {
                          Text("Book Direct (Free)")
                        }
                      }
                    }
                  }
                }
              }

              // Visit & Service Charges Card (if provided)
              if (
                tech.visitCharge.isNotBlank() ||
                  tech.hourlyCharge.isNotBlank() ||
                  tech.dailyCharge.isNotBlank() ||
                  tech.serviceChargeNote.isNotBlank()
              ) {
                item {
                  Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth().padding(16.dp),
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Box(
                        modifier =
                          Modifier.size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                          imageVector = Icons.Rounded.Payments,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        )
                      }
                      Spacer(modifier = Modifier.width(12.dp))
                      Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                          text =
                            if (tech.visitCharge.isNotBlank()) "Visit Charge: ${tech.visitCharge}"
                            else "Service Charges",
                          style = MaterialTheme.typography.titleSmall,
                        )
                        val extraRates =
                          listOfNotNull(
                            tech.hourlyCharge.takeIf { it.isNotBlank() }?.let { "Hourly: $it" },
                            tech.dailyCharge.takeIf { it.isNotBlank() }?.let { "Daily: $it" },
                          )
                        if (extraRates.isNotEmpty()) {
                          Text(
                            text = extraRates.joinToString(" • "),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                          )
                        }
                        if (tech.serviceChargeNote.isNotBlank()) {
                          Text(
                            text = tech.serviceChargeNote,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        }
                      }
                    }
                  }
                }
              }

              // Portfolio Gallery
              if (tech.portfolioImages.isNotEmpty()) {
                item {
                  Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                  ) {
                    Column(
                      modifier = Modifier.padding(16.dp),
                      verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      Text(
                        text = "Previous Work Portfolio",
                        style = MaterialTheme.typography.titleMedium,
                      )
                      LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(tech.portfolioImages) { dataUrl ->
                          val bmp =
                            remember(dataUrl) {
                              ImageEncodingHelper.decodeDataUrlToImageBitmap(dataUrl)
                            }
                          if (bmp != null) {
                            Image(
                              bitmap = bmp,
                              contentDescription = "Portfolio work sample",
                              contentScale = ContentScale.Crop,
                              modifier =
                                Modifier.size(width = 168.dp, height = 120.dp)
                                  .clip(RoundedCornerShape(14.dp)),
                            )
                          }
                        }
                      }
                    }
                  }
                }
              }

              // Service & Location Details
              item {
                Card(
                  shape = RoundedCornerShape(20.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                ) {
                  Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                  ) {
                    Text(
                      text = "Expertise & Service Area",
                      style = MaterialTheme.typography.titleMedium,
                    )

                    val tags =
                      tech.expertise.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (tags.isNotEmpty()) {
                      FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                      ) {
                        tags.forEach { tag ->
                          SuggestionChip(onClick = {}, label = { Text(tag) })
                        }
                      }
                    }

                    if (tech.bio.isNotBlank()) {
                      Text(
                        text = tech.bio,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }

                    val areaDisplay =
                      buildString {
                        append(tech.workingArea)
                        if (tech.city.isNotBlank()) append(", ${tech.city}")
                        if (distKm >= 0.0) {
                          append(" (${LocationHelper.formatDistance(distKm)} • ${LocationHelper.estimateEtaMinutes(distKm)})")
                        } else {
                          append(" • ${tech.serviceRadiusKm} km coverage")
                        }
                        if (tech.preferredAreas.isNotEmpty()) {
                          append("\nPreferred areas: ${tech.preferredAreas.joinToString(", ")}")
                        }
                      }
                    DetailLine(
                      icon = Icons.Rounded.LocationOn,
                      label = "Working Area & Coverage",
                      value = areaDisplay,
                    )
                    if (tech.qualification.isNotBlank()) {
                      DetailLine(
                        icon = Icons.Rounded.School,
                        label = "Qualification",
                        value = tech.qualification,
                      )
                    }
                    DetailLine(
                      icon = Icons.Rounded.CalendarMonth,
                      label = "Working Days",
                      value =
                        if (tech.workingDays.isNotEmpty()) tech.workingDays.joinToString(", ")
                        else "Mon - Sat",
                    )
                    DetailLine(
                      icon = Icons.Rounded.AccessTime,
                      label = "Availability Hours",
                      value = tech.availabilityHours,
                    )
                    DetailLine(
                      icon = Icons.Rounded.DirectionsCar,
                      label = "On-Site Service",
                      value =
                        if (tech.travelToCustomer) "Travels to customer location (${tech.serviceRadiusKm} km)"
                        else "Workshop / fixed service center",
                    )
                    DetailLine(
                      icon = if (tech.phoneVerified) Icons.Rounded.PhoneAndroid else Icons.Rounded.Call,
                      label =
                        if (tech.phoneVerified) "Direct Phone (SMS Verified)"
                        else "Direct Phone",
                      value = tech.contactPhone,
                    )

                    Surface(
                      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                      shape = RoundedCornerShape(12.dp),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Icon(
                          Icons.Rounded.Verified,
                          contentDescription = null,
                          tint = MaterialTheme.colorScheme.primary,
                          modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                          text = "Free booking — no payment gate. Pay the technician directly after completion.",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                      }
                    }
                  }
                }
              }

              // Verified Community Reviews
              item {
                Text(
                  text = "Client Reviews (${tech.reviewCount})",
                  style = MaterialTheme.typography.titleMedium,
                )
              }

              when (reviewsState) {
                is UiState.Loading -> {
                  item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                }
                is UiState.Error -> {
                  item { ErrorCard(message = reviewsState.message) }
                }
                is UiState.Success -> {
                  val reviews = reviewsState.data
                  if (reviews.isEmpty()) {
                    item {
                      Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth(),
                      ) {
                        Text(
                          text = "No verified reviews yet. Complete a booking to leave the first review!",
                          style = MaterialTheme.typography.bodyMedium,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          modifier = Modifier.padding(16.dp),
                        )
                      }
                    }
                  } else {
                    items(reviews, key = { it.id }) { review ->
                      ReviewCard(review = review)
                    }
                  }
                }
              }
            }

            if (showBookingSheet) {
              BookingRequestBottomSheet(
                technician = tech,
                userProfile = userProfile,
                isSignedIn = currentUserId != null,
                isBusy = isBusy,
                onSignInToBook = onSignInToBook,
                onDismiss = { showBookingSheet = false },
                onConfirm = { cName, cPhone, title, desc, area, prefTime ->
                  showBookingSheet = false
                  onBookSubmit(tech, cName, cPhone, title, desc, area, prefTime)
                },
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
    modifier = modifier,
  ) {
    Column(
      modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Text(
        text = value,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
private fun DetailLine(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  value: String,
) {
  Row(verticalAlignment = Alignment.Top) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = MaterialTheme.colorScheme.primary,
      modifier = Modifier.size(18.dp).padding(top = 2.dp),
    )
    Spacer(modifier = Modifier.width(10.dp))
    Column {
      Text(
        text = label,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      Text(
        text = value,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ReviewCard(review: ServiceReview) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Rounded.CheckCircle,
            contentDescription = "Verified client",
            tint = EmeraldLive,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = review.clientName,
            style = MaterialTheme.typography.titleSmall,
          )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          repeat(review.rating.toInt().coerceIn(1, 5)) {
            Icon(
              Icons.Rounded.Star,
              contentDescription = null,
              tint = StarGold,
              modifier = Modifier.size(15.dp),
            )
          }
        }
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = review.comment,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
      )
      if (review.highlights.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          review.highlights.forEach { tag ->
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f),
            ) {
              Text(
                text = tag,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookingRequestBottomSheet(
  technician: TechnicianProfile,
  userProfile: UserProfile?,
  isSignedIn: Boolean,
  isBusy: Boolean,
  onSignInToBook: () -> Unit,
  onDismiss: () -> Unit,
  onConfirm: (
    clientName: String,
    clientPhone: String,
    serviceTitle: String,
    issueDescription: String,
    serviceArea: String,
    preferredTime: String,
  ) -> Unit,
) {
  val context = LocalContext.current
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  var clientName by remember { mutableStateOf(userProfile?.displayName ?: "") }
  var clientPhone by remember { mutableStateOf(userProfile?.phoneNumber ?: "") }
  var serviceTitle by remember { mutableStateOf("") }
  var issueDescription by remember { mutableStateOf("") }
  var serviceArea by remember { mutableStateOf(userProfile?.defaultArea ?: "") }
  var preferredTime by remember { mutableStateOf("Available Now (Urgent)") }
  var formError by remember { mutableStateOf<String?>(null) }

  ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Text(
        text = "Book ${technician.fullName}",
        style = MaterialTheme.typography.titleLarge,
      )
      Text(
        text =
          buildString {
            append("Free direct booking • Pay technician directly after completion.")
            if (technician.visitCharge.isNotBlank()) {
              append(" (Visit: ${technician.visitCharge})")
            }
          },
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
      )

      if (!isSignedIn) {
        Surface(
          color = EmeraldContainer.copy(alpha = 0.65f),
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text =
              "You can call ${technician.fullName} right now with zero registration, or sign in with Google once to submit & track an in-app booking request.",
            style = MaterialTheme.typography.bodySmall,
            color = OnEmeraldContainer,
            modifier = Modifier.padding(12.dp),
          )
        }
        OutlinedButton(
          onClick = {
            onDismiss()
            val dialIntent =
              Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${technician.contactPhone}")
              }
            context.startActivity(dialIntent)
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
          Icon(Icons.Rounded.Call, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text("Call ${technician.fullName} Directly")
        }
        Button(
          onClick = {
            onDismiss()
            onSignInToBook()
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("booking_sheet_google_sign_in"),
        ) {
          Text("Sign in with Google to Book In-App", style = MaterialTheme.typography.titleSmall)
        }
      } else {
        if (formError != null) {
          Text(
            text = formError!!,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
          )
        }

        OutlinedTextField(
          value = clientName,
          onValueChange = { clientName = it },
          label = { Text("Your Name") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("booking_client_name_input"),
        )
        OutlinedTextField(
          value = clientPhone,
          onValueChange = { clientPhone = it },
          label = { Text("Your Contact Phone") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("booking_client_phone_input"),
        )
        OutlinedTextField(
          value = serviceTitle,
          onValueChange = { serviceTitle = it },
          label = { Text("Issue Summary (e.g. Kitchen breaker tripping)") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("booking_title_input"),
        )
        OutlinedTextField(
          value = issueDescription,
          onValueChange = { issueDescription = it },
          label = { Text("Details for the technician") },
          minLines = 2,
          maxLines = 4,
          modifier = Modifier.fillMaxWidth().testTag("booking_desc_input"),
        )
        OutlinedTextField(
          value = serviceArea,
          onValueChange = { serviceArea = it },
          label = { Text("Service Address / Neighborhood") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("booking_area_input"),
        )
        OutlinedTextField(
          value = preferredTime,
          onValueChange = { preferredTime = it },
          label = { Text("Preferred Time") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("booking_time_input"),
        )

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = {
            when {
              clientName.trim().isEmpty() -> formError = "Please enter your name."
              clientPhone.trim().length < 5 -> formError = "Please enter a valid phone number."
              serviceTitle.trim().length < 2 -> formError = "Enter a short summary of the issue."
              issueDescription.trim().length < 5 -> formError = "Describe the problem (at least 5 characters)."
              serviceArea.trim().length < 2 -> formError = "Enter your service location."
              else ->
                onConfirm(
                  clientName,
                  clientPhone,
                  serviceTitle,
                  issueDescription,
                  serviceArea,
                  preferredTime,
                )
            }
          },
          enabled = !isBusy,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("confirm_booking_button"),
        ) {
          Text("Send Booking Request", style = MaterialTheme.typography.titleSmall)
        }
      }
    }
  }
}
