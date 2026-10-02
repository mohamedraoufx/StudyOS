package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    public static final String LINK_NONE = "", LINK_SUBJECT = "Subject", LINK_PROJECT = "Project",
            LINK_GOAL = "Goal", LINK_LANGUAGE = "Language";

    @PrimaryKey(autoGenerate = true) public long id;
    public String title = "";
    public String body = "";
    public String category = "";
    public String linkType = LINK_NONE;
    public String linkName = "";
    public long updatedAt;
}
