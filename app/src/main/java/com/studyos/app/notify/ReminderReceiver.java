package com.studyos.app.notify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String channel = intent.getStringExtra(Reminders.EXTRA_CHANNEL);
        String title = intent.getStringExtra(Reminders.EXTRA_TITLE);
        String text = intent.getStringExtra(Reminders.EXTRA_TEXT);
        int id = intent.getIntExtra(Reminders.EXTRA_ID, 1);
        Notifier.show(context, id, channel == null ? Notifier.CH_REMINDERS : channel,
                title == null ? "Study OS" : title, text == null ? "" : text);
        if (Reminders.KIND_DAILY.equals(intent.getStringExtra(Reminders.EXTRA_KIND))) {
            Reminders.scheduleDaily(context);
        }
    }
}
