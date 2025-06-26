package com.example.mailyapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mailyapp.entities.DraftEntity;

import java.util.List;

@Dao
public interface DraftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(DraftEntity draft);

    @Query("SELECT * FROM drafts ORDER BY date DESC")
    LiveData<List<DraftEntity>> getAllDrafts();

    @Delete
    void delete(DraftEntity draft);

    @Query("DELETE FROM drafts WHERE id = :draftId")
    void deleteById(String draftId);
}
