package hku.cs.smp.guardian.tag;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.os.AsyncTask;

import java.util.List;

public class TagHelper {
    private TagDataBase dataBase;

    private static TagHelper instance;


    public static TagHelper getInstance() {
        return instance;
    }

    public static synchronized void init(Context context) {
        if (instance == null)
            instance = new TagHelper(context);
    }

    private TagHelper(Context context) {
        dataBase = Room.databaseBuilder(context, TagDataBase.class, TagDataBase.NAME)
                .fallbackToDestructiveMigration()
                .build();
    }

    public void write(final String phone, final String tag) {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                dataBase.tagDao().insert(new TagCommand(phone, tag));
                return null;
            }
        }.execute();
    }

    public List<TagCommand> read() {
        return dataBase.tagDao().getAll();
    }

    public boolean isTagged(String phone) {
        return dataBase.tagDao().isTagged(phone);
    }

    public List<TagCommand> getByUploaded(Boolean uploaded) {
        return dataBase.tagDao().getByUploaded(uploaded);
    }


    public void update(TagCommand command) {
        dataBase.tagDao().update(command);
    }

    public void delete(TagCommand command) {
        dataBase.tagDao().delete(command);
    }
}
