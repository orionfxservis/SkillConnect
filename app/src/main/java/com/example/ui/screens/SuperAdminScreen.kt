package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.LaptopMac
import androidx.compose.material.icons.rounded.ReportProblem
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.ImageEncodingHelper
import com.example.model.ComplaintStatus
import com.example.model.ProviderVerificationDoc
import com.example.model.ServiceBooking
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.model.ServiceCategoryEntity
import com.example.model.ServiceComplaint
import com.example.model.TechnicianProfile
import com.example.model.UiState
import com.example.model.UserProfile
import com.example.model.VerificationStatus
import com.example.ui.theme.AmberContainer
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.OnAmberContainer
import com.example.ui.theme.OnEmeraldContainer

private enum class AdminSection(val label: String, val icon: ImageVector) {
  VERIFICATIONS("Verifications", Icons.Rounded.VerifiedUser),
  CATEGORIES("Categories", Icons.Rounded.Category),
  USERS("Users", Icons.Rounded.Group),
  BOOKINGS("Bookings", Icons.Rounded.CalendarMonth),
  COMPLAINTS("Reports", Icons.Rounded.ReportProblem),
}

@Composable
fun SuperAdminScreen(
  adminUsersState: UiState<List<UserProfile>>,
  adminTechniciansState: UiState<List<TechnicianProfile>>,
  adminCategoriesState: UiState<List<ServiceCategoryEntity>>,
  adminBookingsState: UiState<List<ServiceBooking>>,
  adminComplaintsState: UiState<List<ServiceComplaint>>,
  inspectedVerificationDocState: UiState<ProviderVerificationDoc?>,
  mergedCategories: List<ServiceCategory>,
  isBusy: Boolean,
  onInspectVerificationDoc: (String?) -> Unit,
  onReviewTechnician: (
    techId: String,
    status: VerificationStatus,
    verifiedBadge: Boolean,
    remarks: String,
    listed: Boolean,
  ) -> Unit,
  onSeedDefaultCategories: () -> Unit,
  onSaveCategory: (
    categoryId: String,
    name: String,
    subtitle: String,
    iconKey: String,
    subcategories: List<String>,
    active: Boolean,
    sortOrder: Long,
  ) -> Unit,
  onUpdateUser: (userId: String, suspended: Boolean, role: String) -> Unit,
  onResolveComplaint: (complaintId: String, status: ComplaintStatus, resolution: String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var activeSection by remember { mutableStateOf(AdminSection.VERIFICATIONS) }

  val pendingCount =
    (adminTechniciansState as? UiState.Success)?.data?.count {
      it.verificationEnum == VerificationStatus.PENDING
    } ?: 0
  val openReportsCount =
    (adminComplaintsState as? UiState.Success)?.data?.count {
      it.statusEnum != ComplaintStatus.RESOLVED
    } ?: 0

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isWideConsole = maxWidth >= 700.dp

    if (isWideConsole) {
      // Laptop / Desktop Two-Pane Admin Console
      Row(modifier = Modifier.fillMaxSize()) {
        Column(
          modifier =
            Modifier.width(248.dp)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surface)
              .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
          ) {
            Icon(
              Icons.Rounded.AdminPanelSettings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text("Super Admin", style = MaterialTheme.typography.titleMedium)
              Text(
                "Desktop Console",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }

          AdminSection.entries.forEach { section ->
            val badgeCount =
              when (section) {
                AdminSection.VERIFICATIONS -> pendingCount
                AdminSection.COMPLAINTS -> openReportsCount
                else -> 0
              }
            NavigationDrawerItem(
              selected = activeSection == section,
              onClick = { activeSection = section },
              icon = { Icon(section.icon, contentDescription = null) },
              label = {
                Text(
                  if (badgeCount > 0) "${section.label} ($badgeCount)" else section.label
                )
              },
              modifier = Modifier.testTag("admin_nav_${section.name.lowercase()}"),
            )
          }
        }

        VerticalDivider()

        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
          AdminSectionWorkspace(
            activeSection = activeSection,
            adminUsersState = adminUsersState,
            adminTechniciansState = adminTechniciansState,
            adminCategoriesState = adminCategoriesState,
            adminBookingsState = adminBookingsState,
            adminComplaintsState = adminComplaintsState,
            inspectedVerificationDocState = inspectedVerificationDocState,
            mergedCategories = mergedCategories,
            isBusy = isBusy,
            onInspectVerificationDoc = onInspectVerificationDoc,
            onReviewTechnician = onReviewTechnician,
            onSeedDefaultCategories = onSeedDefaultCategories,
            onSaveCategory = onSaveCategory,
            onUpdateUser = onUpdateUser,
            onResolveComplaint = onResolveComplaint,
          )
        }
      }
    } else {
      // Compact Mobile Fallback with Horizontal Tabs + Laptop Tip
      Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              Icons.Rounded.AdminPanelSettings,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Super Admin Console", style = MaterialTheme.typography.headlineSmall)
          }
          Spacer(modifier = Modifier.height(6.dp))
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.padding(10.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                Icons.Rounded.LaptopMac,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Best operated on laptop/tablet wide screen for multi-pane management.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
              )
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            AdminSection.entries.forEach { section ->
              val badgeCount =
                when (section) {
                  AdminSection.VERIFICATIONS -> pendingCount
                  AdminSection.COMPLAINTS -> openReportsCount
                  else -> 0
                }
              FilterChip(
                selected = activeSection == section,
                onClick = { activeSection = section },
                leadingIcon = {
                  Icon(section.icon, contentDescription = null, modifier = Modifier.size(16.dp))
                },
                label = {
                  Text(if (badgeCount > 0) "${section.label} ($badgeCount)" else section.label)
                },
              )
            }
          }
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
          AdminSectionWorkspace(
            activeSection = activeSection,
            adminUsersState = adminUsersState,
            adminTechniciansState = adminTechniciansState,
            adminCategoriesState = adminCategoriesState,
            adminBookingsState = adminBookingsState,
            adminComplaintsState = adminComplaintsState,
            inspectedVerificationDocState = inspectedVerificationDocState,
            mergedCategories = mergedCategories,
            isBusy = isBusy,
            onInspectVerificationDoc = onInspectVerificationDoc,
            onReviewTechnician = onReviewTechnician,
            onSeedDefaultCategories = onSeedDefaultCategories,
            onSaveCategory = onSaveCategory,
            onUpdateUser = onUpdateUser,
            onResolveComplaint = onResolveComplaint,
          )
        }
      }
    }
  }
}

