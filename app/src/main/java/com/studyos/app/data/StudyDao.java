package com.studyos.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StudyDao {
    @Insert long insertSession(StudySession s);
    @Update void updateSession(StudySession s);
    @Delete void deleteSession(StudySession s);

    @Query("SELECT * FROM sessions ORDER BY startTime DESC")
    LiveData<List<StudySession>> observeSessions();

    @Insert long insertSubject(Subject s);
    @Insert void insertSubjects(List<Subject> list);
    @Update void updateSubject(Subject s);
    @Delete void deleteSubject(Subject s);

    @Query("SELECT * FROM subjects ORDER BY id")
    LiveData<List<Subject>> observeSubjects();

    @Query("SELECT * FROM subjects WHERE name LIKE :q OR groupName LIKE :q LIMIT 30")
    List<Subject> searchSubjects(String q);

    @Insert long insertAllocation(DailyAllocation a);
    @Insert void insertAllocations(List<DailyAllocation> list);
    @Update void updateAllocation(DailyAllocation a);
    @Delete void deleteAllocation(DailyAllocation a);

    @Query("SELECT * FROM allocations ORDER BY minutes DESC")
    LiveData<List<DailyAllocation>> observeAllocations();
}
