package com.example.buscaminas;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private String dificultad = "Facil"; // Valor predeterminado

    // Variables para estadisticas
    private int partidasJugadas = 0;
    private int partidasGanadas = 0;
    private int partidasPerdidas = 0;
    private long tiempoTotalJugado = 0; // Tiempo total en segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        // Ocultar la barra de estado
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // Configurar la Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurar BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            if (item.getItemId() == R.id.nav_game) {
                selectedFragment = new GameFragment();
                toolbar.setTitle("Busca Minas");
            } else if (item.getItemId() == R.id.nav_settings) {
                selectedFragment = new SettingsFragment();
                toolbar.setTitle("Configuración");
            } else if (item.getItemId() == R.id.nav_stats) {
                selectedFragment = new StatsFragment();
                toolbar.setTitle("Estadísticas");
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
            }

            return true;
        });

        // Cargar el fragmento inicial (Juego)
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_game);
        }
    }

    public String getDificultad() {
        return dificultad;
    }

    public void setDificultad(String dificultad) {
        this.dificultad = dificultad;
    }

    // Métodos para estadísticas
    public void incrementarPartidasJugadas() {
        partidasJugadas++;
    }

    public void incrementarPartidasGanadas() {
        partidasGanadas++;
        Log.d("DEBUG", "Partidas ganadas incrementadas. Total: " + partidasGanadas);
    }

    public void incrementarPartidasPerdidas() {
        partidasPerdidas++;
    }

    public void agregarTiempoJugado(long segundos) {
        tiempoTotalJugado += segundos;
    }

    public int getPartidasJugadas() {
        return partidasJugadas;
    }

    public int getPartidasGanadas() {
        return partidasGanadas;
    }

    public int getPartidasPerdidas() {
        return partidasPerdidas;
    }

    public long getTiempoTotalJugado() {
        return tiempoTotalJugado;
    }
}
