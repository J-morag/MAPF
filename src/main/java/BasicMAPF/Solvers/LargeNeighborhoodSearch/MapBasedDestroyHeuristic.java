package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.ArrayMap;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MapBasedDestroyHeuristic implements I_DestroyHeuristic {

    private I_Map cachedMap;
    private List<I_Location> cachedMapIntersections;

    @Override
    public List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map) {
        Map<I_Location, List<AgentTime>> intersectionsToAgentsSortedByTime = getIntersectionsToAgentsSortedByTime(currentSolution, map);

        List<I_Location> allIntersections = getAndCacheAllIntersections(map, intersectionsToAgentsSortedByTime);
        if (allIntersections.isEmpty()){
            return new ArrayList<>();
        }

        // start with a random intersection
        I_Location currLocation = allIntersections.get(rnd.nextInt(allIntersections.size()));

        Queue<I_Location> open = new ArrayDeque<>();
        open.add(currLocation);
        Set<I_Location> closed = new HashSet<>();

        Set<Agent> selectedAgents = new HashSet<>();

        while ((! open.isEmpty()) && selectedAgents.size() < neighborhoodSize){
            currLocation = open.poll();
            if (closed.contains(currLocation)){
                continue;
            }
            else {
                closed.add(currLocation);
            }

            if (isIntersection(currLocation)){
                addAgentsAtIntersectionByProximityToARandomTime(neighborhoodSize, rnd, intersectionsToAgentsSortedByTime, currLocation, selectedAgents);
            }

            Set<I_Location> children = new HashSet<>(currLocation.outgoingEdges());
            // we also traverse edges in reverse
            children.addAll(currLocation.incomingEdges());
            for (I_Location childLocation :
                    children) {
                if (! closed.contains(childLocation)){
                    open.add(childLocation);
                }
            }
        }

        return new ArrayList<>(selectedAgents);
    }

    private void addAgentsAtIntersectionByProximityToARandomTime(int neighborhoodSize, Random rnd, Map<I_Location, List<AgentTime>> intersectionsToAgentsSortedByTime, I_Location currLocation, Set<Agent> selectedAgents) {
        List<AgentTime> agentTimesSorted = intersectionsToAgentsSortedByTime.get(currLocation);
        if (agentTimesSorted == null){
            return;
        }
        int T = agentTimesSorted.get(agentTimesSorted.size()-1).t;
        int t = rnd.nextInt(T);

        // find the index where the time is closest to t
        int minDeltaIndex = getMinDeltaIndex(agentTimesSorted, t);

        // iterate from the min delta index outwards (left and right), always taking the current min delta
        int leftIndex = minDeltaIndex;
        int rightIndex = minDeltaIndex;
        while (selectedAgents.size() < neighborhoodSize && (leftIndex >= 0 || rightIndex < agentTimesSorted.size())){
            if (leftIndex < 0){ // only right is within bounds
                selectedAgents.add(agentTimesSorted.get(rightIndex).a);
                rightIndex++;
            }
            else if (rightIndex >= agentTimesSorted.size()){ // only left is within bounds
                selectedAgents.add(agentTimesSorted.get(leftIndex).a);
                leftIndex--;
            }
            else { // both are within bounds
                if (getTDeltaByIndex(agentTimesSorted, t, leftIndex) <= getTDeltaByIndex(agentTimesSorted, t, rightIndex)){
                    selectedAgents.add(agentTimesSorted.get(leftIndex).a);
                    leftIndex--;
                }
                else {
                    selectedAgents.add(agentTimesSorted.get(rightIndex).a);
                    rightIndex++;
                }

            }
        }
    }

    private int getMinDeltaIndex(List<AgentTime> agentTimesSorted, int t) {
        int minDeltaIndex = 0;
        for (int i = 0;
             i < agentTimesSorted.size() &&
                     getTDeltaByIndex(agentTimesSorted, t, i) <= getTDeltaByIndex(agentTimesSorted, t, minDeltaIndex)
                ; i++) {
            minDeltaIndex = i;
        }
        return minDeltaIndex;
    }

    private int getTDeltaByIndex(List<AgentTime> agentTimesSorted, int t, int index) {
        return Math.abs(agentTimesSorted.get(index).t - t);
    }

    @NotNull
    private Map<I_Location, List<AgentTime>> getIntersectionsToAgentsSortedByTime(Solution currentSolution, I_Map map) {
        Map<I_Location, List<AgentTime>> intersectionsToAgentsByTime = map instanceof I_ExplicitMap explicitMap ?
                new ArrayMap<>(explicitMap.getNumMapLocations()) : new HashMap<>();
        for (SingleAgentPlan p :
                currentSolution) {
            I_Location sourceLocation = p.getFirstMove().prevLocation;
            if (isIntersection(sourceLocation)){
                updateAgentsAtIntersection(intersectionsToAgentsByTime, p.agent, sourceLocation, p.getFirstMoveTime());
            }
            for (Move m:
                 p) {
                if (isIntersection(m.currLocation)){
                    updateAgentsAtIntersection(intersectionsToAgentsByTime, p.agent, m.currLocation, m.timeNow);
                }
            }
        }
        for (List<AgentTime> agentTimes: intersectionsToAgentsByTime.values()){
            Collections.sort(agentTimes);
        }

        return intersectionsToAgentsByTime;
    }

    /**
     * If possible, gets all intersections. Else, gets all intersections that agens visit.
     * Uses the cached result when possible.
     */
    private List<I_Location> getAndCacheAllIntersections(I_Map map, Map<I_Location, List<AgentTime>> intersectionsToAgentsSortedByTime) {
        List<I_Location> allIntersections;
        if (map instanceof I_ExplicitMap){
            if (cachedMap != null && cachedMap.equals(map)){
                allIntersections = cachedMapIntersections;
            }
            else{
                allIntersections = new ArrayList<>();
                for (I_Location location :
                        ((I_ExplicitMap) map).getAllLocations()) {
                    if (isIntersection(location)){
                        allIntersections.add(location);
                    }
                }
                cachedMap = map;
                cachedMapIntersections = allIntersections;
            }
        }
        else {
            // TODO conversion to list is non-deterministic? could lead to unrepeatable results?
            allIntersections = new ArrayList<>(intersectionsToAgentsSortedByTime.keySet());
        }
        return allIntersections;
    }

    private void updateAgentsAtIntersection(Map<I_Location, List<AgentTime>> intersectionsToAgents, Agent agent, I_Location intersection, int time) {
        List<AgentTime> agentsTimesAtIntersection = intersectionsToAgents.putIfAbsent(intersection, new ArrayList<>());
        if (agentsTimesAtIntersection == null){
            agentsTimesAtIntersection = intersectionsToAgents.get(intersection);
        }
        agentsTimesAtIntersection.add(new AgentTime(agent, time));
    }

    /**
     * The original definition is degree > 2, but assumes an undirected graph. We use (potentially) directed graphs, so
     * we define as in-degree > 2.
     * If a vertex of an undirected graph is an intersection under the original definition, it will also be an
     * intersection when the graph is converted to a directed graph and using the new definition.
     */
    private boolean isIntersection(I_Location location) {
        return location.incomingEdges().size() > 2;
    }

    @Override
    public void clear() {
        cachedMap = null;
        cachedMapIntersections = null;
    }

    /*  = private classes = */
    private static class AgentTime implements Comparable<AgentTime>{
        public final Agent a;
        public final int t;

        public AgentTime(Agent a, int t) {
            this.a = a;
            this.t = t;
        }

        @Override
        public int compareTo(@NotNull MapBasedDestroyHeuristic.AgentTime o) {
            return Integer.compare(this.t, o.t);
        }
    }

}
