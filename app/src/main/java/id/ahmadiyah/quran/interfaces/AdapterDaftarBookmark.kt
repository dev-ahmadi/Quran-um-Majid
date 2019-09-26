package id.ahmadiyah.quran.interfaces

import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.text.format.DateFormat
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.text.SimpleDateFormat
import java.util.Calendar

import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper
import id.ahmadiyah.quran.models.Bookmark
import id.ahmadiyah.quran.activities.FragmentDaftarBookmark

class AdapterDaftarBookmark(internal var mContext: Context) : RecyclerView.Adapter<AdapterDaftarBookmark.ViewHolderBookmark>() {

    lateinit var cursor: Cursor
        internal set

    internal var mOnClickListener: BookmarkClickListener? = null

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        updateCursor()
    }

    @Synchronized
    fun updateCursor() {
        val db = DatabaseHelper.getInstance(mContext).readableDatabase
        this.cursor = db.rawQuery("SELECT `bookmark`.`_id`, `surat`.`nama` as nama_surat, `bookmark`.`surat` as nomor_surat, `bookmark`.`ayat`, `bookmark`.`terakhir_dibuka` FROM `bookmark` JOIN `surat` ON `bookmark`.`surat` = `surat`.`_id` ORDER BY `bookmark`.`_id`", null)
    }

    override fun getItemViewType(position: Int): Int {
        cursor.moveToPosition(position)
        val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))

        if (id <= FragmentDaftarBookmark.PRESERVED_RECENT_READING_ID) {
            return 1
        }

        return 2
    }

    open inner class ViewHolderBookmark(v: View) : RecyclerView.ViewHolder(v), View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        var id: Int = -1
        var nomorSurat: Int = -1
        var nomorAyat: Int = -1

        var tipeBookmark: Bookmark.BookmarkType? = null
        var bookmark: Bookmark? = null
        var labelPosisi: TextView

        init {
            labelPosisi = v.findViewById<View>(R.id.label_posisi) as TextView
            v.setOnCreateContextMenuListener(this)
            v.setOnClickListener{ mOnClickListener!!.onBookmarkClick(tipeBookmark!!, id, nomorAyat, nomorSurat) }
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                         menuInfo: ContextMenu.ContextMenuInfo?) {

            val copyAyat = menu.add(Menu.NONE, MENU_HAPUS_BOOKMARK, Menu.NONE, "Hapus Bookmark")
            copyAyat.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {

            when (item.itemId) {
                MENU_HAPUS_BOOKMARK -> {
                    bookmark!!.hapus()
                    updateCursor()
                    notifyDataSetChanged()
                }
            }

            return true
        }

    }

    inner class ViewHolderSessionBookmark(v: View): AdapterDaftarBookmark.ViewHolderBookmark(v){
        var mLabelInfo: TextView = v.findViewById<View>(R.id.label_info) as TextView
    }

    override fun getItemCount(): Int {
        return cursor.count
    }

    override fun onBindViewHolder(holder: ViewHolderBookmark, position: Int) {
        cursor.moveToPosition(position)

        val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))
        val namaSurat = cursor.getString(cursor.getColumnIndexOrThrow("nama_surat"))
        val nomorSurat = cursor.getInt(cursor.getColumnIndexOrThrow("nomor_surat"))
        val nomorAyat = cursor.getInt(cursor.getColumnIndexOrThrow("ayat"))

        val stringTerakhirDibuka = cursor.getString(cursor.getColumnIndexOrThrow("terakhir_dibuka"))
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        var labelInfo = ""
        try {
            val terakhirDibuka = iso8601Format.parse(stringTerakhirDibuka)!!
            val formattedTerakhirDibuka = getFormattedDate(terakhirDibuka.time)
            labelInfo = formattedTerakhirDibuka
        } catch (e: Exception) {
            e.printStackTrace()
        }


        val tipeBookmark: Bookmark.BookmarkType
        if (holder is ViewHolderSessionBookmark) {
            tipeBookmark = Bookmark.BookmarkType.RECENTLY
            holder.mLabelInfo.text = labelInfo
        } else {
            tipeBookmark = Bookmark.BookmarkType.USER_DEFINED
        }

        holder.id = id
        holder.nomorSurat = nomorSurat
        holder.nomorAyat = nomorAyat
        holder.tipeBookmark = tipeBookmark
        holder.labelPosisi.text = Html.fromHtml(String.format("%s:%d", namaSurat, nomorAyat))
        holder.bookmark = Bookmark(mContext, id, tipeBookmark)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBookmark {
        val v: View

        when (viewType) {
            1 -> {
                v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_bookmark, parent, false)
                return ViewHolderSessionBookmark(v)
            }
            else -> {
                v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_bookmark_user, parent, false)
                return ViewHolderBookmark(v)
            }
        }



    }

    fun getFormattedDate(smsTimeInMilis: Long): String {
        val smsTime = Calendar.getInstance()
        smsTime.timeInMillis = smsTimeInMilis

        val now = Calendar.getInstance()

        val timeFormatString = "h:mm aa"
        val dateTimeFormatString = "EEEE, MMMM d, h:mm aa"

        if (now.get(Calendar.DATE) == smsTime.get(Calendar.DATE)) {
            return "Today " + DateFormat.format(timeFormatString, smsTime)
        } else if (now.get(Calendar.DATE) - smsTime.get(Calendar.DATE) == 1) {
            return "Yesterday " + DateFormat.format(timeFormatString, smsTime)
        } else if (now.get(Calendar.YEAR) == smsTime.get(Calendar.YEAR)) {
            return DateFormat.format(dateTimeFormatString, smsTime).toString()
        } else {
            return DateFormat.format("MMMM dd yyyy, h:mm aa", smsTime).toString()
        }
    }

    fun setOnClickListener(listener: BookmarkClickListener) {
        mOnClickListener = listener
    }

    companion object {

        private val MENU_HAPUS_BOOKMARK = 1
    }

    interface BookmarkClickListener {
        fun onBookmarkClick(tipeBookmark: Bookmark.BookmarkType, id: Int, ayat: Int, surat: Int)
    }



}