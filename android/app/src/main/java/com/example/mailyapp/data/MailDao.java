package com.example.mailyapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mailyapp.entities.MailEntity;

import java.util.List;

@Dao
public interface MailDao {

    // Insert a single mail or a list of mails
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MailEntity> mails);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(MailEntity mail);

    // Get all mails, ordered by date (assuming date is a string that sorts correctly)
    @Query("SELECT * FROM mails ORDER BY date DESC")
    LiveData<List<MailEntity>> getAllMails();

    // Get a specific mail by id (String-based for MongoDB _id)
    @Query("SELECT * FROM mails WHERE id = :mailId")
    LiveData<MailEntity> getMailById(String mailId);

    // Delete a mail by ID
    @Query("DELETE FROM mails WHERE id = :mailId")
    void deleteById(String mailId);

    // Delete all mails
    @Query("DELETE FROM mails")
    void deleteAll();
}