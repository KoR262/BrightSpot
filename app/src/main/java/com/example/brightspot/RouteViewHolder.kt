package com.example.brightspot

import android.view.View
import android.widget.TextView
import com.example.visible_guitar.ui.adapter.holder.RecyclerViewHolder

class RouteViewHolder(private val view: View) : RecyclerViewHolder<RouteSimple>(view) {
    override fun bind(item: RouteSimple) {
        val author = view.findViewById<TextView>(R.id.route_author)
        author.text = item.userID
        val distance = view.findViewById<TextView>(R.id.route_distance)
        distance.text = item.distance.toString() + " м."
    }
}