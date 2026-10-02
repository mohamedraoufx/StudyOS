package com.studyos.app.ui;

import androidx.fragment.app.Fragment;

public class StudyFragment extends TabHostFragment {
    @Override protected String[] tabs() { return new String[]{"Focus", "Sessions", "Subjects"}; }

    @Override
    protected Fragment create(int index) {
        switch (index) {
            case 1: return new SessionsFragment();
            case 2: return new SubjectsFragment();
            default: return new FocusFragment();
        }
    }
}
