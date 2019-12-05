package Environment.IO_Package;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Writer{

    private File file;
    private BufferedWriter buffer;
    private IO_Manager io_manager;


    public Writer(){
        this.io_manager = IO_Manager.getInstance();
    }



    /* This method allows to use the same Writer */
    public Enum_IO openFileToAppend(String folderPath, String fileName){

        // Buffer indicates that file in use
        if( this.buffer != null ){
            return Enum_IO.CURRENT_FILE_STILL_OPEN;
        }


        this.file = new File(folderPath, fileName);
        File directory = new File(folderPath);

        // Check that directory's path exists and there is no such file in it
        if ( !IO_Manager.pathExists(directory) ){
            return Enum_IO.INVALID_PATH;
        }


        // Try to create Buffer
        try {
            this.buffer = new BufferedWriter(new FileWriter(this.file, true));
            // Means buffer created successfully
            if( this.buffer != null){
                if ( this.io_manager.addOpenPath( this.file.getPath()) ){
                    return Enum_IO.OPENED;
                }
            }

        }catch (IOException exception){
            exception.printStackTrace();
        }
        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }




    // Writes a given String to the file
    public Enum_IO writeText(String textToWrite){


        if ( this.buffer != null ){
            try{
                // Append the text
                this.buffer.append(textToWrite);
                this.buffer.flush();

                return Enum_IO.WROTE_SUCCESSFULLY;

            }catch ( IOException exception){
                exception.printStackTrace();
            }
        }
        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }


    // This method closes the file
    public Enum_IO closeFile() {

        try{

            if( this.buffer != null ){
                this.buffer.close();
                this.buffer = null;
            }

            if( this.file == null ){
                return Enum_IO.CLOSED;
            }


            // removeOpenPath also returns true if the file isn't listed
            if( this.io_manager.removeOpenPath(this.file.getPath()) ){
                this.file = null;
                return Enum_IO.CLOSED;
            }

        }catch (IOException exception){
            exception.printStackTrace();
        }

        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }



}
