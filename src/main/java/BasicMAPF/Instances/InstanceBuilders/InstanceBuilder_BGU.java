package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.IO_Manager;
import Environment.IO_Package.Reader;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.*;
import java.util.HashMap;
import java.util.Stack;

public class InstanceBuilder_BGU implements I_InstanceBuilder {

    public static final MapDimensions.Enum_mapOrientation MAP_ORIENTATION = MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL;

    private final String INDICATOR_AGENTS = "Agents:";
    private final String SEPARATOR_AGENTS = ",";
    private final String INDICATOR_MAP = "Grid:";
    private final String SEPARATOR_DIMENSIONS = ",";
    private final String SEPARATOR_MAP = "";

    private final Stack<MAPF_Instance> instanceStack = new Stack<>();

    // Skip Lines
    private final int SKIP_LINES = 0;


    /*  =  Agent line Indexing =   */
    private final int INDEX_AGENT_SOURCE_XVALUE = 3;
    private final int INDEX_AGENT_SOURCE_YVALUE = 4;
    private final int INDEX_AGENT_TARGET_XVALUE = 1;
    private final int INDEX_AGENT_TARGET_YVALUE = 2;


    /*      =Location Types=   */
    private final char EMPTY = '.';
    private final char WALL = '@';

    private HashMap<Character, Enum_MapLocationType> locationTypeHashMap = new HashMap<>(){{
        put(EMPTY, Enum_MapLocationType.EMPTY);
        put(WALL, Enum_MapLocationType.WALL);
    }};


    /*  =Default Values=    */
    private final int defaultNumOfDimensions = 2;
    private final MapDimensions defaultDimensions = new MapDimensions();
    private final int[] defaultNumOfAgents = new int[0];

    private final Priorities priorities;

    public InstanceBuilder_BGU() {
        priorities = new Priorities();
    }

    public InstanceBuilder_BGU(Priorities priorities) {
        this.priorities = priorities;
    }

    private MAPF_Instance getInstance(String instanceName, InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties) {

        // Try to open file
        Reader reader=new Reader();
        Enum_IO enum_io =reader.openFile(instancePath.path);
        if( !enum_io.equals(Enum_IO.OPENED) ){ return null; /* couldn't open the file */ }

        /*  =Init values=  */
        MAPF_Instance mapf_instance = null;
        GraphMap graphMap = null;
        Agent[] agents = null;
        MapDimensions mapDimensionsFromFile = null;
        instanceProperties = ( instanceProperties == null ? new InstanceProperties() : instanceProperties);
        instanceProperties.mapSize.setMapOrientation(this.getMapOrientation());

        /*  =Get data from reader=  */

        reader.skipFirstLines(this.SKIP_LINES);
        String nextLine = reader.getNextLine();

        while ( nextLine != null ){

            switch (nextLine){

                case INDICATOR_MAP:
                    String dimensionsAsString = reader.getNextLine();
                    mapDimensionsFromFile = getMapDimensions(dimensionsAsString);
                    // Checks validity of dimensions:
                    if ( mapDimensionsFromFile == null || ! this.checkMapDimensions(mapDimensionsFromFile, instanceProperties, reader)){
                        break;
                    }

                    String[] mapAsStrings = I_InstanceBuilder.buildMapAsStringArray(reader, mapDimensionsFromFile);

                    // build map
                    graphMap = I_InstanceBuilder.buildGraphMap(mapAsStrings, this.SEPARATOR_MAP, mapDimensionsFromFile, this.locationTypeHashMap, instanceProperties.obstacles);

                    break; // end case

                case INDICATOR_AGENTS:
                    mapDimensionsFromFile = (mapDimensionsFromFile == null ? this.defaultDimensions : mapDimensionsFromFile);
                    agents = buildAgents(reader, mapDimensionsFromFile.numOfDimensions); // currently supports only 2D

                    if (agents == null){
                        break; // No need to check the num of agents
                    }

                    int[] numOfAgents = ( instanceProperties.numOfAgents == null ? this.defaultNumOfAgents : instanceProperties.numOfAgents);
                    int index = I_InstanceBuilder.equalsAny(agents.length, numOfAgents);

                    // index equals -1 if agents.length doesn't match any of the values in numOfAgents
                    if( numOfAgents.length != this.defaultNumOfAgents.length && index == -1 ){
                        agents = null;
                    }
                    break; // end case

            } // switch end

            nextLine = reader.getNextLine();
        }

        reader.closeFile(); // No more data in the file

        if ( instanceName == null || graphMap == null || agents == null){
            return null; // Invalid parameters
        }

        mapf_instance = new MAPF_Instance(instanceName, graphMap, agents);
        mapf_instance.setObstaclePercentage(instanceProperties.obstacles.getReportPercentage());
        return mapf_instance;
    }

