package Environment.Visualization;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GridVisualizer extends JPanel {
    public static final int CELL_SIZE = 5;
    private final List<char[][]> grids;
    private int iterationTime;
    private int currentIndex;
    private Timer timer;
    private boolean paused;
    public JButton pausePlayButton;
    public GridVisualizer(List<char[][]> grids, int iterationTime, JButton pausePlayButton) {
        this.grids = grids;
        this.iterationTime = iterationTime;
        this.currentIndex = 0;
        this.paused = false;
        this.pausePlayButton = pausePlayButton;
        setPreferredSize(new Dimension(grids.get(0)[0].length * CELL_SIZE, grids.get(0).length * CELL_SIZE));
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
        char[][] grid = grids.get(currentIndex);
        int rows = grid.length;
        int cols = grid[0].length;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                Color color = getColor(grid[row][col]);
                g.setColor(color);
                g.fillRect(col * CELL_SIZE, row * CELL_SIZE, CELL_SIZE, CELL_SIZE);
            }
        }
    }

    private Color getColor(char c) {
        return switch (c) {
            case 'o' -> Color.BLACK;
            case 'f' -> Color.WHITE;
            case 'a' -> Color.RED;
            case 'g' -> Color.GREEN;
            default -> throw new IllegalArgumentException("Invalid char: " + c);
        };
    }

    public boolean isPaused() {
        return paused;
    }

    public static void visualize(List<char[][]> grids, int iterationTime) {
        JFrame frame = new JFrame("Char Grid");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridVisualizer gridVisualizer = new GridVisualizer(grids, iterationTime, new JButton("Pause"));
        frame.add(gridVisualizer);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> gridVisualizer.startIteration());
        frame.add(startButton);

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
        frame.add(pausePlayButton);

        JButton stepBackwardButton = new JButton("Step -");
        stepBackwardButton.setEnabled(false);
        stepBackwardButton.addActionListener(e -> gridVisualizer.stepBackward());
        frame.add(stepBackwardButton);

        JButton stepForwardButton = new JButton("Step +");
        stepForwardButton.setEnabled(false);
        stepForwardButton.addActionListener(e -> gridVisualizer.stepForward());
        frame.add(stepForwardButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> {
            gridVisualizer.reset();
            stepBackwardButton.setEnabled(false);
            stepForwardButton.setEnabled(false);
        });
        frame.add(resetButton);

        javax.swing.Timer timer = new javax.swing.Timer(500, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepBackwardButton.setEnabled(gridVisualizer.isPaused() && gridVisualizer.getCurrentIndex() > 0);
                stepForwardButton.setEnabled(gridVisualizer.isPaused() && gridVisualizer.getCurrentIndex() < grids.size() - 1);
            }
        });

        startButton.addActionListener(e -> {
            gridVisualizer.reset();
            timer.start();
            pausePlayButton.setText("Pause");
        });

        // Create the slider with default value of 500ms and range of 50ms to 2s
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 50, 2000, iterationTime);
        slider.setMajorTickSpacing(900);
        slider.setMinorTickSpacing(100);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.addChangeListener(e -> {
            JSlider source = (JSlider)e.getSource();
            if (!source.getValueIsAdjusting()) {
                int newDelay = source.getValue();
                gridVisualizer.iterationTime = newDelay;
                gridVisualizer.startIteration();
            }
        });

        frame.add(slider);

        frame.setLayout(new FlowLayout());
        frame.pack();
        frame.setVisible(true);
    }
}
