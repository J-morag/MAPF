package BasicMAPF.Solvers.ICTS.HighLevel;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.DataTypesAndStructures.MDDs.AStarFactory;
import BasicMAPF.DataTypesAndStructures.MDDs.I_MDDSearcherFactory;
import BasicMAPF.DataTypesAndStructures.MDDs.MDD;
import BasicMAPF.DataTypesAndStructures.MDDs.MDDManager;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.DistanceTableSingleAgentHeuristic;
import BasicMAPF.Solvers.AStar.CostsAndHeuristics.SingleAgentGAndH;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.ICTS.MergedMDDs.*;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import Environment.Metrics.InstanceReport;
import com.google.common.base.Predicate;
import com.google.common.collect.Maps;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.*;

public class ICTS_Solver extends A_Solver {
    private Set<ICT_Node> contentOfOpen;
    private Queue<ICT_Node> openList;
    private ICT_NodeComparator comparator;
    protected I_MDDSearcherFactory searcherFactory;
    private I_MergedMDDSolver mergedMDDSolver;
    private PruningStrategy pruningStrategy;
    protected MDDManager mddManager;
    protected SingleAgentGAndH heuristic;
    protected MAPF_Instance instance;
    private final I_MergedMDDCreator mergedMDDCreator;
    /**
     * If set to true, will copy each individual agent's mdd before attempting enhanced pruning. Otherwise, will generate
     * them from scratch.
     */
    public boolean copyMddsBeforeEnhancedPruning = true;
    private AgentFilterPredicate agentFilterPredicate = new AgentFilterPredicate();

    public ICTS_Solver(ICT_NodeComparator comparator, I_MDDSearcherFactory searcherFactory, I_MergedMDDSolver mergedMDDSolver,
                       PruningStrategy pruningStrategy, I_MergedMDDCreator mergedMDDCreator) {
        this.comparator = Objects.requireNonNullElseGet(comparator, ICT_NodeSumOfCostsComparator::new);
        this.searcherFactory = Objects.requireNonNullElseGet(searcherFactory, AStarFactory::new);
        this.mergedMDDSolver = Objects.requireNonNullElseGet(mergedMDDSolver, () -> new IndependenceDetection_MergedMDDSolver(new DFS_MergedMDDSpaceSolver()));
        this.pruningStrategy = Objects.requireNonNullElse(pruningStrategy, PruningStrategy.S2P);
        this.mergedMDDCreator = Objects.requireNonNullElseGet(mergedMDDCreator, BreadthFirstSearch_MergedMDDCreator::new);
        super.name = "ICTS_Solver_" + this.pruningStrategy;
    }

    public ICTS_Solver(){
        this(null, null, null, null, null);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        openList = createOpenList();
        contentOfOpen = new HashSet<>();
        // We don't need both contents of open and closed list, since when we expand we always get nodes not in closed.
        // We only need to make sure we don't pop a closed node from open, and contents of open makes sure of that.
//        closedList = createClosedList();
        heuristic = Objects.requireNonNullElseGet(parameters.singleAgentGAndH, () -> new DistanceTableSingleAgentHeuristic(instance.agents, instance.map));
        this.mddManager = new MDDManager(searcherFactory, super.getTimeout(), heuristic);
        this.instance = instance;
    }

    protected Queue<ICT_Node> createOpenList() {
        return new PriorityQueue<>(comparator);
    }

    protected Set<ICT_Node> createClosedList() {
        return new HashSet<>();
    }

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        if (!initRoot(instance))
            return null;

