package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Coordinates.MillimetricCoordinate_2D;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.IO_Manager;
import Environment.IO_Package.Reader;
import LifelongMAPF.LifelongAgent;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


public class InstanceBuilder_Warehouse implements I_InstanceBuilder{


    public static final MapDimensions.Enum_mapOrientation ENUMMAP_ORIENTATION = MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL;
    public static final int STICKER_DISTANCE_UNIT_MM = 490;

    /*  =constants=  */

    public static final String FILE_TYPE_MAP = ".json";
    public static final String FILE_TYPE_SCENARIO = ".csv";
    public static final int SKIP_LINES_SCENARIO = 1;
    public static final int NUM_TARGETS_PER_AGENT = 20;
    public static final String SEPARATOR_SCENARIO = ",";
    public static final int INDEX_XVALUE = 1;
    public static final int INDEX_YVALUE = 2;
    private static final int randomSeed = 42;

    /*  =Default Values=    */
    private final int defaultNumOfAgents = 10;

    private final ArrayList<MAPF_Instance> instanceList = new ArrayList<>();

    private final boolean dropDisabledEdges;
    public final boolean lifelong;
    private final boolean forceNoSharedSourceAndFinalDestinations;

    public InstanceBuilder_Warehouse() {
        this(null, null, null);
    }

    public InstanceBuilder_Warehouse(Boolean dropDisabledEdges, Boolean lifelong, Boolean forceNoSharedSourceAndFinalDestinations) {
        this.dropDisabledEdges = Objects.requireNonNullElse(dropDisabledEdges, true);
        this.lifelong = Objects.requireNonNullElse(lifelong, true);
        this.forceNoSharedSourceAndFinalDestinations = Objects.requireNonNullElse(forceNoSharedSourceAndFinalDestinations, this.lifelong);
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
                ? new int[]{this.defaultNumOfAgents} : instanceProperties.numOfAgents);

        ArrayList<ArrayList<String>> agentLines = getAgentLines(moving_ai_path, Arrays.stream(numOfAgentsFromProperties).max().getAsInt());

