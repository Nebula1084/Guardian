package hku.cs.smp.guardian.tag;

import android.arch.persistence.room.*;

import java.util.List;

@Dao
public abstract class TagDao {

    @Query("SELECT * FROM tag_command")
    public abstract List<TagCommand> getAll();

    @Query("SELECT EXISTS(SELECT * FROM tag_command WHERE phone = :phone LIMIT 1)")
    public abstract boolean isTagged(String phone);

    @Insert
    public abstract void insert(TagCommand command);

    @Delete
    public abstract void delete(TagCommand command);

    @Query("SELECT * FROM tag_command WHERE uploaded = :uploaded")
    public abstract List<TagCommand> getByUploaded(Boolean uploaded);

    @Update
    public abstract void update(TagCommand command);
}
