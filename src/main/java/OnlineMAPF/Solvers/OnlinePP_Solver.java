package OnlineMAPF.Solvers;


import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.*;
import BasicCBS.Solvers.AStar.DistanceTableAStarHeuristic;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import Environment.Metrics.InstanceReport;
import OnlineMAPF.*;

import java.io.IOException;
import java.util.*;

/**
 * An online version of {@link PrioritisedPlanning_Solver}. Agents disappear at their goals, and start in private garages.
 */
public class OnlinePP_Solver extends PrioritisedPlanning_Solver {

    /**
     * Constructor.
     *
     * @param lowLevelSolver A {@link I_Solver solver}, to be used for solving sub-problems where only one agent is to
     *                       be planned for, and the existing {@link SingleAgentPlan plans} for other
     *                       {@link Agent}s are to be avoided.
     */
    public OnlinePP_Solver(I_Solver lowLevelSolver) {
        super(lowLevelSolver);
        if(! (lowLevelSolver instanceof OnlineAStar) ) {
            throw new IllegalArgumentException(this.getClass().getSimpleName() + " requires an online low level solver.");
        }
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        for (Agent agent :
                instance.agents) {
            if (! (agent instanceof OnlineAgent) )
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " is an online solver and accepts only Online Agents.");
        }

        if(parameters.constraints != null){
            if (! (parameters.constraints instanceof OnlineConstraintSet) ) {
                throw new IllegalArgumentException(this.getClass().getSimpleName() + " is an online solver and accepts only Online Constraint Sets.");
            }
            super.constraints = parameters.constraints;
        }
        else{
            super.constraints = new OnlineConstraintSet();
        }
    }

    @Override
    protected Solution solvePrioritisedPlanning(List<? extends Agent> agents, MAPF_Instance instance, ConstraintSet initialConstraints) {
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        SortedMap<Integer, List<OnlineAgent>> agentsForTimes = OnlineSolverContainer.getAgentsByTime(agents);
        for (int timestepWithNewAgents :
                agentsForTimes.keySet()) {
            if(super.checkTimeout()) break;
            solveAtTimeStep(instance, initialConstraints, solutionsAtTimes, agentsForTimes, timestepWithNewAgents);
        }

        super.endTime = System.currentTimeMillis();
        return new OnlineSolution(solutionsAtTimes);
    }

    protected void solveAtTimeStep(MAPF_Instance instance, ConstraintSet constraints, SortedMap<Integer, Solution> solutionsAtTimes, SortedMap<Integer, List<OnlineAgent>> agentsForTimes, int timestepWithNewAgents) {
        List<OnlineAgent> newArrivals = agentsForTimes.get(timestepWithNewAgents);
        // no need to change the starting positions of old agents or modify their plans, since their plans will be avoided, not modified.
        trimOutdatedConstraints(constraints, timestepWithNewAgents); //avoid huge constraint sets in problems with many agents
        Solution subgroupSolution = super.solvePrioritisedPlanning(newArrivals, instance, constraints);
        solutionsAtTimes.put(timestepWithNewAgents, subgroupSolution);
    }

    @Override
    protected RunParameters getSubproblemParameters(MAPF_Instance subproblem, InstanceReport subproblemReport, ConstraintSet constraints) {
        RunParameters parameters = super.getSubproblemParameters(subproblem, subproblemReport, constraints);
        parameters.constraints = new OnlineConstraintSet(constraints);


        Agent agent = subproblem.agents.get(0);
        // assumes agents are online agents, and throws an exception if they aren't
        OnlineAgent onlineAgent = ((OnlineAgent) agent);
        RunParameters_SAAStar astarParameters = ((RunParameters_SAAStar)parameters);

        // set start time for when the agent arrives
        astarParameters.problemStartTime = onlineAgent.arrivalTime;
        astarParameters.heuristicFunction = new OnlineDistanceTableAStarHeuristic(((DistanceTableAStarHeuristic)astarParameters.heuristicFunction));

        // set the agent to start at its private garage
        astarParameters.agentStartLocation = ((OnlineAgent) agent).getPrivateGarage(subproblem.map.getMapCell(agent.source));

        return astarParameters;
    }

    @Override
    protected void addConstraintsAtGoal(SingleAgentPlan planForAgent, List<Constraint> constraints) {
        // do nothing
    }

    protected void trimOutdatedConstraints(ConstraintSet initialConstraints, int minTime) {
        initialConstraints.trimToTimeRange(minTime, Integer.MAX_VALUE);
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        boolean commit = commitReport;
        commitReport = false;
        super.writeMetricsToReport(solution);
        instanceReport.putStringValue(InstanceReport.StandardFields.solution, solution.readableToString());
        commitReport = commit;
        if(commitReport){
            try {
                instanceReport.commit();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

