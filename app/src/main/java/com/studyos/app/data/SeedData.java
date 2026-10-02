package com.studyos.app.data;

import com.studyos.app.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

/** Sample data inserted on first launch so the app looks alive. Everything can be edited or deleted. */
public final class SeedData {
    private SeedData() {}

    private static final String[] SE_TOPICS = {
            "Java", "OOP", "Data Structures", "Algorithms", "SQL", "Databases", "JDBC", "JPA", "Hibernate",
            "Spring", "Spring Boot", "REST APIs", "Spring Security", "Testing", "Git", "GitHub", "Linux",
            "Docker", "Kubernetes", "System Design", "Software Architecture", "Design Patterns",
            "Distributed Systems", "Microservices", "Cloud", "DevOps"};
    private static final int[] SE_LEVELS = {
            40, 35, 30, 25, 30, 25, 10, 5, 5, 10, 5, 10, 0, 10, 30, 25, 20, 5, 0, 5, 5, 10, 0, 0, 0, 0};

    private static final String[] LANGS = {"English", "French", "German", "Japanese"};
    private static final String[][] LANG_SKILLS = {
            {"Vocabulary", "Grammar", "Listening", "Speaking", "Reading", "Writing"},
            {"Vocabulary", "Grammar", "Listening", "Speaking", "Reading", "Writing"},
            {"Vocabulary", "Grammar", "Listening", "Speaking", "Reading", "Writing"},
            {"Hiragana", "Katakana", "Kanji", "Vocabulary", "Grammar", "Listening", "Speaking", "Reading", "Writing"}};
    private static final int[][] LANG_LEVELS = {
            {60, 55, 50, 40, 60, 45},
            {35, 30, 25, 15, 30, 20},
            {20, 15, 10, 5, 15, 10},
            {70, 50, 10, 15, 10, 10, 5, 10, 10}};

    public static void run(final AppDatabase db) {
        db.runInTransaction(() -> insertAll(db));
    }

