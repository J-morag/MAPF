package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.VertexConflict;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Coordiantes.*;
class MDDTest {
    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    // = levels = //

    @Test
    void getLevelCircleTest() {
        I_Map map = mapCircle;
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

        // line + all waiting options x2 + go around
        mdd1 = searcher1.continueSearching(5);
        assertEquals(5, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(3, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(4, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(4, mdd1.getLevel(3).size());
        System.out.println(mdd1.getLevel(4));
        assertEquals(3, mdd1.getLevel(4).size());
        System.out.println(mdd1.getLevel(5));
        assertEquals(1, mdd1.getLevel(5).size());
    }
    @Test
    void getLevelMapWithPocketTest1() {
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33, agent33to12), map);

        MDD mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic).continueSearching(3);

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
        mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic).continueSearching(4);
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
        mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic).continueSearching(5);
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
    void getLevelMapWithPocketTest2() {
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent33to12), map);

        MDD mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent33to12.source),
                map.getMapLocation(agent33to12.target), agent33to12, heuristic).continueSearching(3);

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
        mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent33to12.source),
                map.getMapLocation(agent33to12.target), agent33to12, heuristic).continueSearching(4);
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
        mdd1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent33to12.source),
                map.getMapLocation(agent33to12.target), agent33to12, heuristic).continueSearching(5);
        assertEquals(5, mdd1.getDepth());
        System.out.println(mdd1.getLevel(1));
        assertEquals(4, mdd1.getLevel(1).size());
        System.out.println(mdd1.getLevel(2));
        assertEquals(4, mdd1.getLevel(2).size());
        System.out.println(mdd1.getLevel(3));
        assertEquals(3, mdd1.getLevel(3).size());
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

    // = conflicts = //

    @Test
    void conflictsWithMDDAtDepthVertexConflictTest1() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent12to33;
        Agent agent2 = agent43to12;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(3);
        MDD mdd2 = searcher2.continueSearching(4);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(1, conflicts.size());
        assertEquals(new VertexConflict(agent1, agent2, 2, map.getMapLocation(coor32)), conflicts.get(0));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthVertexConflictTest2() {
        I_Map map = mapCircle;
        Agent agent1 = agent12to34;
        Agent agent2 = agent34to12;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(4);
        MDD mdd2 = searcher2.continueSearching(4);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(2, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 2, map.getMapLocation(coor32)),
                new VertexConflict(agent1, agent2, 2, map.getMapLocation(coor14))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthVertexConflictTest3() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to43;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(5);
        MDD mdd2 = searcher2.continueSearching(5);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(1, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 3, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthVertexConflictTest4() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to30;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(6);
        MDD mdd2 = searcher2.continueSearching(6);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
//        assertEquals(1, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 3, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
//        assertEquals(2, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 4, map.getMapLocation(coor32)),
                new VertexConflict(agent1, agent2, 4, map.getMapLocation(coor31))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
//        assertEquals(2, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor31)),
                new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor30))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 6, map.getMapLocation(coor30))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));
    }

    @Test
    void conflictsWithMDDAtDepthSwappingConflictTest1() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent12to33;
        Agent agent2 = agent33to12;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(3);
        MDD mdd2 = searcher2.continueSearching(3);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(1, conflicts.size());
        assertEquals(new SwappingConflict(agent1, agent2, 2, map.getMapLocation(coor32), map.getMapLocation(coor22)), conflicts.get(0));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthSwappingConflictTest2() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent12to35;
        Agent agent2 = agent35to12;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(5);
        MDD mdd2 = searcher2.continueSearching(5);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(2, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new SwappingConflict(agent1, agent2, 3, map.getMapLocation(coor15), map.getMapLocation(coor14)),
                new SwappingConflict(agent1, agent2, 3, map.getMapLocation(coor33), map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthVertexAndSwappingConflictsTest1() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to43;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(6);
        MDD mdd2 = searcher2.continueSearching(6);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
//        assertEquals(1, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 3, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
//        assertEquals(2, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 4, map.getMapLocation(coor32)),
                new SwappingConflict(agent1, agent2, 4, map.getMapLocation(coor33), map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthTargetConflictTest1() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent04to30;
        Agent agent2 = agent43to32;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(7);
        MDD mdd2 = searcher2.continueSearching(2);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts;
        Set<A_Conflict> expectedConflicts;
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 7, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthTargetConflictTest2() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent04to30;
        Agent agent2 = agent43to32;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(8);
        MDD mdd2 = searcher2.continueSearching(2);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts;
        Set<A_Conflict> expectedConflicts;
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 6, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 7, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 8, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthTargetConflictTest3() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent04to30;
        Agent agent2 = agent43to32;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(8);
        MDD mdd2 = searcher2.continueSearching(3);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts;
        Set<A_Conflict> expectedConflicts;
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
        assertEquals(0, conflicts.size());

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 6, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 7, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 8, false);
        assertEquals(0, conflicts.size());
    }

    @Test
    void conflictsWithMDDAtDepthTargetConflictTest4() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent04to30;
        Agent agent2 = agent43to32;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(8);
        MDD mdd2 = searcher2.continueSearching(5);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts;
        Set<A_Conflict> expectedConflicts;
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 4, map.getMapLocation(coor22))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 5, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, false);
//        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 6, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 7, false);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 8, false);
        assertEquals(0, conflicts.size());
    }



    @Test
    void conflictsWithMDDAtDepthStopAtOneTest() {
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to43;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent1.source),
                map.getMapLocation(agent1.target), agent1, heuristic);

        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent2.source),
                map.getMapLocation(agent2.target), agent2, heuristic);

        MDD mdd1 = searcher1.continueSearching(6);
        MDD mdd2 = searcher2.continueSearching(6);

        mdd1.getLevel(1);
        mdd2.getLevel(1);
        System.out.println(mdd1);
        System.out.println(mdd2);

        List<A_Conflict> conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 1, true);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 2, true);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 3, true);
        assertEquals(1, conflicts.size());
        Set<A_Conflict> expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 3, map.getMapLocation(coor32))));
        assertEquals(expectedConflicts, new HashSet<>(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 4, true);
        assertEquals(1, conflicts.size());
        expectedConflicts = new HashSet<>(List.of(new VertexConflict(agent1, agent2, 4, map.getMapLocation(coor32)),
                new SwappingConflict(agent1, agent2, 4, map.getMapLocation(coor33), map.getMapLocation(coor32))));
        assertTrue(expectedConflicts.containsAll(conflicts));

        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 5, true);
        assertEquals(0, conflicts.size());
        conflicts = mdd1.conflictsWithMDDAtDepth(mdd2, 6, true);
        assertEquals(0, conflicts.size());
    }

    // = constraints = //

    @Test
    void copyUnderConstraintsNoChangeWhenNoRelevanceTest(){
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to43;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent1, 3, map.getMapLocation(coor40)));
        constraintSet.add(new Constraint(agent2, 1, map.getMapLocation(coor12)));

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent1.source), map.getMapLocation(agent1.target), agent1, heuristic);
        MDD mdd1 = searcher1.continueSearching(6);
        MDD mdd1Copy = new MDD(mdd1, constraintSet);
        System.out.println("mdd1 = " + mdd1);
        System.out.println("mdd1Copy = " + mdd1Copy);
        assertTrue(mdd1.levelsEquals(mdd1Copy)); // doesn't check MDD edges


        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent2.source), map.getMapLocation(agent2.target), agent2, heuristic);
        MDD mdd2 = searcher2.continueSearching(6);
        MDD mdd2Copy = new MDD(mdd2, constraintSet);
        System.out.println("mdd2 = " + mdd2);
        System.out.println("mdd2Copy = " + mdd2Copy);
        assertTrue(mdd2.levelsEquals(mdd2Copy)); // doesn't check MDD edges
    }

    @Test
    void copyUnderConstraintsTest(){
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent12to33, 1, map.getMapLocation(coor22)));

        MDD mdd1 = searcher1.continueSearching(3);
        MDD mdd1Constrained = new MDD(mdd1, constraintSet);
        assertEquals(-1, mdd1Constrained.getDepth());

        // just the option of waiting and then moving
        mdd1 = searcher1.continueSearching(4);
        mdd1Constrained = new MDD(mdd1, constraintSet);
        assertEquals(4, mdd1Constrained.getDepth());
        System.out.println(mdd1Constrained.getLevel(0));
        assertEquals(1, mdd1Constrained.getLevel(0).size());
        System.out.println(mdd1Constrained.getLevel(1));
        assertEquals(1, mdd1Constrained.getLevel(1).size());
        System.out.println(mdd1Constrained.getLevel(2));
        assertEquals(1, mdd1Constrained.getLevel(2).size());
        System.out.println(mdd1Constrained.getLevel(3));
        assertEquals(1, mdd1Constrained.getLevel(3).size());
        System.out.println(mdd1Constrained.getLevel(4));
        assertEquals(1, mdd1Constrained.getLevel(4).size());

        // the option of waiting and then moving + waiting options later + small detours
        mdd1 = searcher1.continueSearching(5);
        mdd1Constrained = new MDD(mdd1, constraintSet);
        assertEquals(5, mdd1Constrained.getDepth());
        System.out.println(mdd1Constrained.getLevel(0));
        assertEquals(1, mdd1Constrained.getLevel(0).size());
        System.out.println(mdd1Constrained.getLevel(1));
        assertEquals(3, mdd1Constrained.getLevel(1).size());
        System.out.println(mdd1Constrained.getLevel(2));
        assertEquals(2, mdd1Constrained.getLevel(2).size());
        System.out.println(mdd1Constrained.getLevel(3));
        assertEquals(2, mdd1Constrained.getLevel(3).size());
        System.out.println(mdd1Constrained.getLevel(4));
        assertEquals(2, mdd1Constrained.getLevel(4).size());
        System.out.println(mdd1Constrained.getLevel(5));
        assertEquals(1, mdd1Constrained.getLevel(5).size());
    }

    @Test
    void copyUnderConstraintsAfterGoalTest(){
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.add(new Constraint(agent12to33, 9, map.getMapLocation(coor22)));

        MDD mdd1 = searcher1.continueSearching(3);
        MDD mdd1Constrained = new MDD(mdd1, constraintSet);
        assertEquals(3, mdd1Constrained.getDepth());

        constraintSet.add(new Constraint(agent12to33, 9, map.getMapLocation(coor33)));
        mdd1Constrained = new MDD(mdd1, constraintSet);
        assertEquals(-1, mdd1Constrained.getDepth());
    }

    @Test
    void copyUnderPositiveConstraintNoChangeWhenNoRelevanceThrowsExceptionTest(){
        I_Map map = mapWithPocket;
        Agent agent1 = agent02to43;
        Agent agent2 = agent35to30;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent1, agent2), map);
        Constraint positiveConstraint = new Constraint(agent1, 3, map.getMapLocation(coor40));

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent1.source), map.getMapLocation(agent1.target), agent1, heuristic);
        MDD mdd1 = searcher1.continueSearching(6);
        assertThrows(IllegalArgumentException.class, () -> mdd1.shallowCopyWithConstraint(positiveConstraint, true));

        Constraint positiveConstraint2 = new Constraint(agent2, 1, map.getMapLocation(coor12));
        A_MDDSearcher searcher2 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L),
                map.getMapLocation(agent2.source), map.getMapLocation(agent2.target), agent2, heuristic);
        MDD mdd2 = searcher2.continueSearching(6);
        assertThrows(IllegalArgumentException.class, () -> mdd2.shallowCopyWithConstraint(positiveConstraint2, true));
    }

    @Test
    void copyUnderPositiveConstraintTest(){
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        Constraint positiveConstraint = new Constraint(agent12to33, 1, map.getMapLocation(coor22));

        MDD mdd1 = searcher1.continueSearching(3);
        System.out.println(mdd1);
        MDD mdd1Constrained = mdd1.shallowCopyWithConstraint(positiveConstraint, true);
        System.out.println(mdd1Constrained);
        assertTrue(mdd1.levelsEquals(mdd1Constrained)); // doesn't check MDD edges

        // without the option of waiting at the first vertex
        MDD mdd2 = searcher1.continueSearching(4);
        System.out.println(mdd2);
        MDD mdd2Constrained = mdd2.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(4, mdd2Constrained.getDepth());
        System.out.println(mdd2Constrained.getLevel(0));
        assertEquals(1, mdd2Constrained.getLevel(0).size());
        System.out.println(mdd2Constrained.getLevel(1));
        assertEquals(1, mdd2Constrained.getLevel(1).size());
        System.out.println(mdd2Constrained.getLevel(2));
        assertEquals(2, mdd2Constrained.getLevel(2).size());
        System.out.println(mdd2Constrained.getLevel(3));
        assertEquals(2, mdd2Constrained.getLevel(3).size());
        System.out.println(mdd2Constrained.getLevel(4));
        assertEquals(1, mdd2Constrained.getLevel(4).size());

        MDD mdd3 = searcher1.continueSearching(5);
        System.out.println(mdd3);
        MDD mdd3Constrained = mdd3.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(5, mdd3Constrained.getDepth());
        System.out.println(mdd3Constrained.getLevel(0));
        assertEquals(1, mdd3Constrained.getLevel(0).size());
        System.out.println(mdd3Constrained.getLevel(1));
        assertEquals(1, mdd3Constrained.getLevel(1).size());
        System.out.println(mdd3Constrained.getLevel(2));
        assertEquals(3, mdd3Constrained.getLevel(2).size());
        System.out.println(mdd3Constrained.getLevel(3));
        assertEquals(4, mdd3Constrained.getLevel(3).size());
        System.out.println(mdd3Constrained.getLevel(4));
        assertEquals(4, mdd3Constrained.getLevel(4).size());
        System.out.println(mdd3Constrained.getLevel(5));
        assertEquals(1, mdd3Constrained.getLevel(5).size());
    }

    @Test
    void copyUnderPositiveConstraintTest2(){
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        Constraint positiveConstraint = new Constraint(agent12to33, 2, map.getMapLocation(coor32));

        MDD mdd1 = searcher1.continueSearching(3);
        System.out.println(mdd1);
        MDD mdd1Constrained = mdd1.shallowCopyWithConstraint(positiveConstraint, true);
        System.out.println(mdd1Constrained);
        assertTrue(mdd1.levelsEquals(mdd1Constrained)); // doesn't check MDD edges

        // without the option of waiting at the first vertex
        MDD mdd2 = searcher1.continueSearching(4);
        System.out.println(mdd2);
        MDD mdd2Constrained = mdd2.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(4, mdd2Constrained.getDepth());
        System.out.println(mdd2Constrained.getLevel(0));
        assertEquals(1, mdd2Constrained.getLevel(0).size());
        System.out.println(mdd2Constrained.getLevel(1));
        assertEquals(1, mdd2Constrained.getLevel(1).size());
        System.out.println(mdd2Constrained.getLevel(2));
        assertEquals(1, mdd2Constrained.getLevel(2).size());
        System.out.println(mdd2Constrained.getLevel(3));
        assertEquals(2, mdd2Constrained.getLevel(3).size());
        System.out.println(mdd2Constrained.getLevel(4));
        assertEquals(1, mdd2Constrained.getLevel(4).size());

        MDD mdd3 = searcher1.continueSearching(5);
        System.out.println(mdd3);
        MDD mdd3Constrained = mdd3.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(5, mdd3Constrained.getDepth());
        System.out.println(mdd3Constrained.getLevel(0));
        assertEquals(1, mdd3Constrained.getLevel(0).size());
        System.out.println(mdd3Constrained.getLevel(1));
        assertEquals(1, mdd3Constrained.getLevel(1).size());
        System.out.println(mdd3Constrained.getLevel(2));
        assertEquals(1, mdd3Constrained.getLevel(2).size());
        System.out.println(mdd3Constrained.getLevel(3));
        assertEquals(4, mdd3Constrained.getLevel(3).size());
        System.out.println(mdd3Constrained.getLevel(4));
        assertEquals(4, mdd3Constrained.getLevel(4).size());
        System.out.println(mdd3Constrained.getLevel(5));
        assertEquals(1, mdd3Constrained.getLevel(5).size());
    }

    @Test
    void copyUnderPositiveConstraintTest3(){
        I_Map map = mapWithPocket;
        SingleAgentGAndH heuristic = new DistanceTableSingleAgentHeuristic(List.of(agent12to33), map);

        A_MDDSearcher searcher1 = new AStarMDDBuilder(new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), 1000L), map.getMapLocation(agent12to33.source),
                map.getMapLocation(agent12to33.target), agent12to33, heuristic);
        Constraint positiveConstraint = new Constraint(agent12to33, 3, map.getMapLocation(coor33));

        MDD mdd1 = searcher1.continueSearching(3);
        System.out.println(mdd1);
        MDD mdd1Constrained = mdd1.shallowCopyWithConstraint(positiveConstraint, true);
        System.out.println(mdd1Constrained);
        assertTrue(mdd1.levelsEquals(mdd1Constrained)); // doesn't check MDD edges

        // without the option of waiting at the first vertex
        MDD mdd2 = searcher1.continueSearching(4);
        System.out.println(mdd2);
        MDD mdd2Constrained = mdd2.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(4, mdd2Constrained.getDepth());
        System.out.println(mdd2Constrained.getLevel(0));
        assertEquals(1, mdd2Constrained.getLevel(0).size());
        System.out.println(mdd2Constrained.getLevel(1));
        assertEquals(1, mdd2Constrained.getLevel(1).size());
        System.out.println(mdd2Constrained.getLevel(2));
        assertEquals(1, mdd2Constrained.getLevel(2).size());
        System.out.println(mdd2Constrained.getLevel(3));
        assertEquals(1, mdd2Constrained.getLevel(3).size());
        System.out.println(mdd2Constrained.getLevel(4));
        assertEquals(1, mdd2Constrained.getLevel(4).size());

        MDD mdd3 = searcher1.continueSearching(5);
        System.out.println(mdd3);
        MDD mdd3Constrained = mdd3.shallowCopyWithConstraint(positiveConstraint, true);
        assertEquals(5, mdd3Constrained.getDepth());
        System.out.println(mdd3Constrained.getLevel(0));
        assertEquals(1, mdd3Constrained.getLevel(0).size());
        System.out.println(mdd3Constrained.getLevel(1));
        assertEquals(1, mdd3Constrained.getLevel(1).size());
        System.out.println(mdd3Constrained.getLevel(2));
        assertEquals(1, mdd3Constrained.getLevel(2).size());
        System.out.println(mdd3Constrained.getLevel(3));
        assertEquals(1, mdd3Constrained.getLevel(3).size());
        System.out.println(mdd3Constrained.getLevel(4));
        assertEquals(4, mdd3Constrained.getLevel(4).size());
        System.out.println(mdd3Constrained.getLevel(5));
        assertEquals(1, mdd3Constrained.getLevel(5).size());
    }

}