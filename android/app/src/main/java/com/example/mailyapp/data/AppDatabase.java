package com.example.mailyapp.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.mailyapp.entities.DraftEntity;
import com.example.mailyapp.entities.LabelEntity;
import com.example.mailyapp.entities.MailEntity;
import com.example.mailyapp.entities.MailFolderCrossRef;
import com.example.mailyapp.utils.Converters;

@Database(entities = {MailEntity.class, LabelEntity.class, DraftEntity.class, MailFolderCrossRef.class}, version = 7, exportSchema = false)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase INSTANCE;

    // DAO getter
    public abstract MailDao mailDao();
    public abstract DraftDao draftDao();
    public abstract LabelDao labelDao();
    // Singleton instance
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "MailyDB"
                    ).fallbackToDestructiveMigration().build();
                }
            }
        }
        return INSTANCE;
    }
}
