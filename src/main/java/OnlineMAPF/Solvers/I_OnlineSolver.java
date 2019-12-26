package OnlineMAPF.Solvers;

import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;

import java.util.List;

/**
 * An online solver solves MAPF problems where new agents can arrive over time.
 *
 * Agents disappear at their goal, and start at a private garage.
 *
 * Implementing classes will have to keep persistent dat, so when a solver is used, the following protocol should be followed:
 * 1. call {@link #setEnvironment(MAPF_Instance, RunParameters)}.
 * 2. while(new agents arriving) call {@link #newArrivals(int, List)}.
 * 3. call {@link #writeReportAndClearData()} ()}.
 * The solver can then be reused.
 */
public interface I_OnlineSolver {

    /**
     * Initialises the solver. Mostly just keeps the map. {@link MAPF_Instance#agents} should be ignored, since agents
     * are expected to arrive over time.
     * @param instance an instance to solve on. The agent data contained in the instance should be ignored (spoilers).
     * @param parameters {@link RunParameters} for this instance.
     */
    void setEnvironment(MAPF_Instance instance, RunParameters parameters);

    /**
     * New {@link OnlineAgent agents}arrive. The solver moves the existing agents to the positions that they had moved
     * to in the meantime (as they've been following a previously existing solution). The solver accommodates the new
     * agents into a new solution that starts at the given time.
     * @param time the time when new agents arrive
     * @param agents new {@link OnlineAgent}s arriving at the given time.
     * @return a solution that accommodates both the existing agents and the new agents.
     */
    Solution newArrivals(int time, List<? extends OnlineAgent> agents);

    /**
     * Clears any persistent data that the solver was keeping, such as the {@link BasicCBS.Instances.Maps.I_Map map},
     * or the previous {@link Solution}.
     */
    void writeReportAndClearData(OnlineSolution solution);
}
