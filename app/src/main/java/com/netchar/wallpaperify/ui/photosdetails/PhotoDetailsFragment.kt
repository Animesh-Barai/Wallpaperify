package com.netchar.wallpaperify.ui.photosdetails

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.transition.Fade
import androidx.transition.TransitionInflater
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.netchar.common.base.BaseFragment
import com.netchar.wallpaperify.R
import kotlinx.android.synthetic.main.fragment_photo_details.*

class PhotoDetailsFragment : BaseFragment() {

    override val layoutResId: Int = R.layout.fragment_photo_details

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            val safeArgs = PhotoDetailsFragmentArgs.fromBundle(it)

            postponeEnterTransition()

            photo_details_image.transitionName = safeArgs.imageTransitionName
            sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
            enterTransition = Fade().apply {
                excludeTarget(android.R.id.statusBarBackground, true)
                excludeTarget(android.R.id.navigationBarBackground, true)
                excludeTarget(R.id.photo_details_image, true)
            }

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
