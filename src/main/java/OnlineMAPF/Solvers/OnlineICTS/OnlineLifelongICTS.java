package OnlineMAPF.Solvers.OnlineICTS;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.MAPF_Instance;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Solvers.ICTS.HighLevel.ICT_Node;
import BasicCBS.Solvers.ICTS.HighLevel.ICT_NodeComparator;
import BasicCBS.Solvers.ICTS.MDDs.I_MDDSearcherFactory;
import BasicCBS.Solvers.ICTS.MDDs.MDD;
import BasicCBS.Solvers.ICTS.MDDs.MDDManager;
import BasicCBS.Solvers.ICTS.MDDs.MDDManager.SourceTargetAgent;
import BasicCBS.Solvers.ICTS.MDDs.MDDNode;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDCreator;
import BasicCBS.Solvers.ICTS.MergedMDDs.I_MergedMDDSolver;
import BasicCBS.Solvers.Move;
import BasicCBS.Solvers.RunParameters;
import BasicCBS.Solvers.SingleAgentPlan;
import BasicCBS.Solvers.Solution;
import Environment.Metrics.S_Metrics;
import OnlineMAPF.OnlineAgent;
import OnlineMAPF.OnlineSolution;
import OnlineMAPF.RunParametersOnline;
import OnlineMAPF.Solvers.A_OnlineSolver;
import OnlineMAPF.Solvers.OnlineSolverContainer;

import java.util.*;

public class OnlineLifelongICTS extends OnlineCompatibleICTS {

    public String name = "LifelongICTS_" + super.pruningStrategy.toString();

    protected Solution latestSolution;
    public boolean ignoreCOR = false;
    protected int costOfReroute = 0;
    protected MAPF_Instance baseInstance;
    /**
     * If set to true, will cut existing MDDs when time progresses, instead of getting them from scratch. Also throws
     * away old MDDs instead of letting them accumulate in {@link #mddManager}.
     */
    public boolean updateMDDsWhenTimeProgresses = true;
    public boolean keepOnlyRelevantUpdatedMDDs = true;

    public OnlineLifelongICTS(ICT_NodeComparator comparator, I_MDDSearcherFactory searcherFactory, I_MergedMDDSolver mergedMDDSolver,
                              PruningStrategy pruningStrategy, I_MergedMDDCreator mergedMDDCreator, Map<Agent, I_Location> customStartLocations,
                              int customStartTime, Boolean updateMDDsWhenTimeProgresses) {
        super(comparator, searcherFactory, mergedMDDSolver, pruningStrategy, mergedMDDCreator, customStartLocations, customStartTime);
        this.updateMDDsWhenTimeProgresses = updateMDDsWhenTimeProgresses;
    }

    public OnlineLifelongICTS(Map<Agent, I_Location> customStartLocations, int customStartTime) {
        super(customStartLocations, customStartTime > 0 ? customStartTime : -1);
    }

    public OnlineLifelongICTS(){
        this(null, -1);
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        this.latestSolution = new Solution();
        this.baseInstance = instance;
        this.instanceReport = parameters.instanceReport != null ? parameters.instanceReport : S_Metrics.newInstanceReport();
        if(parameters instanceof RunParametersOnline && !this.ignoreCOR){
            this.costOfReroute = ((RunParametersOnline)parameters).costOfReroute;
            if (this.costOfReroute < 0) throw new IllegalArgumentException("cost of reroute must be non negative");
        }
        else{
            this.costOfReroute = 0;
        }
    }

