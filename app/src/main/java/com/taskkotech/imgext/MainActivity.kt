package com.taskkotech.imgext

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.taskkotech.imgext.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private companion object{
        private const val CAMERA_REQUEST_CODE=100
        private const val STORAGE_REQUEST_CODE=101
    }
    private var imageUri: Uri? =null
    private lateinit var cameraPermission: Array<String>
    private lateinit var storagePermission: Array<String>

    private lateinit var textRecognizer: TextRecognizer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //permission

       /* cameraPermission= arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermission= arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)*/

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.uploadBTN.setOnClickListener {
            showInputImage()
        }

       // recognizeTextFromImage()
    }

    private fun recognizeTextFromImage() {
        try {
            val inputImage= InputImage.fromFilePath(this,imageUri!!)
            val textTaskResult = textRecognizer.process(inputImage)
                .addOnSuccessListener {recognizedText->
                    val recognizeText =recognizedText.text

                    binding.recognizedETV.setText(recognizeText)
                }
                .addOnFailureListener { e->
                    showToast("Failed to recognize text due to ${e.message}")

                }
        }catch (e:Exception){
            showToast("Failed to prepare image due to ${e.message} ")
        }
    }

    private fun showInputImage() {
        val popupMenu = PopupMenu(this, binding.uploadBTN)
        popupMenu.menu.add(Menu.NONE,1,1,"CAMERA")
        popupMenu.menu.add(Menu.NONE,2,2,"GALLERY")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { menuItem->
            val id= menuItem.itemId
            if (id==1){
                picImageCamera()
                /*if (checkCameraPermission()){
                    picImageCamera()
                }else{
                    requestCameraPermission()
                }*/
            }
            else if (id==2){
                pickImageGallery()
                /*if (checkStoragePermission()){
                    pickImageGallery()
                }else{
                    requestStoragePermission()
                }*/
            }
            return@setOnMenuItemClickListener true
        }
    }

    private fun pickImageGallery(){
        val intent = Intent(Intent.ACTION_PICK)

        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val galleryActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data = result.data
            imageUri = data!!.data
            binding.image.setImageURI(imageUri)
            recognizeTextFromImage()
        }else{
            showToast("Cancelled...!")
        }
    }

    private fun picImageCamera(){
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncer.launch(intent)
    }

    private val cameraActivityResultLauncer =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if (result.resultCode == Activity.RESULT_OK){
                binding.image.setImageURI(imageUri)
                recognizeTextFromImage()
            }else{
                showToast("Cancelled...!")
            }
        }

   /* private fun checkStoragePermission(): Boolean{
        return ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) && PackageManager.PERMISSION_GRANTED
    }

    private fun checkCameraPermission(): Boolean{
        val cameraResult = ContextCompat.checkSelfPermission(this,Manifest.permission.CAMERA) && PackageManager.PERMISSION_GRANTED
        val storageResult = ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_EXTERNAL_STORAGE) && PackageManager.PERMISSION_GRANTED

        return cameraResult && storageResult
    }

    private fun requestStoragePermission(){
        ActivityCompat.requestPermissions(this,storagePermission, STORAGE_REQUEST_CODE)
    }

    private fun requestCameraPermission(){
        ActivityCompat.requestPermissions(this,cameraPermission, CAMERA_REQUEST_CODE)
    }*/

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    val cameraAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted =grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted){
                        picImageCamera()
                    }
                    else{
                        showToast("Camera & Storage permission are required..!")
                    }
                }
            }
            STORAGE_REQUEST_CODE->{
                if (grantResults.isNotEmpty()){
                    val storageAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted){
                        pickImageGallery()
                    }
                    else{
                        showToast("Storage permission are required..!")
                    }
                }
            }
        }
    }

    private fun showToast(message:String){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}