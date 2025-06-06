package com.foss.naksha.android.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.foss.naksha.android.data.GenericAction
import com.foss.naksha.android.data_access.ipc.IPCUtils.writeAsync
import com.foss.naksha.android.processing.UpdateState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import org.ramani.compose.LocationRequestProperties
import to.holepunch.bare.kit.IPC
import java.io.File
import java.nio.ByteBuffer
import java.nio.charset.Charset

class HomeViewModel(context: Context, private val ipc: IPC) : ViewModel(), UpdateState {
    private val fileDir = context.filesDir

    val styleUrl: MutableState<String> = mutableStateOf("")
    val locationPropertiesState: MutableState<LocationRequestProperties> = mutableStateOf(LocationRequestProperties())

    suspend fun getMapLink() {
        val dynamicData = buildJsonObject {}
        val message = GenericAction(
            action = "requestMapLink",
            data = dynamicData
        )

        val jsonString = Json.encodeToString(message) + "\n"

        val byteBuffer = ByteBuffer.wrap(jsonString.toByteArray(Charset.forName("UTF-8")))
        ipc.writeAsync(byteBuffer)
    }

    override fun setStyleUrl(url: String) {
        val styleFile = File(fileDir, "style.json")

        if (!styleFile.exists()) {
            Log.e("HomeViewModel", "style.json not found.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val styleContent = styleFile.readText()
                val json = Json.parseToJsonElement(styleContent).jsonObject

                // Use an early return here instead of inside the `apply`
                val sources = json["sources"]?.jsonObject
                if (sources == null) {
                    Log.e("HomeViewModel", "No 'sources' found in style.json")
                    return@launch // Exit the coroutine early
                }

                val protomaps = sources["protomaps"]?.jsonObject
                if (protomaps == null) {
                    Log.e("HomeViewModel", "No 'protomaps' found in style.json")
                    return@launch // Exit the coroutine early
                }

                val updatedProtomaps = JsonObject(
                    protomaps.toMutableMap().apply {
                        put("url", JsonPrimitive("pmtiles://$url"))
                    }
                )

                val updatedSources = JsonObject(
                    sources.toMutableMap().apply {
                        put("protomaps", updatedProtomaps)
                    }
                )

                val updatedJson = JsonObject(
                    json.toMutableMap().toMutableMap().apply {
                        put("sources", updatedSources)
                    }
                )

                // Save back to file
                styleFile.writeText(Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), updatedJson))
                withContext(Dispatchers.Main) {
                    styleUrl.value = "file://${fileDir}/style.json"
                }
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error updating style.json: ${e.message}")
            }
        }
    }
}