package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BookmarkRemove
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ImageEncodingHelper
import com.example.data.LocationHelper
import com.example.model.ServiceCategories
import com.example.model.ServiceCategory
import com.example.ui.TechnicianWithDistance
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.StarGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedProfessionalsSheet(
  isSignedIn: Boolean,
  savedTechnicians: List<TechnicianWithDistance>,
  categories: List<ServiceCategory>,
  onSignInWithGoogle: () -> Unit,
  onOpenTechnician: (String) -> Unit,
  onToggleSavedTechnician: (String) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "Saved Professionals", style = MaterialTheme.typography.titleLarge)
      if (!isSignedIn) {
        Text(
          text = "Create an account or log in on the Profile tab to bookmark favorite technicians across devices.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Button(
          onClick = {
            onDismiss()
            onSignInWithGoogle()
          },
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
          Text("Go to Sign In")
        }
      } else if (savedTechnicians.isEmpty()) {
        Text(
          text = "No saved professionals yet. Tap the bookmark icon on any technician's profile to save them here.",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          items(savedTechnicians, key = { it.technician.userId }) { item ->
            val tech = item.technician
            val cat = ServiceCategories.byId(tech.categoryId, categories)
            val avatar =
              remember(tech.profilePhotoData) {
                ImageEncodingHelper.decodeDataUrlToImageBitmap(tech.profilePhotoData)
              }
            Card(
              onClick = {
                onDismiss()
                onOpenTechnician(tech.userId)
              },
              shape = RoundedCornerShape(16.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier.fillMaxWidth(),
            ) {
              Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                if (avatar != null) {
                  Image(
                    bitmap = avatar,
                    contentDescription = tech.fullName,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(48.dp).clip(CircleShape),
                  )
                } else {
                  Box(
                    modifier =
                      Modifier.size(48.dp)
                        .clip(CircleShape)
                        .background(cat.tint.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center,
                  ) {
                    Icon(cat.icon, contentDescription = null, tint = cat.tint)
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(tech.fullName, style = MaterialTheme.typography.titleSmall)
                  Text(
                    text = "${cat.name} • ${if (item.distanceKm >= 0.0) LocationHelper.formatDistance(item.distanceKm) else tech.workingArea}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.Star, contentDescription = null, tint = StarGold, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                      text = if (tech.reviewCount > 0) "${tech.averageRating} (${tech.reviewCount})" else "New",
                      style = MaterialTheme.typography.labelSmall,
                    )
                    if (tech.availableNow) {
                      Spacer(modifier = Modifier.width(8.dp))
                      Text("• Available Now", style = MaterialTheme.typography.labelSmall, color = EmeraldLive)
                    }
                  }
                }
                IconButton(onClick = { onToggleSavedTechnician(tech.userId) }) {
                  Icon(
                    Icons.Rounded.BookmarkRemove,
                    contentDescription = "Remove from saved",
                    tint = MaterialTheme.colorScheme.primary,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsSheet(
  initialArea: String,
  onSaveArea: (String) -> Unit,
  onRequestLocation: () -> Unit,
  onDismiss: () -> Unit,
) {
  var areaInput by remember(initialArea) { mutableStateOf(initialArea) }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "Location & App Settings", style = MaterialTheme.typography.titleLarge)
      OutlinedTextField(
        value = areaInput,
        onValueChange = { areaInput = it },
        label = { Text("Preferred City / Service Area") },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().testTag("guest_area_input"),
      )
      Button(
        onClick = {
          onSaveArea(areaInput)
          onDismiss()
        },
        enabled = areaInput.trim().isNotEmpty(),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_guest_area_button"),
      ) {
        Text("Save Service Area")
      }
      OutlinedButton(
        onClick = {
          onRequestLocation()
          onDismiss()
        },
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth().height(48.dp),
      ) {
        Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text("Sync Current GPS Location")
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpAndSupportSheet(onDismiss: () -> Unit) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 28.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      Text(text = "Help & Support", style = MaterialTheme.typography.titleLarge)
      SupportFaqItem(
        title = "100% Free Connection — No Payment Gate",
        body = "SkillConnect never charges commissions or holds payments online. You agree charges directly with the technical person and pay them after completion of work.",
      )
      SupportFaqItem(
        title = "Dynamic Trust & Rating Accountability",
        body = "After a booking is completed, clients leave verified 1-5 star ratings that raise or lower a technician's profile rank across nearby searches.",
      )
      SupportFaqItem(
        title = "Provider Verification & Disputes",
        body = "Super Admin reviews CNIC and trade certificates before issuing the Verified badge. You can report any booking issue directly from your Booking Details screen.",
      )
    }
  }
}

@Composable
private fun SupportFaqItem(title: String, body: String) {
  Surface(
    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
    shape = RoundedCornerShape(14.dp),
    modifier = Modifier.fillMaxWidth(),
  ) {
    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Rounded.VerifiedUser,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = title, style = MaterialTheme.typography.titleSmall)
      }
      Text(
        text = body,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}
