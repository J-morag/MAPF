package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import Environment.IO_Package.IO_Manager;
import org.junit.jupiter.api.Test;

import java.util.List;

import static BasicMAPF.TestConstants.Coordinates.*;
import static BasicMAPF.TestConstants.Coordinates.coor04;
import static BasicMAPF.TestConstants.Instances.instanceEmpty1;
import static org.junit.jupiter.api.Assertions.*;

class RemovableConflictAvoidanceTableWithContestedGoalsTest {
    // todo decide about the correct way to count conflicts in edge cases

    @Test
    void numConflictsWith1AgentInTable() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        Agent queryingAgent = new Agent(2, coor05, coor15);

        int conflictsOfTime1EdgeConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)), false);
        assertEquals(1, conflictsOfTime1EdgeConflict);
        int conflictsOfTime1VertexConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsOfTime1VertexConflict);
        int conflictsOfMovingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsOfMovingAgentWithStayingAgent);

        int conflictsOfStayingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)), false);
        // both edge and vertex... though I'm not sure if this actually should be counted as 1 or 2.
        assertEquals(2, conflictsOfStayingAgentWithStayingAgent);

        int conflictsOfMovingAgentWithMovingAgentSameDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)), false);
        assertEquals(1, conflictsOfMovingAgentWithMovingAgentSameDirection);
        int conflictsOfMovingAgentWithMovingAgentOppositeDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsOfMovingAgentWithMovingAgentOppositeDirection);
        int conflictsOfLastMoveWithStayingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), true);
        assertEquals(1, conflictsOfLastMoveWithStayingAgentSameTime);
        int conflictsOfLastMoveWithAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04)), true);
        assertEquals(2, conflictsOfLastMoveWithAgentDifferentTime); // 2 conflicts during AgentToAvoid's plan

        int conflictsOfEarlyLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 5, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(2, conflictsOfEarlyLastMoveWithAgentToAvoidLastMove);

        int conflictsOfLateLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        assertEquals(1, conflictsOfLateLastMoveWithAgentToAvoidLastMove);

        int conflictsOfSimultaneousLastMoves = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(2, conflictsOfSimultaneousLastMoves);

        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(1, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime);
        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(1, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime);
    }

    @Test
    void testNumConflictsWith2AgentsInTable() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid1 = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid1 = new SingleAgentPlan(agentToAvoid1);
        planToAvoid1.addMoves(List.of(
                new Move(agentToAvoid1, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid1, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid1, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid1);
        Agent agentToAvoid2 = new Agent(2, coor05, coor15);
        SingleAgentPlan planToAvoid2 = new SingleAgentPlan(agentToAvoid2);
        planToAvoid2.addMoves(List.of(
                new Move(agentToAvoid2, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid2, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid2, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid2);
        Agent queryingAgent = new Agent(3, coor05, coor15);

        int conflictsOfTime1EdgeConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)), false);
        assertEquals(2, conflictsOfTime1EdgeConflict);
        int conflictsOfTime1VertexConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(2, conflictsOfTime1VertexConflict);
        int conflictsOfMovingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(2, conflictsOfMovingAgentWithStayingAgent);

        int conflictsOfStayingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)), false);
        // both edge and vertex... though I'm not sure if this actually should be counted as 1 or 2.
        assertEquals(4, conflictsOfStayingAgentWithStayingAgent);

        int conflictsOfMovingAgentWithMovingAgentSameDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)), false);
        assertEquals(2, conflictsOfMovingAgentWithMovingAgentSameDirection);
        int conflictsOfMovingAgentWithMovingAgentOppositeDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(2, conflictsOfMovingAgentWithMovingAgentOppositeDirection);
        int conflictsOfLastMoveWithStayingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), true);
        assertEquals(2, conflictsOfLastMoveWithStayingAgentSameTime);
        int conflictsOfLastMoveWithAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04)), true);
        assertEquals(4, conflictsOfLastMoveWithAgentDifferentTime); // 2 conflicts during AgentToAvoid's plan

        int conflictsOfEarlyLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 5, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(4, conflictsOfEarlyLastMoveWithAgentToAvoidLastMove);

        int conflictsOfLateLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        assertEquals(2, conflictsOfLateLastMoveWithAgentToAvoidLastMove);

        int conflictsOfSimultaneousLastMoves = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(4, conflictsOfSimultaneousLastMoves);

        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(2, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime);
        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(2, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime);
    }

    @Test
    void testNumConflictsWith3AgentsInTable() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid1 = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid1 = new SingleAgentPlan(agentToAvoid1);
        planToAvoid1.addMoves(List.of(
                new Move(agentToAvoid1, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid1, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid1, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid1, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid1);
        Agent agentToAvoid2 = new Agent(2, coor05, coor15);
        SingleAgentPlan planToAvoid2 = new SingleAgentPlan(agentToAvoid2);
        planToAvoid2.addMoves(List.of(
                new Move(agentToAvoid2, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid2, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid2, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid2, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid2);
        Agent agentToAvoid3 = new Agent(3, coor05, coor15);
        SingleAgentPlan planToAvoid3 = new SingleAgentPlan(agentToAvoid3);
        planToAvoid3.addMoves(List.of(
                new Move(agentToAvoid3, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 6, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid3, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid3, 8, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor04)),
                new Move(agentToAvoid3, 9, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor03))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid3);
        Agent queryingAgent = new Agent(4, coor05, coor15);

        int conflictsOfTime1EdgeConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)), false);
        assertEquals(3, conflictsOfTime1EdgeConflict);
        int conflictsOfTime1VertexConflict = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(3, conflictsOfTime1VertexConflict);
        int conflictsOfMovingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(3, conflictsOfMovingAgentWithStayingAgent);

        int conflictsOfStayingAgentWithStayingAgent = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)), false);
        // both edge and vertex... though I'm not sure if this actually should be counted as 1 or 2.
        assertEquals(6, conflictsOfStayingAgentWithStayingAgent);

        int conflictsOfMovingAgentWithMovingAgentSameDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)), false);
        assertEquals(3, conflictsOfMovingAgentWithMovingAgentSameDirection);
        int conflictsOfMovingAgentWithMovingAgentOppositeDirection = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(3, conflictsOfMovingAgentWithMovingAgentOppositeDirection);
        int conflictsOfLastMoveWithStayingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), true);
        assertEquals(3, conflictsOfLastMoveWithStayingAgentSameTime);
        int conflictsOfLastMoveWithAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 6, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor04)), true);
        assertEquals(6, conflictsOfLastMoveWithAgentDifferentTime); // 2 conflicts during AgentToAvoid's plan

        int conflictsOfEarlyLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 5, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(6, conflictsOfEarlyLastMoveWithAgentToAvoidLastMove);

        int conflictsOfLateLastMoveWithAgentToAvoidLastMove = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        assertEquals(3, conflictsOfLateLastMoveWithAgentToAvoidLastMove);

        int conflictsOfSimultaneousLastMoves = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor03)), true);
        // one for the AgentToAvoid entering the target location, and another for the infinite conflict they share afterwards... not sure if this is the right way to count it. should it be 1 or 2 or inf?
        assertEquals(6, conflictsOfSimultaneousLastMoves);

        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 9, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(3, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentSameTime);
        int conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 11, instanceEmpty1.map.getMapLocation(coor03), instanceEmpty1.map.getMapLocation(coor03)), false);
        assertEquals(3, conflictsOfLastMoveOfAgentToAvoidWithStayingMoveOfQueryingAgentDifferentTime);
    }

    @Test
    void addPlanThrowsExceptionIfAgentAlreadyHasPlan() {
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agent = new Agent(1, coor15, coor05);
        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMoves(List.of(
                new Move(agent, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(plan);
        assertThrows(IllegalStateException.class, () -> conflictAvoidanceTable.addPlan(plan));
    }

    @Test
    void removePlanRemovesAgentFromCoveredAgents() {
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agent = new Agent(1, coor15, coor05);
        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMoves(List.of(
                new Move(agent, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(plan);
        conflictAvoidanceTable.removePlan(plan);
        assertFalse(conflictAvoidanceTable.coveredAgents.contains(agent));
    }

    @Test
    void replacePlanReplacesExistingPlan() {
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agent = new Agent(1, coor15, coor05);
        SingleAgentPlan oldPlan = new SingleAgentPlan(agent);
        oldPlan.addMoves(List.of(
                new Move(agent, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        SingleAgentPlan newPlan = new SingleAgentPlan(agent);
        newPlan.addMoves(List.of(
                new Move(agent, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor04))
        ));
        conflictAvoidanceTable.addPlan(oldPlan);
        conflictAvoidanceTable.replacePlan(oldPlan, newPlan);
        // check that now there is no conflict with a move that conflicts with the old plan and not the new one
        Agent queryingAgent = new Agent(2, coor05, coor15);
        int conflicts = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor15)), false);
        assertEquals(0, conflicts);
    }

    @Test
    void numConflictsReturnsZeroIfNoConflicts() {
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agent = new Agent(1, coor15, coor05);
        SingleAgentPlan plan = new SingleAgentPlan(agent);
        plan.addMoves(List.of(
                new Move(agent, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(plan);
        Agent queryingAgent = new Agent(2, coor05, coor04);
        int conflicts = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor04)), false);
        assertEquals(0, conflicts);
    }


    @Test
    void testFirstConflictTime_IdentifiesSwappingConflict_BetweenTwoPlans() {
        String path = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, "Instances", "MovingAI"});
        // use map empty-48-48, instance empty-48-48-even-5, agents 1 and 7
        InstanceManager instanceManager = new InstanceManager(path, new InstanceBuilder_MovingAI(), new InstanceProperties(null, -1d, new int[]{10}));
        MAPF_Instance instance;
        boolean foundInstance = false;
        while ((instance = instanceManager.getNextInstance()) != null) {
            if (! instance.extendedName.equals("empty-48-48-even-5.scen")) {
                continue;
            }
            foundInstance = true;
            System.out.println("Testing instance: " + instance.extendedName);
            Agent agent1 = instance.agents.get(1);
            Agent agent7 = instance.agents.get(7);
            // plan agent 1
            SingleAgentPlan plan1 = new SingleAgentPlan(agent1);
            plan1.addMoves(List.of(
                    new Move(agent1, 189, instance.map.getMapLocation(new Coordinate_2D(36, 36)), instance.map.getMapLocation(new Coordinate_2D(36, 36))),
                    new Move(agent1, 190, instance.map.getMapLocation(new Coordinate_2D(36, 36)), instance.map.getMapLocation(new Coordinate_2D(36, 35))),
                    new Move(agent1, 191, instance.map.getMapLocation(new Coordinate_2D(36, 35)), instance.map.getMapLocation(new Coordinate_2D(36, 34))),
                    new Move(agent1, 192, instance.map.getMapLocation(new Coordinate_2D(36, 34)), instance.map.getMapLocation(new Coordinate_2D(36, 33))),
                    new Move(agent1, 193, instance.map.getMapLocation(new Coordinate_2D(36, 33)), instance.map.getMapLocation(new Coordinate_2D(36, 32))),
                    new Move(agent1, 194, instance.map.getMapLocation(new Coordinate_2D(36, 32)), instance.map.getMapLocation(new Coordinate_2D(36, 31))),
                    new Move(agent1, 195, instance.map.getMapLocation(new Coordinate_2D(36, 31)), instance.map.getMapLocation(new Coordinate_2D(36, 30))),
                    new Move(agent1, 196, instance.map.getMapLocation(new Coordinate_2D(36, 30)), instance.map.getMapLocation(new Coordinate_2D(36, 29))),
                    new Move(agent1, 197, instance.map.getMapLocation(new Coordinate_2D(36, 29)), instance.map.getMapLocation(new Coordinate_2D(36, 28))),
                    new Move(agent1, 198, instance.map.getMapLocation(new Coordinate_2D(36, 28)), instance.map.getMapLocation(new Coordinate_2D(37, 28))),
                    new Move(agent1, 199, instance.map.getMapLocation(new Coordinate_2D(37, 28)), instance.map.getMapLocation(new Coordinate_2D(38, 28))),
                    new Move(agent1, 200, instance.map.getMapLocation(new Coordinate_2D(38, 28)), instance.map.getMapLocation(new Coordinate_2D(39, 28))),
                    new Move(agent1, 201, instance.map.getMapLocation(new Coordinate_2D(39, 28)), instance.map.getMapLocation(new Coordinate_2D(40, 28)))
            ));
            // plan agent 7
            SingleAgentPlan plan7 = new SingleAgentPlan(agent7);
            plan7.addMoves(List.of(
                    new Move(agent7, 189, instance.map.getMapLocation(new Coordinate_2D(36, 29)), instance.map.getMapLocation(new Coordinate_2D(36, 29))),
                    new Move(agent7, 190, instance.map.getMapLocation(new Coordinate_2D(36, 29)), instance.map.getMapLocation(new Coordinate_2D(36, 30))),
                    new Move(agent7, 191, instance.map.getMapLocation(new Coordinate_2D(36, 30)), instance.map.getMapLocation(new Coordinate_2D(36, 31))),
                    new Move(agent7, 192, instance.map.getMapLocation(new Coordinate_2D(36, 31)), instance.map.getMapLocation(new Coordinate_2D(36, 32))),
                    new Move(agent7, 193, instance.map.getMapLocation(new Coordinate_2D(36, 32)), instance.map.getMapLocation(new Coordinate_2D(36, 33))),
                    new Move(agent7, 194, instance.map.getMapLocation(new Coordinate_2D(36, 33)), instance.map.getMapLocation(new Coordinate_2D(36, 34))),
                    new Move(agent7, 195, instance.map.getMapLocation(new Coordinate_2D(36, 34)), instance.map.getMapLocation(new Coordinate_2D(35, 34))),
                    new Move(agent7, 196, instance.map.getMapLocation(new Coordinate_2D(35, 34)), instance.map.getMapLocation(new Coordinate_2D(34, 34))),
                    new Move(agent7, 197, instance.map.getMapLocation(new Coordinate_2D(34, 34)), instance.map.getMapLocation(new Coordinate_2D(33, 34))),
                    new Move(agent7, 198, instance.map.getMapLocation(new Coordinate_2D(33, 34)), instance.map.getMapLocation(new Coordinate_2D(32, 34))),
                    new Move(agent7, 199, instance.map.getMapLocation(new Coordinate_2D(32, 34)), instance.map.getMapLocation(new Coordinate_2D(31, 34))),
                    new Move(agent7, 200, instance.map.getMapLocation(new Coordinate_2D(31, 34)), instance.map.getMapLocation(new Coordinate_2D(30, 34))),
                    new Move(agent7, 201, instance.map.getMapLocation(new Coordinate_2D(30, 34)), instance.map.getMapLocation(new Coordinate_2D(29, 34)))
            ));

            RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
            conflictAvoidanceTable.addPlan(plan1);

            System.out.println("Adding plan 1");
            assertEquals(193, conflictAvoidanceTable.firstConflictTime(plan7.moveAt(193), false));

            conflictAvoidanceTable.removePlan(plan1);

            System.out.println("Adding plan 7");
            conflictAvoidanceTable.addPlan(plan7);
            assertEquals(193, conflictAvoidanceTable.firstConflictTime(plan1.moveAt(193), false));
        }
        assertTrue(foundInstance, "No instance found with name empty-48-48-even-5.scen");
    }

    @Test
    void testLastTimeToConsiderConflicts_IgnoresConflictsAfterSpecifiedTime() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 5, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        Agent queryingAgent = new Agent(2, coor05, coor15);

        // Act & Assert - Without time limit
        int conflictsAtTime3 = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsAtTime3);
        
        int conflictsAtTime4 = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 4, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsAtTime4);
        
        // Set time limit to 3
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(3);
        
        // Act & Assert - With time limit
        int conflictsAtTime3WithLimit = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(1, conflictsAtTime3WithLimit, "Conflicts at time 3 should still be counted");
        
        int conflictsAtTime4WithLimit = conflictAvoidanceTable.numConflicts(new Move(queryingAgent, 4, instanceEmpty1.map.getMapLocation(coor04), instanceEmpty1.map.getMapLocation(coor05)), false);
        assertEquals(0, conflictsAtTime4WithLimit, "Conflicts at time 4 should be ignored");
    }

    @Test
    void testLastTimeToConsiderConflicts_WithEdgeConflicts() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor14)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor14)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor15))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        Agent queryingAgent = new Agent(2, coor05, coor15);

        // Act & Assert - Without time limit
        // Edge conflict at time 3
        int edgeConflicts = conflictAvoidanceTable.getNumberOfEdgeConflicts(
                new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13))
        );
        assertEquals(1, edgeConflicts, "Should detect edge conflict at time 3");

        // Set time limit to 2
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(2);
        
        // Act & Assert - With time limit
        int edgeConflictsWithLimit = conflictAvoidanceTable.getNumberOfEdgeConflicts(
                new Move(queryingAgent, 3, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13))
        );
        assertEquals(0, edgeConflictsWithLimit, "Edge conflicts after time 2 should be ignored");
    }

    @Test
    void testLastTimeToConsiderConflicts_WithGoalConflicts() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor05), instanceEmpty1.map.getMapLocation(coor05))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        Agent queryingAgent = new Agent(2, coor15, coor05);

        // Act & Assert - Without time limit
        // Last move (goal conflict)
        Move lastMove = new Move(queryingAgent, 5, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor05));
        int goalConflicts = conflictAvoidanceTable.numConflicts(lastMove, true);
        assertEquals(1, goalConflicts, "Should detect goal conflict");

        // Set time limit to 4 (before the goal move)
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(4);
        
        // Act & Assert - With time limit
        int goalConflictsWithLimit = conflictAvoidanceTable.numConflicts(lastMove, true);
        assertEquals(0, goalConflictsWithLimit, "Goal conflicts after time 4 should be ignored");
    }

    @Test
    void testLastTimeToConsiderConflicts_WithFirstConflictTime() {
        // Arrange
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor05);
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor14)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor12)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor12), instanceEmpty1.map.getMapLocation(coor11)),
                new Move(agentToAvoid, 5, instanceEmpty1.map.getMapLocation(coor11), instanceEmpty1.map.getMapLocation(coor10))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        Agent queryingAgent = new Agent(2, coor10, coor15);

        // Act & Assert - Without time limit
        // This plan conflicts at multiple times, firstConflictTime should return the earliest one
        Move queryMove = new Move(queryingAgent, 1, instanceEmpty1.map.getMapLocation(coor10), instanceEmpty1.map.getMapLocation(coor10));
        int firstConflictTime = conflictAvoidanceTable.firstConflictTime(queryMove, true);
        assertEquals(5, firstConflictTime, "Should detect first conflict at time 5");

        // Set time limit to 4
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(4);
        
        // Act & Assert - With time limit
        int firstConflictTimeWithLimit = conflictAvoidanceTable.firstConflictTime(queryMove, true);
        assertEquals(-1, firstConflictTimeWithLimit, "Should not detect any conflicts when limited to time 4");
    }

    @Test
    void testLastTimeToConsiderConflicts_QueryLastMoveWithRegularMove() {
        // Arrange - Create a plan where one agent occupies a location but isn't at its goal
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor03); // target is coor03
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor14)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor12)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor12), instanceEmpty1.map.getMapLocation(coor10)),
                new Move(agentToAvoid, 5, instanceEmpty1.map.getMapLocation(coor10), instanceEmpty1.map.getMapLocation(coor10))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        
        // Query agent's last move conflicts with a regular move of the other agent
        Agent queryingAgent = new Agent(2, coor15, coor10);

        // Act & Assert - Without time limit
        // Create a last move that conflicts with agentToAvoid's regular move at time 5
        Move lastMove = new Move(queryingAgent, 4, instanceEmpty1.map.getMapLocation(coor11), instanceEmpty1.map.getMapLocation(coor10));
        int conflicts = conflictAvoidanceTable.numConflicts(lastMove, true);
        assertEquals(3, conflicts, "Expecting conflicts at time 4, 5, and then target(/goal) conflict at time 6");

        // Set time limit to 3
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(3);
        
        // Act & Assert - With time limit
        int conflictsWithLimit = conflictAvoidanceTable.numConflicts(lastMove, true);
        assertEquals(0, conflictsWithLimit, "Conflicts after time 3 should be ignored");
    }

    @Test
    void testLastTimeToConsiderConflicts_QueryRegularMoveWithLastMove() {
        // Arrange - Create a plan where one agent is at its goal
        RemovableConflictAvoidanceTableWithContestedGoals conflictAvoidanceTable = new RemovableConflictAvoidanceTableWithContestedGoals();
        Agent agentToAvoid = new Agent(1, coor15, coor10); // target is coor10
        SingleAgentPlan planToAvoid = new SingleAgentPlan(agentToAvoid);
        planToAvoid.addMoves(List.of(
                new Move(agentToAvoid, 1, instanceEmpty1.map.getMapLocation(coor15), instanceEmpty1.map.getMapLocation(coor14)),
                new Move(agentToAvoid, 2, instanceEmpty1.map.getMapLocation(coor14), instanceEmpty1.map.getMapLocation(coor13)),
                new Move(agentToAvoid, 3, instanceEmpty1.map.getMapLocation(coor13), instanceEmpty1.map.getMapLocation(coor12)),
                new Move(agentToAvoid, 4, instanceEmpty1.map.getMapLocation(coor12), instanceEmpty1.map.getMapLocation(coor11)),
                new Move(agentToAvoid, 5, instanceEmpty1.map.getMapLocation(coor11), instanceEmpty1.map.getMapLocation(coor10))
        ));
        conflictAvoidanceTable.addPlan(planToAvoid);
        
        // Create a regular move that conflicts with agentToAvoid's goal occupancy
        Agent queryingAgent = new Agent(2, coor15, coor05);

        // Act & Assert - Without time limit
        // Non-last move that would conflict with goal occupancy at time 7 (goal occupancy starts at time 6)
        Move regularMove = new Move(queryingAgent, 7, instanceEmpty1.map.getMapLocation(coor11), instanceEmpty1.map.getMapLocation(coor10));
        int conflicts = conflictAvoidanceTable.numConflicts(regularMove, false);
        assertEquals(1, conflicts, "Regular move should conflict with goal occupancy");

        // Set time limit to 6
        conflictAvoidanceTable.setLastTimeToConsiderConflicts(6);
        
        // Act & Assert - With time limit
        int conflictsWithLimit = conflictAvoidanceTable.numConflicts(regularMove, false);
        assertEquals(0, conflictsWithLimit, "Conflicts after time 6 should be ignored");
    }
}