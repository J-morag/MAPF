package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.*;

/**
 * A class relevant to LaCAM solver.
 * High-Level node represent a configuration of locations in LaCAM search.
 * The node saves more details such as priority order of agents, time step and Low-Level tree.
 */
public class HighLevelNode {
    public HashMap<Agent, I_Location> configuration;
    public Queue<LowLevelNode> tree;
    public ArrayList<Agent> order;
    public HashMap<Agent, Float> priorities;
    public HighLevelNode parent;
    public HashMap<Agent, Boolean> reachedGoalsMap;
    public int timeStep;

    public HighLevelNode(HashMap<Agent, I_Location> configuration, LowLevelNode root,ArrayList<Agent> order, HashMap<Agent, Float> priorities, HighLevelNode parent) {
        this.configuration = configuration;
        this.tree = new LinkedList<>();
        this.tree.add(root);
        this.order = order;
        this.priorities = priorities;
        this.parent = parent;


        if (parent == null) {
            // update reachedGoalsMap according to new configuration
            this.reachedGoalsMap = new HashMap<>();
            for (Agent agent : this.configuration.keySet()) {
                this.reachedGoalsMap.put(agent, false);
            }
            // update time step
            this.timeStep = 1;
        }
        else {
            this.reachedGoalsMap = new HashMap<>(parent.reachedGoalsMap);
            for (Map.Entry<Agent, Boolean> entry : this.reachedGoalsMap.entrySet()) {
                Agent agent = entry.getKey();
                Boolean reachedGoal = entry.getValue();
                if (!reachedGoal && configuration.get(agent).getCoordinate().equals(agent.target)) {
                    this.reachedGoalsMap.put(agent, true);
                }
            }
            // update time step
            this.timeStep = parent.timeStep + 1;
        }
    }
}
