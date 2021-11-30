package BasicCBS.Solvers;

import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.I_Location;

import java.util.*;

/**
 * Includes static implementations of searches that don't include state-time.
 * Because states do not include time, these are single-agent searches. They don't support things like constraints, as
 * those are time-dependant. Consequently, they search a significantly smaller state-space. Additionally, these searches
 * terminate even if the goal is unreachable (returning an appropriate value to indicate that).
 * Non thread safe!
 */
public class NoStateTimeSearches {

    // these being class static makes this class not thread safe
    private static final HashSet<I_Location> visited = new HashSet<>();
    private static final Queue<I_Location> open = new ArrayDeque<>();


    /**
     * Performs a light version of breadth first search to determine if a location is reachable from another location.
     * Does not save the path to get to goal.
     * @param goal once this location is found, the search stops and the function returns.
     * @param source the search starts at this location
     * @return true iff the goal is reachable form the source.
     */
    public static boolean BFSReachableFrom(I_Location goal, I_Location source) {
        // BFS algorithm
        return basicSearch(goal, source, open);
    }

    /**
     * Performs a light version of Pure Heuristic Search to determine if a location is reachable from another location.
     * Does not save the path to get to goal.
     * Uses {@link BasicCBS.Instances.Maps.Coordinates.I_Coordinate#distance(I_Coordinate)} as the heuristic.
     * @param goal once this location is found, the search stops and the function returns.
     * @param source the search starts at this location
     * @return true iff the goal is reachable form the source.
     */
    public static boolean PHSReachableFrom(I_Location goal, I_Location source) {
        Comparator<I_Location> fComparator = Comparator.comparingDouble((I_Location loc) -> loc.getCoordinate().distance(goal.getCoordinate()));
        PriorityQueue<I_Location> openPriority = new PriorityQueue<>(fComparator);
        // PHS algorithm
        return basicSearch(goal, source, openPriority);
    }

    private static boolean basicSearch(I_Location goal, I_Location source, Queue<I_Location> open) {
        open.offer(source);
        while(!open.isEmpty()){
            I_Location location = open.remove();
            visited.add(location);
            for (I_Location neighbour: location.outgoingEdges()) {
                // must compare coordinates since these locations might come from different copies of the map and thus won't be equal
                if(neighbour.getCoordinate().equals(goal.getCoordinate())){
                    // found (reachable)
                    return finish(true);
                }
                if(!visited.contains(neighbour)){
                    open.offer(neighbour);
                }
            }
        }
        // they are in disjoint graph components
        return finish(false);
    }

    /**
     * cleans up and returns returnVal. clean up eagerly.
     * @param returnVal will return this.
     * @return returnVal
     */
    private static boolean finish(boolean returnVal){
        visited.clear();
        open.clear();
        return returnVal;
    }

}
