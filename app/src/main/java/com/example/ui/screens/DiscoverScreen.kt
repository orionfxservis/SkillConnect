package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GpsFixed
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Radar
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.ImageEncodingHelper
import com.example.data.LocationHelper
import com.example.model.BookingStatus
import com.example.model.Coordinates
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.TechnicianProfile
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.ui.TechnicianWithDistance
import com.example.ui.theme.BrandMint
import com.example.ui.theme.CategoryTileBg
import com.example.ui.theme.CategoryTileBorder
import com.example.ui.theme.DeepTeal
import com.example.ui.theme.DeepTealDark
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.LightBackground
import com.example.ui.theme.OnEmeraldContainer
import com.example.ui.theme.StarGold
import kotlinx.coroutines.launch

private val HeaderCurveShape =
  GenericShape { size, _ ->
    val curveDepth = size.height * 0.085f
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height - curveDepth)
    quadraticTo(size.width * 0.5f, size.height + curveDepth * 0.75f, 0f, size.height - curveDepth)
    close()
  }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(
  techniciansState: UiState<List<TechnicianWithDistance>>,
  myTechnicianState: UiState<TechnicianProfile?>,
  bookingsState: UiState<List<ServiceBooking>>,
  userProfile: UserProfile?,
  guestArea: String = "Karachi, Sindh",
  categories: List<ServiceCategory>,
  userCoordinates: Coordinates?,
  hasLocationPermission: Boolean,
  selectedCategoryId: String?,
  selectedSubcategory: String?,
  availableOnly: Boolean,
  verifiedOnly: Boolean,
  searchQuery: String,
  onRequestLocation: () -> Unit,
  onSaveArea: (String) -> Unit,
  onSelectCategory: (String?) -> Unit,
  onSelectSubcategory: (String?) -> Unit,
  onToggleAvailableOnly: () -> Unit,
  onToggleVerifiedOnly: () -> Unit,
  onSearchQueryChange: (String) -> Unit,
  onOpenTechnician: (String) -> Unit,
  onOpenCategory: (String) -> Unit = onSelectCategory,
  onOpenMap: () -> Unit,
  onOpenProHub: () -> Unit,
  onOpenBooking: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var showLocationDialog by remember { mutableStateOf(false) }
  var showNotificationsSheet by remember { mutableStateOf(false) }

  val isEnrolled = (myTechnicianState as? UiState.Success)?.data != null
  val activeBookings =
    (bookingsState as? UiState.Success)?.data?.filter {
      it.bookingStatus !in setOf(BookingStatus.COMPLETED, BookingStatus.CANCELLED)
    } ?: emptyList()

  val orderedCategories =
    remember(categories) {
      val preferredOrder =
        listOf(
          "electrician",
          "plumber",
          "ac_technician",
          "mechanic",
          "carpenter",
          "it_expert",
          "cctv_expert",
          "solar_technician",
          "mobile_repair",
          "painter",
          "welder",
        )
      val byId = categories.associateBy { it.id }
      val prioritized = preferredOrder.mapNotNull { byId[it] }
      val remaining = categories.filter { cat -> preferredOrder.none { it == cat.id } }
      prioritized + remaining
    }

  val categoryColumns = remember(orderedCategories) { orderedCategories.chunked(3) }
  val categoryRowState = rememberLazyListState()
  val pagerState = rememberPagerState(pageCount = { 3 })

  Box(modifier = modifier.fillMaxSize().background(LightBackground)) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 760.dp).align(Alignment.TopCenter),
      contentPadding = PaddingValues(bottom = 24.dp),
    ) {
      // Curved Emerald-Teal Top Header (bleeds edge-to-edge under status bar)
      item {
        CurvedBrandHeader(
          areaLabel =
            userProfile?.defaultArea?.takeIf { it.isNotBlank() }
              ?: guestArea.ifBlank {
                if (userCoordinates != null) "Current GPS Area" else "Karachi, Sindh"
              },
          hasGps = userCoordinates != null,
          notificationCount = activeBookings.size,
          searchQuery = searchQuery,
          onSearchQueryChange = onSearchQueryChange,
          onOpenLocationPicker = { showLocationDialog = true },
          onRequestLocation = onRequestLocation,
          onOpenNotifications = { showNotificationsSheet = true },
        )
      }

      // Hero Banner Carousel + Dot Indicators
      item {
        Column(modifier = Modifier.padding(top = 10.dp)) {
          HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth(),
          ) { page ->
            when (page) {
              0 ->
                HeroPromoSlide(
                  title = "Skilled Help.\nJust a Tap Away!",
                  subtitle = "Find verified professionals\nnear your location",
                  ctaLabel = "Explore Now",
                  drawableRes = R.drawable.img_hero_technician,
                  onCtaClick = {
                    onOpenCategory(orderedCategories.firstOrNull()?.id ?: "electrician")
                  },
                )
              1 ->
                HeroPromoSlide(
                  title = if (isEnrolled) "Manage Your\nPro Dispatch!" else "Are You a\nSkilled Pro?",
                  subtitle =
                    if (isEnrolled) "Share live GPS & accept\nnearby client requests"
                    else "Enroll free & grow your\nlocal service bookings",
                  ctaLabel = if (isEnrolled) "Open Pro Hub" else "Join Pro Hub",
                  drawableRes = R.drawable.img_hero_technician,
                  onCtaClick = onOpenProHub,
                )
              else ->
                HeroPromoSlide(
                  title = "Live GPS\nDispatch Radar",
                  subtitle = "Track available pros near you\nwith zero payment gates",
                  ctaLabel = "Open Map",
                  drawableRes = R.drawable.img_hero_dispatch,
                  onCtaClick = onOpenMap,
                )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Carousel Page Indicator Dots
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            repeat(3) { index ->
              val selected = pagerState.currentPage == index
              Box(
                modifier =
                  Modifier.padding(horizontal = 4.dp)
                    .size(width = if (selected) 10.dp else 8.dp, height = 8.dp)
                    .clip(CircleShape)
                    .background(
                      if (selected) DeepTeal else MaterialTheme.colorScheme.outline
                    )
                    .clickable {
                      scope.launch { pagerState.animateScrollToPage(index) }
                    }
              )
            }
          }
        }
      }

      // Popular Categories (Horizontal Scrolling 3-Row Carousel with Left & Right Arrows)
      item {
        Column(modifier = Modifier.padding(vertical = 14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Popular Categories",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground,
            )

            Row(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              val canScrollLeft = categoryRowState.canScrollBackward
              val canScrollRight = categoryRowState.canScrollForward

              IconButton(
                onClick = {
                  scope.launch {
                    val target = (categoryRowState.firstVisibleItemIndex - 2).coerceAtLeast(0)
                    categoryRowState.animateScrollToItem(target)
                  }
                },
                enabled = canScrollLeft,
                modifier = Modifier.size(42.dp).testTag("categories_scroll_left"),
              ) {
                Surface(
                  shape = CircleShape,
                  color = if (canScrollLeft) EmeraldContainer else Color(0xFFF1F5F9),
                  border =
                    BorderStroke(
                      1.dp,
                      if (canScrollLeft) CategoryTileBorder else Color(0xFFE2E8F0),
                    ),
                  modifier = Modifier.size(34.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                      contentDescription = "Scroll categories left",
                      tint = if (canScrollLeft) DeepTeal else Color(0xFF94A3B8),
                      modifier = Modifier.size(22.dp),
                    )
                  }
                }
              }

              IconButton(
                onClick = {
                  scope.launch {
                    val lastIndex = (categoryColumns.size - 1).coerceAtLeast(0)
                    val target =
                      (categoryRowState.firstVisibleItemIndex + 2).coerceAtMost(lastIndex)
                    categoryRowState.animateScrollToItem(target)
                  }
                },
                enabled = canScrollRight,
                modifier = Modifier.size(42.dp).testTag("categories_scroll_right"),
              ) {
                Surface(
                  shape = CircleShape,
                  color = if (canScrollRight) EmeraldContainer else Color(0xFFF1F5F9),
                  border =
                    BorderStroke(
                      1.dp,
                      if (canScrollRight) CategoryTileBorder else Color(0xFFE2E8F0),
                    ),
                  modifier = Modifier.size(34.dp),
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                      contentDescription = "Scroll categories right",
                      tint = if (canScrollRight) DeepTeal else Color(0xFF94A3B8),
                      modifier = Modifier.size(22.dp),
                    )
                  }
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val tileWidth =
              remember(maxWidth) { ((maxWidth - 32.dp - 30.dp) / 4f).coerceIn(74.dp, 108.dp) }

            LazyRow(
              state = categoryRowState,
              contentPadding = PaddingValues(horizontal = 16.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("popular_categories_carousel"),
            ) {
              items(
                items = categoryColumns,
                key = { col -> col.firstOrNull()?.id ?: "" },
              ) { colCategories ->
                Column(
                  modifier = Modifier.width(tileWidth),
                  verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                  colCategories.forEach { cat ->
                    CategoryTile(
                      category = cat,
                      selected = selectedCategoryId == cat.id,
                      onClick = { onOpenCategory(cat.id) },
                      modifier = Modifier.fillMaxWidth(),
                    )
                  }
                }
              }
            }
          }
        }
      }

      // Show matching professionals only when actively searching from the Home search bar
      if (searchQuery.isNotBlank()) {
        item {
          Row(
            modifier =
              Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "Matching Professionals",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onBackground,
            )
            TextButton(onClick = { onSearchQueryChange("") }) {
              Text("Clear", color = DeepTeal)
            }
          }
        }

        when (techniciansState) {
          is UiState.Loading -> {
            item {
              Box(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                contentAlignment = Alignment.Center,
              ) {
                CircularProgressIndicator()
              }
            }
          }
          is UiState.Error -> {
            item {
              ErrorCard(
                message = techniciansState.message,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
              )
            }
          }
          is UiState.Success -> {
            val list = techniciansState.data
            if (list.isEmpty()) {
              item {
                EmptyDirectoryCard(
                  hasFilters = true,
                  onResetFilters = {
                    onSelectCategory(null)
                    if (availableOnly) onToggleAvailableOnly()
                    if (verifiedOnly) onToggleVerifiedOnly()
                    onSearchQueryChange("")
                  },
                  onOpenProHub = onOpenProHub,
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
              }
            } else {
              items(list, key = { it.technician.userId }) { item ->
                TechnicianDirectoryCard(
                  entry = item,
                  categories = categories,
                  onCallClick = {
                    runCatching {
                      val dialIntent =
                        Intent(Intent.ACTION_DIAL).apply {
                          data = Uri.parse("tel:${item.technician.contactPhone}")
                        }
                      context.startActivity(dialIntent)
                    }
                  },
                  onBookClick = { onOpenTechnician(item.technician.userId) },
                  modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                )
              }
            }
          }
        }
      }
    }
  }


  // Area / City Quick Picker Dialog
  if (showLocationDialog) {
    val activeArea = userProfile?.defaultArea?.takeIf { it.isNotBlank() } ?: guestArea
    var areaDraft by remember(activeArea) {
      mutableStateOf(activeArea)
    }
    AlertDialog(
      onDismissRequest = { showLocationDialog = false },
      title = { Text("Your Service Area") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Set your neighborhood or city to discover nearby technicians.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          OutlinedTextField(
            value = areaDraft,
            onValueChange = { areaDraft = it },
            label = { Text("City / Area (e.g. Clifton, Karachi)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("location_area_input"),
          )
          OutlinedButton(
            onClick = {
              onRequestLocation()
              showLocationDialog = false
            },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Use Current Device GPS")
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (areaDraft.trim().isNotEmpty()) {
              onSaveArea(areaDraft.trim())
            }
            showLocationDialog = false
          }
        ) {
          Text("Save Area")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLocationDialog = false }) { Text("Cancel") }
      },
    )
  }

  // Active Booking Notifications Bottom Sheet
  if (showNotificationsSheet) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
      onDismissRequest = { showNotificationsSheet = false },
      sheetState = sheetState,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp).padding(bottom = 26.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Text(
          text = "Live Dispatch Updates",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        if (activeBookings.isEmpty()) {
          Text(
            text = "No active dispatch requests right now. Book any nearby technician to track live status here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        } else {
          activeBookings.forEach { booking ->
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
              modifier =
                Modifier.fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .clickable {
                    showNotificationsSheet = false
                    onOpenBooking(booking.id)
                  },
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(booking.serviceTitle, style = MaterialTheme.typography.titleSmall)
                  Text(
                    text = "${booking.technicianName} • ${booking.bookingStatus.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepTeal,
                  )
                }
                Icon(Icons.AutoMirrored.Rounded.ArrowForward, contentDescription = null, tint = DeepTeal)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CurvedBrandHeader(
  areaLabel: String,
  hasGps: Boolean,
  notificationCount: Int,
  searchQuery: String,
  onSearchQueryChange: (String) -> Unit,
  onOpenLocationPicker: () -> Unit,
  onRequestLocation: () -> Unit,
  onOpenNotifications: () -> Unit,
) {
  Box(
    modifier =
      Modifier.fillMaxWidth()
        .clip(HeaderCurveShape)
        .background(
          Brush.linearGradient(
            colors = listOf(Color(0xFF032D22), Color(0xFF064835), Color(0xFF0D7A44))
          )
        )
  ) {
    // Rich sweeping emerald wave on bottom-right matching SkillConnect brand banner
    Canvas(modifier = Modifier.matchParentSize()) {
      val backWave =
        Path().apply {
          moveTo(size.width * 0.25f, size.height)
          quadraticTo(
            size.width * 0.68f,
            size.height * 0.40f,
            size.width,
            size.height * 0.44f,
          )
          lineTo(size.width, size.height)
          close()
        }
      drawPath(path = backWave, color = Color(0xFF1AA653).copy(alpha = 0.45f))

      val frontWave =
        Path().apply {
          moveTo(0f, size.height * 0.88f)
          quadraticTo(
            size.width * 0.56f,
            size.height * 0.62f,
            size.width,
            size.height * 0.82f,
          )
          lineTo(size.width, size.height)
          lineTo(0f, size.height)
          close()
        }
      drawPath(path = frontWave, color = Color(0xFF28C365).copy(alpha = 0.36f))
    }

    Column(
      modifier =
        Modifier.fillMaxWidth()
          .statusBarsPadding()
          .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 26.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      // Brand Row + Notification Bell
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
          SkillConnectLogoMark(modifier = Modifier.size(46.dp))
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text =
                buildAnnotatedString {
                  withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                    append("Skill")
                  }
                  withStyle(SpanStyle(color = BrandMint, fontWeight = FontWeight.Bold)) {
                    append("Connect")
                  }
                },
              fontSize = 25.sp,
              lineHeight = 28.sp,
            )
            Text(
              text = "Find Skilled Professionals Near You",
              style = MaterialTheme.typography.bodySmall,
              color = Color.White.copy(alpha = 0.9f),
            )
          }
        }

        IconButton(
          onClick = onOpenNotifications,
          modifier = Modifier.size(48.dp).testTag("notifications_button"),
        ) {
          BadgedBox(
            badge = {
              if (notificationCount > 0) {
                Badge(containerColor = Color(0xFFEF4444), contentColor = Color.White) {
                  Text(notificationCount.toString())
                }
              }
            }
          ) {
            Icon(
              imageVector = Icons.Outlined.Notifications,
              contentDescription = "Notifications",
              tint = Color.White,
              modifier = Modifier.size(27.dp),
            )
          }
        }
      }

      // Location Selector Pill + Circular GPS Target Button
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(50),
          color = Color.White,
          shadowElevation = 2.dp,
          modifier =
            Modifier.clip(RoundedCornerShape(50))
              .clickable(onClick = onOpenLocationPicker)
              .testTag("location_area_pill"),
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Icon(
              imageVector = Icons.Rounded.LocationOn,
              contentDescription = null,
              tint = EmeraldLive,
              modifier = Modifier.size(18.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = areaLabel,
              style = MaterialTheme.typography.labelLarge,
              color = Color(0xFF0F172A),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.widthIn(max = 220.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
              imageVector = Icons.Rounded.KeyboardArrowDown,
              contentDescription = "Change area",
              tint = Color(0xFF334155),
              modifier = Modifier.size(18.dp),
            )
          }
        }

        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 2.dp,
          modifier =
            Modifier.size(44.dp)
              .clip(CircleShape)
              .clickable(onClick = onRequestLocation)
              .testTag("gps_status_pill"),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector =
                if (hasGps) Icons.Rounded.GpsFixed else Icons.Rounded.MyLocation,
              contentDescription = "Sync Live GPS",
              tint = DeepTealDark,
              modifier = Modifier.size(22.dp),
            )
          }
        }
      }

      // Crisp Pill Search Bar
      Surface(
        shape = RoundedCornerShape(50),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(48.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = "Search",
            tint = Color(0xFF64748B),
            modifier = Modifier.size(20.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (searchQuery.isEmpty()) {
              Text(
                text = "Search electrician, plumber, AC repair...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF64748B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            BasicTextField(
              value = searchQuery,
              onValueChange = onSearchQueryChange,
              singleLine = true,
              textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF0F172A)),
              cursorBrush = SolidColor(DeepTeal),
              modifier = Modifier.fillMaxWidth().testTag("search_technicians_input"),
            )
          }
          if (searchQuery.isNotEmpty()) {
            IconButton(
              onClick = { onSearchQueryChange("") },
              modifier = Modifier.size(32.dp),
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "Clear search",
                tint = Color(0xFF64748B),
                modifier = Modifier.size(18.dp),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SkillConnectLogoMark(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    val w = size.width
    val h = size.height
    val strokeW = w * 0.068f

    // House roof + chimney
    val roofPath =
      Path().apply {
        moveTo(w * 0.12f, h * 0.48f)
        lineTo(w * 0.50f, h * 0.16f)
        lineTo(w * 0.88f, h * 0.48f)
      }
    drawPath(
      path = roofPath,
      color = Color.White,
      style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    // Chimney
    drawLine(
      color = Color.White,
      start = Offset(w * 0.70f, h * 0.21f),
      end = Offset(w * 0.70f, h * 0.34f),
      strokeWidth = strokeW,
      cap = StrokeCap.Round,
    )
    // House walls
    val wallPath =
      Path().apply {
        moveTo(w * 0.21f, h * 0.43f)
        lineTo(w * 0.21f, h * 0.85f)
        lineTo(w * 0.79f, h * 0.85f)
        lineTo(w * 0.79f, h * 0.43f)
      }
    drawPath(
      path = wallPath,
      color = Color.White,
      style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round),
    )
    // Crossed amber wrench & screwdriver
    drawLine(
      color = Color(0xFFF59E0B),
      start = Offset(w * 0.33f, h * 0.71f),
      end = Offset(w * 0.67f, h * 0.39f),
      strokeWidth = strokeW * 1.15f,
      cap = StrokeCap.Round,
    )
    drawCircle(
      color = Color(0xFFF59E0B),
      radius = w * 0.08f,
      center = Offset(w * 0.67f, h * 0.39f),
      style = Stroke(width = strokeW * 0.8f),
    )
    drawLine(
      color = Color(0xFFFBBF24),
      start = Offset(w * 0.67f, h * 0.71f),
      end = Offset(w * 0.35f, h * 0.41f),
      strokeWidth = strokeW * 1.15f,
      cap = StrokeCap.Round,
    )
  }
}

@Composable
private fun HeroPromoSlide(
  title: String,
  subtitle: String,
  ctaLabel: String,
  drawableRes: Int,
  onCtaClick: () -> Unit,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
    modifier = Modifier.fillMaxWidth().height(176.dp),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(Color(0xFF0C343A), Color(0xFF144E54), Color(0xFF1A625B))
            )
          )
    ) {
      // Sweeping vibrant emerald ribbon behind the portrait edge
      Canvas(modifier = Modifier.matchParentSize()) {
        val ribbon =
          Path().apply {
            moveTo(size.width * 0.50f, size.height)
            quadraticTo(
              size.width * 0.56f,
              size.height * 0.32f,
              size.width * 0.72f,
              0f,
            )
            lineTo(size.width, 0f)
            lineTo(size.width, size.height)
            close()
          }
        drawPath(
          path = ribbon,
          brush =
            Brush.verticalGradient(
              colors = listOf(Color(0xFF2AC466), Color(0xFF20A554))
            ),
        )
      }

      // Bright un-dimmed Technician Portrait clipped cleanly on right curve
      val rightArcShape = remember {
        GenericShape { size, _ ->
          moveTo(size.width * 0.22f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width, size.height)
          lineTo(0f, size.height)
          quadraticTo(size.width * 0.04f, size.height * 0.36f, size.width * 0.22f, 0f)
          close()
        }
      }

      Image(
        painter = painterResource(id = drawableRes),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        alignment = Alignment.TopCenter,
        modifier =
          Modifier.fillMaxHeight()
            .width(172.dp)
            .align(Alignment.CenterEnd)
            .clip(rightArcShape),
      )

      Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(modifier = Modifier.fillMaxWidth(0.58f)) {
          Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            lineHeight = 24.sp,
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = subtitle,
            fontSize = 12.sp,
            color = Color.White.copy(alpha = 0.92f),
            lineHeight = 16.sp,
          )
        }

        Button(
          onClick = onCtaClick,
          shape = RoundedCornerShape(50),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = Color(0xFF27B75E),
              contentColor = Color.White,
            ),
          contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
          modifier = Modifier.height(36.dp).testTag("hero_cta_button"),
        ) {
          Text(
            text = ctaLabel,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Icon(
            imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
          )
        }
      }
    }
  }
}

