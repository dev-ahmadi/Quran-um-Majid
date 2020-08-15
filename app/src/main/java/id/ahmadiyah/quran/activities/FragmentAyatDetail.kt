package id.ahmadiyah.quran.activities

import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.preference.PreferenceManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper

/**
 * Created by ibrohim on 2/18/17.
 */

class FragmentAyatDetail : BottomSheetDialogFragment() {

    private var tafsirId = ""

    fun setTafsirId(id: String) {
        tafsirId = id
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_ayat_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val settings = PreferenceManager.getDefaultSharedPreferences(context)
        val alphabetFontSizeString = settings.getString("alphabet_font_size", "2")!!
        val alphabetMap = resources.getIntArray(R.array.alphabet_font_size)
        val alphabetFontSize = alphabetMap[Integer.parseInt(alphabetFontSizeString)]

        val teksDetail = view.findViewById<View>(R.id.teks_detail) as TextView
        val teksJudul = view.findViewById<View>(R.id.judul) as TextView

        // Load tafsir content
        val db = DatabaseHelper.getInstance(requireActivity().applicationContext).readableDatabase
        val row = db.rawQuery("SELECT `teks` FROM tafsir WHERE `_id` = ? AND `_lang_id` = 2", arrayOf(tafsirId))
        var teks = ""
        if (row.moveToFirst()) { teks = row.getString(row.getColumnIndex("teks")) }
        row.close()

        val judul = String.format("Catatan No.%s", tafsirId)
        teksJudul.text = judul
        teksDetail.text = Html.fromHtml(teks)

        //make text clickable
        //mTeksDetail!!.movementMethod = LinkMovementMethod.getInstance()

        teksDetail.textSize = alphabetFontSize.toFloat()
    }

}