package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * A topic the user studies. SE = software engineering topic, LANG = a language,
 * SKILL = a skill of a language (groupName = language name), OTHER = anything else.
 */
@Entity(tableName = "subjects")
public class Subject {
    public static final String SE = "SE", LANG = "LANG", SKILL = "SKILL", OTHER = "OTHER";

    @PrimaryKey(autoGenerate = true) public long id;
    public String name = "";
    public String category = OTHER;
    public String groupName = "";
    /** Self-assessed level 0..100. */
    public int level;

    public static Subject of(String name, String category, String group, int level) {
        Subject s = new Subject();
        s.name = name; s.category = category; s.groupName = group; s.level = level;
        return s;
    }
}