@Composable
private fun CategoryTile(
  category: ServiceCategory,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    shape = RoundedCornerShape(14.dp),
    color = if (selected) EmeraldContainer else CategoryTileBg,
    border =
      if (selected) BorderStroke(1.5.dp, DeepTeal)
      else BorderStroke(1.dp, CategoryTileBorder),
    shadowElevation = 1.dp,
    modifier =
      modifier
        .height(92.dp)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .testTag("category_chip_${category.id}"),
  ) {
    Column(
      modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 8.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
    ) {
      Icon(
        imageVector = category.icon,
        contentDescription = category.name,
        tint = category.tint,
        modifier = Modifier.size(34.dp),
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = category.name,
        fontSize = 10.5.sp,
        lineHeight = 12.sp,
        letterSpacing = (-0.2).sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF1E293B),
        maxLines = 2,
        softWrap = true,
        overflow = TextOverflow.Ellipsis,
        textAlign = TextAlign.Center,
      )
    }
  }
}


@Composable
fun TechnicianDirectoryCard(
  entry: TechnicianWithDistance,
  categories: List<ServiceCategory> = ServiceCategories.defaults,
  highlighted: Boolean = false,
  onCallClick: () -> Unit = {},
  onBookClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val tech = entry.technician
  val category = ServiceCategories.byId(tech.categoryId, categories)
  val avatarBitmap =
    remember(tech.profilePhotoData) {
      ImageEncodingHelper.decodeDataUrlToImageBitmap(tech.profilePhotoData)
    }

  Card(
    shape = RoundedCornerShape(20.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border =
      BorderStroke(
        width = if (highlighted) 2.dp else 1.dp,
        color =
          if (highlighted) DeepTeal
          else MaterialTheme.colorScheme.outlineVariant,
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(20.dp))
        .clickable(onClick = onBookClick)
        .testTag("technician_card_${tech.userId}"),
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        // Circular Profile Avatar
        if (avatarBitmap != null) {
          Image(
            bitmap = avatarBitmap,
            contentDescription = tech.fullName,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(64.dp).clip(CircleShape),
          )
        } else {
          Box(
            modifier =
              Modifier.size(64.dp)
                .clip(CircleShape)
                .background(category.tint.copy(alpha = 0.14f)),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = category.icon,
              contentDescription = category.name,
              tint = category.tint,
              modifier = Modifier.size(30.dp),
            )
          }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Name, Trade + Exp, Star Rating
        Column(modifier = Modifier.weight(1f)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
              text = tech.fullName,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
              modifier = Modifier.weight(1f, fill = false),
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

          Spacer(modifier = Modifier.height(2.dp))

          Text(
            text = "${category.name} • ${tech.yearsExperience} Years Exp.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )

          Spacer(modifier = Modifier.height(4.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.Star,
              contentDescription = "Rating",
              tint = StarGold,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = if (tech.reviewCount > 0) "${tech.averageRating}" else "New",
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "(${tech.reviewCount} reviews)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Distance Pill + Available Status
        Column(horizontalAlignment = Alignment.End) {
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
                  else tech.workingArea.take(10),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = OnEmeraldContainer,
                maxLines = 1,
              )
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier =
                Modifier.size(8.dp)
                  .clip(CircleShape)
                  .background(if (tech.availableNow) EmeraldLive else Color(0xFF94A3B8))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
              text = if (tech.availableNow) "Available Now" else "Offline",
              style = MaterialTheme.typography.labelSmall,
              fontWeight = FontWeight.Medium,
              color = if (tech.availableNow) OnEmeraldContainer else MaterialTheme.colorScheme.onSurfaceVariant,
              maxLines = 1,
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Bottom Row: Visit Fee on Left + Crisp Fixed-Alignment "Call" & "Book Now" Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Row(
          modifier = Modifier.weight(1f),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Icon(
            imageVector = Icons.Rounded.Payments,
            contentDescription = null,
            tint = DeepTeal,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text =
              if (tech.visitCharge.isNotBlank()) "${tech.visitCharge} (Visit)"
              else "Free Quote",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
          )
        }

        OutlinedButton(
          onClick = onCallClick,
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.2.dp, EmeraldLive),
          colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepTeal),
          contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
          modifier = Modifier.height(42.dp).testTag("card_call_${tech.userId}"),
        ) {
          Icon(
            imageVector = Icons.Rounded.Call,
            contentDescription = null,
            tint = EmeraldLive,
            modifier = Modifier.size(16.dp),
          )
          Spacer(modifier = Modifier.width(5.dp))
          Text(
            text = "Call",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            softWrap = false,
          )
        }

        Button(
          onClick = onBookClick,
          shape = RoundedCornerShape(10.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = DeepTeal,
              contentColor = Color.White,
            ),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 0.dp),
          modifier = Modifier.height(42.dp).testTag("card_book_${tech.userId}"),
        ) {
          Text(
            text = "Book Now",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
          )
        }
      }
    }
  }
}


