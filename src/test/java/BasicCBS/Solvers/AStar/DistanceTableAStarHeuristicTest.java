package BasicCBS.Solvers.AStar;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.*;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DistanceTableAStarHeuristicTest {

    final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    final Enum_MapLocationType w = Enum_MapLocationType.WALL;
    Enum_MapLocationType[][] map_2D_H = {
            { e, w, w, e},
            { e, e, e, e},
            { e, w, w, e},
    };

    I_Map map= MapFactory.newSimple4Connected2D_GraphMap(map_2D_H);

    /*   = Equals Maps =    */
    private boolean equalsAllAgentMap(Map<I_Location, Map<I_Location, Integer>> expectedValues, Map<I_Location, Map<I_Location, Integer>> actualValues){

        if( expectedValues.size() != actualValues.size() ){
            return false;
        }
        for (Map.Entry<I_Location, Map<I_Location, Integer>> agentMapEntry: expectedValues.entrySet()){

            I_Location target = agentMapEntry.getKey();
            Map<I_Location, Integer> expectedLocationMap = expectedValues.get(target);
            Map<I_Location, Integer> actualLocationMap = actualValues.get(target);

            if (! this.equalsAllLocationMap(expectedLocationMap,actualLocationMap)){
                return false;
            }
        }
        return true;
    }

    private boolean equalsAllLocationMap(Map<I_Location, Integer> expectedLocationMap, Map<I_Location, Integer> actualLocationMap) {
        if( expectedLocationMap.size() != actualLocationMap.size() ){
            return false;
        }
        for (Map.Entry<I_Location,Integer> MapLocationEntry: expectedLocationMap.entrySet()){

            I_Location mapLocation = MapLocationEntry.getKey();
            int expectedDistance = expectedLocationMap.get(mapLocation);
            int actualDistance = actualLocationMap.get(mapLocation);

            if ( expectedDistance != actualDistance){
                return false;
            }
        }
        return true;
    }


    @Test
    public void test(){
        HashMap<I_Coordinate, I_Location> hashMap=new HashMap<>();

        Coordinate_2D[][] array=new Coordinate_2D[3][4];
        for(int i=0;i<array.length;i++){
            for(int j=0;j<array[0].length;j++){
                I_Location mapLocation= map.getMapLocation(new Coordinate_2D(i,j)); ///change to public
                hashMap.put(array[i][j],mapLocation);
            }
        }

        Coordinate_2D coordinate_2D_1=new Coordinate_2D(0,0);
        Coordinate_2D coordinate_2D_2=new Coordinate_2D(0,3);
        Coordinate_2D coordinate_2D_3=new Coordinate_2D(2,0);
        Coordinate_2D coordinate_2D_4=new Coordinate_2D(2,3);

        Agent agent_1=new Agent(1,coordinate_2D_1,coordinate_2D_2);
        Agent agent_2=new Agent(2,coordinate_2D_3,coordinate_2D_4);

        List list=new LinkedList();
        list.add(agent_1);
        list.add(agent_2);

        /*      = Expected values =     */

        Map<I_Location, Map<I_Location, Integer>> expected = new HashMap<>();
        Map<I_Location, Integer> insideMap = new HashMap<>();
        Map<I_Location, Integer> insideMap2 = new HashMap<>();
        insideMap.put(map.getMapLocation(new Coordinate_2D(1,3)),1);
        insideMap.put(map.getMapLocation(new Coordinate_2D(2,3)),2);
        insideMap.put(map.getMapLocation(new Coordinate_2D(1,2)),2);
        insideMap.put(map.getMapLocation(new Coordinate_2D(1,1)),3);
        insideMap.put(map.getMapLocation(new Coordinate_2D(1,0)),4);
        insideMap.put(map.getMapLocation(new Coordinate_2D(0,0)),5);
        insideMap.put(map.getMapLocation(new Coordinate_2D(2,0)),5);
        insideMap.put(map.getMapLocation(new Coordinate_2D(0,3)),0);

        insideMap2.put(map.getMapLocation(new Coordinate_2D(1,3)),1);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(0,3)),2);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(1,2)),2);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(1,1)),3);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(1,0)),4);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(0,0)),5);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(2,0)),5);
        insideMap2.put(map.getMapLocation(new Coordinate_2D(2,3)),0);

        expected.put(map.getMapLocation(agent_1.target), insideMap);
        expected.put(map.getMapLocation(agent_2.target), insideMap2);

        /*  = Test actual values =  */
        DistanceTableAStarHeuristic distanceTableAStarHeuristic = new DistanceTableAStarHeuristic(list, map);

        assertTrue(equalsAllAgentMap(expected, distanceTableAStarHeuristic.getDistanceDictionaries()));
    }

}