package com.example.visible_guitar.ui.adapter.holder

import android.view.View
import androidx.recyclerview.widget.RecyclerView

typealias OnItemClickListener<T> = (T) -> Unit

abstract class RecyclerViewHolder<T>(view: View) : RecyclerView.ViewHolder(view) {

    protected open fun bind(item: T) = Unit

    fun bind(
        item: T,
        listener: OnItemClickListener<T>? = null
    ) = with(itemView) {
        bind(item)
        setOnClickListener {
            listener?.let { it(item) }
        }
    }
}