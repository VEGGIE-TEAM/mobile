package com.example.deteksisayur

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import java.io.*

class DetailPasar : AppCompatActivity() {

    private lateinit var lineChart: LineChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_pasar)

        val pasarTitle = intent.getStringExtra("PASAR_TITLE")
        val pasarTitleTextView = findViewById<TextView>(R.id.pasarTitle)
        pasarTitleTextView.text = pasarTitle

        val pasarImage = intent.getIntExtra("PASAR_IMAGE", 0)
        val pasarImageView = findViewById<ImageView>(R.id.pasarImage)
        pasarImageView.setImageResource(pasarImage)

        lineChart = findViewById(R.id.lineChart)

        val grafikData = intent.getSerializableExtra("PASAR_GRAPH_DATA") as Pair<Array<String>, Array<String>>

        val tanggalInput = grafikData.first

        val mingguKe = HashMap<String, Int>()
        var minggu = 1

        for (tanggal in tanggalInput) {
            if (!mingguKe.containsKey(tanggal)) {
                mingguKe[tanggal] = minggu
                minggu++
            }
        }

        val labelMinggu = ArrayList<String>()

        for (tanggal in tanggalInput) {
            val mingguKeberapa = mingguKe[tanggal]
            if (mingguKeberapa != null) {
                labelMinggu.add("Minggu $mingguKeberapa")
            }
        }

        val percent = grafikData.second

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()

        for (i in tanggalInput.indices) {
            val freshVegetablePercentage = percent[i].replace(",", ".").toFloatOrNull()
            if (freshVegetablePercentage != null) {
                val entry = Entry(i.toFloat(), freshVegetablePercentage)
                entries.add(entry)

                val date = labelMinggu[i]
                labels.add(date)
            }
        }

        val dataSet = LineDataSet(entries, "Persentase Kesegaran Sayur Pada $pasarTitle")
        dataSet.apply {
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawFilled(true)
            fillColor = Color.BLUE
            color = Color.BLUE
            lineWidth = 2f
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis: XAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        val leftAxis = lineChart.axisLeft
        leftAxis.axisMinimum = 0f

        lineChart.animateX(1000)
        lineChart.invalidate()
    }
}