    private static void insertAll(AppDatabase db) {
        long now = System.currentTimeMillis();
        long today = TimeUtil.startOfDay(now);
        long day = TimeUtil.DAY;

        // ---- subjects ----
        List<Subject> subjects = new ArrayList<>();
        for (int i = 0; i < SE_TOPICS.length; i++) {
            subjects.add(Subject.of(SE_TOPICS[i], Subject.SE, "Software Engineering", SE_LEVELS[i]));
        }
        subjects.add(Subject.of("Projects", Subject.OTHER, "Other", 0));
        for (int l = 0; l < LANGS.length; l++) {
            subjects.add(Subject.of(LANGS[l], Subject.LANG, LANGS[l], 0));
            for (int k = 0; k < LANG_SKILLS[l].length; k++) {
                subjects.add(Subject.of(LANG_SKILLS[l][k], Subject.SKILL, LANGS[l], LANG_LEVELS[l][k]));
            }
        }
        db.studyDao().insertSubjects(subjects);

        // ---- daily allocation (6h total) ----
        String[] allocNames = {"Java", "Projects", "Algorithms", "English", "French", "German", "Japanese"};
        int[] allocMinutes = {120, 90, 45, 45, 30, 15, 15};
        List<DailyAllocation> allocations = new ArrayList<>();
        for (int i = 0; i < allocNames.length; i++) {
            DailyAllocation a = new DailyAllocation();
            a.name = allocNames[i];
            a.minutes = allocMinutes[i];
            allocations.add(a);
        }
        db.studyDao().insertAllocations(allocations);

        // ---- project & goals ----
        Project studyOs = new Project();
        studyOs.name = "Study OS";
        studyOs.notes = "My personal study manager app.";
        studyOs.deadline = today + 60 * day;
        studyOs.createdAt = now;
        long projectId = db.projectDao().insertProject(studyOs);

        Goal pro = new Goal();
        pro.title = "Become Professional Software Engineer";
        pro.notes = "Follow the roadmap step by step and build real projects.";
        pro.deadline = today + 365 * day;
        pro.createdAt = now;
        long proId = db.projectDao().insertGoal(pro);
        String[] steps = {"Java", "OOP", "Algorithms", "SQL", "Spring Boot", "Security", "Testing",
                "System Design", "Docker", "Cloud"};
        List<Milestone> ms = new ArrayList<>();
        for (int i = 0; i < steps.length; i++) {
            Milestone m = new Milestone();
            m.goalId = proId;
            m.title = steps[i];
            m.done = i < 1;
            ms.add(m);
        }
        db.projectDao().insertMilestones(ms);

        Goal english = new Goal();
        english.title = "Reach B2 in English";
        english.notes = "Grammar, reading and speaking practice every week.";
        english.deadline = today + 180 * day;
        english.createdAt = now;
        long engId = db.projectDao().insertGoal(english);
        String[] engSteps = {"Finish grammar course", "Read 5 books", "Weekly speaking practice", "Pass a mock exam"};
        List<Milestone> ems = new ArrayList<>();
        for (int i = 0; i < engSteps.length; i++) {
            Milestone m = new Milestone();
            m.goalId = engId;
            m.title = engSteps[i];
            m.done = i == 0;
            ems.add(m);
        }
        db.projectDao().insertMilestones(ems);

        // ---- tasks ----
        Task t1 = task("Finish OOP chapter exercises", Task.HIGH, Task.TODO, "Java", "Study", today, 10 * 60, 60, 15, now);
        t1.goalId = proId;
        long t1Id = db.taskDao().insert(t1);
        String[] subs = {"Read the chapter", "Do exercises 1-5", "Write a short summary"};
        for (int i = 0; i < subs.length; i++) {
            Subtask s = new Subtask();
            s.taskId = t1Id;
            s.title = subs[i];
            s.done = i == 0;
            db.taskDao().insertSubtask(s);
        }
        db.taskDao().insert(task("Solve 3 binary search problems", Task.MEDIUM, Task.TODO, "Algorithms", "Practice", today, 14 * 60, 45, 10, now));
        db.taskDao().insert(task("Review 20 French flashcards", Task.MEDIUM, Task.TODO, "French", "Vocabulary", today, 19 * 60, 20, -1, now));
        db.taskDao().insert(task("Write SQL joins cheat sheet", Task.LOW, Task.TODO, "SQL", "Notes", today + day, -1, 40, -1, now));
        Task hira = task("Practice Hiragana writing", Task.MEDIUM, Task.TODO, "Japanese", "Writing", today + day, 8 * 60, 15, -1, now);
        hira.repeat = Task.REPEAT_DAILY;
        db.taskDao().insert(hira);
        db.taskDao().insert(task("Read the Spring Boot getting started guide", Task.HIGH, Task.TODO, "Spring Boot", "Reading", today + 2 * day, -1, 60, -1, now));
        Task schema = task("Design the Study OS database schema", Task.URGENT, Task.IN_PROGRESS, "Projects", "Development", today + 3 * day, -1, 90, -1, now);
        schema.projectId = projectId;
        db.taskDao().insert(schema);
        db.taskDao().insert(task("German listening practice (podcast)", Task.LOW, Task.TODO, "German", "Listening", today + 4 * day, -1, 20, -1, now));
        Task done = task("Set up the GitHub repository", Task.MEDIUM, Task.DONE, "GitHub", "Setup", today, -1, 20, -1, now);
        done.completedAt = now;
        done.projectId = projectId;
        db.taskDao().insert(done);

        // ---- study sessions (last 14 days) ----
        String[] names = {"Java", "Algorithms", "English", "French", "German", "Japanese", "Projects"};
        String[] cats = {Subject.SE, Subject.SE, Subject.LANG, Subject.LANG, Subject.LANG, Subject.LANG, Subject.OTHER};
        int[] base = {120, 45, 45, 30, 15, 15, 90};
        for (int d = 0; d < 14; d++) {
            for (int i = 0; i < names.length; i++) {
                if (d == 0 && i != 0 && i != 2) continue;
                if (d > 0 && (d + i) % 5 == 4) continue;
                int minutes = d == 0 ? (i == 0 ? 60 : 30) : Math.max(10, base[i] - ((d * (i + 2)) % 4) * 5);
                StudySession s = new StudySession();
                s.subjectName = names[i];
                s.category = cats[i];
                s.startTime = d == 0 ? today + (i * 35 + 1) * 60_000L : today - d * day + (8 * 60 + i * 70) * 60_000L;
                s.durationSec = minutes * 60L;
                s.endTime = s.startTime + s.durationSec * 1000L;
                s.source = (i % 2 == 0) ? StudySession.SRC_POMODORO : StudySession.SRC_MANUAL;
                if (i == 6) s.projectId = projectId;
                db.studyDao().insertSession(s);
            }
        }

        // ---- habits ----
        String[] habitNames = {"Study Java", "English", "French", "German", "Japanese", "Algorithms", "Weekly review"};
        long todayNo = TimeUtil.dayNumber(now);
        for (int i = 0; i < habitNames.length; i++) {
            Habit h = new Habit();
            h.name = habitNames[i];
            h.weekly = i == 6;
            h.createdAt = now;
            long hid = db.habitDao().insertHabit(h);
            List<HabitLog> logs = new ArrayList<>();
            for (int d = 0; d < 14; d++) {
                boolean hit = h.weekly ? (d == 3 || d == 10) : ((d + i) % 3 != 0 || d == 0 && i < 3);
                if (!hit) continue;
                HabitLog l = new HabitLog();
                l.habitId = hid;
                l.day = todayNo - d;
                logs.add(l);
            }
            db.habitDao().insertLogs(logs);
        }

        // ---- vocabulary ----
        List<Vocab> words = new ArrayList<>();
        words.add(vocab("English", "ubiquitous", "present everywhere", "Smartphones are ubiquitous today.", 3, "Adjectives"));
        words.add(vocab("English", "to overcome", "to succeed in dealing with a problem", "She overcame her fear of speaking.", 2, "Verbs"));
        words.add(vocab("English", "thorough", "complete and careful", "He did a thorough review of the code.", 2, "Adjectives"));
        words.add(vocab("French", "la bibliothèque", "the library", "Je travaille à la bibliothèque.", 1, "Places"));
        words.add(vocab("French", "apprendre", "to learn", "J'aime apprendre de nouvelles langues.", 1, "Verbs"));
        words.add(vocab("French", "un développeur", "a developer", "Mon frère est développeur.", 2, "Work"));
        words.add(vocab("German", "die Aufgabe", "the task", "Die Aufgabe ist schwierig.", 2, "Study"));
        words.add(vocab("German", "lernen", "to learn", "Ich lerne jeden Tag Deutsch.", 1, "Verbs"));
        words.add(vocab("German", "der Fortschritt", "the progress", "Ich mache Fortschritte.", 3, "Study"));
        words.add(vocab("Japanese", "ありがとう", "thank you", "ほんとうにありがとう。", 1, "Greetings"));
        words.add(vocab("Japanese", "勉強", "study", "毎日勉強します。", 2, "Study"));
        words.add(vocab("Japanese", "先生", "teacher", "先生は親切です。", 2, "People"));
        for (Vocab v : words) v.createdAt = now;
        db.noteDao().insertVocabs(words);

        // ---- notes ----
        Note n1 = new Note();
        n1.title = "Java: interface vs abstract class";
        n1.body = "Interfaces define a contract; abstract classes share state and behaviour. Prefer interfaces + composition.";
        n1.category = "Study notes";
        n1.linkType = Note.LINK_SUBJECT;
        n1.linkName = "Java";
        n1.updatedAt = now;
        db.noteDao().insertNote(n1);
        Note n2 = new Note();
        n2.title = "French: passé composé reminder";
        n2.body = "avoir/être + participe passé. Verbs of movement usually take être.";
        n2.category = "Grammar";
        n2.linkType = Note.LINK_LANGUAGE;
        n2.linkName = "French";
        n2.updatedAt = now - 3600_000L;
        db.noteDao().insertNote(n2);
    }

    private static Task task(String title, int priority, int status, String subject, String category,
                             long due, int startMinute, int durationMin, int reminderMin, long now) {
        Task t = new Task();
        t.title = title;
        t.priority = priority;
        t.status = status;
        t.subject = subject;
        t.category = category;
        t.dueDate = due;
        t.startMinute = startMinute;
        t.durationMin = durationMin;
        t.reminderMin = reminderMin;
        t.createdAt = now;
        return t;
    }

    private static Vocab vocab(String language, String word, String translation, String example,
                               int difficulty, String category) {
        Vocab v = new Vocab();
        v.language = language;
        v.word = word;
        v.translation = translation;
        v.example = example;
        v.difficulty = difficulty;
        v.category = category;
        return v;
    }
}
