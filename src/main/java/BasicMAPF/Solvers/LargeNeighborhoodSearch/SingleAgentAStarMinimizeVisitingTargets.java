package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Instances.Maps.I_Map;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;

import java.util.*;


/**
 * For LNS2 using a failure-based neighborhood selection strategy.
 * Finds a path which minimizes visiting other agents' targets.
 */
public class SingleAgentAStarMinimizeVisitingTargets {

    private static ArrayList<I_Coordinate> createTargetsTable(Solution currentSolution) {
        ArrayList<I_Coordinate> goals = new ArrayList<>();
        for (SingleAgentPlan plan : currentSolution) {
            goals.add(plan.agent.target);
        }
        return goals;
    }

    static public Solution findMinimumSetOfCollidingTargets(PriorityQueue<Node> openList, SingleAgentGAndH singleAgentGAndH, Agent agent, I_Map map, Solution currentSolution) {
        ArrayList<I_Coordinate> targetsTable = createTargetsTable(currentSolution);

        Map<I_Location, Node> seen = new HashMap<>();
        I_Location startLocation = map.getMapLocation(agent.source);
        int startHeuristic = singleAgentGAndH.getHToTargetFromLocation(agent.target, map.getMapLocation(agent.source));
        Node startNode = new Node(startLocation, 0, startHeuristic, 0, null);
        openList.add(startNode);
        seen.put(startLocation, startNode);

        while (!openList.isEmpty()) {
            Node currentNode = openList.poll();

            if (reachedGoal(currentNode, agent)) {
                return reconstructPath(currentNode, agent);
            }

            List<I_Location> neighbors = getNeighbors(currentNode.location);
            for (I_Location neighbor : neighbors) {
                int newGVal = currentNode.g_val + 1;
                int newNumOfTargets = currentNode.num_of_targets + (isTargetLocation(neighbor, targetsTable) ? 1 : 0);
                Node neighborNode = seen.get(neighbor);

                if (neighborNode == null || neighborNode.num_of_targets > newNumOfTargets ||
                        (neighborNode.num_of_targets == newNumOfTargets && neighborNode.g_val > newGVal)) {
                    int neighborHeuristic = singleAgentGAndH.getHToTargetFromLocation(agent.target, neighbor);
                    neighborNode = new Node(neighbor, newGVal, neighborHeuristic, newNumOfTargets, currentNode);
                    seen.put(neighbor, neighborNode);
                    openList.add(neighborNode);
                }
            }
        }

        return null; // No path found
    }

    private static boolean isTargetLocation(I_Location location, ArrayList<I_Coordinate> goalsTable) {
        return goalsTable.contains(location.getCoordinate());
    }

    private static boolean reachedGoal(Node currentNode, Agent agent) {
        return currentNode.location.getCoordinate().equals(agent.target);
    }

    private static Solution reconstructPath(Node node, Agent agent) {
        LinkedList<I_Location> path = new LinkedList<>();
        Solution solution = new Solution();
        SingleAgentPlan plan = new SingleAgentPlan(agent);
        int timeStep = 0;

        while (node != null) {
            path.addFirst(node.location);
            node = node.parent;
        }

        // Convert locations to moves
        I_Location prevLocation = null; // Initialize to null or the start location as appropriate.
        for (I_Location currLocation : path) {
            if (prevLocation != null) {
                timeStep++; // Increment time step each move
                Move move = new Move(agent, timeStep, prevLocation, currLocation);
                plan.addMove(move); // Assume SingleAgentPlan has a method to add moves
            }
            prevLocation = currLocation;
        }
        solution.putPlan(plan);
        return solution;
    }

    private static List<I_Location> getNeighbors(I_Location location) {
        return location.outgoingEdges();
    }

    static class Node {
        I_Location location;
        int g_val;
        int h_val;
        int num_of_targets;
        Node parent;

        public Node(I_Location location, int g_val, int h_val, int num_of_targets, Node parent) {
            this.location = location;
            this.g_val = g_val;
            this.h_val = h_val;
            this.num_of_targets = num_of_targets;
            this.parent = parent;
        }
    }

    static class NodeComparator implements Comparator<Node> {
        public int compare(Node n1, Node n2) {
            // First criterion: number of targets visited
            if (n1.num_of_targets != n2.num_of_targets) {
                return Integer.compare(n1.num_of_targets, n2.num_of_targets);
            }

            // Second criterion: f-value (g + h)
            int f1 = n1.g_val + n1.h_val;
            int f2 = n2.g_val + n2.h_val;
            if (f1 != f2) {
                return Integer.compare(f1, f2);
            }

            // Third criterion: h-value
                return Integer.compare(n1.h_val, n2.h_val);
        }
    }
}

