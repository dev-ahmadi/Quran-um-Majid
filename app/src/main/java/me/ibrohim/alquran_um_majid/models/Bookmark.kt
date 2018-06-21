package me.ibrohim.alquran_um_majid.models

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.util.Log

import me.ibrohim.alquran_um_majid.activities.FragmentDaftarBookmark
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper

/**
 * Created by ibrohim on 3/2/17.
 */

class Bookmark(private val mContext: Context, private val mId: Int, private val mType: BookmarkType) {
    enum class BookmarkType {
        USER_DEFINED, RECENTLY
    }

    var posisi: Posisi? = null

    fun hapus() {
        val Db = DatabaseHelper.getInstance(mContext).database
        Db.execSQL("DELETE FROM `bookmark` WHERE _id = ?", arrayOf(Integer.toString(mId)))
    }

    fun save() {
        if (mType == BookmarkType.USER_DEFINED) {
            saveUserDefinedBookmark()
        } else {
            saveRecentBookmark()
        }
    }

    private fun saveUserDefinedBookmark() {
        val Db = DatabaseHelper.getInstance(mContext).database
        Db.execSQL("INSERT INTO `bookmark` (`surat`, `ayat`, `terakhir_dibuka`, `frekwensi`) VALUES (?,?,0,0)", arrayOf(Integer.toString(posisi!!.surat), Integer.toString(posisi!!.ayat)))
    }

    private fun saveRecentBookmark() {
        val Db = DatabaseHelper.getInstance(mContext).database

        val isNew = mId == BOOKMARK_NEW_ID
        var _id = mId

        if (isNew) {
            _id = leastFrequentlyRecentlyUse
            Log.d("LFRU", "new id: " + _id)
        }

        if (!isNew) {
            Db.execSQL("UPDATE `bookmark` SET `surat` = ?, `ayat` = ?, `terakhir_dibuka`=datetime('now','localtime'), `frekwensi`= `frekwensi`+1 WHERE _id = ?", arrayOf(Integer.toString(posisi!!.surat), Integer.toString(posisi!!.ayat), Integer.toString(_id)))
        } else {
            Db.execSQL("INSERT OR REPLACE INTO `bookmark` (`_id`, `surat`, `ayat`, `terakhir_dibuka`, `frekwensi`) VALUES (?,?,?,datetime('now','localtime'),1)", arrayOf(Integer.toString(_id), Integer.toString(posisi!!.surat), Integer.toString(posisi!!.ayat)))
        }
    }

    private val leastFrequentlyRecentlyUse: Int
        get() {
            val Db = DatabaseHelper.getInstance(mContext).database

            var cursor = Db.rawQuery("SELECT _id FROM `bookmark` WHERE _id <= ? " + "ORDER BY `frekwensi` ASC, `terakhir_dibuka` ASC ", arrayOf(Integer.toString(FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID)))

            if (cursor.count < FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID) {

                cursor.close()
                cursor = Db.rawQuery("WITH special_id (_id, maxid)\n" +
                        "AS (SELECT 1 AS _id, ? UNION ALL SELECT _id + 1, maxid FROM special_id WHERE _id < maxid)\n" +
                        "SELECT _id FROM special_id WHERE _id NOT IN (SELECT _id FROM bookmark) LIMIT 1", arrayOf(Integer.toString(FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID)))

            }

            cursor.moveToFirst()
            val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
            cursor.close()

            return id
        }

    companion object {

        val BOOKMARK_NEW_ID = FragmentDaftarBookmark.UNDEFINED_SESSION_ID
    }
}
