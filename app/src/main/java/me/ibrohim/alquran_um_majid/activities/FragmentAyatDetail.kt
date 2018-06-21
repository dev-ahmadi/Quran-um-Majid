package me.ibrohim.alquran_um_majid.activities

import android.app.Dialog
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.preference.PreferenceManager
import android.support.design.widget.BottomSheetDialogFragment
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.TextView

import com.google.firebase.crash.FirebaseCrash

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper

/**
 * Created by ibrohim on 2/18/17.
 */

class FragmentAyatDetail : BottomSheetDialogFragment() {

    private var mTafsirId = ""

    private var mContextView: View? = null
    private var mTeksJudul: TextView? = null
    private var mTeksDetail: TextView? = null

    private var mAlphabetFontSize: Int = 0

    fun setTafsirId(id: String) {
        mTafsirId = id
    }

    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val settings = PreferenceManager.getDefaultSharedPreferences(context)

        val alphabetFontSizeString = settings.getString("alphabet_font_size", "2")
        val alphabetMap = resources.getIntArray(R.array.alphabet_font_size)
        mAlphabetFontSize = alphabetMap[Integer.parseInt(alphabetFontSizeString)]

        mContextView = View.inflate(context, R.layout.fragment_ayat_detail, null)
        mTeksDetail = mContextView!!.findViewById<View>(R.id.teks_detail) as TextView
        mTeksJudul = mContextView!!.findViewById<View>(R.id.judul) as TextView

        // Load tafsir content
        val db = DatabaseHelper.getInstance(activity).database

        Log.d(javaClass.name, mTafsirId)

        val row = db.rawQuery(
                "SELECT `teks` FROM tafsir WHERE `_id` = ? AND `_lang_id` = ?",
                arrayOf(mTafsirId, "2"))

        var teks = ""
        if (row.moveToFirst()) {
            teks = row.getString(row.getColumnIndex("teks"))
        } else {
            val reportFormat = "Tafsir index: %s (_lang_id: %d) not found."

            teks = "Mohon maaf, tafsir tidak ditemukan. Kejadian ini akan secara otomatis dilaporkan kepada developer, pastikan perangkat dalam keadaan terhubung koneksi Internet."
            FirebaseCrash.log(String.format(reportFormat, mTafsirId, 2))
        }

        val judul = String.format("Tafsir %s", mTafsirId)
        mTeksJudul!!.text = judul
        mTeksDetail!!.text = Html.fromHtml(teks)
        mTeksDetail!!.textSize = mAlphabetFontSize.toFloat()
        dialog.setContentView(mContextView!!)
    }

}