package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.NearMe
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import com.example.data.ImageEncodingHelper
import com.example.data.LocationHelper
import com.example.model.Coordinates
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.UiState
import com.example.ui.TechnicianWithDistance
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold
import com.example.ui.theme.greenThemeTextFieldColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCandidatesScreen(
  categoryId: String,
  allTechniciansState: UiState<List<TechnicianWithDistance>>,
  categories: List<ServiceCategory>,
  userCoordinates: Coordinates?,
  onRequestLocation: () -> Unit,
  onBack: () -> Unit,
  onOpenTechnician: (String) -> Unit,
  onOpenProHub: () -> Unit,
  onExploreServices: () -> Unit = onBack,
  onOpenMap: () -> Unit = {},
  onOpenSubscription: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val listState = rememberLazyListState()
  val category = remember(categoryId, categories) { ServiceCategories.byId(categoryId, categories) }
  var localQuery by remember(categoryId) { mutableStateOf("") }
  var selectedSub by remember(categoryId) { mutableStateOf<String?>(null) }

  val handlePromoSlideAction: (CategoryPromoAction) -> Unit = { action ->
    when (action) {
      CategoryPromoAction.EXPLORE_SERVICES -> onExploreServices()
      CategoryPromoAction.FIND_PROFESSIONAL -> {
        localQuery = ""
        selectedSub = null
        scope.launch { listState.animateScrollToItem(0) }
      }
      CategoryPromoAction.JOIN_SKILLCONNECT -> onOpenProHub()
      CategoryPromoAction.FIND_NEARBY_SERVICES -> onOpenMap()
      CategoryPromoAction.EXPLORE_PRO_PLAN -> onOpenSubscription()
    }
  }

  val categoryCandidates =
    remember(allTechniciansState, category.id, categories, localQuery, selectedSub) {
      when (allTechniciansState) {
        is UiState.Success -> {
          val q = localQuery.trim().lowercase()
          val filtered =
            allTechniciansState.data.filter { entry ->
              val tech = entry.technician
              val primaryMatch = ServiceCategories.byId(tech.categoryId, categories).id == category.id
              val extraMatch =
                tech.additionalCategoryIds.any {
                  ServiceCategories.byId(it, categories).id == category.id
                }
              val subMatch =
                selectedSub == null ||
                  tech.subcategory.equals(selectedSub, ignoreCase = true) ||
                  tech.expertise.contains(selectedSub!!, ignoreCase = true)
              val queryMatch =
                q.isEmpty() ||
                  tech.fullName.lowercase().contains(q) ||
                  tech.contactPhone.lowercase().contains(q) ||
                  tech.workingArea.lowercase().contains(q) ||
                  tech.city.lowercase().contains(q) ||
                  tech.expertise.lowercase().contains(q)
              (primaryMatch || extraMatch) && subMatch && queryMatch
            }
          UiState.Success(filtered)
        }
        is UiState.Loading -> UiState.Loading
        is UiState.Error -> UiState.Error(allTechniciansState.message)
      }
    }

  val totalCount = (categoryCandidates as? UiState.Success)?.data?.size ?: 0

  Scaffold(
    containerColor = LightBackground,
    topBar = {
      TopAppBar(
        colors =
          TopAppBarDefaults.topAppBarColors(
            containerColor = DeepTeal,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White,
          ),
        navigationIcon = {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("category_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
              contentDescription = "Back",
            )
          }
        },
        title = {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier.size(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color.White.copy(alpha = 0.16f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = category.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp),
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = category.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = "$totalCount candidate(s) • Near to Far",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.85f),
              )
            }
          }
        },
        actions = {
          IconButton(
            onClick = onRequestLocation,
            modifier = Modifier.testTag("category_sync_gps_button"),
          ) {
            Icon(
              imageVector = Icons.Rounded.MyLocation,
              contentDescription = "Update GPS Location",
            )
          }
        },
      )
    },
  ) { innerPadding ->
    Box(modifier = modifier.fillMaxSize().padding(innerPadding)) {
      Column(
        modifier =
          Modifier.fillMaxSize()
            .widthIn(max = 760.dp)
            .align(Alignment.TopCenter),
      ) {
        // Search + GPS Distance Sort Bar
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .background(Color.White)
              .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = localQuery,
            onValueChange = { localQuery = it },
            placeholder = { Text("Search ${category.name} by name, phone or area...") },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
              if (localQuery.isNotEmpty()) {
                IconButton(onClick = { localQuery = "" }) {
                  Icon(Icons.Rounded.Close, contentDescription = "Clear")
                }
              }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = greenThemeTextFieldColors(),
            modifier = Modifier.fillMaxWidth().testTag("category_search_input"),
          )

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Surface(
              shape = RoundedCornerShape(50),
              color = EmeraldContainer,
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Icon(
                  imageVector = Icons.Rounded.NearMe,
                  contentDescription = null,
                  tint = OnEmeraldContainer,
                  modifier = Modifier.size(14.dp),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                  text =
                    if (userCoordinates != null) "Sorted Near to Far (Live GPS)"
                    else "Near to Far (Enable GPS for exact km)",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = OnEmeraldContainer,
                )
              }
            }

            if (userCoordinates == null) {
              Text(
                text = "Detect GPS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = DeepTeal,
                modifier =
                  Modifier.clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onRequestLocation)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .testTag("category_enable_gps_link"),
              )
            }
          }

          if (category.subcategories.isNotEmpty()) {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              item {
                FilterChip(
                  selected = selectedSub == null,
                  onClick = { selectedSub = null },
                  label = { Text("All (${category.name})") },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = EmeraldContainer,
                      selectedLabelColor = OnEmeraldContainer,
                    ),
                )
              }
              items(category.subcategories) { sub ->
                FilterChip(
                  selected = selectedSub == sub,
                  onClick = { selectedSub = if (selectedSub == sub) null else sub },
                  label = { Text(sub) },
                  colors =
                    FilterChipDefaults.filterChipColors(
                      selectedContainerColor = EmeraldContainer,
                      selectedLabelColor = OnEmeraldContainer,
                    ),
                )
              }
            }
          }
        }

        // Candidates List
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
          when (categoryCandidates) {
            is UiState.Loading -> {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
              }
            }
            is UiState.Error -> {
              ErrorCard(
                message = categoryCandidates.message,
                modifier = Modifier.padding(16.dp),
              )
            }
            is UiState.Success -> {
              val candidates = categoryCandidates.data
              if (candidates.isEmpty()) {
                Box(
                  modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 12.dp),
                  contentAlignment = Alignment.Center,
                ) {
                  Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth(),
                  ) {
                    Column(
                      modifier = Modifier.fillMaxWidth().padding(22.dp),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                      Box(
                        modifier =
                          Modifier.size(56.dp)
                            .clip(CircleShape)
                            .background(category.tint.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center,
                      ) {
                        Icon(
                          imageVector = category.icon,
                          contentDescription = null,
                          tint = category.tint,
                          modifier = Modifier.size(28.dp),
                        )
                      }
                      Text(
                        text = "No ${category.name} candidates found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text =
                          if (localQuery.isNotBlank() || selectedSub != null)
                            "Try clearing your search or subcategory filter."
                          else
                            "Be the first ${category.name} professional in this category by enrolling in Pro Hub.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                      )
                      if (localQuery.isNotBlank() || selectedSub != null) {
                        OutlinedButton(
                          onClick = {
                            localQuery = ""
                            selectedSub = null
                          },
                          shape = RoundedCornerShape(12.dp),
                        ) {
                          Text("Show All ${category.name}")
                        }
                      } else {
                        Button(
                          onClick = onOpenProHub,
                          shape = RoundedCornerShape(12.dp),
                          colors = ButtonDefaults.buttonColors(containerColor = DeepTeal),
                        ) {
                          Text("Enrol as ${category.name}")
                        }
                      }
                    }
                  }
                }
              } else {
                LazyColumn(
                  state = listState,
                  modifier = Modifier.fillMaxSize().testTag("category_candidates_list"),
                  contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                  verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                  items(candidates, key = { it.technician.userId }) { entry ->
                    CategoryCandidateCard(
                      entry = entry,
                      category = category,
                      onCallClick = {
                        runCatching {
                          val dialIntent =
                            Intent(Intent.ACTION_DIAL).apply {
                              data = Uri.parse("tel:${entry.technician.contactPhone}")
                            }
                          context.startActivity(dialIntent)
                        }
                      },
                      onOpenDetails = { onOpenTechnician(entry.technician.userId) },
                    )
                  }
                }
              }
            }
          }
        }

        // Banner Carousel + Five Dots pinned at bottom matching the main page
        CategoryPromoBannerCarousel(
          onSlideAction = handlePromoSlideAction,
          modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
        )
      }
    }
  }
}

