package com.example.mailyapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.entities.MailFolderCrossRef;

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

    @Query("UPDATE mails SET isStarred = :isStarred WHERE id = :mailId")
    void updateStarredFlag(String mailId, boolean isStarred);


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFolderRef(MailFolderCrossRef ref);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertFolderRefs(List<MailFolderCrossRef> refs);

    @Query("DELETE FROM mail_folder WHERE mailId = :mailId AND folder = :folder")
    void removeMailFromFolder(String mailId, String folder);

    @Query("DELETE FROM mail_folder WHERE mailId = :mailId")
    void removeMailFromAllFolders(String mailId);

    @Query("SELECT * FROM mails WHERE id IN (SELECT mailId FROM mail_folder WHERE folder = :folder) ORDER BY date DESC")
    LiveData<List<MailEntity>> getMailsByFolder(String folder);

    @Query("SELECT * FROM mails WHERE isStarred = 1 ORDER BY date DESC")
    LiveData<List<MailEntity>> getStarredMails();

    @Query("SELECT * FROM mails WHERE id = :id LIMIT 1")
    MailEntity getNow(String id);

    @Query("DELETE FROM mail_folder WHERE folder = :folder")
    void removeAllMappingsForFolder(String folder);

    @Query("SELECT * FROM mails WHERE labels LIKE '%' || :labelId || '%' ORDER BY date DESC")
    LiveData<List<MailEntity>> getMailsByLabel(String labelId);

    @Query("SELECT * FROM mails")
    List<MailEntity> getAllNow();

    @Query("UPDATE mails SET labels = :labels WHERE id = :mailId")
    void updateLabels(String mailId, List<String> labels);

    @Query("SELECT * FROM mails WHERE " +
            "subject LIKE '%' || :query || '%' " +
            "OR content LIKE '%' || :query || '%' " +
            "OR sender LIKE '%' || :query || '%' " +
            "OR receiver LIKE '%' || :query || '%' " +
            "ORDER BY date DESC")
    LiveData<List<MailEntity>> searchMails(String query);

}