    /**
     * Code mostly taken from {@link OnlineMAPF.Solvers.OnlineSolverContainer}. In this class, this method runs the online
     * simulation (iterating over time, etc.), whereas in super, it runs the main ICTS loop.
     * @param instance {@inheritDoc}
     * @param parameters {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        OnlineSolverContainer.verifyAgentsUniqueId(instance.agents);
        SortedMap<Integer, Solution> solutionsAtTimes = new TreeMap<>();
        SortedMap<Integer, List<OnlineAgent>> agentsForTimes = OnlineSolverContainer.getAgentsByTime(instance.agents);
        this.costOfReroute = parameters instanceof RunParametersOnline ? ((RunParametersOnline)parameters).costOfReroute : 0;
        // feed the solver with new agents for every timestep when new agents arrive
        // agentsForTimes should be a sorted map
        for (int timestepWithNewAgents :
                agentsForTimes.keySet()) {
            List<OnlineAgent> newArrivals = agentsForTimes.get(timestepWithNewAgents);
            // get a solution for the agents to follow as of this timestep
            Solution solutionAtTime = newArrivals(timestepWithNewAgents, newArrivals, parameters);
            if(solutionAtTime == null){
                return null; //probably as timeout
            }
            // store the solution
            this.latestSolution = solutionAtTime;
            solutionsAtTimes.put(timestepWithNewAgents, solutionAtTime);
        }

        // combine the stored solutions at times into a single online solution
        return new OnlineSolution(solutionsAtTimes);
    }

    /**
     * copied from {@link OnlineMAPF.Solvers.A_OnlineSolver}.
     * @param time {@inheritDoc}
     * @param agents {@inheritDoc}
     * @param parameters {@inheritDoc}
     * @return {@inheritDoc}
     */
    protected Solution newArrivals(int time, List<? extends OnlineAgent> agents, RunParameters parameters) {
        HashMap<Agent, I_Location> currentAgentLocations = new HashMap<>(agents.size());
        // existing agents will start where the current solution had them at time
        A_OnlineSolver.addExistingAgents(time, currentAgentLocations, this.latestSolution);
        // new agents will start at their private garages.
        A_OnlineSolver.addNewAgents(agents, currentAgentLocations, this.baseInstance);

        return solveForNewArrivals(time, currentAgentLocations, parameters, agents);
    }

    protected Solution solveForNewArrivals(int time, HashMap<Agent, I_Location> currentAgentLocations, RunParameters parameters, List<? extends OnlineAgent> newAgents) {
        // The heuristic doesn't need updating, since super creates it for all agents in init. This isn't cheating because
        // it takes the same amount of time that updating the heuristic every time a new agent arrives would have taken.
        super.customStartLocations = currentAgentLocations;
        MAPF_Instance subProblem = baseInstance.getSubproblemFor(currentAgentLocations.keySet());
        super.instance = subProblem; // so that super will be limited to just the current sub problem
        // cut mdds to update them for all the existing agents' new positions
        int timeDelta = time - (this.latestSolution.size() == 0 ? 0 : this.latestSolution.iterator().next().getPlanStartTime());
//        updateAllMDDs(time, this.latestSolution, timeDelta);
        // update the open list with new prices
        updateOpen(newAgents, timeDelta,this.latestSolution, time);
        latestSolution = super.runAlgorithm(subProblem, parameters);
        if (latestSolution != null){
            // the ICTS solver assumes everyone starts at 0
            OnlineICTSSolver.updateTimes(latestSolution, time);
        }
        return latestSolution;
    }

