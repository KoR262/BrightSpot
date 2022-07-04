package com.example.visible_guitar.ui.adapter.core

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.brightspot.adapter.Simple
import com.example.visible_guitar.ui.adapter.holder.OnItemClickListener
import com.example.visible_guitar.ui.adapter.holder.RecyclerViewHolder

class RecyclerViewAdapter<T : Simple, VH : RecyclerViewHolder<T>>(
    @LayoutRes private val layout: Int,
    private val viewHolderFactory: ViewHolderFactory<T, VH>,
    private val onItemClickListener: OnItemClickListener<T>?,
    diffUtilItemCallback: DiffUtil.ItemCallback<T> = RecyclerViewItemCallback()
    ) : ListAdapter<T, VH>(diffUtilItemCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(layout, parent, false)
        return viewHolderFactory.create(itemView)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = getItem(position)
        holder.bind(item, onItemClickListener)
    }

}