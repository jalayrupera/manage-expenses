package com.jalay.manageexpenses

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.jalay.manageexpenses.presentation.navigation.AppNavigation
import com.jalay.manageexpenses.presentation.ui.onboarding.PermissionRequestScreen
import com.jalay.manageexpenses.presentation.ui.theme.ManageExpensesTheme

class MainActivity : ComponentActivity() {

    // State holder for permission - accessible from callback
    private var permissionGrantedCallback: (() -> Unit)? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
            permissionGrantedCallback?.invoke()
        } else {
            Toast.makeText(this, "SMS permission required to read transactions", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = AppContainer.getInstance(this)

        setContent {
            ManageExpensesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var hasPermission by remember {
                        mutableStateOf(
                            ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.READ_SMS
                            ) == PackageManager.PERMISSION_GRANTED
                        )
                    }

                    // Set the callback to update Compose state
                    LaunchedEffect(Unit) {
                        permissionGrantedCallback = { hasPermission = true }
                    }

                    LaunchedEffect(Unit) {
                        appContainer.initializeDefaultCategories()
                    }

                    if (!hasPermission) {
                        PermissionRequestScreen(
                            onRequestPermission = {
                                requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
                            }
                        )
                    } else {
                        AppNavigation(appContainer = appContainer)
                    }
                }
            }
        }
    }
}