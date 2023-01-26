package BasicMAPF.TestConstants;

import BasicMAPF.Instances.Maps.*;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;

import java.util.*;

public class Maps {
    private final static Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final static Enum_MapLocationType w = Enum_MapLocationType.WALL;
    public static final  Enum_MapLocationType[][] map_2D_circle = {
            {w, w, w, w, w, w},
            {w, w, e, e, e, w},
            {w, w, e, w, e, w},
            {w, w, e, e, e, w},
            {w, w, w, w, w, w},
            {w, w, w, w, w, w},
    };
    public static final I_ExplicitMap mapCircle = MapFactory.newSimple4Connected2D_GraphMap(map_2D_circle);

    public static final Enum_MapLocationType[][] map_2D_empty = {
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
            {e, e, e, e, e, e},
    };
    public static final I_ExplicitMap mapEmpty = MapFactory.newSimple4Connected2D_GraphMap(map_2D_empty);

    public static final Enum_MapLocationType[][] map_2D_withPocket = {
            {e, w, e, w, e, w},
            {e, w, e, e, e, e},
            {w, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, e, w, e, e, e},
    };
    public static final I_ExplicitMap mapWithPocket = MapFactory.newSimple4Connected2D_GraphMap(map_2D_withPocket, false);

    public static final Enum_MapLocationType[][] map_2D_smallMaze = {
            {e, e, e, w, e, w},
            {e, w, e, e, e, e},
            {e, w, e, w, w, e},
            {e, e, e, e, e, e},
            {e, e, w, e, w, w},
            {w, w, w, e, e, e},
    };
    public static final I_ExplicitMap mapSmallMaze = MapFactory.newSimple4Connected2D_GraphMap(map_2D_smallMaze);

    public static final Enum_MapLocationType[][] map_2D_H = {
            { e, w, w, e},
            { e, e, e, e},
            { e, w, w, e},
    };
    public static final I_ExplicitMap mapH = MapFactory.newSimple4Connected2D_GraphMap(map_2D_H);

    public static final Enum_MapLocationType[][] twoLocationMap = new Enum_MapLocationType[][]{{e,e}};
    public static final I_ExplicitMap mapTwoLocations = MapFactory.newSimple4Connected2D_GraphMap(twoLocationMap);

    public static final Enum_MapLocationType[][] map_2D_H_long = {
            {e, w, w, e},
            {e, w, w, e},
            {e, e, e, e},
            {e, w, w, e},
            {e, w, w, e},
    };
    public static final I_ExplicitMap mapHLong = MapFactory.newSimple4Connected2D_GraphMap(map_2D_H_long);
    
    public static final I_ExplicitMap randomArbitraryGraphMap1 = createRandomStronglyConnectedGraphMap(42, 10, 4, true);
    public static final I_ExplicitMap randomArbitraryGraphMap2 = createRandomStronglyConnectedGraphMap(43, 20, 2, true);
    public static final I_ExplicitMap randomArbitraryGraphMap3 = createRandomStronglyConnectedGraphMap(44, 5, 5, true);
    public static final I_ExplicitMap randomArbitraryGraphMap4 = createRandomStronglyConnectedGraphMap(45, 2, 1, true);
    public static final I_ExplicitMap randomArbitraryGraphMap5 = createRandomStronglyConnectedGraphMap(46, 20, 1, true);

    private static I_ExplicitMap createRandomStronglyConnectedGraphMap(int seed, int maxWeight, int connectivityFactor, boolean enforceStronglyConnected) {
        int weightBound = maxWeight + 1;
        Map<Coordinate_2D, List<Coordinate_2D>> coordinatesAdjacencyLists = new HashMap<>();
        Map<Coordinate_2D, List<Integer>> coordinatesEdgeWeights = new HashMap<>();
        Map<Coordinate_2D, Enum_MapLocationType> coordinatesLocationTypes = new HashMap<>();
        Random rand = new Random(seed);

        List<Coordinate_2D> coordinates = new ArrayList<>();
        for (I_Location loc:
             mapCircle.getAllLocations()) {
            I_Coordinate coor = loc.getCoordinate();
            coordinates.add((Coordinate_2D) coor);
        }

        Coordinate_2D coor = coordinates.get(rand.nextInt(coordinates.size()));
        Set<Coordinate_2D> coordinatesNeedingNeighbors = new HashSet<>();
        while (coor != null){
            List<Coordinate_2D> neighbors = new ArrayList<>(coordinates);
            neighbors.remove(coor);
            Collections.shuffle(neighbors, rand);
            neighbors = neighbors.subList(0, 1 + Math.min(rand.nextInt(coordinates.size()/connectivityFactor), coordinates.size() - 2));
            coordinatesAdjacencyLists.put(coor, neighbors);

            List<Integer> randomWeights = new ArrayList<>();
            for (int i = 0; i < neighbors.size(); i++) {
                randomWeights.add(rand.nextInt(weightBound));
            }
            coordinatesEdgeWeights.put(coor, randomWeights);

            coordinatesLocationTypes.put(coor, Enum_MapLocationType.EMPTY);

            for (Coordinate_2D neighbor :
                    neighbors) {
                if (!coordinatesAdjacencyLists.containsKey(neighbor)) {
                    coordinatesNeedingNeighbors.add(neighbor);
                }
            }

            // sample the next coordinate that will get neighbors
            coordinatesNeedingNeighbors.remove(coor);
            coor = null;
            for (Coordinate_2D needsNeighbors :
                    coordinatesNeedingNeighbors) {
                coor = needsNeighbors;
                break;
            }
        }

        if (enforceStronglyConnected){
            // add reverse of every edge (with unrelated random weight)
            for (Coordinate_2D coordinate:
                    new ArrayList<>(coordinatesAdjacencyLists.keySet())) {
                List<Coordinate_2D> neighbors = coordinatesAdjacencyLists.get(coordinate);
                for (Coordinate_2D neighbor:
                        neighbors) {
                    List<Coordinate_2D> neighborsOfNeighbor = coordinatesAdjacencyLists.get(neighbor);
                    if (! neighborsOfNeighbor.contains(coordinate)){
                        neighborsOfNeighbor.add(coordinate);
                        coordinatesEdgeWeights.get(neighbor).add(rand.nextInt(weightBound));
                    }
                }
            }
        }

        return MapFactory.newArbitraryGraphMap(coordinatesAdjacencyLists, coordinatesEdgeWeights, coordinatesLocationTypes, true);
    }

    public static final Map<I_ExplicitMap, String> singleStronglyConnectedComponentMapsWithNames = Map.of(
            mapCircle, "mapCircle", mapEmpty, "mapEmpty", mapSmallMaze, "mapSmallMaze", mapH, "mapH",
            mapHLong, "mapHLong", randomArbitraryGraphMap1, "randomArbitraryGraphMap1",
            randomArbitraryGraphMap2, "randomArbitraryGraphMap2", randomArbitraryGraphMap3, "randomArbitraryGraphMap3",
            randomArbitraryGraphMap4, "randomArbitraryGraphMap4", randomArbitraryGraphMap5, "randomArbitraryGraphMap5"
    );

    public static final Map<I_ExplicitMap, String> singleStronglyConnectedComponentGridMapsWithNames = Map.of(
            mapCircle, "mapCircle", mapEmpty, "mapEmpty", mapSmallMaze, "mapSmallMaze", mapH, "mapH",
            mapHLong, "mapHLong"
    );
}
