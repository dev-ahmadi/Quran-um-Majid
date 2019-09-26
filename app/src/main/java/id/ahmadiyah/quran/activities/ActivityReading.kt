package id.ahmadiyah.quran.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.MenuItem
import androidx.core.app.NavUtils

import id.ahmadiyah.quran.interfaces.AdapterDaftarAyat
import id.ahmadiyah.quran.R
import id.ahmadiyah.quran.infrastucure.DatabaseHelper
import id.ahmadiyah.quran.models.Bookmark
import id.ahmadiyah.quran.models.Posisi
import id.ahmadiyah.quran.models.Surat

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class ActivityReading : AppCompatActivity(), View.OnClickListener, DialogMenujuKe.PosisiListener {
    private val mHideHandler = Handler()
    private var mContentView: View? = null

    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: RecyclerView.Adapter<*>? = null
    private var mLayoutManager: RecyclerView.LayoutManager? = null

    private var mSessionId: Int = 0
    private var mTampilkanTerjemah: Boolean = false
    private var mSharedPref: SharedPreferences? = null

    internal var mDetailAyat: FragmentAyatDetail? = null

    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        mContentView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_IMMERSIVE
    }
    private var mControlsView: View? = null
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        val actionBar = supportActionBar
        actionBar?.show()
        mControlsView!!.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    private val mDelayHideTouchListener = View.OnTouchListener { view, motionEvent ->
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS)
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        setContentView(R.layout.activity_reading)
        mVisible = true
        mControlsView = findViewById<View>(R.id.fullscreen_content_controls)
        mContentView = findViewById<View>(R.id.ayat_recycler_view)
        mDetailAyat = FragmentAyatDetail()
        mSharedPref = application.getSharedPreferences(localClassName, Context.MODE_PRIVATE)

        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
        actionBar!!.setBackgroundDrawable(ColorDrawable(0xAA555555.toInt()))
        actionBar.elevation = 0f

        mContentView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_IMMERSIVE

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        //findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);

        mRecyclerView = findViewById<View>(R.id.ayat_recycler_view) as RecyclerView
        mRecyclerView!!.isNestedScrollingEnabled = true

        // use a linear layout manager
        mLayoutManager = LinearLayoutManager(applicationContext)
        mRecyclerView!!.layoutManager = mLayoutManager

        mTampilkanTerjemah = mSharedPref!!.getBoolean("tampilkanTerjemah", true)

        val adapter = AdapterDaftarAyat(applicationContext, mTampilkanTerjemah)
        adapter.setOnClickListener(this)

        mAdapter = adapter
        mRecyclerView!!.adapter = mAdapter

        val intent = intent
        val surat = intent.getIntExtra(INTENT_ARGUMENT_SURAT, 1)
        val ayat = intent.getIntExtra(INTENT_ARGUMENT_AYAT, 1)
        val posisi = Posisi(surat, ayat)

        mSessionId = intent.getIntExtra(INTENT_ARGUMENT_SESSION,
                FragmentDaftarBookmark.UNDEFINED_SESSION_ID)

        val namaSurat = Surat.getNamaSurat(applicationContext, surat)
        supportActionBar!!.title = namaSurat
        ScrollTo(posisi)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.

        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            setTitleAsCurrentSurat()
            show()
        }
    }

    private fun setTitleAsCurrentSurat() {
        val posisi = posisi

        val namaSurat = Surat.getNamaSurat(applicationContext, posisi.surat)
        supportActionBar!!.title = namaSurat
    }

    private fun hide() {
        // Hide UI first
        val actionBar = supportActionBar
        actionBar?.hide()
        mControlsView!!.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY.toLong())
    }

    @SuppressLint("InlinedApi")
    private fun show() {
        // Show the system bar
        mContentView!!.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY.toLong())
    }


    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun ScrollTo(posisi: Posisi) {
        val surat = posisi.surat
        val ayat = posisi.ayat

        val db = DatabaseHelper.getInstance(this.applicationContext).readableDatabase

        val cursorPos = db.rawQuery("SELECT * FROM surat WHERE _id = ?", arrayOf(Integer.toString(surat)))
        cursorPos.moveToFirst()
        val posisiAwalSurat = cursorPos.getInt(cursorPos.getColumnIndexOrThrow("position"))
        cursorPos.close()

        val indeks = posisiAwalSurat - 1 + ayat - 1

        val layoutManager = mRecyclerView!!
                .layoutManager as LinearLayoutManager
        layoutManager.scrollToPositionWithOffset(indeks, 0)

    }

    public override fun onPause() {
        super.onPause()
        val posisi = posisi

        val bookmark = Bookmark(applicationContext, mSessionId, Bookmark.BookmarkType.RECENTLY)
        bookmark.posisi = posisi
        bookmark.save()
    }

    private val posisi: Posisi
        get() {
            val layoutManager = mRecyclerView!!.layoutManager as LinearLayoutManager
            val index = layoutManager.findFirstVisibleItemPosition()

            val viewHolder = mRecyclerView!!
                    .findViewHolderForAdapterPosition(index) as AdapterDaftarAyat.ViewHolder

            return viewHolder.posisi!!
        }

    override fun onClick(view: View) {
        toggle()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_reading, menu)

        menu.findItem(R.id.opsi_tampilkan_terjemah).isChecked = mTampilkanTerjemah

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
            R.id.opsi_tampilkan_terjemah -> {
                mTampilkanTerjemah = !mTampilkanTerjemah
                item.isChecked = mTampilkanTerjemah

                val editor = mSharedPref!!.edit()
                editor.putBoolean("tampilkanTerjemah", mTampilkanTerjemah)
                editor.apply()

                perbaharuiTampilan()
                return true
            }
            R.id.action_goto -> {
                val manager = supportFragmentManager
                val frag = manager.findFragmentByTag("fragment_menuju_ke")

                if (frag != null) {
                    manager.beginTransaction().remove(frag).commit()
                }

                val dialogMenujuKe = DialogMenujuKe()

                dialogMenujuKe.setCurrentPosition(posisi)
                dialogMenujuKe.setPosisiListener(this)
                //dialogMenujuKe.setStyle(DialogFragment.STYLE_NORMAL, R.style.CustomDialog);
                dialogMenujuKe.show(manager, "test")
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    private fun perbaharuiTampilan() {
        val adapter = mAdapter as AdapterDaftarAyat?
        adapter!!.setOpsiTampilkanTerjemah(mTampilkanTerjemah)

        adapter.notifyDataSetChanged()
        mRecyclerView!!.invalidate()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        val intentData = intent.data
        if (intentData != null) {
            when (intentData.path) {
                "/tafsir" -> {
                    val id = intentData.getQueryParameter("id")
                    if (id != null) {
                        tampilkanTafsir(id)
                    }
                }
            }
        }
    }

    fun tampilkanTafsir(id: String) {
        mDetailAyat!!.setTafsirId(id)
        mDetailAyat!!.show(supportFragmentManager, mDetailAyat!!.tag)
    }

    override fun SetPosisi(posisi: Posisi) {
        Log.d(javaClass.name, posisi.surat.toString())
        Log.d(javaClass.name, posisi.ayat.toString())

        hide()
        ScrollTo(posisi)
    }

    companion object {
        const val INTENT_ARGUMENT_SURAT = "surat"
        const val INTENT_ARGUMENT_AYAT = "ayat"
        const val INTENT_ARGUMENT_SESSION = "session"

        private const val AUTO_HIDE = true
        private const val AUTO_HIDE_DELAY_MILLIS = 3000

        private const val UI_ANIMATION_DELAY = 300
    }
}
