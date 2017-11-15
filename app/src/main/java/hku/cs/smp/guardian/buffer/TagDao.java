package hku.cs.smp.guardian.buffer;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public abstract class TagDao {

    @Query("SELECT * FROM tag_command")
    public abstract List<TagCommand> getAll();

    @Insert
    public abstract void insert(TagCommand command);

    @Delete
    public abstract void delete(TagCommand command);
}
