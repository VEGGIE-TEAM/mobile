package com.example.deteksisayur

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class SQLITE(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "CREATE TABLE $TABLE_NAME " +
                "($COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_IMAGE BLOB, " +
                "$COL_DETECTION_RESULT TEXT)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "DeteksiSayurDB"
        const val TABLE_NAME = "simpan_local"
        const val COL_ID = "id"
        const val COL_IMAGE = "gambar"
        const val COL_DETECTION_RESULT = "hasil_detect"
    }
}