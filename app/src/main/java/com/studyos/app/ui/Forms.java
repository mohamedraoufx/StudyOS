package com.studyos.app.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.studyos.app.R;
import com.studyos.app.data.DailyAllocation;
import com.studyos.app.data.Goal;
import com.studyos.app.data.Habit;
import com.studyos.app.data.HabitLog;
import com.studyos.app.data.Milestone;
import com.studyos.app.data.Note;
import com.studyos.app.data.Project;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Subtask;
import com.studyos.app.data.Task;
import com.studyos.app.data.Vocab;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** All create/edit dialogs of the app. Pass existing = null to create a new item. */
public final class Forms {
    public static final String[] PRIORITIES = {"Low", "Medium", "High", "Urgent"};
    public static final String[] STATUSES = {"To do", "In progress", "Completed"};
    private static final String[] REPEATS = {"None", "Daily", "Weekly"};
    private static final String[] REMINDER_LABELS = {
            "None", "At start time", "5 min before", "15 min before", "30 min before", "1 hour before", "1 day before"};
    private static final int[] REMINDER_VALUES = {-1, 0, 5, 15, 30, 60, 1440};

    private Forms() {}

    // ------------------------------------------------------------------ tasks
    public static void task(Context c, final MainViewModel vm, final Task existing) {
        final boolean isNew = existing == null;
        final Task w = isNew ? new Task() : existing.copy();
        Form f = new Form(c, isNew ? "New task" : "Edit task");
        final EditText title = f.text("Title", w.title, false, false);
        final EditText notes = f.text("Notes", w.notes, true, false);
        final Spinner priority = f.spinner("Priority", Arrays.asList(PRIORITIES), w.priority);
        final Spinner status = f.spinner("Status", Arrays.asList(STATUSES), w.status);

        final List<String> subjectNames = new ArrayList<>();
        subjectNames.add("(none)");
        for (Subject s : vm.studySubjects()) subjectNames.add(s.name);
        int si = subjectNames.indexOf(w.subject);
        if (si < 0 && !w.subject.isEmpty()) {
            subjectNames.add(w.subject);
            si = subjectNames.size() - 1;
        }
        final Spinner subject = f.spinner("Subject", subjectNames, Math.max(0, si));
        final EditText category = f.text("Category", w.category, false, false);
        final Form.DateField due = f.date("Due date", w.dueDate, true);
        final Form.TimeField start = f.time("Start time", w.startMinute, true);
        final EditText duration = f.text("Duration (minutes)", w.durationMin > 0 ? String.valueOf(w.durationMin) : "", false, true);
        final Spinner repeat = f.spinner("Repeat", Arrays.asList(REPEATS), w.repeat);

        int ri = 0;
        for (int i = 0; i < REMINDER_VALUES.length; i++) if (REMINDER_VALUES[i] == w.reminderMin) ri = i;
        final Spinner reminder = f.spinner("Reminder", Arrays.asList(REMINDER_LABELS), ri);

        final List<Project> projects = vm.projectSnapshot();
        List<String> pn = new ArrayList<>();
        pn.add("(none)");
        int pi = 0;
        for (int i = 0; i < projects.size(); i++) {
            pn.add(projects.get(i).name);
            if (w.projectId != null && w.projectId.longValue() == projects.get(i).id) pi = i + 1;
        }
        final Spinner project = f.spinner("Project", pn, pi);

        final List<Goal> goals = vm.goalSnapshot();
        List<String> gn = new ArrayList<>();
        gn.add("(none)");
        int gi = 0;
        for (int i = 0; i < goals.size(); i++) {
            gn.add(goals.get(i).title);
            if (w.goalId != null && w.goalId.longValue() == goals.get(i).id) gi = i + 1;
        }
        final Spinner goal = f.spinner("Goal", gn, gi);

        final ChecklistEditor subs = new ChecklistEditor(c, "Subtasks", "Add subtask");
        if (!isNew) {
            for (Subtask s : LiveUtil.nz(vm.subtasks.getValue())) {
                if (s.taskId == w.id) subs.addRow(s.title, s.done);
            }
        }
        f.add(subs.view());

        f.show("Save", () -> {
            String t = Form.str(title);
            if (t.isEmpty()) {
                title.setError("Required");
                return false;
            }
            w.title = t;
            w.notes = Form.str(notes);
            w.priority = priority.getSelectedItemPosition();
            w.status = status.getSelectedItemPosition();
            int sp = subject.getSelectedItemPosition();
            w.subject = sp <= 0 ? "" : subjectNames.get(sp);
            w.category = Form.str(category);
            w.dueDate = due.value;
            w.startMinute = start.minute;
            w.durationMin = Form.num(duration, 0);
            w.repeat = repeat.getSelectedItemPosition();
            w.reminderMin = REMINDER_VALUES[reminder.getSelectedItemPosition()];
            int pp = project.getSelectedItemPosition();
            w.projectId = pp <= 0 ? null : Long.valueOf(projects.get(pp - 1).id);
            int gp = goal.getSelectedItemPosition();
            w.goalId = gp <= 0 ? null : Long.valueOf(goals.get(gp - 1).id);
            if (w.status == Task.DONE && w.completedAt == 0) w.completedAt = System.currentTimeMillis();
            if (w.status != Task.DONE) w.completedAt = 0;
            List<Subtask> list = new ArrayList<>();
            List<String> texts = subs.texts();
            List<Boolean> dones = subs.dones();
            for (int i = 0; i < texts.size(); i++) {
                Subtask s = new Subtask();
                s.title = texts.get(i);
                s.done = dones.get(i);
                list.add(s);
            }
            vm.repo().saveTask(w, list);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteTask(existing); }
        });
    }

    // --------------------------------------------------------------- sessions
    public static void session(final Context c, final MainViewModel vm, final StudySession existing) {
        final boolean isNew = existing == null;
        final StudySession w = new StudySession();
        long now = System.currentTimeMillis();
        if (!isNew) {
            w.id = existing.id;
            w.startTime = existing.startTime;
            w.endTime = existing.endTime;
            w.source = existing.source;
            w.notes = existing.notes;
            w.projectId = existing.projectId;
            w.subjectName = existing.subjectName;
        }
        Form f = new Form(c, isNew ? "Log study session" : "Edit session");
        final List<Subject> subjects = vm.studySubjects();
        List<String> names = new ArrayList<>();
        int si = 0;
        for (int i = 0; i < subjects.size(); i++) {
            names.add(subjects.get(i).name);
            if (subjects.get(i).name.equals(w.subjectName)) si = i;
        }
        if (names.isEmpty()) names.add("Study");
        final Spinner subject = f.spinner("Subject", names, si);

        final List<Project> projects = vm.projectSnapshot();
        List<String> pn = new ArrayList<>();
        pn.add("(none)");
        int pi = 0;
        for (int i = 0; i < projects.size(); i++) {
            pn.add(projects.get(i).name);
            if (w.projectId != null && w.projectId.longValue() == projects.get(i).id) pi = i + 1;
        }
        final Spinner project = f.spinner("Project", pn, pi);

        long day = isNew ? TimeUtil.startOfDay(now) : TimeUtil.startOfDay(w.startTime);
        int nowMin = TimeUtil.minuteOfDay(now);
        int startMin = isNew ? Math.max(0, nowMin - 30) : TimeUtil.minuteOfDay(w.startTime);
        int endMin = isNew ? nowMin : TimeUtil.minuteOfDay(w.endTime);
        final Form.DateField date = f.date("Date", day, false);
        final Form.TimeField start = f.time("Start time", startMin, false);
        final Form.TimeField end = f.time("End time", endMin, false);
        final EditText notes = f.text("Notes", w.notes, true, false);

        f.show("Save", () -> {
            if (end.minute <= start.minute) {
                Ui.toast(c, "End time must be after the start time");
                return false;
            }
            int sp = subject.getSelectedItemPosition();
            if (subjects.isEmpty()) {
                w.subjectName = "Study";
                w.category = "";
            } else {
                w.subjectName = subjects.get(sp).name;
                w.category = subjects.get(sp).category;
            }
            int pp = project.getSelectedItemPosition();
            w.projectId = pp <= 0 ? null : Long.valueOf(projects.get(pp - 1).id);
            w.startTime = date.value + start.minute * 60_000L;
            w.endTime = date.value + end.minute * 60_000L;
            w.durationSec = (end.minute - start.minute) * 60L;
            w.notes = Form.str(notes);
            vm.repo().saveSession(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteSession(existing); }
        });
    }

    // --------------------------------------------------------------- subjects
    private static final String[] SUBJECT_TYPES = {"Software Engineering", "Other", "Language"};

    public static void subject(Context c, final MainViewModel vm, final Subject existing) {
        final boolean isNew = existing == null;
        final Subject w = isNew ? Subject.of("", Subject.SE, "Software Engineering", 0)
                : Subject.of(existing.name, existing.category, existing.groupName, existing.level);
        if (!isNew) w.id = existing.id;
        Form f = new Form(c, isNew ? "New subject" : "Edit subject");
        final EditText name = f.text("Name", w.name, false, false);
        int ti = Subject.OTHER.equals(w.category) ? 1 : Subject.LANG.equals(w.category) ? 2 : 0;
        final Spinner type = f.spinner("Type", Arrays.asList(SUBJECT_TYPES), ti);
        final SeekBar level = f.seek("Level", w.level);
        f.show("Save", () -> {
            String n = Form.str(name);
            if (n.isEmpty()) {
                name.setError("Required");
                return false;
            }
            w.name = n;
            int p = type.getSelectedItemPosition();
            w.category = p == 1 ? Subject.OTHER : p == 2 ? Subject.LANG : Subject.SE;
            w.groupName = p == 1 ? "Other" : p == 2 ? n : "Software Engineering";
            w.level = level.getProgress();
            vm.repo().saveSubject(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteSubject(existing); }
        });
    }

    public static void skill(Context c, final MainViewModel vm, final String language, final Subject existing) {
        final boolean isNew = existing == null;
        final Subject w = isNew ? Subject.of("", Subject.SKILL, language, 0)
                : Subject.of(existing.name, existing.category, existing.groupName, existing.level);
        if (!isNew) w.id = existing.id;
        Form f = new Form(c, isNew ? "New " + language + " skill" : "Edit skill");
        final EditText name = f.text("Skill (e.g. Listening)", w.name, false, false);
        final SeekBar level = f.seek("Level", w.level);
        f.show("Save", () -> {
            String n = Form.str(name);
            if (n.isEmpty()) {
                name.setError("Required");
                return false;
            }
            w.name = n;
            w.level = level.getProgress();
            vm.repo().saveSubject(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteSubject(existing); }
        });
    }

    // --------------------------------------------------------------- projects
    public static void project(Context c, final MainViewModel vm, final Project existing) {
        final boolean isNew = existing == null;
        final Project w = new Project();
        if (!isNew) {
            w.id = existing.id;
            w.name = existing.name;
            w.notes = existing.notes;
            w.deadline = existing.deadline;
            w.createdAt = existing.createdAt;
        }
        Form f = new Form(c, isNew ? "New project" : "Edit project");
        final EditText name = f.text("Name", w.name, false, false);
        final EditText notes = f.text("Description / notes", w.notes, true, false);
        final Form.DateField deadline = f.date("Deadline", w.deadline, true);
        if (!isNew) {
            StringBuilder sb = new StringBuilder();
            for (Task t : LiveUtil.nz(vm.tasks.getValue())) {
                if (t.projectId != null && t.projectId.longValue() == w.id) {
                    sb.append(t.status == Task.DONE ? "\u2611 " : "\u2610 ").append(t.title).append('\n');
                }
            }
            if (sb.length() > 0) {
                f.add(Ui.text(c, "Tasks in this project", 12, false, R.color.so_text2));
                f.add(Ui.text(c, sb.toString().trim(), 14, false, R.color.so_text));
            }
        }
        f.show("Save", () -> {
            String n = Form.str(name);
            if (n.isEmpty()) {
                name.setError("Required");
                return false;
            }
            w.name = n;
            w.notes = Form.str(notes);
            w.deadline = deadline.value;
            vm.repo().saveProject(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteProject(existing); }
        });
    }

    // ------------------------------------------------------------------ goals
    public static void goal(Context c, final MainViewModel vm, final Goal existing) {
        final boolean isNew = existing == null;
        final Goal w = new Goal();
        if (!isNew) {
            w.id = existing.id;
            w.title = existing.title;
            w.notes = existing.notes;
            w.deadline = existing.deadline;
            w.createdAt = existing.createdAt;
        }
        Form f = new Form(c, isNew ? "New goal" : "Edit goal");
        final EditText title = f.text("Goal", w.title, false, false);
        final EditText notes = f.text("Notes", w.notes, true, false);
        final Form.DateField deadline = f.date("Deadline", w.deadline, true);
        final ChecklistEditor ms = new ChecklistEditor(c, "Milestones", "Add milestone");
        if (!isNew) {
            for (Milestone m : LiveUtil.nz(vm.milestones.getValue())) {
                if (m.goalId == w.id) ms.addRow(m.title, m.done);
            }
        }
        f.add(ms.view());
        f.show("Save", () -> {
            String t = Form.str(title);
            if (t.isEmpty()) {
                title.setError("Required");
                return false;
            }
            w.title = t;
            w.notes = Form.str(notes);
            w.deadline = deadline.value;
            List<Milestone> list = new ArrayList<>();
            List<String> texts = ms.texts();
            List<Boolean> dones = ms.dones();
            for (int i = 0; i < texts.size(); i++) {
                Milestone m = new Milestone();
                m.title = texts.get(i);
                m.done = dones.get(i);
                list.add(m);
            }
            vm.repo().saveGoal(w, list);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteGoal(existing); }
        });
    }

    // ----------------------------------------------------------------- habits
    public static void habit(final Context c, final MainViewModel vm, final Habit existing) {
        final boolean isNew = existing == null;
        final Habit w = new Habit();
        if (!isNew) {
            w.id = existing.id;
            w.name = existing.name;
            w.weekly = existing.weekly;
            w.createdAt = existing.createdAt;
        }
        Form f = new Form(c, isNew ? "New habit" : "Edit habit");
        final EditText name = f.text("Habit", w.name, false, false);
        final Spinner freq = f.spinner("Frequency", Arrays.asList("Daily", "Weekly"), w.weekly ? 1 : 0);
        if (!isNew) {
            f.add(Ui.text(c, "Last 14 days (tap a day to toggle)", 12, false, R.color.so_text2));
            final Set<Long> done = new HashSet<>();
            for (HabitLog l : LiveUtil.nz(vm.habitLogs.getValue())) {
                if (l.habitId == w.id) done.add(l.day);
            }
            HorizontalScrollView hs = new HorizontalScrollView(c);
            LinearLayout strip = Ui.hbox(c);
            long now = System.currentTimeMillis();
            long todayNo = TimeUtil.dayNumber(now);
            for (int i = 13; i >= 0; i--) {
                final long day = todayNo - i;
                final TextView tv = Ui.text(c, TimeUtil.format(now - i * TimeUtil.DAY, "d"), 13, true, R.color.so_text);
                tv.setGravity(Gravity.CENTER);
                tv.setMinWidth(Ui.dp(34));
                tv.setMinHeight(Ui.dp(34));
                paintDay(c, tv, done.contains(day));
                tv.setOnClickListener((View v) -> {
                    if (!done.remove(day)) done.add(day);
                    paintDay(c, tv, done.contains(day));
                    vm.repo().toggleHabit(w.id, day);
                });
                strip.addView(tv, Ui.lp(Ui.WRAP, Ui.WRAP, 2, 4, 2, 4));
            }
            hs.addView(strip);
            f.add(hs);
        }
        f.show("Save", () -> {
            String n = Form.str(name);
            if (n.isEmpty()) {
                name.setError("Required");
                return false;
            }
            w.name = n;
            w.weekly = freq.getSelectedItemPosition() == 1;
            vm.repo().saveHabit(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteHabit(existing); }
        });
    }

    private static void paintDay(Context c, TextView tv, boolean done) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(Ui.dp(8));
        g.setColor(Ui.col(c, done ? R.color.so_green : R.color.so_divider));
        tv.setBackground(g);
        tv.setTextColor(Ui.col(c, done ? R.color.so_on_primary : R.color.so_text));
    }

    // ------------------------------------------------------------------ notes
    private static final String[] LINK_TYPES = {"None", "Subject", "Project", "Goal", "Language"};

    private static List<String> linkTargets(MainViewModel vm, String type) {
        List<String> out = new ArrayList<>();
        if ("Subject".equals(type)) {
            for (Subject s : vm.studySubjects()) out.add(s.name);
        } else if ("Project".equals(type)) {
            for (Project p : vm.projectSnapshot()) out.add(p.name);
        } else if ("Goal".equals(type)) {
            for (Goal g : vm.goalSnapshot()) out.add(g.title);
        } else if ("Language".equals(type)) {
            for (Subject s : vm.languages()) out.add(s.name);
        }
        if (out.isEmpty()) out.add("-");
        return out;
    }

    public static void note(Context c, final MainViewModel vm, final Note existing, String presetType, String presetName) {
        final boolean isNew = existing == null;
        final Note w = new Note();
        if (!isNew) {
            w.id = existing.id;
            w.title = existing.title;
            w.body = existing.body;
            w.category = existing.category;
            w.linkType = existing.linkType;
            w.linkName = existing.linkName;
        } else if (presetType != null) {
            w.linkType = presetType;
            w.linkName = presetName == null ? "" : presetName;
        }
        Form f = new Form(c, isNew ? "New note" : "Edit note");
        final EditText title = f.text("Title", w.title, false, false);
        final EditText body = f.text("Note", w.body, true, false);
        final EditText category = f.text("Category", w.category, false, false);
        int ti = Math.max(0, Arrays.asList(LINK_TYPES).indexOf(w.linkType));
        final Spinner type = f.spinner("Link to", Arrays.asList(LINK_TYPES), ti);
        final Spinner target = f.spinner("Linked item", linkTargets(vm, LINK_TYPES[ti]), 0);
        final String[] pending = {w.linkName};
        final List<String>[] current = new List[]{linkTargets(vm, LINK_TYPES[ti])};
        Form.fill(target, current[0], Math.max(0, current[0].indexOf(pending[0])));
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                List<String> names = linkTargets(vm, LINK_TYPES[position]);
                current[0] = names;
                Form.fill(target, names, Math.max(0, names.indexOf(pending[0])));
                pending[0] = "";
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        f.show("Save", () -> {
            String t = Form.str(title);
            if (t.isEmpty()) {
                title.setError("Required");
                return false;
            }
            w.title = t;
            w.body = Form.str(body);
            w.category = Form.str(category);
            int tp = type.getSelectedItemPosition();
            w.linkType = tp == 0 ? Note.LINK_NONE : LINK_TYPES[tp];
            String chosen = (String) target.getSelectedItem();
            w.linkName = (tp == 0 || chosen == null || "-".equals(chosen)) ? "" : chosen;
            vm.repo().saveNote(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteNote(existing); }
        });
    }

    // ------------------------------------------------------------- vocabulary
    public static void vocab(Context c, final MainViewModel vm, final Vocab existing, String presetLanguage) {
        final boolean isNew = existing == null;
        final Vocab w = isNew ? new Vocab() : existing.copy();
        if (isNew && presetLanguage != null) w.language = presetLanguage;
        Form f = new Form(c, isNew ? "New word" : "Edit word");
        final List<Subject> langs = vm.languages();
        List<String> names = new ArrayList<>();
        int li = 0;
        for (int i = 0; i < langs.size(); i++) {
            names.add(langs.get(i).name);
            if (langs.get(i).name.equals(w.language)) li = i;
        }
        if (names.isEmpty()) names.add(w.language.isEmpty() ? "English" : w.language);
        final Spinner language = f.spinner("Language", names, li);
        final EditText word = f.text("Word / phrase", w.word, false, false);
        final EditText translation = f.text("Translation / meaning", w.translation, false, false);
        final EditText example = f.text("Example sentence", w.example, true, false);
        final Spinner difficulty = f.spinner("Difficulty", Arrays.asList("Easy", "Medium", "Hard"), Math.max(0, w.difficulty - 1));
        final EditText category = f.text("Category", w.category, false, false);
        final CheckBox learned = f.check("Learned", w.learned);
        f.show("Save", () -> {
            String wd = Form.str(word);
            if (wd.isEmpty()) {
                word.setError("Required");
                return false;
            }
            w.language = (String) language.getSelectedItem();
            w.word = wd;
            w.translation = Form.str(translation);
            w.example = Form.str(example);
            w.difficulty = difficulty.getSelectedItemPosition() + 1;
            w.category = Form.str(category);
            w.learned = learned.isChecked();
            vm.repo().saveVocab(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteVocab(existing); }
        });
    }

    // ------------------------------------------------------------ allocations
    public static void allocation(Context c, final MainViewModel vm, final DailyAllocation existing) {
        final boolean isNew = existing == null;
        final DailyAllocation w = new DailyAllocation();
        if (!isNew) {
            w.id = existing.id;
            w.name = existing.name;
            w.minutes = existing.minutes;
        }
        Form f = new Form(c, isNew ? "New allocation" : "Edit allocation");
        final List<Subject> subjects = vm.studySubjects();
        final List<String> names = new ArrayList<>();
        int si = 0;
        for (int i = 0; i < subjects.size(); i++) {
            names.add(subjects.get(i).name);
            if (subjects.get(i).name.equals(w.name)) si = i;
        }
        if (names.isEmpty()) names.add("Study");
        final Spinner subject = f.spinner("Subject", names, si);
        final EditText minutes = f.text("Minutes per day", w.minutes > 0 ? String.valueOf(w.minutes) : "", false, true);
        f.show("Save", () -> {
            int m = Form.num(minutes, 0);
            if (m <= 0) {
                minutes.setError("Enter minutes");
                return false;
            }
            w.name = names.get(subject.getSelectedItemPosition());
            w.minutes = m;
            vm.repo().saveAllocation(w);
            return true;
        }, isNew ? null : new Runnable() {
            @Override public void run() { vm.repo().deleteAllocation(existing); }
        });
    }
}
