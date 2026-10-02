package com.studyos.app.data;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        Task.class, Subtask.class, StudySession.class, Subject.class, DailyAllocation.class,
        Project.class, Goal.class, Milestone.class, Habit.class, HabitLog.class,
        Note.class, Vocab.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public static final String NAME = "studyos.db";
    private static volatile AppDatabase instance;

    public abstract TaskDao taskDao();
    public abstract StudyDao studyDao();
    public abstract ProjectDao projectDao();
    public abstract HabitDao habitDao();
    public abstract NoteDao noteDao();

    public static AppDatabase get(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(context.getApplicationContext(), AppDatabase.class, NAME).build();
                }
            }
        }
        return instance;
    }

    /** Closes the singleton (used before restoring a backup). */
    public static synchronized void closeInstance() {
        if (instance != null) {
            instance.close();
            instance = null;
        }
    }
}
