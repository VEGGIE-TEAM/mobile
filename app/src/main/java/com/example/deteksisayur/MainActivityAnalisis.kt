package com.example.deteksisayur

import Data.DPasar
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.*

class MainActivityAnalisis : AppCompatActivity(), PasarClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: SearchView
    private var mList = ArrayList<DPasar>()
    private lateinit var adapter: AdapterPasar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_analisis)

        recyclerView = findViewById(R.id.recyclerView)
        searchView = findViewById(R.id.searchView)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)
        addDataToList()
        adapter = AdapterPasar(mList, this)
        recyclerView.adapter = adapter

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }

        })
    }

    override fun onPasarClicked(pasar: DPasar) {
        val intent = Intent(this, DetailPasar::class.java)
        intent.putExtra("PASAR_TITLE", pasar.title)
        intent.putExtra("PASAR_IMAGE", pasar.logo)
        intent.putExtra("PASAR_GRAPH_DATA", pasar.grafikData)
        startActivity(intent)
    }

    private fun filterList(query: String?) {

        if (query != null) {
            val filteredList = ArrayList<DPasar>()
            for (i in mList) {
                if (i.title.lowercase(Locale.ROOT).contains(query)) {
                    filteredList.add(i)
                }
            }

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No Data found", Toast.LENGTH_SHORT).show()
            } else {
                adapter.setFilteredList(filteredList)
            }
        }
    }

    private fun addDataToList() {
        val dataPasar1 = DPasar(
            "Pasar Soponyono",
            R.drawable.soponyono,
            Pair(
                arrayOf("15", "22", "29", "5"),
                arrayOf("42.85", "71.42", "71.42", "85.71")
            )
        )
        val dataPasar2 = DPasar(
            "Pasar Wadung Asri",
            R.drawable.wadung_asri,
            Pair(
                arrayOf("15", "22", "29", "5"),
                arrayOf("62.50", "55.55", "87.50", "75.0")
            )
        )
        val dataPasar3 = DPasar(
            "Pasar Rungkut Kidul",
            R.drawable.rungkut,
            Pair(
                arrayOf("15", "22", "29", "5"),
                arrayOf("83.33", "66.66", "83.33", "100.0")
            )
        )

        mList.add(dataPasar1)
        mList.add(dataPasar2)
        mList.add(dataPasar3)
    }
}