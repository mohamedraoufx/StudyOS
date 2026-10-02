package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.studyos.app.MainActivity;
import com.studyos.app.R;
import com.studyos.app.data.Note;
import com.studyos.app.data.StudySession;
import com.studyos.app.data.Subject;
import com.studyos.app.data.Task;
import com.studyos.app.data.Vocab;
import com.studyos.app.util.LiveUtil;
import com.studyos.app.util.Metrics;
import com.studyos.app.util.StatsCalculator;
import com.studyos.app.util.TimeUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LanguageDetailFragment extends Fragment {
    private MainViewModel vm;
    private LinearLayout content;
    private String language = "";

    public static LanguageDetailFragment of(String language) {
        LanguageDetailFragment f = new LanguageDetailFragment();
        Bundle b = new Bundle();
        b.putString("language", language);
        f.setArguments(b);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);
        language = getArguments() == null ? "" : getArguments().getString("language", "");
        content = Ui.vbox(c);
        content.setPadding(0, Ui.dp(12), 0, Ui.dp(24));
        LiveUtil.observeAll(getViewLifecycleOwner(), this::render, vm.subjects, vm.sessions, vm.vocab, vm.tasks, vm.notes);
        return Ui.scroll(c, content);
    }

    private void render() {
        if (!isAdded()) return;
        final Context c = requireContext();
        content.removeAllViews();
        List<Subject> subjects = LiveUtil.nz(vm.subjects.getValue());
        long now = System.currentTimeMillis();
        long today = TimeUtil.startOfDay(now);
        long total = 0, todaySec = 0;
        Set<Long> days = new HashSet<>();
        for (StudySession s : LiveUtil.nz(vm.sessions.getValue())) {
            if (!language.equals(s.subjectName)) continue;
            total += s.durationSec;
            days.add(TimeUtil.dayNumber(s.startTime));
            if (s.startTime >= today && s.startTime < today + TimeUtil.DAY) todaySec += s.durationSec;
        }
        int[] st = StatsCalculator.streaks(days, TimeUtil.dayNumber(now));
        int words = 0, learned = 0;
        for (Vocab v : LiveUtil.nz(vm.vocab.getValue())) {
            if (language.equals(v.language)) {
                words++;
                if (v.learned) learned++;
            }
        }
        int tasks = 0;
        for (Task t : LiveUtil.nz(vm.tasks.getValue())) {
            if (language.equals(t.subject) && t.status != Task.DONE) tasks++;
        }
        int notes = 0;
        for (Note n : LiveUtil.nz(vm.notes.getValue())) if (language.equals(n.linkName)) notes++;
        int level = Metrics.languageLevel(subjects, language);

        content.addView(Ui.title(c, language), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 4));
        MaterialCardView overview = Ui.card(c);
        LinearLayout o = Ui.vbox(c);
        o.addView(Ui.text(c, "Overall level: " + level + "%", 16, true, R.color.so_text));
        o.addView(Ui.progress(c, level, R.color.so_primary), Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 8));
        o.addView(Ui.text(c, "Studied today " + TimeUtil.duration(todaySec) + " \u2022 total " + TimeUtil.duration(total),
                14, false, R.color.so_text2));
        o.addView(Ui.text(c, "Streak " + st[0] + " days (best " + st[1] + ")", 14, false, R.color.so_text2));
        o.addView(Ui.text(c, words + " words (" + learned + " learned) \u2022 " + tasks + " open tasks \u2022 " + notes + " notes",
                14, false, R.color.so_text2));
        overview.addView(o);
        content.addView(overview);

        LinearLayout buttons = Ui.hbox(c);
        buttons.addView(Ui.button(c, "Flashcards", true, v ->
                ((MainActivity) requireActivity()).open(FlashcardsFragment.forLanguage(language))), new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        buttons.addView(Ui.button(c, "Notes", false, v ->
                ((MainActivity) requireActivity()).open(NotesFragment.forLink(Note.LINK_LANGUAGE, language))),
                Ui.lp(0, Ui.WRAP, 8, 0, 0, 0));
        ((LinearLayout.LayoutParams) buttons.getChildAt(1).getLayoutParams()).weight = 1f;
        content.addView(buttons, Ui.lp(Ui.MATCH, Ui.WRAP, 12, 4, 12, 4));

        content.addView(Ui.section(c, "Skills"), Ui.lp(Ui.MATCH, Ui.WRAP, 16, 12, 16, 0));
        MaterialCardView skillCard = Ui.card(c);
        LinearLayout sc = Ui.vbox(c);
        for (final Subject s : subjects) {
            if (!Subject.SKILL.equals(s.category) || !language.equals(s.groupName)) continue;
            final TextView label = Ui.text(c, s.name + ": " + s.level + "%", 14, true, R.color.so_text);
            label.setOnClickListener(v -> Forms.skill(c, vm, language, s));
            sc.addView(label, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 0));
            SeekBar sb = new SeekBar(c);
            sb.setMax(100);
            sb.setProgress(s.level);
            sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override public void onProgressChanged(SeekBar bar, int p, boolean fromUser) {
                    label.setText(s.name + ": " + p + "%");
                }
                @Override public void onStartTrackingTouch(SeekBar bar) {}
                @Override public void onStopTrackingTouch(SeekBar bar) {
                    Subject copy = Subject.of(s.name, s.category, s.groupName, bar.getProgress());
                    copy.id = s.id;
                    vm.repo().saveSubject(copy);
                }
            });
            sc.addView(sb);
        }
        sc.addView(Ui.button(c, "Add skill", false, v -> Forms.skill(c, vm, language, null)), Ui.lp(Ui.WRAP, Ui.WRAP, 0, 8, 0, 0));
        skillCard.addView(sc);
        content.addView(skillCard);
        content.addView(Ui.text(c, "Tap a skill name to rename or delete it.", 12, false, R.color.so_text2),
                Ui.lp(Ui.MATCH, Ui.WRAP, 16, 0, 16, 0));
    }
}
