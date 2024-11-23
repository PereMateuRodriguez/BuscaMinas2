package com.example.buscaminas;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Random;

public class GameFragment extends Fragment {

    private static final int DEFAULT_GRID_SIZE = 8; // Tamaño predeterminado de la cuadrícula
    private int currentGridSize = DEFAULT_GRID_SIZE; // Tamaño dinámico de la cuadrícula
    private Button[][] botones;
    private boolean[][] mines;
    private GridLayout gridLayout;
    private Button btnStartGame, btnRetry;

    @Nullable
    @Override
    public View onCreateView(@NonNull android.view.LayoutInflater inflater, @Nullable android.view.ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // Inicializar componentes
        gridLayout = view.findViewById(R.id.game_grid);
        btnStartGame = view.findViewById(R.id.btn_start_game);
        btnRetry = view.findViewById(R.id.btn_retry);

        // Configurar botones de control
        btnStartGame.setOnClickListener(v -> startGame());
        btnRetry.setOnClickListener(v -> resetGame());

        return view;
    }

    private void createGrid() {
        // Limpiar el GridLayout anterior
        gridLayout.removeAllViews();

        // Configurar matrices dinámicamente según el tamaño actual
        botones = new Button[currentGridSize][currentGridSize];
        mines = new boolean[currentGridSize][currentGridSize];

        // Calcular tamaño de los botones
        int buttonSize = calculateButtonSize();

        // Ajustar tamaño del texto dinámicamente según el tamaño de la cuadrícula
        float textSize = calculateTextSize();

        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                Button button = new Button(getActivity());

                // Configurar el tamaño y márgenes del botón
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(1, 1, 1, 1);
                button.setLayoutParams(params);

                button.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
                button.setTextColor(getResources().getColor(android.R.color.white));
                button.setTextSize(textSize); // Tamaño de texto ajustado dinámicamente

                button.setOnClickListener(new CellClickListener(fila, col));
                button.setEnabled(false);
                botones[fila][col] = button;

                gridLayout.addView(button);
            }
        }

        // Actualizar filas y columnas del GridLayout
        gridLayout.setRowCount(currentGridSize);
        gridLayout.setColumnCount(currentGridSize);
    }

    private int calculateButtonSize() {
        // Calcular el tamaño de los botones basado en el ancho disponible
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int totalMargins = 2 * currentGridSize; // Márgenes de 2px por botón
        return (gridWidth - totalMargins) / currentGridSize;
    }

    private float calculateTextSize() {
        // Ajustar el tamaño del texto según el tamaño de la cuadrícula
        if (currentGridSize <= 6) {
            return 16f; // Tamaño más grande para cuadrículas pequeñas
        } else if (currentGridSize <= 8) {
            return 14f; // Tamaño medio
        } else {
            return 10f; // Tamaño pequeño para cuadrículas grandes
        }
    }

    private void startGame() {
        String difficulty = ((MainActivity) requireActivity()).getDifficulty();

        // Configurar tamaño de cuadrícula y número de minas según la dificultad
        int numMines;
        int backgroundColor;

        switch (difficulty) {
            case "Medio":
                currentGridSize = 8;
                numMines = 15;
                backgroundColor = android.R.color.holo_orange_light;
                break;
            case "Dificil":
                currentGridSize = 10;
                numMines = 20;
                backgroundColor = android.R.color.holo_red_dark;
                break;
            default: // Fácil
                currentGridSize = 6;
                numMines = 10;
                backgroundColor = android.R.color.holo_blue_light;
        }

        // Cambiar el color de fondo del layout
        gridLayout.setBackgroundColor(getResources().getColor(backgroundColor));

        // Crear la cuadrícula con el nuevo tamaño
        createGrid();

        // Colocar minas y habilitar botones
        placeMines(numMines);
        btnStartGame.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);

        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                botones[fila][col].setEnabled(true);
                botones[fila][col].setText("");
                botones[fila][col].setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            }
        }
    }

    private void placeMines(int numMines) {
        Random random = new Random();

        // Limpiar minas anteriores
        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                mines[fila][col] = false;
            }
        }

        // Colocar nuevas minas
        while (numMines > 0) {
            int fila = random.nextInt(currentGridSize);
            int col = random.nextInt(currentGridSize);

            if (!mines[fila][col]) {
                mines[fila][col] = true;
                numMines--;
            }
        }
    }

    private class CellClickListener implements View.OnClickListener {
        private final int fila, col;

        public CellClickListener(int fila, int col) {
            this.fila = fila;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            if (mines[fila][col]) {
                ((Button) v).setText("💣");
                ((Button) v).setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                Toast.makeText(getActivity(), "¡Game Over!", Toast.LENGTH_SHORT).show();
                revealMines();
                endGame();
            } else {
                int minesAround = countMines(fila, col);
                ((Button) v).setText(String.valueOf(minesAround));
                ((Button) v).setEnabled(false);
                ((Button) v).setBackgroundColor(getResources().getColor(android.R.color.holo_blue_light));
            }
        }
    }

    private int countMines(int fila, int col) {
        int count = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newFila = fila + i;
                int newCol = col + j;

                if (newFila >= 0 && newFila < currentGridSize && newCol >= 0 && newCol < currentGridSize && mines[newFila][newCol]) {
                    count++;
                }
            }
        }
        return count;
    }

    private void revealMines() {
        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                if (mines[fila][col]) {
                    botones[fila][col].setText("💣");
                    botones[fila][col].setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        }
    }

    private void endGame() {
        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                botones[fila][col].setEnabled(false);
            }
        }
        btnRetry.setVisibility(View.VISIBLE);
    }

    private void resetGame() {
        btnRetry.setVisibility(View.GONE);
        btnStartGame.setVisibility(View.VISIBLE);

        for (int fila = 0; fila < currentGridSize; fila++) {
            for (int col = 0; col < currentGridSize; col++) {
                botones[fila][col].setText("");
                botones[fila][col].setEnabled(false);
                botones[fila][col].setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            }
        }
    }
}
