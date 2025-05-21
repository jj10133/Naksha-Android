package to.holepunch.bare.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import to.holepunch.bare.android.data_access.ipc.IPCMessageConsumer
import to.holepunch.bare.android.data_access.ipc.IPCUtils.writeAsync
import to.holepunch.bare.android.processing.GenericMessageProcessor
import to.holepunch.bare.android.services.DataProcessingService
import to.holepunch.bare.android.ui.HomeView
import to.holepunch.bare.android.viewmodel.HomeViewModel
import to.holepunch.bare.kit.IPC
import to.holepunch.bare.kit.Worklet
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset

class MainActivity : ComponentActivity() {
    private var worklet: Worklet? = null
    private var ipc: IPC? = null
    private lateinit var messageProcessor: GenericMessageProcessor
    private var ipcMessageConsumer: IPCMessageConsumer? = null
    private lateinit var dataProcessingService: DataProcessingService
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        dataProcessingService = DataProcessingService(this)
        messageProcessor = GenericMessageProcessor(dataProcessingService)

        worklet = Worklet(null)

        try {
            worklet!!.start("/app.bundle", assets.open("app.bundle"), null)
            ipc = IPC(worklet)
            ipcMessageConsumer = IPCMessageConsumer(ipc!!, messageProcessor)
            ipcMessageConsumer?.lifecycleScope = lifecycleScope
            ipcMessageConsumer?.startConsuming()
            homeViewModel = HomeViewModel(this.application, ipc!!)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        lifecycleScope.launch(Dispatchers.IO) {
            start()
        }

        copyStyleFileToInternalStorage()

        setContent {
            HomeView(homeViewModel)
        }
    }

    override fun onPause() {
        super.onPause()

        worklet!!.suspend()
    }

    override fun onResume() {
        super.onResume()

        worklet!!.resume()
    }

    override fun onDestroy() {
        super.onDestroy()

        worklet!!.terminate()
        worklet = null
    }

    private fun copyStyleFileToInternalStorage() {
        val styleFile = File(filesDir, "style.json")

        if (!styleFile.exists()) {
            lifecycleScope.launch {
                try {
                    assets.open("style.json").use { inputStream ->
                        FileOutputStream(styleFile).use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    withContext(Dispatchers.Main) {
                        Log.d("MainActivity", "style.json copied to internal storage")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e("MainActivity", "Error copying style.json: ${e.message}")
                    }
                }
            }
        }
    }

    suspend fun start() {
        val message = """
        {
            "action": "start",
            "data": "$filesDir"
        }
        """.trimIndent()

        val byteBuffer = ByteBuffer.wrap(message.toByteArray(Charset.forName("UTF-8")))
        ipc?.writeAsync(byteBuffer)
    }

}
