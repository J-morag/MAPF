package BasicCBS.Solvers;

import BasicCBS.Instances.Maps.I_Location;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Stack;

/**
 * Implements Depth First Search.
 * Non thread safe!
 */
public class BreadthFirstSearch {

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
    public static boolean reachableFrom(I_Location goal, I_Location source) {
        // BFS algorithm
        open.offer(source);
        while(!open.isEmpty()){
            I_Location location = open.remove();
            visited.add(location);
            for (I_Location neighbour: location.getNeighbors()) {
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
