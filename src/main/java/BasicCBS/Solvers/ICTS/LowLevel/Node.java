package BasicCBS.Solvers.ICTS.LowLevel;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.I_Location;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class Node implements Comparable<Node>{
    private I_Location location;
    private List<Node> parents;
    private int g;
    private float h;
    private Agent agent;

    public Node(Agent agent, I_Location location, int g, DistanceTableAStarHeuristicICTS heuristic) {
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
        if (o == null || getClass() != o.getClass()) return false;
        Node node = (Node) o;
        return g == node.g &&
                location.equals(node.location) &&
                agent.equals(node.agent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, g, agent);
    }

    public Agent getAgent() {
        return agent;
    }

    public List<Node> getParents() {
        return parents;
    }

    public void addParent(Node parent){
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
    public int compareTo(Node node) {
        return Float.compare(this.getF(), node.getF());
    }

    public void addParents(List<Node> parents) {
        this.parents.addAll(parents);
    }

    public List<I_Location> getNeighborLocations(){
        // can move to neighboring cells or stay put
        I_Location currLocation = this.location;
        List<I_Location> neighborCellsIncludingCurrent = new ArrayList<>(currLocation.getNeighbors());
        neighborCellsIncludingCurrent.add(currLocation); //staying in the same location is possible
        return neighborCellsIncludingCurrent;
    }

    @Override
    public String toString() {
        return "Node{" +
                "location=" + location +
                ", g=" + g +
                '}';
    }
}
