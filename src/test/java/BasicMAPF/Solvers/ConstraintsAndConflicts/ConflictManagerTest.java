package BasicMAPF.Solvers.ConstraintsAndConflicts;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.ConflictAvoidance;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocationTables;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.MinTimeConflictSelectionStrategy;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.RemovableConflictManager;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import static BasicMAPF.TestConstants.Maps.*;


public class ConflictManagerTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    public void goalConflict(){

        ConflictManager conflictAvoidanceTable = new ConflictManager(new MinTimeConflictSelectionStrategy());



        /*  = Add a1 Plan =
            { S1 , G1 }
            S = Start
            G = Goal
        */
        Agent a1 = new Agent(1,new Coordinate_2D(0,0),new Coordinate_2D(0,1));
        SingleAgentPlan a1_plan;
        ArrayList<Move> a1_moves = new ArrayList<>();
        a1_moves.add(new Move(a1,1, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0))));
        a1_moves.add(new Move(a1,2, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0))));
        a1_moves.add(new Move(a1,3, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0))));
        a1_moves.add(new Move(a1,4, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0))));
        a1_moves.add(new Move(a1,5, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,1))));

        a1_plan = new SingleAgentPlan(a1,a1_moves);
        conflictAvoidanceTable.addPlan(a1_plan);




        /*  = Add a2 Plan =
            { EE , S2 & G2 } // (0,1) is Start and Goal
            EE = Empty
            S = Start
            G = Goal
        */
        Agent a2 = new Agent(2,new Coordinate_2D(0,1),new Coordinate_2D(0,1));
        SingleAgentPlan a2_plan;
        ArrayList<Move> a2_moves = new ArrayList<>();
        a2_moves.add(new Move(a2,1, mapTwoLocations.getMapLocation(new Coordinate_2D(0,1)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,1))));


        a2_plan = new SingleAgentPlan(a2,a2_moves);
        conflictAvoidanceTable.addPlan(a2_plan);


        /*      == Expected conflicts ==     */

        VertexConflict expectedGoalConflict = new VertexConflict(a1, a2, 5, mapTwoLocations.getMapLocation(new Coordinate_2D(0,1)));

        HashSet<A_Conflict> expectedSet = new HashSet<>();
        expectedSet.add(expectedGoalConflict);


        /*      = Test actual values =  */
        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, conflictAvoidanceTable.getAllConflicts()));

    }





    @Test
    public void swappingConflict2LocationMap(){

        ConflictManager conflictAvoidanceTable = new ConflictManager(new MinTimeConflictSelectionStrategy());



        /*  = Add a1 Plan =
            { S1 , G1 }
            S = Start
            G = Goal
        */
        Agent a1 = new Agent(1,new Coordinate_2D(0,0),new Coordinate_2D(0,1));
        SingleAgentPlan a1_plan;
        ArrayList<Move> a1_moves = new ArrayList<>();
        a1_moves.add(new Move(a1,1, mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,1))));

        a1_plan = new SingleAgentPlan(a1,a1_moves);
        conflictAvoidanceTable.addPlan(a1_plan);




        /*  = Add a2 Plan =
            { G2 , S2 }
            S = Start
            G = Goal
        */
        Agent a2 = new Agent(2,new Coordinate_2D(0,1),new Coordinate_2D(0,0));
        SingleAgentPlan a2_plan;
        ArrayList<Move> a2_moves = new ArrayList<>();
        a2_moves.add(new Move(a2,1, mapTwoLocations.getMapLocation(new Coordinate_2D(0,1)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0))));


        a2_plan = new SingleAgentPlan(a2,a2_moves);
        conflictAvoidanceTable.addPlan(a2_plan);


        /*      == Expected conflicts ==     */

        SwappingConflict expectedConflict_time1 = new SwappingConflict(a1,a2,1, mapTwoLocations.getMapLocation(new Coordinate_2D(0,1)), mapTwoLocations.getMapLocation(new Coordinate_2D(0,0)));

        HashSet<A_Conflict> expectedSet = new HashSet<>();
        expectedSet.add(expectedConflict_time1);


        /*      = Test actual values =  */
        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, conflictAvoidanceTable.getAllConflicts()));


    }



    @Test
    public void TwoAgentsWith4VertexConflicts_graphH() {

        ConflictManager conflictAvoidanceTable = new ConflictManager(new MinTimeConflictSelectionStrategy());


        /*  = Add a1 Plan =
            { S1, WW, WW, G1},
            { T1, T2, T3, T4},
            { EE, WW, WW, EE},
            T = Time
            S = Start
            G = Goal
            EE = Empty location
            WW = Wall
        */
        Agent a1 = new Agent(1,new Coordinate_2D(0,0),new Coordinate_2D(0,3));
        SingleAgentPlan a1_plan;
        ArrayList<Move> a1_moves = new ArrayList<>();

        a1_moves.add(new Move(a1,1, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(1,0))));
        a1_moves.add(new Move(a1,2, mapH.getMapLocation(new Coordinate_2D(1,0)),mapH.getMapLocation(new Coordinate_2D(1,1))));
        a1_moves.add(new Move(a1,3, mapH.getMapLocation(new Coordinate_2D(1,1)),mapH.getMapLocation(new Coordinate_2D(1,2))));
        a1_moves.add(new Move(a1,4, mapH.getMapLocation(new Coordinate_2D(1,2)),mapH.getMapLocation(new Coordinate_2D(1,3))));
        a1_moves.add(new Move(a1,5, mapH.getMapLocation(new Coordinate_2D(1,3)),mapH.getMapLocation(new Coordinate_2D(0,3))));

        a1_plan = new SingleAgentPlan(a1,a1_moves);
        conflictAvoidanceTable.addPlan(a1_plan);

        /*  = Add a2 Plan =
            { EE, WW, WW, EE},
            { T1, T2, T3, T4},
            { S2, WW, WW, G2},
            T = Time
            S = Start
            G = Goal
            EE = Empty location
            WW = Wall
        */
        Agent a2 = new Agent(2,new Coordinate_2D(2,0),new Coordinate_2D(2,3));
        SingleAgentPlan a2_plan;
        ArrayList<Move> a2_moves = new ArrayList<>();

        a2_moves.add(new Move(a2,1, mapH.getMapLocation(new Coordinate_2D(2,0)), mapH.getMapLocation(new Coordinate_2D(1,0))));
        a2_moves.add(new Move(a2,2, mapH.getMapLocation(new Coordinate_2D(1,0)), mapH.getMapLocation(new Coordinate_2D(1,1))));
        a2_moves.add(new Move(a2,3, mapH.getMapLocation(new Coordinate_2D(1,1)), mapH.getMapLocation(new Coordinate_2D(1,2))));
        a2_moves.add(new Move(a2,4, mapH.getMapLocation(new Coordinate_2D(1,2)), mapH.getMapLocation(new Coordinate_2D(1,3))));
        a2_moves.add(new Move(a2,5, mapH.getMapLocation(new Coordinate_2D(1,3)), mapH.getMapLocation(new Coordinate_2D(2,3))));

        a2_plan = new SingleAgentPlan(a2,a2_moves);
        conflictAvoidanceTable.addPlan(a2_plan);


        System.out.println("TwoAgentsWith4VertexConflicts_graphH: Done - Initialized two plans");


        /*      = Copy constructor =      */
        RemovableConflictManager copiedTable = new RemovableConflictManager(conflictAvoidanceTable);
        assertTrue(ConflictAvoidance.equalsAllConflicts(conflictAvoidanceTable.getAllConflicts(), copiedTable.removableConflictCounter.getAllConflicts()));
        assertTrue(TimeLocationTables.equalsTimeLocations(conflictAvoidanceTable.timeLocationTables.timeLocation_Agents,copiedTable.timeLocationTables.timeLocation_Agents));
        System.out.println("TwoAgentsWith4VertexConflicts_graphH: Done - Copy Constructor");



        /*      = Expected values =     */

        /*      == Expected locations ==     */

        Map<TimeLocation,Set<Agent>> expected_timeLocationAgents = new HashMap<>();
        // Agent 1
        TimeLocation time0_a1 = new TimeLocation(0, mapH.getMapLocation(new Coordinate_2D(0,0)));
        expected_timeLocationAgents.computeIfAbsent(time0_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time0_a1).add(a1);
        TimeLocation time1_a1 = new TimeLocation(1, mapH.getMapLocation(new Coordinate_2D(1,0)));
        expected_timeLocationAgents.computeIfAbsent(time1_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time1_a1).add(a1);
        TimeLocation time2_a1 = new TimeLocation(2, mapH.getMapLocation(new Coordinate_2D(1,1)));
        expected_timeLocationAgents.computeIfAbsent(time2_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time2_a1).add(a1);
        TimeLocation time3_a1 = new TimeLocation(3, mapH.getMapLocation(new Coordinate_2D(1,2)));
        expected_timeLocationAgents.computeIfAbsent(time3_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time3_a1).add(a1);
        TimeLocation time4_a1 = new TimeLocation(4, mapH.getMapLocation(new Coordinate_2D(1,3)));
        expected_timeLocationAgents.computeIfAbsent(time4_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time4_a1).add(a1);
        TimeLocation time5_a1 = new TimeLocation(5, mapH.getMapLocation(new Coordinate_2D(0,3)));
        expected_timeLocationAgents.computeIfAbsent(time5_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time5_a1).add(a1);

        // Agent 2
        TimeLocation time0_a2 = new TimeLocation(0, mapH.getMapLocation(new Coordinate_2D(2,0)));
        expected_timeLocationAgents.computeIfAbsent(time0_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time0_a2).add(a2);
        TimeLocation time1_a2 = new TimeLocation(1, mapH.getMapLocation(new Coordinate_2D(1,0)));
        expected_timeLocationAgents.computeIfAbsent(time1_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time1_a2).add(a2);
        TimeLocation time2_a2 = new TimeLocation(2, mapH.getMapLocation(new Coordinate_2D(1,1)));
        expected_timeLocationAgents.computeIfAbsent(time2_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time2_a2).add(a2);
        TimeLocation time3_a2 = new TimeLocation(3, mapH.getMapLocation(new Coordinate_2D(1,2)));
        expected_timeLocationAgents.computeIfAbsent(time3_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time3_a2).add(a2);
        TimeLocation time4_a2 = new TimeLocation(4, mapH.getMapLocation(new Coordinate_2D(1,3)));
        expected_timeLocationAgents.computeIfAbsent(time4_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time4_a2).add(a2);
        TimeLocation time5_a2 = new TimeLocation(5, mapH.getMapLocation(new Coordinate_2D(2,3)));
        expected_timeLocationAgents.computeIfAbsent(time5_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time5_a2).add(a2);


        /*      == Expected conflicts ==     */

        VertexConflict expectedConflict_time1 = new VertexConflict(a1,a2,1,mapH.getMapLocation(new Coordinate_2D(1,0)));
        VertexConflict expectedConflict_time2 = new VertexConflict(a1,a2,2,mapH.getMapLocation(new Coordinate_2D(1,1)));
        VertexConflict expectedConflict_time3 = new VertexConflict(a1,a2,3,mapH.getMapLocation(new Coordinate_2D(1,2)));
        VertexConflict expectedConflict_time4 = new VertexConflict(a1,a2,4,mapH.getMapLocation(new Coordinate_2D(1,3)));

        HashSet<A_Conflict> expectedSet = new HashSet<>();
        expectedSet.add(expectedConflict_time1);
        expectedSet.add(expectedConflict_time2);
        expectedSet.add(expectedConflict_time3);
        expectedSet.add(expectedConflict_time4);


        /*  = Test actual values =  */

        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, copiedTable.removableConflictCounter.getAllConflicts()));
        assertTrue(TimeLocationTables.equalsTimeLocations(expected_timeLocationAgents,copiedTable.timeLocationTables.timeLocation_Agents));




        /*      = Test Select conflict =     */
        A_Conflict actualConflict_time1 = copiedTable.selectConflict();
        assertEquals(expectedConflict_time1,actualConflict_time1);



        /*    = Agent 1 new Plan =    */
        // Waits at start  position for t = 1
        SingleAgentPlan a1_newPlan;
        ArrayList<Move> a1_newMoves = new ArrayList<>();

        a1_newMoves.add(new Move(a1,1, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(0,0))));
        a1_newMoves.add(new Move(a1,2, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(1,0))));
        a1_newMoves.add(new Move(a1,3, mapH.getMapLocation(new Coordinate_2D(1,0)),mapH.getMapLocation(new Coordinate_2D(1,1))));
        a1_newMoves.add(new Move(a1,4, mapH.getMapLocation(new Coordinate_2D(1,1)),mapH.getMapLocation(new Coordinate_2D(1,2))));
        a1_newMoves.add(new Move(a1,5, mapH.getMapLocation(new Coordinate_2D(1,2)),mapH.getMapLocation(new Coordinate_2D(1,3))));
        a1_newMoves.add(new Move(a1,6, mapH.getMapLocation(new Coordinate_2D(1,3)),mapH.getMapLocation(new Coordinate_2D(0,3))));

        a1_newPlan = new SingleAgentPlan(a1,a1_newMoves);
        copiedTable.addPlan(a1_newPlan);


        System.out.println("TwoAgentsWith4VertexConflicts_graphH: Done - Add agent1 new plan");


        /*      = Expected values =     */
        expectedSet = new HashSet<>();


        /*      = Test actual values =  */
        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, copiedTable.removableConflictCounter.getAllConflicts()));


    }





    @Test
    public void TwoAgentsWith1SwappingConflict_graphH() {

        ConflictManager conflictAvoidanceTable = new ConflictManager(new MinTimeConflictSelectionStrategy());


        /*  = Add a1 Plan =
            { S1, WW, WW, G1},
            { T1, T2, T3, T4},
            { EE, WW, WW, EE},
            T = Time
            S = Start
            G = Goal
            EE = Empty location
            WW = Wall
        */
        Agent a1 = new Agent(1,new Coordinate_2D(0,0),new Coordinate_2D(0,3));
        SingleAgentPlan a1_plan;
        ArrayList<Move> a1_moves = new ArrayList<>();

        a1_moves.add(new Move(a1,1, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(1,0))));
        a1_moves.add(new Move(a1,2, mapH.getMapLocation(new Coordinate_2D(1,0)),mapH.getMapLocation(new Coordinate_2D(1,1))));
        a1_moves.add(new Move(a1,3, mapH.getMapLocation(new Coordinate_2D(1,1)),mapH.getMapLocation(new Coordinate_2D(1,2))));
        a1_moves.add(new Move(a1,4, mapH.getMapLocation(new Coordinate_2D(1,2)),mapH.getMapLocation(new Coordinate_2D(1,3))));
        a1_moves.add(new Move(a1,5, mapH.getMapLocation(new Coordinate_2D(1,3)),mapH.getMapLocation(new Coordinate_2D(0,3))));

        a1_plan = new SingleAgentPlan(a1,a1_moves);
        conflictAvoidanceTable.addPlan(a1_plan);


        /*  = Add a2 Plan =
            { EE, WW, WW, EE},
            { T4, T3, T2, T1},
            { G1, WW, WW, S2},
            T = Time
            S = Start
            G = Goal
            EE = Empty location
            WW = Wall
        */
        Agent a2 = new Agent(2,new Coordinate_2D(2,3),new Coordinate_2D(2,0));
        SingleAgentPlan a2_plan;
        ArrayList<Move> a2_moves = new ArrayList<>();

        a2_moves.add(new Move(a2,1, mapH.getMapLocation(new Coordinate_2D(2,3)), mapH.getMapLocation(new Coordinate_2D(1,3))));
        a2_moves.add(new Move(a2,2, mapH.getMapLocation(new Coordinate_2D(1,3)), mapH.getMapLocation(new Coordinate_2D(1,2))));
        a2_moves.add(new Move(a2,3, mapH.getMapLocation(new Coordinate_2D(1,2)), mapH.getMapLocation(new Coordinate_2D(1,1))));
        a2_moves.add(new Move(a2,4, mapH.getMapLocation(new Coordinate_2D(1,1)), mapH.getMapLocation(new Coordinate_2D(1,0))));
        a2_moves.add(new Move(a2,5, mapH.getMapLocation(new Coordinate_2D(1,0)), mapH.getMapLocation(new Coordinate_2D(2,0))));

        a2_plan = new SingleAgentPlan(a2,a2_moves);
        conflictAvoidanceTable.addPlan(a2_plan);


        System.out.println("TwoAgentsWith1SwappingConflict_graphH: Done - Initialized two plans");


        /*      = Copy constructor =      */
        RemovableConflictManager copiedTable = new RemovableConflictManager(conflictAvoidanceTable);
        assertTrue(ConflictAvoidance.equalsAllConflicts(conflictAvoidanceTable.getAllConflicts(), copiedTable.removableConflictCounter.getAllConflicts()));
        assertTrue(TimeLocationTables.equalsTimeLocations(conflictAvoidanceTable.timeLocationTables.timeLocation_Agents,copiedTable.timeLocationTables.timeLocation_Agents));
        System.out.println("TwoAgentsWith1SwappingConflict_graphH: Done - Copy Constructor");



        /*      = Expected values =     */

        /*      == Expected locations ==     */

        Map<TimeLocation,Set<Agent>> expected_timeLocationAgents = new HashMap<>();
        // Agent 1
        TimeLocation time0_a1 = new TimeLocation(0, mapH.getMapLocation(new Coordinate_2D(0,0)));
        expected_timeLocationAgents.computeIfAbsent(time0_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time0_a1).add(a1);
        TimeLocation time1_a1 = new TimeLocation(1, mapH.getMapLocation(new Coordinate_2D(1,0)));
        expected_timeLocationAgents.computeIfAbsent(time1_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time1_a1).add(a1);
        TimeLocation time2_a1 = new TimeLocation(2, mapH.getMapLocation(new Coordinate_2D(1,1)));
        expected_timeLocationAgents.computeIfAbsent(time2_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time2_a1).add(a1);
        TimeLocation time3_a1 = new TimeLocation(3, mapH.getMapLocation(new Coordinate_2D(1,2)));
        expected_timeLocationAgents.computeIfAbsent(time3_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time3_a1).add(a1);
        TimeLocation time4_a1 = new TimeLocation(4, mapH.getMapLocation(new Coordinate_2D(1,3)));
        expected_timeLocationAgents.computeIfAbsent(time4_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time4_a1).add(a1);
        TimeLocation time5_a1 = new TimeLocation(5, mapH.getMapLocation(new Coordinate_2D(0,3)));
        expected_timeLocationAgents.computeIfAbsent(time5_a1,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time5_a1).add(a1);

        // Agent 2
        TimeLocation time0_a2 = new TimeLocation(0, mapH.getMapLocation(new Coordinate_2D(2,3)));
        expected_timeLocationAgents.computeIfAbsent(time0_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time0_a2).add(a2);
        TimeLocation time1_a2 = new TimeLocation(1, mapH.getMapLocation(new Coordinate_2D(1,3)));
        expected_timeLocationAgents.computeIfAbsent(time1_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time1_a2).add(a2);
        TimeLocation time2_a2 = new TimeLocation(2, mapH.getMapLocation(new Coordinate_2D(1,2)));
        expected_timeLocationAgents.computeIfAbsent(time2_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time2_a2).add(a2);
        TimeLocation time3_a2 = new TimeLocation(3, mapH.getMapLocation(new Coordinate_2D(1,1)));
        expected_timeLocationAgents.computeIfAbsent(time3_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time3_a2).add(a2);
        TimeLocation time4_a2 = new TimeLocation(4, mapH.getMapLocation(new Coordinate_2D(1,0)));
        expected_timeLocationAgents.computeIfAbsent(time4_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time4_a2).add(a2);
        TimeLocation time5_a2 = new TimeLocation(5, mapH.getMapLocation(new Coordinate_2D(2,0)));
        expected_timeLocationAgents.computeIfAbsent(time5_a2,k -> new HashSet<Agent>());
        expected_timeLocationAgents.get(time5_a2).add(a2);


        /*      == Expected conflicts ==     */

        SwappingConflict expectedConflict_time3 = new SwappingConflict(a1,a2,3,mapH.getMapLocation(new Coordinate_2D(1,2)),mapH.getMapLocation(new Coordinate_2D(1,1)));

        HashSet<A_Conflict> expectedSet = new HashSet<>();
        expectedSet.add(expectedConflict_time3);


        /*  = Test actual values =  */

        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, copiedTable.removableConflictCounter.getAllConflicts()));
        assertTrue(TimeLocationTables.equalsTimeLocations(expected_timeLocationAgents,copiedTable.timeLocationTables.timeLocation_Agents));



        /*      = Test Select conflict =     */
        A_Conflict actualConflict_time1 = copiedTable.selectConflict();
        assertEquals(expectedConflict_time3,actualConflict_time1);



        /*    = Agent 1 new Plan =    */



        SingleAgentPlan a1_newPlan;
        ArrayList<Move> a1_newMoves = new ArrayList<>();

        a1_newMoves.add(new Move(a1,1, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(0,0))));
        a1_newMoves.add(new Move(a1,2, mapH.getMapLocation(new Coordinate_2D(0,0)),mapH.getMapLocation(new Coordinate_2D(1,0))));
        a1_newMoves.add(new Move(a1,3, mapH.getMapLocation(new Coordinate_2D(1,0)),mapH.getMapLocation(new Coordinate_2D(1,1))));
        a1_newMoves.add(new Move(a1,4, mapH.getMapLocation(new Coordinate_2D(1,1)),mapH.getMapLocation(new Coordinate_2D(1,2))));
        a1_newMoves.add(new Move(a1,5, mapH.getMapLocation(new Coordinate_2D(1,2)),mapH.getMapLocation(new Coordinate_2D(1,3))));
        a1_newMoves.add(new Move(a1,6, mapH.getMapLocation(new Coordinate_2D(1,3)),mapH.getMapLocation(new Coordinate_2D(0,3))));

        a1_newPlan = new SingleAgentPlan(a1,a1_newMoves);
        copiedTable.addPlan(a1_newPlan);


        System.out.println("TwoAgentsWith1SwappingConflict_graphH: Done - Add agent1 new plan");


        /*      = Expected values =     */
        expectedSet = new HashSet<>();
        VertexConflict expectedVertexConflict_time3 = new VertexConflict(a1,a2,3,mapH.getMapLocation(new Coordinate_2D(1,1)));
        expectedSet.add(expectedVertexConflict_time3);


        /*      = Test actual values =  */
        assertTrue(ConflictAvoidance.equalsAllConflicts(expectedSet, copiedTable.removableConflictCounter.getAllConflicts()));


    }



}