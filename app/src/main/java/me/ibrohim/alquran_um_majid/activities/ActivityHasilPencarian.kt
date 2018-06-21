package me.ibrohim.alquran_um_majid.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.support.v4.view.MenuItemCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.MenuItem

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper
import me.ibrohim.alquran_um_majid.interfaces.AdapterDaftarHasilPencarian
import me.ibrohim.alquran_um_majid.models.Posisi
import me.ibrohim.alquran_um_majid.models.Surat

class ActivityHasilPencarian : AppCompatActivity(), OnNavigateListener {

    private var mQuery: String? = null
    private var mCursor: Cursor? = null
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: LinearLayoutManager? = null

    private val mSearchQuery = "SELECT _lang_id, surat, ayat, snippet(`searchable_quran_index`) as teks FROM `searchable_quran_index` WHERE `searchable_quran_index` MATCH ?"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_pencarian)

        mRecyclerView = findViewById<RecyclerView>(R.id.my_recycler_view)

        mLayoutManager = LinearLayoutManager(this)
        mRecyclerView!!.layoutManager = mLayoutManager

        supportActionBar!!.elevation = .0f

        val dividerItemDecoration = DividerItemDecoration(mRecyclerView!!.context,
                mLayoutManager!!.orientation)
        mRecyclerView!!.addItemDecoration(dividerItemDecoration)

        Surat.initialize(this)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        handleIntent(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_search, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = menu.findItem(R.id.search).actionView as SearchView

        searchView.setSearchableInfo(
                searchManager.getSearchableInfo(componentName))

        val searchMenuItem = menu.findItem(R.id.search)
        searchMenuItem.expandActionView()
        searchView.setQuery(mQuery, false)

        MenuItemCompat.setOnActionExpandListener(searchMenuItem, object : MenuItemCompat.OnActionExpandListener {

            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                return false
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                finish()
                return true
            }

        })

        return true

    }


    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            mQuery = intent.getStringExtra(SearchManager.QUERY)
            showQueryResult()
        }

    }

    private fun showQueryResult() {
        Log.d(javaClass.name, mQuery)

        val Db = DatabaseHelper.getInstance(applicationContext).database
        mCursor = Db.rawQuery(mSearchQuery, arrayOf(mQuery))

        val adapter = AdapterDaftarHasilPencarian(this, mCursor!!)
        adapter.setOnNavigateListener(this)
        mAdapter = adapter

        mRecyclerView!!.adapter = mAdapter
    }

    public override fun onPause() {
        super.onPause()
        if (mCursor != null) mCursor!!.close()
    }

    public override fun onResume() {
        super.onResume()
        if (mQuery != null) showQueryResult()
    }


    override fun OnNavigate(posisi: Posisi) {
        val intent = Intent(this, ActivityReading::class.java)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_SURAT, posisi.surat)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_AYAT, posisi.ayat)
        startActivity(intent)
    }
}
