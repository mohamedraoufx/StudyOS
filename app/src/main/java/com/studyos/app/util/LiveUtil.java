package com.studyos.app.util;

import android.os.Handler;
import android.os.Looper;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;

import java.util.Collections;
import java.util.List;

public final class LiveUtil {
    private LiveUtil() {}

    public static <T> List<T> nz(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }

    /** Runs r (coalesced) whenever any of the sources changes while the owner is alive. */
    public static void observeAll(final LifecycleOwner owner, final Runnable r, LiveData<?>... sources) {
        final Handler handler = new Handler(Looper.getMainLooper());
        final Runnable run = new Runnable() {
            @Override public void run() {
                if (owner.getLifecycle().getCurrentState() != Lifecycle.State.DESTROYED) r.run();
            }
        };
        for (LiveData<?> source : sources) {
            source.observe(owner, x -> {
                handler.removeCallbacks(run);
                handler.postDelayed(run, 50);
            });
        }
    }
}
