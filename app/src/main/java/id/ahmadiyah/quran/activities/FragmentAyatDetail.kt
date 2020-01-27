package id.ahmadiyah.quran.activities

import android.app.Dialog
import android.preference.PreferenceManager
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper

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

        val alphabetFontSizeString = settings.getString("alphabet_font_size", "2")!!
        val alphabetMap = resources.getIntArray(R.array.alphabet_font_size)
        mAlphabetFontSize = alphabetMap[Integer.parseInt(alphabetFontSizeString)]

        mContextView = View.inflate(context, R.layout.fragment_ayat_detail, null)
        mTeksDetail = mContextView!!.findViewById<View>(R.id.teks_detail) as TextView
        mTeksJudul = mContextView!!.findViewById<View>(R.id.judul) as TextView

        // Load tafsir content
        val db = DatabaseHelper.getInstance(activity!!.applicationContext).readableDatabase
        val row = db.rawQuery("SELECT `teks` FROM tafsir WHERE `_id` = ? AND `_lang_id` = 2", arrayOf(mTafsirId))
        var teks = ""
        if (row.moveToFirst()) { teks = row.getString(row.getColumnIndex("teks")) }
        row.close()

        val judul = String.format("Catatan No.%s", mTafsirId)
        mTeksJudul!!.text = judul
        mTeksDetail!!.text = Html.fromHtml(teks)

        //make text clickable
        mTeksDetail!!.movementMethod = LinkMovementMethod.getInstance()

        mTeksDetail!!.textSize = mAlphabetFontSize.toFloat()
        dialog.setContentView(mContextView!!)

    }

}