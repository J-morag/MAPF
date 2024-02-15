package BasicMAPF.DataTypesAndStructures.MDDs;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class MDDSearchNode implements Comparable<MDDSearchNode>{
    private final I_Location location;
    /**
     * Used to stop searching the time dimension when searching for a minimal MDD and have passed last constraint time.
     */
    private final int t;
    private final List<MDDSearchNode> parents;
    private final int g;
    private final int h;
    private final Agent agent;
    // serial ID for tie-breaking consistency
    private static int serialIDCounter = 0;
    private final int serialID;

    public MDDSearchNode(Agent agent, I_Location location, int g, int h, int t) {
        this.agent = agent;
        this.location = location;
        this.g = g;
        this.t = t;
        parents = new LinkedList<>();
        this.h = h;
        this.serialID = serialIDCounter++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MDDSearchNode that)) return false;

        if (t != that.t) return false;
        if (!location.equals(that.location)) return false;
        return agent.equals(that.agent);
    }

    @Override
    public int hashCode() {
        int result = location.hashCode();
        result = 31 * result + t;
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

    public int getF(){
        return g + h;
    }

    public I_Location getLocation() {
        return location;
    }

    public int getG() {
        return g;
    }

    /**
     * Used to stop searching the time dimension when searching for a minimal MDD and have passed last constraint time.
     */
    public int getT() {
        return t;
    }

    public float getH() {
        return h;
    }

    @Override
    public int compareTo(MDDSearchNode node) {
        int result = Integer.compare(this.getF(), node.getF());
        if(result != 0) return result;
        result = Integer.compare(node.getG(), this.getG());
        if(result != 0) return result;
        result = Integer.compare(this.serialID, node.serialID);
        return result;
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
