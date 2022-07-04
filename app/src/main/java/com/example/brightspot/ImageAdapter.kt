package com.example.brightspot

import android.content.Context
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.image_item.view.*

class ImageAdapter () : ListAdapter<PhotoSimple, ImageAdapter.ImageViewHolder> (RecyclerViewItemCallback()){



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        return ImageViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.image_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val photo = getItem(position)
        holder.bind(photo)
    }
//
//    override fun getItemCount(): Int {
//        return photoList.size
//    }
//
    class ImageViewHolder(private val itemview: View) : RecyclerView.ViewHolder(itemview){

        fun bind(
            item: PhotoSimple
        ){
            val photoImage : ImageView = itemview.findViewById(R.id.recycler_item)
            Glide.with(itemView.context)
                .load(item.photoUrl)
                .into(photoImage)
        }
    }

    class RecyclerViewItemCallback: DiffUtil.ItemCallback<PhotoSimple>(){
        override fun areItemsTheSame(oldItem: PhotoSimple, newItem: PhotoSimple): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: PhotoSimple, newItem: PhotoSimple): Boolean {
            return oldItem == newItem
        }

    }
}