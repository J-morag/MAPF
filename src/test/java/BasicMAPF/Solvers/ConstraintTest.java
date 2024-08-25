package BasicMAPF.Solvers;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static BasicMAPF.TestConstants.Maps.mapCircle;

import static org.junit.jupiter.api.Assertions.*;

class ConstraintTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    void acceptsForVertexConflict() {
        I_Coordinate coor13 = new Coordinate_2D(1,3);
        I_Coordinate coor14 = new Coordinate_2D(1,4);
        I_Coordinate coor24 = new Coordinate_2D(2,4);
        I_Coordinate coor34 = new Coordinate_2D(3,4);
        Agent agent1 = new Agent(0, coor13, coor14);
        Agent agent2 = new Agent(1, coor24, coor24);

        // this move is just to illustrate why the constraint might exist, it isn't actually used
        Move move1 = new Move(agent1, 1, mapCircle.getMapLocation(coor13), mapCircle.getMapLocation(coor14));
        Move moveConflicts = new Move(agent2, 1, mapCircle.getMapLocation(coor24), mapCircle.getMapLocation(coor14));
        Move moveDoesntConflict = new Move(agent2, 1, mapCircle.getMapLocation(coor24), mapCircle.getMapLocation(coor34));

        Constraint constraintHoldsSameAgent = new Constraint(agent2, 1, mapCircle.getMapLocation(coor14));
        Constraint constraintHoldsAllAgents = new Constraint(null, 1, mapCircle.getMapLocation(coor14));

        Constraint constraintDoesntHoldDifferentAgent = new Constraint(agent1, 1, mapCircle.getMapLocation(coor14));
        Constraint constraintDoesntHoldDifferentTime = new Constraint(agent2, 2, mapCircle.getMapLocation(coor14));
        Constraint constraintDoesntHoldDifferentlocation = new Constraint(agent2, 1, mapCircle.getMapLocation(coor13));
        Constraint constraintDoesntHoldPrevlocation = new Constraint(agent2, 1, mapCircle.getMapLocation(coor24));

        /*  =should accept=  */
        /*  =  =because constraint doesn't hold=  */
        assertTrue(constraintDoesntHoldDifferentAgent.accepts(moveConflicts));
        assertTrue(constraintDoesntHoldDifferentTime.accepts(moveConflicts));
        assertTrue(constraintDoesntHoldDifferentlocation.accepts(moveConflicts));
        assertTrue(constraintDoesntHoldPrevlocation.accepts(moveConflicts));
        /*  =  =because move doesn't violate the constraint=  */
        assertTrue(constraintHoldsSameAgent.accepts(moveDoesntConflict));
        assertTrue(constraintHoldsAllAgents.accepts(moveDoesntConflict));

        /*  =should reject (return false)=  */
        assertFalse(constraintHoldsSameAgent.accepts(moveConflicts));
        assertFalse(constraintHoldsAllAgents.accepts(moveConflicts));

    }

    @Test
    void acceptsForSwappingConflicts() {
        // doesnt check things that are checked in LocationConstraintTest
        I_Coordinate coor13 = new Coordinate_2D(1,3);
        I_Coordinate coor14 = new Coordinate_2D(1,4);
        I_Coordinate coor24 = new Coordinate_2D(2,4);
        I_Coordinate coor12 = new Coordinate_2D(1,2);
        Agent agent1 = new Agent(0, coor13, coor14);
        Agent agent2 = new Agent(0, coor24, coor24);

        // this move is just to illustrate why the constraint might exist, it isn't actually used
        Move move1 = new Move(agent1, 1, mapCircle.getMapLocation(coor13), mapCircle.getMapLocation(coor14));

        Move moveConflicts = new Move(agent2, 1, mapCircle.getMapLocation(coor14), mapCircle.getMapLocation(coor13));
        Move moveDoesntConflictOnMoveConstraint = new Move(agent2, 1, mapCircle.getMapLocation(coor12), mapCircle.getMapLocation(coor13));

        Constraint constraintHoldsSameAgent = new Constraint(agent2, 1, mapCircle.getMapLocation(coor14), mapCircle.getMapLocation(coor13));
        Constraint constraintHoldsAllAgents = new Constraint(null, 1, mapCircle.getMapLocation(coor14), mapCircle.getMapLocation(coor13));

        Constraint constraintDoesntHoldDifferentPrevlocation = new Constraint(agent2, 1, mapCircle.getMapLocation(coor12), mapCircle.getMapLocation(coor13));

        /*  =should accept=  */
        /*  =  =because constraint doesn't hold=  */
        assertTrue(constraintDoesntHoldDifferentPrevlocation.accepts(moveConflicts));
        /*  =  =because move doesn't violate the constraint=  */
        assertTrue(constraintHoldsSameAgent.accepts(moveDoesntConflictOnMoveConstraint));
        assertTrue(constraintHoldsAllAgents.accepts(moveDoesntConflictOnMoveConstraint));

        /*  =should reject (return false)=  */
        assertFalse(constraintHoldsSameAgent.accepts(moveConflicts));
        assertFalse(constraintHoldsAllAgents.accepts(moveConflicts));

    }

}