@Composable
private fun EmptyDirectoryCard(
  hasFilters: Boolean,
  onResetFilters: () -> Unit,
  onOpenProHub: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Box(
        modifier =
          Modifier.size(48.dp)
            .clip(CircleShape)
            .background(EmeraldContainer),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = Icons.Rounded.Radar,
          contentDescription = null,
          tint = DeepTeal,
          modifier = Modifier.size(26.dp),
        )
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text =
            if (hasFilters) "No matching professionals"
            else "No enrolled pros in this area yet",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text =
            if (hasFilters) "Reset filters to see all nearby technicians."
            else "Technicians can enroll free via Pro Hub to appear live here.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(10.dp))
        if (hasFilters) {
          OutlinedButton(
            onClick = onResetFilters,
            shape = RoundedCornerShape(10.dp),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp),
          ) {
            Text("Show All", maxLines = 1, softWrap = false)
          }
        } else {
          Button(
            onClick = onOpenProHub,
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(containerColor = DeepTeal, contentColor = Color.White),
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp),
          ) {
            Text("Enroll as Pro", maxLines = 1, softWrap = false)
          }
        }
      }
    }
  }
}

@Composable
fun ErrorCard(message: String, modifier: Modifier = Modifier) {
  Surface(
    color = MaterialTheme.colorScheme.errorContainer,
    shape = RoundedCornerShape(16.dp),
    modifier = modifier.fillMaxWidth(),
  ) {
    Text(
      text = message,
      color = MaterialTheme.colorScheme.onErrorContainer,
      style = MaterialTheme.typography.bodyMedium,
      modifier = Modifier.padding(16.dp),
    )
  }
}
