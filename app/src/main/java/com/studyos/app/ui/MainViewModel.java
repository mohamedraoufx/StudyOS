package com.studyos.app.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.studyos.app.StudyOsApp;
import com.studyos.app.data.DailyAllocation;
import com.studyos.app.data.Goal;
import com.studyos.app.data.Habit;
import com.studyos.app.data.HabitLog;
import com.studyos.app.data.Milestone;
import com.studyos.app.data.Note;
import com.studyos.app.data.Project;
import com.studyos.app.data.Repository;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Subtask;
import com.studyos.app.data.Task;
import com.studyos.app.data.Vocab;

import java.util.ArrayList;
import java.util.List;

/** One shared ViewModel exposing the repository's data streams to every screen. */
public class MainViewModel extends AndroidViewModel {
    private final Repository repo;

    public final LiveData<List<Task>> tasks;
    public final LiveData<List<Subtask>> subtasks;
    public final LiveData<List<StudySession>> sessions;
    public final LiveData<List<Subject>> subjects;
    public final LiveData<List<DailyAllocation>> allocations;
    public final LiveData<List<Project>> projects;
    public final LiveData<List<Goal>> goals;
    public final LiveData<List<Milestone>> milestones;
    public final LiveData<List<Habit>> habits;
    public final LiveData<List<HabitLog>> habitLogs;
    public final LiveData<List<Note>> notes;
    public final LiveData<List<Vocab>> vocab;

    /** Always-available snapshots (used to fill dialogs synchronously). */
    private List<Subject> subjectSnap = new ArrayList<>();
    private List<Project> projectSnap = new ArrayList<>();
    private List<Goal> goalSnap = new ArrayList<>();
    private final Observer<List<Subject>> subjectObserver = l -> subjectSnap = l == null ? new ArrayList<Subject>() : l;
    private final Observer<List<Project>> projectObserver = l -> projectSnap = l == null ? new ArrayList<Project>() : l;
    private final Observer<List<Goal>> goalObserver = l -> goalSnap = l == null ? new ArrayList<Goal>() : l;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repo = StudyOsApp.repo(application);
        tasks = repo.tasks;
        subtasks = repo.subtasks;
        sessions = repo.sessions;
        subjects = repo.subjects;
        allocations = repo.allocations;
        projects = repo.projects;
        goals = repo.goals;
        milestones = repo.milestones;
        habits = repo.habits;
        habitLogs = repo.habitLogs;
        notes = repo.notes;
        vocab = repo.vocab;
        subjects.observeForever(subjectObserver);
        projects.observeForever(projectObserver);
        goals.observeForever(goalObserver);
    }

    public Repository repo() { return repo; }

    public List<Subject> subjectSnapshot() { return subjectSnap; }
    public List<Project> projectSnapshot() { return projectSnap; }
    public List<Goal> goalSnapshot() { return goalSnap; }

    /** Names of everything the user can log study time against (no language skills). */
    public List<Subject> studySubjects() {
        List<Subject> out = new ArrayList<>();
        for (Subject s : subjectSnap) {
            if (!Subject.SKILL.equals(s.category)) out.add(s);
        }
        return out;
    }

    public List<Subject> languages() {
        List<Subject> out = new ArrayList<>();
        for (Subject s : subjectSnap) {
            if (Subject.LANG.equals(s.category)) out.add(s);
        }
        return out;
    }

    @Override
    protected void onCleared() {
        subjects.removeObserver(subjectObserver);
        projects.removeObserver(projectObserver);
        goals.removeObserver(goalObserver);
    }
}
