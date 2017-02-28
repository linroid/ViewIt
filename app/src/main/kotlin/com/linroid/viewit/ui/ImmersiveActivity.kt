package com.linroid.viewit.ui

import android.os.Build
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.View.*
import timber.log.Timber

/**
 * 沉浸模式的 Activity
 * 如果继承这个 Activity，主题需要使用 @style/AppTheme.Immersive
 *
 * @author linroid <linroid@gmail.com>
 * @since 25/01/2017
 */
abstract class ImmersiveActivity : BaseActivity() {
    /**
     * the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private val AUTO_HIDE_DELAY_MILLIS = 3000L

    private val ACTION_BAR_HIDE_DELAY_MILLIS = 300L

    private lateinit var decorView: View

    private val delayHideCallback = Runnable { hide() }
    private val delayHideActionBarCallback = Runnable { supportActionBar?.hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        decorView = window.decorView;
        decorView.setOnSystemUiVisibilityChangeListener {
            Timber.v("height changed:${decorView.height}, visibility:$it")
        }
        setImmersiveMode()
    }

    private fun setImmersiveMode() {
        var uiOptions = decorView.systemUiVisibility
        if (Build.VERSION.SDK_INT >= 19) {
            uiOptions = uiOptions or SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        }
        uiOptions = uiOptions or SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        uiOptions = uiOptions or SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        uiOptions = uiOptions or SYSTEM_UI_FLAG_LOW_PROFILE
        decorView.systemUiVisibility = uiOptions
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.actionMasked == MotionEvent.ACTION_DOWN) {
            if (componentsHidden()) {
                show();
                return true
            }
        }
        delayHide()
        return super.dispatchTouchEvent(ev)
    }

    fun toggle() {
        if (componentsHidden()) {
            show()
        } else {
            hide()
        }
    }

    fun delayHide() {
        decorView.removeCallbacks(delayHideCallback)
        decorView.postDelayed(delayHideCallback, AUTO_HIDE_DELAY_MILLIS)
    }

    fun pauseHide() {
        decorView.removeCallbacks(delayHideCallback)
    }

    protected fun componentsHidden(): Boolean {
        return supportActionBar?.isShowing?.not() ?: true
    }

    private fun hide() {
        var uiOptions = decorView.systemUiVisibility
        uiOptions = uiOptions or SYSTEM_UI_FLAG_HIDE_NAVIGATION
        uiOptions = uiOptions or SYSTEM_UI_FLAG_FULLSCREEN
        decorView.systemUiVisibility = uiOptions
        decorView.postDelayed(delayHideActionBarCallback, ACTION_BAR_HIDE_DELAY_MILLIS)
        shouldHideComponents()
    }

    private fun show() {
        var uiOptions = decorView.systemUiVisibility
        uiOptions = uiOptions and SYSTEM_UI_FLAG_HIDE_NAVIGATION.inv()
        uiOptions = uiOptions and SYSTEM_UI_FLAG_FULLSCREEN.inv()
        decorView.systemUiVisibility = uiOptions
        supportActionBar?.show()
        decorView.removeCallbacks(delayHideActionBarCallback)
        shouldShowComponents()
        delayHide()
    }

    override fun onPause() {
        super.onPause()
        pauseHide()
    }

    override fun onResume() {
        super.onResume()
        delayHide()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    abstract protected fun shouldHideComponents()
    abstract protected fun shouldShowComponents()
}