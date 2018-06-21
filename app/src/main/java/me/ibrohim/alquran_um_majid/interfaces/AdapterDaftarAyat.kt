package me.ibrohim.alquran_um_majid.interfaces

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.content.res.AppCompatResources
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView

import java.util.HashSet
import java.util.regex.Matcher
import java.util.regex.Pattern

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper
import me.ibrohim.alquran_um_majid.models.Bookmark
import me.ibrohim.alquran_um_majid.models.Posisi
import me.ibrohim.alquran_um_majid.models.Surat

/**
 * Created by ibrohim on 2/5/17.
 */

class AdapterDaftarAyat(private val mContext: Context, private val mCursorAyat: Cursor, private val mCursorTerjemah: Cursor, private var mTampilkanTerjemah: Boolean) : RecyclerView.Adapter<AdapterDaftarAyat.ViewHolder>() {

    private val mAyatPertama: MutableSet<Int>
    private var mListener: View.OnClickListener? = null

    private val mAlphabetFontSize: Int
    private val mArabicFontSize: Int

    private val mArabicFont: Typeface

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        mAyatPertama = HashSet<Int>()

        val Db = DatabaseHelper.getInstance(mContext).database
        val cursor = Db.rawQuery("SELECT position FROM surat", null)
        val cPosition = cursor.getColumnIndexOrThrow("position")
        cursor.moveToFirst()
        do {
            val position = cursor.getInt(cPosition)
            mAyatPertama.add(position)
        } while (cursor.moveToNext())
        cursor.close()

        val settings = PreferenceManager.getDefaultSharedPreferences(mContext)
        val alphabetFontSizeString = settings.getString("alphabet_font_size", "1")
        val arabicFontSizeString = settings.getString("arabic_font_size", "1")

        val arabicMap = mContext.resources.getIntArray(R.array.arabic_font_size)
        val alphabetMap = mContext.resources.getIntArray(R.array.alphabet_font_size)

        mAlphabetFontSize = alphabetMap[Integer.parseInt(alphabetFontSizeString)]
        mArabicFontSize = arabicMap[Integer.parseInt(arabicFontSizeString)]

