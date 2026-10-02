package com.studyos.app.ui;

import android.app.TimePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.studyos.app.R;
import com.studyos.app.data.BackupManager;
import com.studyos.app.data.DailyAllocation;
import com.studyos.app.focus.PomodoroManager;
import com.studyos.app.notify.Reminders;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Prefs;
import com.studyos.app.util.TimeUtil;

import java.util.List;

public class SettingsFragment extends Fragment {
    private static final String[] THEMES = {"System default", "Light", "Dark"};

    private MainViewModel vm;
    private Prefs prefs;
    private LinearLayout content;

    private final ActivityResultLauncher<String> exportLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/octet-stream"), uri -> {
                if (uri != null) doExport(uri);
            });
    private final ActivityResultLauncher<String[]> importLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) confirmImport(uri);
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        prefs = new Prefs(c);
        content = Ui.vbox(c);
        content.setPadding(0, Ui.dp(12), 0, Ui.dp(24));
        LiveUtil.observeAll(getViewLifecycleOwner(), this::render, vm.allocations);
        render();
        return Ui.scroll(c, content);
    }

    private void section(String name) {
        content.addView(Ui.section(requireContext(), name), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 16, 16, 4));
    }

    private void item(String title, String sub, String meta, final Runnable action) {
        RowView r = new RowView(requireContext());
        r.texts(title, sub, meta);
        r.setOnClickListener(v -> action.run());
        content.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP, 12, 4, 12, 4));
    }

    private void render() {
        if (!isAdded() || content == null) return;
        final Context c = requireContext();
        content.removeAllViews();
        content.addView(Ui.title(c, "Settings"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 4));

        section("Appearance");
        item("Theme", "Dark / light mode", THEMES[prefs.theme()], () ->
                new MaterialAlertDialogBuilder(c).setTitle("Theme")
                        .setSingleChoiceItems(THEMES, prefs.theme(), (d, which) -> {
                            prefs.setTheme(which);
                            d.dismiss();
                            AppCompatDelegate.setDefaultNightMode(prefs.nightMode());
                        }).show());

        section("Daily goal");
        item("Daily study goal", "Tap to change", TimeUtil.duration(prefs.dailyGoalMin() * 60L), this::editGoal);
        List<DailyAllocation> allocations = LiveUtil.nz(vm.allocations.getValue());
        int allocated = 0;
        for (DailyAllocation a : allocations) allocated += a.minutes;
        content.addView(Ui.text(c, "Breakdown per subject \u2022 allocated " + TimeUtil.duration(allocated * 60L)
                + " of " + TimeUtil.duration(prefs.dailyGoalMin() * 60L), 12, false, R.color.so_text2),
                Ui.lp(Ui.MATCH, Ui.WRAP, 16, 4, 16, 2));
        for (final DailyAllocation a : allocations) {
            item(a.name, "", TimeUtil.duration(a.minutes * 60L), () -> Forms.allocation(c, vm, a));
        }
        content.addView(Ui.button(c, "Add subject allocation", false, v -> Forms.allocation(c, vm, null)),
                Ui.lp(Ui.WRAP, Ui.WRAP, 12, 4, 12, 0));

        section("Pomodoro");
        item("Focus and break length", "Default 25 / 5", prefs.focusMin() + " / " + prefs.breakMin() + " min", this::editPomodoro);

        section("Notifications");
        MaterialSwitch sw = new MaterialSwitch(c);
        sw.setText("Enable reminders and timer alerts");
        sw.setChecked(prefs.notifications());
        sw.setOnCheckedChangeListener((b, checked) -> {
            prefs.setNotifications(checked);
            Reminders.scheduleDaily(c);
        });
        content.addView(sw, Ui.lp(Ui.MATCH, Ui.WRAP, 16, 4, 16, 4));
        item("Daily study reminder", "Time of the daily reminder", TimeUtil.hhmm(prefs.reminderHour() * 60), () ->
                new TimePickerDialog(c, (tp, h, m) -> {
                    prefs.setReminderHour(h);
                    Reminders.scheduleDaily(c);
                    render();
                }, prefs.reminderHour(), 0, true).show());

        section("Data");
        item("Export / backup", "Save all your data to a file", "", () ->
                exportLauncher.launch("studyos-backup-" + TimeUtil.format(System.currentTimeMillis(), "yyyyMMdd") + ".db"));
        item("Import / restore", "Replace current data with a backup file", "", () ->
                importLauncher.launch(new String[]{"*/*"}));
        item("Reset all data", "Deletes everything permanently", "", () ->
                Ui.confirm(c, "Delete ALL data? This cannot be undone.", () ->
                        vm.repo().resetAll(() -> Ui.toast(c, "All data deleted"))));
    }

    private void editGoal() {
        Context c = requireContext();
        Form f = new Form(c, "Daily study goal");
        int goal = prefs.dailyGoalMin();
        final EditText hours = f.text("Hours", String.valueOf(goal / 60), false, true);
        final EditText minutes = f.text("Minutes", String.valueOf(goal % 60), false, true);
        f.show("Save", () -> {
            int total = Form.num(hours, 0) * 60 + Form.num(minutes, 0);
            if (total <= 0) {
                hours.setError("Enter a goal");
                return false;
            }
            prefs.setDailyGoalMin(total);
            render();
            return true;
        }, null);
    }

    private void editPomodoro() {
        final Context c = requireContext();
        Form f = new Form(c, "Pomodoro");
        final EditText focus = f.text("Focus minutes", String.valueOf(prefs.focusMin()), false, true);
        final EditText brk = f.text("Break minutes", String.valueOf(prefs.breakMin()), false, true);
        f.show("Save", () -> {
            int fm = Form.num(focus, 0);
            int bm = Form.num(brk, 0);
            if (fm <= 0 || bm <= 0) {
                Ui.toast(c, "Enter minutes greater than 0");
                return false;
            }
            prefs.setFocusMin(fm);
            prefs.setBreakMin(bm);
            PomodoroManager.get(c).onSettingsChanged();
            render();
            return true;
        }, null);
    }

    // ------------------------------------------------------------ backup / restore
    private void toastLater(final String msg) {
        final Context app = requireContext().getApplicationContext();
        new Handler(Looper.getMainLooper()).post(() -> Toast.makeText(app, msg, Toast.LENGTH_LONG).show());
    }

    private void doExport(final Uri uri) {
        final Context app = requireContext().getApplicationContext();
        vm.repo().io(() -> {
            try {
                BackupManager.export(app, uri);
                toastLater("Backup saved");
            } catch (Exception e) {
                toastLater("Backup failed: " + e.getMessage());
            }
        });
    }

    private void confirmImport(final Uri uri) {
        final Context app = requireContext().getApplicationContext();
        Ui.confirm(requireContext(), "This replaces ALL current data with the backup and restarts the app. Continue?", () ->
                vm.repo().io(() -> {
                    try {
                        BackupManager.restore(app, uri);
                    } catch (Exception e) {
                        toastLater("Restore failed: " + e.getMessage());
                    }
                }));
    }
}
