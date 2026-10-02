package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "sessions",
        foreignKeys = @ForeignKey(entity = Project.class, parentColumns = "id", childColumns = "projectId",
                onDelete = ForeignKey.SET_NULL),
        indices = {@Index("projectId"), @Index("startTime")})
public class StudySession {
    public static final String SRC_MANUAL = "manual";
    public static final String SRC_POMODORO = "pomodoro";

    @PrimaryKey(autoGenerate = true) public long id;
    public String subjectName = "";
    public String category = "";
    public Long projectId;
    public long startTime;
    public long endTime;
    public long durationSec;
    public String notes = "";
    public String source = SRC_MANUAL;
}
