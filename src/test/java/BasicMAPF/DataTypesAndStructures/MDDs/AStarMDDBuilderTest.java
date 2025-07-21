package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.GoalConstraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordinates.*;
import static BasicMAPF.TestConstants.Instances.*;
import static BasicMAPF.TestConstants.Maps.mapEmpty;
import static BasicMAPF.TestConstants.Maps.mapWithPocket;
import static BasicMAPF.TestUtils.addRandomConstraints;
import static org.junit.jupiter.api.Assertions.*;

class AStarMDDBuilderTest {
    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

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
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        MDD mdd1 = searcher1.continueSearching(3);

        // line
        assertEquals(3, mdd1.getDepth());
        System.out.println(mdd1.getLevel(0));
        assertEquals(1, mdd1.getLevel(0).size());
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
        System.out.println(mdd1.getLevel(0));
        assertEquals(1, mdd1.getLevel(0).size());
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
        System.out.println(mdd1.getLevel(0));
        assertEquals(1, mdd1.getLevel(0).size());
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
    void standardFlowMore() {
        int depthDelta = 40;
        for (MAPF_Instance instance: List.of(instanceEmpty1, instanceCircle2, instanceCircle1, instanceEmpty2,
                instanceSmallMaze, instanceStartAdjacentGoAround, instanceEmptyHarder)){
            I_Map map = instance.map;
            SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);

            for (Agent agent: instance.agents){
                A_MDDSearcher searcher = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 60 * 1000L),
                        map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);

                MDD mddWithSearchToFirstSolution = searcher.searchToFirstSolution(null);
                assertNotNull(mddWithSearchToFirstSolution);
                System.out.println(mddWithSearchToFirstSolution);

                int minDepth = heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
                assertEquals(minDepth, mddWithSearchToFirstSolution.getDepth());

                for (int delta = 0; delta < depthDelta; delta++){
                    System.out.println("minDepth + delta: " + (minDepth + delta));
                    MDD mddWithContinueSearching = searcher.continueSearching(minDepth + delta);
                    assertNotNull(mddWithContinueSearching);
                    System.out.println(mddWithContinueSearching);
                }
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
    void skipsMore() {
        int depthDelta = 200;
        for (MAPF_Instance instance: List.of(instanceEmpty1, instanceCircle2, instanceCircle1, instanceEmpty2,
                instanceSmallMaze, instanceStartAdjacentGoAround, instanceEmptyHarder)){
            System.out.println("instance: " + instance.extendedName);
            I_Map map = instance.map;
            SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(instance.agents, map);

            for (Agent agent: instance.agents){
                A_MDDSearcher searcher = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 60 * 1000L),
                        map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);

                MDD mddWithSearchToFirstSolution = searcher.searchToFirstSolution(null);
                assertNotNull(mddWithSearchToFirstSolution);
                System.out.println(mddWithSearchToFirstSolution);

                int minDepth = heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
                assertEquals(minDepth, mddWithSearchToFirstSolution.getDepth());

                for (int delta = 0; delta < depthDelta; delta+=7){
                    System.out.println("minDepth + delta: " + (minDepth + delta));
                    MDD mddWithContinueSearching = searcher.continueSearching(minDepth + delta);
                    assertNotNull(mddWithContinueSearching);
                    System.out.println(mddWithContinueSearching);
                }
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

        mdd = builder.searchToFirstSolution(null);
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

        mdd = builder.searchToFirstSolution(null);
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

        mdd = builder.searchToFirstSolution(null);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints1() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth()); // should be same depth still
    }

    @Test
    void searchToFirstSolutionWithConstraints2() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor13)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth + 1, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints3() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth + 1, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints4() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new GoalConstraint(agent, 1, map.getMapLocation(coor22), new Agent(1000, coor34, coor34)));
//        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints5() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new GoalConstraint(agent, 1, map.getMapLocation(coor22), new Agent(1000, coor34, coor34)));
        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth + 1, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints6() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
