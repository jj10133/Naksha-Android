package com.foss.naksha.android.ui

import android.graphics.Color
import android.location.Location
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.foss.naksha.android.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre

@Composable
fun HomeView(homeViewModel: HomeViewModel = koinViewModel()) {
    var styleUri by homeViewModel.styleUrl
    val locationProperties by homeViewModel.locationPropertiesState

    val cameraPosition = rememberSaveable { mutableStateOf(CameraPosition(zoom = 14.0)) }
    val cameraMode = rememberSaveable { mutableIntStateOf(CameraMode.TRACKING) }
    val renderMode = rememberSaveable { mutableIntStateOf(RenderMode.NORMAL) }
    val userLocation = rememberSaveable { mutableStateOf(Location("gps")) }

    PermissionRequest()

    LaunchedEffect(Unit) {
        delay(2000)
        homeViewModel.getMapLink()
    }

    if (styleUri.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                MapLibre(
                    modifier = Modifier.fillMaxSize(),
                    styleBuilder = Style.Builder()
                        .fromUri(styleUri),
                    cameraPosition = cameraPosition.value,
                    locationRequestProperties = locationProperties,
                    locationStyling = LocationStyling(
                        enablePulse = true,
                        pulseColor = Color.BLUE
                    ),
                    userLocation = userLocation,
                    cameraMode = cameraMode,
                    renderMode = renderMode.value
                )
            }
        }
    }
}