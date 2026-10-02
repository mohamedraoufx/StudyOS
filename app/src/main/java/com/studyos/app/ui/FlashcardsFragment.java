package com.studyos.app.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.lifecycle.LiveData;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.studyos.app.R;
import com.studyos.app.data.Vocab;
import com.studyos.app.util.Srs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/** Vocabulary list plus a spaced-repetition review session. Optional argument "language". */
public class FlashcardsFragment extends CrudListFragment<Vocab> {
    public static FlashcardsFragment forLanguage(String language) {
        FlashcardsFragment f = new FlashcardsFragment();
        Bundle b = new Bundle();
        b.putString("language", language);
        f.setArguments(b);
        return f;
    }

    private String language() { return getArguments() == null ? null : getArguments().getString("language"); }

    @Override protected String title() { return language() == null ? "Flashcards" : language() + " words"; }
    @Override protected LiveData<List<Vocab>> source() { return vm.vocab; }
    @Override protected void onAdd() { Forms.vocab(requireContext(), vm, null, language()); }
    @Override protected void onItem(Vocab v) { Forms.vocab(requireContext(), vm, v, null); }
    @Override protected String emptyText() { return "No words yet. Tap + to add your first card."; }

    @Override
    protected void buildHeader(LinearLayout header) {
        header.addView(Ui.button(requireContext(), "Start review", true, v -> startReview()),
                Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 4));
    }

    @Override
    protected List<Vocab> transform(List<Vocab> in) {
        String lang = language();
        if (lang == null) return in;
        List<Vocab> out = new ArrayList<>();
        for (Vocab v : in) if (lang.equals(v.language)) out.add(v);
        return out;
    }

    @Override
    protected String subtitle(List<Vocab> shown) {
        return dueCards(shown).size() + " due \u2022 " + shown.size() + " cards";
    }

    private List<Vocab> dueCards(List<Vocab> list) {
        long now = System.currentTimeMillis();
        List<Vocab> due = new ArrayList<>();
        for (Vocab v : list) if (v.reviewDate <= now) due.add(v);
        return due;
    }

    @Override
    protected void bindRow(RowView row, Vocab v) {
        String sub = Ui.join(" \u2022 ", v.translation, language() == null ? v.language : "", v.category);
        row.texts(v.word, sub, v.learned ? "Learned" : levelLabel(v.difficulty));
        row.metaColor(v.learned ? R.color.so_green : R.color.so_text2);
    }

    private static String levelLabel(int d) {
        return d <= 1 ? "Easy" : d == 2 ? "Medium" : "Hard";
    }

    // ----------------------------------------------------------- review session
    private void startReview() {
        List<Vocab> due = dueCards(transform(raw));
        if (due.isEmpty()) {
            Ui.toast(requireContext(), "No cards are due right now");
            return;
        }
        new Review(requireContext(), due).show();
    }

    private class Review {
        private final Context c;
        private final ArrayDeque<Vocab> queue;
        private Vocab current;
        private TextView counter;
        private TextView word;
        private TextView answer;
        private LinearLayout rate;
        private MaterialButton reveal;
        private androidx.appcompat.app.AlertDialog dialog;

        Review(Context c, List<Vocab> cards) {
            this.c = c;
            this.queue = new ArrayDeque<>(cards);
        }

        void show() {
            LinearLayout col = Ui.vbox(c);
            col.setPadding(Ui.dp(24), Ui.dp(16), Ui.dp(24), Ui.dp(8));
            counter = Ui.text(c, "", 12, false, R.color.so_text2);
            counter.setGravity(Gravity.CENTER);
            word = Ui.text(c, "", 28, true, R.color.so_text);
            word.setGravity(Gravity.CENTER);
            answer = Ui.text(c, "", 16, false, R.color.so_text2);
            answer.setGravity(Gravity.CENTER);
            reveal = Ui.button(c, "Show answer", true, v -> reveal());
            rate = Ui.hbox(c);
            addRate("Again", Srs.AGAIN);
            addRate("Hard", Srs.HARD);
            addRate("Good", Srs.GOOD);
            addRate("Easy", Srs.EASY);
            col.addView(counter, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 16));
            col.addView(word, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 8, 0, 16));
            col.addView(answer, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 24));
            col.addView(reveal, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8));
            col.addView(rate, Ui.lp(Ui.MATCH, Ui.WRAP, 0, 0, 0, 8));
            dialog = new MaterialAlertDialogBuilder(c)
                    .setTitle("Review")
                    .setView(col)
                    .setNegativeButton("Close", null)
                    .create();
            dialog.show();
            next();
        }

        private void addRate(String label, final int rating) {
            MaterialButton b = Ui.button(c, label, false, v -> rate(rating));
            b.setInsetLeft(0);
            b.setInsetRight(0);
            rate.addView(b, new LinearLayout.LayoutParams(0, Ui.WRAP, 1f));
        }

        private void next() {
            current = queue.poll();
            if (current == null) {
                dialog.dismiss();
                Ui.toast(c, "Review complete \uD83C\uDF89");
                return;
            }
            counter.setText((queue.size() + 1) + " left");
            word.setText(current.word);
            String ex = current.example == null || current.example.isEmpty() ? "" : "\n\n" + current.example;
            answer.setText(current.translation + ex);
            answer.setVisibility(View.INVISIBLE);
            rate.setVisibility(View.GONE);
            reveal.setVisibility(View.VISIBLE);
        }

        private void reveal() {
            answer.setVisibility(View.VISIBLE);
            reveal.setVisibility(View.GONE);
            rate.setVisibility(View.VISIBLE);
        }

        private void rate(int rating) {
            if (rating == Srs.AGAIN) queue.add(current);
            else vm.repo().reviewVocab(current, rating);
            next();
        }
    }
}
