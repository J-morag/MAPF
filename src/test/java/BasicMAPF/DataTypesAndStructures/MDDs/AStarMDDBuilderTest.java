package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.junit.jupiter.api.Test;

import static BasicMAPF.TestConstants.Instances.instanceEmpty1;
import static BasicMAPF.TestConstants.Instances.instanceSmallMaze;
import static org.junit.jupiter.api.Assertions.*;

class AStarMDDBuilderTest {

    @Test
    void continueSearchingStandardFlow() {
        MAPF_Instance instance = instanceEmpty1;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(0);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;

        int expectedDepth = minDepth;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth++;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth++;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth++;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
    }

    @Test
    void continueSearchingWithSkips() {
        MAPF_Instance instance = instanceEmpty1;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(0);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;

        int expectedDepth = minDepth + 3;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth += 2;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth += 1;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());

        expectedDepth += 4;
        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
    }

    @Test
    void continueSearchingStandardFlowReusesWork() {
        MAPF_Instance instance = instanceEmpty1;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(0);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        AStarMDDBuilder scratchBuilder;
        MDD scratchmdd;
        int expectedDepth;
        int expandedNodesContinued;
        int expandedNodesScratch;
        int cumSumExpandedNodesContinued = 0;

        expectedDepth = minDepth;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertEquals(expandedNodesScratch, expandedNodesContinued);

        expectedDepth++;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertTrue(expandedNodesScratch > expandedNodesContinued);

        expectedDepth++;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertTrue(expandedNodesScratch > expandedNodesContinued);

        expectedDepth++;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertTrue(expandedNodesScratch > expandedNodesContinued);

        expectedDepth++;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertTrue(expandedNodesScratch > expandedNodesContinued);

        expectedDepth++;

        mdd = builder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, mdd.getDepth());
        expandedNodesContinued = builder.getExpandedNodesNum() - cumSumExpandedNodesContinued;
        cumSumExpandedNodesContinued += expandedNodesContinued;
        System.out.println("expandedNodesContinued: " + expandedNodesContinued);
        // search from scratch
        scratchBuilder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        scratchmdd = scratchBuilder.continueSearching(expectedDepth);
        assertEquals(expectedDepth, scratchmdd.getDepth());
        expandedNodesScratch = scratchBuilder.getExpandedNodesNum();
        System.out.println("expandedNodesScratch: " + expandedNodesScratch);
        assertTrue(expandedNodesScratch > expandedNodesContinued);
    }

    @Test
    void searchToFirstSolution1() {
        MAPF_Instance instance = instanceEmpty1;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(0);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;

        mdd = builder.searchToFirstSolution();
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }

    @Test
    void searchToFirstSolution2() {
        MAPF_Instance instance = instanceEmpty1;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(2);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;

        mdd = builder.searchToFirstSolution();
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }

    @Test
    void searchToFirstSolution3() {
        MAPF_Instance instance = instanceSmallMaze;
        I_Map map = instance.map;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);
        Agent agent = instance.agents.get(0);
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;

        mdd = builder.searchToFirstSolution();
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }
}