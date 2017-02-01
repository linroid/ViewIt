package com.linroid.viewit.utils

import rx.Subscription

/**
 * @author linroid <linroid@gmail.com>
 * @since 07/01/2017
 */

fun Subscription?.unsubscribeIfNotNull() {
    this?.unsubscribe()
}