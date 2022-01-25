package Environment.IO_Package;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Reader {

    private File file;
    private Scanner scanner;
    private IO_Manager io_manager;


    public Reader(){
        this.io_manager = IO_Manager.getInstance();
    }

    /**
     *
     * @return the next line in file, if there is no nextLine - returns null
     */
    public String getNextLine(){

        // Tries to read the file's next line
        if( this.scanner != null && this.scanner.hasNextLine() ){
            return this.scanner.nextLine();
        }
        return null;
    }


    /**
     * skips the first lines in the file, mostly info lines
     * @param numOfLines how many lines to skip
     */
    public void skipFirstLines(int numOfLines){
        for (int i = 0; i < numOfLines; i++) {
            this.getNextLine();
        }
    }


    /**
     * Open file with a given path
     * This method allows to use the same Reader
     * @param filePath file's path
     * @return The file's state as {@link Enum_IO}
     */
    public Enum_IO openFile(String filePath){

        // Scanner indicates that file is in use
        if ( this.scanner != null ){ return Enum_IO.CURRENT_FILE_STILL_OPEN; }

        this.file = new File(filePath);

        // Check that path exists
        if ( !this.file.isFile() ){ return Enum_IO.INVALID_PATH; }


        // Try to create Scanner
        try {
            this.scanner = new Scanner(this.file);
            // Means scanner created successfully
            if ( this.scanner != null ){
                // Check if file is open in list
                if( this.io_manager.isOpen(this.file.getPath()) ){ return Enum_IO.CURRENT_FILE_STILL_OPEN; }
                if( this.io_manager.addOpenPath(this.file.getPath()) ){ return Enum_IO.OPENED; }
            }

        } catch (FileNotFoundException exception){
            exception.printStackTrace();
        }

        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }


    // This method closes the file
    public Enum_IO closeFile() {

        if( this.scanner != null ){
            this.scanner.close();
            this.scanner = null;
        }

        if( this.file == null ){ return Enum_IO.CLOSED; }

        // removeOpenPath also returns true if the file isn't listed
        if (IO_Manager.getInstance().removeOpenPath(this.file.getPath()) ){
            this.file = null;
            return Enum_IO.CLOSED;
        }

        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }
}
