package id.ahmadiyah.quran.infrastucure.updater;

/**
 * Created by ibrohim on 3/3/17.
 */

public class Updater {

    private static class UpdaterHolder {
        public static final Updater INSTANCE = new Updater();
    }
    public static Updater getInstance(){
        return UpdaterHolder.INSTANCE;
    }
}
