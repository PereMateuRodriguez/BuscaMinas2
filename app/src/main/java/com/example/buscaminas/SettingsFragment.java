package com.example.buscaminas;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

public class SettingsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        RadioGroup radioGroup = view.findViewById(R.id.radio_group);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String difficulty = "Facil";
            if (checkedId == R.id.radio_medium) {
                difficulty = "Medio";
            } else if (checkedId == R.id.radio_hard) {
                difficulty = "Dificil";
            }

            // Actualizar la dificultad en MainActivity
            if (requireActivity() instanceof MainActivity) {
                ((MainActivity) requireActivity()).setDificultad(difficulty);
            }
        });

        return view;
    }
}
