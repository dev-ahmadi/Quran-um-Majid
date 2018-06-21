package me.ibrohim.alquran_um_majid.infrastucure;

import android.database.Cursor;

/**
 * Created by ibrohim on 3/28/17.
 */

public interface CursorHolder {
    void closeCursor();
    void updateCursor();
}
