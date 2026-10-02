package com.studyos.app.data;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;

import com.studyos.app.notify.Reminders;
import com.studyos.app.util.Srs;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Single access point to the database. All writes run on a background thread. */
public class Repository {
    public static class SearchResult {
        public List<Task> tasks = new ArrayList<>();
        public List<Project> projects = new ArrayList<>();
        public List<Goal> goals = new ArrayList<>();
        public List<Note> notes = new ArrayList<>();
        public List<Vocab> vocab = new ArrayList<>();
        public List<Subject> subjects = new ArrayList<>();

        public boolean isEmpty() {
            return tasks.isEmpty() && projects.isEmpty() && goals.isEmpty()
                    && notes.isEmpty() && vocab.isEmpty() && subjects.isEmpty();
        }
    }

    public interface SearchCallback {
        void onResult(SearchResult result);
    }

    private final Context app;
    private final AppDatabase db;
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

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

    public Repository(Context context) {
        app = context.getApplicationContext();
        db = AppDatabase.get(app);
        tasks = db.taskDao().observeAll();
        subtasks = db.taskDao().observeSubtasks();
        sessions = db.studyDao().observeSessions();
        subjects = db.studyDao().observeSubjects();
        allocations = db.studyDao().observeAllocations();
        projects = db.projectDao().observeProjects();
        goals = db.projectDao().observeGoals();
        milestones = db.projectDao().observeMilestones();
        habits = db.habitDao().observeHabits();
        habitLogs = db.habitDao().observeLogs();
        notes = db.noteDao().observeNotes();
        vocab = db.noteDao().observeVocab();
    }

    public AppDatabase database() { return db; }

    public void io(Runnable r) { io.execute(r); }

    // ---------------- tasks ----------------
    public void saveTask(final Task t, final List<Subtask> subs) {
        io.execute(() -> {
            if (t.createdAt == 0) t.createdAt = System.currentTimeMillis();
            if (t.id == 0) t.id = db.taskDao().insert(t);
            else db.taskDao().update(t);
            if (subs != null) {
                db.taskDao().deleteSubtasksFor(t.id);
                for (Subtask s : subs) {
                    s.id = 0;
                    s.taskId = t.id;
                    db.taskDao().insertSubtask(s);
                }
            }
            Reminders.scheduleTask(app, t);
        });
    }

    public void deleteTask(final Task t) {
        io.execute(() -> {
            Reminders.cancelTask(app, t.id);
            db.taskDao().delete(t);
        });
    }

    /** Completes/reopens a task. Completing a repeating task creates the next occurrence. */
    public void setTaskDone(final Task original, final boolean done) {
        io.execute(() -> {
            Task t = original.copy();
            if (done) {
                t.status = Task.DONE;
                t.completedAt = System.currentTimeMillis();
                db.taskDao().update(t);
                if (t.repeat != Task.REPEAT_NONE && t.dueDate > 0) {
                    Task next = t.copy();
                    next.id = 0;
                    next.status = Task.TODO;
                    next.completedAt = 0;
                    next.createdAt = System.currentTimeMillis();
                    next.dueDate = TimeUtil.addDays(t.dueDate, t.repeat == Task.REPEAT_DAILY ? 1 : 7);
                    next.id = db.taskDao().insert(next);
                    for (Subtask old : db.taskDao().subtasksFor(t.id)) {
                        Subtask copy = new Subtask();
                        copy.taskId = next.id;
                        copy.title = old.title;
                        copy.done = false;
                        db.taskDao().insertSubtask(copy);
                    }
                    Reminders.scheduleTask(app, next);
                }
            } else {
                t.status = Task.TODO;
                t.completedAt = 0;
                db.taskDao().update(t);
            }
            Reminders.scheduleTask(app, t);
        });
    }

    // ---------------- study ----------------
    public void saveSession(final StudySession s) {
        io.execute(() -> {
            if (s.id == 0) s.id = db.studyDao().insertSession(s);
            else db.studyDao().updateSession(s);
        });
    }

    public void deleteSession(final StudySession s) {
        io.execute(() -> db.studyDao().deleteSession(s));
    }

    public void saveSubject(final Subject s) {
        io.execute(() -> {
            if (s.id == 0) s.id = db.studyDao().insertSubject(s);
            else db.studyDao().updateSubject(s);
        });
    }

    public void deleteSubject(final Subject s) {
        io.execute(() -> db.studyDao().deleteSubject(s));
    }

    public void saveAllocation(final DailyAllocation a) {
        io.execute(() -> {
            if (a.id == 0) a.id = db.studyDao().insertAllocation(a);
            else db.studyDao().updateAllocation(a);
        });
    }

