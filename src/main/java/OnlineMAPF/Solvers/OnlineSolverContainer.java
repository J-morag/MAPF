package OnlineMAPF.Solvers;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.I_Solver;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;
import OnlineMAPF.RunParametersOnline;

import java.util.*;

/**
 * Simulates an online environment for an online solver to run in.
 *
 * New {@link OnlineAgent agents} arrive at different times, and the {@link #onlineSolver} has to accommodate them into
 * a new {@link Solution}, which it then returns.
 */
public class OnlineSolverContainer implements I_Solver {

    /**
     * An online solver to use for solving online problems.
     */
    private final I_OnlineSolver onlineSolver;
    /**
     * A custom arrival time to give {@link Agent offline agents} that this solver attempts to solve for.
     * Defaults to {@link OnlineAgent#DEFAULT_ARRIVAL_TIME}.
     */
    private static final int ARRIVAL_TIME_FOR_OFFLINE_AGENTS = OnlineAgent.DEFAULT_ARRIVAL_TIME;
    /**
     * The cost of rerouting an agent.
     */
    private int costOfReroute;

    public OnlineSolverContainer(I_OnlineSolver onlineSolver) {
        if(onlineSolver == null) {
            throw new IllegalArgumentException("null is not an acceptable value for onlineSolver");
        }
        this.onlineSolver = onlineSolver;
    }

    /**
     * Runs a simulation of an online environment. New {@link OnlineAgent agents} arrive at different times, and the
     * {@link #onlineSolver} has to accommodate them into a new {@link Solution}, which it then returns.
     * Finally, the solutions are combined into an {@link OnlineSolution}.
     * @param instance a problem instance to solve.
     * @param parameters parameters that expand upon the problem instance or change the solver's behaviour for this specific
     *                   run.
     * @return an {@link OnlineSolution}.
     */
    @Override
    public Solution solve(MAPF_Instance instance, RunParameters parameters) {
        verifyAgentsUniqueId(instance.agents);
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        SortedMap<Integer, List<OnlineAgent>> agentsForTimes = OnlineSolverContainer.getAgentsByTime(instance.agents);
        this.costOfReroute = parameters instanceof RunParametersOnline ? ((RunParametersOnline)parameters).costOfReroute : 0;
        // must initialize the solver because later we will only be giving it new agents, no other data
        onlineSolver.setEnvironment(instance, parameters);
        // feed the solver with new agents for every timestep when new agents arrive
        // agentsForTimes should be a sorted map
        for (int timestepWithNewAgents :
                agentsForTimes.keySet()) {
            List<OnlineAgent> newArrivals = agentsForTimes.get(timestepWithNewAgents);
            // get a solution for the agents to follow as of this timestep
            Solution solutionAtTime = onlineSolver.newArrivals(timestepWithNewAgents, newArrivals);
            if(solutionAtTime == null){
                wrapUp(parameters, null);
                return null; //probably as timeout
            }
            // store the solution
            solutionsAtTimes.put(timestepWithNewAgents, solutionAtTime);
        }
        OnlineSolution solution = new OnlineSolution(solutionsAtTimes);

        //clear the solver and write the report
        wrapUp(parameters, solution);

        // combine the stored solutions at times into a single online solution
        return solution;
    }

    @Override
    public String name() {
        return onlineSolver.name();
    }

    public static void verifyAgentsUniqueId(List<Agent> agents) {
        HashSet<Integer> ids = new HashSet<>(agents.size());
        for (Agent agent :
                agents) {
            if(ids.contains(agent.iD)){
                throw new IllegalArgumentException("OnlineSolverContainer: Online solvers require all agents to have unique IDs");
            }
            else ids.add(agent.iD);
        }
    }

    /**
     * Groups agent by their arrival times. Converts any non-online {@link Agent agents} into {@link OnlineAgent online agents}
     * with the default arrival time.
     * @param agents agents to group
     * @return
     */
    public static SortedMap<Integer, List<OnlineAgent>> getAgentsByTime(List<? extends Agent> agents) {
        SortedMap<Integer, List<OnlineAgent>> result = new TreeMap<>(); //tree map to preserve order of arrival times
        ArrayList<OnlineAgent> onlineAgents = offlineToOnlineAgents(agents);

        //sort by time. Should already be sorted, so just in case.
        onlineAgents.sort(Comparator.comparing(OnlineAgent::getArrivalTime));

        //group by time
        for (int i = 0; i < onlineAgents.size();) {
            int currentTime = onlineAgents.get(i).arrivalTime;
            //find range with same arrival time
            int j = i;
            while(j < onlineAgents.size() && onlineAgents.get(j).arrivalTime == currentTime){
                j++;
            }
            //so the range we found is [i,j)

            result.put(currentTime, onlineAgents.subList(i, j /*end index is non-inclusive*/ ));

            i=j; //next group
        }

        return result;
    }


    /**
     * Cast agents to online agents. If they are regular Agents, create new OnlineAgents out of them with the default arrival time.
     * @param agents
     */
    public static ArrayList<OnlineAgent> offlineToOnlineAgents(List<? extends Agent> agents) {
        ArrayList<OnlineAgent> onlineAgents = new ArrayList<>(agents.size());
        for (Agent a :
                agents) {
            onlineAgents.add(a instanceof OnlineAgent ? (OnlineAgent)a : new OnlineAgent(a, ARRIVAL_TIME_FOR_OFFLINE_AGENTS));
        }
        return onlineAgents;
    }

    private void wrapUp(RunParameters parameters, OnlineSolution solution) {
        onlineSolver.writeReportAndClearData(solution);
        parameters.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
        parameters.instanceReport.putIntegerValue(InstanceReport.StandardFields.COR, costOfReroute);

        if(solution != null){
            parameters.instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
            parameters.instanceReport.putIntegerValue(InstanceReport.StandardFields.totalReroutesCost, solution.costOfReroutes(costOfReroute));
            parameters.instanceReport.putStringValue(InstanceReport.StandardFields.solution, solution.toString());
            parameters.instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, 1);
        }
        else{
            parameters.instanceReport.putIntegerValue(InstanceReport.StandardFields.solved, 0);
        }
    }

}
