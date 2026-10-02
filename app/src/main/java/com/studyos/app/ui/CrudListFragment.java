package com.studyos.app.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.studyos.app.R;

import java.util.ArrayList;
import java.util.List;

/** Base class for "title + list + add button" screens. */
public abstract class CrudListFragment<T> extends Fragment {
    protected MainViewModel vm;
    protected RowAdapter<T> adapter;
    protected List<T> raw = new ArrayList<>();
    protected LinearLayout header;
    private TextView subtitleView;
    private TextView emptyView;

    /** Screen title, or null to hide the header title. */
    protected abstract String title();
    protected abstract LiveData<List<T>> source();
    protected abstract void bindRow(RowView row, T item);
    protected abstract void onItem(T item);
    protected abstract void onAdd();

    protected boolean hasFab() { return true; }
    protected String emptyText() { return "Nothing here yet. Tap + to add."; }
    protected String subtitle(List<T> shown) { return ""; }
    protected List<T> transform(List<T> in) { return in; }
    protected void buildHeader(LinearLayout header) {}
    protected void observeExtra() {}
    protected void onLongItem(T item) {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        Context c = requireContext();
        vm = new ViewModelProvider(requireActivity()).get(MainViewModel.class);

        FrameLayout root = new FrameLayout(c);
        LinearLayout col = Ui.vbox(c);

        header = Ui.vbox(c);
        header.setPadding(Ui.dp(16), Ui.dp(12), Ui.dp(16), Ui.dp(4));
        if (title() != null) header.addView(Ui.title(c, title()));
        subtitleView = Ui.text(c, "", 13, false, R.color.so_text2);
        header.addView(subtitleView);
        buildHeader(header);
        col.addView(header, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        emptyView = Ui.text(c, emptyText(), 14, false, R.color.so_text2);
        emptyView.setGravity(Gravity.CENTER);
        emptyView.setPadding(Ui.dp(24), Ui.dp(32), Ui.dp(24), Ui.dp(32));
        col.addView(emptyView, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));

        RecyclerView rv = new RecyclerView(c);
        rv.setLayoutManager(new LinearLayoutManager(c));
        rv.setClipToPadding(false);
        rv.setPadding(0, Ui.dp(4), 0, Ui.dp(96));
        adapter = new RowAdapter<>(this::bindRow, this::onItem, this::onLongItem);
        rv.setAdapter(adapter);
        col.addView(rv, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));
        root.addView(col, new FrameLayout.LayoutParams(Ui.MATCH, Ui.MATCH));

        if (hasFab()) {
            FloatingActionButton fab = new FloatingActionButton(c);
            fab.setImageResource(R.drawable.ic_add);
            fab.setBackgroundTintList(ColorStateList.valueOf(Ui.col(c, R.color.so_primary)));
            fab.setColorFilter(Ui.col(c, R.color.so_on_primary));
            fab.setOnClickListener(v -> onAdd());
            FrameLayout.LayoutParams p = new FrameLayout.LayoutParams(Ui.WRAP, Ui.WRAP, Gravity.BOTTOM | Gravity.END);
            p.setMargins(0, 0, Ui.dp(16), Ui.dp(16));
            root.addView(fab, p);
        }

        source().observe(getViewLifecycleOwner(), list -> {
            raw = list == null ? new ArrayList<T>() : list;
            refresh();
        });
        observeExtra();
        return root;
    }

    protected void refresh() {
        if (adapter == null) return;
        List<T> shown = transform(raw);
        adapter.submit(shown);
        subtitleView.setText(subtitle(shown));
        subtitleView.setVisibility(subtitleView.getText().length() == 0 ? View.GONE : View.VISIBLE);
        emptyView.setText(emptyText());
        emptyView.setVisibility(shown.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
