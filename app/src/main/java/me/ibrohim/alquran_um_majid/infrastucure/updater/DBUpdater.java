package me.ibrohim.alquran_um_majid.infrastucure.updater;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import me.ibrohim.alquran_um_majid.BuildConfig;
import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper;

public class DBUpdater {

    private final String TAG = "DATABASE_VERSIONING";

    private final Context mContext;

    public DBUpdater(Context _context) {
        mContext = _context;
    }

}
