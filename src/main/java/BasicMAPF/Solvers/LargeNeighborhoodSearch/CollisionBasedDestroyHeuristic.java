package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;

import java.util.*;

import static BasicMAPF.Solvers.LargeNeighborhoodSearch.CollisionGraphUtils.addRandomAgentsToNeighborhood;

public class CollisionBasedDestroyHeuristic implements I_DestroyHeuristic{

    private static final int MAX_NUM_ITERATIONS = 50;
    private static final double PROBABILITY_TO_CHOOSE_AGENT_IN_RANDOM_WALK = 0.7;

    @Override
    public List<Agent> selectNeighborhood(Solution currentSolution, int neighborhoodSize, Random rnd, I_Map map, int maxTimeToConsider) {
        List<Agent> neighborhood = new ArrayList<>();
        Map<Agent, Set<Agent>> collisionGraph = CollisionGraphUtils.createCollisionGraph(currentSolution, maxTimeToConsider);

        // no collisions were found
        if (collisionGraph.isEmpty()) {
            return CollisionGraphUtils.randomAgentsGenerator(currentSolution, neighborhoodSize, rnd);
        }

        // Select a random starting agent
        List<Agent> agentsInConflict = new ArrayList<>(collisionGraph.keySet());
        Agent startAgent = agentsInConflict.get(rnd.nextInt(agentsInConflict.size()));
        // find the largest connected component in the collision graph which contains startAgent
        Set<Agent> connectedComponent = findConnectedComponent(collisionGraph, startAgent);

        if (connectedComponent.size() <= neighborhoodSize) {
            neighborhood.addAll(connectedComponent);
            while (neighborhood.size() < neighborhoodSize) {
                Agent selectedRandomAgent = neighborhood.get(rnd.nextInt(neighborhood.size()));
                Agent otherAgent = getAgentUsingRandomWalkOnTheMap(selectedRandomAgent, neighborhood, rnd, currentSolution, MAX_NUM_ITERATIONS);
                if (otherAgent != null && !neighborhood.contains(otherAgent)) {
                    neighborhood.add(otherAgent);
                }
                if (otherAgent == null) {
                    addRandomAgentsToNeighborhood(currentSolution, neighborhood, neighborhoodSize, rnd);
                }
            }

        } else {
            neighborhood.add(startAgent);
            addAgentUsingRandomWalkOnCollisionGraph(startAgent, collisionGraph, neighborhoodSize, neighborhood, rnd);
            if (neighborhood.size() < neighborhoodSize) {
                addRandomAgentsToNeighborhood(currentSolution, neighborhood, neighborhoodSize, rnd);
            }
        }
        return neighborhood;
    }


    /**
     * Performs a random walk on the map from a random location in the map.
     * The walk starts at a random position on the path and stops when it finds another agent that conflicts with the current agent at a specific time.
     * @param startAgent The agent to start the random walk from.
     * @param neighborhood The list of agents already in the neighborhood (to avoid duplicates).
     * @param rnd The Random instance used for randomness in the walk.
     * @param currentSolution The current solution containing agents' paths.
     * @return An agent that conflicts with the startAgent and is not in the neighborhood, or null if no such agent is found during the walk.
     */

    public Agent getAgentUsingRandomWalkOnTheMap(Agent startAgent, List<Agent> neighborhood, Random rnd, Solution currentSolution, int maxSteps) {
        SingleAgentPlan plan = currentSolution.getPlanFor(startAgent);

        // Choose a random starting time within the plan
        int time = rnd.nextInt(plan.size()) + 1;
        Move randomMove = plan.moveAt(time);
        I_Location currentPosition = randomMove.currLocation;

        int steps = 0;
        while (steps < maxSteps) {
            List<I_Location> possibleMoves = currentPosition.outgoingEdges();
            if (possibleMoves.isEmpty()) return null; // No possible moves

            // Pick a random move from available neighbors
            currentPosition = possibleMoves.get(rnd.nextInt(possibleMoves.size()));

            // Check for conflicts at this new position
            for (SingleAgentPlan otherAgentPlan : currentSolution) {
                if (otherAgentPlan.moveAt(time) == null) {
                    continue;
                }

                Agent otherAgent = otherAgentPlan.agent;
                if (otherAgent.equals(startAgent) || neighborhood.contains(otherAgent)) {
                    continue; // Ignore the same agent or existing neighborhood
                }

                Move otherAgentMove = otherAgentPlan.moveAt(time);
                if (otherAgentMove.currLocation.equals(currentPosition)) {
                    if (rnd.nextDouble() < PROBABILITY_TO_CHOOSE_AGENT_IN_RANDOM_WALK) {
                        return otherAgent;
                    }
                }
            }
            time++;
            steps++;
        }
        return null; // No valid agent found within maxSteps
    }


    /**
     * Performs a random walk on the collision graph starting from a given agent.
     * The walk stops when it finds an agent that is not already part of the neighborhood.
     * @param startAgent   The agent to start the random walk from.
     * @param neighborhood The list of agents already in the neighborhood (to avoid duplicates).
     * @param rnd          The Random instance used for randomness in the walk.
     */

    private void addAgentUsingRandomWalkOnCollisionGraph(Agent startAgent, Map<Agent, Set<Agent>> collisionGraph, int neighborhoodSize, List<Agent> neighborhood, Random rnd) {
        Agent currentAgent = startAgent;

        while (neighborhood.size() < neighborhoodSize) {
            Set<Agent> neighbors = collisionGraph.getOrDefault(currentAgent, Collections.emptySet());

            if (neighbors.isEmpty()) {
                break; // No neighbors to walk to
            }

            // Pick a random neighbor
            List<Agent> neighborList = new ArrayList<>(neighbors);
            Agent nextAgent = neighborList.get(rnd.nextInt(neighborList.size()));

            if (!neighborhood.contains(nextAgent)) {
                neighborhood.add(nextAgent);
            }

            // Continue walking from the newly selected neighbor
            currentAgent = nextAgent;
        }

    }

    /**
     * Find the connected component in collisionGraph that contains startAgent.
     * @param startAgent - the agent to start the search from.
     * @return set of agents which is the largest connected component in collisionGraph.
     */
    private Set<Agent> findConnectedComponent(Map<Agent, Set<Agent>> collisionGraph, Agent startAgent) {
        Set<Agent> visited = new HashSet<>();
        Queue<Agent> queue = new LinkedList<>();
        queue.add(startAgent);
        visited.add(startAgent);

        // perform BFS to find the largest connected component in
        while (!queue.isEmpty()) {
            Agent current = queue.poll();
            for (Agent neighbor : collisionGraph.getOrDefault(current, Collections.emptySet())) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }
        return visited;
    }

    @Override
    public void clear() {

    }
}
