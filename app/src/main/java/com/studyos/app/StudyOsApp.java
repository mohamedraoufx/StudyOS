package com.studyos.app;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

import com.studyos.app.data.AppDatabase;
import com.studyos.app.data.Repository;
import com.studyos.app.data.SeedData;
import com.studyos.app.notify.Notifier;
import com.studyos.app.notify.Reminders;
import com.studyos.app.util.Prefs;

public class StudyOsApp extends Application {
    private Repository repository;

    @Override
    public void onCreate() {
        super.onCreate();
        final Prefs prefs = new Prefs(this);
        AppCompatDelegate.setDefaultNightMode(prefs.nightMode());
        Notifier.createChannels(this);
        repository = new Repository(this);
        repository.io(() -> {
            if (!prefs.seeded()) {
                SeedData.run(AppDatabase.get(StudyOsApp.this));
                prefs.setSeeded();
            }
            repository.rescheduleAll();
        });
        Reminders.scheduleDaily(this);
    }

    public Repository repository() {
        return repository;
    }

    public static Repository repo(Context context) {
        return ((StudyOsApp) context.getApplicationContext()).repository();
    }
}
