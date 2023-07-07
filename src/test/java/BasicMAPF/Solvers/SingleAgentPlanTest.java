package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import Environment.Metrics.InstanceReport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static BasicMAPF.TestConstants.Agents.agent33to12;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Instances.instanceEmpty1;
import static BasicMAPF.TestConstants.Maps.mapCircle;
import static org.junit.jupiter.api.Assertions.*;

class SingleAgentPlanTest {
    private final I_Location location12 = mapCircle.getMapLocation(coor12);
    private final I_Location location13 = mapCircle.getMapLocation(coor13);
    private final I_Location location14 = mapCircle.getMapLocation(coor14);
    private final I_Location location22 = mapCircle.getMapLocation(coor22);
    private final I_Location location24 = mapCircle.getMapLocation(coor24);
    private final I_Location location32 = mapCircle.getMapLocation(coor32);
    private final I_Location location33 = mapCircle.getMapLocation(coor33);
    private final I_Location location34 = mapCircle.getMapLocation(coor34);
    private final Agent agent1 = new Agent(0, coor13, coor14);
    private final Agent agent2 = new Agent(1, coor24, coor24);


    /*  =valid inputs=  */
    //note that validity of move from one location to the next (neighbors or not) is not checked by SingleAgentPlan
    private final Move move1agent1 = new Move(agent1, 1, location13, location14);
    private Move move2agent1 = new Move(agent1, 2, location14, location24);
    private final Move move3agent1 = new Move(agent1, 3, location24, location14);
    private final Move move1agent2 = new Move(agent2, 1, location24, location24);

    private final Move move4agent1 = new Move(agent1, 4, location14, location13);

    /*  =invalid inputs=  */
    private final Move move4agent1BadTime = new Move(agent1, 1, location14, location24);
    private final Move move4agent1BadAgent = new Move(agent2, 4, location14, location24);

    /*  =plans=  */
    private SingleAgentPlan emptyPlanAgent1;
    private SingleAgentPlan existingPlanAgent1;
    private SingleAgentPlan existingPlanAgent2;

    @BeforeEach
    void setUp() {
        /*  =init plans=  */
        emptyPlanAgent1 = new SingleAgentPlan(agent1);

        List<Move> agent1Moves123 = new ArrayList<>();
        agent1Moves123.add(move1agent1);
        agent1Moves123.add(move2agent1);
        agent1Moves123.add(move3agent1);
        existingPlanAgent1 = new SingleAgentPlan(agent1, agent1Moves123);

        List<Move> agent2Moves1 = new ArrayList<>();
        existingPlanAgent2 = new SingleAgentPlan(agent2, agent2Moves1);
    }

