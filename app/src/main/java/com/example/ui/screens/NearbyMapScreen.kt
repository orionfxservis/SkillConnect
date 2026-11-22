package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.Coordinates
import com.example.model.ServiceCategory
import com.example.model.UiState
import com.example.ui.TechnicianWithDistance
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldLive
import com.example.ui.theme.OnEmeraldContainer
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun NearbyMapScreen(
  techniciansState: UiState<List<TechnicianWithDistance>>,
  categories: List<ServiceCategory>,
  userCoordinates: Coordinates?,
  selectedCategoryId: String?,
  availableOnly: Boolean,
  onRequestLocation: () -> Unit,
  onSelectCategory: (String?) -> Unit,
  onToggleAvailableOnly: () -> Unit,
  onOpenTechnician: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var selectedTechId by remember { mutableStateOf<String?>(null) }

  Column(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Live Nearby Map",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Tap any live pin to inspect, call, or book directly",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Spacer(modifier = Modifier.width(8.dp))
        OutlinedButton(onClick = onRequestLocation, shape = RoundedCornerShape(12.dp)) {
          Icon(Icons.Rounded.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(6.dp))
          Text(if (userCoordinates != null) "GPS Live" else "Sync GPS", maxLines = 1, softWrap = false)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterChip(
            selected = availableOnly,
            onClick = onToggleAvailableOnly,
            label = { Text("Available Now") },
            colors =
              FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldContainer,
                selectedLabelColor = OnEmeraldContainer,
              ),
          )
        }
        items(categories, key = { it.id }) { cat ->
          FilterChip(
            selected = selectedCategoryId == cat.id,
            onClick = { onSelectCategory(cat.id) },
            label = { Text(cat.name) },
          )
        }
      }
    }

    when (techniciansState) {
      is UiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
      is UiState.Error -> ErrorCard(techniciansState.message, Modifier.padding(16.dp))
      is UiState.Success -> {
        val list = techniciansState.data
        val selectedEntry = list.firstOrNull { it.technician.userId == selectedTechId } ?: list.firstOrNull()

        Column(
          modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          NearbyDispatchRadar(
            technicians = list,
            userCoordinates = userCoordinates,
            selectedTechId = selectedEntry?.technician?.userId,
            onSelectTech = { selectedTechId = it },
            modifier = Modifier.weight(1f),
          )

          if (selectedEntry != null) {
            TechnicianDirectoryCard(
              entry = selectedEntry,
              categories = categories,
              highlighted = true,
              onCallClick = {
                runCatching {
                  val dialIntent =
                    Intent(Intent.ACTION_DIAL).apply {
                      data = Uri.parse("tel:${selectedEntry.technician.contactPhone}")
                    }
                  context.startActivity(dialIntent)
                }
              },
              onBookClick = { onOpenTechnician(selectedEntry.technician.userId) },
              modifier = Modifier.padding(bottom = 12.dp),
            )
          }
        }
      }
    }
  }
}

@Composable
private fun NearbyDispatchRadar(
  technicians: List<TechnicianWithDistance>,
  userCoordinates: Coordinates?,
  selectedTechId: String?,
  onSelectTech: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "radar")
  val pulse by
    infiniteTransition.animateFloat(
      initialValue = 0.25f,
      targetValue = 1f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(2200, easing = LinearEasing),
          repeatMode = RepeatMode.Restart,
        ),
      label = "radarPulse",
    )

  val centerLat =
    userCoordinates?.latitude
      ?: technicians.firstOrNull { it.technician.latitude != 0.0 }?.technician?.latitude
      ?: 37.7749
  val centerLng =
    userCoordinates?.longitude
      ?: technicians.firstOrNull { it.technician.longitude != 0.0 }?.technician?.longitude
      ?: -122.4194

  val primaryColor = MaterialTheme.colorScheme.primary
  val outlineColor = MaterialTheme.colorScheme.outlineVariant

  val plottedTechs =
    remember(technicians, centerLat, centerLng) {
      technicians.mapIndexed { idx, entry ->
        val tech = entry.technician
        val hasGps = tech.latitude != 0.0 || tech.longitude != 0.0
        val (dxNorm, dyNorm) =
          if (hasGps) {
            val dLat = (tech.latitude - centerLat) * 111.0
            val dLng = (tech.longitude - centerLng) * 111.0 * cos(Math.toRadians(centerLat))
            val maxRadiusKm = 15.0
            val nx = (dLng / maxRadiusKm).toFloat().coerceIn(-0.85f, 0.85f)
            val ny = (-dLat / maxRadiusKm).toFloat().coerceIn(-0.85f, 0.85f)
            nx to ny
          } else {
            val angle = idx * 1.35
            val r = 0.35f + (idx % 3) * 0.18f
            (cos(angle) * r).toFloat() to (sin(angle) * r).toFloat()
          }
        Triple(entry, dxNorm, dyNorm)
      }
    }

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = MaterialTheme.colorScheme.surface,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    modifier = modifier.fillMaxWidth(),
  ) {
    Box(
      modifier =
        Modifier.fillMaxSize()
          .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.22f))
    ) {
      var pinOffsets by remember { mutableStateOf<List<Pair<String, Offset>>>(emptyList()) }

      Canvas(
        modifier =
          Modifier.fillMaxSize().pointerInput(plottedTechs) {
            detectTapGestures { tapOffset ->
              val hit =
                pinOffsets.minByOrNull { (_, pos) ->
                  hypot(pos.x - tapOffset.x, pos.y - tapOffset.y)
                }
              if (hit != null && hypot(hit.second.x - tapOffset.x, hit.second.y - tapOffset.y) < 62f) {
                onSelectTech(hit.first)
              }
            }
          }
      ) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val maxR = size.minDimension * 0.44f

        listOf(0.35f, 0.68f, 1.0f).forEach { factor ->
          drawCircle(
            color = outlineColor,
            radius = maxR * factor,
            center = center,
            style =
              Stroke(
                width = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)),
              ),
          )
        }

        drawCircle(
          color = primaryColor.copy(alpha = (1f - pulse) * 0.26f),
          radius = maxR * pulse,
          center = center,
        )

        drawCircle(color = Color.White, radius = 15f, center = center)
        drawCircle(color = primaryColor, radius = 10f, center = center)

        val computed = mutableListOf<Pair<String, Offset>>()
        plottedTechs.forEach { (entry, dx, dy) ->
          val pos = Offset(center.x + dx * maxR, center.y + dy * maxR)
          computed.add(entry.technician.userId to pos)
          val isSelected = entry.technician.userId == selectedTechId
          val pinColor = if (entry.technician.availableNow) EmeraldLive else Color(0xFF64748B)
          if (isSelected) {
            drawCircle(color = pinColor.copy(alpha = 0.28f), radius = 28f, center = pos)
          }
          drawCircle(color = Color.White, radius = if (isSelected) 17f else 13f, center = pos)
          drawCircle(color = pinColor, radius = if (isSelected) 12f else 9f, center = pos)
        }
        pinOffsets = computed
      }

      Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp),
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldLive))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "${technicians.size} nearby",
            style = MaterialTheme.typography.labelMedium,
          )
        }
      }
    }
  }
}
