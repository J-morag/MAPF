package BasicMAPF.Instances.Maps;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

import java.util.*;

/**
 * Represents a {@link I_Map map} as an abstract graph. This implementation can, in principle, support any domain -
 * maps representing n dimensional space, any connectivity function, disjoint sub-graphs.
 *
 * Space complexity:
 * This implementation requires the entire graph to be built at initialization. For very large, sparse maps, this may
 * pose a space complexity challenge. Example: A 1000x1000x1000 map with just one agent, whose source and target are
 * adjacent.
 */
public class GraphMap implements I_ExplicitMap {

    private final HashMap<I_Coordinate, GraphMapVertex> allGraphLocations;
    private final boolean isStronglyConnected;

    /**
     * Initialization in {@link MapFactory}.
     * @param allGraphVertices a {@link HashMap} containing all locations in the graph.
     */
    GraphMap(HashMap<I_Coordinate, GraphMapVertex> allGraphVertices) {
        this(allGraphVertices, null);
    }

    /**
     * Initialization in {@link MapFactory}.
     * @param allGraphVertices a {@link HashMap} containing all locations in the graph.
     * @param isStronglyConnected - if the graph can be assumed to be strongly connected.
     */
    GraphMap(HashMap<I_Coordinate, GraphMapVertex> allGraphVertices, Boolean isStronglyConnected) {
        this.allGraphLocations = allGraphVertices;
        this.isStronglyConnected = Objects.requireNonNullElse(isStronglyConnected, true);
    }

    /**
     * Returns the {@link GraphMapVertex} for the given {@link I_Coordinate}.
     * @param coordinate the {@link I_Coordinate} of the {@link GraphMapVertex}.
     * @return the {@link GraphMapVertex} for the given {@link I_Coordinate}.
     */
    @Override
    public GraphMapVertex getMapLocation(I_Coordinate coordinate) {
        return allGraphLocations.get(coordinate);
    }

    @Override
    public HashMap<I_Coordinate, GraphMapVertex> getAllGraphLocations(){
        return allGraphLocations;
    }

    @Override
    public boolean isValidCoordinate(I_Coordinate coordinate) {
        return this.allGraphLocations.containsKey(coordinate);
    }

    @Override
    public I_Map getSubmapWithout(Collection<? extends I_Location> mapLocations) {
        // have to rebuild the entire map to remove the removed vertices from the neighbour lists of remaining vertices.

        HashMap<I_Coordinate, GraphMapVertex> vertexMappings = new HashMap<>();
        // populate with stub vertices (copies), except for vertices that we want to remove
        for (Map.Entry<I_Coordinate, GraphMapVertex> pair : this.allGraphLocations.entrySet()) {
            if(!mapLocations.contains(pair.getValue())){
                vertexMappings.put(pair.getKey(), new GraphMapVertex(pair.getValue().locationType, pair.getKey()));
            }
        }
        // now iterate over original vertices and copy over their neighbors, except for neighbors that were removed.
        for (Map.Entry<I_Coordinate, GraphMapVertex> pair : this.allGraphLocations.entrySet()) {
            I_Coordinate coor = pair.getKey();
            GraphMapVertex originalVertex = pair.getValue();
            if(!mapLocations.contains(originalVertex)){
                GraphMapVertex newVertex = vertexMappings.get(coor);
                List<GraphMapVertex> neighbors = new ArrayList<>();
                for (I_Location neighbor : originalVertex.outgoingEdges) {
                    if (! mapLocations.contains(neighbor)){
                        // getting the neighbor from the new vertices, not the original ones.
                        neighbors.add(vertexMappings.get(neighbor.getCoordinate()));
                    }
                }
                newVertex.setNeighbors(neighbors.toArray(new GraphMapVertex[0]));
            }
        }
        return new GraphMap(vertexMappings);
    }

    public int getNumMapLocations(){
        return allGraphLocations.size();
    }


    /**
     * O(n)
     */
    @Override
    public Collection<? extends I_Location> getAllLocations() {
        return new ArrayList<>(this.allGraphLocations.values());
    }

    @Override
    public boolean isStronglyConnected() {
        return this.isStronglyConnected;
    }
}
