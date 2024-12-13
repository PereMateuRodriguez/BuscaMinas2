


package com.example.buscaminas;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameFragment extends Fragment {

    private static final int DEFAULT_GRID_SIZE = 8;
    private int tamanoActualcuadricula = DEFAULT_GRID_SIZE;
    private Button[][] botones;
    private boolean[][] mines;
    private GridLayout gridLayout;
    private boolean isGameOver = false;
    private boolean esPrimerClick = true;
    private Button restartButton;

    // Variables para el temporizador
    private LinearLayout headerLayout;
    private TextView timerTextView;
    private long startTime = 0;
    private boolean temporizadorCorriendo = false;
    private Handler manejadorTemporizador = new Handler();
    private Runnable tareaTemporizador;

    // Variables para controlar las banderas
    private TextView textoBanderasRestantes;
    private int maxBanderas;
    private int banderasColocadas = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflar el diseño del fragmento y asociarlo a la vista
        View view = inflater.inflate(R.layout.fragment_game, container, false);

        // Vincular elementos de la interfaz grafica con variables del codigo
        headerLayout = view.findViewById(R.id.header_layout); // Contenedor del encabezado
        timerTextView = view.findViewById(R.id.timer_text); // Texto para mostrar el temporizador
        textoBanderasRestantes = view.findViewById(R.id.flags_remaining_text); // Texto para mostrar las banderas restantes

        // Boton para iniciar el juego
        Button startButton = view.findViewById(R.id.start_button);

        // Boton para reiniciar el juego
        restartButton = view.findViewById(R.id.restart_button);

        // Layout de cuadriula del juego
        gridLayout = view.findViewById(R.id.game_grid);

        // Configurar el evento de clic para el boton de inicio
        startButton.setOnClickListener(v -> {
            // Ocultar el botón de inicio
            startButton.setVisibility(View.GONE);

            // Mostrar la cuadricula del juego, el boton de reinicio y el encabezado
            gridLayout.setVisibility(View.VISIBLE);
            restartButton.setVisibility(View.VISIBLE);
            headerLayout.setVisibility(View.VISIBLE);

            // Iniciar el juego
            startGame();
        });

        // Configurar el evento de clic para el boton de reinicio
        restartButton.setOnClickListener(v -> restartGame());

        // Devolber la vista inflada
        return view;
    }


    private void createGrid() {
        // Eliminar cualquier vista previa en el diseño de la cuadrucula
        gridLayout.removeAllViews();

        // Inicializar matrices para botones i minas
        botones = new Button[tamanoActualcuadricula][tamanoActualcuadricula];
        mines = new boolean[tamanoActualcuadricula][tamanoActualcuadricula];

        // Calcular el tamaño de cada boton en función del tamaño de la cuadricula
        int buttonSize = calculateButtonSize();

        // Crear los botones de la cuadricula
        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                // Crear un nuevo boton
                Button button = new Button(getActivity());

                // Configurar los parametros de diseño del boton
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                button.setLayoutParams(params);

                // Establecer el fondo del boton
                button.setBackgroundResource(R.drawable.cell_background);

                // Configurar eventos de clic y clic largo para cada boton
                button.setOnClickListener(new CellClickListener(fila, col)); // Clic para acciones especificas
                button.setOnLongClickListener(new EscuchaBanderaCelda(fila, col)); // Clic largo para marcar con bandera

                // Almacenar el boton en la matriz
                botones[fila][col] = button;

                // Agregar el boton al diseño de cuadricula
                gridLayout.addView(button);
            }
        }


        // Establecer el numero de filas y columnas en el diseño de cuadricula
        gridLayout.setRowCount(tamanoActualcuadricula);
        gridLayout.setColumnCount(tamanoActualcuadricula);
    }


    private int calculateButtonSize() {
        // Obtener el ancho y alto de la pantalla en pixeles
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int gridHeight = getResources().getDisplayMetrics().heightPixels;

        // Calcular el tamaño de los botones en funcion del menor valor disponible
        int buttonSize = Math.min(
                gridWidth / tamanoActualcuadricula, // Tamaño basado en el ancho
                (gridHeight - restartButton.getHeight()) / tamanoActualcuadricula // Tamaño basado en el alto menos el boton de reinicio
        );

        // Reducir ligeramente el tamaño del boton para ajustes visuales
        return (int) (buttonSize * 0.89);
    }


    private void startGame() {
        // Reiniciar el estado del juego
        isGameOver = false; // Indica que el juego no ha terminado
        esPrimerClick = true; // Marca el primer click como no realizado aun

        // Reiniciar y comenzar el temporizador
        resetTimer();
        startTimer();

        // Establecer el tamaño predeterminado de la cuadricula
        tamanoActualcuadricula = DEFAULT_GRID_SIZE;

        // Obtener la dificultad seleccionada desde la actividad principal
        String difficulty = ((MainActivity) requireActivity()).getDificultad();
        int numMines;

        // Determinar el numero de minas segun la dificultad
        switch (difficulty) {
            case "Medio":
                numMines = 15; // Numero de minas para dificultad media
                break;
            case "Dificil":
                numMines = 20; // Numero de minas para dificultad dificil
                break;
            default:
                numMines = 1; // Numero de minas para dificultad facil o predeterminada
                break;
        }

        // Configurar el numero maximo de banderas igual  numero de minas
        maxBanderas = numMines;

        // Reiniciar el contador de banderas colocadas
        banderasColocadas = 0;

        // Actualizar el texto que muestra las banderas restantes
        textoBanderasRestantes.setText("Banderas: " + maxBanderas);

        // Crear la cuadrícula del juego
        createGrid();

        // Colocar las minas en posiciones aleatorias dentro de la cuadricula
        placeMines(numMines);
    }

    private void placeMines(int numMines) {
        // Generar minas de forma aleatoria dentro de la cuadricula
        Random random = new Random();

        // Repetir hasta que todas las minas esten colocadas
        while (numMines > 0) {
            // Generar coordenadas aleatorias para una fila y columna
            int fila = random.nextInt(tamanoActualcuadricula);
            int col = random.nextInt(tamanoActualcuadricula);

            // Verificar si la posicion ya contiene una mina
            if (!mines[fila][col]) {
                mines[fila][col] = true; // Colocar una mina en la posicion
                numMines--; // Reducir el numero de minas restantes por colocar
            }
        }
    }

    private void resetTimer() {
        // Detener el temporizador si esta activo
        stopTimer();

        // Restablecer el texto del temporizador a "00:00"
        timerTextView.setText("00:00");
    }

    private void startTimer() {
        // Registrar el tiempo de inicio en milisegundos
        startTime = System.currentTimeMillis();

        // Marcar el temporizador como activo
        temporizadorCorriendo = true;

        // Crear un Runnable para actualizar el temporizador cada segundo
        tareaTemporizador = new Runnable() {
            @Override
            public void run() {
                // Verificar si el temporizador sigue activo
                if (!temporizadorCorriendo) return;

                // Calcular el tiempo transcurrido desde el inicio
                long milisegundosTranscurridos = System.currentTimeMillis() - startTime;

                // Convertir el tiempo transcurrido en minutos i segundos
                int seconds = (int) (milisegundosTranscurridos / 1000);
                int minutes = seconds / 60;
                seconds = seconds % 60;

                // Actualizar el texto del temporiçador en la interfaç
                timerTextView.setText(String.format("%02d:%02d", minutes, seconds));

                // Reprogramar el Runnable para ejecutarse nuevamente despues de 1 segundo
                manejadorTemporizador.postDelayed(this, 1000);
            }
        };

        // Iniciar el Runnable para que actualice el temporizador
        manejadorTemporizador.post(tareaTemporizador);
    }

    private void stopTimer() {
        // Marcar el temporizador como detenido
        temporizadorCorriendo = false;

        // Eliminar los callbacks del Runnable para detener actualizaciones
        manejadorTemporizador.removeCallbacks(tareaTemporizador);
    }

    private void restartGame() {
        // Reiniciar el juego desde cero
        startGame();

        // Mostrar un mensaje breve indicando que se inicio un nuevo juego
        Toast.makeText(getActivity(), "¡Nuevo juejo iniciado!", Toast.LENGTH_SHORT).show();
    }

    private class CellClickListener implements View.OnClickListener {
        private final int fila, col; // Coordenadas de la celda asociada al boton

        // Constructor que inicializa las coordenadas de la celda
        public CellClickListener(int fila, int col) {
            this.fila = fila;
            this.col = col;
        }

        @Override
        public void onClick(View v) {
            // Si el juego ya termino, no realizar ninguna accio
            if (isGameOver) return;

            // Si es el primer clic del juego
            if (esPrimerClick) {
                esPrimerClick = false; // Marcar el primer clic como realizado

                // Si la primera celda contiene una mina redistrivuir las minas
                if (mines[fila][col]) {
                    redistributeMines(fila, col);
                }
            }

            // Verificar si la celda contiene una mina
            if (mines[fila][col]) {
                // Mostrar un ícono de mina y cambiar el fondo del botón
                ((Button) v).setText("💣");
                v.setBackgroundResource(R.drawable.cell_mine);

                // Terminar el juego con estado de derrota
                endGame(false);
            } else {
                // Revelar la celda si no contiene una mina
                revelarCelda(fila, col);
            }
        }
    }
