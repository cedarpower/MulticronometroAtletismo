package com.example.multicronometroatletismo

import android.content.Context
import android.os.SystemClock
import android.view.Gravity
import android.view.View
import android.widget.*
import kotlin.math.roundToInt

data class Lap(val tiempoLap: Long, val ritmo: String)

class Cronometro(context: Context, private val distanciaSerie: Double, var nombre: String) {
    val laps = mutableListOf<Lap>()
    val view: View = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(10, 10, 10, 10)

        val label = EditText(context).apply {
            setText(nombre)
            setSingleLine()
            setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) nombre = text.toString()
            }
        }
        addView(label)

        val tiempoText = TextView(context).apply { text = "00:00.000" }
        addView(tiempoText)

        val lapText = TextView(context).apply { text = "Última vuelta: --" }
        addView(lapText)

        val btnContainer = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER

            val startBtn = Button(context).apply { text = "Iniciar" }
            val pauseBtn = Button(context).apply { text = "Pausar" }
            val lapBtn = Button(context).apply { text = "Vuelta" }
            val resetBtn = Button(context).apply { text = "Reiniciar" }

            addView(startBtn)
            addView(pauseBtn)
            addView(lapBtn)
            addView(resetBtn)
        }
        addView(btnContainer)

        var running = false
        var baseTime = 0L
        var pauseOffset = 0L

        val updateRunnable = object : Runnable {
            override fun run() {
                if (running) {
                    val elapsed = SystemClock.elapsedRealtime() - baseTime + pauseOffset
                    tiempoText.text = formatoTiempo(elapsed)
                    tiempoText.postDelayed(this, 50)
                }
            }
        }

        startBtn.setOnClickListener {
            if (!running) {
                baseTime = SystemClock.elapsedRealtime()
                running = true
                tiempoText.post(updateRunnable)
            }
        }

        pauseBtn.setOnClickListener {
            if (running) {
                pauseOffset += SystemClock.elapsedRealtime() - baseTime
                running = false
            }
        }

        lapBtn.setOnClickListener {
            if (running) {
                val elapsed = SystemClock.elapsedRealtime() - baseTime + pauseOffset
                val lapTime = if (laps.isEmpty()) elapsed else elapsed - laps.sumOf { it.tiempoLap }
                val ritmo = calculaRitmo(lapTime)
                laps.add(Lap(lapTime, ritmo))
                lapText.text = "Última vuelta: \${formatoTiempo(lapTime)} / Ritmo: \$ritmo"
            }
        }

        resetBtn.setOnClickListener {
            running = false
            baseTime = 0L
            pauseOffset = 0L
            tiempoText.text = "00:00.000"
            lapText.text = "Última vuelta: --"
            laps.clear()
        }
    }

    private fun formatoTiempo(millis: Long): String {
        val minutes = millis / 60000
        val seconds = (millis % 60000) / 1000
        val ms = millis % 1000
        return "%02d:%02d.%03d".format(minutes, seconds, ms)
    }

    private fun calculaRitmo(tiempoLap: Long): String {
        val minutos = tiempoLap.toDouble() / 60000.0
        val ritmoKm = minutos / (distanciaSerie / 1000.0)
        val min = ritmoKm.toInt()
        val seg = ((ritmoKm - min) * 60).roundToInt()
        return "%d'%02d\"/km".format(min, seg)
    }
}
