package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.studyos.app.MainActivity;

public class MoreFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        LinearLayout col = Ui.vbox(c);
        col.setPadding(0, Ui.dp(12), 0, Ui.dp(24));
        col.addView(Ui.title(c, "More"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 8));
        item(col, "Languages", "English, French, German, Japanese", new LanguagesFragment());
        item(col, "Flashcards", "Vocabulary review with spaced repetition", new FlashcardsFragment());
        item(col, "Habits", "Daily and weekly habits with streaks", new HabitsFragment());
        item(col, "Notes", "Study notes linked to subjects, projects and goals", new NotesFragment());
        item(col, "Statistics", "Charts, streaks and progress", new StatsFragment());
        item(col, "Search", "Find tasks, notes, goals, projects and words", new SearchFragment());
        item(col, "Settings", "Theme, goals, Pomodoro, notifications, backup", new SettingsFragment());
        return Ui.scroll(c, col);
    }

    private void item(LinearLayout col, String title, String sub, final Fragment target) {
        RowView r = new RowView(requireContext());
        r.texts(title, sub, "\u203A");
        r.setOnClickListener(v -> ((MainActivity) requireActivity()).open(target));
        col.addView(r, Ui.lp(Ui.MATCH, Ui.WRAP, 12, 5, 12, 5));
    }
}
