package BasicMAPF.Instances.InstanceBuilders;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import Environment.IO_Package.Reader;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.GraphMap;
import BasicMAPF.Instances.Maps.MapDimensions;
import BasicMAPF.Instances.Maps.MapFactory;
import java.util.HashMap;

/**
 *  An Interface for parsing instance files
 */
public interface I_InstanceBuilder {



    /*  Builds instances and saves it in a data structure, ready for future use */
    void prepareInstances(String instanceName, InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties);

    /*  Saves all paths in a data structure, ready for iterative use    */
    InstanceManager.InstancePath[] getInstancesPaths(String directoryPath);

    /*  Returns the next existing instance from the prepareInstances structure  */
    MAPF_Instance getNextExistingInstance();

    /* Determent the map's orientation, which axis is horizontal or vertical */
    MapDimensions.Enum_mapOrientation getMapOrientation();




    /*  =Static methods=    */

    /*  ==Build maps==  */

    /**
     * Reads map's lines from file into a String[]
     * @param reader the current file reader
     * @param mapDimensions Holds the dimensions sizes and orientation
     * @return Map lines as String array
     */
    static String[] buildMapAsStringArray(Reader reader, MapDimensions mapDimensions){

        int axis_length = 0;// Indicates num of lines
        if(mapDimensions.mapOrientation.equals(MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL)){
            axis_length = mapDimensions.xAxis_length;
        }else if( mapDimensions.mapOrientation.equals(MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL)){
            axis_length = mapDimensions.yAxis_length;
        }

        String[] mapAsStringArray = new String[axis_length];
        for (int index = 0; index < axis_length; index++) {

            String nextLine = reader.getNextLine();
            if ( nextLine != null ){
                mapAsStringArray[index] = nextLine;
            }else {
                return null; // unexpected num of lines
            }
        }
        return mapAsStringArray;
    }


    /***
     * Builds a {@link GraphMap} from String array
     * @param mapAsStrings - Map from file, rows are yAxis
     * @param mapSeparator - Regex value to split map values. by default is "".
     * @param mapDimensions - A {@link MapDimensions}, must be valid.
     * @param locationTypeHashMap - HashMap for converting Character to {@link Enum_MapLocationType}
     * @param obstacle - Value is a {@link BasicMAPF.Instances.InstanceProperties.ObstacleWrapper} indicates obstacle in the map.
     * @return A GraphMap
     */
    static GraphMap buildGraphMap(String[] mapAsStrings, String mapSeparator, MapDimensions mapDimensions, HashMap<Character, Enum_MapLocationType> locationTypeHashMap, InstanceProperties.ObstacleWrapper obstacle, boolean isStronglyConnected) {

        switch (mapDimensions.numOfDimensions) {
            case 2 -> {
                Character[][] mapAsCharacters_2d = build2D_CharacterMap(mapAsStrings, mapDimensions, mapSeparator);
                Enum_MapLocationType[][] mapAsLocationType_2D = build_2D_locationTypeMap(mapAsCharacters_2d, locationTypeHashMap, mapDimensions.mapOrientation, obstacle);
                if (mapAsLocationType_2D == null) {
                    return null; // Error while building the map
                }
                return MapFactory.newSimple4Connected2D_GraphMap(mapAsLocationType_2D, isStronglyConnected);
            }
            case 3 -> {
                Character[][][] mapAsCharacters_3d = new Character[][][]{};
                Enum_MapLocationType[][][] mapAsLocationType_3D = build_3D_locationTypeMap(mapAsCharacters_3d, locationTypeHashMap, obstacle);
                return null; // niceToHave - change to newSimple 4Connected 3D_GraphMap if exists in MapFactory
            }
        }

        return null; // If something went wrong ( should return in switch-case )
    }


