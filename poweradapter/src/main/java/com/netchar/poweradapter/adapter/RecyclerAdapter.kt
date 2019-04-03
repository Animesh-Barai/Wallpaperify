package com.netchar.poweradapter.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Created by Netchar on 02.04.2019.
 * e.glushankov@gmail.com
 */

class RecyclerAdapter(private val dataSource: RecyclerDataSource) : RecyclerView.Adapter<RecyclerViewHolder>() {

    init {
        dataSource.attach(this)
        setHasStableIds(true)
    }

    override fun getItemViewType(position: Int) : Int {
        return dataSource.getLayoutResFor(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerViewHolder {
        return RecyclerViewHolder(parent, dataSource.rendererForType(viewType))
    }

    override fun getItemCount() = dataSource.getCount()

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) = holder.bind(dataSource.getItem(position))

    override fun getItemId(position: Int) = dataSource.getItem(position).getId()
}
