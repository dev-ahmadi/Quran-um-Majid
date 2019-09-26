package id.ahmadiyah.quran.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import id.ahmadiyah.quran.interfaces.AdapterDaftarBookmark
import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.models.Bookmark
import id.ahmadiyah.quran.models.Bookmark.BookmarkType

class FragmentDaftarBookmark : Fragment(), AdapterDaftarBookmark.BookmarkClickListener {

    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var adapter: AdapterDaftarBookmark? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_daftar, container, false)

        recyclerView = rootView.findViewById<View>(R.id.my_recycler_view) as RecyclerView
        recyclerView!!.isNestedScrollingEnabled = true

        layoutManager = LinearLayoutManager(this.context)
        recyclerView!!.layoutManager = layoutManager

        return rootView
    }

    override fun onResume() {
        super.onResume()

        recyclerView!!.adapter = null
        adapter = AdapterDaftarBookmark(context!!.applicationContext)
        adapter!!.setOnClickListener(this)
        recyclerView!!.adapter = adapter
    }

    override fun onBookmarkClick(type: BookmarkType, id: Int, ayat: Int, surat: Int) {
        val intent = Intent(activity, ActivityReading::class.java)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_SURAT, surat)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_AYAT, ayat)

        if (type == BookmarkType.RECENTLY) {
            intent.putExtra(ActivityReading.INTENT_ARGUMENT_SESSION, id)
        }

        startActivity(intent)
    }

    companion object {

        val PRESERVED_RECENT_READING_ID = 3
        val UNDEFINED_SESSION_ID = -1
    }
}