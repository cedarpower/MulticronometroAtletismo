package com.example.multicronometroatletismo

import android.os.Bundle
import android.os.SystemClock
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {
    private val distanciaSerie = 400.0 // metros, puedes cambiar
    private val cronometros = mutableListOf<Cronometro>()
    private lateinit var container: LinearLayout
    private val numCronos = 12

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        container = findViewById(R.id.crono_container)

        for(i in 1..numCronos){
            val crono = Cronometro(this, distanciaSerie, "Crono $i")
            cronometros.add(crono)
            container.addView(crono.view)
        }

        val exportBtn: Button = findViewById(R.id.export_button)
        exportBtn.setOnClickListener {
            exportarCSV()
        }
    }

    private fun exportarCSV() {
        val fileName = "resultados_multicronometro.csv"
        val file = File(getExternalFilesDir(null), fileName)
        FileOutputStream(file).bufferedWriter().use { out ->
            out.write("Cronómetro;Lap;Tiempo (ms);Ritmo (min/km)\\n")
            cronometros.forEachIndexed { idx, crono ->
                crono.laps.forEachIndexed { lapIdx, lap ->
                    out.write("\${crono.nombre};\${lapIdx+1};\${lap.tiempoLap};\${lap.ritmo}\\n")
                }
            }
        }
        Toast.makeText(this, "Exportado a \$file", Toast.LENGTH_LONG).show()
    }
}
