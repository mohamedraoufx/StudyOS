package com.studyos.app.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.studyos.app.R;

/** A screen with a tab bar that swaps child fragments. */
public abstract class TabHostFragment extends Fragment {
    private int selected = 0;

    protected abstract String[] tabs();
    protected abstract Fragment create(int index);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        LinearLayout col = Ui.vbox(requireContext());
        final TabLayout tl = new TabLayout(requireContext());
        tl.setTabMode(TabLayout.MODE_FIXED);
        tl.setBackgroundColor(Ui.col(requireContext(), R.color.so_bg));
        for (String t : tabs()) tl.addTab(tl.newTab().setText(t));
        FrameLayout content = new FrameLayout(requireContext());
        content.setId(R.id.tab_content);
        col.addView(tl, new LinearLayout.LayoutParams(Ui.MATCH, Ui.WRAP));
        col.addView(content, new LinearLayout.LayoutParams(Ui.MATCH, 0, 1f));

        selected = state == null ? 0 : state.getInt("tab", 0);
        TabLayout.Tab tab = tl.getTabAt(selected);
        if (tab != null) tab.select();
        if (state == null) show(selected);
        tl.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab t) {
                selected = t.getPosition();
                show(selected);
            }
            @Override public void onTabUnselected(TabLayout.Tab t) {}
            @Override public void onTabReselected(TabLayout.Tab t) {}
        });
        return col;
    }

    private void show(int index) {
        getChildFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.tab_content, create(index))
                .commit();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle out) {
        super.onSaveInstanceState(out);
        out.putInt("tab", selected);
    }
}
