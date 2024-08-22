package BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Instances.Maps.MapFactory;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Agents.agent53to15;
import static BasicMAPF.TestConstants.Coordiantes.coor34;
import static BasicMAPF.TestConstants.Coordiantes.coor43;
import static BasicMAPF.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

class UnmodifiableConstraintSetTest {

    @Test
    void AStarAndSIPPLargeNumberOfConstraintsWithInfiniteConstraintsBig(){
        SingleAgentAStarSIPP_Solver sipp = new SingleAgentAStarSIPP_Solver();
        SingleAgentAStar_Solver astar = new SingleAgentAStar_Solver();

        int mapDim = 20;
        Enum_MapLocationType[][] map_matrix = new Enum_MapLocationType[mapDim][mapDim];
        for (int i = 0; i < mapDim; i++) {
            for (int j = 0; j < mapDim; j++) {
                map_matrix[i][j] = Enum_MapLocationType.EMPTY;
            }
        }
        I_Map map = MapFactory.newSimple4Connected2D_GraphMap(map_matrix);
        MAPF_Instance baseInstance = new MAPF_Instance("instanceEmpty" + mapDim + "=" + mapDim, map,
                new Agent[]{agent53to05, agent43to11, agent33to12, agent12to33, agent04to00, agent00to55, agent43to53, agent53to15,
                        new Agent(100, new Coordinate_2D(1,2), new Coordinate_2D(mapDim - 2, mapDim - 3))});
        int seeds = 1;
        for (int seed = 0; seed < seeds; seed++) {
            for (Agent agent : baseInstance.agents) {
                MAPF_Instance testInstance = baseInstance.getSubproblemFor(agent);
                List<I_Location> locations = new ArrayList<>();
                for (int i = 0; i < mapDim; i++) {
                    for (int j = 0; j < mapDim; j++) {
                        I_Coordinate newCoor = new Coordinate_2D(i, j);
                        I_Location newLocation = testInstance.map.getMapLocation(newCoor);
                        locations.add(newLocation);
                    }
                }
                Random rand = new Random(seed);
                I_ConstraintSet constraints = new ConstraintSet();
                for (int i = 0; i < mapDim; i++){
                    I_Location randomLocation = locations.get(rand.nextInt(locations.size()));
                    GoalConstraint goalConstraint = new GoalConstraint(agent, rand.nextInt(3000), null, randomLocation, new Agent(1000, coor43,  coor34)); // arbitrary agent not in instance
                    constraints.add(goalConstraint);
                }
                addRandomConstraints(agent, locations, rand, constraints, 3000, mapDim);
                constraints = new UnmodifiableConstraintSet(constraints);
                RunParameters parameters = new RunParametersBuilder().setConstraints(constraints).createRP();
                long startTime = System.currentTimeMillis();
                Solution sippSolution = sipp.solve(testInstance, parameters);
                long endTime = System.currentTimeMillis();

                int sippExpandedNodes = sipp.getExpandedNodes();
                int sippGeneratedNodes = sipp.getGeneratedNodes();

                List<Integer> sippPlanCosts = null;
                boolean sippSolved = sippSolution != null;
                if (sippSolved){
                    List<I_Location> sippPlanLocations = planLocations(sippSolution.getPlanFor(agent));
                    sippPlanCosts = getPlanCosts(agent, unitCostAndNoHeuristic, sippPlanLocations);
                    System.out.println("SIPP:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(sippExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(sippGeneratedNodes);
                }
                else{
                    System.out.println("SIPP Didn't Solve!!!");
                }

                startTime = System.currentTimeMillis();
                Solution aStarSolution = astar.solve(testInstance, parameters);
                endTime = System.currentTimeMillis();

                int astarExpandedNodes = astar.getExpandedNodes();
                int astarGeneratedNodes = astar.getGeneratedNodes();

                List<Integer> aStarPlanCosts = null;
                boolean aStarSolved = aStarSolution != null;
                if (aStarSolved){
                    List<I_Location> aStarPlanLocations = planLocations(aStarSolution.getPlanFor(agent));
                    aStarPlanCosts = getPlanCosts(agent, unitCostAndNoHeuristic, aStarPlanLocations);
                    System.out.println("aStar:");
                    System.out.println("Running Time:");
                    System.out.println(endTime - startTime);
                    System.out.println("Expanded nodes:");
                    System.out.println(astarExpandedNodes);
                    System.out.println("Generated nodes:");
                    System.out.println(astarGeneratedNodes);
                }
                else{
                    System.out.println("aStar Didn't Solve!!!");
                }


                System.out.println("Costs were:");
                System.out.println(unitCostAndNoHeuristic);

                assertTrue(!aStarSolved || sippSolved, "SIPP should solve if AStar solved");

                if (aStarSolved && sippSolved){
                    int costAStar = 0;
                    int costSipp = 0;
                    for (int i = 0; i < Math.max(aStarPlanCosts.size(), sippPlanCosts.size()); i++) {
                        if (i < aStarPlanCosts.size()){
                            costAStar += aStarPlanCosts.get(i);
                        }
                        if (i < sippPlanCosts.size()){
                            costSipp += sippPlanCosts.get(i);
                        }
                    }
                    assertEquals(costAStar, costSipp, "aStar cost " + costAStar + " should be the same as Sipp cost " + costSipp);
                    assertTrue(astarExpandedNodes >= sippExpandedNodes, "aStar number of expanded nodes: " + astarExpandedNodes + " not be smaller than Sipp number of expanded nodes: " + sippExpandedNodes);
                }
            }
        }
    }

}