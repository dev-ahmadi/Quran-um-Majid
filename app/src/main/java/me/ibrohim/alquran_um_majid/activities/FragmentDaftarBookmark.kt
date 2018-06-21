package me.ibrohim.alquran_um_majid.activities

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.util.SparseArrayCompat
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import java.util.ArrayList

import me.ibrohim.alquran_um_majid.interfaces.AdapterDaftarBookmark
import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper
import me.ibrohim.alquran_um_majid.interfaces.SectionedRecyclerViewAdapter

class FragmentDaftarBookmark : Fragment(), View.OnClickListener {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    internal var mSectionedAdapter: SectionedRecyclerViewAdapter? = null
    private var mLayoutManager: LinearLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_daftar, container, false)

        mRecyclerView = rootView.findViewById<View>(R.id.my_recycler_view) as RecyclerView
        mRecyclerView!!.isNestedScrollingEnabled = true

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this.context)
        mRecyclerView!!.layoutManager = mLayoutManager

        resetAdapter()

        return rootView
    }

    override fun onClick(view: View) {
        var index = mRecyclerView!!.getChildLayoutPosition(view)

        val adapter = mAdapter as AdapterDaftarBookmark?
        val cursor = adapter!!.cursor

        // cast sectionedPositionToPosition
        index = mSectionedAdapter!!.sectionedPositionToPosition(index)

        cursor.moveToPosition(index)
        val surat = cursor.getInt(cursor.getColumnIndexOrThrow("surat"))
        val ayat = cursor.getInt(cursor.getColumnIndexOrThrow("ayat"))
        val id = cursor.getInt(cursor.getColumnIndexOrThrow("_id"))

        val intent = Intent(activity, ActivityReading::class.java)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_SURAT, surat)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_AYAT, ayat)

        if (id <= PRESERVED_RECENT_READING_ID) {
            intent.putExtra(ActivityReading.INTENT_ARGUMENT_SESSION, id)
        }

        startActivity(intent)
    }

    override fun onPause() {
        super.onPause()
        //AdapterDaftarBookmark adapter = (AdapterDaftarBookmark) mAdapter;
        //adapter.closeCursor();
    }

    override fun onResume() {
        super.onResume()

        resetAdapter()
    }

    private fun resetAdapter() {

        val Db = DatabaseHelper.getInstance(context).database
        val mapSurat = SparseArrayCompat<String>()
        val cursorSurat = Db.rawQuery("SELECT * FROM surat", null)
        cursorSurat.moveToFirst()
        do {
            val index = cursorSurat.getInt(cursorSurat.getColumnIndexOrThrow("_id"))
            val nama = cursorSurat.getString(cursorSurat.getColumnIndexOrThrow("nama"))

            mapSurat.put(index, nama)
        } while (cursorSurat.moveToNext())
        cursorSurat.close()

        val cursorBookmark = Db.rawQuery("SELECT * FROM bookmark ORDER BY _id", null)
        val adapter = AdapterDaftarBookmark(context, mapSurat, cursorBookmark)
        adapter.setOnClickListener(this)
        mAdapter = adapter

        //This is the code to provide a sectioned list
        val sections = ArrayList<SectionedRecyclerViewAdapter.Section>()

        //Sections
        sections.add(SectionedRecyclerViewAdapter.Section(0, "Sesi Membaca"))
        //sections.add(new SectionedRecyclerViewAdapter.Section(3,"Bookmark"));

        val dummy = arrayOfNulls<SectionedRecyclerViewAdapter.Section>(sections.size)
        mSectionedAdapter = SectionedRecyclerViewAdapter(context, R.layout.list_bookmark_section, R.id.section_text, mAdapter)
        mSectionedAdapter!!.setSections(sections.toTypedArray())

        mRecyclerView!!.adapter = mSectionedAdapter
    }

    companion object {

        val PRESERVED_RECENT_READING_ID = 3
        val UNDEFINED_SESSION_ID = -1
    }
}