        while (!openList.isEmpty() && !checkTimeout()) {
            ICT_Node current = pollFromOpen();
            expandedNodes++;
            // we want copies of agent MDDs if we use enhanced pruning, because we would like to trim them as part of the pruning
            Map<Agent, MDD> agentMdds = getMDDs(instance, current);
            if (agentMdds == null) {
                checkTimeout();
                return null; // if there is some agent that can't be solved at this cost (shouldn't happen) or there was a timeout
            }
            boolean pruningFailed = true;
            if ((this.pruningStrategy == PruningStrategy.S2P || this.pruningStrategy == PruningStrategy.E2P) && instance.agents.size() > 2) {
                // false := no joint solution for some pair agents at these costs (which is good, because we can prune!)
                pruningFailed = pairwiseGoalTest(agentMdds, this.pruningStrategy == PruningStrategy.E2P);
            }
            else if ((this.pruningStrategy == PruningStrategy.S3P || this.pruningStrategy == PruningStrategy.E3P) && instance.agents.size() > 3) {
                // false := no joint solution for some triplet of agents at these costs (which is good, because we can prune!)
                pruningFailed = tripletWiseGoalTest(agentMdds, this.pruningStrategy == PruningStrategy.E3P);
            }
            // if pruning is turned off, or we didn't manage to prune (no cardinal pairwise/triplet-wise conflict at current costs)
            if (this.pruningStrategy == PruningStrategy.NO_PRUNING || pruningFailed) {
                Solution mergedMDDSolution = getMergedMddSolution(agentMdds);
                if (mergedMDDSolution != null) {
                    //We found the goal!
                    updateExpandedAndGeneratedNum();
//                    openList.add(current);
//                    contentOfOpen.add(current);
                    return mergedMDDSolution;
                }
            }
            if(!checkTimeout())
                expand(current);
        }

