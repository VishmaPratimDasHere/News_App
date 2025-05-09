package com.yourapp.translator

import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

// Coroutines
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.tasks.await   // <-- comes from coroutines-play-services

// ML Kit
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier

class TranslatorExampleActivity : ComponentActivity() {

    // on-device translator client (EN → HI)
    private val translator: Translator by lazy {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(TranslateLanguage.HINDI)
            .build()
        Translation.getClient(options)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var uiText by remember { mutableStateOf("Preparing model…") }

            LaunchedEffect(Unit) {
                uiText = try {
                    // 1) download model if needed (suspends)
                    withContext(Dispatchers.IO) {
                        translator.downloadModelIfNeeded().await()
                    }
                    // 2) translate (suspends)
                    withContext(Dispatchers.IO) {
                        translator.translate("Here’s some sample content to translate.").await()
                    }
                } catch (e: Exception) {
                    "Error: ${e.localizedMessage}"
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Text(
                    text = uiText,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        translator.close() // free model resources
    }
}
