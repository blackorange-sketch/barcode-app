package io.github.blackorange_sketch.twa

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private val baseUrl = "https://blackorange-sketch.github.io/barcode-app/index.html"
    private val cameraRequestCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.mediaPlaybackRequiresUserGesture = false
        webView.settings.cacheMode = WebSettings.LOAD_DEFAULT
        webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                runOnUiThread {
                    val needsCamera = request.resources.contains(PermissionRequest.RESOURCE_VIDEO_CAPTURE)
                    if (needsCamera) {
                        if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED
                        ) {
                            requestPermissions(arrayOf(Manifest.permission.CAMERA), cameraRequestCode)
                        }
                        // Android сам покаже системний діалог дозволу камери,
                        // якщо потрібно; grant() дозволяє WebView отримати доступ,
                        // коли/якщо системний дозвіл уже надано.
                        request.grant(request.resources)
                    } else {
                        request.deny()
                    }
                }
            }
        }

        loadFromIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        loadFromIntent(intent)
    }

    /**
     * Власна обробка "Поділитися" — напряму з Android Intent, без залежності
     * від share_target у web-маніфесті (там був невирішений баг Bubblewrap).
     */
    private fun loadFromIntent(intent: Intent?) {
        var url = baseUrl
        if (intent?.action == Intent.ACTION_SEND && intent.type == "text/plain") {
            val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT)
            if (!sharedText.isNullOrBlank()) {
                url = baseUrl + "?shared_text=" + Uri.encode(sharedText)
            }
        }
        webView.loadUrl(url)
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }
}
