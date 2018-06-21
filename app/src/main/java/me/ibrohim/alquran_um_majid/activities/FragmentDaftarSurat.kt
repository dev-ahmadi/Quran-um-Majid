package me.ibrohim.alquran_um_majid.activities

import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import me.ibrohim.alquran_um_majid.interfaces.AdapterDaftarSurat
import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper

class FragmentDaftarSurat : Fragment(), View.OnClickListener {

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_daftar, container, false)

        mRecyclerView = rootView.findViewById<View>(R.id.my_recycler_view) as RecyclerView
        mRecyclerView!!.isNestedScrollingEnabled = true

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(this.context)
        mRecyclerView!!.layoutManager = mLayoutManager

        val DbHelper = DatabaseHelper.getInstance(context).database
        val cursor = DbHelper.rawQuery("SELECT * FROM surat", null)

        val adapter = AdapterDaftarSurat(context, cursor)
        adapter.setOnClickListener(this)

        mAdapter = adapter
        mRecyclerView!!.adapter = mAdapter

        val mDividerItemDecoration = DividerItemDecoration(mRecyclerView!!.context,
                mLayoutManager!!.orientation)
        mRecyclerView!!.addItemDecoration(mDividerItemDecoration)

        return rootView
    }

    override fun onClick(view: View) {
        val surat = mRecyclerView!!.getChildLayoutPosition(view) + 1
        val ayat = 1

        val intent = Intent(activity, ActivityReading::class.java)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_SURAT, surat)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_AYAT, ayat)
        startActivity(intent)
    }
}