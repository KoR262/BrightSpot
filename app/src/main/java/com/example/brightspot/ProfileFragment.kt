package com.example.brightspot

import android.media.Image
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {
    private var userId: String? = null
    private lateinit var uDatabase : DatabaseReference
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var pDatabase: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = FirebaseAuth.getInstance().currentUser?.uid
        uDatabase = FirebaseDatabase.getInstance().getReference("Users/$userId/nickName")
        uDatabase.get().addOnSuccessListener {
            profile_nickname.text = it.value.toString()
        }

        pDatabase = FirebaseDatabase.getInstance().getReference("Photo")
        pDatabase.get().addOnSuccessListener { documents ->
            var photoList = mutableListOf<PhotoSimple>()
            for (document in documents.children){
                if (document.child("userId").value == userId){
                    Log.d("MainActivity", "Зашел внутрь")
                    photoList.add(PhotoSimple(document.child("photoUrl").value.toString()))
                }
            }
            Log.d("MainActivity", "Photo List: $photoList")
            val imageAdapter = ImageAdapter()
            imageAdapter.submitList(photoList)
            recyclerImageView.layoutManager = GridLayoutManager(requireContext(), 2, GridLayoutManager.VERTICAL, false)
            recyclerImageView.adapter = imageAdapter
        }
    }
}