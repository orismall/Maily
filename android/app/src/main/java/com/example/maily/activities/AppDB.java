package com.example.maily.activities;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {User.class}, version = 1)
public abstract class AppDB extends RoomDatabase{
    public abstract UserDao userDao();
}
