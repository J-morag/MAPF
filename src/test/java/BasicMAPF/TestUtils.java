package BasicMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;

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

    public static void addRandomConstraints(Agent agent, List<I_Location> locations, Random rand, ConstraintSet constraints,
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
}
