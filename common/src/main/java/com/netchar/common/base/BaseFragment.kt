/*
 * Copyright © 2019 Eugene Glushankov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netchar.common.base

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.annotation.CheckResult
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.netchar.common.R
import com.netchar.common.utils.Injector
import com.netchar.common.utils.navigation.IToolbarNavigationBinder
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import javax.inject.Inject


abstract class BaseFragment : Fragment(), HasSupportFragmentInjector {

    @Inject
    lateinit var childFragmentInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var navigationBinder: IToolbarNavigationBinder

    @get:LayoutRes
    abstract val layoutResId: Int

    protected var fragmentToolbar: Toolbar? = null

    protected lateinit var baseContext: Context

    override fun onAttach(context: Context) {
        Injector.inject(this)
        super.onAttach(context)
        baseContext = context
    }

    override fun supportFragmentInjector() = childFragmentInjector

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(layoutResId, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentToolbar = view.findViewById(R.id.toolbar)
        navigationBinder.bind(this, fragmentToolbar)
    }

    @CheckResult
    open fun onBackPressed(): Boolean {
        return false
    }

    /*
        Disable touch events during fragment page transition animation. Since it's
        allows to add a view into parent with not yet detached view and cause IllegalStateException
     */
    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        val animation: Animation?
        if (nextAnim == 0) {
            animation = super.onCreateAnimation(transit, enter, nextAnim)
        } else {
            animation = AnimationUtils.loadAnimation(activity, nextAnim)
            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
                    disableTouches()
                }

                override fun onAnimationRepeat(animation: Animation) {}

                override fun onAnimationEnd(animation: Animation) {
                    enableTouches()

                    if (enter) {
                        onEnterAnimationComplete()
                    }
                }

                private fun enableTouches() {
                    view?.setLayerType(View.LAYER_TYPE_NONE, null)
                    activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }

                private fun disableTouches() {
                    view?.setLayerType(View.LAYER_TYPE_HARDWARE, null)
                    activity?.window?.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                }
            })
        }
        return animation
    }

    protected open fun onEnterAnimationComplete() {

    }

    protected inline fun <T> LiveData<T>.observe(crossinline function: (T) -> Unit) = this.observe(viewLifecycleOwner, Observer { it?.let(function) })
}

