package to.holepunch.bare.android.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

import org.maplibre.android.maps.Style
import org.ramani.compose.MapLibre
import to.holepunch.bare.android.viewmodel.HomeViewModel

@Composable
fun HomeView(homeViewModel: HomeViewModel) {
    var styleUri by homeViewModel.styleUri

    LaunchedEffect(Unit) {
        homeViewModel.getMapLink()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            MapLibre(
                modifier = Modifier.fillMaxSize(),
                styleBuilder = Style.Builder()
                    .fromUri(styleUri)
            )
        }
    }
}