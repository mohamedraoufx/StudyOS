package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "tasks",
        foreignKeys = {
                @ForeignKey(entity = Project.class, parentColumns = "id", childColumns = "projectId",
                        onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Goal.class, parentColumns = "id", childColumns = "goalId",
                        onDelete = ForeignKey.SET_NULL)},
        indices = {@Index("projectId"), @Index("goalId")})
public class Task {
    public static final int TODO = 0, IN_PROGRESS = 1, DONE = 2;
    public static final int LOW = 0, MEDIUM = 1, HIGH = 2, URGENT = 3;
    public static final int REPEAT_NONE = 0, REPEAT_DAILY = 1, REPEAT_WEEKLY = 2;

    @PrimaryKey(autoGenerate = true) public long id;
    public String title = "";
    public String notes = "";
    public int priority = MEDIUM;
    public int status = TODO;
    public String category = "";
    public String subject = "";
    /** Midnight (local) of the due day in millis, 0 = no due date. */
    public long dueDate;
    /** Minutes after midnight, -1 = not set. */
    public int startMinute = -1;
    public int durationMin;
    public int repeat = REPEAT_NONE;
    /** Minutes before start time, -1 = no reminder. */
    public int reminderMin = -1;
    public long completedAt;
    public long createdAt;
    public Long projectId;
    public Long goalId;

    public Task copy() {
        Task t = new Task();
        t.id = id; t.title = title; t.notes = notes; t.priority = priority; t.status = status;
        t.category = category; t.subject = subject; t.dueDate = dueDate; t.startMinute = startMinute;
        t.durationMin = durationMin; t.repeat = repeat; t.reminderMin = reminderMin;
        t.completedAt = completedAt; t.createdAt = createdAt; t.projectId = projectId; t.goalId = goalId;
        return t;
    }
}
