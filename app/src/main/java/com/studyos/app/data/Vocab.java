package com.studyos.app.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "vocab")
public class Vocab {
    @PrimaryKey(autoGenerate = true) public long id;
    public String language = "";
    public String word = "";
    public String translation = "";
    public String example = "";
    /** 1 = easy, 2 = medium, 3 = hard. */
    public int difficulty = 2;
    public String category = "";
    public boolean learned;
    /** Next review time in millis; 0 = never reviewed (due now). */
    public long reviewDate;
    public int intervalDays;
    public long createdAt;

    public Vocab copy() {
        Vocab v = new Vocab();
        v.id = id; v.language = language; v.word = word; v.translation = translation; v.example = example;
        v.difficulty = difficulty; v.category = category; v.learned = learned; v.reviewDate = reviewDate;
        v.intervalDays = intervalDays; v.createdAt = createdAt;
        return v;
    }
}