        //Got here because of timeout
        updateExpandedAndGeneratedNum();
        return null;
    }

    private boolean tripletWiseGoalTest(Map<Agent, MDD> agentMdds, boolean unfoldMergedMdds) {
        for (int i = 0; i < instance.agents.size(); i++) {
            Agent agentI = instance.agents.get(i);
            for (int j = i + 1; j < instance.agents.size(); j++) {
                Agent agentJ = instance.agents.get(j);
                for (int k = j + 1; k < instance.agents.size(); k++) {
                    Agent agentK = instance.agents.get(k);
                    this.agentFilterPredicate.setAgents(agentI, agentJ, agentK);
                    if (pruningGoalTest(agentMdds, unfoldMergedMdds)) return false;
                }
            }
        }
        return true;
    }

    private boolean pairwiseGoalTest(Map<Agent, MDD> agentMdds, boolean unfoldMergedMdds) {
        for (int i = 0; i < instance.agents.size(); i++) {
            Agent agentI = instance.agents.get(i);
            for (int j = i + 1; j < instance.agents.size(); j++) {
                Agent agentJ = instance.agents.get(j);
                this.agentFilterPredicate.setAgents(agentI, agentJ);
                if (pruningGoalTest(agentMdds, unfoldMergedMdds)) return false;
            }
        }
        return true;
    }

    private boolean pruningGoalTest(Map<Agent, MDD> agentMdds, boolean unfoldMergedMdds) {
        Map<Agent, MDD> filteredMap = Maps.filterKeys(agentMdds, this.agentFilterPredicate);
        if (unfoldMergedMdds){ // enhanced version, so explicitly build merged MDD, and try to unfold in
            MergedMDD mergedMDD = getMergedMDD(filteredMap);
            if (mergedMDD == null) //couldn't find solution between 2 agents
                return true;
            else{
                // unfold the merged mdd, because it may give us trimmed MDDs for the individual agents
                mergedMDD.unfold(agentMdds);
            }
        }
        else{ // simple version, so just try to find a joint solution
            Solution mergedSolution = getMergedMddSolution(filteredMap);
            //couldn't find solution between 2 agents
            return mergedSolution == null;
        }
        return false;
    }

    private Solution getMergedMddSolution(Map<Agent, MDD> agentMdds) {
        Solution solution = mergedMDDSolver.findJointSolution(agentMdds, getTimeout());
        super.totalLowLevelNodesExpanded = mergedMDDSolver.getExpandedLowLevelNodesNum();
        super.totalLowLevelNodesGenerated = mergedMDDSolver.getGeneratedLowLevelNodesNum();
        return solution;
    }

    private Map<Agent, MDD> getMDDs(MAPF_Instance instance, ICT_Node current) {
        boolean isEnchancedPruning = this.pruningStrategy == PruningStrategy.E3P || this.pruningStrategy == PruningStrategy.E2P;
        Map<Agent, MDD> mddCopies = new HashMap<>(instance.agents.size());
        for (Agent a : instance.agents){
            // get a fresh MDD if we are using enhanced pruning, and we won'y be copying it
            MDD agentMDD = isEnchancedPruning && !this.copyMddsBeforeEnhancedPruning ?
                    this.getMDD(a, current.getCost(a), true) : this.getMDD(a, current.getCost(a), false);
            if (agentMDD == null)
                return null;
            // copy the MDD if we are using enhanced pruning, and we are reusing existing MDDs from the mdd manager
            mddCopies.put(a, isEnchancedPruning && this.copyMddsBeforeEnhancedPruning ? new MDD(agentMDD) : agentMDD);
        }
        return mddCopies;
    }

    private MergedMDD getMergedMDD(Map<Agent, MDD> filteredMap) {
        MergedMDD mergedMDD = mergedMDDCreator.createMergedMDD(filteredMap, getTimeout());
        super.totalLowLevelNodesExpanded += mergedMDDCreator.getExpandedLowLevelNodesNum();
        super.totalLowLevelNodesGenerated += mergedMDDCreator.getGeneratedLowLevelNodesNum();
        return mergedMDD;
    }

    protected MDD getMDD(Agent agent, int cost, boolean forceCreateNewMDD) {
        return forceCreateNewMDD ? mddManager.getMDDNoReuse(getSource(agent), getTarget(agent), agent, cost) :
                mddManager.getMDD(getSource(agent), getTarget(agent), agent, cost);
    }

    public boolean reachedTimeout(){
        return checkTimeout();
    }

    private void updateExpandedAndGeneratedNum() {
        super.totalLowLevelNodesExpanded = mddManager.getExpandedNodesNum();
        super.totalLowLevelNodesGenerated = mddManager.getGeneratedNodesNum();
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        super.totalLowLevelNodesExpanded += mddManager.getExpandedNodesNum();
        super.totalLowLevelNodesGenerated += mddManager.getGeneratedNodesNum();
        if(solution != null){
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
            super.instanceReport.putFloatValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
        }
    }

    private void expand(ICT_Node current) {
        List<ICT_Node> children = current.getChildren();
        for (ICT_Node child : children) {
            addToOpen(child);
        }
    }

    private boolean initRoot(MAPF_Instance instance) {
        Map<Agent, Integer> startCosts = new HashMap<>();
        for (Agent agent : instance.agents) {

            Integer depth = Math.round(heuristic.getHToTargetFromLocation(agent.target, getSource(agent)));
            startCosts.put(agent, depth);
        }
        ICT_Node startNode = new ICT_Node(startCosts);
        addToOpen(startNode);
        return true;
    }

    protected I_Location getSource(Agent agent){
        return instance.map.getMapLocation(agent.source);
    }

    protected I_Location getTarget(Agent agent){
        return instance.map.getMapLocation(agent.target);
    }

    private ICT_Node pollFromOpen() {
        ICT_Node current = openList.poll();
        contentOfOpen.remove(current);
        return current;
    }

    private void addToOpen(ICT_Node node) {
        if (!contentOfOpen.contains(node)) {
            generatedNodes++;
            openList.add(node);
            contentOfOpen.add(node);
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.contentOfOpen = null;
        this.openList = null;
        this.mddManager = null;
        this.instance = null;
    }

    public enum PruningStrategy{
        /**
         * no pruning - always do full goal test
         */
        NO_PRUNING,
        /**
         * simple pairwise pruning.
         */
        S2P,
        /**
         * simple triplet-wise pruning.
         */
        S3P,
        /**
         * enhanced pairwise pruning.
         */
        E2P,
        /**
         * enhanced triplet-wise pruning.
         */
        E3P
    }

    protected class AgentFilterPredicate implements Predicate<Agent>{
        Agent agent1;
        Agent agent2;
        Agent agent3;
        public void setAgents(Agent a1, Agent a2, Agent a3){
            this.agent1 = a1;
            this.agent2 = a2;
            this.agent3 = a3;
        }
        public void setAgents(Agent a1, Agent a2) {
            this.agent1 = a1;
            this.agent2 = a2;
            this.agent3 = null;
        }

        @Override
        public boolean apply(@Nullable Agent agent) {
            return agent != null && (agent.equals(agent1) || agent.equals(agent2) || agent3 == null || agent.equals(agent3));
        }
    }
}
