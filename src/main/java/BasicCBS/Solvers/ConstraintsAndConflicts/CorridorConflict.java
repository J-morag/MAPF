package BasicCBS.Solvers.ConstraintsAndConflicts;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.AStar.RunParameters_SAAStar;
import BasicCBS.Solvers.AStar.SingleAgentAStar_Solver;
import BasicCBS.Solvers.BreadthFirstSearch;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicCBS.Solvers.ConstraintsAndConflicts.Constraint.RangeConstraint;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;

import java.util.HashSet;

/**
 * Implements Corridor conflicts from:
 * Li, Jiaoyang, et al. "New techniques for pairwise symmetry breaking in multi-agent path finding."
 * Proceedings of the International Conference on Automated Planning and Scheduling. Vol. 30. 2020.
 * Not Thread safe!
 */
public class CorridorConflict extends A_Conflict {

    private static final SingleAgentAStar_Solver aStar = new SingleAgentAStar_Solver();

    /**
     * The end vertex of the corridor. The beginning vertex is stored in {@link #location}.
     */
    private final I_Location end;
    private final ConstraintSet constraints;
    private final MAPF_Instance instance;
    private final HashSet<I_Location> corridorVertices;
    private final int agent1MinTimeToEnd;
    private final int agent2MinTimeToBeginning;
    /**
     * Since finding the preventing constraints is expensive, once they are found they should be kept for future use.
     */
    private Constraint[] preventingConstraints = null;

    public CorridorConflict(Agent agent1, Agent agent2, int time, I_Location begin, I_Location end,
                            HashSet<I_Location> corridorVertices, ConstraintSet constraints, MAPF_Instance instance,
                            SingleAgentPlan agent1CurrentPlan, SingleAgentPlan agent2CurrentPlan) {
        super(agent1, agent2, time, begin);
        this.end = end;
        this.constraints = constraints;
        this.instance = instance;
        this.corridorVertices = corridorVertices;
        corridorVertices.remove(begin);
        corridorVertices.remove(end);
        this.agent1MinTimeToEnd = getTimeOfMoveTo(end, agent1CurrentPlan);
        this.agent2MinTimeToBeginning = getTimeOfMoveTo(begin, agent2CurrentPlan);
    }

    /**
     * @return the minimum time for getting to the location in the given plan.
     */
    private int getTimeOfMoveTo(I_Location fartherSide, SingleAgentPlan plan) {
        for (Move move : plan){
            // must compare coordinates since these locations might come from different copies of the map and thus won't be equal
            if(move.currLocation.getCoordinate().equals(fartherSide.getCoordinate())){
                return move.timeNow;
            }
        }
        return Integer.MAX_VALUE;
    }

    @Override
    public Constraint[] getPreventingConstraints() {
        if(this.preventingConstraints != null){return this.preventingConstraints;}

        // create an instance without the corridor (the beginning and end will remain)
        MAPF_Instance trimmedInstance = instance.getSubproblemWithout(corridorVertices);
        int corridorLength = this.corridorVertices.size() + 1;

        // find bypasses (also check if they exist)
        int agent1MinTimeToEndBypass = getMinTimeToCorridorFartherSideBypass(agent1, end, trimmedInstance);
        int agent2MinTimeToBeginningBypass = getMinTimeToCorridorFartherSideBypass(agent2, location, trimmedInstance);

        // derive constraints
        // see the cited article, under "Resolving Corridor Conflicts"
        return new Constraint[]{
                new RangeConstraint(agent1, 0, Math.min(getBypassCase(agent1MinTimeToEndBypass), agent2MinTimeToBeginning + corridorLength), end),
                new RangeConstraint(agent2, 0, Math.min(getBypassCase(agent2MinTimeToBeginningBypass), agent1MinTimeToEnd + corridorLength), this.location)
        };
    }

    protected int getBypassCase(int agentMinTimeWithBypass) {
        return agentMinTimeWithBypass == Integer.MAX_VALUE ? Integer.MAX_VALUE : agentMinTimeWithBypass - 1;
    }

    /**
     * Get the time for this agent to get to the farther side of the corridor, using a bypass.
     * @param agent the agent. need this to get the source location for search.
     * @param fartherSide the location that the agent will get to when it finishes traversing the corridor.
     * @param trimmedInstance the instance without the corridor.
     * @return the time for this agent to get to the farther side of the corridor, using a bypass. If impossible,
     *          return {@link Integer#MAX_VALUE}.
     */
    private int getMinTimeToCorridorFartherSideBypass(Agent agent, I_Location fartherSide, MAPF_Instance trimmedInstance) {
        // must make sure that it is reachable first. if unreachable, state-time A Star will not halt.
        if(!reachableFrom(fartherSide, trimmedInstance.map.getMapCell(agent.source))){
            return Integer.MAX_VALUE;
        }
        else{
            // get time to farther side with bypass with state-time A Star
            RunParameters_SAAStar runParameters = new RunParameters_SAAStar(constraints, new InstanceReport(),null, null);
            runParameters.targetCoor = fartherSide.getCoordinate();
            Solution solution = aStar.solve(trimmedInstance.getSubproblemFor(agent), runParameters);
            return getTimeOfMoveTo(fartherSide, solution.getPlanFor(agent));
        }
    }

    private boolean reachableFrom(I_Location fartherSide, I_Location source) {
        return BreadthFirstSearch.reachableFrom(fartherSide, source);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CorridorConflict)) return false;
        if (!super.equals(o)) return false;

        CorridorConflict that = (CorridorConflict) o;

        if (agent1MinTimeToEnd != that.agent1MinTimeToEnd) return false;
        if (agent2MinTimeToBeginning != that.agent2MinTimeToBeginning) return false;
        return end != null ? end.equals(that.end) : that.end == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + agent1MinTimeToEnd;
        result = 31 * result + agent2MinTimeToBeginning;
        return result;
    }
}
