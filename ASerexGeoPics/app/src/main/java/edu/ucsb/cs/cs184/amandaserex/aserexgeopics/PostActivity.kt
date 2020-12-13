package edu.ucsb.cs.cs184.amandaserex.aserexgeopics

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class PostActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)

        val edit: EditText = findViewById<EditText>(R.id.editText)
        val addButton: Button = findViewById<Button>(R.id.add)


        addButton.setOnClickListener(View.OnClickListener {
            //need to add photo
            MapsActivity.GlobalVars.comment = edit.text.toString()
            MapsActivity.GlobalVars.addNew = true
            val i = Intent(this, MapsActivity::class.java)
            startActivity(i)
        })
    }


}