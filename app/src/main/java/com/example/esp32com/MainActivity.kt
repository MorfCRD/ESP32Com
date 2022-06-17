package com.example.esp32com

import android.graphics.Color
import android.graphics.Color.parseColor
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.esp32com.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    var strip = Strip("*",20,"000000",0,1000,0)

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)
        //setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val modesArray = resources.getStringArray(R.array.modesList)

        binding.spinnerMode.adapter = ArrayAdapter<String>(this,
            android.R.layout.simple_expandable_list_item_1,modesArray)

        binding.buttonApply.setOnClickListener{
            applyChanges()
        }

        binding.seekBarBrightness.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar_Brightness: SeekBar,
                                           progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar_Brightness: SeekBar) {}
            override fun onStopTrackingTouch(seekBar_Brightness: SeekBar) {
                val sb = StringBuilder()
                sb.append("Brightness: ")
                sb.append(seekBar_Brightness.progress)
                binding.textViewBrightness.text = sb
                strip.brightness = seekBar_Brightness.progress

            }
        })

        binding.seekBarSpeed.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar_Speed: SeekBar,
                progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(seekBar_Speed: SeekBar) {}
            override fun onStopTrackingTouch(seekBar_Speed: SeekBar) {
                val sb = StringBuilder()
                sb.append("Speed: ")
                sb.append(seekBar_Speed.progress)
                binding.textViewSpeed.text = sb
                strip.speed = seekBar_Speed.progress

            }
        })

        binding.spinnerMode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, position: Int, id: Long) {
                strip.mode = position
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        binding.textViewColorData.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //TODO("Not yet implemented")
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //TODO("Not yet implemented")
            }

            override fun afterTextChanged(s: Editable) {
                binding.SeekBarColorR.progress = Integer.parseInt(s.substring(0..1), 16)
                binding.SeekBarColorG.progress = Integer.parseInt(s.substring(2..3), 16)
                binding.SeekBarColorB.progress = Integer.parseInt(s.substring(4..5), 16)
            }
        })

        binding.SeekBarColorR.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                binding.textViewColorData.text = colorStr.replace("#","").uppercase(Locale.getDefault())
                binding.textViewColorData.setBackgroundColor(parseColor("#$colorStr"))
            }
        })
        binding.SeekBarColorG.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                binding.textViewColorData.text = colorStr.replace("#","").uppercase(Locale.getDefault())
                binding.textViewColorData.setBackgroundColor(parseColor("#$colorStr"))
            }
        })
        binding.SeekBarColorB.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onProgressChanged(seekBar: SeekBar, progress: Int,
                                           fromUser: Boolean) {
                val colorStr = getColorString()
                binding.textViewColorData.text = colorStr.replace("#","").uppercase(Locale.getDefault())
                binding.textViewColorData.setBackgroundColor(parseColor("#$colorStr"))
            }
        })

        readChanges()

        binding.switchOnOff.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener{
            override fun onCheckedChanged(p0: CompoundButton?, isChecked: Boolean) {
                if (isChecked){
                    val dataString = binding.textViewBrightness.text
                    strip.brightness = 50//(dataString.split(":")[1]).trim().toInt()
                } else {
                    strip.brightness = 0
                }
                //applyChanges()
            }

        })
    }

    private fun showToast(msg : String){
        Toast.makeText(applicationContext, "Read Error: $msg", Toast.LENGTH_LONG).show()
    }

    private fun readChanges() {
        val ref = FirebaseDatabase.getInstance().getReference("salon")

        val getData = object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                try {
                    strip.scmd = p0.child("scmd").value.toString()
                    binding.editTextCmd.setText(strip.scmd)
                } catch (exp : Exception) { showToast("scmd") }

                try {
                    val brightness = p0.child("brightness").value.toString()
                    strip.brightness = brightness.toInt()
                    binding.seekBarBrightness.progress = brightness.toInt()
                    val boolTmp = brightness.toInt()>0
                    binding.textViewBrightness.text = "Brightness: $brightness"
                    binding.switchOnOff.isChecked = boolTmp
                } catch (exp : Exception) {
                    showToast("Brightness")
                }

                try {
                    strip.color = p0.child("color").value.toString()
                    if (strip.color.isNotBlank()) {
                        var color = strip.color.uppercase(Locale.getDefault())
                        if (color.length < 6){
                            val cnt = 6 - color.length
                            for (i in 1..cnt)
                                color = "0$color"
                        }
                        binding.textViewColorData.text = color
                        binding.textViewColorData.setBackgroundColor(parseColor("#$color"))
                    }
                } catch (exp : Exception) {
                    showToast("color")
                }

                try {
                    val mode = p0.child("mode").value.toString()
                    strip.mode = mode.toInt()
                    binding.spinnerMode.setSelection(strip.mode)
                } catch (exp : Exception) { showToast("mode") }

                try {
                    val speed = p0.child("speed").value.toString()
                    strip.speed = speed.toInt()
                    binding.seekBarSpeed.progress = speed.toInt()
                } catch (exp : Exception) { showToast("speed") }

                try {
                    val update_req = p0.child("update_req").value.toString()
                    strip.update_req = update_req.toInt()
                } catch (exp : Exception) { showToast("update_req") }

                try {
                    val voltage = p0.child("vbat").value.toString()
                    "Voltage: $voltage".also { binding.textViewVoltage.text = it }
                    //Toast.makeText(applicationContext, "", Toast.LENGTH_SHORT).show()
                } catch (exp : Exception) { showToast("Voltage") }
            }
            override fun onCancelled(p0: DatabaseError) {
            }
        }

        ref.addValueEventListener(getData)
        ref.addListenerForSingleValueEvent(getData)
    }


    private fun applyChanges(){
        strip.scmd = binding.editTextCmd.text.toString().trim()

        val color = binding.textViewColorData.text.toString().trim()
        if(binding.textViewColorData.text.toString().isEmpty()){
            binding.textViewColorData.error = "Please enter CMD"
        } else {
            strip.color =  color
        }

        strip.update_req = 1

        val ref = FirebaseDatabase.getInstance().getReference("salon")

        ref.setValue(strip).addOnCompleteListener() {
            Toast.makeText(applicationContext, "Data Applied", Toast.LENGTH_SHORT).show()
        }
    }

    fun getColorString(): String {
        var r = Integer.toHexString(((255*binding.SeekBarColorR.progress)/binding.SeekBarColorR.max))
        if(r.length==1) r = "0$r"
        var g = Integer.toHexString(((255*binding.SeekBarColorG.progress)/binding.SeekBarColorG.max))
        if(g.length==1) g = "0$g"
        var b = Integer.toHexString(((255*binding.SeekBarColorB.progress)/binding.SeekBarColorB.max))
        if(b.length==1) b = "0$b"
        return r + g + b
    }

}