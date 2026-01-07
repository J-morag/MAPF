package Environment.Visualization;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.ffmpeg.global.avutil;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.Java2DFrameConverter;
import org.jetbrains.annotations.Nullable;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GridVisualizer extends JPanel {
    public static final int DEFAULT_CELL_SIZE = 20;
    public static final int MIN_CELL_SIZE = 2;
    public static final int GRID_MAX_WIDTH_PIXELS = 1000;
    public static final int GRID_MAX_HEIGHT_PIXELS = 1000;
    public static final int DEFAULT_EXPORT_FPS = 5;

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
    private JFrame parentFrame;
    private double scaleFactor = 1.0; // We'll keep this as double for the UI display
    private int scaleNumerator = 1;   // Integer scaling: numerator
    private int scaleDenominator = 1; // Integer scaling: denominator

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
        pausePlayButton.setText("▶");  // Unicode play symbol

        // grid dimensions
        this.gridWidth = grids.get(0).length; // x dimension / width / horizontal
        this.gridHeight = grids.get(0)[0].length; // y dimension / height / vertical

        // Calculate the cell size based on grid dimensions
        this.cellSize = Math.min(GRID_MAX_WIDTH_PIXELS / this.gridWidth, GRID_MAX_HEIGHT_PIXELS / this.gridHeight);
        this.cellSize = Math.min(this.cellSize, DEFAULT_CELL_SIZE);
        this.cellSize = Math.max(this.cellSize, MIN_CELL_SIZE);
        updatePreferredSize();
    }

    private void updatePreferredSize() {
        int scaledWidth = (this.gridWidth * this.cellSize * this.scaleNumerator) / this.scaleDenominator;
        int scaledHeight = (this.gridHeight * this.cellSize * this.scaleNumerator) / this.scaleDenominator;

        setPreferredSize(new Dimension(scaledWidth, scaledHeight + 20)); // Add vertical space

        // Request parent container to revalidate layout if available
        if (getParent() != null) {
            getParent().revalidate();
            if (parentFrame != null) {
                parentFrame.pack();
            }
        }
    }

    public void setParentFrame(JFrame frame) {
        this.parentFrame = frame;
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

    /**
     * Set the scale factor for the visualization
     * @param scale the scale factor (e.g. 1, 2, 0.5)
     */
    public void setScaleFactor(double scale) {
        // Store as double for display purposes
        this.scaleFactor = Math.max(0.1, scale);

        // Convert to a rational fraction for integer scaling
        if (scale == 0.25) {
            scaleNumerator = 1;
            scaleDenominator = 4;
        } else if (scale == 0.5) {
            scaleNumerator = 1;
            scaleDenominator = 2;
        } else if (scale == 1.0) {
            scaleNumerator = 1;
            scaleDenominator = 1;
        } else if (scale == 2.0) {
            scaleNumerator = 2;
            scaleDenominator = 1;
        } else if (scale == 3.0) {
            scaleNumerator = 3;
            scaleDenominator = 1;
        } else if (scale == 4.0) {
            scaleNumerator = 4;
            scaleDenominator = 1;
        } else {
            // Default fallback to avoid division by zero
            scaleNumerator = 1;
            scaleDenominator = 1;
        }

        updatePreferredSize();
        repaint();
    }

    /**
     * Get the current scale factor
     */
    public double getScaleFactor() {
        return this.scaleFactor;
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
        pausePlayButton.setText("| |");
    }

    public void pauseIteration() {
        if (timer != null) {
            timer.cancel();
        }
        paused = true;
        pausePlayButton.setText("▶");  // Unicode play symbol
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

        // Integer scaling approach
        if (scaleNumerator >= scaleDenominator) {
            // Upscaling (2x, 3x, 4x): Draw each cell larger
            int pixelsPerCell = (cellSize * scaleNumerator) / scaleDenominator;

            for (int column = 0; column < this.gridWidth; column++) {
                for (int row = 0; row < this.gridHeight; row++) {
                    Color color = grid[column][row];
                    g.setColor(color != null ? color : Color.WHITE);
                    g.fillRect(
                        column * pixelsPerCell,
                        row * pixelsPerCell,
                        pixelsPerCell,
                        pixelsPerCell
                    );
                }
            }
        } else {
            // Downscaling (0.5x, 0.25x): Combine cells
            // When downscaling, we need to draw cells that are smaller
            int cellsPerPixel = scaleDenominator / scaleNumerator;
            int pixelsPerCell = Math.max(1, cellSize / cellsPerPixel);

            for (int column = 0; column < this.gridWidth; column++) {
                for (int row = 0; row < this.gridHeight; row++) {
                    Color color = grid[column][row];
                    g.setColor(color != null ? color : Color.WHITE);
                    g.fillRect(
                        (column * pixelsPerCell),
                        (row * pixelsPerCell),
                        Math.max(1, pixelsPerCell),
                        Math.max(1, pixelsPerCell)
                    );
                }
            }
        }
    }

    /**
     * Export the visualization as an MP4 video
     */
    private void exportAsVideo(File outputFile) throws IOException {
        // Calculate dimensions with integer scaling
        int pixelsPerCell = (cellSize * scaleNumerator) / scaleDenominator;
        int frameWidth = gridWidth * pixelsPerCell;
        int frameHeight = gridHeight * pixelsPerCell;

        // Create a JavaCV frame recorder with simple, universally supported codec settings
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, frameWidth, frameHeight);

        // Use Raw Video codec (uncompressed) - maximum compatibility
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_RAWVIDEO);
        recorder.setFormat("avi"); // AVI container format
        recorder.setFrameRate(DEFAULT_EXPORT_FPS);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_BGR24); // Simple pixel format

        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            recorder.start();

            // Save original position
            int originalIndex = currentIndex;
            currentIndex = 0;

            // Add each frame to the video
            for (int i = 0; i < grids.size(); i++) {
                // Create a fresh BufferedImage with accurate colors
                BufferedImage image = new BufferedImage(
                        frameWidth, frameHeight,
                        BufferedImage.TYPE_3BYTE_BGR);

                Graphics2D g = image.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Paint the current grid with precise colors and integer scaling
                Color[][] grid = grids.get(currentIndex);
                for (int column = 0; column < gridWidth; column++) {
                    for (int row = 0; row < gridHeight; row++) {
                        Color color = grid[column][row];
                        g.setColor(color != null ? color : Color.WHITE);
                        g.fillRect(
                            column * pixelsPerCell,
                            row * pixelsPerCell,
                            pixelsPerCell,
                            pixelsPerCell
                        );
                    }
                }

                g.dispose();
                Frame frame = converter.convert(image);
                recorder.record(frame);
                currentIndex = (currentIndex + 1) % grids.size();
            }

            // Restore original position
            currentIndex = originalIndex;
            repaint();
        } finally {
            recorder.stop();
            recorder.release();
        }
    }

    /**
     * Renders the current grid to a BufferedImage
     */
    public BufferedImage renderCurrentGridToImage() {
        // Use integer scaling for image dimensions
        int pixelsPerCell = (cellSize * scaleNumerator) / scaleDenominator;

        // Create image with explicit RGB color model for consistent colors
        BufferedImage image = new BufferedImage(
                this.gridWidth * pixelsPerCell,
                this.gridHeight * pixelsPerCell,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = image.createGraphics();

        Color[][] grid = grids.get(currentIndex);
        for (int column = 0; column < this.gridWidth; column++) {
            for (int row = 0; row < this.gridHeight; row++) {
                Color color = grid[column][row];
                g.setColor(color != null ? color : Color.WHITE);
                g.fillRect(
                    column * pixelsPerCell,
                    row * pixelsPerCell,
                    pixelsPerCell,
                    pixelsPerCell
                );
            }
        }
        g.dispose();
        return image;
    }

    /**
     * Shows a save dialog and exports the visualization in the selected format
     */
    public void saveAs() {
        pauseIteration(); // Pause the animation while saving

        // Create a dialog for selecting file format
        String[] formats = {"GIF Animation (*.gif)", "AVI Video (*.avi)", "WebM Video (*.webm)"};
        String selectedFormat = (String) JOptionPane.showInputDialog(
                parentFrame,
                "Choose export format:",
                "Save Visualization As",
                JOptionPane.QUESTION_MESSAGE,
                null,
                formats,
                formats[0]);

        if (selectedFormat == null) {
            return; // User canceled format selection
        }

        // Determine file extension from selected format
        String extension;
        if (selectedFormat.contains("gif")) {
            extension = "gif";
        } else if (selectedFormat.contains("webm")) {
            extension = "webm";
        } else {
            extension = "avi";
        }

        // Use system's native file dialog
        FileDialog fileDialog = new FileDialog(parentFrame, "Save Visualization As", FileDialog.SAVE);
        fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith("." + extension));
        fileDialog.setFile("*." + extension);
        fileDialog.setVisible(true);

        // Get the selected file
        String directory = fileDialog.getDirectory();
        String filename = fileDialog.getFile();

        if (directory != null && filename != null) {
            // Add extension if not present
            if (!filename.toLowerCase().endsWith("." + extension)) {
                filename += "." + extension;
            }

            String filePath = directory + filename;
            File selectedFile = new File(filePath);

            // Create a progress dialog
            JDialog progressDialog = new JDialog(parentFrame, "Exporting Visualization", false);
            JPanel panel = new JPanel(new BorderLayout(10, 10));
            panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            JLabel messageLabel = new JLabel("Creating " + extension.toUpperCase() + " file, please wait...");
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            panel.add(messageLabel, BorderLayout.NORTH);
            panel.add(progressBar, BorderLayout.CENTER);
            progressDialog.add(panel);
            progressDialog.pack();
            progressDialog.setLocationRelativeTo(parentFrame);

            // Use SwingWorker to perform export in background
            SwingWorker<Void, Void> worker = new SwingWorker<>() {
                @Override
                protected Void doInBackground() throws Exception {
                    try {
                        // Choose export method based on selected format
                        if (extension.equals("gif")) {
                            exportAsGif(selectedFile);
                        } else if (extension.equals("webm")) {
                            exportAsWebM(selectedFile);
                        } else if (extension.equals("avi")) {
                            exportAsVideo(selectedFile);
                        }
                    } catch (Exception e) {
                        throw e;
                    }
                    return null;
                }

                @Override
                protected void done() {
                    progressDialog.dispose();
                    try {
                        get(); // Check for exceptions during export
                        JOptionPane.showMessageDialog(parentFrame,
                                "Visualization saved successfully to:\n" + filePath,
                                "Save Successful", JOptionPane.INFORMATION_MESSAGE);
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(parentFrame,
                                "Error saving file: " + e.getMessage(),
                                "Save Error", JOptionPane.ERROR_MESSAGE);
                        e.printStackTrace();
                    }
                }
            };

            worker.execute();
            progressDialog.setVisible(true);
        }
    }

    /**
     * Export the visualization as an animated GIF
     */
    private void exportAsGif(File outputFile) throws IOException {
        // Create a GIF writer using animated-gif-lib
        try (ImageOutputStream output = new FileImageOutputStream(outputFile)) {
            GifSequenceWriter writer = new GifSequenceWriter(
                    output,
                    BufferedImage.TYPE_INT_RGB,
                    iterationTime, // Use the same delay time as in visualization
                    true); // Loop continuously

            // Reset to beginning for export
            int originalIndex = currentIndex;
            currentIndex = 0;

            // Add each frame to the GIF
            for (int i = 0; i < grids.size(); i++) {
                BufferedImage image = renderCurrentGridToImage(); // This now includes scale factor
                writer.writeToSequence(image);
                currentIndex = (currentIndex + 1) % grids.size();
            }

            // Close the writer
            writer.close();

            // Restore original index
            currentIndex = originalIndex;
            repaint();
        }
    }

    /**
     * Export the visualization as a WebM video
     */
    private void exportAsWebM(File outputFile) throws IOException {
        // Calculate dimensions with integer scaling
        int pixelsPerCell = (cellSize * scaleNumerator) / scaleDenominator;
        int frameWidth = gridWidth * pixelsPerCell;
        int frameHeight = gridHeight * pixelsPerCell;

        // Create a JavaCV frame recorder with WebM codec settings
        FFmpegFrameRecorder recorder = new FFmpegFrameRecorder(outputFile, frameWidth, frameHeight);

        // Configure for WebM (VP9 codec)
        recorder.setVideoCodec(avcodec.AV_CODEC_ID_VP9);
        recorder.setFormat("webm");
        recorder.setFrameRate(DEFAULT_EXPORT_FPS);
        recorder.setPixelFormat(avutil.AV_PIX_FMT_YUV420P);

        // Quality settings for VP9
        recorder.setVideoQuality(15); // Lower is better quality, range 0-51
        recorder.setVideoBitrate(500000); // Bit rate in bits/s (0.5 Mbps)

        Java2DFrameConverter converter = new Java2DFrameConverter();

        try {
            recorder.start();

            // Save original position
            int originalIndex = currentIndex;
            currentIndex = 0;

            // Add each frame to the video
            for (int i = 0; i < grids.size(); i++) {
                // Create a fresh BufferedImage with accurate colors
                BufferedImage image = new BufferedImage(
                        frameWidth, frameHeight,
                        BufferedImage.TYPE_3BYTE_BGR);

                Graphics2D g = image.createGraphics();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                // Paint the current grid with precise colors and integer scaling
                Color[][] grid = grids.get(currentIndex);
                for (int column = 0; column < gridWidth; column++) {
                    for (int row = 0; row < gridHeight; row++) {
                        Color color = grid[column][row];
                        g.setColor(color != null ? color : Color.WHITE);
                        g.fillRect(
                            column * pixelsPerCell,
                            row * pixelsPerCell,
                            pixelsPerCell,
                            pixelsPerCell
                        );
                    }
                }

                g.dispose();
                Frame frame = converter.convert(image);
                recorder.record(frame);
                currentIndex = (currentIndex + 1) % grids.size();
            }

            // Restore original position
            currentIndex = originalIndex;
            repaint();
        } finally {
            recorder.stop();
            recorder.release();
        }
    }

    public static void visualize(List<Color[][]> grids, @Nullable List<Integer> finishedGoals, int initialIterationTime, String title) {
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GridVisualizer gridVisualizer = new GridVisualizer(grids, finishedGoals, initialIterationTime, new JButton("▶"));
        gridVisualizer.setParentFrame(frame);

        // Main controls panel
        JPanel controlPanel = new JPanel(new BorderLayout());

        // Playback controls (buttons)
        JPanel playbackPanel = new JPanel();

        // First set of buttons
        JButton stepBackwardButton = new JButton("Step -");
        stepBackwardButton.addActionListener(e -> gridVisualizer.stepBackward());
        playbackPanel.add(stepBackwardButton);

        JButton pausePlayButton = gridVisualizer.pausePlayButton;
        pausePlayButton.addActionListener(e -> {
            if (gridVisualizer.isPaused()) {
                gridVisualizer.startIteration();
            } else {
                gridVisualizer.pauseIteration();
            }
        });
        playbackPanel.add(pausePlayButton);

        JButton stepForwardButton = new JButton("Step +");
        stepForwardButton.addActionListener(e -> gridVisualizer.stepForward());
        playbackPanel.add(stepForwardButton);

        JButton resetButton = new JButton("Reset");
        resetButton.addActionListener(e -> gridVisualizer.reset());
        playbackPanel.add(resetButton);

        JButton startButton = new JButton("Start");
        startButton.addActionListener(e -> {
            gridVisualizer.reset();
            gridVisualizer.startIteration();
        });
        playbackPanel.add(startButton);

        // Add space between control buttons and Save As button
        playbackPanel.add(Box.createHorizontalStrut(15));

        // Add Save As button with floppy disk symbol
        JButton saveAsButton = new JButton("💾 Save As");
        saveAsButton.addActionListener(e -> gridVisualizer.saveAs());
        playbackPanel.add(saveAsButton);

        controlPanel.add(playbackPanel, BorderLayout.NORTH);

        // Speed and Scale controls in one row
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 5));

        // Speed controls (renamed to Step Time)
        JPanel speedPanel = new JPanel();
        speedPanel.add(new JLabel("Step Time (ms): "));
        JSlider slider = new JSlider(JSlider.HORIZONTAL, 10, 2000, initialIterationTime);

        // Set both custom labels and tick marks
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);

        // Set major and minor tick spacing
        slider.setMajorTickSpacing(500);
        slider.setMinorTickSpacing(100);

        // Create a custom label table for specific values
        Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
        labelTable.put(10, new JLabel("10"));
        labelTable.put(1000, new JLabel("1000"));
        labelTable.put(2000, new JLabel("2000"));
        slider.setLabelTable(labelTable);

        slider.addChangeListener(e -> {
            JSlider source = (JSlider) e.getSource();
            if (!source.getValueIsAdjusting()) {
                gridVisualizer.iterationTime = source.getValue();
                if (!gridVisualizer.isPaused()) {
                    gridVisualizer.startIteration();
                }
            }
        });
        speedPanel.add(slider);
        settingsPanel.add(speedPanel);

        // Scale controls
        JPanel scalePanel = new JPanel();
        scalePanel.add(new JLabel("Scale: "));
        String[] scaleOptions = {"0.25x", "0.5x", "1x", "2x", "3x", "4x"};
        JComboBox<String> scaleDropdown = new JComboBox<>(scaleOptions);
        scaleDropdown.setSelectedIndex(2); // Default to 1x
        scaleDropdown.addActionListener(e -> {
            String selected = (String)scaleDropdown.getSelectedItem();
            double scale = Double.parseDouble(selected.substring(0, selected.length() - 1));
            gridVisualizer.setScaleFactor(scale);
        });
        scalePanel.add(scaleDropdown);
        settingsPanel.add(scalePanel);

        controlPanel.add(settingsPanel, BorderLayout.CENTER);

        // Status panel with time step and goals info - now positioned above grid
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
        JLabel timeStepLabel = new JLabel();
        JLabel finishedGoalsLabel = new JLabel();
        statusPanel.add(Box.createHorizontalGlue());
        statusPanel.add(timeStepLabel);
        statusPanel.add(Box.createHorizontalStrut(20));
        statusPanel.add(finishedGoalsLabel);
        statusPanel.add(Box.createHorizontalGlue());

        controlPanel.add(statusPanel, BorderLayout.SOUTH);

        // Update labels with a timer
        javax.swing.Timer labelTimer = new javax.swing.Timer(100, e -> {
            timeStepLabel.setText("Time Step: " + gridVisualizer.getCurrentIndex());
            if (gridVisualizer.finishedGoals != null) {
                finishedGoalsLabel.setText("Finished Goals: " + gridVisualizer.finishedGoals.get(gridVisualizer.getCurrentIndex()));
            } else {
                finishedGoalsLabel.setText("Finished Goals: N/A");
            }
        });
        labelTimer.start();

        // Add components to frame
        frame.add(controlPanel, BorderLayout.NORTH);
        frame.add(gridVisualizer, BorderLayout.CENTER);

        frame.pack();
        frame.setVisible(true);
    }
}