@Composable
private fun AdminSectionWorkspace(
  activeSection: AdminSection,
  adminUsersState: UiState<List<UserProfile>>,
  adminTechniciansState: UiState<List<TechnicianProfile>>,
  adminCategoriesState: UiState<List<ServiceCategoryEntity>>,
  adminBookingsState: UiState<List<ServiceBooking>>,
  adminComplaintsState: UiState<List<ServiceComplaint>>,
  inspectedVerificationDocState: UiState<ProviderVerificationDoc?>,
  mergedCategories: List<ServiceCategory>,
  isBusy: Boolean,
  onInspectVerificationDoc: (String?) -> Unit,
  onReviewTechnician: (String, VerificationStatus, Boolean, String, Boolean) -> Unit,
  onSeedDefaultCategories: () -> Unit,
  onSaveCategory: (String, String, String, String, List<String>, Boolean, Long) -> Unit,
  onUpdateUser: (String, Boolean, String) -> Unit,
  onResolveComplaint: (String, ComplaintStatus, String) -> Unit,
) {
  when (activeSection) {
    AdminSection.VERIFICATIONS ->
      AdminVerificationsPane(
        techniciansState = adminTechniciansState,
        inspectedVerificationDocState = inspectedVerificationDocState,
        categories = mergedCategories,
        isBusy = isBusy,
        onInspectVerificationDoc = onInspectVerificationDoc,
        onReviewTechnician = onReviewTechnician,
      )
    AdminSection.CATEGORIES ->
      AdminCategoriesPane(
        remoteCategoriesState = adminCategoriesState,
        mergedCategories = mergedCategories,
        isBusy = isBusy,
        onSeedDefaultCategories = onSeedDefaultCategories,
        onSaveCategory = onSaveCategory,
      )
    AdminSection.USERS ->
      AdminUsersPane(
        usersState = adminUsersState,
        isBusy = isBusy,
        onUpdateUser = onUpdateUser,
      )
    AdminSection.BOOKINGS ->
      AdminBookingsPane(bookingsState = adminBookingsState)
    AdminSection.COMPLAINTS ->
      AdminComplaintsPane(
        complaintsState = adminComplaintsState,
        isBusy = isBusy,
        onResolveComplaint = onResolveComplaint,
      )
  }
}

