package com.studyos.app.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ProjectDao {
    @Insert long insertProject(Project p);
    @Update void updateProject(Project p);
    @Delete void deleteProject(Project p);

    @Query("SELECT * FROM projects ORDER BY id")
    LiveData<List<Project>> observeProjects();

    @Query("SELECT * FROM projects WHERE name LIKE :q OR notes LIKE :q LIMIT 30")
    List<Project> searchProjects(String q);

    @Insert long insertGoal(Goal g);
    @Update void updateGoal(Goal g);
    @Delete void deleteGoal(Goal g);

    @Query("SELECT * FROM goals ORDER BY id")
    LiveData<List<Goal>> observeGoals();

    @Query("SELECT * FROM goals WHERE deadline > 0")
    List<Goal> goalsWithDeadline();

    @Query("SELECT * FROM goals WHERE title LIKE :q OR notes LIKE :q LIMIT 30")
    List<Goal> searchGoals(String q);

    @Insert long insertMilestone(Milestone m);
    @Insert void insertMilestones(List<Milestone> list);

    @Query("DELETE FROM milestones WHERE goalId = :goalId")
    void deleteMilestonesFor(long goalId);

    @Query("SELECT * FROM milestones ORDER BY id")
    LiveData<List<Milestone>> observeMilestones();
}
