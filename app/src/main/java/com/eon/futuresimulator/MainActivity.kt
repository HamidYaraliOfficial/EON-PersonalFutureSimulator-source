package com.eon.futuresimulator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.getValue
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.eon.futuresimulator.core.navigation.EonApp
import com.eon.futuresimulator.core.preferences.UserPreferencesRepository
import com.eon.futuresimulator.core.theme.EonTheme
import com.eon.futuresimulator.core.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Single-Activity host. FragmentActivity (not plain ComponentActivity) because
 * BiometricPrompt (Privacy Center) requires a FragmentActivity.
 */
@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val deepLink = intent?.getStringExtra(EXTRA_DEEP_LINK)

        setContent {
            val themeMode by userPreferencesRepository.themeMode.collectAsStateWithLifecycle(initialValue = ThemeMode.SYSTEM)
            val dynamicColorAllowed by userPreferencesRepository.dynamicColorAllowed.collectAsStateWithLifecycle(initialValue = false)
            val languageTag by userPreferencesRepository.languageTag.collectAsStateWithLifecycle(initialValue = "en")

            LaunchedApplyLocale(languageTag)

            EonTheme(themeMode = themeMode, dynamicColorAllowed = dynamicColorAllowed) {
                EonApp(startDeepLink = if (deepLink == DEEP_LINK_WHAT_IF) "what_if" else null)
            }
        }
    }

    companion object {
        const val EXTRA_DEEP_LINK = "eon_deep_link"
        const val DEEP_LINK_WHAT_IF = "what_if"
    }
}

@androidx.compose.runtime.Composable
private fun LaunchedApplyLocale(languageTag: String) {
    androidx.compose.runtime.LaunchedEffect(languageTag) {
        val current = AppCompatDelegate.getApplicationLocales()
        if (current.toLanguageTags() != languageTag) {
            AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(languageTag))
        }
    }
}
