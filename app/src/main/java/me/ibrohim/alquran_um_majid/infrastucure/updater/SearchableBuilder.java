package me.ibrohim.alquran_um_majid.infrastucure.updater;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import me.ibrohim.alquran_um_majid.infrastucure.DatabaseHelper;

public class SearchableBuilder {

    Context mContext;

    public SearchableBuilder(Context context) {
        mContext = context;
    }

    public void update(int _id, int _lang_id, int surat, int ayat, String teks){

        teks = cast(_lang_id, teks);

        SQLiteDatabase Db = DatabaseHelper.getInstance(mContext).getDatabase();

        final String query=
                "INSERT OR REPLACE INTO `searchable_quran` " +
                        "(_id, _lang_id, surat, ayat, teks) " +
                        "VALUES (?,?,?,?,?)";

        Db.execSQL(query, new String[]{
                Integer.toString(_id),
                Integer.toString(_lang_id),
                Integer.toString(surat),
                Integer.toString(ayat),
                teks
        });

    }

    public static String cast(int _lang_id, String teks){
        switch (_lang_id) {
            case 1:
                // menghilangkan harakat kecuali tasydid
                teks = teks.replaceAll("[\\p{M}-[\u0651]]", "");
                break;
            case 2: teks = teks.replaceAll("<.*?>",""); break;
        }
        return teks;
    }

    public void doIndexing(){
        SQLiteDatabase Db = DatabaseHelper.getInstance(mContext).getDatabase();

        Db.execSQL("INSERT INTO searchable_quran_index(searchable_quran_index) VALUES('rebuild');");
        Db.execSQL("INSERT INTO searchable_quran_index(searchable_quran_index) VALUES('optimize');");
    }

    public static class FullSearchableBuilder extends Thread {

        Context mContext;

        private List<OnFinishListener> mListeners = new ArrayList<>();

        public FullSearchableBuilder(Context context){
            mContext = context;
        }

        public void addOnFinishListener(OnFinishListener listener){
            mListeners.add(listener);
        }

        @Override
        public void run() {

            SQLiteDatabase Db = DatabaseHelper.getInstance(mContext).getDatabase();
            Cursor cursor = Db.rawQuery("SELECT * FROM `quran`", new String[]{});

            int colId = cursor.getColumnIndexOrThrow("_id");
            int colLangId = cursor.getColumnIndexOrThrow("_lang_id");
            int colSurat = cursor.getColumnIndexOrThrow("surat");
            int colAyat = cursor.getColumnIndexOrThrow("ayat");
            int colTeks = cursor.getColumnIndexOrThrow("teks");

            SearchableBuilder sBuilder = new SearchableBuilder(mContext);

            while (cursor.moveToNext()) {

                int _id = cursor.getInt(colId);
                int _lang_id = cursor.getInt(colLangId);
                int surat = cursor.getInt(colSurat);
                int ayat = cursor.getInt(colAyat);
                String teks = cursor.getString(colTeks);

                if (_id % 100 == 0){
                    Log.d(getClass().getName(),Integer.toString(_id));
                }

                sBuilder.update(_id,_lang_id,surat,ayat,teks);

            }

            sBuilder.doIndexing();

            for (OnFinishListener listener:mListeners){
                listener.OnFinish();
            }
        }

    }

    public interface OnFinishListener {
        void OnFinish();
    }

}
