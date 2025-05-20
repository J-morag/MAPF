package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.*;
import BasicMAPF.Instances.Maps.Coordinates.CoordinateNamed;
import BasicMAPF.Instances.Maps.*;
import Environment.Config;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.IO_Manager;
import Environment.IO_Package.Reader;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.regex.Pattern;


public class InstanceBuilder_ArbitraryGraph implements I_InstanceBuilder{

    /*  =constants=  */

    public static final String FILE_TYPE_MAP = ".edgelist";
    public static final String FILE_TYPE_SCENARIO = ".scene";

    /*  =Default Values=    */
    private static final int defaultNumOfAgents = 10;

    /* = Fields = */

    private final ArrayList<MAPF_Instance> instanceList = new ArrayList<>();

    /* = Instance Fields = */

    @Override
    public void prepareInstances(String mapName, InstanceManagerFromFileSystem.InstancePath instancePath, InstanceProperties instanceProperties) {
        if (!(instancePath instanceof InstanceManagerFromFileSystem.Moving_AI_Path)) { return; }

        InstanceManagerFromFileSystem.Moving_AI_Path moving_ai_path = (InstanceManagerFromFileSystem.Moving_AI_Path) instancePath;
        if( instanceProperties == null ){ instanceProperties = new InstanceProperties(); }

        GraphMap graphMap = getMap(moving_ai_path, instanceProperties);
        if( graphMap == null ){ return; }

        // create agent properties
        int[] numOfAgentsFromProperties = (instanceProperties.numOfAgents == null || instanceProperties.numOfAgents.length == 0
                ? new int[]{this.defaultNumOfAgents} : instanceProperties.numOfAgents);

        populateAgents(mapName, instanceProperties, moving_ai_path, graphMap, numOfAgentsFromProperties);
    }

    public GraphMap getMap(InstanceManagerFromFileSystem.InstancePath instancePath, InstanceProperties instanceProperties){
        Map<CoordinateNamed, List<CoordinateNamed>> coordinatesAdjacencyLists = new HashMap<>();
        Map<CoordinateNamed, List<Integer>> coordinatesEdgeWeights = new HashMap<>();
        Map<CoordinateNamed, Enum_MapLocationType> coordinatesLocationType = new HashMap<>();
        Map<CoordinateNamed, List<String>> coordinatesLocationSubtypes = new HashMap<>();
        Set<CoordinateNamed> canonicalCoordinates = new HashSet<>();

        // read the file line by line
        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(instancePath.path);
        if( !enum_io.equals(Enum_IO.OPENED) ){
            reader.closeFile();
            return null; /* couldn't open the file */
        }
        String nextLine = reader.getNextLine();

        boolean isUndirected = nextLine.contains("undirected");
        nextLine = reader.getNextLine();

        while ( nextLine != null ){
            // parse the line
            String[] splitLine = nextLine.split(" ");
            if (splitLine.length == 2){
                // coordinate and its neighbor
                CoordinateNamed coordinate1 = new CoordinateNamed(splitLine[0]);
                CoordinateNamed coordinate2 = new CoordinateNamed(splitLine[1]);
                canonicalCoordinates.add(coordinate1);
                canonicalCoordinates.add(coordinate2);
                coordinatesLocationType.put(coordinate1, Enum_MapLocationType.EMPTY);
                coordinatesLocationType.put(coordinate2, Enum_MapLocationType.EMPTY);
                coordinatesLocationSubtypes.put(coordinate1, Collections.emptyList());
                coordinatesLocationSubtypes.put(coordinate2, Collections.emptyList());
                // add the edge
                List<CoordinateNamed> neighbors = coordinatesAdjacencyLists.computeIfAbsent(coordinate1, coor -> new ArrayList<>());
                if (Config.DEBUG >= 1 && neighbors.contains(coordinate2)){
                    throw new IllegalStateException("Duplicate edge: " + coordinate1 + " -> " + coordinate2);
                }
                neighbors.add(coordinate2);
                List<Integer> edgeWeights = coordinatesEdgeWeights.computeIfAbsent(coordinate1, coor -> new ArrayList<>());
                if (Config.DEBUG >= 1 && edgeWeights.size() != neighbors.size()-1){
                    throw new IllegalStateException("Edge weights out of sync with neighbors: " + coordinate1 + " -> " + coordinate2);
                }
                edgeWeights.add(1);
                if (isUndirected){
                    neighbors = coordinatesAdjacencyLists.computeIfAbsent(coordinate2, coor -> new ArrayList<>());
                    if (Config.DEBUG >= 1 && neighbors.contains(coordinate1)){
                        throw new IllegalStateException("Duplicate edge: " + coordinate2 + " -> " + coordinate1);
                    }
                    neighbors.add(coordinate1);
                    edgeWeights = coordinatesEdgeWeights.computeIfAbsent(coordinate2, coor -> new ArrayList<>());
                    if (Config.DEBUG >= 1 && edgeWeights.size() != neighbors.size()-1){
                        throw new IllegalStateException("Edge weights out of sync with neighbors: " + coordinate2 + " -> " + coordinate1);
                    }
                    edgeWeights.add(1);
                }
            }
            nextLine = reader.getNextLine();
        }

        return MapFactory.newArbitraryGraphMap(coordinatesAdjacencyLists, coordinatesEdgeWeights,
                coordinatesLocationType, true, coordinatesLocationSubtypes);
    }

