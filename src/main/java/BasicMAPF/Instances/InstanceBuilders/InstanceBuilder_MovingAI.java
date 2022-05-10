package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.IO_Manager;
import Environment.IO_Package.Reader;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.*;
import LifelongMAPF.LifelongAgent;

import java.util.*;

public class InstanceBuilder_MovingAI implements I_InstanceBuilder {


    public static final MapDimensions.Enum_mapOrientation ENUMMAP_ORIENTATION = MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL;


    public static final String FILE_TYPE_MAP = ".map";
    public static final String FILE_TYPE_SCENARIO = ".scen";

    // Indicators
    static private final String INDICATOR_MAP = "map";
    static private final String INDICATOR_HEIGHT = "height";
    static private final String INDICATOR_WIDTH = "width";

    // Separators
    static private final String SEPARATOR_DIMENSIONS = " ";
    static private final String SEPARATOR_MAP = "";
    static private final String SEPARATOR_SCENARIO = "\t";

    // Skip Lines
    static private final int SKIP_LINES_MAP = 1;
    static private final int SKIP_LINES_SCENARIO = 1;


    /*  =Default Values=    */
    static private final int defaultNumOfAgents = 10;
    static private final int defaultNumOfBatches = 5;
    static private final int defaultNumOfAgentsInSingleBatch = 10;


    /*  =Default Index Values=    */
    // Line example: "1	maps/rooms/8room_000.map	512	512	500	366	497	371	6.24264"
    //    Start: ( 500 , 366 )
    //    Goal: ( 497 , 371 )
    static private final int INDEX_AGENT_SOURCE_XVALUE = 4;
    static private final int INDEX_AGENT_SOURCE_YVALUE = 5;
    static private final int INDEX_AGENT_TARGET_XVALUE = 6;
    static private final int INDEX_AGENT_TARGET_YVALUE = 7;

    /*      =Location Types=   */
    static private final char EMPTY = '.';
    static private final char WALL = '@';
    static private final char TREE = 'T';

    /*  Mapping from char to Location type */
    static private final HashMap<Character, Enum_MapLocationType> locationTypeHashMap = new HashMap<Character, Enum_MapLocationType>(){{
        put(EMPTY, Enum_MapLocationType.EMPTY);
        put(WALL, Enum_MapLocationType.WALL);
        put(TREE, Enum_MapLocationType.TREE);
    }};

    static private final int defaultNumWaypoints = 30;

    public boolean reuseAgents = true;
    private final ArrayList<MAPF_Instance> instanceList = new ArrayList<>();

    private final Priorities priorities;
    private final boolean lifelong;

    public InstanceBuilder_MovingAI(Priorities priorities, Boolean lifelong){
        this.priorities = Objects.requireNonNullElse(priorities, new Priorities());
        this.lifelong = Objects.requireNonNullElse(lifelong, false);
    }

    public InstanceBuilder_MovingAI() {
        this(null, null);
    }

    public InstanceBuilder_MovingAI(Priorities priorities) {
        this(priorities, null);
    }

    public InstanceBuilder_MovingAI(Boolean lifelong) {
        this(null, lifelong);
    }