//        constraints.add(new GoalConstraint(agent, 1, map.getMapLocation(coor22)));
        constraints.add(new GoalConstraint(agent, 1, map.getMapLocation(coor23), new Agent(1000, coor34, coor34)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraints7() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new GoalConstraint(agent, 0, map.getMapLocation(coor22), new Agent(1000, coor34, coor34)));
        constraints.add(new GoalConstraint(agent, 0, map.getMapLocation(coor23), new Agent(1000, coor34, coor34)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth + 2, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraintOnGoal() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 3, map.getMapLocation(coor33)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(minDepth + 1, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithConstraintOnGoalLater() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(6, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraintsOnGoalEarlierAndLater() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();
        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 7, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(11, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints1() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));

        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 11, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 11, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 11, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 11, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(11, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints2() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));

        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints3() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

//        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
//        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
//
//        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor33)));
//        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));
//
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(10));
        assertEquals(7, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(4, mdd.getLevel(11).size());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints4() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

//        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));
//        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
//
//        constraints.add(new Constraint(agent, 2, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));
//
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(5));
        assertEquals(31, mdd.getLevel(5).size());
        System.out.println(mdd.getLevel(10));
        assertEquals(7, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(4, mdd.getLevel(11).size());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints5() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor54)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor45)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor43)));

        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(5));
        assertEquals(31, mdd.getLevel(5).size());
        System.out.println(mdd.getLevel(9));
        assertEquals(17, mdd.getLevel(9).size());
        System.out.println(mdd.getLevel(10));
        assertEquals(7, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(4, mdd.getLevel(11).size());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints6() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor44)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor54)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor45)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor43)));

        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(5));
        assertEquals(31, mdd.getLevel(5).size());
        System.out.println(mdd.getLevel(9));
        assertEquals(16, mdd.getLevel(9).size());
        System.out.println(mdd.getLevel(10));
        assertEquals(6, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(4, mdd.getLevel(11).size());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints7() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new GoalConstraint(agent, 6, map.getMapLocation(coor35), new Agent(1000, coor34, coor34)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor44)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor54)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor45)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor43)));

        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(5));
        assertEquals(31, mdd.getLevel(5).size());
        System.out.println(mdd.getLevel(6));
        assertEquals(34, mdd.getLevel(6).size());
        System.out.println(mdd.getLevel(9));
        // can't do (2,5) anymore. I guess because Manhattan((2,5), (3,3)) = 3
        assertEquals(15 - 1, mdd.getLevel(9).size());
        System.out.println(mdd.getLevel(10));
        assertEquals(5, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(3, mdd.getLevel(11).size());
    }

    @Test
    void searchToFirstSolutionWithMultipleConstraints8() {
        I_Map map = mapEmpty;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(Collections.singletonList(agent12to33), map);
        Agent agent = agent12to33;
        int minDepth = (int)heuristic.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));

        AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent.source), map.getMapLocation(agent.target), agent, heuristic);
        MDD mdd;
        ConstraintSet constraints = new ConstraintSet();

        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor11)));
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor12)));
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor13)));
        constraints.add(new Constraint(agent, 1, map.getMapLocation(coor22)));

        constraints.add(new GoalConstraint(agent, 6, map.getMapLocation(coor35), new Agent(1000, coor34, coor34)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor24)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor44)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor54)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor45)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 9, map.getMapLocation(coor43)));

        constraints.add(new Constraint(agent, 5, map.getMapLocation(coor33)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor33)));

        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor23)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor34)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor32)));
        constraints.add(new Constraint(agent, 10, map.getMapLocation(coor43)));

        mdd = builder.searchToFirstSolution(constraints);
        System.out.println("mdd: " + mdd);
        System.out.println("mdd.getDepth(): " + mdd.getDepth());
        System.out.println("minDepth: " + minDepth);
        assertEquals(12, mdd.getDepth());

        System.out.println(mdd.getLevel(1));
        assertEquals(1, mdd.getLevel(1).size());
        System.out.println(mdd.getLevel(2));
        assertEquals(4, mdd.getLevel(2).size());
        // restricts the intermediate levels but it's hard to compute manually
        assertEquals(14, mdd.getLevel(9).size());
        System.out.println(mdd.getLevel(10));
        assertEquals(5, mdd.getLevel(10).size());
        System.out.println(mdd.getLevel(11));
        assertEquals(3, mdd.getLevel(11).size());
    }

    @Test
    void largeNumberOfConstraintsWithInfiniteConstraints(){
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        MAPF_Instance baseInstance = instanceEmpty1;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(baseInstance.agents, baseInstance.map);

        int seeds = 20;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i <= 5; i++) {
                    for (int j = 0; j <= 5; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                ConstraintSet constraints = new ConstraintSet();
                for (int i = 0; i < 5; i++){
                    I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation, new Agent(1000, coor43,  coor34)); // arbitrary agent not in instance
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, 10);
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).setAStarGAndH(heuristic).createRP();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                if (sippSolution != null){
                    int sippPlanLength = sippSolution.getPlanFor(agent).size();

                    // get MDD for the depth of the solution that SIPP found

                    AStarMDDBuilder builder = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 60 * 1000L),
                            testInstance.map.getMapLocation(agent.source), testInstance.map.getMapLocation(agent.target), agent, heuristic);

                    MDD mddWithSearchToFirstSolution = builder.searchToFirstSolution(constraints);
                    assertNotNull(mddWithSearchToFirstSolution);
                    assertEquals(sippPlanLength, mddWithSearchToFirstSolution.getDepth());
                }
            }
        }
    }

    // todo add test for source == target
}