@Composable
private fun CategoryCandidateCard(
  entry: TechnicianWithDistance,
  category: ServiceCategory,
  onCallClick: () -> Unit,
  onOpenDetails: () -> Unit,
) {
  val tech = entry.technician
  val avatarBitmap =
    remember(tech.profilePhotoData) {
      ImageEncodingHelper.decodeDataUrlToImageBitmap(tech.profilePhotoData)
    }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(onClick = onOpenDetails)
        .testTag("category_candidate_card_${tech.userId}"),
  ) {
    Column(
      modifier = Modifier.padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Candidate Image
        if (avatarBitmap != null) {
          Image(
            bitmap = avatarBitmap,
            contentDescription = "${tech.fullName} photo",
            contentScale = ContentScale.Crop,
            modifier =
              Modifier.size(72.dp)
                .clip(CircleShape)
                .testTag("candidate_image_${tech.userId}"),
          )
        } else {
          Box(
            modifier =
              Modifier.size(72.dp)
                .clip(CircleShape)
                .background(category.tint.copy(alpha = 0.14f))
                .testTag("candidate_image_placeholder_${tech.userId}"),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.Person,
              contentDescription = "${tech.fullName} avatar",
              tint = category.tint,
              modifier = Modifier.size(34.dp),
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Candidate Name, Trade & Contact No
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = tech.fullName.ifBlank { "Service Provider" },
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false).testTag("candidate_name_${tech.userId}"),
            )
            if (tech.verifiedBadge) {
              Spacer(modifier = Modifier.width(5.dp))
              Icon(
                imageVector = Icons.Rounded.Verified,
                contentDescription = "Verified Professional",
                tint = EmeraldLive,
                modifier = Modifier.size(17.dp),
              )
            }
          }

          Text(
            text =
              buildString {
                append(tech.professionalTitle.ifBlank { category.name })
                if (tech.subcategory.isNotBlank()) append(" • ${tech.subcategory}")
                append(" • ${tech.yearsExperience} yrs exp")
              },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          // Explicit Contact No Row
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier =
              Modifier.clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onCallClick)
                .padding(vertical = 2.dp)
                .testTag("candidate_contact_${tech.userId}"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Phone,
              contentDescription = "Contact Number",
              tint = DeepTeal,
              modifier = Modifier.size(15.dp),
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = tech.contactPhone.ifBlank { "No contact number" },
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.SemiBold,
              color = DeepTeal,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          // Rating + Area Row
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = null,
              tint = StarGold,
              modifier = Modifier.size(14.dp),
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
              text = if (tech.reviewCount > 0) "${tech.averageRating} (${tech.reviewCount})" else "New",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.SemiBold,
            )
            val locationSummary =
              listOf(tech.workingArea, tech.city).filter { it.isNotBlank() }.joinToString(", ")
            if (locationSummary.isNotBlank()) {
              Text(
                text = " • $locationSummary",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Distance Badge (Near to Far indicator) & Live Status
        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = EmeraldContainer,
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.LocationOn,
                contentDescription = null,
                tint = EmeraldLive,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text =
                  if (entry.distanceKm >= 0.0) LocationHelper.formatDistance(entry.distanceKm).removeSuffix(" away")
                  else tech.workingArea.ifBlank { tech.city }.take(10).ifBlank { "Area" },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnEmeraldContainer,
                maxLines = 1,
              )
            }
          }

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier.size(8.dp)
                  .clip(CircleShape)
                  .background(if (tech.availableNow) EmeraldLive else Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (tech.availableNow) "Online" else "Offline",
              style = MaterialTheme.typography.labelSmall,
              color = if (tech.availableNow) OnEmeraldContainer else MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Bottom Call & Book Direct Row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        OutlinedButton(
          onClick = onCallClick,
          shape = RoundedCornerShape(12.dp),
          border = BorderStroke(1.2.dp, EmeraldLive),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
          modifier = Modifier.weight(1f).height(44.dp).testTag("category_call_${tech.userId}"),
        ) {
          Icon(Icons.Rounded.Call, contentDescription = null, tint = EmeraldLive, modifier = Modifier.size(17.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Call Now", fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }

        Button(
          onClick = onOpenDetails,
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = Color.White),
          modifier = Modifier.weight(1.2f).height(44.dp).testTag("category_book_${tech.userId}"),
        ) {
          Text("View & Book", fontWeight = FontWeight.Bold, maxLines = 1, softWrap = false)
        }
      }
    }
  }
}
