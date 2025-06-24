package com.example.mailyapp.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.mailyapp.entities.LabelEntity;
import java.util.List;

@Dao
public interface LabelDao {

    @Query("SELECT * FROM LabelEntity")
    List<LabelEntity> index();

    @Query("SELECT * FROM LabelEntity WHERE id = :id")
    LabelEntity get(String id);
    @Query("DELETE FROM LabelEntity")
    void deleteAll();
    @Insert
    void insert(LabelEntity... labels);
    @Update
    void update(LabelEntity... labels);
    @Delete
    void delete(LabelEntity... labels);
}
