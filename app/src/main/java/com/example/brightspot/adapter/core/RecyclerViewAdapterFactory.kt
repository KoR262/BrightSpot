package com.example.visible_guitar.ui.adapter.core

import android.view.View
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.ListAdapter
import com.example.brightspot.adapter.Simple
import com.example.visible_guitar.ui.adapter.holder.OnItemClickListener
import com.example.visible_guitar.ui.adapter.holder.RecyclerViewHolder


class RecyclerViewAdapterFactory<T : Simple>
    : AdapterFactory<T, ListAdapter<T, RecyclerViewHolder<T>>> {
    override fun create(
        @LayoutRes layout: Int,
        viewHolder: (View) -> RecyclerViewHolder<T>,
        onItemClickListener: OnItemClickListener<T>?
    ): ListAdapter<T, RecyclerViewHolder<T>> {

        val viewAdapterFactory = object : ViewHolderFactory<T, RecyclerViewHolder<T>> {
            override fun create(view: View): RecyclerViewHolder<T> = viewHolder(view)
        }

        return RecyclerViewAdapter(layout, viewAdapterFactory, onItemClickListener)
    }


}