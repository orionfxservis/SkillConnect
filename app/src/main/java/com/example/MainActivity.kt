package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.EventAvailable
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.FixNearRepository
import com.example.data.LocationHelper
import com.example.model.UiState
import com.example.data.SubscriptionPreferencesStore
import com.example.ui.FixNearViewModel
import com.example.ui.screens.BookingDetailAndChatScreen
import com.example.ui.screens.BookingsScreen
import com.example.ui.screens.CategoryCandidatesScreen
import com.example.ui.screens.DiscoverScreen
import com.example.ui.screens.MySubscriptionScreen
import com.example.ui.screens.NearbyMapScreen
import com.example.ui.screens.ProHubScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.SuperAdminScreen
import com.example.ui.screens.TechnicianDetailScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.onGoogleSignInClicked
import com.example.ui.screens.signOutUser
import com.example.ui.theme.BrandMint
import com.example.ui.theme.FixNearTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch

object AppRoutes {
  const val DISCOVER = "discover"
  const val MAP = "map"
  const val BOOKINGS = "bookings"
  const val PRO_HUB = "pro_hub"
  const val PROFILE = "profile"
  const val SUBSCRIPTION = "my_subscription"
  const val SUPER_ADMIN = "super_admin"
  const val CATEGORY_CANDIDATES = "category_candidates/{categoryId}"
  const val TECH_DETAIL = "technician_detail/{techId}"
  const val BOOKING_DETAIL = "booking_detail/{bookingId}"

  fun categoryCandidates(categoryId: String) = "category_candidates/$categoryId"

  fun techDetail(techId: String) = "technician_detail/$techId"

  fun bookingDetail(bookingId: String) = "booking_detail/$bookingId"
}

private data class TopLevelTab(
  val route: String,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

private val BaseTopLevelTabs =
  listOf(
    TopLevelTab(
      route = AppRoutes.DISCOVER,
      label = "Home",
      selectedIcon = Icons.Rounded.Home,
      unselectedIcon = Icons.Outlined.Home,
      testTag = "nav_discover",
    ),
    TopLevelTab(
      route = AppRoutes.MAP,
      label = "Map",
      selectedIcon = Icons.Rounded.LocationOn,
      unselectedIcon = Icons.Outlined.LocationOn,
      testTag = "nav_map",
    ),
    TopLevelTab(
      route = AppRoutes.BOOKINGS,
      label = "Bookings",
      selectedIcon = Icons.Rounded.EventAvailable,
      unselectedIcon = Icons.Outlined.EventAvailable,
      testTag = "nav_bookings",
    ),
    TopLevelTab(
      route = AppRoutes.PROFILE,
      label = "Profile",
      selectedIcon = Icons.Rounded.Person,
      unselectedIcon = Icons.Outlined.Person,
      testTag = "nav_profile",
    ),
  )

private val ProHubTab =
  TopLevelTab(
    route = AppRoutes.PRO_HUB,
    label = "Pro Hub",
    selectedIcon = Icons.Rounded.Build,
    unselectedIcon = Icons.Outlined.Build,
    testTag = "nav_pro_hub",
  )

private val SuperAdminTab =
  TopLevelTab(
    route = AppRoutes.SUPER_ADMIN,
    label = "Admin",
    selectedIcon = Icons.Rounded.AdminPanelSettings,
    unselectedIcon = Icons.Outlined.AdminPanelSettings,
    testTag = "nav_super_admin",
  )

internal fun FirebaseAuth.authStateFlow(): Flow<FirebaseUser?> = callbackFlow {
  val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
  addAuthStateListener(listener)
  awaitClose { removeAuthStateListener(listener) }
}

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
    )
    setContent {
      FixNearTheme {
        AppNavigation()
      }
    }
  }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }
  val currentUser by
    auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
  var pendingRouteAfterSignIn by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    if (auth.currentUser == null) {
      attemptAutoSignIn(
        context = context,
        credentialManager = credentialManager,
        onAuthSuccess = {},
        onUnauthenticated = {},
        scope = scope,
      )
    }
  }

  val user = currentUser
  val sessionKey = user?.uid ?: "guest"

  key(sessionKey) {
    val viewModel: FixNearViewModel =
      viewModel(
        key = sessionKey,
        factory =
          viewModelFactory {
            initializer {
              val app =
                checkNotNull(this[APPLICATION_KEY]) {
                  "APPLICATION_KEY missing from CreationExtras"
                }
              val databaseId = app.getString(R.string.firestore_database_id)
              val db = FirebaseFirestore.getInstance(databaseId)
              FixNearViewModel(
                repository = FixNearRepository(db = db, auth = auth),
                currentUserId = user?.uid,
                firebaseUser = user,
                subscriptionStore = SubscriptionPreferencesStore(app),
              )
            }
          },
      )
    MainFixNearShell(
      viewModel = viewModel,
      firebaseUser = user,
      pendingRouteAfterSignIn = pendingRouteAfterSignIn,
      onConsumePendingRoute = { pendingRouteAfterSignIn = null },
      onRequestPendingRouteAfterSignIn = { route -> pendingRouteAfterSignIn = route },
    )
  }
}

