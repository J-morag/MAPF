package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.GraphMap;
import BasicMAPF.Instances.Maps.I_Location;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ScenarioBuilder_WarehouseGenerative extends ScenarioBuilder_Warehouse {

    /**
     * The random seed to use whenever assigning destinations to agents in this scenario.
     */
    private int randomSeed;
    /**
     * The cycle of destination subtypes to use whenever assigning destinations to agents in this scenario.
     */
    private List<String> destinationSubtypesCycle;

    @Override
    public Agent[] getAgents(InstanceManager.Moving_AI_Path moving_ai_path, int numOfNeededAgents, Set<Coordinate_2D> canonicalCoordinates, GraphMap map) {
        readScenarioJson(moving_ai_path.scenarioPath);
        return getAgents(numOfNeededAgents, map.getAllLocations());
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

    private Agent[] getAgents(int numOfNeededAgents, Collection<? extends I_Location> allLocations) {
        if (numOfNeededAgents > allLocations.size()){
            numOfNeededAgents = allLocations.size();
        }

        Map<String, List<? extends I_Location>> locationBySubtype = getLocationsByType(allLocations);

        Random random = new Random(randomSeed);

//        List<I_Location> sourceLocations = getRandomLocations(allLocations, numOfNeededAgents, random);
        List<? extends I_Location> sourceLocations = locationBySubtype.get(destinationSubtypesCycle.get(0));
        if (numOfNeededAgents > sourceLocations.size()){
            numOfNeededAgents = sourceLocations.size();
        }
        sourceLocations = getRandomLocations(locationBySubtype.get(destinationSubtypesCycle.get(0)), numOfNeededAgents, random);

        List<I_Location> possibleTargetLocations = getRandomLocations(allLocations, allLocations.size(), random);

        Agent[] agents = new Agent[numOfNeededAgents];

        for (int agentID = 0; agentID < numOfNeededAgents; agentID++) {
            I_Location sourceLocation = sourceLocations.get(agentID);
            I_Location targetLocation = getTargetLocation(sourceLocation, possibleTargetLocations);

            agents[agentID] = new Agent(agentID, sourceLocation.getCoordinate(), targetLocation.getCoordinate());

        }

        return agents;
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
