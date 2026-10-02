package com.studyos.app.ui;

import androidx.fragment.app.Fragment;

public class ProjectsFragment extends TabHostFragment {
    @Override protected String[] tabs() { return new String[]{"Projects", "Goals"}; }

    @Override
    protected Fragment create(int index) {
        return index == 1 ? new GoalListFragment() : new ProjectListFragment();
    }
}
