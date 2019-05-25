package com.netchar.common.base

import androidx.lifecycle.ViewModel
import com.netchar.common.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel


/**
 * Created by Netchar on 26.04.2019.
 * e.glushankov@gmail.com
 */

open class BaseViewModel(dispatchers: CoroutineDispatchers) : ViewModel() {
    protected val job = SupervisorJob()
    protected val scope = CoroutineScope(job + dispatchers.main)

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}