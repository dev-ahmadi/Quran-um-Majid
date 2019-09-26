package id.ahmadiyah.quran.infrastucure

import android.content.Context
import android.content.SharedPreferences
import android.database.SQLException
import android.database.sqlite.SQLiteCantOpenDatabaseException
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.*
import android.os.Build

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

    private fun installedDatabaseIsOutdated(): Boolean {
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
        if (installedDatabaseIsOutdated()) {
            context.deleteDatabase(DB_NAME)
            installDatabaseFromAssets()
            writeDatabaseVersionInPreferences()
        }
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

}