    @Override
    public void prepareInstances(String instanceName, InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties) {

        if (!(instancePath instanceof InstanceManager.Moving_AI_Path)) { return; }

        InstanceManager.Moving_AI_Path moving_ai_path = (InstanceManager.Moving_AI_Path) instancePath;
        if( instanceProperties == null ){ instanceProperties = new InstanceProperties(); }


        MAPF_Instance mapf_instance = null;
        GraphMap graphMap = getMap(moving_ai_path, instanceProperties);
        if( graphMap == null ){ return; }

        // create agent properties
        int[] numOfAgentsFromProperties = (instanceProperties.numOfAgents == null || instanceProperties.numOfAgents.length == 0
                                            ? new int[]{defaultNumOfAgents} : instanceProperties.numOfAgents);

        int numOfBatches = this.getNumOfBatches(numOfAgentsFromProperties);
        ArrayList<String> agentLines = getAgentLines(moving_ai_path, numOfBatches * defaultNumOfAgentsInSingleBatch);

        ArrayList<I_Location> allEmptyMapCoordinates = new ArrayList<>(graphMap.getAllLocations());
        allEmptyMapCoordinates.removeIf(loc -> !loc.getType().equals(Enum_MapLocationType.EMPTY));

        for (int i = 0; i < numOfAgentsFromProperties.length; i++) {

            Agent[] agents = getAgents(agentLines,numOfAgentsFromProperties[i]);
            Random rnd = new Random(agentLines != null ? agentLines.hashCode() : 0); // consistent results given same instance

            if (instanceName == null || agents == null) { continue; /* Invalid parameters */ }

            if (lifelong){
                for (int j = 0; j < agents.length; j++) {
                    Agent agent = agents[j];

                    I_Coordinate[] waypoints = new I_Coordinate[defaultNumWaypoints];
                    waypoints[0] = agent.source;
                    waypoints[waypoints.length - 1] = agent.target;
                    for (int k = 1; k < waypoints.length - 1; k++) {
                        while (waypoints[k] == null ||
                                waypoints[k-1].equals(waypoints[k]) ||
                                (k == waypoints.length - 2 && waypoints[k+1].equals(waypoints[k]))){
                            waypoints[k] = allEmptyMapCoordinates.get(rnd.nextInt(allEmptyMapCoordinates.size())).getCoordinate();
                        }
                    }

                    LifelongAgent lifelongAgent = new LifelongAgent(agent, waypoints);
                    agents[j] = lifelongAgent;
                }
            }

            mapf_instance = makeInstance(instanceName, graphMap, agents, moving_ai_path);
            mapf_instance.setObstaclePercentage(instanceProperties.obstacles.getReportPercentage());
            this.instanceList.add(mapf_instance);
        }
    }

    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManager.Moving_AI_Path instancePath){
        String[] splitScenarioPath = instancePath.scenarioPath.split("\\\\");
        return new MAPF_Instance(instanceName, graphMap, agents, splitScenarioPath[splitScenarioPath.length-1]);
    }


    // Returns an array of agents using the line queue
    private Agent[] getAgents(ArrayList<String> agentLinesList, int numOfAgents) {
        if( agentLinesList == null){ return null; }
        agentLinesList.removeIf(Objects::isNull);
        Agent[] arrayOfAgents = new Agent[Math.min(numOfAgents,agentLinesList.size())];

        if (reuseAgents){
            if(agentLinesList.isEmpty()){ return null; }

            // Iterate over all the agents in numOfAgentsByBatches
            for (int id = 0; id < numOfAgents; id++) {

                if( id < arrayOfAgents.length ){
                    Agent agentToAdd = buildSingleAgent(id ,agentLinesList.get(id), numOfAgents);
                    arrayOfAgents[id] =  agentToAdd; // Wanted agent to add
                }
            }
        }
        else {
            int numOfAgentsByBatches = this.getNumOfBatches(new int[]{numOfAgents});

            // Iterate over all the agents in numOfAgentsByBatches
            for (int id = 0; !agentLinesList.isEmpty() && id < numOfAgentsByBatches * defaultNumOfAgentsInSingleBatch; id++) {

                if( id < numOfAgents ){
                    Agent agentToAdd = buildSingleAgent(id ,agentLinesList.remove(0), numOfAgentsByBatches * defaultNumOfAgentsInSingleBatch);
                    arrayOfAgents[id] =  agentToAdd; // Wanted agent to add
                }else {
                    agentLinesList.remove(0);
                }
            }
        }
        return arrayOfAgents;
    }

    private Agent buildSingleAgent(int id, String agentLine, int numAgents) {
        String[] splitLine = agentLine.split(SEPARATOR_SCENARIO);

        return agentFromStringArray(id, splitLine, numAgents);
    }

    protected Agent agentFromStringArray(int id, String[] splitLine, int numAgents){
        // Init coordinates
        int source_xValue = Integer.parseInt(splitLine[INDEX_AGENT_SOURCE_XVALUE]);
        int source_yValue = Integer.parseInt(splitLine[INDEX_AGENT_SOURCE_YVALUE]);
        Coordinate_2D source = new Coordinate_2D(source_xValue, source_yValue);
        int target_xValue = Integer.parseInt(splitLine[INDEX_AGENT_TARGET_XVALUE]);
        int target_yValue = Integer.parseInt(splitLine[INDEX_AGENT_TARGET_YVALUE]);
        Coordinate_2D target = new Coordinate_2D(target_xValue, target_yValue);

        return new Agent(id, source, target, priorities.getPriorityForAgent(id, numAgents));
    }


    // Returns agentLines from scenario file as a queue
    private ArrayList<String> getAgentLines(InstanceManager.Moving_AI_Path moving_ai_path, int numOfNeededAgents) {

        // Open scenario file
        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(moving_ai_path.scenarioPath);
        if( !enum_io.equals(Enum_IO.OPENED) ){
            reader.closeFile();
            return null; /* couldn't open the file */
        }


        /*  =Get data from reader=  */
        reader.skipFirstLines(SKIP_LINES_SCENARIO); // First line

        ArrayList<String> agentsLines = new ArrayList<>(); // Init queue of agents lines

        String nextLine = reader.getNextLine();
        // Add lines as the num of needed agents
        for (int i = 0; nextLine != null && i < numOfNeededAgents ; i++) {
            agentsLines.add(nextLine);
            nextLine = reader.getNextLine(); // next line
        }

        reader.closeFile();
        return agentsLines;
    }


    private GraphMap getMap( InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties ){

        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(instancePath.path);
        if( !enum_io.equals(Enum_IO.OPENED) ){ return null; /* couldn't open the file */ }

        /*  =Init values=  */
        GraphMap graphMap = null;
        MapDimensions dimensionsFromProperties = ( instanceProperties == null || instanceProperties.mapSize == null ? new MapDimensions() : instanceProperties.mapSize);
        dimensionsFromProperties.setMapOrientation(this.getMapOrientation());
        MapDimensions dimensionsFromFile = new MapDimensions();
        dimensionsFromFile.setMapOrientation(this.getMapOrientation());


        /*  =Get data from reader=  */
        reader.skipFirstLines(SKIP_LINES_MAP); // First line
        String nextLine = reader.getNextLine();

        while ( nextLine != null ){

            if(nextLine.startsWith(INDICATOR_HEIGHT)){
                String[] splitedLineHeight = nextLine.split(SEPARATOR_DIMENSIONS);
                if( IO_Manager.isPositiveInt(splitedLineHeight[1])){
                    dimensionsFromFile.yAxis_length = Integer.parseInt(splitedLineHeight[1]);
                    dimensionsFromFile.numOfDimensions++;
                    if( dimensionsFromProperties.yAxis_length > 0
                            && dimensionsFromFile.yAxis_length != dimensionsFromProperties.yAxis_length ){
                        reader.closeFile();
                        return null; // Bad yAxis length
                    }
                }

            }else if ( nextLine.startsWith(INDICATOR_WIDTH) ){
                String[] splitedLineWidth = nextLine.split(SEPARATOR_DIMENSIONS);
                if( IO_Manager.isPositiveInt(splitedLineWidth[1])){
                    dimensionsFromFile.xAxis_length = Integer.parseInt(splitedLineWidth[1]);
                    dimensionsFromFile.numOfDimensions++;
                    if( dimensionsFromProperties.xAxis_length > 0
                            && dimensionsFromFile.xAxis_length != dimensionsFromProperties.xAxis_length ){
                        reader.closeFile();
                        return null; // Bad xAxis length
                    }
                }

            }else if( nextLine.startsWith(INDICATOR_MAP) ){
                String[] mapAsStrings = I_InstanceBuilder.buildMapAsStringArray(reader, dimensionsFromFile);

                // build map
                graphMap = I_InstanceBuilder.buildGraphMap(mapAsStrings, SEPARATOR_MAP, dimensionsFromFile, locationTypeHashMap, instanceProperties.obstacles);
                break;
            }
            nextLine = reader.getNextLine();
        }

        reader.closeFile(); // No more data in the file
        return graphMap;
    }



    @Override
    public MAPF_Instance getNextExistingInstance(){
        if( ! this.instanceList.isEmpty() ){
            return this.instanceList.remove(0);
        }
        return null;
    }

    @Override
    public MapDimensions.Enum_mapOrientation getMapOrientation() {
        return ENUMMAP_ORIENTATION;
    }


    @Override
    public InstanceManager.InstancePath[] getInstancesPaths(String directoryPath) {
        InstanceManager.InstancePath[] pathArray = IO_Manager.getFilesFromDirectory(directoryPath);
        if(pathArray == null){ return null; }

        ArrayList<InstanceManager.InstancePath> list = new ArrayList<>();

        for (InstanceManager.InstancePath instancePath : pathArray ) {
            if ( instancePath.path.endsWith(FILE_TYPE_MAP) ){

                String[] splitPath = instancePath.path.split("\\\\");
                String mapPrefix = splitPath[splitPath.length-1].replace(FILE_TYPE_MAP, "");
                for (InstanceManager.InstancePath scenarioCandidate : pathArray ){
                    if(scenarioCandidate.path.split("-even")[0].split("-random")[0].endsWith(mapPrefix) && scenarioCandidate.path.endsWith(FILE_TYPE_SCENARIO)){
                        list.add( new InstanceManager.Moving_AI_Path(instancePath.path, scenarioCandidate.path));
                    }

                }
            }
        }

        pathArray = new InstanceManager.InstancePath[list.size()];
        for (int i = 0; i < pathArray.length; i++) {
            pathArray[i] = list.get(i);
        }
        return pathArray;
    }


    private int getNumOfBatches(int[] values){
        if( values == null || values.length == 0){
            return defaultNumOfBatches; // default num of batches
        }
        int curBatch = 0;

        for (int i = 1; i < values.length + 1; i++) {
            // Examples:
            // values[i-1] = 15 -> division = 1.5 -> ( 1.5 > 1 ) addition = 1
            // values[i-1] = 20 -> division = 2.0 -> ( 2.0 !> 2 )addition = 0
            double division = values[i-1] / 10.0;
            int addition = (division > (int)division ? 1 : 0);
            curBatch = curBatch + (int)division + addition;
        }
        return curBatch;
    }
}
