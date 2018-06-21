package me.ibrohim.alquran_um_majid.activities

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AppCompatActivity
import android.util.Log

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

import java.io.IOException
import java.util.ArrayList
import java.util.HashMap
import java.util.LinkedList
import java.util.Objects

import me.ibrohim.alquran_um_majid.R
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper
import me.ibrohim.alquran_um_majid.models.Surat
import name.fraser.neil.plaintext.diff_match_patch

class ActivitySplash : AppCompatActivity() {

    internal val WAIT_SPLASH = 2000
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)


        val DbHelper = DatabaseHelper.getInstance(applicationContext)
        try {
            DbHelper.createDatabase()
        } catch (ioe: IOException) {
            throw Error("Unable to create database")
        }

        Surat.initialize(applicationContext)

        //
        //        final SearchableBuilder.FullSearchableBuilder sBuilder =
        //                new SearchableBuilder.FullSearchableBuilder(getApplicationContext());
        //
        //        sBuilder.addOnFinishListener(new SearchableBuilder.OnFinishListener(){
        //            @Override
        //            public void OnFinish() { Log.d(getClass().getName(),"BUILD SUCCESS"); }
        //        });
        //
        //        sBuilder.start();

        if (isNetworkAvailable) {
            checkUpdateAndWait(WAIT_SPLASH)
        } else {
            Log.d(javaClass.name, "network not available")
            Handler().postDelayed({ showMain() }, WAIT_SPLASH.toLong())
        }


    }

    private fun checkUpdateAndWait(timeout: Int) {

        val lock = Any()

        val threadChecker = object : Thread() {

            override fun run() {

                try {

                    synchronized(lock) {
                        (lock as java.lang.Object).wait(WAIT_SPLASH.toLong())
                    }

                    Log.d(javaClass.name, "resume")

                } catch (ignored: InterruptedException) {
                }

                showMain()
            }

        }

        val vC = VersionChecker()
        vC.doCheckUpdate(lock)

        threadChecker.start()

    }

    private fun showMain() {
        val intent = Intent(this@ActivitySplash, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_TASK_ON_HOME
        startActivity(intent)
        finish()
    }

    private val isNetworkAvailable: Boolean
        get() {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }

    internal inner class VersionChecker : ValueEventListener {

        private var mDatabase: FirebaseDatabase? = null
        private var mUpdater: DatabaseUpdater? = null

        private var mLock: Any? = null
        private var waitState = 0

        private val mCurrentVersion = HashMap<String, Int>()

        fun doCheckUpdate(_Lock: Any) {

            val DbHelper = DatabaseHelper.getInstance(applicationContext)

            mLock = _Lock
            mUpdater = DatabaseUpdater()
            mDatabase = FirebaseDatabase.getInstance()

            val SQLiteDB = DbHelper.database
            val cursor = SQLiteDB.rawQuery("SELECT * FROM versi", arrayOf<String>())

            while (cursor.moveToNext()) {
                val table = cursor.getString(cursor.getColumnIndexOrThrow("tabel"))
                val branch = cursor.getString(cursor.getColumnIndexOrThrow("branch"))
                val versi = cursor.getInt(cursor.getColumnIndexOrThrow("versi"))

                val key = String.format("%s:%s", table, branch)

                mCurrentVersion.put(key, versi)
                mDatabase!!.getReference(key).addListenerForSingleValueEvent(this)
                waitState = waitState + 1
            }

            cursor.close()

        }

        override fun onDataChange(dataSnapshot: DataSnapshot) {

            val key = dataSnapshot.key
            val latestVersion = dataSnapshot.getValue(Int::class.java)
            val currentVersion = mCurrentVersion[key]

            Log.d(javaClass.name, "key: " + key)
            Log.d(javaClass.name, "latestVersion: " + latestVersion!!)
            Log.d(javaClass.name, "currentVersion: " + currentVersion!!)

            if (latestVersion != currentVersion) {

                Log.i(javaClass.name, key + " is obsolete, do updating...")
                val token = key.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                mUpdater!!.doUpdate(token[0], token[1], currentVersion, latestVersion)

            } else {

                waitState = waitState - 1
                if (waitState == 0) {
                    synchronized(mLock!!) {
                        (mLock as java.lang.Object).notifyAll()
                    }
                }

            }

        }

        override fun onCancelled(databaseError: DatabaseError) {
            Log.e(javaClass.name, databaseError.message)

            synchronized(mLock!!) {
                (mLock as java.lang.Object).notifyAll()
            }
        }

    }

    internal inner class DatabaseUpdater : ValueEventListener {

        private val mDatabase: FirebaseDatabase

        init {
            mDatabase = FirebaseDatabase.getInstance()
        }

        private var mTable: String? = null
        private var mBranch: String? = null
        private var mRev: Int = 0
        private var mTarget: Int = 0

        fun doUpdate(table: String, branch: String, currentVersion: Int, latestVersion: Int) {

            mTable = table
            mBranch = branch
            mRev = currentVersion
            mTarget = latestVersion

            updateRoutine()

        }

        private fun updateRoutine() {
            val key = String.format("/%s/%s/%d", mTable, mBranch, mRev)
            mDatabase.getReference(key).addListenerForSingleValueEvent(this)
        }

        override fun onDataChange(versionSnapshot: DataSnapshot) {

            for (patchSnapshot in versionSnapshot.children) {
                val key = patchSnapshot.key
                val splitedKey = key.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

                val surat = Integer.parseInt(splitedKey[0])
                val ayat = Integer.parseInt(splitedKey[1])

                val patch = getPatch(patchSnapshot)
                Log.d(javaClass.name, patch.toString())
            }

            if (mRev < mTarget) {
                mRev = mRev + 1
                updateRoutine()
            }

        }

        private fun getPatch(patchSnapshot: DataSnapshot): diff_match_patch.Patch {

            val patch = diff_match_patch.Patch()

            for (diff in patchSnapshot.children) {
                val operation = diff.child("operation").value as Long
                val text = diff.child("text").value as String

                patch.diffs.push(generateDiff(operation, text))
            }

            return patch
        }

        private fun generateDiff(operation: Long, text: String): diff_match_patch.Diff? {
            when (operation.toInt()) {
                0 -> return diff_match_patch.Diff(diff_match_patch.Operation.EQUAL, text)
                1 -> return diff_match_patch.Diff(diff_match_patch.Operation.INSERT, text)
                -1 -> return diff_match_patch.Diff(diff_match_patch.Operation.DELETE, text)
                else -> return null
            }
        }

        override fun onCancelled(databaseError: DatabaseError) {}
    }

}

