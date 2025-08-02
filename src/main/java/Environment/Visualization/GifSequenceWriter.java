package Environment.Visualization;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.*;
import java.util.*;

/**
 * A class for creating GIF animations from BufferedImage frames
 * Based on code by Elliot Kroo (https://elliot.kroo.net/software/java/GifSequenceWriter/)
 */
public class GifSequenceWriter {
    protected ImageWriter gifWriter;
    protected ImageWriteParam imageWriteParam;
    protected IIOMetadata imageMetaData;

    /**
     * Creates a new GifSequenceWriter
     *
     * @param outputStream the ImageOutputStream to be written to
     * @param imageType one of the imageTypes specified in BufferedImage
     * @param timeBetweenFramesMS the time between frames in milliseconds
     * @param loopContinuously whether the gif should loop repeatedly
     * @throws IIOException if no gif ImageWriters are found
     */
    public GifSequenceWriter(
            ImageOutputStream outputStream,
            int imageType,
            int timeBetweenFramesMS,
            boolean loopContinuously) throws IIOException, IOException {

        // Get the first available GIF ImageWriter
        gifWriter = getWriter();
        imageWriteParam = gifWriter.getDefaultWriteParam();

        ImageTypeSpecifier imageTypeSpecifier =
                ImageTypeSpecifier.createFromBufferedImageType(imageType);

        imageMetaData = gifWriter.getDefaultImageMetadata(imageTypeSpecifier,
                imageWriteParam);

        // Configure the GIF metadata
        configureRootMetadata(timeBetweenFramesMS, loopContinuously);

        gifWriter.setOutput(outputStream);
        gifWriter.prepareWriteSequence(null);
    }

    private ImageWriter getWriter() throws IIOException {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersBySuffix("gif");
        if(!iter.hasNext()) {
            throw new IIOException("No GIF Image Writers Found");
        }
        return iter.next();
    }

    private void configureRootMetadata(int timeBetweenFramesMS, boolean loopContinuously) throws IIOException {
        String metaFormatName = imageMetaData.getNativeMetadataFormatName();

        IIOMetadataNode root = (IIOMetadataNode)
                imageMetaData.getAsTree(metaFormatName);

        IIOMetadataNode graphicsControlExtensionNode = getNode(
                root,
                "GraphicControlExtension");

        graphicsControlExtensionNode.setAttribute("disposalMethod", "none");
        graphicsControlExtensionNode.setAttribute("userInputFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("transparentColorFlag", "FALSE");
        graphicsControlExtensionNode.setAttribute("delayTime",
                Integer.toString(timeBetweenFramesMS / 10)); // In 1/100ths of a second
        graphicsControlExtensionNode.setAttribute("transparentColorIndex", "0");

        // Set application extension for looping
        if (loopContinuously) {
            IIOMetadataNode applicationExtensions = getNode(root, "ApplicationExtensions");
            IIOMetadataNode applicationExtension = new IIOMetadataNode("ApplicationExtension");

            applicationExtension.setAttribute("applicationID", "NETSCAPE");
            applicationExtension.setAttribute("authenticationCode", "2.0");

            int loop = loopContinuously ? 0 : 1;
            byte[] loopBytes = new byte[] {1, 0, 0};
            loopBytes[1] = (byte)(loop & 0xFF);
            loopBytes[2] = (byte)((loop >> 8) & 0xFF);
            applicationExtension.setUserObject(loopBytes);
            applicationExtensions.appendChild(applicationExtension);
        }

        imageMetaData.setFromTree(metaFormatName, root);
    }

    /**
     * Get or create a node with the given name
     */
    private static IIOMetadataNode getNode(IIOMetadataNode rootNode, String nodeName) {
        for (int i = 0; i < rootNode.getLength(); i++) {
            if (rootNode.item(i).getNodeName().equalsIgnoreCase(nodeName)) {
                return (IIOMetadataNode) rootNode.item(i);
            }
        }

        IIOMetadataNode node = new IIOMetadataNode(nodeName);
        rootNode.appendChild(node);
        return node;
    }

    /**
     * Adds an image to the GIF sequence
     *
     * @param img the BufferedImage to be added to the sequence
     * @throws IOException if there's an error writing the image
     */
    public void writeToSequence(BufferedImage img) throws IOException {
        gifWriter.writeToSequence(
                new IIOImage(img, null, imageMetaData),
                imageWriteParam);
    }

    /**
     * Close this GifSequenceWriter
     *
     * @throws IOException if there's an error closing the writer
     */
    public void close() throws IOException {
        gifWriter.endWriteSequence();
    }
}

