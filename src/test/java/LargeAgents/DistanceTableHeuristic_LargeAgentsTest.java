package LargeAgents;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import GraphMapPackage.GraphMapVertex;
import GraphMapPackage.MapFactory;
import LargeAgents_CBS.Instances.LargeAgent;
import LargeAgents_CBS.Instances.Maps.Coordinate_2D_LargeAgent;
import LargeAgents_CBS.Solvers.LowLevel.DistanceTableHeuristic_LargeAgents;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DistanceTableHeuristic_LargeAgentsTest {

    final Enum_MapCellType e = Enum_MapCellType.EMPTY;
    final Enum_MapCellType w = Enum_MapCellType.WALL;

    Enum_MapCellType[][] map_2D_H = {
            {e, e, e, e},
            {e, e, e, e},
            {e, w, e, e},
            {e, w, e, e},
    };

    I_Map map = MapFactory.newSimple4Connected2D_GraphMap_LargeAgents(map_2D_H);

    /*   = Equals Maps =    */
    private boolean equalsAllAgentMap(Map<Agent, Map<I_Coordinate, Integer>> expectedValues, Map<Agent, Map<I_Coordinate, Integer>> actualValues) {

        if (expectedValues.size() != actualValues.size()) {
            return false;
        }
        for (Map.Entry<Agent, Map<I_Coordinate, Integer>> agentMapEntry : expectedValues.entrySet()) {

            Agent agent = agentMapEntry.getKey();
            Map<I_Coordinate, Integer> expectedCellMap = expectedValues.get(agent);
            Map<I_Coordinate, Integer> actualCellMap = actualValues.get(agent);

            if (!this.equalsAllCellMap(expectedCellMap, actualCellMap)) {
                return false;
            }
        }
        return true;
    }

    private boolean equalsAllCellMap(Map<I_Coordinate, Integer> expectedCellMap, Map<I_Coordinate, Integer> actualCellMap) {
        if (expectedCellMap.size() != actualCellMap.size()) {
            return false;
        }
        for (Map.Entry<I_Coordinate, Integer> MapCellEntry : expectedCellMap.entrySet()) {

            I_Coordinate coordinate = MapCellEntry.getKey();
            int expectedDistance = expectedCellMap.get(coordinate);
            int actualDistance = actualCellMap.get(coordinate);

            if (expectedDistance != actualDistance) {
                return false;
            }
        }
        return true;
    }

    @Test
    public void test() {

        Coordinate_2D coordinate_2D_1 = new Coordinate_2D(0, 0);
        Coordinate_2D coordinate_2D_2 = new Coordinate_2D(0, 1);
        Coordinate_2D coordinate_2D_3 = new Coordinate_2D(1, 0);
        Coordinate_2D coordinate_2D_4 = new Coordinate_2D(1, 1);
        Coordinate_2D[][] source= new Coordinate_2D[2][2];
        source[0][0]=coordinate_2D_1;
        source[0][1]=coordinate_2D_2;
        source[1][0]=coordinate_2D_3;
        source[1][1]=coordinate_2D_4;
        Coordinate_2D_LargeAgent sourceLargeAgent=new Coordinate_2D_LargeAgent(source);

        Coordinate_2D coordinate_2D_5 = new Coordinate_2D(2, 2);
        Coordinate_2D coordinate_2D_6 = new Coordinate_2D(2, 3);
        Coordinate_2D coordinate_2D_7 = new Coordinate_2D(3, 2);
        Coordinate_2D coordinate_2D_8 = new Coordinate_2D(3, 3);
        Coordinate_2D[][] target= new Coordinate_2D[2][2];
        target[0][0]=coordinate_2D_5;
        target[0][1]=coordinate_2D_6;
        target[1][0]=coordinate_2D_7;
        target[1][1]=coordinate_2D_8;
        Coordinate_2D_LargeAgent targetLargeAgent=new Coordinate_2D_LargeAgent(target);

        // Agent 1:
        //            X0  X1  X2  X3
        //         Y0{S1, S2, EE, EE}
        //         Y1{S3, S4, WW, WW}
        //         Y2{EE, EE, G1, G2}
        //         Y3{EE, EE, G3, G4}
        LargeAgent agent_1 = new LargeAgent(1, sourceLargeAgent, targetLargeAgent);

        Coordinate_2D neighbor1_1 = new Coordinate_2D(0, 1);
        Coordinate_2D neighbor1_2 = new Coordinate_2D(0, 2);
        Coordinate_2D neighbor1_3 = new Coordinate_2D(1, 1);
        Coordinate_2D neighbor1_4 = new Coordinate_2D(1, 2);

        Coordinate_2D neighbor2_1 = new Coordinate_2D(0, 2);
        Coordinate_2D neighbor2_2 = new Coordinate_2D(0, 3);
        Coordinate_2D neighbor2_3 = new Coordinate_2D(1, 2);
        Coordinate_2D neighbor2_4 = new Coordinate_2D(1, 3);

        Coordinate_2D neighbor3_1 = new Coordinate_2D(1, 2);
        Coordinate_2D neighbor3_2 = new Coordinate_2D(1, 3);
        Coordinate_2D neighbor3_3 = new Coordinate_2D(2, 2);
        Coordinate_2D neighbor3_4 = new Coordinate_2D(2, 3);

        Coordinate_2D neighbor4_1 = new Coordinate_2D(2, 2);
        Coordinate_2D neighbor4_2 = new Coordinate_2D(2, 3);
        Coordinate_2D neighbor4_3 = new Coordinate_2D(3, 2);
        Coordinate_2D neighbor4_4 = new Coordinate_2D(3, 3);

        Coordinate_2D neighbor5_1 = new Coordinate_2D(0, 0);
        Coordinate_2D neighbor5_2 = new Coordinate_2D(0, 1);
        Coordinate_2D neighbor5_3 = new Coordinate_2D(1, 0);
        Coordinate_2D neighbor5_4 = new Coordinate_2D(1, 1);

        Coordinate_2D[][] neighbor1=new Coordinate_2D[2][2];
        neighbor1[0][0]= neighbor1_1;
        neighbor1[0][1]= neighbor1_2;
        neighbor1[1][0]= neighbor1_3;
        neighbor1[1][1]= neighbor1_4;

        Coordinate_2D[][] neighbor2=new Coordinate_2D[2][2];
        neighbor2[0][0]= neighbor2_1;
        neighbor2[0][1]= neighbor2_2;
        neighbor2[1][0]= neighbor2_3;
        neighbor2[1][1]= neighbor2_4;

        Coordinate_2D[][] neighbor3=new Coordinate_2D[2][2];
        neighbor3[0][0]= neighbor3_1;
        neighbor3[0][1]= neighbor3_2;
        neighbor3[1][0]= neighbor3_3;
        neighbor3[1][1]= neighbor3_4;

        Coordinate_2D[][] neighbor4=new Coordinate_2D[2][2];
        neighbor4[0][0]= neighbor4_1;
        neighbor4[0][1]= neighbor4_2;
        neighbor4[1][0]= neighbor4_3;
        neighbor4[1][1]= neighbor4_4;

        Coordinate_2D[][] neighbor5=new Coordinate_2D[2][2];
        neighbor5[0][0]= neighbor5_1;
        neighbor5[0][1]= neighbor5_2;
        neighbor5[1][0]= neighbor5_3;
        neighbor5[1][1]= neighbor5_4;

        HashMap<I_Coordinate, I_Location> hashMap = new HashMap<>();

        List list = new LinkedList();
        list.add(agent_1);

        /*      = Expected values =     */

        Map<Agent, Map<I_Coordinate, Integer>> expected = new HashMap<>();
        Map<I_Coordinate, Integer> insideMap = new HashMap<>();

        Coordinate_2D_LargeAgent neighbor1Large= new Coordinate_2D_LargeAgent(neighbor1);
        Coordinate_2D_LargeAgent neighbor2Large= new Coordinate_2D_LargeAgent(neighbor2);
        Coordinate_2D_LargeAgent neighbor3Large= new Coordinate_2D_LargeAgent(neighbor3);
        Coordinate_2D_LargeAgent neighbor4Large= new Coordinate_2D_LargeAgent(neighbor4);
        Coordinate_2D_LargeAgent neighbor5Large= new Coordinate_2D_LargeAgent(neighbor5);

        insideMap.put(neighbor1Large,3);
        insideMap.put(neighbor2Large,2);
        insideMap.put(neighbor3Large,1);
        insideMap.put(neighbor4Large,0);
        insideMap.put(neighbor5Large,4);

        expected.put(agent_1, insideMap);

        /*  = Test actual values =  */
        DistanceTableHeuristic_LargeAgents distanceTableAStarHeuristic = new DistanceTableHeuristic_LargeAgents(list, map);
        Map<Agent, Map<I_Location, Integer>> map=distanceTableAStarHeuristic.getDistanceDictionaries();

        Map<Agent,Map<I_Coordinate,Integer>> mapOfCoordinates=new HashMap<>();

        for (Map.Entry<Agent, Map<I_Location, Integer>>agentMapEntry : map.entrySet()) {
            HashMap<I_Coordinate,Integer> convertToCoordinate=new HashMap<>();
            convertToCoordinate.put(((GraphMapVertex)agentMapEntry.getValue()).getCoordinate(),(agentMapEntry.getValue().get(agentMapEntry)));
            mapOfCoordinates.put(agentMapEntry.getKey(),convertToCoordinate);
        }
        Assert.assertTrue(equalsAllAgentMap(expected, mapOfCoordinates));
    }
}