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

package com.netchar.wallpaperify.ui.photosdetails

import android.app.AlertDialog
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.view.animation.OvershootInterpolator
import android.widget.TextView
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.lifecycle.Observer
import androidx.transition.AutoTransition
import androidx.transition.Transition
import androidx.transition.TransitionInflater
import androidx.transition.TransitionManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.netchar.common.base.BaseFragment
import com.netchar.common.extensions.*
import com.netchar.common.utils.getThemeAttrColor
import com.netchar.wallpaperify.R
import com.netchar.wallpaperify.di.ViewModelFactory
import kotlinx.android.synthetic.main.fragment_photo_details.*
import timber.log.Timber
import javax.inject.Inject

class PhotoDetailsFragment : BaseFragment() {

    private var isMenuOpen: Boolean = false
    private val interpolator = OvershootInterpolator()
    private val autoTransition = AutoTransition().apply { duration = 100 }
    private val initialFabTranslationY = 100f
    private val initialFabLabelTranslationX = 100f

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: PhotoDetailsViewModel

    override val layoutResId: Int = R.layout.fragment_photo_details

    private val safeArguments: PhotoDetailsFragmentArgs by lazy {
        PhotoDetailsFragmentArgs.fromBundle(arguments!!)
    }

    private val downloadDialog: DownloadDialogFragment by lazy {
        DownloadDialogFragment().apply {
            onDialogCancel = { viewModel.cancelDownloading() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
        postponeEnterTransition()
        sharedElementEnterTransition = getEnterTransitionAnimation()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = injectViewModel(viewModelFactory)

        setTransparentStatusBars()
        applyWindowsInsets()
        disableToolbarTitle()
        initFabs()
        initViews()
        observe()
    }

    private fun initViews() {
        photo_details_iv_photo.transitionName = safeArguments.imageTransitionName
        Glide.with(this)
            .load(safeArguments.photoUrl)
            .listener(photoTransitionRequestListener)
            .into(photo_details_iv_photo)
    }

    private fun startShimmer() {
        photo_details_loading_shimmer.toVisible()
        photo_details_loading_shimmer.startShimmer()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        restoreStatusBarsThemeColors()
    }

    private fun observe() {
        viewModel.photo.observe(viewLifecycleOwner, Observer { photo ->

            TransitionManager.beginDelayedTransition(photo_details_constraint_main, autoTransition)

            Glide.with(this)
                .load(photo.user.profileImage.small)
                .transform(CircleCrop())
                .transition(DrawableTransitionOptions.withCrossFade())
                .error(R.drawable.ic_person)
                .into(photo_details_author_img)

            photo_details_tv_photo_by.text = getString(R.string.collection_item_author_prefix, photo.user.name)
            photo_details_tv_description.text = photo.description
            photo_details_tv_likes.text = photo.likes.toString()
            photo_details_tv_total_downloads.text = photo.downloads.toString()

            photo_details_tv_description.goneIfEmpty()
            photo_details_constraint_bottom_panel.toVisible()
        })

        viewModel.error.observe(viewLifecycleOwner, Observer { error ->
            toast(getStringSafe(error.messageRes))
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { loading ->
            handleShimmer(loading)
        })

        viewModel.downloadDialog.observe(viewLifecycleOwner, Observer { dialogState ->
            if (dialogState.show) {
                downloadDialog.show(childFragmentManager, DownloadDialogFragment::class.java.simpleName)
            } else {
                downloadDialog.isDownloadFinished = !dialogState.isCanceled
                downloadDialog.dismiss()
            }
        })

        viewModel.downloadProgress.observe(viewLifecycleOwner, Observer { progress ->
            if (progress > 0 && downloadDialog.dialog?.isShowing == true) {
                downloadDialog.setProgress(progress)
            }
        })

        viewModel.toast.observe(viewLifecycleOwner, Observer { message ->
            message.messageRes?.let { toast(it) }
        })

        val overrideDialog = AlertDialog.Builder(activity).apply {
            setTitle(getString(R.string.message_dialog_error_title_photo_exists))
            setMessage(getString(R.string.message_dialog_photo_already_exists))
            setPositiveButton(getString(R.string.label_override)) { _, _ ->
                viewModel.overrideDownloadedPhoto()
            }
            setNegativeButton(getString(R.string.label_cancel), null)
        }.create()

        viewModel.overrideDialog.observe(viewLifecycleOwner, Observer { dialogState ->
            if (dialogState.show) {
                overrideDialog.show()
            } else {
                overrideDialog.dismiss()
            }
        })

        viewModel.wallpaper.observe(viewLifecycleOwner, Observer { uri ->
            setWallpaper(uri)
        })
    }

    private fun setWallpaper(uri: Uri) {
        try {
            Timber.d("Set wallpaper via WallpaperManager. Uri: $uri")
            val wallpaperManager = WallpaperManager.getInstance(this.context)
            wallpaperManager.getCropAndSetWallpaperIntent(uri)
                .apply {
                    setDataAndType(uri, "image/*")
                    putExtra("mimeType", "image/*")
                }.also {
                    startActivityForResult(it, 13451)
                }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Timber.d("Set wallpaper via Chooser. Uri: $uri")

            val intent = Intent(Intent.ACTION_ATTACH_DATA).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                setDataAndType(uri, "image/*")
                putExtra("mimeType", "image/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.titile_set_wallpaper_as)))
        }
    }

    private fun handleShimmer(loading: Boolean) {
        TransitionManager.beginDelayedTransition(photo_details_constraint_main, autoTransition)

        if (loading) {
            startShimmer()
        } else {
            stopShimmer()
        }
    }

    private fun disableToolbarTitle() {
        fragmentToolbar?.title = ""
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_photo_details, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.photo_details_share_menu_item -> consume { toast("Share") }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed(): Boolean {

        if (isMenuOpen) {
            closeMenu()
            return true
        }

        return false
    }

    private fun getEnterTransitionAnimation(): Transition {
        val imageTransition: Transition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
        imageTransition.duration = 250
        imageTransition.onTransitionEnd {

            val contentTransition = inflateTransition(R.transition.photo_details_transition_content_enter)
            contentTransition.onTransitionEnd {
                viewModel.fetchPhoto(safeArguments.photoId)
            }

            TransitionManager.beginDelayedTransition(photo_details_coordinator, contentTransition)

            photo_details_bottom_panel_background_overlay.toVisible()
            photo_details_floating_main.toVisible()
            fragmentToolbar?.toVisible()
        }
        return imageTransition
    }

    private fun restoreStatusBarsThemeColors() {
        activity?.let {
            it.window.statusBarColor = getThemeAttrColor(it, android.R.attr.statusBarColor)
            it.window.navigationBarColor = getThemeAttrColor(it, android.R.attr.navigationBarColor)
        }
    }

    private fun setTransparentStatusBars() {
        activity?.let {
            it.window.statusBarColor = Color.TRANSPARENT
            it.window.navigationBarColor = Color.TRANSPARENT
        }
    }

    private fun applyWindowsInsets() {
        photo_details_coordinator.setOnApplyWindowInsetsListener { _, windowInsets ->
            fragmentToolbar?.updatePadding(top = windowInsets.systemWindowInsetTop, bottom = 0)
            photo_details_constraint_bottom_panel.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = windowInsets.systemWindowInsetBottom }
            windowInsets.consumeSystemWindowInsets()
        }
    }

    // todo: create custom control
    private fun initFabs() {
        photo_details_floating_download.alpha = 0f
        photo_details_floating_raw.alpha = 0f
        photo_details_floating_wallpaper.alpha = 0f

        photo_details_floating_label_download.alpha = 0f
        photo_details_floating_label_raw.alpha = 0f
        photo_details_floating_label_wallpaper.alpha = 0f

        photo_details_floating_label_download.translationX = initialFabLabelTranslationX
        photo_details_floating_label_raw.translationX = initialFabLabelTranslationX
        photo_details_floating_label_wallpaper.translationX = initialFabLabelTranslationX

        photo_details_floating_download.translationY = initialFabTranslationY
        photo_details_floating_raw.translationY = initialFabTranslationY
        photo_details_floating_wallpaper.translationY = initialFabTranslationY

        photo_details_floating_download.toGone()
        photo_details_floating_raw.toGone()
        photo_details_floating_wallpaper.toGone()

        photo_details_floating_label_download.toGone()
        photo_details_floating_label_raw.toGone()
        photo_details_floating_label_wallpaper.toGone()

        val fabClickListener = View.OnClickListener {

            when (it.id) {
                R.id.photo_details_floating_download,
                R.id.photo_details_floating_label_download -> {
                    runWithPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                        viewModel.downloadImage()
                    }
                }
                R.id.photo_details_floating_raw,
                R.id.photo_details_floating_label_raw -> {
                    toast("Raw")
                }
                R.id.photo_details_floating_wallpaper,
                R.id.photo_details_floating_label_wallpaper -> {
                    runWithPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE, android.Manifest.permission.READ_EXTERNAL_STORAGE) {
                        viewModel.downloadWallpaper()
                    }
                }
            }

            closeMenu()
        }

        photo_details_floating_download.setOnClickListener(fabClickListener)
        photo_details_floating_raw.setOnClickListener(fabClickListener)
        photo_details_floating_wallpaper.setOnClickListener(fabClickListener)

        photo_details_floating_label_download.setOnClickListener(fabClickListener)
        photo_details_floating_label_raw.setOnClickListener(fabClickListener)
        photo_details_floating_label_wallpaper.setOnClickListener(fabClickListener)

        photo_details_floating_main.setOnClickListener {
            if (isMenuOpen) {
                closeMenu()
            } else {
                openMenu()
            }
        }

        fab_overlay.setOnClickListener { closeMenu() }
        fab_overlay.toGone()
    }

    private fun openMenu() {
        isMenuOpen = !isMenuOpen

        animateRotateMainFab(180f)
        animateFabMenuItemOpen(photo_details_floating_download, photo_details_floating_label_download)
        animateFabMenuItemOpen(photo_details_floating_raw, photo_details_floating_label_raw)
        animateFabMenuItemOpen(photo_details_floating_wallpaper, photo_details_floating_label_wallpaper)
        animateOverlayShow()
    }

    private fun closeMenu() {
        isMenuOpen = !isMenuOpen

        animateRotateMainFab(0f)
        animateFabMenuItemClose(photo_details_floating_download, photo_details_floating_label_download)
        animateFabMenuItemClose(photo_details_floating_raw, photo_details_floating_label_raw)
        animateFabMenuItemClose(photo_details_floating_wallpaper, photo_details_floating_label_wallpaper)
        animateOverlayHide()
    }

    private fun animateOverlayShow() {
        TransitionManager.beginDelayedTransition(photo_details_constraint_main)
        fab_overlay.toVisible()
    }

    private fun animateRotateMainFab(angle: Float) {
        photo_details_floating_main.animate().setInterpolator(interpolator).rotation(angle).setDuration(300).start()
    }

    private fun animateOverlayHide() {
        TransitionManager.beginDelayedTransition(photo_details_constraint_main)
        fab_overlay.toGone()
    }

    private fun animateFabMenuItemOpen(fab: FloatingActionButton, label: TextView) {
        fab.animate().withStartAction { fab.toVisible() }.translationY(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start()
        label.animate().withStartAction { label.toVisible() }.setStartDelay(0).translationX(0f).alpha(1f).setInterpolator(interpolator).setDuration(300).start()
    }

    private fun animateFabMenuItemClose(fab: FloatingActionButton, label: TextView) {
        fab.animate().withEndAction { fab.toGone() }.translationY(initialFabTranslationY).alpha(0f).setInterpolator(interpolator).setDuration(300).start()
        label.animate().withEndAction { label.toGone() }.translationX(initialFabLabelTranslationX).alpha(0f).setInterpolator(interpolator).setDuration(300).start()
    }

    private fun stopShimmer() {
        photo_details_loading_shimmer.toGone()
        photo_details_loading_shimmer.stopShimmer()
    }

    private val photoTransitionRequestListener = object : RequestListener<Drawable> {
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            startPostponedEnterTransition()
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            startPostponedEnterTransition()
            return false
        }
    }
}
