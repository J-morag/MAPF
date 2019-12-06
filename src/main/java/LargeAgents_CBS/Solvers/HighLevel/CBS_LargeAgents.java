package LargeAgents_CBS.Solvers.HighLevel;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.CBS.CBS_Solver;
import BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement.I_ConflictManager;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import LargeAgents_CBS.Solvers.LowLevel.AStar_LargeAgents;
import LargeAgents_CBS.Solvers.LowLevel.DistanceTableHeuristic_LargeAgents;

import java.util.ArrayList;
import java.util.Objects;

public class CBS_LargeAgents extends CBS_Solver {


    @Override
    protected void init(MAPF_Instance instance, RunParameters runParameters) {
        //super.init(instance, runParameters);
        this.initialConstraints = Objects.requireNonNullElseGet(runParameters.constraints, ConstraintSet::new);
        this.currentConstraints = new ConstraintSet();
        this.generatedNodes = 0;
        this.expandedNodes = 0;
        this.instance = instance;
        this.aStarHeuristic = this.lowLevelSolver instanceof SingleAgentAStar_Solver ?
                new DistanceTableHeuristic_LargeAgents(new ArrayList<>(this.instance.agents), this.instance.map) :
                null;
    }


    @Override
    protected I_ConflictManager getConflictAvoidanceTableFor(CBS_Solver.CBS_Node node) {

        I_ConflictManager conflictManager = new ConflictManager_LargeAgents();
        for (SingleAgentPlan plan :
                node.getSolution()) {
            conflictManager.addPlan(plan);
        }
        return conflictManager;
    }
}
