package me.ibrohim.alquran_um_majid.interfaces

import android.content.Context
import android.database.Cursor
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import me.ibrohim.alquran_um_majid.activities.OnNavigateListener
import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.models.Posisi
import me.ibrohim.alquran_um_majid.models.Surat

/**
 * Created by ibrohim on 3/4/17.
 */

class AdapterDaftarHasilPencarian(internal var mContext: Context, internal var mCursor: Cursor) : RecyclerView.Adapter<AdapterDaftarHasilPencarian.ViewHolder>() {

    internal var mListener: OnNavigateListener? = null

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
    }

    fun setCursor(cursor: Cursor) {
        mCursor = cursor
        notifyDataSetChanged()
    }

    fun setOnNavigateListener(listener: OnNavigateListener) {
        mListener = listener
    }

    class ViewHolder(v: View) : RecyclerView.ViewHolder(v), View.OnClickListener {

        var mMatchText: TextView
        var mIdentifier: TextView

        var mPosisi: Posisi? = null

        var mListener: OnNavigateListener? = null

        init {

            mMatchText = v.findViewById<View>(R.id.match_text) as TextView
            mIdentifier = v.findViewById<View>(R.id.identifier) as TextView

            v.setOnClickListener(this)
        }

        fun setOnNavigateListener(listener: OnNavigateListener) {
            mListener = listener
        }

        override fun onClick(view: View) {
            mListener!!.OnNavigate(mPosisi!!)
        }

    }

    override fun getItemCount(): Int {
        return mCursor.count
    }

    override fun onBindViewHolder(holder: AdapterDaftarHasilPencarian.ViewHolder, position: Int) {
        mCursor.moveToPosition(position)

        val colLangId = mCursor.getColumnIndexOrThrow("_lang_id")
        val colSurat = mCursor.getColumnIndexOrThrow("surat")
        val colAyat = mCursor.getColumnIndexOrThrow("ayat")
        val colTeks = mCursor.getColumnIndexOrThrow("teks")

        val langId = mCursor.getInt(colLangId)
        val teks = mCursor.getString(colTeks)
        val surat = mCursor.getInt(colSurat)
        val ayat = mCursor.getInt(colAyat)
        val namaSurat = Surat.getNamaSurat(surat)

        if (langId == 1) {
            val fontSize = mContext.resources.getInteger(R.integer.default_arabic_search_font_size)
            holder.mMatchText.textSize = fontSize.toFloat()
        }

        val identifier = String.format("%s:%d", namaSurat, ayat)

        holder.mPosisi = Posisi(surat, ayat)
        holder.mMatchText.text = Html.fromHtml(teks)
        holder.mIdentifier.text = identifier
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterDaftarHasilPencarian.ViewHolder {
        val v = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_hasil_pencarian, parent, false)

        val vH = AdapterDaftarHasilPencarian.ViewHolder(v)
        vH.setOnNavigateListener(mListener!!)

        return vH
    }

}
