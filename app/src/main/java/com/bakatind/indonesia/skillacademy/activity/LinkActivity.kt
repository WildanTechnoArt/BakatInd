package com.bakatind.indonesia.skillacademy.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import com.bakatind.indonesia.skillacademy.R
import com.bakatind.indonesia.skillacademy.util.UtilsConstant.LINK_LESSON
import kotlinx.android.synthetic.main.activity_link.*

class LinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_link)
        settings()
        loadWebView()
        swipe_refresh?.setOnRefreshListener {
            loadWebView()
        }
    }

    private fun loadWebView() {
        swipe_refresh?.isRefreshing = true
        val getLink = intent.getStringExtra(LINK_LESSON).toString()

        wv_link.loadUrl(getLink)
        wv_link.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, URL: String?): Boolean {
                swipe_refresh?.isRefreshing = true
                return true
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                swipe_refresh?.isRefreshing = false
            }
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun settings() {
        val mSettings = wv_link.settings
        mSettings.javaScriptEnabled = true
        mSettings.allowContentAccess = true
        mSettings.loadsImagesAutomatically = true
        mSettings.domStorageEnabled = true
    }
}