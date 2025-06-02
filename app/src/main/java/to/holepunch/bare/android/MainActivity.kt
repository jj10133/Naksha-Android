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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.koin.androidx.viewmodel.ext.android.viewModel
import to.holepunch.bare.android.data.GenericAction
import to.holepunch.bare.android.data_access.ipc.IPCMessageConsumer
import to.holepunch.bare.android.data_access.ipc.IPCProvider
import to.holepunch.bare.android.data_access.ipc.IPCUtils.writeAsync
import to.holepunch.bare.android.processing.GenericMessageProcessor
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
    private val homeViewModel: HomeViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }

        worklet = Worklet(null)

        try {
            worklet!!.start("/app.bundle", assets.open("app.bundle"), null)
            ipc = IPC(worklet)
            IPCProvider.ipc = ipc

            messageProcessor = GenericMessageProcessor(homeViewModel)
            ipcMessageConsumer = IPCMessageConsumer(ipc!!, messageProcessor)
            ipcMessageConsumer?.lifecycleScope = lifecycleScope
            ipcMessageConsumer?.startConsuming()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

        lifecycleScope.launch {
            copyStyleFileToInternalStorage()
            start()
            withContext(Dispatchers.Main) {
                setContent {
                    HomeView()
                }
            }
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

    private suspend fun copyStyleFileToInternalStorage() {
        val styleFile = File(filesDir, "style.json")

        if (!styleFile.exists()) {
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

    suspend fun start() {
        val dynamicData = buildJsonObject {
            put("path", filesDir.path)
        }
        val message = GenericAction(
            action = "start",
            data = dynamicData
        )

        val jsonString = Json.encodeToString(message) + "\n"

        val byteBuffer = ByteBuffer.wrap(jsonString.toByteArray(Charset.forName("UTF-8")))
        ipc?.writeAsync(byteBuffer)
    }

}
