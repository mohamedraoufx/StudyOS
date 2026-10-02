package com.studyos.app.focus;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.studyos.app.StudyOsApp;
import com.studyos.app.data.StudySession;
import com.studyos.app.notify.Notifier;
import com.studyos.app.notify.Reminders;
import com.studyos.app.util.Prefs;

/**
 * Process-wide Pomodoro timer. Lives outside the UI so navigating between screens does not
 * stop it. A completed (or skipped after 1+ minute) focus phase is saved as a study session.
 * All methods must be called on the main thread.
 */
public final class PomodoroManager {
    public static final class State {
        public final boolean focus;
        public final boolean running;
        public final long remainingMs;
        public final long totalMs;

        State(boolean focus, boolean running, long remainingMs, long totalMs) {
            this.focus = focus;
            this.running = running;
            this.remainingMs = remainingMs;
            this.totalMs = totalMs;
        }
    }

    private static PomodoroManager instance;

    public static synchronized PomodoroManager get(Context c) {
        if (instance == null) instance = new PomodoroManager(c.getApplicationContext());
        return instance;
    }

    private final Context app;
    private final Prefs prefs;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final MutableLiveData<State> state = new MutableLiveData<>();

    private boolean focus = true;
    private boolean running = false;
    private long remainingMs;
    private long endElapsed;
    private long startedWall = 0;

    private String subject = "Study";
    private String category = "";
    private Long projectId = null;

    private final Runnable tick = new Runnable() {
        @Override public void run() {
            long left = endElapsed - SystemClock.elapsedRealtime();
            if (left <= 0) {
                finishPhase(true);
            } else {
                remainingMs = left;
                publish();
                handler.postDelayed(this, 250);
            }
        }
    };

    private PomodoroManager(Context app) {
        this.app = app;
        this.prefs = new Prefs(app);
        this.remainingMs = phaseMs();
        publish();
    }

    public LiveData<State> state() { return state; }

    public void setTarget(String subject, String category, Long projectId) {
        this.subject = subject;
        this.category = category;
        this.projectId = projectId;
    }

    private long phaseMs() {
        return (focus ? prefs.focusMin() : prefs.breakMin()) * 60_000L;
    }

    private void publish() {
        state.setValue(new State(focus, running, remainingMs, phaseMs()));
    }

    public void start() {
        if (running) return;
        if (remainingMs <= 0) remainingMs = phaseMs();
        if (focus && startedWall == 0) startedWall = System.currentTimeMillis();
        running = true;
        endElapsed = SystemClock.elapsedRealtime() + remainingMs;
        Reminders.scheduleFocusEnd(app, System.currentTimeMillis() + remainingMs, focus);
        handler.removeCallbacks(tick);
        handler.post(tick);
        publish();
    }

    public void pause() {
        if (!running) return;
        remainingMs = Math.max(0, endElapsed - SystemClock.elapsedRealtime());
        running = false;
        handler.removeCallbacks(tick);
        Reminders.cancelFocusEnd(app);
        publish();
    }

    public void reset() {
        handler.removeCallbacks(tick);
        Reminders.cancelFocusEnd(app);
        running = false;
        startedWall = 0;
        remainingMs = phaseMs();
        publish();
    }

    /** Ends the current phase early. A focus phase of at least one minute is still recorded. */
    public void skip() {
        finishPhase(false);
    }

    /** Called after the user changes durations in settings. */
    public void onSettingsChanged() {
        if (!running && (remainingMs == 0 || startedWall == 0)) {
            remainingMs = phaseMs();
            publish();
        }
    }

    private void finishPhase(boolean completed) {
        handler.removeCallbacks(tick);
        Reminders.cancelFocusEnd(app);
        long total = phaseMs();
        long left = completed ? 0 : (running ? Math.max(0, endElapsed - SystemClock.elapsedRealtime()) : remainingMs);
        long doneMs = total - left;
        long now = System.currentTimeMillis();

        if (focus && doneMs >= 60_000L) {
            StudySession s = new StudySession();
            s.subjectName = subject;
            s.category = category;
            s.projectId = projectId;
            s.endTime = now;
            s.startTime = startedWall > 0 ? startedWall : now - doneMs;
            s.durationSec = doneMs / 1000;
            s.source = StudySession.SRC_POMODORO;
            StudyOsApp.repo(app).saveSession(s);
        }
        if (completed) {
            Notifier.show(app, Reminders.focusNotificationId(), Notifier.CH_FOCUS,
                    focus ? "Focus session finished" : "Break is over",
                    focus ? "Saved to your study log. Take a short break." : "Ready for the next focus session?");
        }
        focus = !focus;
        running = false;
        startedWall = 0;
        remainingMs = phaseMs();
        publish();
    }
}
