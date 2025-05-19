package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.DataTypesAndStructures.TimeInterval;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.UnitCostsAndManhattanDistance;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPPS_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStarSIPP_Solver;
import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static BasicMAPF.Solvers.LargeNeighborhoodSearch.CollisionGraphUtils.addRandomAgentsToNeighborhood;

public class FailureBasedDestroyHeuristic implements I_DestroyHeuristic{

    @Override
    public List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map) {
        List<Agent> neighborhood = new ArrayList<>();
        Map<Agent, Set<Agent>> collisionGraph = CollisionGraphUtils.createCollisionGraph(currentSolution);

        // no collisions were found
        if (collisionGraph.isEmpty()) {
            return CollisionGraphUtils.randomAgentsGenerator(currentSolution, neighborhoodSize, rnd);
        }

        Agent firstAgent = selectRandomAgentByDegreeDistribution(rnd, collisionGraph);
        neighborhood.add(firstAgent);

        // find the path that minimizes Ag
        Solution solutionToMinimizeAg = SingleAgentAStarMinimizeVisitingTargets.findMinimumSetOfCollidingTargets(new PriorityQueue<>(new SingleAgentAStarMinimizeVisitingTargets.NodeComparator()), new UnitCostsAndManhattanDistance(firstAgent.target), firstAgent, map, currentSolution);
        if (solutionToMinimizeAg != null) {
            currentSolution.putPlan(solutionToMinimizeAg.getPlanFor(firstAgent));
        }

        Set<Agent> A_s = getAgentsVisitingSource(currentSolution, firstAgent.source);
        Set<Agent> A_g = getAgentsWhoseTargetsAreVisited(currentSolution, firstAgent);

        Set<Agent> A_s_union_A_g = new HashSet<>(A_s);
        A_s_union_A_g.addAll(A_g);

        // rule 1
        if (A_s_union_A_g.isEmpty()) {
            if (neighborhood.size() < neighborhoodSize) {
                addRandomAgentsToNeighborhood(currentSolution, neighborhood, neighborhoodSize, rnd);
            }
            return neighborhood;
        }

        // rule 2
        else if (A_s_union_A_g.size() < neighborhoodSize - 1) {
            neighborhood.addAll(A_s_union_A_g);

            List<Agent> agentsInNeighborhood = new ArrayList<>(neighborhood);
            while (!agentsInNeighborhood.isEmpty() && neighborhood.size() < neighborhoodSize) {
                Agent randomAgent = agentsInNeighborhood.remove(rnd.nextInt(agentsInNeighborhood.size()));
                ArrayList<Agent> randomAgentVisitsTargets = new ArrayList<>(getAgentsWhoseTargetsAreVisited(currentSolution, randomAgent));
                if (!randomAgentVisitsTargets.isEmpty()) {
                    Agent nextAgent = randomAgentVisitsTargets.get(rnd.nextInt(randomAgentVisitsTargets.size()));
                    if (!neighborhood.contains(nextAgent)) {
                        neighborhood.add(nextAgent);
                    }
                }
            }
            if (neighborhood.size() < neighborhoodSize) {
                addRandomAgentsToNeighborhood(currentSolution, neighborhood, neighborhoodSize, rnd);
            }
            return neighborhood;
        }

        // rule 3
        else {
            // rule 3a - Add N - 1 random agents from A_g
            if (A_s.isEmpty()) {
                List<Agent> A_g_list = new ArrayList<>(A_g);
                while (neighborhood.size() < neighborhoodSize && !A_g_list.isEmpty()) {
                    Agent randomAgent = A_g_list.remove(rnd.nextInt(A_g_list.size()));
                    if (!neighborhood.contains(randomAgent)) {
                        neighborhood.add(randomAgent);
                    }
                }
            }
            // rule 3b - Add the agent from A_s that visits s_i earliest and N - 2 random agents from A_g
            else if (A_g.size() >= neighborhoodSize - 1) {
                Agent earliestAgent = getEarliestAgentVisitingSource(A_s, firstAgent, currentSolution);
                if (earliestAgent != null && !neighborhood.contains(earliestAgent)) {
                    neighborhood.add(earliestAgent);
                }

                List<Agent> A_g_list = new ArrayList<>(A_g);
                while (neighborhood.size() < neighborhoodSize && !A_g_list.isEmpty()) {
                    Agent randomAgent = A_g_list.remove(rnd.nextInt(A_g_list.size()));
                    if (!neighborhood.contains(randomAgent)) {
                        neighborhood.add(randomAgent);
                    }
                }
            }
            // rule 3c - Add all agents in A_g and the remaining agents from A_s
            else {
                for (Agent agent : A_g) {
                    if (!neighborhood.contains(agent)) {
                        neighborhood.add(agent);
                    }
                }
                // Sort agents in A_s by the time they visit first agent's source
                List<Agent> A_s_sorted = getAgentsSortedByVisitationTime(A_s, firstAgent, currentSolution);
                for (Agent agent : A_s_sorted) {
                    if (neighborhood.size() >= neighborhoodSize) {
                        break;
                    }
                    if (!neighborhood.contains(agent)) {
                        neighborhood.add(agent);
                    }
                }
            }
        }
        if (neighborhood.size() < neighborhoodSize) {
            addRandomAgentsToNeighborhood(currentSolution, neighborhood, neighborhoodSize, rnd);
        }
        return neighborhood;
    }

    /**
     * Sorts agents in A_s by the time steps when they visit the source location of the startAgent (s_i).
     *
     * @param A_s             The set of agents visiting s_i.
     * @param startAgent      The agent whose source location (s_i) we are interested in.
     * @param currentSolution The current solution containing all agents' plans.
     * @return A list of agents in ascending order of the time steps when their paths visit s_i.
     */
    private List<Agent> getAgentsSortedByVisitationTime(Set<Agent> A_s, Agent startAgent, Solution currentSolution) {
        I_Coordinate startLocation = startAgent.source; // s_i

        // Convert A_s to a list and sort by visitation time
        List<Agent> sortedAgents = new ArrayList<>(A_s);
        sortedAgents.sort(Comparator.comparingInt(agent -> {
            SingleAgentPlan plan = currentSolution.getPlanFor(agent);
            for (Move move : plan) {
                if (move.currLocation.getCoordinate().equals(startLocation)) {
                    return move.timeNow; // Return the time step when the agent visits s_i
                }
            }
            return Integer.MAX_VALUE;
        }));
        return sortedAgents;
    }

    /**
     * Finds the agent in A_s that visits the source location of the startAgent (s_i) the earliest.
     *
     * @param A_s            The set of agents visiting s_i.
     * @param startAgent     The agent whose source location (s_i) we are interested in.
     * @param currentSolution The current solution containing all agents' plans.
     * @return The agent from A_s that visits s_i the earliest, or null if no such agent is found.
     */
    private Agent getEarliestAgentVisitingSource(Set<Agent> A_s, Agent startAgent, Solution currentSolution) {
        Agent earliestAgent = null;
        int earliestTime = Integer.MAX_VALUE;
        I_Coordinate startLocation = startAgent.source; // s_i, the source of startAgent

        // Iterate over all agents in A_s
        for (Agent agent : A_s) {
            SingleAgentPlan plan = currentSolution.getPlanFor(agent);

            // Check all moves in the agent's plan
            for (int i = 1; i < plan.size(); i++) {
                Move move = plan.moveAt(i);

                // If the move visits the start location (s_i), check the time step
                if (move.currLocation.getCoordinate().equals(startLocation)) {
                    if (i < earliestTime) { // Update if this is the earliest time step
                        earliestTime = i;
                        earliestAgent = agent;
                    }
                    break; // No need to check further moves for this agent
                }
            }
        }
        return earliestAgent;
    }

    /**
     * Finds agents whose paths visit the given location.
     * @param currentSolution The current solution containing all agents' plans.
     * @param source          The source location to check for visits.
     * @return A set of agents whose paths visit the source location.
     */
    private Set<Agent> getAgentsVisitingSource(Solution currentSolution, I_Coordinate source) {
        Set<Agent> A_s = new HashSet<>();
        for (SingleAgentPlan plan : currentSolution) {
            for (Move move : plan) {
                if (move.currLocation.getCoordinate().equals(source)) {
                    A_s.add(plan.agent);
                }
            }
        }
        return A_s;
    }

    /**
     * Finds agents whose target locations are visited by the given startAgent's path.
     * @param currentSolution The current solution containing all agents' plans.
     * @param startAgent      The agent whose path will be checked.
     * @return A set of agents whose target locations are visited by the startAgent's path.
     */
    private Set<Agent> getAgentsWhoseTargetsAreVisited(Solution currentSolution, Agent startAgent) {
        Set<Agent> A_g = new HashSet<>();
        SingleAgentPlan startAgentPlan = currentSolution.getPlanFor(startAgent);
        // Build a map from target coordinate to agent (excluding the startAgent)
        Map<I_Coordinate, Agent> targetToAgentMap = new HashMap<>();
        for (SingleAgentPlan plan : currentSolution) {
            Agent agent = plan.agent;
            if (!agent.equals(startAgent)) {
                targetToAgentMap.put(agent.target, agent);
            }
        }
        // Traverse startAgent's path and check if any coordinate matches a target
        for (Move move : startAgentPlan) {
            Agent agent = targetToAgentMap.get(move.currLocation.getCoordinate());
            if (agent != null) {
                A_g.add(agent);
            }
        }
        return A_g;
    }


    /**
     * Selects a random agent based on the degree distribution in the collision graph.
     * The selection probability of an agent is proportional to its degree (i.e., the number of neighbors in the graph).
     * A higher-degree agent has a higher likelihood of being chosen.
     * @param rnd A random number generator.
     * @return A randomly selected agent based on the degree distribution.
     */
    private Agent selectRandomAgentByDegreeDistribution(Random rnd, Map<Agent, Set<Agent>> collisionGraph) {
        // Calculate total degree sum
        int totalDegree = 0;
        for (Set<Agent> neighbors : collisionGraph.values()) {
            totalDegree += neighbors.size();
        }

        // Generate random number between 0 and totalDegree
        int randomNum = rnd.nextInt(totalDegree);

        // Select agent
        int currentSum = 0;
        for (Map.Entry<Agent, Set<Agent>> entry : collisionGraph.entrySet()) {
            currentSum += entry.getValue().size();
            if (randomNum < currentSum) {
                return entry.getKey();
            }
        }

        // Should never reach here if collisionGraph is not empty
        return collisionGraph.keySet().iterator().next();
    }

    @Override
    public void clear() {

    }
}
