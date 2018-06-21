package me.ibrohim.alquran_um_majid.models

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase

import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper

/**
 * Created by ibrohim on 3/2/17.
 */

object Surat {

    var listNamaSurat: Array<String?>? = null
    var jumlahAyat: IntArray? = null
    var indeksAyatPertama: Set<Int>? = null


    val isInitialized: Boolean
        get() = listNamaSurat != null && jumlahAyat != null

    fun initialize(context: Context) {
        if (!isInitialized) {
            initializeSurat(context)
        }
    }

    fun initializeSurat(context: Context) {
        listNamaSurat = arrayOfNulls<String?>(114)
        jumlahAyat = IntArray(114)

        val Db = DatabaseHelper.getInstance(context).database
        val cursor = Db.rawQuery("SELECT * FROM surat", null)

        var indeks = 0
        while (cursor.moveToNext()) {
            listNamaSurat!![indeks] = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
            jumlahAyat!![indeks] = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah_ayat"))

            indeks++
        }
        cursor.close()
    }

    fun getNamaSurat(index: Int): String? {
        if (index >= 1 && index <= 114) {
            return listNamaSurat!![index - 1]
        } else {
            return "Unknown"
        }
    }

    fun getJumlahAyat(index: Int): Int {
        if (index >= 1 && index <= 114) {
            return jumlahAyat!![index - 1]
        } else {
            return 0
        }
    }

    class InvalidSuratException internal constructor() : Exception("1 <= indeks <= 114 tidak tepenuhi.")
}
