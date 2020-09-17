package BasicCBS.Instances;

import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;

import java.util.Collection;
import java.util.List;

/**
 * Represents a Multi Agent Path Finding instance/problem.
 * An immutable datatype.
 */
public class MAPF_Instance {

    /**
     * A name representing the instance/problem. There is no guarantee about the contents of this field, including
     * uniqueness.
     */
    public final String name;
    /**
     * An instance might have a longer, more descriptive name. If not provided, defaults to being the same as {@link #name}.
     */
    public final String extendedName;
    /**
     * The map on which the MAPF instance/problem is solved. For example, a 2-dimensional, 4-connected grid.
     */
    public final I_Map map;
    /**
     * An unmodifiable list of agents. Since the {@link Agent}s should themselves be immutable, the contents of this
     * list cannot be changed in any way.
     */
    public final List<Agent> agents;
    /**
    * Obstacle as a percentage, Like: 15%
    */
    private int ObstaclePercentage = -1;


    public MAPF_Instance(String name, I_Map map, Agent[] agents, String extendedName) {
        if(name == null || map == null || agents == null){throw new IllegalArgumentException();}
        this.name = name;
        this.map = map;
        this.agents = List.of(agents); //unmodifiable list
        this.extendedName = extendedName;
    }

    public MAPF_Instance(String name, I_Map map, Agent[] agents) {
        this(name, map, agents, name);
    }

    MAPF_Instance(String name, I_Map map, List<Agent> agents, String extendedName) {
        this(name, map, agents.toArray(Agent[]::new), extendedName);
    }

    MAPF_Instance(String name, I_Map map, List<Agent> agents) {
        this(name, map, agents.toArray(Agent[]::new));
    }

    /**
     * Creates a new {@link MAPF_Instance} from this {@link MAPF_Instance}, which only contains one of the {@link #agents}.
     * @param agent the agent to create a sub-instance for.
     * @return a new {@link MAPF_Instance} created from this {@link MAPF_Instance}, which only contains one of the {@link #agents}.
     * @throws IllegalArgumentException if the agent is not contained in {@link #agents}.
     */
    public MAPF_Instance getSubproblemFor(Agent agent){
        if(agent == null || !this.agents.contains(agent)){throw new IllegalArgumentException("Agent not present in instance.");}
        return new MAPF_Instance(name+"-agent" + agent.iD, map, new Agent[]{agent});
    }

    /**
     * Creates a new {@link MAPF_Instance} from this {@link MAPF_Instance}, where some of the locations in the map are removed.
     * @param mapLocations a collection of location to remove from the instance's map
     * @return a new {@link MAPF_Instance} from this {@link MAPF_Instance}, where some of the locations in the map are removed.
     */
    public MAPF_Instance getSubproblemWithout(Collection<? extends I_Location> mapLocations){
        if(mapLocations == null){throw new IllegalArgumentException("mapLocations can't be null");}
        return new MAPF_Instance(name+"-reducedMap", map.getSubmapWithout(mapLocations), agents);
    }

    public void setObstaclePercentage(int obstaclePercentage){
        this.ObstaclePercentage = obstaclePercentage;
    }

    public int getObstaclePercentage(){
        return this.ObstaclePercentage;
    }

}
