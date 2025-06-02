package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import org.junit.jupiter.api.Test;

import java.util.List;

import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Coordiantes.coor04;
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
}