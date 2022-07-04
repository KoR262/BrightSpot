package com.example.visible_guitar.ui.adapter.core

import android.view.View
import com.example.brightspot.adapter.Simple
import com.example.visible_guitar.ui.adapter.holder.RecyclerViewHolder

interface ViewHolderFactory<T : Simple, VH : RecyclerViewHolder<T>> {
    fun create(view: View) : VH
}