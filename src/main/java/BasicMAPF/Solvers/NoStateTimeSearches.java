package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Includes static implementations of searches that don't include state-time.
 * Because states do not include time, these are single-agent searches. They don't support things like constraints, as
 * those are time-dependent. Consequently, they search a significantly smaller state-space. Additionally, these searches
 * terminate even if the goal is unreachable (returning an appropriate value to indicate that).
 */
public class NoStateTimeSearches {

    /**
     * Performs a light version of breadth first search to determine if a location is reachable from another location.
     * Does not save the path to get to goal.
     * @param goal once this location is found, the search stops and the function returns.
     * @param source the search starts at this location
     * @return true iff the goal is reachable form the source.
     */
    public static boolean BFSReachableFrom(I_Location goal, I_Location source) {
        // BFS algorithm
        return basicSearch(goal, source, new ArrayDeque<>(), true);
    }

    /**
     * Performs a light version of Pure Heuristic Search to determine if a location is reachable from another location.
     * Does not save the path to get to goal.
     * Uses {@link BasicMAPF.Instances.Maps.Coordinates.I_Coordinate#distance(I_Coordinate)} as the heuristic.
     * @param goal once this location is found, the search stops and the function returns.
     * @param source the search starts at this location
     * @return true iff the goal is reachable form the source.
     */
    public static boolean PHSReachableFrom(I_Location goal, I_Location source) {
        Comparator<I_Location> fComparator = Comparator.comparingDouble((I_Location loc) -> loc.getCoordinate().distance(goal.getCoordinate()));
        PriorityQueue<I_Location> openPriority = new PriorityQueue<>(fComparator);
        // PHS algorithm
        return basicSearch(goal, source, openPriority, true);
    }

    private static boolean basicSearch(I_Location goal, I_Location source, Queue<I_Location> open, boolean earlyGoalTest) {
        HashSet<I_Location> visited = new HashSet<>();
        open.offer(source);
        while(!open.isEmpty()){
            I_Location location = open.remove();
            if (visited.contains(location)){
                continue;
            }

            visited.add(location);
            if (!earlyGoalTest && simpleGoalTest(goal, location)){
                // found (reachable)
                return true;
            }
            for (I_Location neighbour: location.outgoingEdges()) {
                if(earlyGoalTest && simpleGoalTest(goal, neighbour)){
                    // found (reachable)
                    return true;
                }
                if(!visited.contains(neighbour)){
                    open.offer(neighbour);
                }
            }
        }
        // they are in disjoint graph components
        return false;
    }

    private static boolean simpleGoalTest(I_Location goal, I_Location location) {
        // must compare coordinates since these locations might come from different copies of the map and thus won't be equal
        return location.getCoordinate().equals(goal.getCoordinate());
    }

    public static List<I_Location> uniformCostSearch(I_Location goal, I_Location source, SingleAgentGAndH edgeCosts, Agent dummyAgent) {
        dummyAgent = Objects.requireNonNullElseGet(dummyAgent, () -> new Agent(0, source.getCoordinate(), goal.getCoordinate()));
        Map<I_Location, I_Location> parent = new HashMap<>();
        Map<I_Location, Integer> cummulativeCost = new HashMap<>();
        Comparator<I_Location> costComparator = Comparator.comparingInt(cummulativeCost::get);
        PriorityQueue<I_Location> open = new PriorityQueue<>(costComparator);
        HashSet<I_Location> visited = new HashSet<>();

        open.offer(source);
        parent.put(source, null);
        cummulativeCost.put(source, 0);
        while(!open.isEmpty()){
            I_Location location = open.remove();
            if (visited.contains(location)){
                continue;
            }
            visited.add(location);

            if (simpleGoalTest(goal, location)){
                return getLocationSequenceByBacktrack(parent, dummyAgent, location);
            }

            int locationCost = cummulativeCost.get(location);
            for (I_Location neighbour: location.outgoingEdges()) {
                int neighbourCost = locationCost + edgeCosts.cost(new Move(dummyAgent, 1, location, neighbour));
                if(!visited.contains(neighbour) && (!cummulativeCost.containsKey(neighbour) || neighbourCost < cummulativeCost.get(neighbour))){
                    // no need to remove the duplicate (if exists), because it would just have a higher cost, and we will
                    // ignore it when we pop it because it will be closed already

                    cummulativeCost.put(neighbour, neighbourCost);
                    parent.put(neighbour, location);
                    open.offer(neighbour);
                }
            }
        }

        return null;
    }

    @NotNull
    private static List<I_Location> getLocationSequenceByBacktrack(Map<I_Location, I_Location> parent, Agent dummyAgent, I_Location location) {
        ArrayList<I_Location> locations = new ArrayList<>();
        I_Location current = location;
        while (current != null){
            locations.add(current);
            current = parent.get(current);
        }
        Collections.reverse(locations);
        return locations;
    }
}
