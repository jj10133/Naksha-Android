package to.holepunch.bare.android.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import to.holepunch.bare.android.data_access.ipc.IPCUtils.writeAsync
import to.holepunch.bare.android.services.DataProcessingService
import to.holepunch.bare.kit.IPC
import java.nio.ByteBuffer
import java.nio.charset.Charset

class HomeViewModel(application: Application, private val ipc: IPC) : ViewModel() {
    private val appContext: Context = application.applicationContext
    private lateinit var dataProcessingService: DataProcessingService

    var styleUri: MutableState<String> = mutableStateOf("")

    fun setDataProcessingService(service: DataProcessingService) {
        this.dataProcessingService = service
        this.dataProcessingService.onStyleUpdated = {
            val mapLink = "file://${appContext.filesDir}/style.json"
            styleUri.value = mapLink
        }
    }

    suspend fun getMapLink() {
        val message = "{\"action\": \"requestMapLink\"}"

        val byteBuffer = ByteBuffer.wrap(message.toByteArray(Charset.forName("UTF-8")))
        ipc.writeAsync(byteBuffer)
    }
}