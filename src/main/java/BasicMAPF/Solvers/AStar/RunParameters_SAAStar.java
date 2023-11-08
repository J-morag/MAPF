package BasicMAPF.Solvers.AStar;

import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Solvers.AStar.GoalConditions.I_AStarGoalCondition;
import BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictAvoidance.I_ConflictAvoidanceTable;
import BasicMAPF.DataTypesAndStructures.RunParameters;

public class RunParameters_SAAStar extends RunParameters {
    public I_ConflictAvoidanceTable conflictAvoidanceTable;
    public I_Coordinate sourceCoor;
    public I_Coordinate targetCoor;
    public float fBudget = Float.POSITIVE_INFINITY;
    public I_AStarGoalCondition goalCondition;

    public RunParameters_SAAStar(RunParameters parameters){
        super(parameters);
    }

}
