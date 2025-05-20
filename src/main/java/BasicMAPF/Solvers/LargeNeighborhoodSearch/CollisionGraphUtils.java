package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;

import java.util.*;

public class CollisionGraphUtils {
    public static Map<Agent, Set<Agent>> createCollisionGraph(Solution currentSolution, int maxTimeToConsider) {
        Map<Agent, Set<Agent>> collisionGraph = new HashMap<>();
        for (SingleAgentPlan plan1 : currentSolution) {
            for (SingleAgentPlan plan2 : currentSolution) {
                if (plan1.agent == plan2.agent) continue;

                // TODO - check how to get share goals and share sources
                A_Conflict conflict = plan1.firstConflict(plan2, false, false, maxTimeToConsider);
                if(conflict != null) {
                    collisionGraph.computeIfAbsent(plan1.agent, k -> new HashSet<>()).add(plan2.agent);
                    collisionGraph.computeIfAbsent(plan2.agent, k -> new HashSet<>()).add(plan1.agent);
                }
            }
        }
        return collisionGraph;
    }

    public static List<Agent> randomAgentsGenerator(Solution currentSolution, int neighborhoodSize, Random rnd) {
        List<Agent> allAgents = new ArrayList<>();
        List<Agent> randomNeighborhood = new ArrayList<>();
        for (SingleAgentPlan plan : currentSolution) {
            allAgents.add(plan.agent);
        }
        Collections.shuffle(allAgents, rnd);
        for (int i = 0; i < Math.min(neighborhoodSize, allAgents.size()); i++) {
            randomNeighborhood.add(allAgents.get(i));
        }
        return randomNeighborhood;
    }

    public static void addRandomAgentsToNeighborhood(Solution currentSolution, List<Agent> neighborhood, int neighborhoodSize, Random rnd) {
        // Get a list of agents not already in the neighborhood
        List<Agent> remainingAgents = new ArrayList<>();
        for (SingleAgentPlan plan : currentSolution) {
            Agent agent = plan.agent;
            if (!neighborhood.contains(agent)) {
                remainingAgents.add(agent);
            }
        }

        // Add random agents from the remaining agents to fill the neighborhood
        while (neighborhood.size() < neighborhoodSize && !remainingAgents.isEmpty()) {
            int index = rnd.nextInt(remainingAgents.size());
            int lastIndex = remainingAgents.size() - 1;

            // Swap the randomly chosen agent with the last agent in the list
            Collections.swap(remainingAgents, index, lastIndex);

            // Remove the last agent in O(1)
            Agent randomAgent = remainingAgents.remove(lastIndex);

            // Add that agent to the neighborhood
            neighborhood.add(randomAgent);
        }
    }
}
