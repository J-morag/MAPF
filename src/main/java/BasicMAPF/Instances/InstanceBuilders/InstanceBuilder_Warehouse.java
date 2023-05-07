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
import java.util.regex.Pattern;


public class InstanceBuilder_Warehouse implements I_InstanceBuilder{


    public static final MapDimensions.Enum_mapOrientation ENUMMAP_ORIENTATION = MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL;
    public static final int STICKER_DISTANCE_UNIT_MM = 490;

    /*  =constants=  */

    public static final String FILE_TYPE_MAP = ".json";
    public static final String FILE_TYPE_SCENARIO = ".csv";
    public static final int SKIP_LINES_SCENARIO = 1;
    static private final int DEFAULT_NUM_WAYPOINTS = 100;
    public static final String SEPARATOR_SCENARIO = ",";
    public static final int INDEX_XVALUE = 1;
    public static final int INDEX_YVALUE = 2;
    public static final int MERGE_COORDINATES_THRESHOLD = 100;

    /*  =Default Values=    */
    private final int defaultNumOfAgents = 10;

    private final ArrayList<MAPF_Instance> instanceList = new ArrayList<>();

    /* = Instance Fields = */

    private final boolean dropDisabledEdges;
    public final boolean lifelong;
    private final boolean forceNoSharedSourceAndFinalDestinations;
    private final boolean randomWaypoints;
    private final boolean forceEdgesBidirectional;

    public InstanceBuilder_Warehouse() {
        this(null, null, null, null, null);
    }

    public InstanceBuilder_Warehouse(Boolean dropDisabledEdges, Boolean lifelong, Boolean forceNoSharedSourceAndFinalDestinations, Boolean randomWaypoints, Boolean forceEdgesBidirectional) {
        this.dropDisabledEdges = Objects.requireNonNullElse(dropDisabledEdges, true);
        this.lifelong = Objects.requireNonNullElse(lifelong, true);
        this.forceNoSharedSourceAndFinalDestinations = Objects.requireNonNullElse(forceNoSharedSourceAndFinalDestinations, this.lifelong);
        this.randomWaypoints = Objects.requireNonNullElse(randomWaypoints, false);
        this.forceEdgesBidirectional = Objects.requireNonNullElse(forceEdgesBidirectional, true);
    }

    @Override
    public void prepareInstances(String mapName, InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties) {
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

        Set<Coordinate_2D> canonicalCoordinates = new HashSet<>();
        for (I_Location location : graphMap.getAllLocations()) {
            canonicalCoordinates.add((Coordinate_2D) location.getCoordinate());
        }

        for (int numOfAgentsFromProperty : numOfAgentsFromProperties) {
            Agent[] agents = getAgentsFromLines(agentLines, numOfAgentsFromProperty, canonicalCoordinates);
            if (mapName == null || agents == null) {
                continue; /* Invalid parameters */
            }

            Set<I_Coordinate> coordinatesForSourcesOrTargets = new HashSet<>();
            for (Agent agent : agents) {
                coordinatesForSourcesOrTargets.add(agent.source);
                coordinatesForSourcesOrTargets.add(agent.target);
            }
            // to easily mostly guarantee reachability, get extra neighbors from neighbors of sources and targets, not random coordinates in the map
            coordinatesForSourcesOrTargets = addImmediateNeighbors(coordinatesForSourcesOrTargets, graphMap);

            if (randomWaypoints){
                agents = getAgentsWithRandomWaypoints(numOfAgentsFromProperty, moving_ai_path.scenarioPath, coordinatesForSourcesOrTargets, DEFAULT_NUM_WAYPOINTS);
            }

            if (forceNoSharedSourceAndFinalDestinations){
                agents = agentsToNoSharedSourceAndFinalDestinations(agents, moving_ai_path.scenarioPath, coordinatesForSourcesOrTargets);
            }


            mapf_instance = makeInstance(mapName, graphMap, agents, moving_ai_path);
            if (instanceProperties.regexPattern.matcher(mapf_instance.extendedName).matches()){
                mapf_instance.setObstaclePercentage(instanceProperties.obstacles.getReportPercentage());
                this.instanceList.add(mapf_instance);
            }
        }
    }

