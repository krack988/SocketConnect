package com.example.socketconnect.common

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        if (hasActiveObservers()) {
            Timber.tag(LOG_TAG)
                .w("Зарегистрировано несколько observers, но оповещен об изменениях, будет только один.")
        }

        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    @MainThread
    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }

    /**
     * Нужно использовать, когда T равно Void, для более чистого вызова.
     * Однако, в нашем случае, такого быть не должно, т.к. T всегда будет равно [Resource], а Void,
     * будет уже внутри него
     */
    @MainThread
    fun call() {
        value = null
    }

    companion object {
        private val LOG_TAG = "SingleLiveEvent"
    }
}