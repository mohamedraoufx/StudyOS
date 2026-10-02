package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "habit_logs",
        foreignKeys = @ForeignKey(entity = Habit.class, parentColumns = "id", childColumns = "habitId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index(value = {"habitId", "day"}, unique = true))
public class HabitLog {
    @PrimaryKey(autoGenerate = true) public long id;
    public long habitId;
    /** Local day number (see TimeUtil.dayNumber). */
    public long day;
}
