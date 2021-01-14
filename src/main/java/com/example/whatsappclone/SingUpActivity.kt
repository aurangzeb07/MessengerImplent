package com.example.whatsappclone

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_sing_up.*
import java.util.jar.Manifest
import kotlin.coroutines.Continuation

class SingUpActivity : AppCompatActivity() {

    val storage by lazy {
        FirebaseStorage.getInstance()
    }
    val auth by lazy {
        FirebaseAuth.getInstance()
    }
    val database by lazy {
        FirebaseFirestore.getInstance()
    }
    lateinit var downloadUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing_up)

        profilePicture.setOnClickListener {
            checkPermissionForImage()
            // Firebase Extension - Image Thumbnail.
        }

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            val name: String = nameEt.text.toString()
            if(name.isEmpty()) {
                Toast.makeText(this ,  "Name cannot be empty" , Toast.LENGTH_SHORT).show()
            } else if (!::downloadUrl.isInitialized) {
                Toast.makeText(this , "Image cannot be empty" , Toast.LENGTH_SHORT).show()
            } else  {
                val user = User(name , downloadUrl, downloadUrl ,auth.uid!!)
                database.collection("users").document(auth.uid!!).set(user).addOnSuccessListener {

                    val intent = Intent(this , MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }.addOnFailureListener {
                    Toast.makeText(this , "Error occured" , Toast.LENGTH_SHORT).show()
                    nextBtn.isEnabled = true
                }
            }
        }
    }

// Checking whether permission is given or not to the application.

    private fun checkPermissionForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
                && (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED)
            ) {
                val permission = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                val permissionWrite = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

                requestPermissions(
                    permission,
                    1001
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_READ LIKE 1001
                requestPermissions(
                    permissionWrite,
                    1002
                ) // GIVE AN INTEGER VALUE FOR PERMISSION_CODE_WRITE LIKE 1002
            } else {
                pickImageFromGallery()
            }
        } else {
            // system OS is < (less than) Marshmallow.
            pickImageFromGallery()
        }
    }

    // Intent to Open Gallery to Select the Photos.
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(
            intent,
            1000               // We can use this in map and location part as well.
        )
    }

    // handling Permissions results.
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1001 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from popup granted
                    pickImageFromGallery()
                } else {
                    // permission denied from popup
                    Toast.makeText(
                        this,
                        "Permission Denied For Reading External Storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            1002 -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission from popup granted
                    pickImageFromGallery()
                } else {
                    // permission denied from popup
                    Toast.makeText(
                        this,
                        "Permission Denied For Writing External Storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        }
    }

    // handling Image pic Result.
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == 1000) {
            data?.data?.let {
                profilePicture.setImageURI(it)
                uploadImage(it)
            }


        }
    }

    private fun uploadImage(it: Uri) {
        nextBtn.isEnabled = false
        val ref: StorageReference =
            storage.reference.child("uploads/" + auth.uid.toString())   // used uid to upload image(it is the best way to upload and  get the image.)
        val uploadTask: UploadTask = ref.putFile(it)
        uploadTask.continueWithTask(com.google.android.gms.tasks.Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            return@Continuation ref.downloadUrl
        }).addOnCompleteListener { task ->
            nextBtn.isEnabled = true
            if (task.isSuccessful) {
                downloadUrl = task.result.toString()
                Log.i("URL" , "downloadUrl: $downloadUrl")
            } else {

                // Handle failures
            }
        }.addOnFailureListener {

        }
    }
}
