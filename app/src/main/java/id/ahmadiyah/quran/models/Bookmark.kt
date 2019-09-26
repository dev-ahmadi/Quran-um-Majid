package id.ahmadiyah.quran.models

import android.content.Context
import android.util.Log

import id.ahmadiyah.quran.activities.FragmentDaftarBookmark
import id.ahmadiyah.quran.infrastucure.DatabaseHelper

/**
 * Created by ibrohim on 3/2/17.
 */

class Bookmark(private val mContext: Context, private val mId: Int, private val mType: BookmarkType) {
    enum class BookmarkType {
        USER_DEFINED, RECENTLY
    }

    var posisi: Posisi? = null

    fun hapus() {
        val Db = DatabaseHelper.getInstance(mContext).writableDatabase
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
        val Db = DatabaseHelper.getInstance(mContext).writableDatabase
        Db.execSQL("INSERT INTO `bookmark` (`surat`, `ayat`, `terakhir_dibuka`, `frekwensi`) VALUES (?,?,0,0)", arrayOf(Integer.toString(posisi!!.surat), Integer.toString(posisi!!.ayat)))
    }

    private fun saveRecentBookmark() {
        val Db = DatabaseHelper.getInstance(mContext).writableDatabase

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
            val Db = DatabaseHelper.getInstance(mContext).writableDatabase

            var cursor = Db.rawQuery("SELECT _id FROM `bookmark` WHERE _id <= ? ORDER BY `frekwensi` ASC, `terakhir_dibuka` ASC ", arrayOf(Integer.toString(FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID)))

            if (cursor.count < FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID) {

                cursor.close()
                cursor = Db.rawQuery("SELECT MAX(_id) + 1 as _id FROM `bookmark` WHERE _id <= ?", arrayOf(FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID.toString()))

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
