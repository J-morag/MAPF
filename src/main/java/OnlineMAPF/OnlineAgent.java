package OnlineMAPF;

import BasicCBS.Instances.Agent;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.I_Location;

public class OnlineAgent extends Agent {

    public static final int DEFAULT_ARRIVAL_TIME = 0;
    public final int arrivalTime;

     public OnlineAgent(int iD, I_Coordinate source, I_Coordinate target, int priority, int arrivalTime) {
        super(iD, source, target, priority);
        this.arrivalTime = arrivalTime;
    }

    public OnlineAgent(int iD, I_Coordinate source, I_Coordinate target, int arrivalTime) {
        super(iD, source, target);
        this.arrivalTime = arrivalTime;
    }

    public OnlineAgent(Agent offlineAgent, int arrivalTime){
        this(offlineAgent.iD, offlineAgent.source, offlineAgent.target, offlineAgent.priority, arrivalTime);
    }

    public OnlineAgent(Agent offlineAgent){
        this(offlineAgent, DEFAULT_ARRIVAL_TIME);
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public PrivateGarage getPrivateGarage(I_Location mapEntryVertex) {
        if(!mapEntryVertex.getCoordinate().equals(super.source))
            throw new IllegalArgumentException("An OnlineAgent's map entry vertex must correspond to its source coordinate");
        return new PrivateGarage(super.iD, mapEntryVertex);
    }
}
