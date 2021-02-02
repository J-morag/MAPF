package BasicCBS.Solvers.ICTS.HighLevel;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.A_Solver;
import BasicCBS.Solvers.ICTS.MDDs.*;
import BasicCBS.Solvers.ICTS.MergedMDDs.*;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.InstanceReport;

import java.util.*;

public class ICTS_Solver extends A_Solver {
    private Set<ICT_Node> contentOfOpen;
    private Queue<ICT_Node> openList;
    private Set<ICT_Node> closedList;
    private ICT_NodeComparator comparator;
    protected I_MDDSearcherFactory searcherFactory;
    private I_MergedMDDSolver mergedMDDSolver;
    private boolean usePairWiseGoalTest;
    protected MDDManager mddManager;
    protected DistanceTableAStarHeuristicICTS heuristicICTS;
    protected MAPF_Instance instance;

    private int expandedHighLevelNodesNum;
    private int generatedHighLevelNodesNum;

    public ICTS_Solver(ICT_NodeComparator comparator, I_MDDSearcherFactory searcherFactory, I_MergedMDDSolver mergedMDDSolver, Boolean usePairWiseGoalTest) {
        this.comparator = Objects.requireNonNullElse(comparator, new ICT_NodeSumOfCostsComparator());
        this.searcherFactory = Objects.requireNonNullElse(searcherFactory, new AStarFactory());
        this.mergedMDDSolver = Objects.requireNonNullElse(mergedMDDSolver, new IndependenceDetection_MergedMDDSolver(new DFS_MergedMDDSpaceSolver()));
        this.usePairWiseGoalTest = Objects.requireNonNullElse(usePairWiseGoalTest, true);
    }

    public ICTS_Solver(){
        this(null, null, null, null);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        openList = createOpenList();
        contentOfOpen = new HashSet<>();
        closedList = createClosedList();
        expandedHighLevelNodesNum = 0;
        generatedHighLevelNodesNum = 0;
        getHeuristic(instance);
        this.mddManager = new MDDManager(searcherFactory, this, heuristicICTS);
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

        boolean checkPairWiseMDDs = usePairWiseGoalTest && instance.agents.size() > 2;
        while (!openList.isEmpty() && !checkTimeout()) {
            ICT_Node current = pollFromOpen();
            expandedHighLevelNodesNum++;
            boolean pairFlag = true;
            if (checkPairWiseMDDs) {
                // false = no solution for these two agents at these costs
                pairFlag = pairWiseGoalTest(instance, current);
            }
            // if pairwise is turned off, or we didn't manage to prune in pairwise (no cardinal pairwise conflict)
            if (!checkPairWiseMDDs || pairFlag) {
                Map<Agent, MDD> mdds = new HashMap<>();
                for (Agent agent : instance.agents) {
//                    ICTSAgent agent = (ICTSAgent) a;
                    MDD mdd = getMDD(agent, current.getCost(agent));
                    if(mdd == null)
                        return null;
                    mdds.put(agent, mdd);
                }
                Solution mergedMDDSolution = mergedMDDSolver.findJointSolution(mdds, this);
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

    protected MDD getMDD(Agent agent, int cost) {
        return mddManager.getMDD(getSource(agent), getTarget(agent), agent, cost);
    }

    public boolean reachedTimeout(){
        return checkTimeout();
    }

    private void updateExpandedAndGeneratedNum() {
        super.totalLowLevelStatesExpanded = mddManager.getExpandedNodesNum();
        super.totalLowLevelStatesGenerated = mddManager.getGeneratedNodesNum();
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        super.instanceReport.putIntegerValue(InstanceReport.StandardFields.generatedNodes, this.generatedHighLevelNodesNum);
        super.instanceReport.putIntegerValue(InstanceReport.StandardFields.expandedNodes, this.expandedHighLevelNodesNum);
        super.instanceReport.putStringValue(InstanceReport.StandardFields.solver, name());
        if(solution != null){
            super.instanceReport.putStringValue(InstanceReport.StandardFields.solutionCostFunction, "SOC");
            super.instanceReport.putIntegerValue(InstanceReport.StandardFields.solutionCost, solution.sumIndividualCosts());
        }
    }

    @Override
    public String name(){
        String pairWiseString = "_";
        if(usePairWiseGoalTest)
            pairWiseString += "pairwise";
        else
            pairWiseString += "no_pairwise";
        return "ICTS_Solver" + pairWiseString;
    }

    private boolean pairWiseGoalTest(MAPF_Instance instance, ICT_Node current) {
        for (int i = 0; i < instance.agents.size(); i++) {
            Agent agentI = instance.agents.get(i);
            for (int j = i + 1; j < instance.agents.size(); j++) {
                Agent agentJ = instance.agents.get(j);

//                ICTSAgent agentI = (ICTSAgent) aI;
//                ICTSAgent agentJ = (ICTSAgent) aJ;
                MDD mddI = getMDD(agentI, current.getCost(agentI));
                if(mddI == null)
                    return false;
                MDD mddJ = getMDD(agentJ, current.getCost(agentJ));
                if(mddJ == null)
                    return false;
                Map<Agent, MDD> pairwiseMap = new HashMap<>();
                pairwiseMap.put(agentI, mddI);
                pairwiseMap.put(agentJ, mddJ);
                Solution pairwiseMergedMDDSolution = mergedMDDSolver.findJointSolution(pairwiseMap, this);
                if (pairwiseMergedMDDSolution == null) //couldn't find solution between 2 agents
                    return false;
            }
        }
        return true;
    }

    private void expand(ICT_Node current) {
        List<ICT_Node> children = current.getChildren();
        for (ICT_Node child : children) {
            addToOpen(child);
        }
        addToClosed(current);
    }

    private void addToClosed(ICT_Node current) {
        closedList.add(current);
    }

    private boolean initRoot(MAPF_Instance instance) {
        Map<Agent, Integer> startCosts = new HashMap<>();
        for (Agent agent : instance.agents) {

            Integer depth = Math.round(heuristicICTS.getHForAgentAndCurrentLocation(agent, getSource(agent)));
            if (depth == null) {
                //The single agent path does not exist
                try {
                    throw new Exception("The single agent plan for agent " + agent.iD + " does not exist!");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }
            startCosts.put(agent, depth);
        }
        ICT_Node startNode = new ICT_Node(startCosts);
        addToOpen(startNode);
        return true;
    }

    protected void getHeuristic(MAPF_Instance instance) {
        heuristicICTS = new DistanceTableAStarHeuristicICTS(instance.agents, instance.map);
    }

    protected I_Location getSource(Agent agent){
        return instance.map.getMapCell(agent.source);
    }

    protected I_Location getTarget(Agent agent){
        return instance.map.getMapCell(agent.target);
    }

    private ICT_Node pollFromOpen() {
        ICT_Node current = openList.poll();
        contentOfOpen.remove(current);
        return current;
    }

    private void addToOpen(ICT_Node node) {
        if (!contentOfOpen.contains(node) && !closedList.contains(node)) {
            generatedHighLevelNodesNum++;
            openList.add(node);
            contentOfOpen.add(node);
        }
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
        this.contentOfOpen = null;
        this.openList = null;
        this.closedList = null;
        this.mddManager = null;
        this.instance = null;
    }
}
