package to.holepunch.bare.android.processing.services

import android.util.Log

class DataProcessingService {
    fun processDataA(value: String) {
        Log.i("DataService", "Processing Data A: $value")
        // Your specific logic for processing data A
    }

    fun processDataB(id: Int, name: String) {
        Log.i("DataService", "Processing Data B: ID=$id, Name=$name")
        // Your specific logic for processing data B
    }
}