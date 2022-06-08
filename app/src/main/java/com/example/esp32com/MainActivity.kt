package com.example.esp32com

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*


class MainActivity : AppCompatActivity() {

    lateinit var editText_scmd: EditText
    lateinit var textView_brightness : TextView
    lateinit var seekBar_Brightness : SeekBar
    lateinit var textView_color : TextView
    lateinit var SeekBarColorR : SeekBar
    lateinit var SeekBarColorG : SeekBar
    lateinit var SeekBarColorB : SeekBar
    lateinit var editText_color: EditText
    lateinit var textView_speed: TextView
    lateinit var seekBar_Speed : SeekBar
    lateinit var button_Apply: Button
    lateinit var spinner_mode : Spinner
    var strip = Strip("*",20,"000000",0,1000,0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        editText_scmd = findViewById(R.id.editText_scmd)
        textView_brightness = findViewById(R.id.textView_brightness)
        seekBar_Brightness = findViewById(R.id.seekBar_brightness)
        textView_color = findViewById(R.id.textView_color)
        SeekBarColorR = findViewById(R.id.SeekBarColorR)
        SeekBarColorG = findViewById(R.id.SeekBarColorG)
        SeekBarColorB = findViewById(R.id.SeekBarColorB)
        editText_color = findViewById(R.id.editText_color)
        spinner_mode = findViewById(R.id.spinner_mode)
        seekBar_Speed = findViewById(R.id.seekBar_speed)
        button_Apply = findViewById(R.id.button_Apply)
        textView_speed = findViewById(R.id.textView_speed)

        val modesArray = resources.getStringArray(R.array.modesList)

        spinner_mode.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1,modesArray)

        button_Apply.setOnClickListener{
            applyChanges()
        }

        seekBar_Brightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar_Brightness: SeekBar,
                                           progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar_Brightness: SeekBar) {}
            override fun onStopTrackingTouch(seekBar_Brightness: SeekBar) {
                var sb = StringBuilder()
                sb.append("Brightness: ")
                sb.append(seekBar_Brightness.progress)
                textView_brightness.setText(sb)
                strip.brightness = seekBar_Brightness.progress

            }
        })

        seekBar_Speed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar_Speed: SeekBar,
                progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar_Speed: SeekBar) {}
            override fun onStopTrackingTouch(seekBar_Speed: SeekBar) {
                var sb = StringBuilder()
                sb.append("Speed: ")
                sb.append(seekBar_Speed.progress)
                textView_speed.setText(sb)
                strip.speed = seekBar_Speed.progress

            }
        })

        spinner_mode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                strip.mode = position
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        editText_color.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: Editable) {
                val builder = StringBuilder()
                if (s.length < 6){
                    val cnt = 6 - s.length
                    for (i in 1..cnt)
                        builder.append("0")
                }
                builder.append(s)
                SeekBarColorR.progress = Integer.parseInt(builder.substring(0..1), 16)
                SeekBarColorG.progress = Integer.parseInt(builder.substring(2..3), 16)
                SeekBarColorB.progress = Integer.parseInt(builder.substring(4..5), 16)
            }
        })

        SeekBarColorR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                editText_color.setText(colorStr.replace("#","").uppercase(Locale.getDefault()))
            }
        })
        SeekBarColorG.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                editText_color.setText(colorStr.replace("#","").uppercase(Locale.getDefault()))
            }
        })
        SeekBarColorB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                editText_color.setText(colorStr.replace("#","").uppercase(Locale.getDefault()))
            }
        })


        readChanges()
    }

    private fun showToast(msg : String){
        Toast.makeText(applicationContext, "Read Error: $msg", Toast.LENGTH_LONG).show()
    }

    private fun readChanges() {
        val ref = FirebaseDatabase.getInstance().getReference("test")

        var getData = object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    strip.scmd = p0.child("scmd").value.toString()
                    editText_scmd.setText(strip.scmd)
                } catch (exp : Exception) { showToast("scmd") }

                try {
                    val brightness = p0.child("brightness").value.toString()
                    strip.brightness = brightness.toInt()
                    seekBar_Brightness.progress = brightness.toInt()
                } catch (exp : Exception) { showToast("brightness") }

                try {
                    strip.color = p0.child("color").value.toString()
                    if (!strip.color.isNullOrBlank())
                        editText_color.setText(strip.color.toString().uppercase(Locale.getDefault()))
                } catch (exp : Exception) { showToast("color") }

                try {
                    val mode = p0.child("mode").value.toString()
                    strip.mode = mode.toInt()
                    spinner_mode.setSelection(strip.mode)
                } catch (exp : Exception) { showToast("mode") }

                try {
                    val speed = p0.child("speed").value.toString()
                    strip.speed = speed.toInt()
                    seekBar_Speed.progress = speed.toInt()
                } catch (exp : Exception) { showToast("speed") }

                try {
                    val update_req = p0.child("update_req").value.toString()
                    strip.update_req = update_req.toInt()
                } catch (exp : Exception) { showToast("update_req") }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        }

        ref.addValueEventListener(getData)
        ref.addListenerForSingleValueEvent(getData)
    }


    private fun applyChanges(){
        strip.scmd = editText_scmd.text.toString().trim()

        val color = editText_color.text.toString().trim()
        if(editText_color.text.toString().isEmpty()){
            editText_color.error = "Please enter CMD"
        } else {
            strip.color =  color
        }

        strip.update_req = 1

        val ref = FirebaseDatabase.getInstance().getReference("test")

        ref.setValue(strip).addOnCompleteListener() {
            Toast.makeText(applicationContext, "Data Applied", Toast.LENGTH_SHORT).show()
        }
    }

    fun getColorString(): String {
        var r = Integer.toHexString(((255*SeekBarColorR.progress)/SeekBarColorR.max))
        if(r.length==1) r = "0"+r
        var g = Integer.toHexString(((255*SeekBarColorG.progress)/SeekBarColorG.max))
        if(g.length==1) g = "0"+g
        var b = Integer.toHexString(((255*SeekBarColorB.progress)/SeekBarColorB.max))
        if(b.length==1) b = "0"+b
        return r + g + b
    }

}