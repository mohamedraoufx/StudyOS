package com.studyos.app.ui;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/** Generic adapter that renders items as {@link RowView}s. */
public class RowAdapter<T> extends RecyclerView.Adapter<RowAdapter.VH> {
    public interface Binder<T> { void bind(RowView row, T item); }
    public interface Listener<T> { void on(T item); }

    public static class VH extends RecyclerView.ViewHolder {
        final RowView row;

        VH(RowView row) {
            super(row);
            this.row = row;
        }
    }

    private final Binder<T> binder;
    private final Listener<T> click;
    private final Listener<T> longClick;
    private List<T> items = new ArrayList<>();

    public RowAdapter(Binder<T> binder, Listener<T> click, Listener<T> longClick) {
        this.binder = binder;
        this.click = click;
        this.longClick = longClick;
    }

    public void submit(List<T> list) {
        items = list == null ? new ArrayList<T>() : list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RowView r = new RowView(parent.getContext());
        RecyclerView.LayoutParams lp = new RecyclerView.LayoutParams(Ui.MATCH, Ui.WRAP);
        lp.setMargins(Ui.dp(12), Ui.dp(5), Ui.dp(12), Ui.dp(5));
        r.setLayoutParams(lp);
        return new VH(r);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        final T item = items.get(position);
        holder.row.reset();
        binder.bind(holder.row, item);
        holder.row.setOnClickListener(v -> click.on(item));
        holder.row.setOnLongClickListener(v -> {
            if (longClick == null) return false;
            longClick.on(item);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
