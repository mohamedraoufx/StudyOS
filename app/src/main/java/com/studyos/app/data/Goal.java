package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "goals")
public class Goal {
    @PrimaryKey(autoGenerate = true) public long id;
    public String title = "";
    public String notes = "";
    public long deadline;
    public long createdAt;
}
