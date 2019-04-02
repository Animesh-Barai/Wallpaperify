package com.netchar.wallpaperify.ui.home

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.netchar.wallpaperify.R
import com.netchar.wallpaperify.data.models.Resource
import com.netchar.wallpaperify.data.remote.dto.Photo
import com.netchar.wallpaperify.di.factories.ViewModelFactory
import com.netchar.wallpaperify.infrastructure.extensions.injectViewModel
import com.netchar.wallpaperify.ui.base.BaseFragment
import com.netchar.wallpaperify.ui.base.GenericAdapter
import kotlinx.android.synthetic.main.fragment_main.*
import kotlinx.android.synthetic.main.raw_photo.view.*
import javax.inject.Inject

class MainFragment : BaseFragment() {

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    private lateinit var viewModel: MainViewModel

    override val layoutResId: Int = R.layout.fragment_main

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        main_recycler.setHasFixedSize(true)
        main_recycler.adapter = adapter
    }

    private val adapter by lazy {
        PhotoAdapter(Glide.with(this))
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = injectViewModel(viewModelFactory)
        viewModel.photos.observe(viewLifecycleOwner, Observer { response ->
            when (response) {
                is Resource.Success -> {
                    adapter.setItems(response.data)
                }
                is Resource.Loading -> {
                    Toast.makeText(this.context, response.isLoading.toString(), Toast.LENGTH_LONG).show()
                }
                is Resource.Error -> {
                    Toast.makeText(this.context, response.message, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_main_refresh) {
            viewModel.requestPhotos()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed(): Boolean {
        viewModel.cancelFetchingPhotos()
        return true
    }

    class PhotoAdapter(private val glide: RequestManager) : GenericAdapter<Photo>() {
        override fun getLayoutId(position: Int, obj: Photo): Int = R.layout.raw_photo

        override fun getViewHolder(view: View, viewType: Int): androidx.recyclerview.widget.RecyclerView.ViewHolder = ViewHolder(glide, view)

        class ViewHolder(private val glide: RequestManager, view: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(view), GenericAdapter.Binder<Photo> {
            override fun bind(data: Photo) {

                glide
                    .load(data.urls.regular)
                    .centerCrop()
                    .thumbnail(Glide.with(itemView).load(data.urls.thumb))
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(itemView.row_photo_iv)
            }
        }
    }
}