        for (int numOfAgentsFromProperty : numOfAgentsFromProperties) {

            Agent[] agents = getAgents(agentLines, numOfAgentsFromProperty);
            if (forceNoSharedSourceAndFinalDestinations){
                agents = agentsToNoSharedSourceAndFinalDestinations(agents);
            }

            if (instanceName == null || agents == null) {
                continue; /* Invalid parameters */
            }

            mapf_instance = makeInstance(instanceName, graphMap, agents, moving_ai_path);
            mapf_instance.setObstaclePercentage(instanceProperties.obstacles.getReportPercentage());
            this.instanceList.add(mapf_instance);
        }
    }

    private Agent[] agentsToNoSharedSourceAndFinalDestinations(Agent[] agents) {
        if (this.lifelong){
            Set<I_Coordinate> allCoordinates = new HashSet<>();
            for (Agent a :
                    agents) {
                allCoordinates.addAll(((LifelongAgent)a).waypoints);
            }
            ArrayList<I_Coordinate> coordinatesQueue = new ArrayList<>(allCoordinates);
            Collections.shuffle(coordinatesQueue, new Random(randomSeed));

            List<Agent> res = new ArrayList<>();
            for (Agent a :
                    agents) {
                if (coordinatesQueue.isEmpty()){
                    break;
                }
                else {
                    List<I_Coordinate> waypoints = new ArrayList<>(((LifelongAgent)a).waypoints);
                    I_Coordinate uniqueCoordinate = coordinatesQueue.remove(0);
                    if (!a.source.equals(uniqueCoordinate)){
                        waypoints.add(0, uniqueCoordinate);
                    }
                    if (!a.target.equals(uniqueCoordinate)){
                        waypoints.add(uniqueCoordinate);
                    }
                    res.add(new LifelongAgent(a.iD, uniqueCoordinate, uniqueCoordinate, waypoints.toArray(I_Coordinate[]::new)));
                }
            }
            return res.toArray(Agent[]::new);
        }
        return agents;
    }

    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManager.Moving_AI_Path instancePath){
        String[] splitScenarioPath = instancePath.scenarioPath.split("\\\\");
        return new MAPF_Instance(instanceName, graphMap, agents, splitScenarioPath[splitScenarioPath.length-1]);
    }


    // Returns an array of agents using the line queue
    private Agent[] getAgents(ArrayList<ArrayList<String>> agentLinesList, int numOfAgents) {
        if( agentLinesList == null){ return null; }
        agentLinesList.removeIf(Objects::isNull);
        Agent[] arrayOfAgents = new Agent[Math.min(numOfAgents,agentLinesList.size())];

        if(agentLinesList.isEmpty()){ return null; }

        // Iterate over all the agents in numOfAgents
        for (int id = 0; id < numOfAgents; id++) {
            if( id < arrayOfAgents.length ){
                LifelongAgent agentToAdd = new LifelongAgent(buildSingleAgentOffline(id ,agentLinesList.get(id)),
                        buildSingleAgentWaypoints(agentLinesList.get(id)));
                arrayOfAgents[id] =  agentToAdd; // Wanted agent to add
            }
        }
        return arrayOfAgents;
    }

    private Agent buildSingleAgentOffline(int id, ArrayList<String> agentLines) {
        String[] splitLineSource = agentLines.get(0).split(SEPARATOR_SCENARIO);
        String[] splitLineTarget = agentLines.get(agentLines.size()-1).split(SEPARATOR_SCENARIO);
        return new Agent(id, toCoor2D(splitLineSource[INDEX_XVALUE].strip(), splitLineSource[INDEX_YVALUE].strip()),
                toCoor2D(splitLineTarget[INDEX_XVALUE].strip(), splitLineTarget[INDEX_YVALUE].strip()));
    }

    /**
     * @return an array of waypoints for a lifelong agent.
     */
    private I_Coordinate[] buildSingleAgentWaypoints(ArrayList<String> agentLines) {
        I_Coordinate[] waypoints = new I_Coordinate[agentLines.size()];
        for (int i = 0; i < waypoints.length; i++) {
            String[] agentLineSplit = agentLines.get(i).split(SEPARATOR_SCENARIO);
            waypoints[i] = toCoor2D(agentLineSplit[INDEX_XVALUE].strip(), agentLineSplit[INDEX_YVALUE].strip());
        }
        return waypoints;
    }

    // Returns agentLines from scenario file as a queue. each entry is a list of lines (targets) for one agent.
    private ArrayList<ArrayList<String>> getAgentLines(InstanceManager.Moving_AI_Path moving_ai_path, int numOfNeededAgents) {

        // Open scenario file
        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(moving_ai_path.scenarioPath);
        if( !enum_io.equals(Enum_IO.OPENED) ){
            reader.closeFile();
            return null; /* couldn't open the file */
        }

        /*  =Get data from reader=  */
        reader.skipFirstLines(SKIP_LINES_SCENARIO); // skip first line (header = ["agent_id", "x", "y", "tag"])

        ArrayList<ArrayList<String>> agentsLines = new ArrayList<>(); // Init queue of agents lines

        // Each agent gets a list of targets (one per line). Add line batches as the num of needed agents
        ArrayList<String> currAgentLines = new ArrayList<>();
        for (int current_agent_id = 0; current_agent_id < numOfNeededAgents;) {
            String nextLine = reader.getNextLine();
            // lines are batched per agent
            if (nextLine != null && Integer.parseInt(nextLine.split(SEPARATOR_SCENARIO, 2)[0]) == current_agent_id){
                currAgentLines.add(nextLine);
            }
            if (nextLine == null || Integer.parseInt(nextLine.split(SEPARATOR_SCENARIO, 2)[0]) != current_agent_id){
                agentsLines.add(currAgentLines);
                if (nextLine == null){
                    break;
                }
                else{
                    currAgentLines = new ArrayList<>();
                    currAgentLines.add(nextLine);
                    current_agent_id++;
                }
            }
        }

        reader.closeFile();
        return agentsLines;
    }

    private GraphMap getMap( InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties ){
        Map<Coordinate_2D, List<Coordinate_2D>> coordinatesAdjacencyLists = new HashMap<>();
        Map<Coordinate_2D, List<Integer>> coordinatesEdgeWeights = new HashMap<>();
        Map<Coordinate_2D, Enum_MapLocationType> locationTypes = new HashMap<>();

        JSONObject mapJobj = readJsonMapFile(instancePath);
        if (mapJobj == null) return null;

        Iterator<String> keys = mapJobj.keys();
        while(keys.hasNext()) {
            // first level of JSON - keys are coordinates/stickers/vertices like "12345_12345"
            String currentStickerCoordinatesString = keys.next();
            Coordinate_2D currentStickerCoordinate = toCoor2D(currentStickerCoordinatesString);
            // value - sticker data
            JSONArray vertexData = mapJobj.getJSONArray(currentStickerCoordinatesString);
            // first part of sticker data - a map with a key "tags" to array of tags
            JSONArray tagsJA = vertexData.getJSONObject(0).getJSONArray("tags");
            List<String> tagStrings = (List<String>)(Object)(tagsJA.toList()); // not currently doing anything with tags
            // second part of the sticker data - an array of edges
            JSONArray edgesJA = vertexData.getJSONArray(1);
            List<Coordinate_2D> neighbors = new ArrayList<>(edgesJA.length());
            List<Integer> edgeWeights = new ArrayList<>(edgesJA.length());
            for (int i = 0; i < edgesJA.length(); i++) {
                JSONArray currentEdge = edgesJA.getJSONArray(i);
                // edge is composed of a neighbor (coordinate) and an edge weight
                JSONArray neighborCoordinateJA = currentEdge.getJSONArray(0);
                Coordinate_2D neighborCoordinate = toCoor2D(neighborCoordinateJA.getInt(0), neighborCoordinateJA.getInt(1));
                int edgeWeightCode = currentEdge.getInt(1);
                // disabled edges marked with 99999 weight
                if (!dropDisabledEdges || edgeWeightCode < 99999){
                    int delta_x = neighborCoordinate.x_value - currentStickerCoordinate.x_value;
                    int delta_y = neighborCoordinate.y_value - currentStickerCoordinate.y_value;
                    // get actual weight from distances
                    int realDistance = (int)Math.round(Math.sqrt(Math.pow(delta_x, 2)
                            + Math.pow(delta_y, 2)) / STICKER_DISTANCE_UNIT_MM);
                    // make a chain of NO_STOP vertices between vertices, with a length equal to the weight, instead of a weighted edge
                    // TODO in the future, add support for weighted edges and then replace the chain with proper weights
                    List<Coordinate_2D> intermediateVertices = new ArrayList<>(realDistance - 1);
                    for (int j = 1; j < realDistance; j++) {
                        Coordinate_2D intermediateVertex = toCoor2D(currentStickerCoordinate.x_value + (delta_x/realDistance)*j,
                                currentStickerCoordinate.y_value + (delta_y/realDistance)*j);
                        locationTypes.put(intermediateVertex, Enum_MapLocationType.NO_STOP);
                        intermediateVertices.add(intermediateVertex);
                    }
                    intermediateVertices.add(neighborCoordinate);
                    neighbors.add(intermediateVertices.get(0));
                    for (int j = 0; j < intermediateVertices.size() - 1; j++) {
                        // avoid possibility of duplicates overriding each other because of going from both sides
                        List<Coordinate_2D> jNeighbors = coordinatesAdjacencyLists.computeIfAbsent(intermediateVertices.get(j), coor -> new ArrayList<>());
                        jNeighbors.add(intermediateVertices.get(j+1));
                        List<Integer> jNeighborsWeights = coordinatesEdgeWeights.computeIfAbsent(intermediateVertices.get(j), coor -> new ArrayList<>());
                        jNeighborsWeights.add(1);
                    }
                    edgeWeights.add(1);
                }
            }

            if (!neighbors.isEmpty()){
                coordinatesAdjacencyLists.put(currentStickerCoordinate, neighbors);
                coordinatesEdgeWeights.put(currentStickerCoordinate, edgeWeights);
                locationTypes.put(currentStickerCoordinate, Enum_MapLocationType.EMPTY);
            }
        }

        return MapFactory.newArbitraryGraphMap(coordinatesAdjacencyLists, coordinatesEdgeWeights,
                locationTypes, true);
    }

    private JSONObject readJsonMapFile(InstanceManager.InstancePath instancePath) {
        JSONObject mapJobj;
        try {
            String mapStringJSON = Files.readString(Path.of(instancePath.path));
            mapJobj = new JSONObject(mapStringJSON);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: IO error when attempting to read map file.");
            return null;
        }
        return mapJobj;
    }

    private Coordinate_2D toCoor2D(String coorString){
        String[] coorstrings = coorString.split("_");
        return toCoor2D(coorstrings[0], coorstrings[1]);
    }

    private Coordinate_2D toCoor2D(String xString, String yString){
        return toCoor2D(Integer.parseInt(xString), Integer.parseInt(yString));
    }

    private Coordinate_2D toCoor2D(int xInt, int yInt){
//        return new Coordinate_2D(xInt/STICKER_DISTANCE_UNIT_MM, yInt/STICKER_DISTANCE_UNIT_MM);
        return new MillimetricCoordinate_2D(xInt, yInt);
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
                    if(scenarioCandidate.path.split("_start")[0].endsWith(mapPrefix) && scenarioCandidate.path.endsWith(FILE_TYPE_SCENARIO)){
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
}
