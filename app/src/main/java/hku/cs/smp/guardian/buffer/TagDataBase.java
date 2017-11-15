package hku.cs.smp.guardian.buffer;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

@Database(entities = {TagCommand.class}, version = 2)
public abstract class TagDataBase extends RoomDatabase {
    public final static String NAME = "tag_database";

    public abstract TagDao tagDao();

}