        mArabicFont = Typeface.createFromAsset(mContext.assets, "fonts/indopak.ttf")
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        mListener = listener
    }

    open class ViewHolder(v: View, var mContext: Context) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        var mTeksAyat: TextView
        var mTeksTerjemah: TextView
        var mNomorAyatTerjemah: TextView

        var mContainerAyat: View
        var mContainerTerjemah: View

        var mPosisi: Posisi? = null

        init {
            mContainerAyat = v.findViewById<View>(R.id.container_ayat)

            mTeksAyat = v.findViewById<View>(R.id.teks_ayat) as TextView
            mTeksTerjemah = v.findViewById<View>(R.id.teks_terjemah) as TextView
            mNomorAyatTerjemah = v.findViewById<View>(R.id.nomor_ayat_terjemah) as TextView
            mContainerTerjemah = v.findViewById<View>(R.id.layout_terjemah)

            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                         menuInfo: ContextMenu.ContextMenuInfo?) {

            val copyAyat = menu.add(Menu.NONE, MENU_SALIN_AYAT, Menu.NONE, "Salin ayat")
            copyAyat.setOnMenuItemClickListener(this)

            val copyTerjemah = menu.add(Menu.NONE, MENU_SALIN_TERJEMAH, Menu.NONE, "Salin terjemah")
            copyTerjemah.setOnMenuItemClickListener(this)

            val bookmark = menu.add(Menu.NONE, MENU_SALIN_BOOKMARK, Menu.NONE, "Letakkan bookmark")
            bookmark.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData

            when (item.itemId) {
                MENU_SALIN_AYAT -> {
                    clip = ClipData.newPlainText("simple text", mTeksAyat.text)
                    clipboard.primaryClip = clip
                }
                MENU_SALIN_TERJEMAH -> {
                    val formatTerjemah = "(%s:%d)"
                    val namaSurat = Surat.getNamaSurat(mPosisi!!.surat)

                    val teks = mTeksTerjemah.text.toString() + String.format(
                            formatTerjemah,
                            namaSurat,
                            mPosisi!!.ayat)

                    clip = ClipData.newPlainText("simple text", teks)
                    clipboard.primaryClip = clip
                }
                MENU_SALIN_BOOKMARK -> {

                    val bookmark = Bookmark(mContext, Bookmark.BOOKMARK_NEW_ID, Bookmark.BookmarkType.USER_DEFINED)
                    bookmark.posisi = mPosisi
                    bookmark.save()
                }
            }

            Log.d(javaClass.name, Integer.toString(item.itemId))
            return true
        }

        companion object {

            val MENU_SALIN_AYAT = 1
            val MENU_SALIN_TERJEMAH = 2
            val MENU_SALIN_BOOKMARK = 3
        }

    }

    class ViewHolderAyatPertama(v: View, mContext: Context): AdapterDaftarAyat.ViewHolder(v, mContext) {
        var mKhatSurat: ImageView
        init {
            mKhatSurat = v.findViewById<View>(R.id.khat_surat) as ImageView
        }
    }

    override fun getItemCount(): Int {
        return mCursorAyat.count
    }

    override fun onBindViewHolder(holder: AdapterDaftarAyat.ViewHolder, position: Int) {

        mCursorAyat.moveToPosition(position)
        mCursorTerjemah.moveToPosition(position)

        val surat = mCursorAyat.getInt(mCursorAyat.getColumnIndexOrThrow("surat"))
        val id = mCursorAyat.getInt(mCursorAyat.getColumnIndexOrThrow("_id"))
        var teks = mCursorAyat.getString(mCursorAyat.getColumnIndexOrThrow("teks"))
        val ayat = mCursorAyat.getInt(mCursorAyat.getColumnIndexOrThrow("ayat"))
        var terjemah = mCursorTerjemah.getString(mCursorAyat.getColumnIndexOrThrow("teks"))

        val background: Int
        if (id % 2 == 0) {
            background = ContextCompat.getColor(mContext, R.color.backgroundLight)
        } else {
            background = ContextCompat.getColor(mContext, R.color.backgroundDark)
        }
        holder.mContainerAyat.setBackgroundColor(background)

        // nomor ayat
        val formatNomorAyat = "﴿\uFEFF%s\uFEFF﴾"
        val nomorAyat = Integer.toString(ayat)
        val _nomorAyatArab = StringBuilder()
        for (i in 0..nomorAyat.length - 1) {
            var c = nomorAyat[i].toInt()
            c = c - 48 + 1776
            _nomorAyatArab.append(c.toChar())
        }
        val nomorAyatOrnamen = String.format(formatNomorAyat, _nomorAyatArab.toString())
        teks = teks + nomorAyatOrnamen

        //menghilangkan 'lihat juga'
        val rParagraph = Pattern.compile("<p>(.+?)</p>")
        val rSpan = Pattern.compile("<span>(.+?)</span>")
        val matchParagraph = rParagraph.matcher(terjemah)
        if (matchParagraph.find()) {
            val temp = matchParagraph.group(1)

            if (temp.length < 21 && matchParagraph.find()) {
                terjemah = matchParagraph.group(1)
            } else {
                terjemah = temp
            }
        }

        // nomor ayat pada terjemah
        val formatNomorAyatTerjemah = "<b>(%d)</b>"
        holder.mNomorAyatTerjemah.text = Html.fromHtml(String.format(formatNomorAyatTerjemah, ayat))
        holder.mNomorAyatTerjemah.textSize = mAlphabetFontSize.toFloat()

        //percobaan:click
        holder.mTeksTerjemah.movementMethod = LinkMovementMethod.getInstance()
        //terjemah = linkTafsir(terjemah);
        //Log.d(getClass().getName(), terjemah);

        if (mTampilkanTerjemah) {
            holder.mContainerTerjemah.visibility = View.VISIBLE
        } else {
            holder.mContainerTerjemah.visibility = View.GONE
        }

        holder.mPosisi = Posisi(surat, ayat)
        holder.mTeksTerjemah.text = Html.fromHtml(terjemah)
        holder.mTeksTerjemah.textSize = mAlphabetFontSize.toFloat()

        holder.mTeksAyat.text = teks
        holder.mTeksAyat.typeface = mArabicFont
        holder.mTeksAyat.textSize = mArabicFontSize.toFloat()

        if (holder is ViewHolderAyatPertama) {
            val image = getBitmapFromDrawableSurat(getDrawableSurat(surat))
            holder.mKhatSurat.setImageBitmap(image)
        }

    }

    private fun isFirstAyat(position: Int): Boolean {
        return mAyatPertama.contains(position + 1)
    }

    @Deprecated("")
    private fun linkTafsir(terjemah: String): String {
        val terjemahDenganLink = StringBuilder(terjemah)
        val rLink = Pattern.compile("<a>([0-9]+[a-z]?)</a>")
        val matchLink = rLink.matcher(terjemah)

        val packageName = mContext.packageName
        var offset = 0
        while (matchLink.find()) {
            val replace = "<a href=\"content://" + packageName + "/tafsir?id=" + matchLink.group(1) + "\">" + matchLink.group(1) + "</a>"
            val start = matchLink.start(0)
            val end = matchLink.end(0)
            terjemahDenganLink.replace(start + offset, end + offset, replace)
            offset = offset + replace.length - (end - start)
        }
        return terjemahDenganLink.toString()
    }

    override fun getItemViewType(position: Int): Int {
        if (isFirstAyat(position)) {
            return 1
        } else {
            return 2
        }
    }

    private fun getBitmapFromDrawableSurat(drawable: Drawable): Bitmap {
        val bitmap = Bitmap.createBitmap(660, 248, Bitmap.Config.ALPHA_8)

        val canvas = Canvas(bitmap)
        drawable.setBounds(0, -138, 660, 392)
        drawable.draw(canvas)
        return bitmap
    }

    private fun getDrawableSurat(position: Int): Drawable {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val path = String.format("surat_%03d", position)
        val id = mContext.resources.getIdentifier(path, "drawable", mContext.packageName)
        val image = AppCompatResources.getDrawable(mContext, id)
        return image!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterDaftarAyat.ViewHolder {
        val v: View

        if (viewType == 1) {
            v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_ayat_pertama, parent, false)
            v.setOnClickListener(mListener)
            v.findViewById<View>(R.id.layout_terjemah).setOnClickListener(mListener)
            return AdapterDaftarAyat.ViewHolderAyatPertama(v, mContext)
        } else {
            v = LayoutInflater.from(parent.context)
                    .inflate(R.layout.list_ayat, parent, false)
            v.setOnClickListener(mListener)
            v.findViewById<View>(R.id.layout_terjemah).setOnClickListener(mListener)
            return AdapterDaftarAyat.ViewHolder(v, mContext)
        }

    }

    fun setOpsiTampilkanTerjemah(tampilkanTerjemah: Boolean) {
        mTampilkanTerjemah = tampilkanTerjemah
    }
}
