package TransientMAPF;

import BasicMAPF.Instances.Maps.*;

import java.util.*;

public final class SeparatingVerticesFinder {

    public static Set<I_Location> findSeparatingVertices(I_ExplicitMap map) {

        Set<I_Location> separatingVertices = new HashSet<>();
        // Initialize necessary data structures for Tarjan's algorithm
        Map<I_Location, Integer> disc = new HashMap<>(); // Discovery time of visited vertices
        Map<I_Location, Integer> low = new HashMap<>();  // Lowest reachable discovery time
        Map<I_Location, Boolean> visited = new HashMap<>(); // Visited vertices
        Map<I_Location, Boolean> ap = new HashMap<>(); // Articulation points
        Map<I_Location, I_Location> parent = new HashMap<>(); // Parent vertices in DFS tree

        // Initialize for all vertices in the map
        for (I_Location location : map.getAllLocations()) {
            disc.put(location, -1);
            low.put(location, -1);
            parent.put(location, null);
            visited.put(location, false);
            ap.put(location, false);
        }

        int time = 0;
        // Run DFS for each unvisited vertex
        for (I_Location location : map.getAllLocations()) {
            if (!visited.get(location)) {
                dfsIterative(location, visited, disc, low, parent, ap, time);
            }
        }

        // Collect all articulation points
        for (I_Location location : ap.keySet()) {
            if (ap.get(location)) {
                separatingVertices.add(location);
            }
        }

        return separatingVertices;
    }

    private static void dfsIterative(I_Location start, Map<I_Location, Boolean> visited, Map<I_Location, Integer> disc,
                              Map<I_Location, Integer> low, Map<I_Location, I_Location> parent, Map<I_Location, Boolean> ap, int time) {
        // Stack to store vertices and their state
        // the stack aims to simulate recursion
        Stack<DFSState> stack = new Stack<>();
        stack.push(new DFSState(start, null, 0));

        while (!stack.isEmpty()) {
            DFSState current = stack.peek();
            I_Location currentLocation = current.location;

            if (!visited.get(currentLocation)) {
                // First time visiting this vertex
                // set vertex to visited and update its discovery time
                visited.put(currentLocation, true);
                low.put(currentLocation, ++time);
                disc.put(currentLocation, low.get(currentLocation));
            }

            // Process next unprocessed neighbor
            boolean foundNewNeighbor = false;
            List<I_Location> neighbors = currentLocation.outgoingEdges();

            while (current.neighborIndex < neighbors.size() && !foundNewNeighbor) {
                I_Location neighbor = neighbors.get(current.neighborIndex++);

                if (!visited.get(neighbor)) {
                    // Found an unvisited neighbor
                    parent.put(neighbor, currentLocation);
                    current.children++;
                    stack.push(new DFSState(neighbor, currentLocation, 0));
                    foundNewNeighbor = true;

                } else if (!neighbor.equals(current.parent)) { // already visited neighbor - update low value according to Tarjan's algorithm.
                    // Back edge case
                    low.put(currentLocation, Math.min(low.get(currentLocation), disc.get(neighbor)));
                }
            }

            if (!foundNewNeighbor) {
                // All neighbors processed, backtrack
                stack.pop();

                if (!stack.isEmpty()) {
                    // Update parent's low value
                    DFSState parentState = stack.peek();
                    I_Location parentLocation = parentState.location;
                    low.put(parentLocation, Math.min(low.get(parentLocation), low.get(currentLocation)));
                    if (parent.get(parentLocation) == null && parentState.children > 1) {
                        // Root with multiple children
                        ap.put(parentLocation, true);
                    }
                    if (parent.get(parentLocation) != null && low.get(currentLocation) >= disc.get(parentLocation)) {
                        // Non-root vertex with child having low value >= discovery time
                        ap.put(parentLocation, true);
                    }
                }
            }
        }
    }

    // Helper class to maintain state for each vertex in the stack
    private static class DFSState {
        I_Location location;
        I_Location parent;

        // Pointer to the next neighbor to check
        int neighborIndex;
        int children;

        DFSState(I_Location location, I_Location parent, int neighborIndex) {
            this.location = location;
            this.parent = parent;
            this.neighborIndex = neighborIndex;
            this.children = 0;
        }
    }
}
