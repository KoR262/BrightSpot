package com.example.brightspot

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.brightspot.adapter.Simple
import com.example.visible_guitar.ui.adapter.createAdapterOf
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_routes.*


class RoutesFragment : Fragment() {

    private lateinit var rDatabase: DatabaseReference
    private lateinit var uDatabase : DatabaseReference

    private val routeAdapter by lazy {
        createAdapterOf(R.layout.route_item, ::RouteViewHolder, ::itemClickListener)
    }

    private fun itemClickListener(simple: Simple){
        findNavController().navigate(
            R.id.action_navigation_graph_routes_to_navigation_graph_map,
            bundleOf("routeID" to simple.id))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_routes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val listRoutes = mutableListOf<RouteSimple>()

        rDatabase = FirebaseDatabase.getInstance().getReference("Routes")
        rDatabase.get().addOnSuccessListener { documents ->
            for ((i,document) in documents.children.withIndex()) {
                val routeId = document.key.toString()
                val userId = document.child("userID").value
                val distance = document.child("distance").value.toString()
                uDatabase = FirebaseDatabase.getInstance().getReference("Users/$userId/nickName")
                uDatabase.get().addOnSuccessListener {
                    listRoutes.add(RouteSimple(routeId, it.value.toString(), distance.toInt()))

                    Log.d("RoutesFragment", "LastIndex: ${i}")
                    Log.d("RoutesFragment", "Count: ${((documents.children.count() - 1))}")
                    Log.d("RoutesFragment", "DocumentBool: ${i == (documents.children.count() - 1)}")
                    if (i == (documents.children.count() - 1)){
                        Log.d("RoutesFragment", "$listRoutes")
                        routeAdapter.submitList(listRoutes)
                        recyclerRoutesView.layoutManager = GridLayoutManager(requireContext(), 1, GridLayoutManager.VERTICAL, false)
                        recyclerRoutesView.adapter = routeAdapter
                    }
                }
            }
        }
    }

}