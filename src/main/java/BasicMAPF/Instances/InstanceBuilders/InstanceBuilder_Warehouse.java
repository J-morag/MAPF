package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.MillimetricCoordinate_2D;
import Environment.IO_Package.IO_Manager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;


public class InstanceBuilder_Warehouse implements I_InstanceBuilder{

    /*  =constants=  */

    public static final MapDimensions.Enum_mapOrientation ENUMMAP_ORIENTATION = MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL;
    public static final int STICKER_DISTANCE_UNIT_MM = 490;
    public static final String FILE_TYPE_MAP = ".json";
    public static final String FILE_TYPE_SCENARIO = ".csv";
    public static final int MERGE_COORDINATES_THRESHOLD = 100;

    /*  =Default Values=    */
    private final int defaultNumOfAgents = 10;

    /* = Fields = */

    private final ArrayList<MAPF_Instance> instanceList = new ArrayList<>();

    /* = Instance Fields = */

    private final boolean dropDisabledEdges;
    private final boolean forceEdgesBidirectional;
    private final ScenarioBuilder_Warehouse scenarioReader = new ScenarioBuilder_WarehouseGenerative();

    public InstanceBuilder_Warehouse() {
        this(null, null);
    }

    public InstanceBuilder_Warehouse(Boolean dropDisabledEdges, Boolean forceEdgesBidirectional) {
        this.dropDisabledEdges = Objects.requireNonNullElse(dropDisabledEdges, true);
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

        populateAgents(mapName, instanceProperties, moving_ai_path, graphMap, numOfAgentsFromProperties);
    }

    private GraphMap getMap( InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties ){
        Map<Coordinate_2D, List<Coordinate_2D>> coordinatesAdjacencyLists = new HashMap<>();
        Map<Coordinate_2D, List<Integer>> coordinatesEdgeWeights = new HashMap<>();
        Map<Coordinate_2D, Enum_MapLocationType> coordinatesLocationType = new HashMap<>();
        Map<Coordinate_2D, List<String>> coordinatesLocationSubtypes = new HashMap<>();
        Set<Coordinate_2D> canonicalCoordinates = new HashSet<>();

        JSONObject mapJobj = readJsonMapFile(instancePath);
        if (mapJobj == null) return null;

        Iterator<String> keys = mapJobj.keys();
        while(keys.hasNext()) {
            // first level of JSON - keys are coordinates/stickers/vertices like "12345_12345"
            String currentStickerCoordinatesString = keys.next();
            Coordinate_2D currentStickerCoordinate = ScenarioBuilder_Warehouse.toCoor2D(currentStickerCoordinatesString, canonicalCoordinates);
            // value - sticker data
            JSONArray vertexData = mapJobj.getJSONArray(currentStickerCoordinatesString);
            // first part of sticker data - a map with a key "tags" to array of tags
            JSONArray tagsJA = vertexData.getJSONObject(0).getJSONArray("tags");
            List<String> tagStrings = (List<String>)(Object)(tagsJA.toList());
            coordinatesLocationSubtypes.put(currentStickerCoordinate, tagStrings);
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
                        coordinatesLocationType.put(intermediateVertex, Enum_MapLocationType.EMPTY);
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
                coordinatesLocationType.put(currentStickerCoordinate, Enum_MapLocationType.EMPTY);
            }
        }

        if (forceEdgesBidirectional){
            forceEdgesBidirectional(coordinatesAdjacencyLists, coordinatesEdgeWeights);
        }

        validateMapConstruction(coordinatesAdjacencyLists);

        return MapFactory.newArbitraryGraphMap(coordinatesAdjacencyLists, coordinatesEdgeWeights,
                coordinatesLocationType, true, coordinatesLocationSubtypes);
    }

    private static void validateMapConstruction(Map<Coordinate_2D, List<Coordinate_2D>> coordinatesAdjacencyLists) {
        for (Coordinate_2D coordinate1 : new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
            for (Coordinate_2D coordinate2 : new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
                if (coordinate1 != coordinate2 && isWithinThreshold(coordinate1, coordinate2)){
                    throw new IllegalStateException("Found duplicate coordinates: " + coordinate1 + " and " + coordinate2);
                }
            }
        }
    }

    private static void forceEdgesBidirectional(Map<Coordinate_2D, List<Coordinate_2D>> coordinatesAdjacencyLists, Map<Coordinate_2D, List<Integer>> coordinatesEdgeWeights) {
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

    private void populateAgents(@NotNull String instanceName, InstanceProperties instanceProperties,
                                InstanceManager.Moving_AI_Path moving_ai_path, GraphMap graphMap, int[] numOfAgentsFromProperties) {
        MAPF_Instance mapf_instance;

        Set<Coordinate_2D> canonicalCoordinates = new HashSet<>();
        for (I_Location location : graphMap.getAllLocations()) {
            canonicalCoordinates.add((Coordinate_2D) location.getCoordinate());
        }

        Agent[] allAgents = scenarioReader.getAgents(moving_ai_path, Arrays.stream(numOfAgentsFromProperties).max().getAsInt(), canonicalCoordinates, graphMap);
        if (allAgents == null) {
            return;
        }

        for (int numOfAgentsFromProperty : numOfAgentsFromProperties) {
            if (numOfAgentsFromProperty > allAgents.length) {
                numOfAgentsFromProperty = allAgents.length;
            }
            Agent[] agents = Arrays.copyOfRange(allAgents, 0, numOfAgentsFromProperty);
            mapf_instance = makeInstance(instanceName, graphMap, agents, moving_ai_path);
            if (instanceProperties.regexPattern.matcher(mapf_instance.extendedName).matches()){
                mapf_instance.setObstaclePercentage(instanceProperties.obstacles.getReportPercentage());
                this.instanceList.add(mapf_instance);
            }
        }
    }

    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManager.Moving_AI_Path instancePath){
        String[] splitScenarioPath = instancePath.scenarioPath.split(Pattern.quote(IO_Manager.pathSeparator));
        return new MAPF_Instance(instanceName, graphMap, agents, splitScenarioPath[splitScenarioPath.length-1]);
    }

    public static Coordinate_2D toCoor2D(int xInt, int yInt, Set<Coordinate_2D> canonicalCoordinates){
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

    private static boolean isWithinThreshold(Coordinate_2D coordinate1, Coordinate_2D coordinate2) {
        return coordinate2.x_value > coordinate1.x_value - MERGE_COORDINATES_THRESHOLD && coordinate2.x_value < coordinate1.x_value + MERGE_COORDINATES_THRESHOLD
                && coordinate2.y_value > coordinate1.y_value - MERGE_COORDINATES_THRESHOLD && coordinate2.y_value < coordinate1.y_value + MERGE_COORDINATES_THRESHOLD;
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
