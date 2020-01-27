package id.ahmadiyah.quran.infrastucure

import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.*
import android.os.Build
import android.util.Log
import java.util.*

class DatabaseHelper
/**
 * Constructor
 * Takes and keeps a reference of the passed context in order to access to the application assets and resources.
 * @param context
 */
private constructor(private var context: Context, dbVersion: Int) : SQLiteOpenHelper(context, DB_NAME, null, dbVersion) {
    private val dbPath: String

    init {
        this.dbPath = context.getDatabasePath(DB_NAME).path
    }

    private val preferences: SharedPreferences = context.getSharedPreferences(
            "${context.packageName}.database_versions",
            Context.MODE_PRIVATE
    )

    private fun alreadyInstalledDatabase(): Boolean {
        return preferences.getInt(DB_NAME, 0) != 0
    }

    private fun installedDatabaseIsOutdated(): Boolean {
        Log.d(TAG, "${preferences.getInt(DB_NAME, 0)} ${getVersionCode()}")
        return preferences.getInt(DB_NAME, 0) < getVersionCode()
    }

    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(DB_NAME, getVersionCode())
            apply()
        }
    }

    @Synchronized
    private fun installOrUpdateIfNecessary() {
        val isInstalled = alreadyInstalledDatabase()
        val isOutdated = installedDatabaseIsOutdated()
        when {
            isInstalled && isOutdated -> {
                val bookmarks = backupBookmark()
                installLatestDatabase()
                restoreBookmark(bookmarks)
            }
            isInstalled && !isOutdated -> {
                // do nothing
            }
            !isInstalled -> {
                installLatestDatabase()
            }
        }
    }

    private fun backupBookmark(): List<BookmarkInstance>{
        val db = super.getReadableDatabase()
        val cursor = db.rawQuery("SELECT _id, surat, ayat, strftime('%s', terakhir_dibuka) as terakhir_dibuka, frekwensi FROM bookmark", arrayOf())
        val list = LinkedList<BookmarkInstance>()
        if (cursor.moveToFirst()) {
            do {
                val instance = BookmarkInstance(
                        id = cursor.getInt(cursor.getColumnIndexOrThrow("_id")),
                        surat = cursor.getInt(cursor.getColumnIndexOrThrow("surat")),
                        ayat = cursor.getInt(cursor.getColumnIndexOrThrow("ayat")),
                        terakhirDibuka = cursor.getLong(cursor.getColumnIndexOrThrow("terakhir_dibuka")),
                        frekwensi = cursor.getInt(cursor.getColumnIndexOrThrow("frekwensi")))
                list.add(instance)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return list
    }

    private fun restoreBookmark(list: List<BookmarkInstance>) {
        val db = super.getWritableDatabase()
        for (instance in list) {
            db.execSQL("INSERT OR REPLACE INTO `bookmark`(`_id`, `surat`, `ayat`, `terakhir_dibuka`, `frekwensi`) VALUES (?, ?, ?, datetime(?, 'unixepoch'), ?);",
                    arrayOf(instance.id, instance.surat, instance.ayat, instance.terakhirDibuka, instance.frekwensi))
        }
        db.close()
    }

    private fun installLatestDatabase() {
        context.deleteDatabase(DB_NAME)
        installDatabaseFromAssets()
        writeDatabaseVersionInPreferences()
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary()
        return super.getWritableDatabase()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary()
        return super.getReadableDatabase()
    }

    private fun getVersionCode(): Int {
        return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
    }

    @Throws(IOException::class)
    private fun installDatabaseFromAssets() {
        val inputStream = context.assets.open(DB_NAME)
        val outputFilePath = context.getDatabasePath(DB_NAME).absolutePath
        val outputFile = File(outputFilePath)
        if (!outputFile.exists()) { outputFile.parentFile!!.mkdirs() }
        val outputStream = FileOutputStream(outputFile)

        inputStream.copyTo(outputStream, 1024)
        inputStream.close()

        outputStream.flush()
        outputStream.close()
    }

    override fun onCreate(db: SQLiteDatabase) {
        // nothing to do
    }

    override fun onOpen(database: SQLiteDatabase) {
        super.onOpen(database)
        if (Build.VERSION.SDK_INT >= 28) {
            // read: https://stackoverflow.com/questions/54051322/database-import-and-export-not-working-in-android-pie/54056779
            database.disableWriteAheadLogging()
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // nothing to do
    }

    companion object {

        private const val DB_NAME = "quran-db.db"
        private val TAG = DatabaseHelper::class.simpleName

        private var INSTANCE: DatabaseHelper? = null
        fun getInstance(context: Context): DatabaseHelper {

            val versionCode = context.packageManager.getPackageInfo(context.packageName, 0).versionCode

            when (INSTANCE) {
                null -> INSTANCE = DatabaseHelper(context, versionCode)
                else -> INSTANCE!!.context = context
            }

            return INSTANCE!!
        }

    }

    data class BookmarkInstance(
            val id: Int,
            val surat: Int,
            val ayat: Int,
            val terakhirDibuka: Long,
            val frekwensi: Int)

}