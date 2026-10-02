package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Part of the daily study goal reserved for one subject (name matches Subject.name). */
@Entity(tableName = "allocations")
public class DailyAllocation {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name = "";
    public int minutes;
}