    @Test
    void addMove() {
        /*  =shouldn't throw=  */
        assertDoesNotThrow(() -> emptyPlanAgent1.addMove(move1agent1));
        setUp();
        assertDoesNotThrow(() -> emptyPlanAgent1.addMove(move4agent1)); //can start at any time
        assertDoesNotThrow(() -> existingPlanAgent1.addMove(move4agent1));

        /*  =should throw=  */
        setUp();
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.addMove(move1agent2)); //bad agent
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMove(move4agent1BadAgent));
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMove(move4agent1BadTime));
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMove(move3agent1)); //bad time (duplicate move)
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMove(null)); //null
    }

    @Test
    void addMoves() {
        List<Move> a1moves123 = Arrays.asList(move1agent1, move2agent1, move3agent1);
        Move move5agent1 = new Move(agent1, 5, location13, location13);
        List<Move> a1moves45 = Arrays.asList(move4agent1, move5agent1);

        /*  =shouldn't throw=  */
        assertDoesNotThrow(() -> emptyPlanAgent1.addMoves(a1moves123));
        setUp();
        assertDoesNotThrow(() -> emptyPlanAgent1.addMoves(new ArrayList<>()));
        assertDoesNotThrow(() -> existingPlanAgent1.addMoves(a1moves45));
        assertDoesNotThrow(() -> existingPlanAgent1.addMoves(new ArrayList<>()));

        /*  =should throw=  */
        setUp();
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMoves(a1moves123)); //bad times
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.addMoves(List.of(move1agent2))); //bad agent
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMoves(List.of(new Move(agent2, 5, location14, location14)))); //bad agent
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.addMoves(Arrays.asList(
                        move1agent1, new Move(agent2, 2, location14,location14), move3agent1))); //bad agent middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMoves(Arrays.asList(
                        move4agent1, new Move(agent2, 5, location14,location14),
                        new Move(agent1, 6, location14, location14)))); //bad agent middle
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.addMoves(Arrays.asList(move1agent1, move3agent1, move3agent1))); //bad time middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMoves(Arrays.asList(move5agent1, move5agent1))); //bad time middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.addMoves(null)); //null
    }

    @Test
    void setMoves() {
        List<Move> a1moves123 = Arrays.asList(move1agent1, move2agent1, move3agent1);
        Move move5agent1 = new Move(agent1, 5, location13, location13);
        Move move4agent1 = new Move(agent1, 4, location13, location14);
        List<Move> a1moves45 = Arrays.asList(move4agent1, move5agent1);

        /*  =shouldn't throw=  */
        assertDoesNotThrow(() -> emptyPlanAgent1.setMoves(a1moves123));
        setUp();
        assertDoesNotThrow(() -> emptyPlanAgent1.setMoves(new ArrayList<>()));
        assertDoesNotThrow(() -> existingPlanAgent1.setMoves(a1moves45));
        setUp();
        assertDoesNotThrow(() -> existingPlanAgent1.setMoves(new ArrayList<>()));

        /*  =should throw=  */
        setUp();
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.setMoves(List.of(move1agent2))); //bad agent
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.setMoves(List.of(new Move(agent2, 5, location14, location14)))); //bad agent
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.setMoves(Arrays.asList(
                        move1agent1, new Move(agent2, 2, location14,location14), move3agent1))); //bad agent middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.setMoves(Arrays.asList(
                        move4agent1, new Move(agent2, 5, location14,location14),
                        new Move(agent1, 6, location14, location14)))); //bad agent middle
        assertThrows(IllegalArgumentException.class,
                ()-> emptyPlanAgent1.setMoves(Arrays.asList(move1agent1, move3agent1, move3agent1))); //bad time middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.setMoves(Arrays.asList(move5agent1, move4agent1, move5agent1))); //bad time middle
        assertThrows(IllegalArgumentException.class,
                ()-> existingPlanAgent1.setMoves(null)); //null
    }

    @Test
    void getStartTime() {
        /*  =as initiated=  */
        assertEquals(-1, emptyPlanAgent1.getPlanStartTime());
        assertEquals(0, existingPlanAgent1.getPlanStartTime());
        SingleAgentPlan planStartsAt3 = new SingleAgentPlan(agent1, List.of(new Move(agent1, 4, location13, location12)));
        assertEquals(3, planStartsAt3.getPlanStartTime());

        /*  =when modified=  */
        emptyPlanAgent1.addMove(move2agent1);
        existingPlanAgent1.addMove(move4agent1);
        assertEquals(1, emptyPlanAgent1.getPlanStartTime());
        assertEquals(0, existingPlanAgent1.getPlanStartTime());
    }

    @Test
    void getEndTime() {
        /*  =as initiated=  */
        assertEquals(-1, emptyPlanAgent1.getEndTime());
        assertEquals(3, existingPlanAgent1.getEndTime());
        SingleAgentPlan planStartsAt3 = new SingleAgentPlan(agent1, List.of(new Move(agent1, 4, location13, location12)));
        assertEquals(4, planStartsAt3.getEndTime());

        /*  =when modified=  */
        emptyPlanAgent1.addMove(move2agent1);
        existingPlanAgent1.addMove(move4agent1);
        assertEquals(2, emptyPlanAgent1.getEndTime());
        assertEquals(4, existingPlanAgent1.getEndTime());
    }

    @Test
    void getElapsedTime() {
        /*  =as initiated=  */
        assertEquals(0, emptyPlanAgent1.size());
        assertEquals(3, existingPlanAgent1.size());
        SingleAgentPlan planStartsAt3 = new SingleAgentPlan(agent1, List.of(new Move(agent1, 4, location13, location12)));
        assertEquals(1, planStartsAt3.size());

        /*  =when modified=  */
        emptyPlanAgent1.addMove(move2agent1);
        assertEquals(1, emptyPlanAgent1.size());
        emptyPlanAgent1.addMove(move3agent1);
        assertEquals(2, emptyPlanAgent1.size());

        existingPlanAgent1.addMove(move4agent1);
        assertEquals(4, existingPlanAgent1.size());
    }

    @Test
    void testToString(){
        System.out.println(existingPlanAgent1.toString());
        System.out.println(existingPlanAgent2.toString());

    }

    @Test
    void moveAt() {
        assertEquals(move1agent1, existingPlanAgent1.moveAt(1));
        assertEquals(move2agent1, existingPlanAgent1.moveAt(2));
        existingPlanAgent1.addMove(move4agent1);
        assertEquals(move4agent1, existingPlanAgent1.moveAt(4));

        //starting not at time 1
        move2agent1 = new Move(agent1, 2, location13, location14);
        emptyPlanAgent1.addMove(move2agent1);
        assertEquals(move2agent1, emptyPlanAgent1.moveAt(2));
        emptyPlanAgent1.addMove(move3agent1);
        emptyPlanAgent1.addMove(move4agent1);
        assertEquals(move3agent1, emptyPlanAgent1.moveAt(3));
        assertEquals(move4agent1, emptyPlanAgent1.moveAt(4));
    }

    @Test
    void conflictsBecauseAgentStaysAtGoal() {
        Agent agent1 = new Agent(0, coor13, coor12);
        Agent agent2 = new Agent(1, coor33, coor13);

        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1);
        planAgent1.addMove(new Move(agent1, 1, location13, location12));

        SingleAgentPlan planAgent2 = new SingleAgentPlan(agent2);
        planAgent2.addMove(new Move(agent2, 1, location33, location32));
        planAgent2.addMove(new Move(agent2, 2, location32, location22));
        planAgent2.addMove(new Move(agent2, 3, location22, location12));
        planAgent2.addMove(new Move(agent2, 4, location12, location13));

        assertTrue(planAgent1.conflictsWith(planAgent2));
        assertTrue(planAgent2.conflictsWith(planAgent1));

        SingleAgentPlan alternateAgent2 = new SingleAgentPlan(agent2);
        alternateAgent2.addMove(new Move(agent2, 1, location33, location34));
        alternateAgent2.addMove(new Move(agent2, 2, location34, location24));
        alternateAgent2.addMove(new Move(agent2, 3, location24, location14));
        alternateAgent2.addMove(new Move(agent2, 4, location14, location13));

        assertFalse(planAgent1.conflictsWith(alternateAgent2));
        assertFalse(alternateAgent2.conflictsWith(planAgent1));
    }

    @Test
    void markTargetWasVisitedGradualBuild() {
        Agent agent1 = new Agent(0, coor13, coor33);

        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1);
        planAgent1.addMove(new Move(agent1, 1, location13, location14));
        planAgent1.addMove(new Move(agent1, 2, location14, location24));
        planAgent1.addMove(new Move(agent1, 3, location24, location34));
        planAgent1.addMove(new Move(agent1, 4, location34, location33));

        assertTrue(planAgent1.containsTarget());
    }

    @Test
    void markTargetWasVisitedBatchBuild() {
        Agent agent1 = new Agent(0, coor13, coor33);

        List<Move> moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34), new Move(agent1, 4, location34, location33)});

        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1, moves);

        assertTrue(planAgent1.containsTarget());
    }

    @Test
    void markTargetWasVisitedNotLastMove() {
        Agent agent1 = new Agent(0, coor13, coor33);

        List<Move> moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34), new Move(agent1, 4, location34, location33), new Move(agent1, 5, location33, location32)});

        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1, moves);

        assertTrue(planAgent1.containsTarget());
    }
    @Test
    void doesntMarkTargetWasVisitedWhenWasNotVisited() {
        Agent agent1 = new Agent(0, coor13, coor33);

        List<Move> moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34)});

        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1, moves);

        assertFalse(planAgent1.containsTarget());
    }

    @Test
    void marksTargetVisitedWhenGeneratedBySolve() {
        SingleAgentAStar_Solver solver = new SingleAgentAStar_Solver();
        Solution solution = solver.solve(instanceEmpty1, new RunParameters(new InstanceReport()));
        System.out.println(solution);
        SingleAgentPlan plan = solution.getPlanFor(agent33to12);
        assertTrue(plan.containsTarget());
    }

    @Test
    void marksTargetNotVisitedAfterCleared() {
        Agent agent1 = new Agent(0, coor13, coor33);
        List<Move> moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34), new Move(agent1, 4, location34, location33), new Move(agent1, 5, location33, location32)});
        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1, moves);
        assertTrue(planAgent1.containsTarget());

        planAgent1.clearMoves();
        moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34)});
        planAgent1 = new SingleAgentPlan(agent1, moves);
        assertFalse(planAgent1.containsTarget());
    }

    @Test
    void marksTargetVisitedAfterCleared() {
        Agent agent1 = new Agent(0, coor13, coor33);
        List<Move> moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34), new Move(agent1, 4, location34, location33), new Move(agent1, 5, location33, location32)});
        SingleAgentPlan planAgent1 = new SingleAgentPlan(agent1, moves);
        assertTrue(planAgent1.containsTarget());

        planAgent1.clearMoves();
        moves = List.of(new Move[]{new Move(agent1, 1, location13, location14), new Move(agent1, 2, location14, location24),
                new Move(agent1, 3, location24, location34), new Move(agent1, 4, location34, location33), new Move(agent1, 5, location33, location32)});
        planAgent1 = new SingleAgentPlan(agent1, moves);
        assertTrue(planAgent1.containsTarget());
    }
}