    private void populateAgents(@NotNull String instanceName, InstanceProperties instanceProperties,
                                InstanceManagerFromFileSystem.Moving_AI_Path moving_ai_path, GraphMap graphMap, int[] numOfAgentsFromProperties) {
        MAPF_Instance mapf_instance;

        HashMap<String, CoordinateNamed> canonicalCoordinates = new HashMap<>();
        for (I_Location location : graphMap.getAllLocations()) {
            canonicalCoordinates.put(((CoordinateNamed) location.getCoordinate()).name, (CoordinateNamed) location.getCoordinate());
        }

        List<String> agentLines = getAgentLines(moving_ai_path, Arrays.stream(numOfAgentsFromProperties).max().getAsInt());
        Agent[] allAgents = getAgents(agentLines, Arrays.stream(numOfAgentsFromProperties).max().getAsInt(), canonicalCoordinates, graphMap);
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

    private Agent[] getAgents(List<String> agentLines, int numOfAgentsFromProperties, HashMap<String, CoordinateNamed> canonicalCoordinates, GraphMap graphMap) {
        if( agentLines == null){ return null; }
        agentLines.removeIf(Objects::isNull);
        Agent[] arrayOfAgents = new Agent[Math.min(numOfAgentsFromProperties, agentLines.size())];

        if(agentLines.isEmpty()){ return null; }

        // Iterate over all the agents in numOfAgents
        for (int id = 0; id < arrayOfAgents.length; id++) {
            String[] splitLine = agentLines.get(id).split(" ");
            if (Config.DEBUG >= 1){
                if (splitLine.length != 3) throw new IllegalStateException("Invalid agent line: " + agentLines.get(id));
                if (Integer.parseInt(splitLine[0]) != id) throw new IllegalStateException("Invalid agent id: " + agentLines.get(id));
            }
            if (Config.DEBUG >= 1 && !canonicalCoordinates.containsKey(splitLine[1])){
                throw new IllegalStateException("Unknown coordinate: " + splitLine[1]);
            }
            if (Config.DEBUG >= 1 && !canonicalCoordinates.containsKey(splitLine[2])){
                throw new IllegalStateException("Unknown coordinate: " + splitLine[2]);
            }
            Agent agent = new Agent(id, canonicalCoordinates.get(splitLine[1]), canonicalCoordinates.get(splitLine[2]));
            arrayOfAgents[id] = agent;
        }
        return arrayOfAgents;
    }

    private List<String> getAgentLines(InstanceManagerFromFileSystem.Moving_AI_Path movingAiPath, int numOfNeededAgents) {
        // Open scenario file
        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(movingAiPath.scenarioPath);
        if( !enum_io.equals(Enum_IO.OPENED) ){
            reader.closeFile();
            return null; /* couldn't open the file */
        }

        ArrayList<String> agentsLines = new ArrayList<>(); // Init queue of agents lines

        String nextLine = reader.getNextLine();
        // Add lines as the num of needed agents
        for (int i = 0; nextLine != null && !nextLine.isEmpty() && i < numOfNeededAgents ; i++) {
            agentsLines.add(nextLine);
            nextLine = reader.getNextLine(); // next line
        }

        reader.closeFile();
        return agentsLines;
    }

    protected MAPF_Instance makeInstance(String instanceName, I_Map graphMap, Agent[] agents, InstanceManagerFromFileSystem.Moving_AI_Path instancePath){
        String[] splitScenarioPath = instancePath.scenarioPath.split(Pattern.quote(IO_Manager.pathSeparator));
        return new MAPF_Instance(instanceName, graphMap, agents, splitScenarioPath[splitScenarioPath.length-1]);
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
        return null;
    }

    @Override
    public InstanceManagerFromFileSystem.InstancePath[] getInstancesPaths(String directoryPath) {
        InstanceManagerFromFileSystem.InstancePath[] pathArray = IO_Manager.getFilesFromDirectory(directoryPath);
        if(pathArray == null){ return null; }

        ArrayList<InstanceManagerFromFileSystem.InstancePath> list = new ArrayList<>();

        for (InstanceManagerFromFileSystem.InstancePath instancePath : pathArray ) {
            if ( instancePath.path.endsWith(FILE_TYPE_MAP) ){
                String[] splitPath = instancePath.path.split(Pattern.quote(IO_Manager.pathSeparator));
                String mapPrefix = splitPath[splitPath.length-1].replace(FILE_TYPE_MAP, "");
                for (InstanceManagerFromFileSystem.InstancePath scenarioCandidate : pathArray ){
                    if(isRelevantScenarioFile(scenarioCandidate, mapPrefix)){
                        list.add( new InstanceManagerFromFileSystem.Moving_AI_Path(instancePath.path, scenarioCandidate.path));
                    }
                }
            }
        }

        pathArray = new InstanceManagerFromFileSystem.InstancePath[list.size()];
        for (int i = 0; i < pathArray.length; i++) {
            pathArray[i] = list.get(i);
        }
        return pathArray;
    }

    private static boolean isRelevantScenarioFile(InstanceManagerFromFileSystem.InstancePath scenarioCandidate, String mapPrefix) {
        String[] splitPath = scenarioCandidate.path.split(Pattern.quote(IO_Manager.pathSeparator));
        return splitPath[splitPath.length-1].replace(FILE_TYPE_SCENARIO, "").equals(mapPrefix) && scenarioCandidate.path.endsWith(FILE_TYPE_SCENARIO);
    }
}
