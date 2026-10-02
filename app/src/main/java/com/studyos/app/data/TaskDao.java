package com.studyos.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TaskDao {
    @Insert long insert(Task t);
    @Update void update(Task t);
    @Delete void delete(Task t);

    @Query("SELECT * FROM tasks")
    LiveData<List<Task>> observeAll();

    @Query("SELECT * FROM tasks WHERE status != 2 AND reminderMin >= 0 AND dueDate > 0")
    List<Task> reminderCandidates();

    @Query("SELECT * FROM tasks WHERE title LIKE :q OR notes LIKE :q OR subject LIKE :q ORDER BY dueDate DESC LIMIT 30")
    List<Task> search(String q);

    @Insert long insertSubtask(Subtask s);
    @Update void updateSubtask(Subtask s);

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    void deleteSubtasksFor(long taskId);

    @Query("SELECT * FROM subtasks ORDER BY id")
    LiveData<List<Subtask>> observeSubtasks();

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY id")
    List<Subtask> subtasksFor(long taskId);
}
