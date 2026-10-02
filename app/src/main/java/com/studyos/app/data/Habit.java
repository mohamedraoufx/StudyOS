package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "habits")
public class Habit {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name = "";
    public boolean weekly;
    public long createdAt;
}
