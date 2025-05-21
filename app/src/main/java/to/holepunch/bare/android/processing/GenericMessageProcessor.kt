package to.holepunch.bare.android.processing

import android.util.Log
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import to.holepunch.bare.android.data.GenericAction
import to.holepunch.bare.android.services.DataProcessingService

class GenericMessageProcessor(private val dataProcessingService: DataProcessingService): MessageProcessor {
    override fun processMessage(message: String) {
        try {
            val incomingMessage = Json.decodeFromString<GenericAction>(message)
            when (incomingMessage.action) {
                "requestLink" -> {
                    val value = incomingMessage.data?.jsonObject["url"]?.jsonPrimitive?.content
                    value?.let { dataProcessingService.updateStyleWithURL(it) }
                        ?: Log.w("GenericProcessor", "Missing 'value' for action 'process_a'")
                }
                else -> Log.w("GenericProcessor", "Unknown action: ${incomingMessage.action}")

            }
        } catch (e: Exception) {
            Log.e("GenericProcessor", "Error processing message: ${e.message}")
        }
    }
}