//--------------------------
private class EscuchaBanderaCelda implements View.OnLongClickListener {
    private final int fila, col; // Coordenadas de la celda asociada al boton

    // Constructor que inicializa las coordenadas de la celda
    public EscuchaBanderaCelda(int fila, int col) {
        this.fila = fila;
        this.col = col;
    }



    //Sin uso por ahora IDEA
    @Override
    public boolean onLongClick(View v) {
        // Obtener el boton correspondiente a la celda seleccionada
        Button button = botones[fila][col];

        // Si el boto ya esta deshabilitado celda revelada no hacer nada
        if (!button.isEnabled()) return true;

        //berificar si la celda ya tiene una bandera
        if (button.getText().equals("🚩")) {
            // Quitar la bandera de la celda
            button.setText("");
            button.setBackgroundResource(R.drawable.cell_background);
            banderasColocadas--; // Reducir el contador de banderas colocadas
        } else {
            // Si hay banderas disponibles, colocar una bandera en la celda
            if (banderasColocadas < maxBanderas) {
                button.setText("🚩");
                button.setBackgroundResource(R.drawable.cell_flag);
                banderasColocadas++; // Incrementar el contador de banderas colocadas
            } else {
                // Mostrar un mensaje si no se pueden colocar mas banderas
                Toast.makeText(getActivity(), "No puedo colozar mas banderas", Toast.LENGTH_SHORT).show();
                return true;
            }
        }

        // Actualizar el texto que muestra el numero de banderas restantes
        textoBanderasRestantes.setText("Banderas: " + (maxBanderas - banderasColocadas));

        // Verificar si se colocaron todas las banderas correctamente
        if (banderasColocadas == maxBanderas && todasLasBanderasCorrectas()) {
            endGame(true); // Finalizar el juego con exito
        }

        return true; // Indicar que el evento de clic largo ha sido manejado
    }
}


    private boolean todasLasBanderasCorrectas() {
        // Verifica si todas las banderas colocadas estan en las posiciones correctas donde hay minas
        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                // Si hay una bandera en una celda que no contiene mina devolvera false
                if (botones[fila][col].getText().equals("🚩") && !mines[fila][col]) {
                    return false;
                }
            }
        }
        // Todas las banderas estan correctamente colocadas
        return true;
    }

    private void revelarCelda(int fila, int col) {
        // Verificar si la celda esta fuera de los limites o ya fue revelada
        if (fila < 0 || col < 0 || fila >= tamanoActualcuadricula || col >= tamanoActualcuadricula || !botones[fila][col].isEnabled()) {
            return;
        }

        // Contar el numero de minas alrededor de la celda
        int minasAllado = countMines(fila, col);

        // Mostrar el numero de minas cercanas en la celda  vacio si no hay minas
        botones[fila][col].setText(minasAllado == 0 ? "" : String.valueOf(minasAllado));

        // Deshabilitar el boton para que no pueda ser clicado nuevamente
        botones[fila][col].setEnabled(false);

        // Cambiar el fondo del boton para indicar que la celda fue revelada
        botones[fila][col].setBackgroundResource(R.drawable.cell_revealed);

        // Si no hay minas alrededor, propagar la revelacion a celdas vecinas
        if (minasAllado == 0) {
            PropagarCeros(fila, col);
        }

        // Verificar si se cumplen las condiciones de victoria
        if (verificarCondicionVictoria()) {
            endGame(true); // Finalizar el juego con exito
        }
    }

    private void PropagarCeros(int fila, int col) {
        // Coordenadas relativas para explorar las 8 direcciones alrededor de la celda
        int[] dx = {-1, -1, -1, 0, 0, 1, 1, 1};
        int[] dy = {-1, 0, 1, -1, 1, -1, 0, 1};

        // Iterar sobre las 8 direcciones
        for (int i = 0; i < 8; i++) {
            int newFila = fila + dx[i]; // Nueva fila basada en el desplazamiento
            int newCol = col + dy[i];  // Nueva columna basada en el desplazamiento

            // Verificar si las nuevas coordenadas estan dentro de los limites de la cuadricula
            if (newFila >= 0 && newFila < tamanoActualcuadricula && newCol >= 0 && newCol < tamanoActualcuadricula) {
                // Si la celda vecina esta habilitada  no ha sido revelada  revelarla
                if (botones[newFila][newCol].isEnabled()) {
                    revelarCelda(newFila, newCol);
                }
            }
        }
    }

    private boolean verificarCondicionVictoria() {
        // Verificar si todas las celdas no minadas han sido reveladas
        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                // Si hay una celda sin mina que aún no ha sido revelada el juego no ha terminado
                if (!mines[fila][col] && botones[fila][col].isEnabled()) {
                    return false;
                }
            }
        }
        // Todas las celdas no minadas han sido reveladas el jugador gana
        return true;
    }
    private int countMines(int fila, int col) {
        int count = 0; // Contador para las minas encontradas alrededor de la celda

        // Iterar sobre las celdas vecinas (incluyendo diagonales)
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                int newFila = fila + i; // Calcular nueva fila
                int newCol = col + j;  // Calcular nueva columna

                // Verificar si las coordenadas están dentro de los límites y si hay una mina
                if (newFila >= 0 && newFila < tamanoActualcuadricula &&
                        newCol >= 0 && newCol < tamanoActualcuadricula &&
                        mines[newFila][newCol]) {
                    count++; // Incrementar el contador de minas
                }
            }
        }

        // Devolver el numero total de minas alrededor
        return count;
    }

    private void redistributeMines(int excludeFila, int excludeCol) {
        // Limpia todas las minas antes de redistribuir
        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                mines[fila][col] = false; // Eliminar mina si existe
            }
        }

        Random random = new Random();
        int numMines = maxBanderas; // Numero total de minas a redistribuir

        // Crear una lista de posiciones validas para colocar las minas
        List<int[]> posicionesValidas = new ArrayList<>();

        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                // Excluir las posiciones cercanas a la celda inicial
                if (!estaEnZonaExcluida(fila, col, excludeFila, excludeCol)) {
                    posicionesValidas.add(new int[]{fila, col});
                }
            }
        }

        // Colocar minas de forma aleatoria en las posiciones validas
        while (numMines > 0 && !posicionesValidas.isEmpty()) {
            int index = random.nextInt(posicionesValidas.size()); // Seleccionar una posicion aleatoria
            int[] position = posicionesValidas.remove(index); // Eliminar la posicion seleccionada de la lista
            mines[position[0]][position[1]] = true; // Colocar una mina en esa posicion
            numMines--; // Reducir el numero de minas restantes por colocar
        }
    }
    private boolean estaEnZonaExcluida(int fila, int col, int excludeFila, int excludeCol) {
        // Verifica si la celda especificada esta en la zona excluida  1 celda de distancia
        return Math.abs(fila - excludeFila) <= 1 && Math.abs(col - excludeCol) <= 1;
    }
    private void endGame(boolean ganada) {
        // Marcar el juego como terminado
        isGameOver = true;
        // Detener el temporizador
        stopTimer();
        // Revelar todas las minas en el tablero
        revealMines();
        // Calcular el tiempo total jugado
        long elapsedMillis = System.currentTimeMillis() - startTime;
        int segundos = (int) (elapsedMillis / 1000);
        // Obtener la actividad principal para registrar estadisticas
        MainActivity mainActivity = (MainActivity) requireActivity();
        // Actualizar estadisticas generales
        mainActivity.incrementarPartidasJugadas(); // Incrementar el total de partidas jugadas
        mainActivity.agregarTiempoJugado(segundos); // Registrar el tiempo jugado en segundos

        // Actualizar estadisticas i mostrar mensaje segun el resultado del juego
        if (ganada) {
            mainActivity.incrementarPartidasGanadas(); // Incrementar las partidas ganadas
            Toast.makeText(getActivity(), "¡Win!", Toast.LENGTH_SHORT).show();
        } else {
            mainActivity.incrementarPartidasPerdidas(); // Incrementar las partias perdidas
            Toast.makeText(getActivity(), "¡Game Over!", Toast.LENGTH_SHORT).show();
        }
    }

    private void revealMines() {
        // Recorrer todas las celdas del tablero
        for (int fila = 0; fila < tamanoActualcuadricula; fila++) {
            for (int col = 0; col < tamanoActualcuadricula; col++) {
                // Si hay una mina en la celda rebelarla
                if (mines[fila][col]) {
                    botones[fila][col].setText("💣"); // Mostrari cono de mina
                    botones[fila][col].setBackgroundResource(R.drawable.cell_mine); // Cambiar el fondo
                }
                // Deshabilitar el boton para que no se le pueda dar otra ves despues de que este marcada
                botones[fila][col].setEnabled(false);
            }
        }
    }
}
