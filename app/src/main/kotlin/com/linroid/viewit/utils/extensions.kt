package com.linroid.viewit.utils

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