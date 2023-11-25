package com.example.deteksisayur

import Data.DPasar
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

interface PasarClickListener {
    fun onPasarClicked(pasar: DPasar)
}

class AdapterPasar(var mList: List<DPasar>, private val clickListener: PasarClickListener) :
    RecyclerView.Adapter<AdapterPasar.PasarViewHolder>() {

    inner class PasarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logo : ImageView = itemView.findViewById(R.id.logoIv)
        val titleTv : TextView = itemView.findViewById(R.id.titleTv)
    }

    fun setFilteredList(mList: List<DPasar>){
        this.mList = mList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PasarViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pasar , parent , false)
        return PasarViewHolder(view)
    }

    override fun onBindViewHolder(holder: PasarViewHolder, position: Int) {
        val pasar = mList[position]
        holder.logo.setImageResource(pasar.logo)
        holder.titleTv.text = pasar.title

        holder.itemView.setOnClickListener {
            clickListener.onPasarClicked(pasar)
        }
        val grafikData = pasar.grafikData

        grafikData.first.forEachIndexed { index, tanggal ->
            val persentase = grafikData.second[index]
            Log.d("Data Grafik", "Tanggal: $tanggal, Persentase: $persentase")
        }
    }

    fun getNamaPasarList(): List<String> {
        val namaPasarList = mutableListOf<String>()
        for (pasar in mList) {
            namaPasarList.add(pasar.title)
        }
        return namaPasarList
    }

    override fun getItemCount(): Int {
        return mList.size
    }
}