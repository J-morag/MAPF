package BasicMAPF;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.I_ConstraintSet;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class TestUtils {

    public static Map<String, Map<String, String>> readResultsCSV(String pathToCsv) throws IOException {
        Map<String, Map<String, String>> result  = new HashMap<>();
        BufferedReader csvReader = new BufferedReader(new FileReader(pathToCsv));

        String headerRow = csvReader.readLine();
        String[] header = headerRow.split(",");
        int fileNameIndex = -1;
        for (int i = 0; i < header.length; i++) {
            if(header[i].equals("File")) {fileNameIndex = i;}
        }

        String row;
        while ((row = csvReader.readLine()) != null) {
            String[] tupleAsArray = row.split(",");
            if(tupleAsArray.length < 1 ) continue;
            Map<String, String> tupleAsMap = new HashMap<>(tupleAsArray.length);
            for (int i = 0; i < tupleAsArray.length; i++) {
                String value = tupleAsArray[i];
                tupleAsMap.put(header[i], value);
            }

            String key = tupleAsArray[fileNameIndex];
            result.put(key, tupleAsMap);
        }
        csvReader.close();

        return result;
    }

    public static void addRandomConstraints(Agent agent, List<I_Location> locations, Random rand, I_ConstraintSet constraints,
                                            int maxTime, int numConstraintsEachType) {
        for (int t = 1; t <= maxTime; t++) {
            Set<I_Location> checkDuplicates = new HashSet<>();
            Set<Constraint> edgeConstraints = new HashSet<>();
            for (int j = 0; j < numConstraintsEachType; j++) {
                // vertex constraint
                I_Location randomLocation;
                do {
                    randomLocation = locations.get(rand.nextInt(locations.size()));
                }
                while (checkDuplicates.contains(randomLocation));
                checkDuplicates.add(randomLocation);
                Constraint constraint = new Constraint(agent, t, null, randomLocation);
                constraints.add(constraint);

                // edge constraint
                I_Location toLocation;
                I_Location prevLocation;
                Constraint edgeConstraint;
                do {
                    toLocation = locations.get(rand.nextInt(locations.size()));
                    prevLocation = locations.get(rand.nextInt(locations.size()));
                    edgeConstraint = new Constraint(agent, t, prevLocation, toLocation);
                }
                while (toLocation.equals(prevLocation) || edgeConstraints.contains(edgeConstraint));
                edgeConstraints.add(edgeConstraint);
                constraints.add(edgeConstraint);
            }
        }
    }

    public static List<I_Location> planLocations(SingleAgentPlan planFromAStar) {
        List<I_Location> aStarPlanLocations = new ArrayList<>();
        for (Move move :
                planFromAStar) {
            if (move.timeNow == 1) {
                aStarPlanLocations.add(move.prevLocation);
            }
            aStarPlanLocations.add(move.currLocation);
        }
        return aStarPlanLocations;
    }

    @NotNull
    public static List<Integer> getPlanCosts(Agent agent, SingleAgentGAndH costFunction, List<I_Location> planLocations) {
        List<Integer> UCSPlanCosts = new ArrayList<>();
        UCSPlanCosts.add(0);
        I_Location prev = null;
        for (I_Location curr :
                planLocations) {
            if (prev != null){
                UCSPlanCosts.add(costFunction.cost(new Move(agent, 1, prev, curr)));
            }
            prev = curr;
        }
        return UCSPlanCosts;
    }

    public static class UnitCostAndNoHeuristic implements SingleAgentGAndH {
        @Override
        public float getH(SingleAgentAStar_Solver.AStarState state) {
            return 0;
        }

        @Override
        public int getHToTargetFromLocation(I_Coordinate target, I_Location currLocation) {
            return 0;
        }

        @Override
        public int cost(Move move) {
            return SingleAgentGAndH.super.cost(move);
        }

        @Override
        public boolean isConsistent() {
            return true;
        }

        @Override
        public String toString() {
            return "All edges = 1";
        }
    }

    public static final SingleAgentGAndH unitCostAndNoHeuristic = new UnitCostAndNoHeuristic();
}
