package edu.ucsb.cs.cs184.amandaserex.aserexgeopics

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.OutputStream


class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val edit: EditText = findViewById<EditText>(R.id.editText)
        val addButton: Button = findViewById<Button>(R.id.add)
        val cancelButton: Button = findViewById<Button>(R.id.cancelButton)
        val takePhoto: ImageView = findViewById<ImageView>(R.id.imageView2)

        val REQUEST_IMAGE_CAPTURE = 1

        fun dispatchTakePictureIntent(): Boolean {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            try {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            } catch (e: ActivityNotFoundException) {
                // display error state to the user
                return false
            }
            return true
        }


        takePhoto.setOnClickListener(View.OnClickListener() {
            dispatchTakePictureIntent()
        })


        cancelButton.setOnClickListener(View.OnClickListener {
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        })


        addButton.setOnClickListener(View.OnClickListener {
            MapsActivity.GlobalVars.comment = edit.text.toString()
            MapsActivity.GlobalVars.addNew = true
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val takePhoto: ImageView = findViewById<ImageView>(R.id.imageView2)

        fun createImageFile(){
            val drawable : BitmapDrawable = takePhoto.drawable as BitmapDrawable
            val bitmap : Bitmap = drawable.bitmap
            val filepath : File = Environment.getExternalStorageDirectory()
            val dir : File = File(filepath.toString() + "/ASerexGeoPics/")
            if(dir.mkdir()){
                Log.e("TAG", "Made directory")
            }
            else{
                Log.e("TAG", dir.exists().toString())
            }
            val imageName = System.currentTimeMillis().toString() + ".jpeg"
            val file : File = File(dir, imageName)
            MapsActivity.GlobalVars.image = file.toString()
            Log.e("DIRECTORY", dir.toString())
            Log.e("FILE", file.toString())
            try {
                val outputStream : OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
            } catch (e: FileNotFoundException){
                Log.e("TAG", "File Not Found")
                e.printStackTrace()
            }
        }


        if (resultCode == RESULT_OK) {
                val photo: Bitmap = data?.extras?.get("data") as Bitmap
                takePhoto.setImageBitmap(photo)
                createImageFile()
            }

        }


}