package com.example.brightspot

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_main.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private var userId: String? = null
    private lateinit var photoFile: File
    private val CAMERA_REQUEST_CODE = 1
    private lateinit var pDatabase : DatabaseReference
    private lateinit var lastLocation: Location
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val LOCATION_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navController = findNavController(navigation_fragment.id)
        bottomNavigationView.setupWithNavController(navController)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        userId = intent.getStringExtra("user_id")
        Log.d("MainActivity", "uid: $userId")

        bottomNavigationView.setOnItemSelectedListener { item ->
            when {
                navController.currentDestination?.id == item.itemId -> {
                    false
                }
                item.itemId == R.id.navigation_graph_take_photo -> {
                    cameraCheckPermission()
                    false
                }
                else -> {
                    val handled = NavigationUI.onNavDestinationSelected(item, navController)
                    Log.d("MainActivity", "$item")
                    handled
                }
            }
        }
    }

    private fun setUpMap(uri: Uri) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE
            )
            return
        }
        fusedLocationClient.lastLocation?.addOnSuccessListener(this) { location ->
            if(location != null){
                lastLocation = location

                val photo = Photo(
                    userId,
                    uri.toString(),
                    LatLng(lastLocation.latitude, lastLocation.longitude)
                )
                val uuid = UUID.randomUUID().toString()
                pDatabase.child(uuid).setValue(photo).addOnSuccessListener {
                    Toast.makeText(this, "Запись добавлена", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "Не добавлена", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun cameraCheckPermission(){
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA).withListener(
                object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                        report?.let {
                            if (report.areAllPermissionsGranted()){
                                camera()
                            }
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showRotationalDialogForPermission()
                    }

                }
            ).onSameThread().check()
    }

    private fun camera(){
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if(intent.resolveActivity(packageManager) != null){
            try {
                photoFile = createImageFile()
            }catch (e: IOException){}
            if (photoFile != null){
                val fileProvider = FileProvider.getUriForFile(
                    this,
                    "com.example.android.fileprovider",
                    photoFile
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)
                startActivityForResult(intent, CAMERA_REQUEST_CODE)
            }
        }else{
            Log.d("MainActivity", "intent.resolveActivity null")
        }
    }

    private fun createImageFile(): File {
        val fileName = createFileName()
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            fileName,
            ".jpg",
            storageDir
        )
        return image
    }

    private fun createFileName(): String {
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.getDefault())
        val now = Date()
        return formatter.format(now)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                CAMERA_REQUEST_CODE -> {
//                    val bitmap = data?.extras?.get("data") as Bitmap

//                    val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
//                    val stream = ByteArrayOutputStream()
//                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
//                    val image = stream.toByteArray()

                    val fileName = createFileName()
                    val storageReference =
                        FirebaseStorage.getInstance().getReference("photo/$fileName")

                    storageReference.putFile(Uri.fromFile(photoFile)).addOnSuccessListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Фотография загружена",
                            Toast.LENGTH_SHORT
                        ).show()
                        storageReference.downloadUrl.addOnSuccessListener {
                            Log.d("MainActivity", "downloadurl")
                            pDatabase = FirebaseDatabase.getInstance().getReference("Photo")
                            Log.d("MainActivity", "pDatabase")
                            setUpMap(it)


                            Log.d("MainActivity", "File location: $it")
                        }.addOnFailureListener {
                            Log.d("MainActivity", "$it")
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            this@MainActivity,
                            "Фотография не загружена",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    //setUpMap()
                }
            }
        }
    }

    private fun showRotationalDialogForPermission(){
        AlertDialog.Builder(this)
            .setMessage("Требуются разрешения на доступ к камере и хранилищу")
            .setPositiveButton("Перейти в настройки"){_,_->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
                catch (e: ActivityNotFoundException){
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Выйти"){dialog,_->
                dialog.dismiss()
            }.show()
    }
}