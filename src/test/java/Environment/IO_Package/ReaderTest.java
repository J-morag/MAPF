package Environment.IO_Package;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ReaderTest {

    private Reader reader;
    private final String[] linesToRead = IO_ManagerTest.linesToRead;
    private final String filePath = IO_ManagerTest.fileToReadPath;


    @Before
    public void before(){

        if ( this.reader != null){
            this.reader.closeFile();
        }else{
            this.reader = new Reader();
        }


        /***       Test openFileToAppend with Valid values   ***/
        Enum_IO enum_io = reader.openFile(this.filePath);
        Assert.assertEquals(Enum_IO.OPENED,enum_io);

    }


    @After
    public void after(){
        Assert.assertTrue(IO_Manager.getInstance().removeOpenPath(this.filePath));
    }


    @Test
    public void getNextLine() {

        /***       Valid values   ***/
        for (int i = 0; i < linesToRead.length ; i++) {
            Assert.assertEquals(linesToRead[i],this.reader.getNextLine()); // got the expected line
        }


    }

    @Test
    public void openFile() {

        /***      Invalid values  ***/
        Enum_IO enum_io = this.reader.openFile(this.filePath);
        Assert.assertEquals(Enum_IO.CURRENT_FILE_STILL_OPEN,enum_io); // Trying to open an open file


        Reader badPathReader = new Reader();
        String badFilePath = "not exists.txt";
        Enum_IO enum_io_notExists = badPathReader.openFile(badFilePath);
        Assert.assertEquals(Enum_IO.INVALID_PATH, enum_io_notExists); // Trying to open an invalid path

    }



    @Test
    public void closeFile() {

        /***       Valid values   ***/
        Enum_IO enum_io = this.reader.closeFile();
        Assert.assertEquals(Enum_IO.CLOSED, enum_io); // file closed successfully

        enum_io = this.reader.closeFile();
        Assert.assertEquals(Enum_IO.CLOSED, enum_io); // Trying to close a closed file


    }
}