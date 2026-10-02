package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "projects")
public class Project {
    @PrimaryKey(autoGenerate = true) public long id;
    public String name = "";
    public String notes = "";
    public long deadline;
    public long createdAt;
}
