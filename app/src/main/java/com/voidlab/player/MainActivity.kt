package com.voidlab.player

import android.Manifest
import android.content.ComponentName
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.voidlab.player.audio.playback.PlaybackService
import com.voidlab.player.ui.VoidLabNavHost
import com.voidlab.player.ui.theme.VoidLabTheme
import com.voidlab.player.ui.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private var mediaController: MediaController? = null
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    
    // REMOVED: private val playerViewModel by viewModels()
    // We'll get it from the composable instead
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Permission granted, initializing MediaController")
            initializeMediaController()
        } else {
            Log.e("MainActivity", "Permission denied!")
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("MainActivity", "onCreate started")
        
        checkPermissions()
        
        setContent {
            VoidLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Get PlayerViewModel here and pass mediaController via callback
                    val playerViewModel: PlayerViewModel = hiltViewModel()
                    
                    // Set the MediaController when it's ready
                    if (mediaController != null) {
                        playerViewModel.setMediaController(mediaController!!)
                    }
                    
                    VoidLabNavHost()
                }
            }
        }
    }
    
    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        
        when {
            ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("MainActivity", "Permission already granted")
                initializeMediaController()
            }
            else -> {
                Log.d("MainActivity", "Requesting permission")
                requestPermissionLauncher.launch(permission)
            }
        }
    }
    
    private fun initializeMediaController() {
        Log.d("MainActivity", "Initializing MediaController...")
        
        val sessionToken = SessionToken(
            this,
            ComponentName(this, PlaybackService::class.java)
        )
        
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
        controllerFuture.addListener({
            try {
                mediaController = controllerFuture.get()
                Log.d("MainActivity", "MediaController connected successfully!")
                Log.d("MainActivity", "Call setContent again to trigger recomposition with MediaController")
                // Trigger recomposition
                setContent {
                    VoidLabTheme {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            val playerViewModel: PlayerViewModel = hiltViewModel()
                            Log.d("MainActivity", "Setting MediaController on PlayerViewModel instance: ${playerViewModel.hashCode()}")
                            playerViewModel.setMediaController(mediaController!!)
                            VoidLabNavHost()
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to connect MediaController", e)
            }
        }, MoreExecutors.directExecutor())
    }
    
    override fun onStart() {
        super.onStart()
        Log.d("MainActivity", "onStart")
    }
    
    override fun onStop() {
        super.onStop()
        Log.d("MainActivity", "onStop")
    }
    
    override fun onDestroy() {
        Log.d("MainActivity", "onDestroy - releasing MediaController")
        if (::controllerFuture.isInitialized) {
            MediaController.releaseFuture(controllerFuture)
        }
        super.onDestroy()
    }
}