    /**
     * Builds Character 2D array from String array, split by separator
     * @param mapAsStrings Map lines as string array
     * @param mapDimensions Holds the dimensions sizes and orientation
     * @param mapSeparator Indicates how to split the map lines
     * @return Character 2d array of map locations
     */
    static Character[][] build2D_CharacterMap(String[] mapAsStrings, MapDimensions mapDimensions, String mapSeparator){

        int xAxis_length = mapDimensions.xAxis_length;
        int yAxis_length = mapDimensions.yAxis_length;

        Character[][] mapAsCharacters_2d = new Character[xAxis_length][yAxis_length];
        MapDimensions.Enum_mapOrientation orientation = mapDimensions.mapOrientation;


        for (int lineIndex = 0; lineIndex < mapAsStrings.length; lineIndex++) {
            String[] splitLine = mapAsStrings[lineIndex].split(mapSeparator); // split line

            // set array's value a char in line
            for (int charIndex = 0; charIndex < splitLine.length ; charIndex++) {
                if (orientation.equals(MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL)) {
                    mapAsCharacters_2d[charIndex][lineIndex] = splitLine[charIndex].charAt(0);
                }else if(orientation.equals(MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL)) {
                    mapAsCharacters_2d[lineIndex][charIndex] = splitLine[charIndex].charAt(0);
                }
            }
        }
        return mapAsCharacters_2d;
    }


    /**
     * Builds {@link Enum_MapLocationType} array by converting chars.
     * Also checks that obstacles in map are valid
     * @param mapAsCharacters The map as Character array
     * @param locationTypeHashMap Mapping from char to {@link Enum_MapLocationType}
     * @param mapOrientation Holds the dimensions sizes and orientation
     * @param obstacle A {@link BasicMAPF.Instances.InstanceProperties.ObstacleWrapper} from {@link InstanceProperties}
     * @return An obstacle valid array of {@link Enum_MapLocationType}
     */
    static Enum_MapLocationType[][] build_2D_locationTypeMap(Character[][] mapAsCharacters , HashMap<Character, Enum_MapLocationType> locationTypeHashMap, MapDimensions.Enum_mapOrientation mapOrientation, InstanceProperties.ObstacleWrapper obstacle) {

        if(mapAsCharacters == null){ return null; }

        int xAxis_length = mapAsCharacters.length;
        int yAxis_length = mapAsCharacters[0].length;

        // used to check obstacle percentage
        int actualNumOfObstacles = 0;
        int numOfNonObstacles = 0;

        // init array
        Enum_MapLocationType[][] locationTypeMap = new Enum_MapLocationType[xAxis_length][yAxis_length];

        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {

                Character character = null;
                character = mapAsCharacters[xIndex][yIndex];

                Enum_MapLocationType locationType = locationTypeHashMap.get(character);

                if ( locationType.equals(Enum_MapLocationType.WALL)){
                    actualNumOfObstacles++; // add one wall to counter
                }else{
                    numOfNonObstacles++; // add one to non obstacle counter
                }
                locationTypeMap[xIndex][yIndex] = locationType;
            }
        }

        int boardSize = (numOfNonObstacles + actualNumOfObstacles);

        // check that it matches the value from properties
        if( ! obstacle.isValidNumOfObstacle(boardSize, actualNumOfObstacles)){
            return null; // Invalid obstacle rate
        }

        // Set Obstacle for future use in MAPF_Instance
        int obstaclePercentage = (int) Math.ceil( ((double) actualNumOfObstacles / (double) boardSize) * 100 );
        obstacle.setWithPercentage(obstaclePercentage);

        return locationTypeMap;
    }

    static Enum_MapLocationType[][][] build_3D_locationTypeMap(Character[][][] mapAsCharacters, HashMap<Character, Enum_MapLocationType> locationTypeHashMap, InstanceProperties.ObstacleWrapper obstacle) {
        // niceToHave - no need to implement for now
        return null;
    }



    /*  =Utils= */

    static boolean equalsAll(int[] arr1, int[] arr2){
        if( arr1 == null || arr2 == null){ return false; }

        if( arr1.length != arr2.length ){ return false; }

        for (int i = 0; i < arr1.length; i++) {
            if( arr1[i] != arr2[i] ){ return false; }
        }
        return true;
    }


    static int equalsAny(int lookFor, int[] values){
        if( values == null){ return -1; }

        for (int i = 0; i < values.length ; i++) {
            if( lookFor == values[i] ){ return i; }
        }
        return -1;
    }

}


