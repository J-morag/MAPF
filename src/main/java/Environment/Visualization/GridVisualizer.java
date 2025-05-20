package Environment.Visualization;

import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GridVisualizer extends JPanel {
    public static final int DEFAULT_CELL_SIZE = 20;
    public static final int MIN_CELL_SIZE = 2;
    public static final int GRID_MAX_WIDTH_PIXELS = 1000;
    public static final int GRID_MAX_HEIGHT_PIXELS = 1000;

    private final List<Color[][]> grids;
    private final List<Integer> finishedGoals;
    private final int gridWidth;
    private final int gridHeight;
    private int cellSize;
    private int iterationTime;
    private int currentIndex;
    private Timer timer;
    private boolean paused;
    private final JButton pausePlayButton;

    private GridVisualizer(List<Color[][]> grids, @Nullable List<Integer> finishedGoals, int iterationTime, JButton pausePlayButton) {
        if (grids == null || grids.isEmpty()) {
            throw new IllegalArgumentException("Grids cannot be null or empty");
        }
        if (finishedGoals != null && finishedGoals.size() != grids.size()) {
            throw new IllegalArgumentException("Finished goals must be null or match the size of grids");
        }

        this.grids = grids;
        this.finishedGoals = finishedGoals;
        this.iterationTime = iterationTime;
        this.currentIndex = 0;
        this.paused = true; // Start in paused state
        this.pausePlayButton = pausePlayButton;

        // Update the pause/play button to reflect the paused state
        pausePlayButton.setText("Play");

        // grid dimensions
        this.gridWidth = grids.get(0).length; // x dimension / width / horizontal
        this.gridHeight = grids.get(0)[0].length; // y dimension / height / vertical

        // Calculate the cell size based on grid dimensions
        this.cellSize = Math.min(GRID_MAX_WIDTH_PIXELS / this.gridWidth, GRID_MAX_HEIGHT_PIXELS / this.gridHeight);
        this.cellSize = Math.min(this.cellSize, DEFAULT_CELL_SIZE);
        this.cellSize = Math.max(this.cellSize, MIN_CELL_SIZE);
        setPreferredSize(new Dimension(this.gridWidth * this.cellSize, this.gridHeight * this.cellSize + 20)); // Add vertical space

    }

    public int getIterationTime() {
        return iterationTime;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public boolean isPaused() {
        return paused;
    }

    public void startIteration() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                stepForward();
            }
        }, iterationTime, iterationTime);
        paused = false;
        pausePlayButton.setText("Pause");
    }

    public void pauseIteration() {
        if (timer != null) {
            timer.cancel();
        }
        paused = true;
        pausePlayButton.setText("Play");
    }

    public void stepBackward() {
        currentIndex = (currentIndex - 1 + grids.size()) % grids.size(); // Wrap around
        repaint();
    }

    public void stepForward() {
        currentIndex = (currentIndex + 1) % grids.size(); // Wrap around
        repaint();
    }

    public void reset() {
        pauseIteration();
        currentIndex = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Color[][] grid = grids.get(currentIndex);
        for (int column = 0; column < this.gridWidth; column++) {
            for (int row = 0; row < this.gridHeight; row++) {
                Color color = grid[column][row];
                g.setColor(color != null ? color : Color.WHITE); // Default to white
                g.fillRect(column * cellSize, row * cellSize, cellSize, cellSize);
            }
        }
    }

    public static void visualize(List<Color[][]> grids, @Nullable List<Integer> finishedGoals, int initialIterationTime, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridVisualizer gridVisualizer = new GridVisualizer(grids, finishedGoals, initialIterationTime, new JButton("Pause"));

        JPanel controlPanel = new JPanel();
        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            gridVisualizer.reset();
            gridVisualizer.startIteration();
        });
        controlPanel.add(startButton);

        JButton pausePlayButton = gridVisualizer.pausePlayButton;
        pausePlayButton.addActionListener(e -> {
            if (gridVisualizer.isPaused()) {
                gridVisualizer.startIteration();
            } else {
                gridVisualizer.pauseIteration();
            }
        });
        controlPanel.add(pausePlayButton);

        JButton stepBackwardButton = new JButton("Step -");
        stepBackwardButton.addActionListener(e -> gridVisualizer.stepBackward());
        controlPanel.add(stepBackwardButton);

        JButton stepForwardButton = new JButton("Step +");
        stepForwardButton.addActionListener(e -> gridVisualizer.stepForward());
        controlPanel.add(stepForwardButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> gridVisualizer.reset());
        controlPanel.add(resetButton);

        JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 2000, initialIterationTime);
        slider.setMajorTickSpacing(900);
        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                gridVisualizer.iterationTime = source.getValue();
                if (!gridVisualizer.isPaused()) {
                    gridVisualizer.startIteration();
                }
            }
        });
        controlPanel.add(slider);

        JPanel statusPanel = new JPanel();
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel timeStepLabel = new JLabel();
        JLabel finishedGoalsLabel = new JLabel();
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(timeStepLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(finishedGoalsLabel);
        statusPanel.add(Box.createHorizontalGlue());

        javax.swing.Timer labelTimer = new javax.swing.Timer(100, e -> {
            timeStepLabel.setText("Time Step: " + gridVisualizer.getCurrentIndex());
            if (gridVisualizer.finishedGoals != null) {
                finishedGoalsLabel.setText("Finished Goals: " + gridVisualizer.finishedGoals.get(gridVisualizer.getCurrentIndex()));
            } else {
                finishedGoalsLabel.setText("Finished Goals: N/A");
            }
        });
        labelTimer.start();

        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(gridVisualizer, BorderLayout.CENTER);
        frame.add(statusPanel, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }
}
