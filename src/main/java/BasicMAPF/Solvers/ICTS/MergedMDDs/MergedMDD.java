package BasicMAPF.Solvers.ICTS.MergedMDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Solvers.ICTS.MDDs.MDD;
import BasicMAPF.Solvers.ICTS.MDDs.MDDNode;
import BasicMAPF.DataTypesAndStructures.Move;
import BasicMAPF.DataTypesAndStructures.SingleAgentPlan;
import BasicMAPF.DataTypesAndStructures.Solution;

import java.util.*;

public class MergedMDD {
    private MergedMDDNode start;
    private MergedMDDNode goal;
    private static final boolean debug = false;

    /**
     * The constructor can be accessed only via the Factory (in the same package)
     */
    MergedMDD() {
    }

    public MergedMDDNode getStart() {
        return start;
    }

    public MergedMDDNode getGoal() {
        return goal;
    }

    public void setStart(MergedMDDNode start) {
        this.start = start;
    }

    public void setGoal(MergedMDDNode goal) {
        this.goal = goal;
    }

    /**
     * Finds a solution on this merged MDD. The choice of which solution is arbitrary.
     * @return a solution on this merged MDD. The choice of which solution is arbitrary.
     */
    public Solution getSolution() {
        Solution solution = new Solution();
        Map<Agent, List<Move>> agentMoves = new HashMap<>();
        for (MDDNode node : goal.getMddNodes()) {
            Agent agent = node.getAgent();
            agentMoves.put(agent, new ArrayList<>());
        }

        MergedMDDNode current = goal;
        while (!current.getParents().isEmpty()) {
            MergedMDDNode parent = current.getParents().iterator().next(); //It doesn't matter which parent it was, we take a single path.
            for (MDDNode currentAgentMDDNode : current.getMddNodes()){
                Agent agent = currentAgentMDDNode.getAgent();
                MDDNode parentAgentMDDNode = null;
                // look for the same agent parent merged MDD node
                for (MDDNode parentMDDNode : parent.getMddNodes()){
                    if (parentMDDNode.getAgent().equals(agent)){
                        parentAgentMDDNode = parentMDDNode;
                        break;
                    }
                }
                if (!currentAgentMDDNode.equals(parentAgentMDDNode)) {
                    //Only consider moves that changed something.
                    //this equal calculation uses also the "g" value,
                    //so we will distinguish between being at state s at time g and at time g + 1
                    //(staying in the same location as part of the solution)
                    Move move = new Move(agent, currentAgentMDDNode.getDepth(), parentAgentMDDNode.getLocation(),
                            currentAgentMDDNode.getLocation());
                    agentMoves.get(agent).add(move); //insert the move to the agent's moves
                }
            }
            current = parent;
        }

        for(Agent agent : agentMoves.keySet()){
            List<Move> moves = agentMoves.get(agent);
            Collections.reverse(moves);
            SingleAgentPlan plan = new SingleAgentPlan(agent, moves);
            solution.putPlan(plan);
        }

        return solution;
    }

    /**
     * Iterates through this entire merged MDD, creating a set of all (individual agent) {@link MDDNode mdd nodes} found in it.
     * @return all {@link MDDNode mdd nodes} that can be found in this merged MDD.
     */
    public Set<FatherSonMDDNodePair> toFatherSonPairs() {
        // iterate over the merged mdd in BFS order
        // we don't need a closed list since this is a DAG
        Set<FatherSonMDDNodePair> allFatherSonPairs = new HashSet<>();
        Queue<MergedMDDNode> open = new ArrayDeque<>();
        Set<MergedMDDNode> inOpen = new HashSet<>();
        // must traverse from goal, to get just the merged mdd, without nodes that don't eventually lead to the goal... I think?
        open.add(this.goal);
        inOpen.add(this.goal);
        while (!open.isEmpty()){
            MergedMDDNode currentMergedMddNode = open.remove();
            inOpen.remove(currentMergedMddNode);
            List<MDDNode> currentNodes = currentMergedMddNode.getMddNodes();
            for (MergedMDDNode parent : currentMergedMddNode.getParents()){
                // Update results set
                List<MDDNode> parentNodes = parent.getMddNodes();
                for (int i = 0; i < parentNodes.size(); i++) {
                    if (debug && !currentNodes.get(i).getAgent().equals(parentNodes.get(i).getAgent())){
                        try {
                            throw new Exception("MDDNodes inside all MergedMDDNodes of the same MergedMDD should all be ordered the same (by agent ID)");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    allFatherSonPairs.add(new FatherSonMDDNodePair(parentNodes.get(i), currentNodes.get(i)));
                }
                // update search lists
                if(!inOpen.contains(parent)){
                    open.add(parent);
                    inOpen.add(parent);
                }

            }
        }
        return allFatherSonPairs;
    }

    /**
     * Unfolds the merged MDD, trimming the individual agent MDDs that comprise it. This method doesn't modify this instance,
     * or the given map - it modifies the MDD instances referenced by the map. Only modifies those MDDs that are represented
     * in this merged MDD (so agentsMDDs can contain extra agents).
     * @param agentMdds a map of individual MDDs for agents. The MDDs in the map (not the map) may be modified by this method.
     */
    public void unfold(Map<Agent, MDD> agentMdds) {
        // get the set of all agent mdd nodes in the merged mdd
        Set<FatherSonMDDNodePair> fatherSonPairsToKeep = this.toFatherSonPairs();
        List<Agent> agentsInMergedMdd = this.getRepresentedAgents();
        FatherSonMDDNodePair keyDummy = new FatherSonMDDNodePair(null, null);
        // if an mdd node is not present in the merged mdd, remove it from the agent's mdd
        for (Agent agent : agentsInMergedMdd){
            MDD mdd = agentMdds.get(agent);
            // iterate over the mdd in BFS order
            // we don't need a closed list since this is a DAG
            Queue<MDDNode> open = new ArrayDeque<>();
            Set<MDDNode> inOpen = new HashSet<>();
            open.add(mdd.getStart());
            inOpen.add(mdd.getStart());
            while (!open.isEmpty()){
                MDDNode currentMddNode = open.remove();
                inOpen.remove(currentMddNode);
                List<MDDNode> children = currentMddNode.getNeighbors();
                // we check if the children, not the current node, can be removed, because we remove by
                // removing references to these children from their parents
                // MDDNode exposes its internal list of children, so we can modify it directly
                Iterator<MDDNode> childrenIterator = children.iterator();
                while (childrenIterator.hasNext()){
                    MDDNode child = childrenIterator.next();
                    keyDummy.set(currentMddNode, child);
                    if (!fatherSonPairsToKeep.contains(keyDummy)){
                        childrenIterator.remove();
                    }
                }
                // for those that should be kept, add them to open if they aren't already there
                for (MDDNode child : children){
                    if(!inOpen.contains(child)) {
                        open.add(child);
                        inOpen.add(child);
                    }
                }
            }
        }
    }

    /**
     * @return a {@link List} of agents that are represented (through their MDDs) in this merged MDD.
     */
    public List<Agent> getRepresentedAgents(){
        List<Agent> result = new ArrayList<>(this.start.getMddNodes().size());
        for (MDDNode mddNode : this.start.getMddNodes()){
            result.add(mddNode.getAgent());
        }
        return result;
    }
}
