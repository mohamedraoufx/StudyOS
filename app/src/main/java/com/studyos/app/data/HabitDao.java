package com.studyos.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HabitDao {
    @Insert long insertHabit(Habit h);
    @Update void updateHabit(Habit h);
    @Delete void deleteHabit(Habit h);

    @Query("SELECT * FROM habits ORDER BY id")
    LiveData<List<Habit>> observeHabits();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertLog(HabitLog l);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLogs(List<HabitLog> list);

    @Query("DELETE FROM habit_logs WHERE habitId = :habitId AND day = :day")
    void deleteLog(long habitId, long day);

    @Query("SELECT COUNT(*) FROM habit_logs WHERE habitId = :habitId AND day = :day")
    int countLog(long habitId, long day);

    @Query("SELECT * FROM habit_logs")
    LiveData<List<HabitLog>> observeLogs();
}
