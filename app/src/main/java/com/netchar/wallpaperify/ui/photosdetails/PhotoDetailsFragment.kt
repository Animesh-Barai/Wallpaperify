package com.netchar.wallpaperify.ui.photosdetails

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.transition.*
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netchar.common.base.BaseFragment
import com.netchar.common.extensions.toVisible
import com.netchar.wallpaperify.R
import com.transitionseverywhere.extra.Scale
import kotlinx.android.synthetic.main.fragment_photo_details.*

class PhotoDetailsFragment : BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_photo_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val transition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        transition.duration = 200

        sharedElementEnterTransition = transition

        transition.addListener(object : TransitionListenerAdapter() {

            override fun onTransitionEnd(transition: Transition) {

                toolbar?.let {

                    val set = TransitionSet()

                    val first = TransitionSet()
                    first.addTransition(Fade())
                    first.duration = 250
                    first.interpolator = LinearInterpolator()
                    first.addTarget(photo_details_bottom_sheet_layout)
                    first.addTarget(it)

                    val second = TransitionSet()
                    second.addTransition(Scale(0.2f))
                    second.addTransition(Fade())
                    second.addTarget(photo_details_floating_action_btn)

                    set.setOrdering(TransitionSet.ORDERING_SEQUENTIAL)
                        .addTransition(first)
                        .addTransition(second)

                    TransitionManager.beginDelayedTransition(photo_details_coordinator, set)

                    photo_details_bottom_sheet_layout.toVisible()
                    photo_details_floating_action_btn.toVisible()
                    it.toVisible()
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        var flags = activity?.window?.decorView?.systemUiVisibility!!
//        flags = flags xor View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//        activity?.window?.decorView?.systemUiVisibility = flags

//        activity?.window?.decorView?.systemUiVisibility  = flags
//        activity?.window?.navigationBarColor = Color.BLACK
        photo_details_coordinator.setOnApplyWindowInsetsListener { _, windowInsets ->
            toolbar?.updatePadding(top = windowInsets.systemWindowInsetTop, bottom = 0)
//            toolbar?.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = windowInsets.systemWindowInsetTop
//            }
            photo_details_bottom_sheet_layout.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                setMargins(0, 0, 0, windowInsets.systemWindowInsetBottom)
            }
//            photo_details_floating_action_btn.updatePadding(bottom = 0)
            windowInsets.consumeSystemWindowInsets()
        }

//
//        photo_details_bottom_sheet.getConstraintSet(R.id.start)?.let { startConstraintSet ->
//            startConstraintSet.constrainMinHeight(R.id.photo_details_bottom_sheet_layout, 500.dp)
//        }

        arguments?.let {
            val safeArgs = PhotoDetailsFragmentArgs.fromBundle(it)
            postponeEnterTransition()
            photo_details_image.transitionName = safeArgs.imageTransitionName

            Glide.with(this)
                .load(safeArgs.photoUrl)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }

                    override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                        startPostponedEnterTransition()
                        return false
                    }
                })
                .into(photo_details_image)
        }
    }
}
