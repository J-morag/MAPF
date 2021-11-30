package BasicCBS.Solvers.ICTS.MDDs;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MDDSearchNode implements Comparable<MDDSearchNode>{
    private I_Location location;
    private List<MDDSearchNode> parents;
    private int g;
    private float h;
    private Agent agent;

    public MDDSearchNode(Agent agent, I_Location location, int g, DistanceTableAStarHeuristicICTS heuristic) {
        this.agent = agent;
        this.location = location;
        this.g = g;
        parents = new LinkedList<>();

        //calculate heuristic
        heuristic.setH(this);
    }

    public void setH(float h) {
        this.h = h;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MDDSearchNode)) return false;

        MDDSearchNode node = (MDDSearchNode) o;

        if (g != node.g) return false;
        if (!location.equals(node.location)) return false;
        return agent.equals(node.agent);
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + g;
        result = 31 * result + agent.hashCode();
        return result;
    }

    public Agent getAgent() {
        return agent;
    }

    public List<MDDSearchNode> getParents() {
        return parents;
    }

    public void addParent(MDDSearchNode parent){
        parents.add(parent);
    }

    public float getF(){
        return g + h;
    }

    public I_Location getLocation() {
        return location;
    }

    public int getG() {
        return g;
    }

    public float getH() {
        return h;
    }

    @Override
    public int compareTo(MDDSearchNode node) {
        return Float.compare(this.getF(), node.getF());
    }

    public void addParents(List<MDDSearchNode> parents) {
        this.parents.addAll(parents);
    }

    public List<I_Location> getNeighborLocations(){
        // can move to neighboring locations or stay put
        I_Location currLocation = this.location;
        List<I_Location> neighborLocationsIncludingCurrent = new ArrayList<>(currLocation.outgoingEdges());
        neighborLocationsIncludingCurrent.add(currLocation); //staying in the same location is possible
        return neighborLocationsIncludingCurrent;
    }

    @Override
    public String toString() {
        return "Node{" +
                "location=" + location +
                ", g=" + g +
                '}';
    }
}