    /**
     * Replaces {@link #openList} and {@link #contentOfOpen} with new data structures, containing updated {@link ICT_Node ict nodes}.
     * Nodes are updated so that the costs are reduced by the amount of time that has passed, new agents are added (with
     * their minimal cost), and duplicate nodes that are created by this process are removed. Nodes that contain an updated
     * cost that is impossible are removed.
     * @param newAgents the new agents that joined at this time
     * @param timeDelta the amount of time that has passed since starting the latest solution.
     * @param latestSolution the latest solution that the agents have been following up to this time.
     * @param time the current time of the problem.
     */
    private void updateOpen(List<? extends OnlineAgent> newAgents, int timeDelta, Solution latestSolution, int time) {
        // must create new data structures, because they will not re-index/sort elements after an internal change in the element.
        Set<ICT_Node> newContentOfOpen = new HashSet<>();
        Queue<ICT_Node> newOpenList = createOpenList();
        Map<SourceTargetAgent, Map<Integer, MDD>> oldMdds = mddManager.mdds;

        if (updateMDDsWhenTimeProgresses && !keepOnlyRelevantUpdatedMDDs){
            // wil replace the mdds data structure in mddManager with a new one with updated mdds
            updateAllMDDs(time, latestSolution, timeDelta);
        }
        else{
            // will throw away the old mdd manager and its mdds
            this.mddManager = new MDDManager(searcherFactory, this, heuristicICTS);
        }
        Map<SourceTargetAgent, Map<Integer, MDD>> newMdds = mddManager.mdds;

        for (ICT_Node node : this.openList){
            // update the node: reduce costs by the amount of time that has passed, remove agents who left, add new agents.
            ICT_Node newNode = getUpdatedNode(node, newAgents, timeDelta, latestSolution, time, newMdds, oldMdds);

            if (newNode != null && // node could have impossible costs, in which case it can be removed
                    !newContentOfOpen.contains(newNode)){ // in this way, we also remove all the duplicates that we may be left with after updating ICT nodes.
                newContentOfOpen.add(newNode);
                newOpenList.add(newNode);
            }
        }

        this.openList = newOpenList;
        this.contentOfOpen = newContentOfOpen;
    }

    private ICT_Node getUpdatedNode(ICT_Node node, List<? extends OnlineAgent> newAgents, int timeDelta, Solution latestSolution,
                                    int time, Map<SourceTargetAgent, Map<Integer, MDD>> newMdds, Map<SourceTargetAgent,
                                    Map<Integer, MDD>> oldMdds) {
        // reuse the data structure from the original node
        Map<Agent, Integer> agentCosts = node.agentCosts;
        Iterator<Map.Entry<Agent,Integer>> agentCostsIter = agentCosts.entrySet().iterator();
        while (agentCostsIter.hasNext()){
            Agent a = agentCostsIter.next().getKey();
            if (latestSolution.getPlanFor(a).getEndTime() <= time){
                // if the agent has finished (left), remove it
                agentCostsIter.remove();
            }
            else{
                // reduce cost by the amount of time that has passed
                int newCost = agentCosts.get(a) - timeDelta;
                // check that the new cost is possible
                SingleAgentPlan agentPlan = latestSolution.getPlanFor(a);
                if (Math.floor(heuristicICTS.getHForAgentAndCurrentLocation(a, agentPlan.moveAt(time).currLocation)) > newCost){
                    // abandon node because it has an impossible cost
                    return null;
                }
                else{
                    agentCosts.put(a, newCost);
                    // when we see we have demand in open for a certain mdd for an agent, we will try to cut it from
                    // existing mdds
                    if (updateMDDsWhenTimeProgresses && keepOnlyRelevantUpdatedMDDs
                            && latestSolution.size() > 0 ) {// not first iteration
                        updateMDD(time, newCost, agentPlan, oldMdds, newMdds, agentCosts.get(a));
                    }
                }
            }
        }
        // add the new arriving agents
        for (Agent a : newAgents){
            agentCosts.put(a, Math.round(heuristicICTS.getHForAgentAndCurrentLocation(a, getSource(a))));
        }
        // must use setter, even though we stole the internal data structure.
        node.setAgentCost(agentCosts);
        return node;
    }

    /**
     * Attempts to cut existing MDDs to the new time - the sub-MDD stretching from the node representing the agent's new
     * position (the position the agent progressed to  at the current time), by following the agent's plan in the lates
     * solution. If the plan that was chosen for the agent isn't represented by an MDD, that is normal, and we simply
     * skip it.
     * @param time the current time of the problem.
     * @param latestSolution the latest solution that the agents have been following up to this time.
     * @param timeDelta the amount of time that has passed since starting the latest solution.
     */
    private void updateAllMDDs(int time, Solution latestSolution, int timeDelta) {
        if (updateMDDsWhenTimeProgresses && !keepOnlyRelevantUpdatedMDDs){
            mddManager.searchers.clear();
            Map<SourceTargetAgent, Map<Integer, MDD>> mdds = super.mddManager.mdds;
            Map<SourceTargetAgent, Map<Integer, MDD>> newMdds = new HashMap<>();
            for (SingleAgentPlan plan : latestSolution){
                if(plan.getEndTime() > time){ // agent is still around
                    updateAgentMDDs(time, timeDelta, mdds, newMdds, plan);
                }
            }
            this.mddManager.mdds = newMdds;
        }
    }

