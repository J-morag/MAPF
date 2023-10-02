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
    private int cellSize;
    private final List<Color[][]> grids;
    private int iterationTime;
    private int currentIndex;
    private Timer timer;
    private boolean paused;
    public JButton pausePlayButton;
    private final List<Integer> finishedGoals;

    private GridVisualizer(List<Color[][]> grids, @Nullable List<Integer> finishedGoals, int iterationTime, JButton pausePlayButton) {
        if (grids == null || grids.isEmpty()) {
            throw new IllegalArgumentException("grids cannot be null or empty");
        }
        if (finishedGoals != null && finishedGoals.size() != grids.size()) {
            throw new IllegalArgumentException("finishedGoals must be null or be co-indexed with grids");
        }
        this.grids = grids;
        this.finishedGoals = finishedGoals;
        this.iterationTime = iterationTime;
        this.currentIndex = 0;
        this.paused = false;
        this.pausePlayButton = pausePlayButton;

        this.cellSize = Math.min(GRID_MAX_WIDTH_PIXELS / grids.get(0)[0].length, GRID_MAX_HEIGHT_PIXELS / grids.get(0).length);
        this.cellSize = Math.min(this.cellSize, DEFAULT_CELL_SIZE);
        this.cellSize = Math.max(this.cellSize, MIN_CELL_SIZE);
        setPreferredSize(new Dimension(grids.get(0)[0].length * this.cellSize, grids.get(0).length * this.cellSize));
    }
    public int getIterationTime() {
        return iterationTime;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void startIteration() {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                currentIndex++;
                if (currentIndex >= grids.size()) {
                    currentIndex = 0;
                }
                repaint();
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
        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = grids.size() - 1;
        }
        repaint();
    }

    public void stepForward() {
        currentIndex++;
        if (currentIndex >= grids.size()) {
            currentIndex = 0;
        }
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
        int rows = grid.length;
        int cols = grid[0].length;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color color = grid[row][col];
                g.setColor(color);
                g.fillRect(col * this.cellSize, row * this.cellSize, this.cellSize, this.cellSize);
            }
        }
    }

    private int numFinishedGoals() {
        return this.finishedGoals != null ? this.finishedGoals.get(currentIndex) : 0;
    }

    private int maxFinishedGoals() {
        return this.finishedGoals != null ? finishedGoals.get(finishedGoals.size() - 1) : 0;
    }

    public boolean isPaused() {
        return paused;
    }

    public static void visualize(List<Color[][]> grids, @Nullable List<Integer> finishedGoals, int initialIterationTime, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Container contentPanel = frame.getContentPane();
        JPanel gridPanel = new JPanel();
        JPanel controlPanel = new JPanel();
//        controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));

        GridVisualizer gridVisualizer = new GridVisualizer(grids, finishedGoals, initialIterationTime, new JButton("Pause"));
        gridPanel.add(gridVisualizer);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> gridVisualizer.startIteration());
        controlPanel.add(startButton);

        JButton pausePlayButton = gridVisualizer.pausePlayButton;
        pausePlayButton.addActionListener(e -> {
            if (gridVisualizer.isPaused()) {
                gridVisualizer.startIteration();
                pausePlayButton.setText("Pause");
            } else {
                gridVisualizer.pauseIteration();
                pausePlayButton.setText("Play");
            }
        });
        controlPanel.add(pausePlayButton);

        JButton stepBackwardButton = new JButton("Step -");
        stepBackwardButton.setEnabled(false);
        stepBackwardButton.addActionListener(e -> gridVisualizer.stepBackward());
        controlPanel.add(stepBackwardButton);

        JButton stepForwardButton = new JButton("Step +");
        stepForwardButton.setEnabled(false);
        stepForwardButton.addActionListener(e -> gridVisualizer.stepForward());
        controlPanel.add(stepForwardButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            gridVisualizer.reset();
            stepBackwardButton.setEnabled(false);
            stepForwardButton.setEnabled(false);
        });
        controlPanel.add(resetButton);

        javax.swing.Timer timer = new javax.swing.Timer(500, e -> {
            stepBackwardButton.setEnabled(gridVisualizer.isPaused() && gridVisualizer.getCurrentIndex() > 0);
            stepForwardButton.setEnabled(gridVisualizer.isPaused() && gridVisualizer.getCurrentIndex() < grids.size() - 1);
        });

        startButton.addActionListener(e -> {
            gridVisualizer.reset();
            timer.start();
            pausePlayButton.setText("Pause");
        });

        // Create the slider with default value of 500ms and range of 10ms to 2s
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 2000, initialIterationTime);
        slider.setMajorTickSpacing(900);
        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                gridVisualizer.iterationTime = source.getValue();
                gridVisualizer.startIteration();
            }
        });
        controlPanel.add(slider);

        // Add a panel for the status labels
        JPanel statusPanel = new JPanel();

        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel timeStepLabel = new JLabel();
        JLabel finishedGoalsLabel = new JLabel();
        JLabel durationLabel = new JLabel();
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(timeStepLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(finishedGoalsLabel);
        statusPanel.add(Box.createHorizontalStrut(10));
        statusPanel.add(durationLabel);
        statusPanel.add(Box.createHorizontalGlue());
//        frame.add(statusPanel, BorderLayout.SOUTH);

        // Create a Timer to update the labels
        javax.swing.Timer labelTimer = new javax.swing.Timer(100, e -> {
//                if (! gridVisualizer.paused) {
                // Update the labels
                timeStepLabel.setText("Time Step: " + gridVisualizer.getCurrentIndex());
                finishedGoalsLabel.setText("Finished Goals: " + gridVisualizer.numFinishedGoals() + " / " + gridVisualizer.maxFinishedGoals());
                durationLabel.setText("Time Step Duration: " + gridVisualizer.getIterationTime() + " ms");
//                }
        });
        labelTimer.start();

        // Create a vertical box layout to stack the controls, status, and grid
        Box box = Box.createVerticalBox();
        contentPanel.add(box);

        // Add the controls panel to the box
        box.add(controlPanel);

        // Add the status panel to the box
        box.add(statusPanel);

        // Add the grid panel to the box and give it a vertical glue to push it to the top
        gridPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(Box.createVerticalGlue());
        box.add(gridPanel);



        frame.setLayout(new FlowLayout());
        frame.pack();
        frame.setVisible(true);
    }
}