    private Set<I_Coordinate> addImmediateNeighbors(Set<I_Coordinate> sourceOrTargetCoordinates, GraphMap graphMap) {
        Set<I_Coordinate> res = new HashSet<>(sourceOrTargetCoordinates);
        for (I_Coordinate coordinate : sourceOrTargetCoordinates) {
            for (I_Location location : graphMap.getMapLocation(coordinate).outgoingEdges()) {
                res.add(location.getCoordinate());
            }
            for (I_Location location : graphMap.getMapLocation(coordinate).incomingEdges()) {
                res.add(location.getCoordinate());
            }
        }
        return res;
    }

    private Agent[] getAgentsWithRandomWaypoints(int numOfAgentsFromProperty, String scenarioPath, Set<I_Coordinate> usableCoordinates, int numWaypoints) {
        List<Agent> res = new ArrayList<>();
        String[] splitScenarioPath = scenarioPath.split(Pattern.quote(IO_Manager.pathSeparator));
        String scenarioName = splitScenarioPath[splitScenarioPath.length - 1];
        Random random = new Random(scenarioName.hashCode());
        for (int i = 0; i < numOfAgentsFromProperty; i++) {
            ArrayList<I_Coordinate> randomCoordinates = new ArrayList<>(usableCoordinates);
            Collections.shuffle(randomCoordinates, random);
            ArrayList<I_Coordinate> waypoints = new ArrayList<>();
            for (int j = 0; j < numWaypoints; j++) {
                waypoints.add(randomCoordinates.remove(0));
            }
            res.add(new LifelongAgent(i, waypoints.get(0), waypoints.get(waypoints.size()-1), waypoints.toArray(new I_Coordinate[0])));
        }
        return res.toArray(new Agent[0]);
    }

