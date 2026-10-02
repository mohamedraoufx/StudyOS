package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "milestones",
        foreignKeys = @ForeignKey(entity = Goal.class, parentColumns = "id", childColumns = "goalId",
                onDelete = ForeignKey.CASCADE),
        indices = @Index("goalId"))
public class Milestone {
    @PrimaryKey(autoGenerate = true) public long id;
    public long goalId;
    public String title = "";
    public boolean done;
}
