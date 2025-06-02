package com.foss.naksha.android.processing

import android.util.Log
import com.foss.naksha.android.data.GenericAction
import com.foss.naksha.android.viewmodel.HomeViewModel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class GenericMessageProcessor(val homeViewModel: HomeViewModel) : MessageProcessor {
    override fun processMessage(message: String) {
        try {
            val incomingMessage = Json.decodeFromString<GenericAction>(message)
            when (incomingMessage.action) {
                "requestLink" -> {
                    val value = incomingMessage.data?.jsonObject["url"]?.jsonPrimitive?.content
                    value?.let { homeViewModel.setStyleUrl(it) }
                        ?: Log.w("GenericProcessor", "Missing 'value' for action 'process_a'")
                }
                else -> Log.w("GenericProcessor", "Unknown action: ${incomingMessage.action}")

            }
        } catch (e: Exception) {
            Log.e("GenericProcessor", "Error processing message: ${e.message}")
        }
    }
}