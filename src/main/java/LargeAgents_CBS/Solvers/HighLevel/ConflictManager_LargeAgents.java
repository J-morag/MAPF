package LargeAgents_CBS.Solvers.HighLevel;

import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.DataStructures.TimeLocation;
import BasicCBS.Solvers.SingleAgentPlan;
import LargeAgents_CBS.Instances.Maps.GraphLocationGroup;
import LargeAgents_CBS.Instances.Maps.GraphMapVertex_LargeAgents;

public class ConflictManager_LargeAgents extends ConflictManager {


    @Override
    protected void addAgentNewPlan(SingleAgentPlan singleAgentPlan) {

        if ( singleAgentPlan == null ){
            return;
        }

        int agentFirstMoveTime = singleAgentPlan.getFirstMoveTime();
        int goalTime = singleAgentPlan.getEndTime();

        /*  Check for conflicts and Add timeLocations */
        for (int time = agentFirstMoveTime; time <= goalTime; time++) {

            // Imp - change location to Group

            // Move's from location is 'prevLocation' , therefor timeLocation is time - 1
            GraphLocationGroup locationGroup = (GraphLocationGroup) singleAgentPlan.moveAt(time).prevLocation;
            for (GraphMapVertex_LargeAgents mapCellLocation: locationGroup.getAllCells()) {
                TimeLocation timeLocation = new TimeLocation(time - 1, mapCellLocation);
                super.checkAddConflictsByTimeLocation(timeLocation, singleAgentPlan); // Checks for conflicts
                this.timeLocationTables.addTimeLocation(timeLocation, singleAgentPlan);
            }
        }

        // Check final move to goalLocation
        GraphLocationGroup locationGroup = (GraphLocationGroup) singleAgentPlan.moveAt(goalTime).currLocation;
        TimeLocation goalTimeLocation = new TimeLocation(goalTime, locationGroup);
        super.checkAddConflictsByTimeLocation(goalTimeLocation, singleAgentPlan); // Checks for conflicts
        this.timeLocationTables.addTimeLocation(goalTimeLocation, singleAgentPlan);


        // Checks for conflicts and add if exists. Adds the goal's timeLocation
        this.manageGoalLocationFromPlan(goalTime, singleAgentPlan);
    }


    @Override
    protected void manageGoalLocationFromPlan(int goalTime, SingleAgentPlan singleAgentPlan) {

        // Imp - change location to Group
        GraphLocationGroup goalGroupLocation = (GraphLocationGroup) singleAgentPlan.moveAt(goalTime).currLocation;
        for (GraphMapVertex_LargeAgents mapCellLocation: goalGroupLocation.getAllCells()) {
            TimeLocation goalCellTimeLocation = new TimeLocation(goalTime, mapCellLocation);

            /*  = Check if this agentAtGoal conflicts with other agents =   */
            super.checkAddSwappingConflicts(goalTime, singleAgentPlan);
            super.checkAddVertexConflictsWithGoal(goalCellTimeLocation, singleAgentPlan);

            /*  = Add goal timeLocation =  */
            this.timeLocationTables.addGoalTimeLocation(goalCellTimeLocation, singleAgentPlan);
        }
    }
}
