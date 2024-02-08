package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.junit.jupiter.api.Test;

import java.util.List;

import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Instances.instanceEmpty3;
import static org.junit.jupiter.api.Assertions.*;

class MDDManagerTest {

    @Test
    void standardFlow() {
        int depthDelta = 40;
        for (MAPF_Instance instance: List.of(instanceEmpty1, instanceCircle2, instanceCircle1, instanceEmpty2,
                instanceSmallMaze, instanceStartAdjacentGoAround, instanceEmptyHarder, instanceEmpty3)){
            I_Map map = instance.map;
            SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
            MDDManager mddManager = new MDDManager(new AStarFactory(), new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(),
                    60 * 1000L), heuristic);

            for (Agent agent: instance.agents){
                int minDepth = heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
                MDD mddWithGetMinMDDNoReuse = mddManager.getMinMDDNoReuse(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, null);
                assertNotNull(mddWithGetMinMDDNoReuse);
                assertEquals(minDepth, mddWithGetMinMDDNoReuse.getDepth());
                System.out.println(mddWithGetMinMDDNoReuse);

                for (int delta = 0; delta < depthDelta; delta++){
                    MDD mddWithGetMDD = mddManager.getMDD(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, minDepth + delta);
                    assertNotNull(mddWithGetMDD);
                    System.out.println(mddWithGetMDD);

                    MDD mddwithGetMDDNoReuse = mddManager.getMDDNoReuse(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, minDepth + delta);
                    assertNotNull(mddwithGetMDDNoReuse);
                    System.out.println(mddwithGetMDDNoReuse);
                }
            }
        }
    }

    @Test
    void skips() {
        int depthDelta = 200;
        for (MAPF_Instance instance: List.of(instanceEmpty1, instanceCircle2, instanceCircle1, instanceEmpty2,
                instanceSmallMaze, instanceStartAdjacentGoAround, instanceEmptyHarder, instanceEmpty3)){
            I_Map map = instance.map;
            SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
            MDDManager mddManager = new MDDManager(new AStarFactory(), new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(),
                    60 * 1000L), heuristic);

            for (Agent agent: instance.agents){
                int minDepth = heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
                MDD mddWithGetMinMDDNoReuse = mddManager.getMinMDDNoReuse(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, null);
                assertNotNull(mddWithGetMinMDDNoReuse);
                assertEquals(minDepth, mddWithGetMinMDDNoReuse.getDepth());
                System.out.println(mddWithGetMinMDDNoReuse);

                for (int delta = 0; delta < depthDelta; delta+=7){
                    MDD mddWithGetMDD = mddManager.getMDD(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, minDepth + delta);
                    assertNotNull(mddWithGetMDD);
                    System.out.println(mddWithGetMDD);

                    MDD mddwithGetMDDNoReuse = mddManager.getMDDNoReuse(map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, minDepth + delta);
                    assertNotNull(mddwithGetMDDNoReuse);
                    System.out.println(mddwithGetMDDNoReuse);
                }
            }
        }
    }
}