package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import org.junit.jupiter.api.Test;

import java.util.List;

import static BasicMAPF.TestConstants.Agents.agent12to33;
import static BasicMAPF.TestConstants.Agents.agent33to12;
import static BasicMAPF.TestConstants.Instances.instanceEmpty1;
import static BasicMAPF.TestConstants.Instances.instanceSmallMaze;
import static BasicMAPF.TestConstants.Maps.mapWithPocket;
import static org.junit.jupiter.api.Assertions.*;
import static org.apache.commons.math3.util.CombinatoricsUtils.factorial;

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
    void continueSearchingWithSkipsReusesWork() {
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

        expectedDepth += 3;

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

        expectedDepth += 2;

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
    void continueSearchingStandardFlowValidity() {
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33, agent33to12), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        MDD mdd1 = searcher1.continueSearching(3);

        // line
        assertEquals(3, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(1, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(1, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(1, mdd1.getLevel(3).size());

        for (int l = 1; l <= 3; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }

        // line + all waiting options
        mdd1 = searcher1.continueSearching(4);
        assertEquals(4, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(2, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(2, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(2, mdd1.getLevel(3).size());
        System.out.println(mdd1.getLevel(4));
        assertEquals(1, mdd1.getLevel(4).size());

        for (int l = 1; l <= 4; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }

        // line + all waiting options x2 and small detours
        mdd1 = searcher1.continueSearching(5);
        assertEquals(5, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(4, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(3, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(4, mdd1.getLevel(3).size());
        System.out.println(mdd1.getLevel(4));
        assertEquals(4, mdd1.getLevel(4).size());
        System.out.println(mdd1.getLevel(5));
        assertEquals(1, mdd1.getLevel(5).size());

        for (int l = 1; l <= 5; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }
    }

    @Test
    void continueSearchingWithSkipsValidity1() {
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33, agent33to12), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        MDD mdd1 = searcher1.continueSearching(3);

        // line
        assertEquals(3, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(1, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(1, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(1, mdd1.getLevel(3).size());

        for (int l = 1; l <= 3; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }

        // line + all waiting options x2 and small detours
        mdd1 = searcher1.continueSearching(5);
        assertEquals(5, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(4, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(3, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(4, mdd1.getLevel(3).size());
        System.out.println(mdd1.getLevel(4));
        assertEquals(4, mdd1.getLevel(4).size());
        System.out.println(mdd1.getLevel(5));
        assertEquals(1, mdd1.getLevel(5).size());

        for (int l = 1; l <= 5; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }
    }

    @Test
    void continueSearchingWithSkipsValidity2() {
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33, agent33to12), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        MDD mdd1;

        // line + all waiting options x2 and small detours
        mdd1 = searcher1.continueSearching(5);
        assertEquals(5, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(4, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(3, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(4, mdd1.getLevel(3).size());
        System.out.println(mdd1.getLevel(4));
        assertEquals(4, mdd1.getLevel(4).size());
        System.out.println(mdd1.getLevel(5));
        assertEquals(1, mdd1.getLevel(5).size());

        for (int l = 1; l <= 5; l++) {
            List<MDDNode> level = mdd1.getLevel(l);
            for (int i = 1; i < level.size(); i++) {
                MDDNode node = level.get(i);
                MDDNode prevNode = level.get(i - 1);
                assertTrue(node.compareTo(prevNode) > 0);
            }
        }
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