    private Agent[] agentsToNoSharedSourceAndFinalDestinations(Agent[] agents, String scenarioPath, Set<I_Coordinate> usableCoordinates) {
        if (this.lifelong){
            String[] splitScenarioPath = scenarioPath.split(Pattern.quote(IO_Manager.pathSeparator));
            String scenarioName = splitScenarioPath[splitScenarioPath.length-1];
            ArrayList<I_Coordinate> coordinateQueue = new ArrayList<>(usableCoordinates);
            Collections.shuffle(coordinateQueue, new Random(scenarioName.hashCode()));

            List<Agent> res = new ArrayList<>();
            for (Agent a :
                    agents) {
                if (coordinateQueue.isEmpty()){
                    break;
                }
                else {
                    List<I_Coordinate> waypoints = new ArrayList<>(((LifelongAgent)a).waypoints);
                    I_Coordinate uniqueCoordinate = coordinateQueue.remove(0);
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
        String[] splitScenarioPath = instancePath.scenarioPath.split(Pattern.quote(IO_Manager.pathSeparator));
        return new MAPF_Instance(instanceName, graphMap, agents, splitScenarioPath[splitScenarioPath.length-1]);
    }


    // Returns an array of agents using the line queue
    private Agent[] getAgentsFromLines(ArrayList<ArrayList<String>> agentLinesList, int numOfAgents, Set<Coordinate_2D> canonicalCoordinates) {
        if( agentLinesList == null){ return null; }
        agentLinesList.removeIf(Objects::isNull);
        Agent[] arrayOfAgents = new Agent[Math.min(numOfAgents,agentLinesList.size())];

        if(agentLinesList.isEmpty()){ return null; }

        // Iterate over all the agents in numOfAgents
        for (int id = 0; id < numOfAgents; id++) {
            if( id < arrayOfAgents.length ){
                LifelongAgent agentToAdd = new LifelongAgent(buildSingleAgentOffline(id ,agentLinesList.get(id), canonicalCoordinates),
                        buildSingleAgentWaypoints(agentLinesList.get(id), canonicalCoordinates));
                arrayOfAgents[id] =  agentToAdd; // Wanted agent to add
            }
        }
        return arrayOfAgents;
    }

    private Agent buildSingleAgentOffline(int id, ArrayList<String> agentLines, Set<Coordinate_2D> canonicalCoordinates) {
        String[] splitLineSource = agentLines.get(0).split(SEPARATOR_SCENARIO);
        String[] splitLineTarget = agentLines.get(agentLines.size()-1).split(SEPARATOR_SCENARIO);
        return new Agent(id, toCoor2D(splitLineSource[INDEX_XVALUE].strip(), splitLineSource[INDEX_YVALUE].strip(), canonicalCoordinates),
                toCoor2D(splitLineTarget[INDEX_XVALUE].strip(), splitLineTarget[INDEX_YVALUE].strip(), canonicalCoordinates));
    }

    /**
     * @return an array of waypoints for a lifelong agent.
     */
    private I_Coordinate[] buildSingleAgentWaypoints(ArrayList<String> agentLines, Set<Coordinate_2D> canonicalCoordinates) {
        I_Coordinate[] waypoints = new I_Coordinate[agentLines.size()];
        for (int i = 0; i < waypoints.length; i++) {
            String[] agentLineSplit = agentLines.get(i).split(SEPARATOR_SCENARIO);
            waypoints[i] = toCoor2D(agentLineSplit[INDEX_XVALUE].strip(), agentLineSplit[INDEX_YVALUE].strip(), canonicalCoordinates);
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
        Set<Coordinate_2D> canonicalCoordinates = new HashSet<>();

        JSONObject mapJobj = readJsonMapFile(instancePath);
        if (mapJobj == null) return null;

        Iterator<String> keys = mapJobj.keys();
        while(keys.hasNext()) {
            // first level of JSON - keys are coordinates/stickers/vertices like "12345_12345"
            String currentStickerCoordinatesString = keys.next();
            Coordinate_2D currentStickerCoordinate = toCoor2D(currentStickerCoordinatesString, canonicalCoordinates);
            // value - sticker data
            JSONArray vertexData = mapJobj.getJSONArray(currentStickerCoordinatesString);
            // first part of sticker data - a map with a key "tags" to array of tags
            JSONArray tagsJA = vertexData.getJSONObject(0).getJSONArray("tags");
            List<String> tagStrings = (List<String>)(Object)(tagsJA.toList()); // not currently doing anything with tags
            // second part of the sticker data - an array of edges
            JSONArray edgesJA = vertexData.getJSONArray(1);
            List<Coordinate_2D> neighbors = new ArrayList<>(edgesJA.length());
            List<Integer> edgeWeights = new ArrayList<>(edgesJA.length());
            if (coordinatesAdjacencyLists.containsKey(currentStickerCoordinate)){
//                throw new RuntimeException("Duplicate coordinate in map file: " + currentStickerCoordinate);
                neighbors = coordinatesAdjacencyLists.get(currentStickerCoordinate);
                edgeWeights = coordinatesEdgeWeights.get(currentStickerCoordinate);
            }
            for (int i = 0; i < edgesJA.length(); i++) {
                JSONArray currentEdge = edgesJA.getJSONArray(i);
                // edge is composed of a neighbor (coordinate) and an edge weight
                JSONArray neighborCoordinateJA = currentEdge.getJSONArray(0);
                Coordinate_2D neighborCoordinate = toCoor2D(neighborCoordinateJA.getInt(0), neighborCoordinateJA.getInt(1), canonicalCoordinates);
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
                                currentStickerCoordinate.y_value + (delta_y/realDistance)*j, canonicalCoordinates);
                        locationTypes.put(intermediateVertex, Enum_MapLocationType.EMPTY);
                        intermediateVertices.add(intermediateVertex);
                    }
                    intermediateVertices.add(neighborCoordinate);
                    if (!intermediateVertices.get(0).equals(currentStickerCoordinate)){ // avoid self edges as a result of merged coordinates
                        neighbors.add(intermediateVertices.get(0));
                        edgeWeights.add(1);
                    }
                    for (int j = 0; j < intermediateVertices.size() - 1; j++) {
                        if (! currentStickerCoordinate.equals(intermediateVertices.get(j))){ // avoid self edges as a result of merged coordinates
                            // avoid possibility of duplicates overriding each other because of going from both sides
                            List<Coordinate_2D> jNeighbors = coordinatesAdjacencyLists.computeIfAbsent(intermediateVertices.get(j), coor -> new ArrayList<>());
                            jNeighbors.add(intermediateVertices.get(j+1));
                            List<Integer> jNeighborsWeights = coordinatesEdgeWeights.computeIfAbsent(intermediateVertices.get(j), coor -> new ArrayList<>());
                            jNeighborsWeights.add(1);
                        }
                    }
                }
            }

            if (!neighbors.isEmpty()){
                coordinatesAdjacencyLists.put(currentStickerCoordinate, neighbors);
                coordinatesEdgeWeights.put(currentStickerCoordinate, edgeWeights);
                locationTypes.put(currentStickerCoordinate, Enum_MapLocationType.EMPTY);
            }
        }

        if (forceEdgesBidirectional){
            for (Coordinate_2D coordinate : new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
                List<Coordinate_2D> neighbors = coordinatesAdjacencyLists.get(coordinate);
                for (int neighborIndex = 0; neighborIndex < neighbors.size(); neighborIndex++) {
                    Coordinate_2D neighborOutgoingEdge = neighbors.get(neighborIndex);
                    int neighborOutgoingEdgeWeight = coordinatesEdgeWeights.get(coordinate).get(neighborIndex);

                    // add reverse edge
                    List<Coordinate_2D> neighborNeighbors = coordinatesAdjacencyLists.get(neighborOutgoingEdge);
                    List<Integer> neighborNeighborsWeights = coordinatesEdgeWeights.get(neighborOutgoingEdge);
                    if (neighborNeighbors != null && neighborNeighborsWeights != null){
                        if (!neighborNeighbors.contains(coordinate)){
                            neighborNeighbors.add(coordinate);
                            neighborNeighborsWeights.add(neighborOutgoingEdgeWeight);
                        }
                    }
                    else {
                        throw new IllegalStateException("Missing neighbor: " + neighborOutgoingEdge);
                    }
                }
            }
        }

        // validate
        for (Coordinate_2D coordinate1 : new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
            for (Coordinate_2D coordinate2 : new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
                if (coordinate1 != coordinate2 && isWithinThreshold(coordinate1, coordinate2)){
                    throw new IllegalStateException("Found duplicate coordinates: " + coordinate1 + " and " + coordinate2);
                }
            }
        }

        return MapFactory.newArbitraryGraphMap(coordinatesAdjacencyLists, coordinatesEdgeWeights,
                locationTypes, true);
    }

    private static boolean isWithinThreshold(Coordinate_2D coordinate1, Coordinate_2D coordinate2) {
        return coordinate2.x_value > coordinate1.x_value - MERGE_COORDINATES_THRESHOLD && coordinate2.x_value < coordinate1.x_value + MERGE_COORDINATES_THRESHOLD
                && coordinate2.y_value > coordinate1.y_value - MERGE_COORDINATES_THRESHOLD && coordinate2.y_value < coordinate1.y_value + MERGE_COORDINATES_THRESHOLD;
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

    private Coordinate_2D toCoor2D(String coorString, Set<Coordinate_2D> canonicalCoordinates){
        String[] coorstrings = coorString.split("_");
        return toCoor2D(coorstrings[0], coorstrings[1], canonicalCoordinates);
    }

    private Coordinate_2D toCoor2D(String xString, String yString, Set<Coordinate_2D> canonicalCoordinates){
        return toCoor2D(Integer.parseInt(xString), Integer.parseInt(yString), canonicalCoordinates);
    }

    private Coordinate_2D toCoor2D(int xInt, int yInt, Set<Coordinate_2D> canonicalCoordinates){
        Coordinate_2D newCoordinate = new MillimetricCoordinate_2D(xInt, yInt);
        for (Coordinate_2D canonicalCoordinate:
             canonicalCoordinates) {
            if (isWithinThreshold(newCoordinate, canonicalCoordinate)){
                return canonicalCoordinate;
            }
        }
        canonicalCoordinates.add(newCoordinate);
        return newCoordinate;
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
                String[] splitPath = instancePath.path.split(Pattern.quote(IO_Manager.pathSeparator));
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
