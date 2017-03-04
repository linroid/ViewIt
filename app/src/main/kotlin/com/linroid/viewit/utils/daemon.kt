package com.linroid.viewit.utils

import rx.Observable
import rx.schedulers.Schedulers
import timber.log.Timber

/**
 * @author linroid <linroid@gmail.com>
 * @since 04/03/2017
 */
fun daemon(body: () -> Unit) {
    Observable.create<Unit> {
        body()
        it.onNext(null)
        it.onCompleted()
    }.observeOn(Schedulers.io()).subscribe({}, { error -> Timber.e(error, "daemon error") })
}