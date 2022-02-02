package Environment.IO_Package;

import BasicMAPF.Instances.InstanceManager;
import java.io.File;
import java.util.HashSet;

public class IO_Manager { // Singleton class

    private HashSet<String> openedPaths; // Keeps track on opened files
    public static final String pathSeparator = System.getProperty("file.separator");
    public static final String workingDirectory = System.getProperty("user.dir") + "\\src"; // absolute path to src
    public static final String testResources_Directory = buildPath(new String[]{workingDirectory, "test\\resources"});
    public static final String resources_Directory = buildPath(new String[]{System.getProperty("user.dir"), "resources"});


    /* Singleton */
    private static IO_Manager ourInstance = new IO_Manager();
    public static IO_Manager getInstance() {
        return ourInstance;
    }

    private IO_Manager() {
        this.openedPaths = new HashSet<String>();
    }


    // Tries to add path to the openedPaths list
    // Return true if added successfully , otherwise false
    public boolean addOpenPath(String path){
        return this.openedPaths.add(path);
    }

    // Return true if removed successfully , otherwise false
    public boolean removeOpenPath(String path){
        // if path isn't in the list, returns true
        if( ! this.openedPaths.contains(path)){
            return true;
        }
        return this.openedPaths.remove(path);
    }


    // This method returns a Reader if path is available
    public Reader getReader(String filePath){

        // Means it's in the openPath list
        if ( isOpen(filePath)) {
            return null;
        }


        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(filePath);
        if(enum_io.equals(Enum_IO.OPENED)){
            return reader;
        }

        // If for any reason we got here - something went wrong
        return null;
    }


    // This method returns a Writer if path is available
    public Writer getWriter(String folderPath, String fileName){

        String filePath = IO_Manager.buildPath(new String[]{folderPath, fileName});

        // Means it's in the openPath list
        if ( isOpen(filePath)) {
            return null;
        }

        Writer writer = new Writer();
        Enum_IO enum_io = writer.openFileToAppend(folderPath, fileName);

        if(enum_io.equals(Enum_IO.OPENED)){
            // Means it's in the openPath list
            return writer;
        }

        // If for any reason we got here - something went wrong
        return null;
    }

    public static boolean pathExists(File file){
        return file.exists();
    }

    public static boolean isDirectory(File directory){
        return directory.isDirectory();
    }

    // This method deletes a file
    public Enum_IO deleteFile(File toDelete){

        if ( pathExists(toDelete)) {
            if (toDelete.delete()){
                // returns true also when file not listed in openPath list
                if( this.removeOpenPath(toDelete.getPath()) ){
                    return Enum_IO.DELETED;
                }
            }
        }else {
            return Enum_IO.INVALID_PATH;
        }

        // If for any reason we got here - something went wrong
        return Enum_IO.ERROR;
    }


    // checks if path is in openPath list
    public boolean isOpen(String filePath){
        return this.openedPaths.contains(filePath);
    }


    public static String buildPath(String[] input){

        // basic check
        if( input == null || input.length == 0){
            return null;
        }

        // concat input in format: input[0] + "\" + input[1]
        String result = input[0];
        for (int i = 1; i < input.length ; i++) {
            result += pathSeparator + input[i];
        }

        // returns the concat path
        return result;
    }



    public static InstanceManager.InstancePath[] getFilesFromDirectory(String directoryPath){
        /*  Return null in case of an error */

        if ( isDirectory( new File(directoryPath))){
            File directory = new File(directoryPath);
            File[] listOfFiles = directory.listFiles();

            if ( listOfFiles == null){
                return null;
            }

            InstanceManager.InstancePath[] pathList = new InstanceManager.InstancePath[listOfFiles.length];

            for (int i = 0; i < listOfFiles.length ; i++) {

                pathList[i] = new InstanceManager.InstancePath(listOfFiles[i].getPath());
            }

            return pathList;

        }

        return null;
    }




    public static boolean isPositiveInt(String intAsString){

        try {
            int number = Integer.parseInt(intAsString);
            if(number >= 0) {
                return true;
            }
            else {
                return false;
            }
        }
        catch (Exception e){
            return false;

        }
    }

}
