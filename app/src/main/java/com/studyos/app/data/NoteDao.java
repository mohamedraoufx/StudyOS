package com.studyos.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface NoteDao {
    @Insert long insertNote(Note n);
    @Update void updateNote(Note n);
    @Delete void deleteNote(Note n);

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    LiveData<List<Note>> observeNotes();

    @Query("SELECT * FROM notes WHERE title LIKE :q OR body LIKE :q OR category LIKE :q LIMIT 30")
    List<Note> searchNotes(String q);

    @Insert long insertVocab(Vocab v);
    @Insert void insertVocabs(List<Vocab> list);
    @Update void updateVocab(Vocab v);
    @Delete void deleteVocab(Vocab v);

    @Query("SELECT * FROM vocab ORDER BY language, word")
    LiveData<List<Vocab>> observeVocab();

    @Query("SELECT * FROM vocab WHERE word LIKE :q OR translation LIKE :q OR example LIKE :q LIMIT 30")
    List<Vocab> searchVocab(String q);
}