    private void updateAgentMDDs(int time, int timeDelta, Map<SourceTargetAgent, Map<Integer, MDD>> mdds, Map<SourceTargetAgent, Map<Integer, MDD>> newMdds, SingleAgentPlan plan) {
        // Take the current MDDs we have for the agent, cut them to the sub-MDD that stretches from the agent's new
        // position.
        I_Location originalLocation = plan.moveAt(plan.getFirstMoveTime()).prevLocation;
        Agent agent = plan.agent;
        // get the current MDDs for the agent, and then remove from the mdd manager, so that we don't consume
        // double the memory for a short while
        Map<Integer, MDD> originalAgentMDDs = mdds.get(mddManager.keyDummy.set(originalLocation, getTarget(agent), agent));
        for (int depth : originalAgentMDDs.keySet()){
            updateMDD(time, depth - timeDelta, plan, mdds, newMdds, depth);
        }
    }

    private void updateMDD(int time, int newDepth, SingleAgentPlan plan, Map<SourceTargetAgent, Map<Integer, MDD>> mdds,
                           Map<SourceTargetAgent, Map<Integer, MDD>> newMdds, int originalDepth) {

        // Take the current MDDs we have for the agent, cut them to the sub-MDD that stretches from the agent's new
        // position.
        I_Location originalLocation = plan.moveAt(plan.getFirstMoveTime()).prevLocation;
        I_Location currentLocation = plan.moveAt(time).currLocation;
        Agent agent = plan.agent;

        // if we didn't already compute an mdd for the original depth, there is no mdd to cut
        Map<Integer, MDD> originalAgentMDDs = mdds.get(mddManager.keyDummy.set(originalLocation, getTarget(agent), agent));
        if (!originalAgentMDDs.containsKey(originalDepth)){
            return;
        }

        // if we already have an mdd in the new MDDManager, we don't need to cut one from the old
        SourceTargetAgent key = new SourceTargetAgent(currentLocation, getTarget(agent), agent);
        newMdds.computeIfAbsent(key, sourceTargetAgent -> new HashMap<>());
        Map<Integer, MDD> newAgentMDDs = newMdds.get(key);
        if (newAgentMDDs.containsKey(newDepth)){
            return;
        }

        // cut mdds according to current position of agent (if possible!)
        MDDNode currentMDDNode = originalAgentMDDs.get(originalDepth).getStart();
        // follow the plan that the agent followed, until the time we are at, meanwhile going down the MDD
        // along the correct branch (if it exists)
        for (int i = plan.getFirstMoveTime(); i <= time ; i++){
            Move move = plan.moveAt(i);
            MDDNode nextMDDNode = null;
            for (MDDNode childNode : currentMDDNode.getNeighbors()){
                if (move.currLocation.equals(childNode.getLocation())){
                    nextMDDNode = childNode;
                }
            }
            currentMDDNode = nextMDDNode;
            if (nextMDDNode == null){
                // this means there is no MDD of the appropriate depth from the agent's new position to its goal
                break;
            }
        }
        if (currentMDDNode != null){
            // managed to cut the mdd
            // new depth is old depth minus the amount of time that has passed
            newAgentMDDs.put(newDepth, originalAgentMDDs.get(originalDepth).changeStartNode(currentMDDNode));
        }
    }

    /**
     * Put the goal back into open because we may want to resume search when new agents arrive.
     * @param current {@inheritDoc}
     * @param mergedMDDSolution  {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    protected Solution foundGoal(ICT_Node current, Solution mergedMDDSolution) {
        openList.add(current);
        contentOfOpen.add(current);
        return super.foundGoal(current, mergedMDDSolution);
    }

    @Override
    public String name() {
        return name;
    }
}