    public void deleteAllocation(final DailyAllocation a) {
        io.execute(() -> db.studyDao().deleteAllocation(a));
    }

    // ---------------- projects & goals ----------------
    public void saveProject(final Project p) {
        io.execute(() -> {
            if (p.createdAt == 0) p.createdAt = System.currentTimeMillis();
            if (p.id == 0) p.id = db.projectDao().insertProject(p);
            else db.projectDao().updateProject(p);
        });
    }

    public void deleteProject(final Project p) {
        io.execute(() -> db.projectDao().deleteProject(p));
    }

    public void saveGoal(final Goal g, final List<Milestone> ms) {
        io.execute(() -> {
            if (g.createdAt == 0) g.createdAt = System.currentTimeMillis();
            if (g.id == 0) g.id = db.projectDao().insertGoal(g);
            else db.projectDao().updateGoal(g);
            if (ms != null) {
                db.projectDao().deleteMilestonesFor(g.id);
                for (Milestone m : ms) {
                    m.id = 0;
                    m.goalId = g.id;
                }
                db.projectDao().insertMilestones(ms);
            }
            Reminders.scheduleGoal(app, g);
        });
    }

    public void deleteGoal(final Goal g) {
        io.execute(() -> {
            Reminders.cancelGoal(app, g.id);
            db.projectDao().deleteGoal(g);
        });
    }

    // ---------------- habits ----------------
    public void saveHabit(final Habit h) {
        io.execute(() -> {
            if (h.createdAt == 0) h.createdAt = System.currentTimeMillis();
            if (h.id == 0) h.id = db.habitDao().insertHabit(h);
            else db.habitDao().updateHabit(h);
        });
    }

    public void deleteHabit(final Habit h) {
        io.execute(() -> db.habitDao().deleteHabit(h));
    }

    public void toggleHabit(final long habitId, final long day) {
        io.execute(() -> {
            if (db.habitDao().countLog(habitId, day) > 0) {
                db.habitDao().deleteLog(habitId, day);
            } else {
                HabitLog log = new HabitLog();
                log.habitId = habitId;
                log.day = day;
                db.habitDao().insertLog(log);
            }
        });
    }

    // ---------------- notes & vocabulary ----------------
    public void saveNote(final Note n) {
        io.execute(() -> {
            n.updatedAt = System.currentTimeMillis();
            if (n.id == 0) n.id = db.noteDao().insertNote(n);
            else db.noteDao().updateNote(n);
        });
    }

    public void deleteNote(final Note n) {
        io.execute(() -> db.noteDao().deleteNote(n));
    }

    public void saveVocab(final Vocab v) {
        io.execute(() -> {
            if (v.createdAt == 0) v.createdAt = System.currentTimeMillis();
            if (v.id == 0) v.id = db.noteDao().insertVocab(v);
            else db.noteDao().updateVocab(v);
        });
    }

    public void deleteVocab(final Vocab v) {
        io.execute(() -> db.noteDao().deleteVocab(v));
    }

    /** Applies a flashcard rating (see {@link Srs}) and stores the result. */
    public void reviewVocab(Vocab v, int rating) {
        Vocab copy = v.copy();
        Srs.apply(copy, rating, System.currentTimeMillis());
        saveVocab(copy);
    }

    // ---------------- search ----------------
    public void search(String query, final SearchCallback callback) {
        final String like = "%" + query.trim() + "%";
        io.execute(() -> {
            SearchResult r = new SearchResult();
            r.tasks = db.taskDao().search(like);
            r.projects = db.projectDao().searchProjects(like);
            r.goals = db.projectDao().searchGoals(like);
            r.notes = db.noteDao().searchNotes(like);
            r.vocab = db.noteDao().searchVocab(like);
            r.subjects = db.studyDao().searchSubjects(like);
            main.post(() -> callback.onResult(r));
        });
    }

    // ---------------- maintenance ----------------
    public void resetAll(final Runnable onDone) {
        io.execute(() -> {
            db.clearAllTables();
            if (onDone != null) main.post(onDone);
        });
    }

    /** Re-creates alarms (they are lost when the device restarts). */
    public void rescheduleAll() {
        io.execute(() -> {
            for (Task t : db.taskDao().reminderCandidates()) Reminders.scheduleTask(app, t);
            for (Goal g : db.projectDao().goalsWithDeadline()) Reminders.scheduleGoal(app, g);
        });
    }

    /** Calendar helper kept here so tests can reason about repeat logic in one place. */
    static long nextOccurrence(long due, int repeat) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(due);
        c.add(Calendar.DAY_OF_YEAR, repeat == Task.REPEAT_DAILY ? 1 : 7);
        return c.getTimeInMillis();
    }
}
