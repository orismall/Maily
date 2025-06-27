package com.example.mailyapp.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.mailyapp.entities.UserEntity;

@Dao
public interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users LIMIT 1")
    LiveData<UserEntity> getLoggedInUserLive();

    @Query("DELETE FROM users")
    void deleteAll();

}