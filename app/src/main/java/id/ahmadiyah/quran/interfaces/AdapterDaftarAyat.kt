package id.ahmadiyah.quran.interfaces

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.preference.PreferenceManager
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
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
import java.util.regex.Pattern

import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper
import id.ahmadiyah.quran.models.Bookmark
import id.ahmadiyah.quran.models.Posisi

/**
 * Created by ibrohim on 2/5/17.
 */

class AdapterDaftarAyat(private val mContext: Context, private var mTampilkanTerjemah: Boolean) : RecyclerView.Adapter<AdapterDaftarAyat.ViewHolder>() {

    private val mAyatPertama: MutableSet<Int>
    private var mListener: View.OnClickListener? = null
    private val mCursorAyat: Cursor
    private val mCursorTerjemah: Cursor

    private val mAlphabetFontSize: Int
    private val mArabicFontSize: Int

    private val mArabicFont: Typeface

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        mAyatPertama = HashSet<Int>()

        val db = DatabaseHelper.getInstance(mContext).writableDatabase
        mCursorAyat = db.rawQuery("SELECT * FROM quran WHERE _lang_id = 1", null)
        mCursorTerjemah = db.rawQuery("SELECT * FROM quran WHERE _lang_id = 2", null)
        val cursor = db.rawQuery("SELECT position FROM surat", null)

        cursor.moveToFirst()
        do {
            val position = cursor.getInt(cursor.getColumnIndexOrThrow("position"))
            mAyatPertama.add(position)
        } while (cursor.moveToNext())
        cursor.close()

        val settings = PreferenceManager.getDefaultSharedPreferences(mContext)
        val alphabetFontSizeString = settings.getString("alphabet_font_size", "1")!!
        val arabicFontSizeString = settings.getString("arabic_font_size", "1")!!

        val arabicMap = mContext.resources.getIntArray(R.array.arabic_font_size)
        val alphabetMap = mContext.resources.getIntArray(R.array.alphabet_font_size)

        mAlphabetFontSize = alphabetMap[Integer.parseInt(alphabetFontSizeString)]
        mArabicFontSize = arabicMap[Integer.parseInt(arabicFontSizeString)]

        mArabicFont = Typeface.createFromAsset(mContext.assets, "fonts/noorehuda-webfont.otf")
    }

    fun setOnClickListener(listener: View.OnClickListener) {
        mListener = listener
    }

    open class ViewHolder(v: View, var mContext: Context) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        var teksAyat: TextView
        var teksTerjemah: TextView
        var teksNomorAyatTerjemah: TextView

        var containerAyat: View
        var containerTerjemah: View

        var posisi: Posisi? = null

        init {
            containerAyat = v.findViewById<View>(R.id.container_ayat)

            teksAyat = v.findViewById<View>(R.id.teks_ayat) as TextView
            teksTerjemah = v.findViewById<View>(R.id.teks_terjemah) as TextView
            teksNomorAyatTerjemah = v.findViewById<View>(R.id.nomor_ayat_terjemah) as TextView
            containerTerjemah = v.findViewById<View>(R.id.layout_terjemah)

            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                         menuInfo: ContextMenu.ContextMenuInfo?) {

            val copyAyat = menu.add(Menu.NONE, MENU_SALIN_AYAT, Menu.NONE, "Salin ayat")
            copyAyat.setOnMenuItemClickListener(this)

            val copyTerjemah = menu.add(Menu.NONE, MENU_SALIN_TERJEMAH, Menu.NONE, "Salin terjemah")
            copyTerjemah.setOnMenuItemClickListener(this)

            val bookmark = menu.add(Menu.NONE, MENU_TAMBAH_BOOKMARK, Menu.NONE, "Letakkan bookmark")
            bookmark.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            when (item.itemId) {
                MENU_SALIN_AYAT -> salinAyat()
                MENU_SALIN_TERJEMAH -> salinTerjemah()
                MENU_TAMBAH_BOOKMARK -> tambahBookmark()
            }

            Log.d(javaClass.name, Integer.toString(item.itemId))
            return true
        }

        private fun setClipboard(teks: String) {
            val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("simple text", teks)
            clipboard.setPrimaryClip(clip)
        }

        private fun salinAyat() {
            setClipboard(teksAyat.text.toString())
        }

        private fun salinTerjemah() {
            val query = "SELECT `teks`, `surat`.`nama` as `nama_surat` FROM `searchable_quran` JOIN `surat` ON `searchable_quran`.`surat` = `surat`.`_id` WHERE `surat` = ? AND `ayat` = ? AND `_lang_id` = 2"
            val cursor = DatabaseHelper.getInstance(mContext).writableDatabase.rawQuery(query, arrayOf(posisi!!.surat.toString(), posisi!!.ayat.toString()))
            cursor.moveToFirst()
            val teksTerjemah = cursor.getString(cursor.getColumnIndexOrThrow("teks"))
            val namaSurat = cursor.getString(cursor.getColumnIndexOrThrow("nama_surat"))
            cursor.close()

            val teks = teksTerjemah + String.format("(%s:%d)",namaSurat,posisi!!.ayat)
            setClipboard(teks)
        }

        private fun tambahBookmark() {
            val bookmark = Bookmark(mContext, Bookmark.BOOKMARK_NEW_ID, Bookmark.BookmarkType.USER_DEFINED)
            bookmark.posisi = posisi
            bookmark.save()
        }

        companion object {
            const val MENU_SALIN_AYAT = 1
            const val MENU_SALIN_TERJEMAH = 2
            const val MENU_TAMBAH_BOOKMARK = 3
        }

    }

    class ViewHolderAyatPertama(v: View, mContext: Context): ViewHolder(v, mContext) {
        var mKhatSurat: ImageView
        init {
            mKhatSurat = v.findViewById<View>(R.id.khat_surat) as ImageView
        }
    }

    override fun getItemCount(): Int {
        return mCursorAyat.count
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

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
        holder.containerAyat.setBackgroundColor(background)

        // nomor ayat pada terjemah
        val formatNomorAyatTerjemah = "<b>(%d)</b>"
        holder.teksNomorAyatTerjemah.text = Html.fromHtml(String.format(formatNomorAyatTerjemah, ayat))
        holder.teksNomorAyatTerjemah.textSize = mAlphabetFontSize.toFloat()

        //percobaan:click
        holder.teksTerjemah.movementMethod = LinkMovementMethod.getInstance()
        //terjemah = linkTafsir(terjemah);
        //Log.d(getClass().getName(), terjemah);

        if (mTampilkanTerjemah) {
            holder.containerTerjemah.visibility = View.VISIBLE
        } else {
            holder.containerTerjemah.visibility = View.GONE
        }

        holder.posisi = Posisi(surat, ayat)
        holder.teksTerjemah.text = Html.fromHtml(terjemah)
        holder.teksTerjemah.textSize = mAlphabetFontSize.toFloat()

        holder.teksAyat.text = teks
        holder.teksAyat.typeface = mArabicFont
        holder.teksAyat.textSize = mArabicFontSize.toFloat()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
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
            return ViewHolder(v, mContext)
        }

    }

    fun setOpsiTampilkanTerjemah(tampilkanTerjemah: Boolean) {
        mTampilkanTerjemah = tampilkanTerjemah
    }
}
