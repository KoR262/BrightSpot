package com.example.brightspot

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_photo.*
import kotlinx.android.synthetic.main.fragment_profile.*


class PhotoFragment : Fragment() {
    private lateinit var uDatabase : DatabaseReference

        override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_photo, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val urlTag = arguments?.get("urlTag")
        val userTag = arguments?.get("userTag")

        uDatabase = FirebaseDatabase.getInstance().getReference("Users/$userTag/nickName")
        uDatabase.get().addOnSuccessListener {
            photo_nickname.text = it.value.toString()
        }

        Glide.with(this)
            .load(urlTag)
            .into(photoIV)
    }
}