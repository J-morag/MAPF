package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;

import java.util.*;

public class AgentBasedDestroyHeuristic implements I_DestroyHeuristic{

    private Set<Agent> tabuList = new HashSet<>();
    private static final int MAX_ITERATIONS = 10;

    @Override
    public List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map, SingleAgentGAndH heuristic) {
        Set<Agent> neighborhood = new HashSet<>();

        if (currentSolution.size() <= neighborhoodSize) {
            for (SingleAgentPlan plan : currentSolution) {
                neighborhood.add(plan.agent);
            }
            return new ArrayList<>(neighborhood);
        }

        // find the agent with the largest delay which is not in the tabu list
        Agent mostDelayedAgent = findMostDelayedAgent(currentSolution, heuristic);

        if (mostDelayedAgent == null) {
            return new ArrayList<>();
        }

        this.tabuList.add(mostDelayedAgent);
        if (this.tabuList.size() == currentSolution.size() || getNumberOfDelays(currentSolution.getPlanFor(mostDelayedAgent), heuristic) == 0) {
            this.tabuList.clear();
        }

        neighborhood.add(mostDelayedAgent);
        neighborhood = randomWalk(currentSolution, currentSolution.getPlanFor(mostDelayedAgent), mostDelayedAgent, rnd, true, neighborhood, neighborhoodSize, heuristic);

        int iteration = 0;
        while (neighborhood.size() < neighborhoodSize && iteration < MAX_ITERATIONS) {
            SingleAgentPlan plan = currentSolution.getPlanFor(mostDelayedAgent);
            if (plan == null || plan.size() == 0) break;
            neighborhood = randomWalk(currentSolution, plan, mostDelayedAgent, rnd, false, neighborhood, neighborhoodSize, heuristic);

            // Pick a new random agent from the neighborhood to walk from
            mostDelayedAgent = new ArrayList<>(neighborhood).get(rnd.nextInt(neighborhood.size()));
            iteration++;
        }
        return new ArrayList<>(neighborhood);
    }

    /**
     * Performs a spatiao-temporal random walk from the agent's current path in order to identify and add
     * conflicting agents to a given neighborhood set. The walk only proceeds to locations that may lead
     * to a shorter path (based on heuristic evaluation), and stops once a desired neighborhood size is reached.
     *
     * @param currentSolution   The current multi-agent solution containing all agents' plans.
     * @param plan              The plan of the agent performing the random walk.
     * @param agent             The agent for which the random walk is executed.
     * @param rnd               A Random instance for randomized neighbor selection.
     * @param init              A boolean indicates whether it is the first random walk, meaning that the walk starts at time 1.
     * @param neighborhood      A set of agents (updated in-place) that are selected as part of the neighborhood.
     * @param neighborhoodSize  The maximum number of agents allowed in the neighborhood.
     * @param heuristic         A heuristic function used to estimate the remaining cost to the goal.
     *
     * @return The updated set of agents in the neighborhood, including agents that conflict with the path
     *         generated during the random walk. If the walk finds no valid moves or the neighborhood is full,
     *         it terminates early.
     */
    private Set<Agent> randomWalk(Solution currentSolution, SingleAgentPlan plan, Agent agent, Random rnd, boolean init,
                                  Set<Agent> neighborhood, int neighborhoodSize, SingleAgentGAndH heuristic) {
        if (plan == null || plan.size() == 0) return neighborhood;

        int startTimeStep;
        if (init) {
            startTimeStep = plan.getFirstMoveTime();
        }
        else {
            int firstMoveTime = plan.getFirstMoveTime();
            int lastMoveTime = plan.getEndTime();
            startTimeStep = firstMoveTime + rnd.nextInt(lastMoveTime - firstMoveTime + 1);
        }

        I_Location location = plan.moveAt(startTimeStep).currLocation;

        for (int t = startTimeStep; t < plan.getEndTime(); t++) {
            List<I_Location> nextLocations = new ArrayList<>(location.outgoingEdges());
            nextLocations.add(location);

            while (!nextLocations.isEmpty()) {

                // find a random location
                I_Location nextLocation = nextLocations.remove(rnd.nextInt(nextLocations.size()));
                int nextLocationHeuristicValue = heuristic.getHToTargetFromLocation(agent.target, nextLocation);

                // if this step can lead to shorter path than current one, take the step and add all conflicting agents to neighborhood
                if (t + 1 + nextLocationHeuristicValue < plan.getEndTime()) {
                    Move agentMove = new Move(agent, t + 1, location, nextLocation);
                    Set<Agent> conflictingAgents = getConflictingAgents(currentSolution, agent, agentMove);
                    for (Agent addToNeighborhoodAgent : conflictingAgents) {
                        neighborhood.add(addToNeighborhoodAgent);
                        if (neighborhood.size() >= neighborhoodSize) break;
                    }
                    location = nextLocation;
                    break;
                }
            }
            if (nextLocations.isEmpty() || neighborhood.size() >= neighborhoodSize) {
                break;
            }
        }
        return neighborhood;
    }


    /**
     * Identifies agents in the current solution that would conflict with a proposed move of a given agent
     * at a specific time and location. Considers both vertex and edge conflicts.
     * @param currentSolution  The current multi-agent solution containing all agents' paths.
     * @param agent            The agent whose next move is being evaluated for conflicts.
     * @param move             The proposed move of the agent.
     * @return A set of agents whose paths would conflict with the given agent's move at the specified location and time.
     */
    private static Set<Agent> getConflictingAgents(Solution currentSolution, Agent agent, Move move) {
        Set<Agent> conflictingAgents = new HashSet<>();
        int time = move.timeNow;

        for (SingleAgentPlan otherAgentPlan : currentSolution) {
            Agent otherAgent = otherAgentPlan.agent;
            if (otherAgent.equals(agent)) continue;

            if (otherAgentPlan.getEndTime() < time && A_Conflict.lastMoveHasTargetConflictWith(otherAgentPlan.getLastMove(), move)){
                conflictingAgents.add(otherAgent);
            }
            else if (otherAgentPlan.getEndTime() >= time && A_Conflict.haveConflicts(move, otherAgentPlan.moveAt(time))) {
                conflictingAgents.add(otherAgent);
            }
        }
        return conflictingAgents;
    }


    /**
     * Finds the agent with the highest number of delays (i.e., the largest difference between
     * the actual path length and the heuristic estimate to the goal), excluding agents already
     * present in the tabu list.
     * @param currentSolution  The current multi-agent solution containing all agents' plans.
     * @param heuristic        The heuristic function used to estimate the optimal travel time.
     *
     * @return The agent with the most delays that is not in the tabu list, or null if no such agent exists.
     */
    private Agent findMostDelayedAgent(Solution currentSolution, SingleAgentGAndH heuristic) {
        Agent maxAgent = null;
        int maxDelay = -1;

        for (SingleAgentPlan plan : currentSolution) {
            Agent agent = plan.agent;
            if (this.tabuList.contains(agent)) continue;

            int delays = getNumberOfDelays(plan, heuristic);
            if (delays > maxDelay) {
                maxDelay = delays;
                maxAgent = agent;
            }
        }

        if (maxDelay == 0) {
            this.tabuList.clear();
            return null;
        }
        return maxAgent;
    }


    /**
     * Computes the number of delays for the given agent's plan, defined as the difference between
     * the actual number of moves taken and the heuristic estimate from the start location to the target.
     * @param plan       The plan of the agent whose delays are being measured.
     * @param heuristic  The heuristic function used to estimate the optimal cost to the target.
     *
     * @return The number of delays.
     */
    private int getNumberOfDelays(SingleAgentPlan plan, SingleAgentGAndH heuristic) {
        return plan.size() - 1 - heuristic.getHToTargetFromLocation(plan.agent.target, plan.getFirstMove().prevLocation);
    }

    @Override
    public void clear() {
        this.tabuList = new HashSet<>();
    }
}
