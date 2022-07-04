package com.example.visible_guitar.ui.adapter.core

import androidx.recyclerview.widget.DiffUtil
import com.example.brightspot.adapter.Simple

class RecyclerViewItemCallback<T : Simple> : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T) =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: T, newItem: T) =
        oldItem.equals(newItem)
}