    @Override
    public void prepareInstances(String instanceName, InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties){

        MAPF_Instance mapf_instance = this.getInstance(instanceName, instancePath, instanceProperties);
        if ( mapf_instance != null ){
            this.instanceStack.push(mapf_instance);
        }
    }



    @Override
    public MAPF_Instance getNextExistingInstance(){
        if( ! this.instanceStack.empty() ){
            return this.instanceStack.pop();
        }
        return null;
    }

    @Override
    public MapDimensions.Enum_mapOrientation getMapOrientation() {
        return MAP_ORIENTATION;
    }


    /***  =Validity check=  ***/

    /***
     *
     * @param mapDimensionsFromFile - Board size from file
     * @param instanceProperties - Board size from properties
     * @param reader  - closes the reader for invalid values
     * @return boolean - if dimensions are valid.
     */
    private boolean checkMapDimensions(MapDimensions mapDimensionsFromFile, InstanceProperties instanceProperties, Reader reader){

        if( mapDimensionsFromFile == null || mapDimensionsFromFile.numOfDimensions < 1){
            reader.closeFile();
            return false; // Bad dimensions values
        }

        if( instanceProperties == null || instanceProperties.mapSize.numOfDimensions == 0) {
            return true; // Missing properties values
        }


        // Equals to all the values in the array
        if( mapDimensionsFromFile.equals(instanceProperties.mapSize)){
            return true; // Valid Board size
        }

        // Invalid values, close reader
        reader.closeFile();
        return false;
    }



    /***  =Build Agents=  ***/

    private Agent buildSingleAgent(int dimensions, String line, int numAgents){

        String[] agentLine = line.split(this.SEPARATOR_AGENTS);

        if( agentLine.length < 1){ return null; /* invalid agent line */ }

        int agentID = Integer.parseInt(agentLine[0]);
        dimensions = ( dimensions == 0 ? dimensions = this.defaultNumOfDimensions : dimensions);

        if(dimensions == 2) {
            /*      source values    */
            int source_xValue = Integer.valueOf(agentLine[this.INDEX_AGENT_SOURCE_XVALUE]);
            int source_yValue = Integer.valueOf(agentLine[this.INDEX_AGENT_SOURCE_YVALUE]);
            Coordinate_2D source = new Coordinate_2D(source_xValue, source_yValue);
            /*      Target values    */
            int target_xValue = Integer.valueOf(agentLine[this.INDEX_AGENT_TARGET_XVALUE]);
            int target_yValue = Integer.valueOf(agentLine[this.INDEX_AGENT_TARGET_YVALUE]);
            Coordinate_2D target = new Coordinate_2D(target_xValue, target_yValue);

            return new Agent(agentID, source, target, priorities.getPriorityForAgent(agentID, numAgents));
        }

        if(dimensions == 3) {/* nicetohave */ }

        return null; // Bad dimensions input
    }


    private Agent[] buildAgents(Reader reader, int dimensions) {

        String nextLine = reader.getNextLine(); // expected num of agents
        if( nextLine == null || ! IO_Manager.isPositiveInt(nextLine)) {
            return null; // num of agents should be a positive int
        }

        int numOfAgents = Integer.parseInt(nextLine);
        Agent[] agents = new Agent[numOfAgents];

        for (int i = 0; i < agents.length; i++) {
            nextLine = reader.getNextLine();

            Agent agentToAdd = buildSingleAgent(dimensions, nextLine, agents.length);
            if ( agentToAdd == null ){ return null; /* Bad agent line */ }
            agents[i] = agentToAdd;
        }
        return agents;
    }



    /***  =Build Map and Dimensions=  ***/

    private MapDimensions getMapDimensions(String dimensionsAsString) {

        int[] dimensions = null;
        if(dimensionsAsString.contains(SEPARATOR_DIMENSIONS)) {
            String[] splittedLine = dimensionsAsString.split(SEPARATOR_DIMENSIONS);
            dimensions = new int[splittedLine.length];

            for (int i = 0; i < dimensions.length; i++) {
                if ( IO_Manager.isPositiveInt( splittedLine[i] )){
                    dimensions[i] = Integer.parseInt(splittedLine[i]);
                }else{
                    return null; // dimensions should be positive integers
                }
            }

        }else {
            return null; // Missing expected separator
        }

        return new MapDimensions(dimensions, this.getMapOrientation());
    }



    @Override
    public InstanceManager.InstancePath[] getInstancesPaths(String directoryPath) {
        InstanceManager.InstancePath[] pathArray = IO_Manager.getFilesFromDirectory(directoryPath);
        return pathArray;
    }


}
