package me.ibrohim.alquran_um_majid.interfaces

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.support.v4.util.SparseArrayCompat
import android.support.v7.app.AppCompatDelegate
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.text.format.DateFormat
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper
import me.ibrohim.alquran_um_majid.models.Bookmark
import me.ibrohim.alquran_um_majid.activities.FragmentDaftarBookmark

class AdapterDaftarBookmark(internal var mContext: Context, internal var mSurat: SparseArrayCompat<String>, cursor: Cursor) : RecyclerView.Adapter<AdapterDaftarBookmark.ViewHolderBookmark>() {

    var cursor: Cursor
        internal set

    internal var mOnClickListener: View.OnClickListener? = null

    init {
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)

        val DbHelper = DatabaseHelper.getInstance(mContext).database
        this.cursor = cursor
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

        var mBookmark: Bookmark? = null
        var mLabelPosisi: TextView

        init {
            mLabelPosisi = v.findViewById<View>(R.id.label_posisi) as TextView
            v.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View,
                                         menuInfo: ContextMenu.ContextMenuInfo?) {

            val copyAyat = menu.add(Menu.NONE, MENU_HAPUS_BOOKMARK, Menu.NONE, "Hapus Bookmark")
            copyAyat.setOnMenuItemClickListener(this)

        }

        override fun onMenuItemClick(item: MenuItem): Boolean {

            when (item.itemId) {
                MENU_HAPUS_BOOKMARK -> {
                    mBookmark!!.hapus()
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
        val surat = cursor.getInt(cursor.getColumnIndexOrThrow("surat"))
        val ayat = cursor.getInt(cursor.getColumnIndexOrThrow("ayat"))
        val frekwensi = cursor.getInt(cursor.getColumnIndexOrThrow("frekwensi"))

        val stringTerakhirDibuka = cursor.getString(cursor.getColumnIndexOrThrow("terakhir_dibuka"))
        val iso8601Format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

        val formatPosisi = mContext.getString(R.string.format_posisi)
        holder.mLabelPosisi.text = Html.fromHtml(String.format(formatPosisi, mSurat.get(surat), ayat))

        val tipeBookmark: Bookmark.BookmarkType
        if (holder is ViewHolderSessionBookmark) {
            tipeBookmark = Bookmark.BookmarkType.RECENTLY

            try {
                val terakhirDibuka = iso8601Format.parse(stringTerakhirDibuka)
                val formattedTerakhirDibuka = getFormattedDate(terakhirDibuka.time)
                holder.mLabelInfo.text = formattedTerakhirDibuka
            } catch (e: ParseException) {
                holder.mLabelInfo.text = ""
                e.printStackTrace()
            }

        } else {
            tipeBookmark = Bookmark.BookmarkType.USER_DEFINED
        }

        holder.mBookmark = Bookmark(mContext, id, tipeBookmark)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolderBookmark {
        val v: View

        when (viewType) {
            1 -> {
                v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_bookmark, parent, false)
                v.setOnClickListener(mOnClickListener)
                return ViewHolderSessionBookmark(v)
            }
            else -> {
                v = LayoutInflater.from(parent.context)
                        .inflate(R.layout.list_bookmark_user, parent, false)
                v.setOnClickListener(mOnClickListener)
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

    fun setOnClickListener(listener: View.OnClickListener) {
        mOnClickListener = listener
    }

    companion object {

        private val MENU_HAPUS_BOOKMARK = 1
    }


}