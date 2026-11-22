package com.example.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.os.Looper
import androidx.core.content.ContextCompat
import com.example.model.Coordinates
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine

object LocationHelper {

  fun hasLocationPermission(context: Context): Boolean {
    val fine =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    val coarse =
      ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    return fine || coarse
  }

  @SuppressLint("MissingPermission")
  suspend fun fetchCurrentLocation(context: Context): Coordinates? {
    if (!hasLocationPermission(context)) return null
    val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
    return suspendCancellableCoroutine { cont ->
      val cts = CancellationTokenSource()
      cont.invokeOnCancellation { cts.cancel() }
      client
        .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
        .addOnSuccessListener { loc ->
          if (loc != null) {
            cont.resume(
              Coordinates(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracy,
                isDeviceGps = true,
              )
            )
          } else {
            client.lastLocation
              .addOnSuccessListener { last ->
                if (last != null) {
                  cont.resume(
                    Coordinates(
                      latitude = last.latitude,
                      longitude = last.longitude,
                      accuracyMeters = last.accuracy,
                      isDeviceGps = true,
                    )
                  )
                } else {
                  cont.resume(null)
                }
              }
              .addOnFailureListener { cont.resume(null) }
          }
        }
        .addOnFailureListener { cont.resume(null) }
    }
  }

  @SuppressLint("MissingPermission")
  fun observeLiveLocation(context: Context, intervalMs: Long = 10_000L): Flow<Coordinates> =
    callbackFlow {
      if (!hasLocationPermission(context)) {
        close()
        return@callbackFlow
      }
      val client = LocationServices.getFusedLocationProviderClient(context.applicationContext)
      val request =
        LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, intervalMs)
          .setMinUpdateIntervalMillis(5_000L)
          .setMinUpdateDistanceMeters(15f)
          .build()

      val callback =
        object : LocationCallback() {
          override fun onLocationResult(result: LocationResult) {
            val loc = result.lastLocation ?: return
            trySend(
              Coordinates(
                latitude = loc.latitude,
                longitude = loc.longitude,
                accuracyMeters = loc.accuracy,
                isDeviceGps = true,
              )
            )
          }
        }

      client.requestLocationUpdates(request, callback, Looper.getMainLooper())
      awaitClose { client.removeLocationUpdates(callback) }
    }

  /** Haversine distance in kilometers between two GPS coordinates. */
  fun distanceKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    if ((lat1 == 0.0 && lng1 == 0.0) || (lat2 == 0.0 && lng2 == 0.0)) return -1.0
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLng = Math.toRadians(lng2 - lng1)
    val a =
      sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2) * sin(dLng / 2)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return ((earthRadiusKm * c) * 10.0).roundToInt() / 10.0
  }

  fun formatDistance(distanceKm: Double): String {
    if (distanceKm < 0.0) return "Area listed"
    return if (distanceKm < 1.0) {
      val meters = (distanceKm * 1000).roundToInt().coerceAtLeast(50)
      "$meters m away"
    } else {
      "$distanceKm km away"
    }
  }

  fun estimateEtaMinutes(distanceKm: Double): String {
    if (distanceKm < 0.0) return "ETA on accept"
    val mins = ((distanceKm / 28.0) * 60.0).roundToInt().coerceIn(4, 90)
    return "~$mins min ETA"
  }
}
