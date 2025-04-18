package com.example.nutritrack.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.nutritrack.database.dao.FoodEntryDao;
import com.example.nutritrack.database.dao.WeightHistoryDao;
import com.example.nutritrack.database.entity.FoodEntryEntity;
import com.example.nutritrack.database.entity.WeightHistoryEntity;
import com.example.nutritrack.database.util.DateConverter;

@Database(entities = {FoodEntryEntity.class, WeightHistoryEntity.class}, version = 1, exportSchema = false)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    public abstract FoodEntryDao foodEntryDao();
    public abstract WeightHistoryDao weightHistoryDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                    AppDatabase.class, "nutritrack_database")
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}