package com.example.mailyapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mailyapp.entities.LabelEntity;
import java.util.List;

@Dao
public interface LabelDao {

    @Query("SELECT * FROM labels")
    LiveData<List<LabelEntity>> index();

    @Query("SELECT * FROM labels WHERE id = :id")
    LiveData<LabelEntity> get(String id);
    @Query("DELETE FROM labels")
    void deleteAll();
    @Query("DELETE FROM labels WHERE id = :id")
    void deleteById(String id);
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(LabelEntity... labels);
    @Update
    void update(LabelEntity... labels);
    @Delete
    void delete(LabelEntity... labels);
}
