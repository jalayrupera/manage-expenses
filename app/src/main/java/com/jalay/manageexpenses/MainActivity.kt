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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.jalay.manageexpenses.data.preferences.ThemeMode
import com.jalay.manageexpenses.data.preferences.ThemePreferences
import com.jalay.manageexpenses.presentation.navigation.AppNavigation
import com.jalay.manageexpenses.presentation.ui.onboarding.PermissionRequestScreen
import com.jalay.manageexpenses.presentation.ui.theme.LocalThemeMode
import com.jalay.manageexpenses.presentation.ui.theme.LocalThemeSetter
import com.jalay.manageexpenses.presentation.ui.theme.ManageExpensesTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

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

        setContent {
            val themeMode by themePreferences.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

            val setThemeMode: (ThemeMode) -> Unit = { mode ->
                lifecycleScope.launch {
                    themePreferences.setThemeMode(mode)
                }
            }

            CompositionLocalProvider(
                LocalThemeMode provides themeMode,
                LocalThemeSetter provides setThemeMode
            ) {
                ManageExpensesTheme(themeMode = themeMode) {
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

                        if (!hasPermission) {
                            PermissionRequestScreen(
                                onRequestPermission = {
                                    requestPermissionLauncher.launch(Manifest.permission.READ_SMS)
                                },
                                onSkip = {
                                    hasPermission = true
                                }
                            )
                        } else {
                            AppNavigation()
                        }
                    }
                }
            }
        }
    }
}
