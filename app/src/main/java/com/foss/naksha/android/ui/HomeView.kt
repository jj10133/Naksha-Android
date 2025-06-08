package com.foss.naksha.android.ui

import android.graphics.Color
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.foss.naksha.android.manager.LocationManager
import com.foss.naksha.android.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.Style
import org.ramani.compose.CameraPosition
import org.ramani.compose.LocationStyling
import org.ramani.compose.MapLibre

@Composable
fun HomeView(
        homeViewModel: HomeViewModel = koinViewModel(),
        locationManager: LocationManager = koinInject()
) {
    var styleUri by homeViewModel.styleUrl
    val locationProperties by homeViewModel.locationPropertiesState

    val cameraPosition = rememberSaveable { mutableStateOf(CameraPosition(zoom = 14.0)) }
    val renderMode = rememberSaveable { mutableIntStateOf(RenderMode.NORMAL) }

    PermissionRequest()

    LaunchedEffect(Unit) {
        delay(2000)
        homeViewModel.getMapLink()
        locationManager.getLocation { latitude, longitude ->
            cameraPosition.value = CameraPosition(target = LatLng(latitude, longitude), zoom = 14.0)
        }
    }

    if (styleUri.isNotEmpty()) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column {
                MapLibre(
                        modifier = Modifier.fillMaxSize(),
                        styleBuilder = Style.Builder().fromUri(styleUri),
                        cameraPosition = cameraPosition.value,
                        locationRequestProperties = locationProperties,
                        locationStyling =
                                LocationStyling(enablePulse = true, pulseColor = Color.BLUE),
                        renderMode = renderMode.value
                )
            }
        }
    }
}
