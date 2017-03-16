package com.linroid.viewit.utils

import android.view.View
import android.view.ViewGroup
import rx.Observable
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */
fun Subscription?.unsubscribeIfNotNull() {
    if (this != null && !this.isUnsubscribed) {
        this.unsubscribe()
    }
}

fun <T> Observable<T>.onMain(): Observable<T> {
    return this.observeOn(AndroidSchedulers.mainThread())
}


fun View.removeFromParent() {
    if (this.parent != null && this.parent is ViewGroup) {
        (this.parent as ViewGroup).removeView(this)
    }
}