@Composable
private fun AdminVerificationsPane(
  techniciansState: UiState<List<TechnicianProfile>>,
  inspectedVerificationDocState: UiState<ProviderVerificationDoc?>,
  categories: List<ServiceCategory>,
  isBusy: Boolean,
  onInspectVerificationDoc: (String?) -> Unit,
  onReviewTechnician: (String, VerificationStatus, Boolean, String, Boolean) -> Unit,
) {
  var statusFilter by remember { mutableStateOf<VerificationStatus?>(null) }
  var reviewingTech by remember { mutableStateOf<TechnicianProfile?>(null) }

  when (techniciansState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    is UiState.Error -> ErrorCard(techniciansState.message, Modifier.padding(16.dp))
    is UiState.Success -> {
      val filtered =
        techniciansState.data.filter { statusFilter == null || it.verificationEnum == statusFilter }

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        item {
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            FilterChip(
              selected = statusFilter == null,
              onClick = { statusFilter = null },
              label = { Text("All (${techniciansState.data.size})") },
            )
            VerificationStatus.entries.forEach { st ->
              val count = techniciansState.data.count { it.verificationEnum == st }
              FilterChip(
                selected = statusFilter == st,
                onClick = { statusFilter = st },
                label = { Text("${st.label} ($count)") },
              )
            }
          }
        }

        items(filtered, key = { it.userId }) { tech ->
          val cat = ServiceCategories.byId(tech.categoryId, categories)
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(tech.fullName, style = MaterialTheme.typography.titleMedium)
                    if (tech.verifiedBadge) {
                      Spacer(Modifier.width(6.dp))
                      Icon(
                        Icons.Rounded.Verified,
                        contentDescription = "Verified",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                      )
                    }
                  }
                  Text(
                    text = "${cat.name} • ${tech.workingArea}, ${tech.city} • ${tech.contactPhone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                val (bg, fg) =
                  when (tech.verificationEnum) {
                    VerificationStatus.APPROVED -> EmeraldContainer to OnEmeraldContainer
                    VerificationStatus.PENDING -> AmberContainer to OnAmberContainer
                    VerificationStatus.REJECTED ->
                      MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer
                  }
                Surface(shape = RoundedCornerShape(50), color = bg) {
                  Text(
                    text = tech.verificationEnum.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = fg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }

              Text(
                text = "Skills: ${tech.expertise} • Exp: ${tech.yearsExperience} yrs • Charge: ${tech.visitCharge.ifBlank { "Not set" }}",
                style = MaterialTheme.typography.bodySmall,
              )
              if (tech.adminRemarks.isNotBlank()) {
                Text(
                  text = "Remarks: ${tech.adminRemarks}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = {
                    onInspectVerificationDoc(tech.userId)
                    reviewingTech = tech
                  },
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Icon(Icons.Rounded.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(Modifier.width(6.dp))
                  Text("Inspect & Decide")
                }
              }
            }
          }
        }
      }

      reviewingTech?.let { tech ->
        val doc = (inspectedVerificationDocState as? UiState.Success)?.data
        var remarks by remember(tech.userId) { mutableStateOf(tech.adminRemarks) }
        val cnicFrontBmp =
          remember(doc?.cnicPhotoData) {
            doc?.cnicPhotoData?.let { ImageEncodingHelper.decodeDataUrlToImageBitmap(it) }
          }
        val cnicBackBmp =
          remember(doc?.cnicBackPhotoData) {
            doc?.cnicBackPhotoData?.let { ImageEncodingHelper.decodeDataUrlToImageBitmap(it) }
          }
        val certBmp =
          remember(doc?.certificatePhotoData) {
            doc?.certificatePhotoData?.let { ImageEncodingHelper.decodeDataUrlToImageBitmap(it) }
          }
        val primaryCat = ServiceCategories.byId(tech.categoryId, categories)
        val extraCatNames =
          tech.additionalCategoryIds.map { ServiceCategories.byId(it, categories).name }

        AlertDialog(
          onDismissRequest = {
            reviewingTech = null
            onInspectVerificationDoc(null)
          },
          title = { Text("Review ${tech.fullName}") },
          text = {
            Column(
              modifier = Modifier.verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text =
                  buildString {
                    if (tech.professionalTitle.isNotBlank()) append("${tech.professionalTitle} • ")
                    append(primaryCat.name)
                    if (extraCatNames.isNotEmpty()) append(" (+ ${extraCatNames.joinToString(", ")})")
                  },
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
              )
              Text("Phone: ${tech.contactPhone} (SMS Verified: ${tech.phoneVerified})")
              if (tech.email.isNotBlank() || tech.gender.isNotBlank()) {
                Text(
                  text =
                    listOfNotNull(
                      tech.email.takeIf { it.isNotBlank() }?.let { "Email: $it" },
                      tech.gender.takeIf { it.isNotBlank() }?.let { "Gender: $it" },
                    ).joinToString(" • "),
                  style = MaterialTheme.typography.bodySmall,
                )
              }
              if (tech.qualification.isNotBlank()) {
                Text("Qualification: ${tech.qualification}", style = MaterialTheme.typography.bodySmall)
              }
              Text(
                text = "Location: ${tech.workingArea}, ${tech.city} (${tech.serviceRadiusKm} km • Travel: ${if (tech.travelToCustomer) "Yes" else "No"})",
                style = MaterialTheme.typography.bodySmall,
              )
              if (tech.preferredAreas.isNotEmpty()) {
                Text(
                  text = "Preferred Areas: ${tech.preferredAreas.joinToString(", ")}",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
              Text(
                text = "Rates: Visit ${tech.visitCharge.ifBlank { "-" }}${if (tech.hourlyCharge.isNotBlank()) " • Hourly ${tech.hourlyCharge}" else ""}${if (tech.dailyCharge.isNotBlank()) " • Daily ${tech.dailyCharge}" else ""}",
                style = MaterialTheme.typography.bodySmall,
              )
              Text(
                text = "Schedule: ${tech.workingDays.joinToString(", ")} • ${tech.availabilityHours}",
                style = MaterialTheme.typography.bodySmall,
              )
              Text(
                text = "CNIC Number: ${doc?.cnicNumber?.ifBlank { "Not provided" } ?: "Not provided"}",
                style = MaterialTheme.typography.titleSmall,
              )
              if (cnicFrontBmp != null) {
                Text("CNIC Front", style = MaterialTheme.typography.labelSmall)
                Image(
                  bitmap = cnicFrontBmp,
                  contentDescription = "Provider CNIC Front",
                  contentScale = ContentScale.Crop,
                  modifier =
                    Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
                )
              }
              if (cnicBackBmp != null) {
                Text("CNIC Back", style = MaterialTheme.typography.labelSmall)
                Image(
                  bitmap = cnicBackBmp,
                  contentDescription = "Provider CNIC Back",
                  contentScale = ContentScale.Crop,
                  modifier =
                    Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
                )
              }
              if (certBmp != null) {
                Text("Trade Certificate / License", style = MaterialTheme.typography.labelSmall)
                Image(
                  bitmap = certBmp,
                  contentDescription = "Provider Certificate",
                  contentScale = ContentScale.Crop,
                  modifier =
                    Modifier.fillMaxWidth().height(130.dp).clip(RoundedCornerShape(12.dp)),
                )
              }
              OutlinedTextField(
                value = remarks,
                onValueChange = { remarks = it },
                label = { Text("Admin Remarks") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
              )
            }
          },
          confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              Button(
                onClick = {
                  onReviewTechnician(tech.userId, VerificationStatus.APPROVED, true, remarks, true)
                  reviewingTech = null
                  onInspectVerificationDoc(null)
                },
                enabled = !isBusy,
              ) {
                Text("Approve & Verify")
              }
              OutlinedButton(
                onClick = {
                  onReviewTechnician(tech.userId, VerificationStatus.REJECTED, false, remarks, false)
                  reviewingTech = null
                  onInspectVerificationDoc(null)
                },
                enabled = !isBusy,
                colors =
                  ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
              ) {
                Text("Reject")
              }
            }
          },
          dismissButton = {
            TextButton(
              onClick = {
                reviewingTech = null
                onInspectVerificationDoc(null)
              }
            ) {
              Text("Close")
            }
          },
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AdminCategoriesPane(
  remoteCategoriesState: UiState<List<ServiceCategoryEntity>>,
  mergedCategories: List<ServiceCategory>,
  isBusy: Boolean,
  onSeedDefaultCategories: () -> Unit,
  onSaveCategory: (String, String, String, String, List<String>, Boolean, Long) -> Unit,
) {
  var editingCategory by remember { mutableStateOf<ServiceCategory?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        Button(
          onClick = onSeedDefaultCategories,
          enabled = !isBusy,
          shape = RoundedCornerShape(12.dp),
        ) {
          Text("Sync 21 Default Categories")
        }
        OutlinedButton(
          onClick = { showAddDialog = true },
          shape = RoundedCornerShape(12.dp),
        ) {
          Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text("Add Category")
        }
      }
    }

    if (remoteCategoriesState is UiState.Error) {
      item { ErrorCard(remoteCategoriesState.message) }
    }

    items(mergedCategories, key = { it.id }) { cat ->
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
              Icon(cat.icon, contentDescription = null, tint = cat.tint)
              Spacer(Modifier.width(10.dp))
              Column {
                Text(cat.name, style = MaterialTheme.typography.titleMedium)
                Text(
                  text = cat.subtitle,
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
              }
            }
            OutlinedButton(onClick = { editingCategory = cat }, shape = RoundedCornerShape(10.dp)) {
              Text("Edit")
            }
          }
          if (cat.subcategories.isNotEmpty()) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              cat.subcategories.forEach { sub ->
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant,
                ) {
                  Text(
                    text = sub,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  if (showAddDialog || editingCategory != null) {
    val existing = editingCategory
    var catId by remember(existing) { mutableStateOf(existing?.id ?: "") }
    var name by remember(existing) { mutableStateOf(existing?.name ?: "") }
    var subtitle by remember(existing) { mutableStateOf(existing?.subtitle ?: "") }
    var subText by remember(existing) {
      mutableStateOf(existing?.subcategories?.joinToString(", ") ?: "")
    }
    var active by remember(existing) { mutableStateOf(existing?.active ?: true) }

    AlertDialog(
      onDismissRequest = {
        showAddDialog = false
        editingCategory = null
      },
      title = { Text(if (existing == null) "Add New Category" else "Edit ${existing.name}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          if (existing == null) {
            OutlinedTextField(
              value = catId,
              onValueChange = { catId = it },
              label = { Text("Category Slug (e.g. home_security)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth(),
            )
          }
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Category Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = subtitle,
            onValueChange = { subtitle = it },
            label = { Text("Description / Services") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = subText,
            onValueChange = { subText = it },
            label = { Text("Subcategories (comma separated)") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Active in Directory")
            Switch(checked = active, onCheckedChange = { active = it })
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val resolvedId =
              if (existing != null) existing.id
              else catId.ifBlank { name.lowercase().replace(" ", "_") }
            val subs = subText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            onSaveCategory(
              resolvedId,
              name,
              subtitle,
              resolvedId,
              subs,
              active,
              (existing?.sortOrder ?: (mergedCategories.size + 1)).toLong(),
            )
            showAddDialog = false
            editingCategory = null
          },
          enabled = !isBusy && name.trim().length >= 2,
        ) {
          Text("Save")
        }
      },
      dismissButton = {
        TextButton(
          onClick = {
            showAddDialog = false
            editingCategory = null
          }
        ) {
          Text("Cancel")
        }
      },
    )
  }
}

@Composable
private fun AdminUsersPane(
  usersState: UiState<List<UserProfile>>,
  isBusy: Boolean,
  onUpdateUser: (String, Boolean, String) -> Unit,
) {
  when (usersState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    is UiState.Error -> ErrorCard(usersState.message, Modifier.padding(16.dp))
    is UiState.Success -> {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(usersState.data, key = { it.userId }) { user ->
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(user.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                  text = "${user.email} • Role: ${if (user.enrolledTechnician) "PROVIDER" else user.role}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (user.defaultArea.isNotBlank() || user.phoneNumber.isNotBlank()) {
                  Text(
                    text = "${user.phoneNumber} • ${user.defaultArea}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
              OutlinedButton(
                onClick = { onUpdateUser(user.userId, !user.suspended, user.role) },
                enabled = !isBusy,
                shape = RoundedCornerShape(10.dp),
                colors =
                  ButtonDefaults.outlinedButtonColors(
                    contentColor =
                      if (user.suspended) MaterialTheme.colorScheme.primary
                      else MaterialTheme.colorScheme.error
                  ),
              ) {
                Text(if (user.suspended) "Unsuspend" else "Suspend")
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminBookingsPane(bookingsState: UiState<List<ServiceBooking>>) {
  when (bookingsState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    is UiState.Error -> ErrorCard(bookingsState.message, Modifier.padding(16.dp))
    is UiState.Success -> {
      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        items(bookingsState.data, key = { it.id }) { booking ->
          val (bg, fg) = statusColors(booking.bookingStatus)
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(booking.serviceTitle, style = MaterialTheme.typography.titleMedium)
                Surface(shape = RoundedCornerShape(50), color = bg) {
                  Text(
                    text = booking.bookingStatus.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = fg,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                  )
                }
              }
              Text(
                text = "Client: ${booking.clientName} → Pro: ${booking.technicianName}",
                style = MaterialTheme.typography.bodySmall,
              )
              Text(
                text = "${booking.serviceArea} • ${booking.preferredTime}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun AdminComplaintsPane(
  complaintsState: UiState<List<ServiceComplaint>>,
  isBusy: Boolean,
  onResolveComplaint: (String, ComplaintStatus, String) -> Unit,
) {
  var activeComplaint by remember { mutableStateOf<ServiceComplaint?>(null) }

  when (complaintsState) {
    is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
    is UiState.Error -> ErrorCard(complaintsState.message, Modifier.padding(16.dp))
    is UiState.Success -> {
      if (complaintsState.data.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
              Icons.Rounded.CheckCircle,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(40.dp),
            )
            Spacer(Modifier.height(8.dp))
            Text("No complaints or reports filed", style = MaterialTheme.typography.titleMedium)
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          items(complaintsState.data, key = { it.id }) { complaint ->
            Card(
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Text(
                    text = "${complaint.reason} (${complaint.reportedTechName})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  Surface(
                    shape = RoundedCornerShape(50),
                    color =
                      if (complaint.statusEnum == ComplaintStatus.RESOLVED) EmeraldContainer
                      else AmberContainer,
                  ) {
                    Text(
                      text = complaint.statusEnum.label,
                      style = MaterialTheme.typography.labelSmall,
                      color =
                        if (complaint.statusEnum == ComplaintStatus.RESOLVED) OnEmeraldContainer
                        else OnAmberContainer,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }
                }
                Text(
                  text = "Reporter: ${complaint.reporterName}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(complaint.description, style = MaterialTheme.typography.bodySmall)
                if (complaint.adminResolution.isNotBlank()) {
                  Text(
                    text = "Resolution: ${complaint.adminResolution}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                  )
                }
                OutlinedButton(
                  onClick = { activeComplaint = complaint },
                  shape = RoundedCornerShape(10.dp),
                ) {
                  Text("Update Status / Resolve")
                }
              }
            }
          }
        }

        activeComplaint?.let { comp ->
          var resolutionNote by remember(comp.id) { mutableStateOf(comp.adminResolution) }
          AlertDialog(
            onDismissRequest = { activeComplaint = null },
            title = { Text("Moderate Report") },
            text = {
              Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("${comp.reason} • Reported: ${comp.reportedTechName}")
                OutlinedTextField(
                  value = resolutionNote,
                  onValueChange = { resolutionNote = it },
                  label = { Text("Resolution Note") },
                  minLines = 2,
                  modifier = Modifier.fillMaxWidth(),
                )
              }
            },
            confirmButton = {
              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                  onClick = {
                    onResolveComplaint(comp.id, ComplaintStatus.INVESTIGATING, resolutionNote)
                    activeComplaint = null
                  },
                  enabled = !isBusy,
                ) {
                  Text("Investigating")
                }
                Button(
                  onClick = {
                    onResolveComplaint(comp.id, ComplaintStatus.RESOLVED, resolutionNote)
                    activeComplaint = null
                  },
                  enabled = !isBusy,
                ) {
                  Text("Resolve")
                }
              }
            },
            dismissButton = {
              TextButton(onClick = { activeComplaint = null }) { Text("Cancel") }
            },
          )
        }
      }
    }
  }
}
