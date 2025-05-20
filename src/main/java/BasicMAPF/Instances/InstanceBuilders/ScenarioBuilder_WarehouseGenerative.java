package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceManagerFromFileSystem;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.GraphMap;
import BasicMAPF.Instances.Maps.I_Location;
import LifelongMAPF.LifelongAgent;
import LifelongMAPF.WaypointGenerators.WaypointsGeneratorFactory;
import com.google.common.collect.Collections2;
import org.apache.commons.collections4.CollectionUtils;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ScenarioBuilder_WarehouseGenerative extends ScenarioBuilder_Warehouse {

    private static final int DEBUG = 1;
    /**
     * The random seed to use whenever assigning destinations to agents in this scenario.
     */
    private int randomSeed;
    /**
     * The cycle of destination subtypes to use whenever assigning destinations to agents in this scenario.
     */
    private List<String> destinationSubtypesCycle;

    @Override
    public Agent[] getAgents(InstanceManagerFromFileSystem.Moving_AI_Path moving_ai_path, int numOfNeededAgents, Set<Coordinate_2D> canonicalCoordinates, GraphMap map, boolean lifelong) {
        readScenarioJson(moving_ai_path.scenarioPath);
        // TODO verify/make allLocations unique and deterministically ordered
        return getAgents(numOfNeededAgents, map.getAllLocations(), lifelong);
    }

    private void readScenarioJson(String scenarioPath) {
        // read json file
        JSONObject scenarioJobj;
        try {
            String scenarioStringJSON = Files.readString(Path.of(scenarioPath));
            scenarioJobj = new JSONObject(scenarioStringJSON);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error: IO error when attempting to read scenario file.");
            return;
        }

        randomSeed = scenarioJobj.getInt("random_seed");
        destinationSubtypesCycle = Collections.unmodifiableList((List<String>) (Object) (scenarioJobj.getJSONArray("cycle").toList()));
        if (destinationSubtypesCycle.size() < 2){
            throw new IllegalArgumentException("Cycle must contain at least 2 subtypes.");
        }
    }

    private Agent[] getAgents(int numAgents, Collection<? extends I_Location> allLocations, boolean lifelong) {
        if (numAgents > allLocations.size()){
            numAgents = allLocations.size();
        }

        Map<String, List<? extends I_Location>> locationBySubtype = getLocationsByType(allLocations);

        Random random = new Random(randomSeed);

        List<? extends I_Location> locationsWithSubtypesInCycle = new ArrayList<>(Collections2.filter(allLocations, loc -> {
            List<String> subtypes = loc.getSubtypes();
            return subtypes != null && CollectionUtils.containsAny(subtypes, destinationSubtypesCycle);
        }));
        if (numAgents > locationsWithSubtypesInCycle.size()){
            numAgents = locationsWithSubtypesInCycle.size();
        }

        List<I_Location> sourceLocations  = getRandomLocations(locationsWithSubtypesInCycle, numAgents, random);
        if (DEBUG >= 1){
            verifyUniqueSourceLocations(sourceLocations);
        }
        List<I_Location> possibleTargetLocations = getRandomLocations(locationsWithSubtypesInCycle, numAgents, random);

        Agent[] agents = new Agent[numAgents];

        for (int agentID = 0; agentID < numAgents; agentID++) {
            if (lifelong){
                getAgentLifelongInfiniteDestinations(sourceLocations, possibleTargetLocations, agentID, locationBySubtype, randomSeed, agents);
            }
            else {
                getAgentOffline(sourceLocations, agentID, possibleTargetLocations, agents);
            }
        }

        return agents;
    }

    private void verifyUniqueSourceLocations(List<I_Location> sourceLocations) {
        Set<I_Location> locationsSet = new HashSet<>(sourceLocations);
        if (locationsSet.size() < sourceLocations.size()){
            throw new RuntimeException("Source locations should be unique");
        }
        Set<I_Coordinate> coordinatesSet = new HashSet<>();
        for (I_Location loc :
                sourceLocations) {
            if (coordinatesSet.contains(loc.getCoordinate())){
                throw new RuntimeException("Coordinates should be unique");
            }
            coordinatesSet.add(loc.getCoordinate());
        }
    }

    private void getAgentLifelongInfiniteDestinations(List<I_Location> sourceLocations, List<I_Location> possibleTargetLocations, int agentID, Map<String, List<? extends I_Location>> locationBySubtype, int instanceSeed, Agent[] agents) {
        I_Location sourceLocation = sourceLocations.get(agentID);
        I_Location targetLocation = possibleTargetLocations.get(agentID);
        int agentSeed = instanceSeed * 9973 + agentID;
        agents[agentID] = new LifelongAgent(agentID, sourceLocation.getCoordinate(), targetLocation.getCoordinate(), new WaypointsGeneratorFactory(agentSeed, locationBySubtype, destinationSubtypesCycle, sourceLocation));
    }

    private void getAgentOffline(List<I_Location> sourceLocations, int agentID, List<I_Location> possibleTargetLocations, Agent[] agents) {
        I_Location sourceLocation = sourceLocations.get(agentID);
        I_Location targetLocation = getTargetLocation(sourceLocation, possibleTargetLocations);

        agents[agentID] = new Agent(agentID, sourceLocation.getCoordinate(), targetLocation.getCoordinate());
    }

    @NotNull
    private Map<String, List<? extends I_Location>> getLocationsByType(Collection<? extends I_Location> allLocations) {
        Map<String, List<? extends I_Location>> locationBySubtype = new HashMap<>();
        for (String subtypeInCycle :
                destinationSubtypesCycle) {
            locationBySubtype.computeIfAbsent(subtypeInCycle,
                    k -> new ArrayList<>(Collections2.filter(allLocations, loc -> {
                        List<String> subtypes = loc.getSubtypes();
                        return subtypes != null && subtypes.contains(subtypeInCycle);
                    })));
            if (locationBySubtype.get(subtypeInCycle).size() < Collections.frequency(destinationSubtypesCycle, subtypeInCycle)){
                throw new IllegalArgumentException("subtype " + subtypeInCycle + " appears too many times ("
                        + Collections.frequency(destinationSubtypesCycle, subtypeInCycle) +") in the cycle or too few times ("
                        + locationBySubtype.get(subtypeInCycle).size() + ") in the map.");
            }
            if (locationBySubtype.get(subtypeInCycle).size() < 2){
                throw new IllegalArgumentException("Each location subtype in the cycle must correspond to at least 2 " +
                        "locations in the map to avoid immediately repeated destinations");
            }
        }
        return locationBySubtype;
    }

    private I_Location getTargetLocation(I_Location sourceLocation, List<I_Location> possibleTargetLocations) {
        String targetLocationDesiredSubtype = null;
        I_Location targetLocation = null;

        for (String subtypeInCycle :
                destinationSubtypesCycle) {
            if (sourceLocation.getSubtypes() != null && sourceLocation.getSubtypes().contains(subtypeInCycle)){
                int idx = destinationSubtypesCycle.indexOf(subtypeInCycle);
                int nextIdx = (idx + 1) % destinationSubtypesCycle.size();
                targetLocationDesiredSubtype = destinationSubtypesCycle.get(nextIdx);
            }
        }

        if (targetLocationDesiredSubtype == null) {
            targetLocation = possibleTargetLocations.remove(0);
        } else {
            for (int i = 0; i < possibleTargetLocations.size(); i++) {
                List<String> subtypes = possibleTargetLocations.get(i).getSubtypes();
                if (subtypes != null && subtypes.contains(targetLocationDesiredSubtype)){
                    targetLocation = possibleTargetLocations.remove(i);
                    break;
                }
            }
        }
        if (targetLocation == null){
            targetLocation = possibleTargetLocations.remove(0);
        }
        return targetLocation;
    }

    private static List<I_Location> getRandomLocations(Collection<? extends I_Location> locations, int k, Random random) {
        if (k > locations.size()) {
            throw new IllegalArgumentException("Cannot choose " + k + " unique locations from a collection of size " + locations.size());
        }

        List<I_Location> shuffledList = new ArrayList<>(locations);
        Collections.shuffle(shuffledList, random);

        return shuffledList.subList(0, k);
    }
}