@Composable
private fun MainFixNearShell(
  viewModel: FixNearViewModel,
  firebaseUser: FirebaseUser?,
  pendingRouteAfterSignIn: String?,
  onConsumePendingRoute: () -> Unit,
  onRequestPendingRouteAfterSignIn: (String) -> Unit,
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val navController = rememberNavController()
  val snackbarHostState = remember { SnackbarHostState() }
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val guestArea by viewModel.guestArea.collectAsStateWithLifecycle()
  val userProfileState by viewModel.userProfileState.collectAsStateWithLifecycle()
  val myTechnicianState by viewModel.myTechnicianState.collectAsStateWithLifecycle()
  val myVerificationDocState by viewModel.myVerificationDocState.collectAsStateWithLifecycle()
  val categories by viewModel.categoriesState.collectAsStateWithLifecycle()
  val techniciansState by viewModel.techniciansState.collectAsStateWithLifecycle()
  val allTechniciansNearToFarState by
    viewModel.allTechniciansSortedByDistanceState.collectAsStateWithLifecycle()
  val savedTechnicians by viewModel.savedTechniciansState.collectAsStateWithLifecycle()
  val bookingsState by viewModel.bookingsState.collectAsStateWithLifecycle()
  val subscriptionStatus by viewModel.subscriptionStatusState.collectAsStateWithLifecycle()
  val myComplaintsState by viewModel.myComplaintsState.collectAsStateWithLifecycle()
  val inspectedTechState by viewModel.inspectedTechnicianState.collectAsStateWithLifecycle()
  val inspectedReviewsState by viewModel.inspectedTechnicianReviewsState.collectAsStateWithLifecycle()
  val inspectedBookingState by viewModel.inspectedBookingState.collectAsStateWithLifecycle()
  val trackedTechState by viewModel.trackedBookingTechnicianState.collectAsStateWithLifecycle()
  val bookingMessagesState by viewModel.bookingMessagesState.collectAsStateWithLifecycle()

  val userCoordinates by viewModel.userCoordinates.collectAsStateWithLifecycle()
  val hasLocationPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
  val selectedCategoryId by viewModel.selectedCategoryId.collectAsStateWithLifecycle()
  val selectedSubcategory by viewModel.selectedSubcategory.collectAsStateWithLifecycle()
  val availableOnly by viewModel.availableOnly.collectAsStateWithLifecycle()
  val verifiedOnly by viewModel.verifiedOnly.collectAsStateWithLifecycle()
  val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
  val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
  val isBusy by viewModel.isBusy.collectAsStateWithLifecycle()
  val isLiveBroadcastActive by viewModel.isLiveBroadcastActive.collectAsStateWithLifecycle()
  val phoneOtpVerificationId by viewModel.phoneOtpVerificationId.collectAsStateWithLifecycle()
  val phoneOtpStatusMessage by viewModel.phoneOtpStatusMessage.collectAsStateWithLifecycle()
  val phoneVerifiedSession by viewModel.phoneVerifiedSession.collectAsStateWithLifecycle()

  // Admin streams (safe defaults when !viewModel.isAdmin)
  val adminUsersState by viewModel.adminUsersState.collectAsStateWithLifecycle()
  val adminTechniciansState by viewModel.adminTechniciansState.collectAsStateWithLifecycle()
  val adminCategoriesState by viewModel.adminCategoriesState.collectAsStateWithLifecycle()
  val adminBookingsState by viewModel.adminBookingsState.collectAsStateWithLifecycle()
  val adminComplaintsState by viewModel.adminComplaintsState.collectAsStateWithLifecycle()
  val adminInspectedVerificationDocState by
    viewModel.adminInspectedVerificationDocState.collectAsStateWithLifecycle()

  val bottomBarTabs =
    remember(viewModel.isAdmin) {
      if (viewModel.isAdmin) BaseTopLevelTabs + SuperAdminTab else BaseTopLevelTabs
    }
  val railTabs =
    remember(viewModel.isAdmin) {
      if (viewModel.isAdmin) BaseTopLevelTabs + ProHubTab + SuperAdminTab
      else BaseTopLevelTabs + ProHubTab
    }

  val permissionLauncher =
    rememberLauncherForActivityResult(
      contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
      val granted =
        results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
          results[Manifest.permission.ACCESS_COARSE_LOCATION] == true
      viewModel.setLocationPermissionGranted(granted)
      if (granted) {
        scope.launch {
          LocationHelper.fetchCurrentLocation(context)?.let { coords ->
            viewModel.updateCoordinates(coords)
          }
        }
      }
    }

  val requestLocationAndSync: () -> Unit = {
    if (LocationHelper.hasLocationPermission(context)) {
      viewModel.setLocationPermissionGranted(true)
      scope.launch {
        LocationHelper.fetchCurrentLocation(context)?.let { coords ->
          viewModel.updateCoordinates(coords)
        }
      }
    } else {
      permissionLauncher.launch(
        arrayOf(
          Manifest.permission.ACCESS_FINE_LOCATION,
          Manifest.permission.ACCESS_COARSE_LOCATION,
        )
      )
    }
  }

  LaunchedEffect(Unit) {
    val permitted = LocationHelper.hasLocationPermission(context)
    viewModel.setLocationPermissionGranted(permitted)
    if (permitted) {
      LocationHelper.fetchCurrentLocation(context)?.let { coords ->
        viewModel.updateCoordinates(coords)
      }
    }
  }

  LaunchedEffect(isLiveBroadcastActive, hasLocationPermission) {
    if (isLiveBroadcastActive && hasLocationPermission) {
      LocationHelper.observeLiveLocation(context, intervalMs = 10_000L).collect { coords ->
        viewModel.updateCoordinates(coords)
        val isAvail =
          (myTechnicianState as? UiState.Success)?.data?.availableNow ?: true
        viewModel.updateLiveTechnicianStatusAndLocation(
          availableNow = isAvail,
          latitude = coords.latitude,
          longitude = coords.longitude,
          showToast = false,
        )
      }
    }
  }

  LaunchedEffect(actionMessage) {
    val msg = actionMessage ?: return@LaunchedEffect
    snackbarHostState.showSnackbar(msg)
    viewModel.clearMessage()
  }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route
  val showTopLevelNav =
    currentRoute in
      setOf(
        AppRoutes.DISCOVER,
        AppRoutes.MAP,
        AppRoutes.BOOKINGS,
        AppRoutes.PRO_HUB,
        AppRoutes.PROFILE,
        AppRoutes.SUPER_ADMIN,
      )

  val userProfile = (userProfileState as? UiState.Success)?.data
  val currentUserName =
    userProfile?.displayName?.ifBlank { firebaseUser?.displayName ?: "SkillConnect User" }
      ?: firebaseUser?.displayName
      ?: "SkillConnect User"

  val navigateTopLevel: (String) -> Unit = { route ->
    navController.navigate(route) {
      popUpTo(navController.graph.findStartDestination().id) { saveState = true }
      launchSingleTop = true
      restoreState = true
    }
  }

  LaunchedEffect(firebaseUser?.uid, pendingRouteAfterSignIn) {
    if (firebaseUser != null && pendingRouteAfterSignIn != null) {
      val dest = pendingRouteAfterSignIn
      onConsumePendingRoute()
      navigateTopLevel(dest)
    }
  }

  val triggerGoogleSignIn: (targetRouteAfterSignIn: String?) -> Unit = { targetRoute ->
    if (targetRoute != null) {
      onRequestPendingRouteAfterSignIn(targetRoute)
    }
    onGoogleSignInClicked(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {},
      onAuthError = { msg ->
        onConsumePendingRoute()
        scope.launch { snackbarHostState.showSnackbar(msg) }
      },
      scope = scope,
      onAuthCancelled = { onConsumePendingRoute() },
    )
  }

  BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
    val useNavigationRail = maxWidth >= 600.dp

    Scaffold(
      containerColor = MaterialTheme.colorScheme.background,
      contentWindowInsets = WindowInsets.navigationBars,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      bottomBar = {
        if (showTopLevelNav && !useNavigationRail) {
          Box(
            modifier =
              Modifier.fillMaxWidth()
                .background(
                  Brush.linearGradient(
                    colors = listOf(Color(0xFF032D22), Color(0xFF064835), Color(0xFF0D7A44))
                  )
                )
          ) {
            Column {
              HorizontalDivider(
                thickness = 1.dp,
                color = Color(0xFF28C365).copy(alpha = 0.35f),
              )
              NavigationBar(
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
              ) {
                bottomBarTabs.forEach { tab ->
                  val selected = currentRoute == tab.route
                  NavigationBarItem(
                    selected = selected,
                    onClick = { navigateTopLevel(tab.route) },
                    icon = {
                      Icon(
                        imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                      )
                    },
                    label = {
                      Text(
                        text = tab.label,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                      )
                    },
                    colors =
                      NavigationBarItemDefaults.colors(
                        selectedIconColor = BrandMint,
                        selectedTextColor = BrandMint,
                        unselectedIconColor = Color.White.copy(alpha = 0.80f),
                        unselectedTextColor = Color.White.copy(alpha = 0.80f),
                        indicatorColor = Color.White.copy(alpha = 0.16f),
                      ),
                    modifier = Modifier.testTag(tab.testTag),
                  )
                }
              }
            }
          }
        }
      },
    ) { innerPadding ->
      Row(
        modifier =
          Modifier.fillMaxSize()
            .padding(innerPadding)
            .then(
              if (currentRoute != AppRoutes.DISCOVER) Modifier.statusBarsPadding()
              else Modifier
            )
      ) {
        if (showTopLevelNav && useNavigationRail) {
          NavigationRail {
            railTabs.forEach { tab ->
              val selected = currentRoute == tab.route
              NavigationRailItem(
                selected = selected,
                onClick = { navigateTopLevel(tab.route) },
                icon = {
                  Icon(
                    imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
                    contentDescription = tab.label,
                  )
                },
                label = { Text(tab.label) },
                modifier = Modifier.testTag(tab.testTag),
              )
            }
          }
        }

        NavHost(
          navController = navController,
          startDestination = AppRoutes.DISCOVER,
          modifier = Modifier.weight(1f).fillMaxSize(),
        ) {
          composable(AppRoutes.DISCOVER) {
            DiscoverScreen(
              techniciansState = techniciansState,
              myTechnicianState = myTechnicianState,
              bookingsState = bookingsState,
              userProfile = userProfile,
              guestArea = guestArea,
              categories = categories,
              userCoordinates = userCoordinates,
              hasLocationPermission = hasLocationPermission,
              selectedCategoryId = selectedCategoryId,
              selectedSubcategory = selectedSubcategory,
              availableOnly = availableOnly,
              verifiedOnly = verifiedOnly,
              searchQuery = searchQuery,
              onRequestLocation = requestLocationAndSync,
              onSaveArea = { area ->
                if (viewModel.isSignedIn) {
                  viewModel.saveUserProfile(
                    displayName = userProfile?.displayName ?: currentUserName,
                    phoneNumber = userProfile?.phoneNumber ?: "",
                    defaultArea = area,
                  )
                } else {
                  viewModel.updateGuestArea(area)
                }
              },
              onSelectCategory = viewModel::selectCategory,
              onSelectSubcategory = viewModel::selectSubcategory,
              onToggleAvailableOnly = viewModel::toggleAvailableOnly,
              onToggleVerifiedOnly = viewModel::toggleVerifiedOnly,
              onSearchQueryChange = viewModel::updateSearchQuery,
              onOpenTechnician = { techId ->
                viewModel.inspectTechnician(techId)
                navController.navigate(AppRoutes.techDetail(techId))
              },
              onOpenCategory = { categoryId ->
                navController.navigate(AppRoutes.categoryCandidates(categoryId))
              },
              onOpenMap = { navigateTopLevel(AppRoutes.MAP) },
              onOpenProHub = { navigateTopLevel(AppRoutes.PRO_HUB) },
              onOpenBooking = { bookingId ->
                viewModel.inspectBooking(bookingId)
                navController.navigate(AppRoutes.bookingDetail(bookingId))
              },
            )
          }

          composable(
            route = AppRoutes.CATEGORY_CANDIDATES,
            arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
          ) { entry ->
            val categoryId = entry.arguments?.getString("categoryId") ?: "electrician"
            CategoryCandidatesScreen(
              categoryId = categoryId,
              allTechniciansState = allTechniciansNearToFarState,
              categories = categories,
              userCoordinates = userCoordinates,
              onRequestLocation = requestLocationAndSync,
              onBack = { navController.popBackStack() },
              onOpenTechnician = { techId ->
                viewModel.inspectTechnician(techId)
                navController.navigate(AppRoutes.techDetail(techId))
              },
              onOpenProHub = { navigateTopLevel(AppRoutes.PRO_HUB) },
            )
          }

          composable(AppRoutes.MAP) {
            NearbyMapScreen(
              techniciansState = techniciansState,
              categories = categories,
              userCoordinates = userCoordinates,
              selectedCategoryId = selectedCategoryId,
              availableOnly = availableOnly,
              onRequestLocation = requestLocationAndSync,
              onSelectCategory = viewModel::selectCategory,
              onToggleAvailableOnly = viewModel::toggleAvailableOnly,
              onOpenTechnician = { techId ->
                viewModel.inspectTechnician(techId)
                navController.navigate(AppRoutes.techDetail(techId))
              },
            )
          }

          composable(AppRoutes.BOOKINGS) {
            BookingsScreen(
              bookingsState = bookingsState,
              currentUserId = viewModel.currentUserId,
              subscriptionStatus = subscriptionStatus,
              onSignInClick = { triggerGoogleSignIn(AppRoutes.BOOKINGS) },
              onOpenBooking = { bookingId ->
                viewModel.inspectBooking(bookingId)
                navController.navigate(AppRoutes.bookingDetail(bookingId))
              },
              onOpenSubscription = { navController.navigate(AppRoutes.SUBSCRIPTION) },
            )
          }

          composable(AppRoutes.PRO_HUB) {
            ProHubScreen(
              isSignedIn = viewModel.isSignedIn,
              myTechnicianState = myTechnicianState,
              myVerificationDocState = myVerificationDocState,
              categories = categories,
              userProfile = userProfile,
              userCoordinates = userCoordinates,
              isLiveBroadcastActive = isLiveBroadcastActive,
              isBusy = isBusy,
              phoneOtpVerificationId = phoneOtpVerificationId,
              phoneOtpStatusMessage = phoneOtpStatusMessage,
              phoneVerifiedSession = phoneVerifiedSession,
              onSignInToEnroll = { triggerGoogleSignIn(AppRoutes.PRO_HUB) },
              onRequestLocation = requestLocationAndSync,
              onToggleLiveGpsBroadcast = {
                val next = !isLiveBroadcastActive
                if (next && !LocationHelper.hasLocationPermission(context)) {
                  requestLocationAndSync()
                }
                viewModel.setLiveBroadcastActive(next)
              },
              onToggleAvailability = { avail, lat, lng ->
                viewModel.updateLiveTechnicianStatusAndLocation(avail, lat, lng)
              },
              onStartPhoneOtp = viewModel::startPhoneOtp,
              onConfirmPhoneOtp = viewModel::confirmPhoneOtp,
              onClearPhoneOtpState = viewModel::clearPhoneOtpState,
              onSaveEnrollment = { input -> viewModel.saveTechnicianEnrollment(input) },
            )
          }

          composable(AppRoutes.PROFILE) {
            ProfileScreen(
              isSignedIn = viewModel.isSignedIn,
              guestArea = guestArea,
              userProfileState = userProfileState,
              myTechnicianProfile = (myTechnicianState as? UiState.Success)?.data,
              savedTechnicians = savedTechnicians,
              categories = categories,
              myComplaintsState = myComplaintsState,
              isAdmin = viewModel.isAdmin,
              isBusy = isBusy,
              onUpdateGuestArea = viewModel::updateGuestArea,
              onRequestLocation = requestLocationAndSync,
              onSignInWithGoogle = { triggerGoogleSignIn(AppRoutes.PROFILE) },
              onEnrollTechnicianClick = {
                if (viewModel.isSignedIn) {
                  navigateTopLevel(AppRoutes.PRO_HUB)
                } else {
                  triggerGoogleSignIn(AppRoutes.PRO_HUB)
                }
              },
              onSaveProfile = { name, phone, area ->
                viewModel.saveUserProfile(name, phone, area)
              },
              onOpenBookings = { navigateTopLevel(AppRoutes.BOOKINGS) },
              onOpenSubscription = { navController.navigate(AppRoutes.SUBSCRIPTION) },
              subscriptionStatus = subscriptionStatus,
              onOpenTechnician = { techId ->
                viewModel.inspectTechnician(techId)
                navController.navigate(AppRoutes.techDetail(techId))
              },
              onToggleSavedTechnician = viewModel::toggleSavedTechnician,
              onOpenProHub = { navigateTopLevel(AppRoutes.PRO_HUB) },
              onOpenSuperAdmin = {
                if (viewModel.isAdmin) navigateTopLevel(AppRoutes.SUPER_ADMIN)
              },
              onSignOut = {
                viewModel.setLiveBroadcastActive(false)
                signOutUser(credentialManager = credentialManager, scope = scope)
              },
            )
          }

          composable(AppRoutes.SUBSCRIPTION) {
            MySubscriptionScreen(
              isSignedIn = viewModel.isSignedIn,
              status = subscriptionStatus,
              onBack = { navController.popBackStack() },
              onOpenBookings = { navigateTopLevel(AppRoutes.BOOKINGS) },
              onUpgradeToPro = viewModel::upgradeToProPlan,
              onRenewPro = viewModel::renewProPlan,
              onSwitchToFree = viewModel::switchToFreePlan,
              onSignInPrompt = {
                navController.popBackStack()
                navigateTopLevel(AppRoutes.PROFILE)
              },
            )
          }

          composable(AppRoutes.SUPER_ADMIN) {
            if (viewModel.isAdmin) {
              SuperAdminScreen(
                adminUsersState = adminUsersState,
                adminTechniciansState = adminTechniciansState,
                adminCategoriesState = adminCategoriesState,
                adminBookingsState = adminBookingsState,
                adminComplaintsState = adminComplaintsState,
                inspectedVerificationDocState = adminInspectedVerificationDocState,
                mergedCategories = categories,
                isBusy = isBusy,
                onInspectVerificationDoc = viewModel::inspectVerificationDocAdmin,
                onReviewTechnician = viewModel::reviewTechnicianAdmin,
                onSeedDefaultCategories = viewModel::seedDefaultCategoriesAdmin,
                onSaveCategory = viewModel::saveCategoryAdmin,
                onUpdateUser = viewModel::updateUserAdmin,
                onResolveComplaint = viewModel::resolveComplaintAdmin,
              )
            } else {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Super Admin access restricted.")
              }
            }
          }

          composable(
            route = AppRoutes.TECH_DETAIL,
            arguments = listOf(navArgument("techId") { type = NavType.StringType }),
          ) { entry ->
            val techId = entry.arguments?.getString("techId") ?: ""
            LaunchedEffect(techId) {
              if (techId.isNotBlank()) viewModel.inspectTechnician(techId)
            }
            TechnicianDetailScreen(
              technicianState = inspectedTechState,
              reviewsState = inspectedReviewsState,
              categories = categories,
              userProfile = userProfile,
              userCoordinates = userCoordinates,
              currentUserId = viewModel.currentUserId,
              isBusy = isBusy,
              onSignInToBook = { triggerGoogleSignIn(null) },
              onBack = { navController.popBackStack() },
              onBookSubmit = { tech, cName, cPhone, title, desc, area, prefTime ->
                viewModel.bookTechnician(
                  technician = tech,
                  clientName = cName,
                  clientPhone = cPhone,
                  serviceTitle = title,
                  issueDescription = desc,
                  serviceArea = area,
                  preferredTime = prefTime,
                ) { bookingId ->
                  viewModel.inspectBooking(bookingId)
                  navController.navigate(AppRoutes.bookingDetail(bookingId)) {
                    popUpTo(AppRoutes.DISCOVER)
                  }
                }
              },
              onToggleSaveTechnician = viewModel::toggleSavedTechnician,
              onEditOwnProfile = {
                navController.navigate(AppRoutes.PRO_HUB) {
                  popUpTo(AppRoutes.DISCOVER)
                }
              },
            )
          }

          composable(
            route = AppRoutes.BOOKING_DETAIL,
            arguments = listOf(navArgument("bookingId") { type = NavType.StringType }),
          ) { entry ->
            val bookingId = entry.arguments?.getString("bookingId") ?: ""
            LaunchedEffect(bookingId) {
              if (bookingId.isNotBlank()) viewModel.inspectBooking(bookingId)
            }
            BookingDetailAndChatScreen(
              bookingState = inspectedBookingState,
              trackedTechState = trackedTechState,
              messagesState = bookingMessagesState,
              myComplaintsState = myComplaintsState,
              currentUserId = viewModel.currentUserId ?: "",
              currentUserName = currentUserName,
              isLiveBroadcastActive = isLiveBroadcastActive,
              isBusy = isBusy,
              canConfirmBooking = subscriptionStatus.canConfirmAnotherBooking,
              onBack = { navController.popBackStack() },
              onUpdateStatus = viewModel::updateBookingStatus,
              onToggleLiveGpsBroadcast = {
                val next = !isLiveBroadcastActive
                if (next && !LocationHelper.hasLocationPermission(context)) {
                  requestLocationAndSync()
                }
                viewModel.setLiveBroadcastActive(next)
              },
              onSendMessage = viewModel::sendMessage,
              onSubmitReview = { booking, stars, comment, tags ->
                viewModel.submitReview(booking, stars, comment, tags)
              },
              onSubmitComplaint = { booking, reporterName, reason, description ->
                viewModel.submitComplaint(booking, reporterName, reason, description)
              },
              onOpenSubscription = { navController.navigate(AppRoutes.SUBSCRIPTION) },
            )
          }
        }
      }
    }
  }
}
