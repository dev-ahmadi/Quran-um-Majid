package id.ahmadiyah.quran.models

import android.content.Context

import id.ahmadiyah.quran.infrastucure.DatabaseHelper
import java.util.*

/**
 * Created by ibrohim on 3/2/17.
 */

object Surat {

    fun getListNamaSurat(context: Context): List<String> {
        val cursor = DatabaseHelper.getInstance(context).readableDatabase.rawQuery("SELECT nama FROM surat", arrayOf())
        val result = LinkedList<String>()
        if (cursor.moveToFirst()) {
            do {
                result.add(cursor.getString(cursor.getColumnIndexOrThrow("nama")))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return result
    }

    fun getNamaSurat(context: Context, id: Int): String {
        val cursor = DatabaseHelper.getInstance(context).readableDatabase.rawQuery("SELECT nama FROM surat WHERE _id=?", arrayOf(id.toString()))
        cursor.moveToFirst()
        val result = cursor.getString(cursor.getColumnIndexOrThrow("nama"))
        cursor.close()
        return result
    }

    fun getJumlahAyat(context: Context, id: Int): Int {
        val cursor = DatabaseHelper.getInstance(context).readableDatabase.rawQuery("SELECT jumlah_ayat FROM surat WHERE _id=?", arrayOf(id.toString()))
        cursor.moveToFirst()
        val result = cursor.getInt(cursor.getColumnIndexOrThrow("jumlah_ayat"))
        cursor.close()
        return result
    }

    class InvalidSuratException internal constructor() : Exception("1 <= indeks <= 114 tidak tepenuhi.")
}
