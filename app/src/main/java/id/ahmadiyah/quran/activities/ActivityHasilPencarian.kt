package id.ahmadiyah.quran.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuItemCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.interfaces.AdapterDaftarHasilPencarian
import id.ahmadiyah.quran.models.Posisi

class ActivityHasilPencarian : AppCompatActivity(), OnNavigateListener {

    private var query: String? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hasil_pencarian)

        recyclerView = findViewById(R.id.my_recycler_view)

        layoutManager = LinearLayoutManager(this)
        recyclerView!!.layoutManager = layoutManager
        val dividerItemDecoration = DividerItemDecoration(recyclerView!!.context, layoutManager!!.orientation)
        recyclerView!!.addItemDecoration(dividerItemDecoration)

        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
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
        searchView.setQuery(query, false)

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
            showQueryResult(intent.getStringExtra(SearchManager.QUERY))
        }
    }

    public override fun onResume() {
        super.onResume()
        if (query != null) showQueryResult(query)
    }

    private fun showQueryResult(searchQuery: String?) {
        if (!searchQuery.isNullOrBlank()) {
            val adapter = AdapterDaftarHasilPencarian(this, searchQuery)
            adapter.setOnNavigateListener(this)
            recyclerView!!.adapter = adapter
        }
    }

    override fun OnNavigate(posisi: Posisi) {
        val intent = Intent(this, ActivityReading::class.java)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_SURAT, posisi.surat)
        intent.putExtra(ActivityReading.INTENT_ARGUMENT_AYAT, posisi.ayat)
        startActivity(intent)
    }
}
