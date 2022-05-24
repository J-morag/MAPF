package Environment.IO_Package;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

public class IO_ManagerTest {

    private IO_Manager io_manager = IO_Manager.getInstance();
    private Reader reader;


    static final String testResources_path = IO_Manager.testResources_Directory;


    /***    ReaderTest   ***/

    static final String fileToReadName = IO_Manager.buildPath(new String[]{"IO_Package", "test_reader_file.txt"});
    static final String fileToReadPath = IO_Manager.buildPath(new String[]{testResources_path, fileToReadName});
    static final String[] linesToRead = {   "This file has 3 lines" ,
                                            "This is the second line",
                                            "This is the last line"};


    /***    WriterTest   ***/
    static final String fileToWriteName = IO_Manager.buildPath(new String[]{"IO_Package", "write_test.txt"});
    static final String fileToWritePath = IO_Manager.buildPath(new String[]{testResources_path, fileToWriteName});

    static final String[] linesToWrite = {  "Try to write\n" ,
                                            "Wrote the second line\n",
                                            "Wrote the last line"};




    @AfterEach
    public void after(){
        this.deleteFileToWrite();
    }



    public static void deleteFileToWrite(){

        File file = new File(fileToWritePath);
        if ( IO_Manager.pathExists(file) ){
            IO_Manager.getInstance().deleteFile(file);
        }
    }


    private void openFileToRead(){

        io_manager.removeOpenPath(fileToReadPath);
        this.reader = io_manager.getReader(fileToReadPath);
        assertNotNull(reader);
    }

    @Test
    public void closeRemovesFromOpenList(){

        this.openFileToRead();
        assertNotNull(this.reader);

        // Close file
        this.reader.closeFile();

        // open the file again
        this.reader = this.io_manager.getReader(fileToReadPath);
        assertNotNull(reader);

        this.reader.closeFile();
    }

    @Test
    public void unableMultipleOpening() {

        this.openFileToRead();

        Reader reader_null = this.io_manager.getReader(fileToReadPath);
        assertNull(reader_null); // expecting null

        this.reader.closeFile();
    }



    @Test
    public void fakeFile(){
        assertFalse(this.io_manager.isOpen("fake_file.txt"));
    }



    @Test
    public void getWriter() {

        File file = new File(testResources_path, fileToWriteName);
        if ( IO_Manager.pathExists(file) ){
            file.delete();
        }

        /***       Valid values   ***/
        Writer writer = this.io_manager.getWriter(testResources_path, fileToWriteName);
        assertNotNull(writer);


        // getWriter should return null if path is still open
        Writer writer_null = this.io_manager.getWriter(testResources_path, fileToWriteName);
        assertNull(writer_null);

        writer.closeFile();
    }



    @Test
    public void isOpen() {

        String openPath = "open_path.txt";
        // Try to add
        this.io_manager.addOpenPath(openPath);
        assertTrue(this.io_manager.isOpen(openPath));

        // Try to remove
        this.io_manager.removeOpenPath(openPath);
        assertFalse(this.io_manager.isOpen(openPath));
    }

    @Test
    public void buildPath() {
        // BuildPath format: folder\fileName.txt
        String path = IO_Manager.buildPath(new String[]{"folder","file_name"});
        assertEquals("folder" + IO_Manager.pathSeparator + "file_name", path);
    }
}