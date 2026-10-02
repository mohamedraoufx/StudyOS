package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "subtasks",
        foreignKeys = @ForeignKey(entity = Task.class, parentColumns = "id", childColumns = "taskId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("taskId"))
public class Subtask {
    @PrimaryKey(autoGenerate = true) public long id;
    public long taskId;
    public String title = "";
    public boolean done;
}
