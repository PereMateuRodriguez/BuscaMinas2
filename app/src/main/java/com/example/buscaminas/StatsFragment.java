package com.example.buscaminas;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class StatsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_stats, container, false);

        MainActivity mainActivity = (MainActivity) requireActivity();

        TextView partidasJugadasTextView = view.findViewById(R.id.stats_partidas_jugadas);
        TextView partidasGanadasTextView = view.findViewById(R.id.stats_partidas_ganadas);
        TextView partidasPerdidasTextView = view.findViewById(R.id.stats_partidas_perdidas);
        TextView tiempoJugadoTextView = view.findViewById(R.id.stats_tiempo_jugado);

        partidasJugadasTextView.setText("Partidas Jugadas: " + mainActivity.getPartidasJugadas());
        partidasGanadasTextView.setText("Partidas Ganadas: " + mainActivity.getPartidasGanadas());
        partidasPerdidasTextView.setText("Partidas Perdidas: " + mainActivity.getPartidasPerdidas());
        tiempoJugadoTextView.setText("Tiempo Total Jugado: " + mainActivity.getTiempoTotalJugado() + " segundos");

        return view;
    }
}
