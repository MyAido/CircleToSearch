package com.rr.aido.ui.circletosearch

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.rr.aido.ui.circletosearch.data.BitmapRepository
import com.rr.aido.ui.circletosearch.ui.CircleToSearchScreen
import com.rr.aido.ui.theme.AidoTheme // Assuming Aido has a theme, attempting to use it or just Default
// Replacing CircleToSearchTheme with a basic wrapper or AidoTheme

class OverlayActivity : ComponentActivity() {
    
    private val screenshotBitmap = androidx.compose.runtime.mutableStateOf<android.graphics.Bitmap?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        android.util.Log.d("CircleToSearch", "OverlayActivity onCreate")
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        
        loadScreenshot()

        val dataStoreManager = com.rr.aido.data.DataStoreManager(applicationContext)
        val geminiRepository = com.rr.aido.data.repository.GeminiRepositoryImpl()

        setContent {
            // Using a simple Surface without custom Theme for now, or use AidoTheme if available
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Transparent
            ) {
                CircleToSearchScreen(
                    screenshot = screenshotBitmap.value,
                    dataStoreManager = dataStoreManager,
                    geminiRepository = geminiRepository,
                    onClose = { 
                        BitmapRepository.clear()
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        android.util.Log.d("CircleToSearch", "OverlayActivity onNewIntent")
        setIntent(intent)
        loadScreenshot()
    }

    private fun loadScreenshot() {
        val bitmap = BitmapRepository.getScreenshot()
        if (bitmap != null) {
            android.util.Log.d("CircleToSearch", "Bitmap loaded from Repository. Size: ${bitmap.width}x${bitmap.height}")
            screenshotBitmap.value = bitmap
        } else {
            android.util.Log.e("CircleToSearch", "No bitmap in Repository")
            // Fallback or finish? For now just log.
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
             BitmapRepository.clear()
        }
    }
}
