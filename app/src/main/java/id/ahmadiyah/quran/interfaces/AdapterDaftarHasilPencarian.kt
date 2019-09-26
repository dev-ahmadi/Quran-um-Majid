package id.ahmadiyah.quran.interfaces

import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import id.ahmadiyah.quran.activities.OnNavigateListener
import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper
import id.ahmadiyah.quran.models.Posisi
import id.ahmadiyah.quran.models.Surat

/**
 * Created by ibrohim on 3/4/17.
 */

class AdapterDaftarHasilPencarian(internal var context: Context, internal var query: String) : RecyclerView.Adapter<AdapterDaftarHasilPencarian.ViewHolder>() {

    companion object {
        private const val SEARCH_QUERY = "SELECT _lang_id, `surat`.`nama` as `nama_surat`, `surat`.`_id` as `nomor_surat`, ayat, snippet(`searchable_quran_index`) as teks FROM `searchable_quran_index` JOIN `surat` ON `searchable_quran_index`.`surat` = `surat`.`_id` WHERE `searchable_quran_index` MATCH ?"
    }

    private var listener: OnNavigateListener? = null
    private var cursor: Cursor

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        cursor = DatabaseHelper.getInstance(context).readableDatabase.rawQuery(SEARCH_QUERY, arrayOf(query))
    }


    fun setOnNavigateListener(listener: OnNavigateListener) {
        this.listener = listener
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        var matchText: TextView
        var identifier: TextView

        var posisi: Posisi? = null

        var listener: OnNavigateListener? = null

        init {

            matchText = v.findViewById<View>(R.id.match_text) as TextView
            identifier = v.findViewById<View>(R.id.identifier) as TextView

            v.setOnClickListener(this)
        }

        fun setOnNavigateListener(listener: OnNavigateListener) {
            this.listener = listener
        }

        override fun onClick(view: View) {
            listener!!.OnNavigate(posisi!!)
        }

    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        cursor.moveToPosition(position)

        val langId = cursor.getInt(cursor.getColumnIndexOrThrow("_lang_id"))
        val teks = cursor.getString(cursor.getColumnIndexOrThrow("teks"))
        val namaSurat = cursor.getString(cursor.getColumnIndexOrThrow("nama_surat"))
        val nomorSurat = cursor.getInt(cursor.getColumnIndexOrThrow("nomor_surat"))
        val ayat = cursor.getInt(cursor.getColumnIndexOrThrow("ayat"))

        if (langId == 1) {
            val fontSize = context.resources.getInteger(R.integer.default_arabic_search_font_size)
            holder.matchText.textSize = fontSize.toFloat()
        }

        val identifier = String.format("%s:%d", namaSurat, ayat)

        holder.posisi = Posisi(nomorSurat, ayat)
        holder.matchText.text = Html.fromHtml(teks)
        holder.identifier.text = identifier
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_hasil_pencarian, parent, false)

        val vH = ViewHolder(v)
        vH.setOnNavigateListener(listener!!)

        return vH
    }

}
