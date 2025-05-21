package to.holepunch.bare.android.services

import android.content.Context
import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import java.io.File


class DataProcessingService(private val context: Context) {
    fun updateStyleWithURL(newUrl: String) {
        val styleFile = File(context.filesDir, "style.json")

        if (!styleFile.exists()) {
            Log.e("DataProcessingService", "style.json not found.")
            return
        }

        val styleContent = styleFile.readText()
        val json = Json.parseToJsonElement(styleContent).jsonObject

        val updatedJson = JsonObject(
            json.toMutableMap().toMutableMap().apply {
                val sources = json["sources"]?.jsonObject ?: return
                val protomaps = sources["protomaps"]?.jsonObject ?: return

                val updatedProtomaps = JsonObject(
                    protomaps.toMutableMap().apply {
                        put("url", JsonPrimitive("pmtiles://$newUrl"))
                    }
                )

                val updatedSources = JsonObject(
                    sources.toMutableMap().apply {
                        put("protomaps", updatedProtomaps)
                    }
                )

                put("sources", updatedSources)
            }
        )

        // Save back to file
        styleFile.writeText(Json { prettyPrint = true }.encodeToString(JsonObject.serializer(), updatedJson))

    }
}