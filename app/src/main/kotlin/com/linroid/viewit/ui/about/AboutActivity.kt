package com.linroid.viewit.ui.about

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.avos.avoscloud.AVAnalytics
import com.linroid.viewit.BuildConfig
import com.linroid.viewit.R
import com.linroid.viewit.utils.ALIPAY_QRCODE
import com.linroid.viewit.utils.EVENT_CLICK_ABOUT_DONATE
import com.linroid.viewit.utils.EVENT_CLICK_ABOUT_SHARE
import me.drakeet.multitype.Items
import me.drakeet.support.about.*
import java.net.URLEncoder

/**
 * @author linroid <linroid@gmail.com>
 * @since 30/01/2017
 */
class AboutActivity : AbsAboutActivity() {
    @SuppressLint("SetTextI18n")
    companion object {
        fun navTo(context: Context) {
            context.startActivity(Intent(context, AboutActivity::class.java))
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onPause() {
        super.onPause()
        AVAnalytics.onPause(this)
    }

    override fun onResume() {
        super.onResume()
        AVAnalytics.onResume(this)
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateHeader(icon: ImageView, slogan: TextView, version: TextView) {
        setHeaderContentColor(Color.WHITE)
        icon.setImageResource(R.drawable.ic_eye)
        slogan.setText(R.string.about_title)
        version.text = "v ${BuildConfig.VERSION_NAME}"
    }

    override fun onItemsCreated(items: Items) {
        adapter.register(Developer::class.java, DeveloperViewProvider())

        items.add(Category(getString(R.string.about_header_introduce)))
        items.add(Card(getString(R.string.about_introduce_content), getString(R.string.about_action_donate)))

        items.add(Line())
        items.add(Category("Developers"))
        items.add(Developer(R.drawable.avatar, "linroid", "Developer & designer", "http://linroid.com"))

        items.add(Line())

        items.add(Category("Open Source Licenses"))
        items.add(License("Kotlin", "JetBrains s.r.o.", License.APACHE_2, "https://github.com/JetBrains/kotlin"))
        items.add(License("RxAndroid", "ReactiveX", License.APACHE_2, "https://github.com/ReactiveX/RxAndroid"))
        items.add(License("RxJava", "ReactiveX", License.APACHE_2, "https://github.com/ReactiveX/RxJava"))
        items.add(License("RxLifecycle", "trello", License.APACHE_2, "https://github.com/trello/RxLifecycle"))
        items.add(License("Dagger 2.0", "Google.Inc.", License.APACHE_2, "https://github.com/google/dagger"))
        items.add(License("Timber", "Jake Wharton", License.APACHE_2, "https://github.com/JakeWharton/timber"))
        items.add(License("Glide", "bumptech", "BSD, part MIT and Apache 2.0", "https://github.com/bumptech/glide"))
        items.add(License("PaperParcel", "Bradley Campbell", License.APACHE_2, "https://github.com/grandstaish/paperparcel"))
        items.add(License("sugar", "Satya Narayan", License.MIT, "https://github.com/satyan/sugar"))
        items.add(License("MultiType", "drakeet", License.APACHE_2, "https://github.com/drakeet/MultiType"))
        items.add(License("about-page", "drakeet", License.APACHE_2, "https://github.com/drakeet/about-page"))
    }

    override fun onActionClick(action: View?) {
        openAlipay()
        AVAnalytics.onEvent(this, EVENT_CLICK_ABOUT_DONATE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.about, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            R.id.action_share_app -> {
                AVAnalytics.onEvent(this, EVENT_CLICK_ABOUT_SHARE)
                shareApp()
                return true
            }
        }
        return super.onOptionsItemSelected(menuItem)
    }


    private fun openAlipay() {
        try {
            startActivity(Intent().apply {
                action = Intent.ACTION_VIEW
                val qrCode = URLEncoder.encode(ALIPAY_QRCODE, "UTF-8")
                val url = "alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=${qrCode}%3F_s%3Dweb-other&_t=${System.currentTimeMillis()}"
                data = Uri.parse(url)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } catch (error: Exception) {
            Toast.makeText(this, R.string.error_open_alipay, Toast.LENGTH_LONG).show()
        }
    }


    private fun shareApp() {
        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.action_share_app))
            putExtra(Intent.EXTRA_TEXT, getString(R.string.share_content));
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }, getString(R.string.action_share_app)))
    }
}