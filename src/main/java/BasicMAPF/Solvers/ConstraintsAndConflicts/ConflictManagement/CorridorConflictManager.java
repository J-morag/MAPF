package BasicMAPF.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.Constraint;
import BasicMAPF.Solvers.ConstraintsAndConflicts.Constraint.ConstraintSet;
import BasicMAPF.Solvers.ConstraintsAndConflicts.CorridorConflict;
import BasicMAPF.Solvers.ConstraintsAndConflicts.SwappingConflict;
import BasicMAPF.DataTypesAndStructures.Move;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Implements Corridor conflicts from:
 * Li, Jiaoyang, et al. "New techniques for pairwise symmetry breaking in multi-agent path finding."
 * Proceedings of the International Conference on Automated Planning and Scheduling. Vol. 30. 2020.
 */
public class CorridorConflictManager extends ConflictManager {

    private final ConstraintSet constraints;
    private final MAPF_Instance instance;
    private final Set<CorridorConflict> corridorConflicts = new HashSet<>();

    public CorridorConflictManager(ConflictSelectionStrategy conflictSelectionStrategy, ConstraintSet constraints, MAPF_Instance instance) {
        super(conflictSelectionStrategy);
        this.constraints = constraints;
        this.instance = instance;
    }

    public CorridorConflictManager(ConstraintSet constraints, MAPF_Instance instance) {
        this.constraints = constraints;
        this.instance = instance;
    }

    public CorridorConflictManager(ConflictManager other, ConstraintSet constraints, MAPF_Instance instance) {
        super(other);
        this.constraints = constraints;
        this.instance = instance;
    }

    @Override
    protected boolean addConflict(A_Conflict conflict) {
        boolean changed = super.addConflict(conflict);
        if (conflict instanceof CorridorConflict ){
            return this.corridorConflicts.add((CorridorConflict) conflict);
        }
        // check for a corridor conflict derived from the added conflict
        CorridorConflict corridorConflict = getCorridorConflict(conflict);
        if(corridorConflict != null){
            changed |= corridorConflicts.add(corridorConflict);
        }
        // return a bool indicating whether or not the set of conflicts changed
        return changed;
    }

    private CorridorConflict getCorridorConflict(A_Conflict conflict) {
        // agents must be coming from opposing directions.
        // so if they are coming from same direction or one of them is already at its goal, return null.
        Move agent1Move = super.agentPlans.get(conflict.agent1).moveAt(conflict.time);
        Move agent2Move = super.agentPlans.get(conflict.agent2).moveAt(conflict.time);
        if( (agent1Move == null || agent2Move == null ) ||(
                Objects.equals(agent1Move.prevLocation, agent2Move.prevLocation)
                && agent1Move.currLocation.equals(agent2Move.currLocation)) ){
            return null;
        }

        I_Location conflictLocation = null;
        if (isDegree2(conflict.location)){
            // is the vertex in a corridor?
            conflictLocation = conflict.location;
        }
        else if(conflict instanceof SwappingConflict && isDegree2(((SwappingConflict)conflict).agent2_destination)){
            // is the vertex in a corridor? (swapping conflict case)
            conflictLocation = ((SwappingConflict)conflict).agent2_destination;
        }
        else {return null;}

        // we need to collect the vertices of the corridor to remove them from the map and make the bypass check
        // later (in the corridor conflict)
        HashSet<I_Location> corridorVertices = new HashSet<>();
        corridorVertices.add(conflictLocation);

        // go to both ends of the corridor. vertices belong to the corridor if they are of degree 2.

        I_Location dir1anchor= super.agentPlans.get(conflict.agent1).moveAt(conflict.time).currLocation;
        I_Location dir1neighbor = super.agentPlans.get(conflict.agent1).moveAt(conflict.time).prevLocation;
        I_Location beginning = exploreCorridorOneDirection(corridorVertices, dir1anchor, dir1neighbor, conflict.agent1, conflict.agent2);

        I_Location dir2anchor = super.agentPlans.get(conflict.agent2).moveAt(conflict.time).currLocation;
        I_Location dir2neighbor = super.agentPlans.get(conflict.agent2).moveAt(conflict.time).prevLocation;
        I_Location end = exploreCorridorOneDirection(corridorVertices, dir2anchor, dir2neighbor, conflict.agent1, conflict.agent2);

        if (equalsSourceOrTarget(dir1anchor, conflict.agent1) || equalsSourceOrTarget(dir2anchor, conflict.agent2)){
            // agent could have left its source and then returned, in which case they may conflict on its source,
            // so it can't be treated as a corridor conflict
            return null;
        }

        if (corridorVertices.size() == 2){
            // in this case it is really just a swapping conflict
            return null;
        }
        return new CorridorConflict(conflict.agent1, conflict.agent2, conflict.time, beginning, end, corridorVertices,
                constraints, instance, this.agentPlans.get(conflict.agent1), this.agentPlans.get(conflict.agent2));
    }

    /**
     * Traverses the corridor in one direction, adding the vertices to corridorVertices, and finding one edge
     * @param corridorVertices deposits corridor vetices here
     * @param someDirNeighbor determines the direction of traversal
     * @return an edge of the corridor
     */
    private I_Location exploreCorridorOneDirection(HashSet<I_Location> corridorVertices, I_Location anchor, I_Location someDirNeighbor,
                                                   Agent agent1, Agent agent2) {
        // run until we find a vertex not of degree, or the source or the target of one of the agents
        while(isDegree2(someDirNeighbor) && ! equalsSourceOrTarget(someDirNeighbor, agent1)
                && ! equalsSourceOrTarget(someDirNeighbor, agent2)){
            // might add an existing vertex but it's fine since this is a set.
            corridorVertices.add(someDirNeighbor);
            // get the neighbor of the current neighbor that is not the anchor (so it is in the right direction)
            I_Location newNeighbour = someDirNeighbor.outgoingEdges().get(0).equals(anchor) ? someDirNeighbor.outgoingEdges().get(1)
                    : someDirNeighbor.outgoingEdges().get(0);
            anchor = someDirNeighbor;
            someDirNeighbor = newNeighbour;
        }
        // then add that last vertex
        corridorVertices.add(someDirNeighbor);
        return someDirNeighbor;
    }

    private boolean equalsSourceOrTarget(I_Location location, Agent agent){
        return location.getCoordinate().equals(agent.source) || location.getCoordinate().equals(agent.target);
    }

    private boolean isDegree2(I_Location location){
        return location.outgoingEdges().size() == 2;
    }

    @Override
    public A_Conflict selectConflict() {
        // prefer corridor conflicts if any exist
        while(!this.corridorConflicts.isEmpty()){
            CorridorConflict corridorConflict = (CorridorConflict) super.conflictSelectionStrategy.selectConflict(this.corridorConflicts);
            // check that the current paths for both agents violate the range constraint derived from the conflict
            // This guarantees that the paths in both child CT nodes will be different from the paths in the current CT node
            Constraint[] preventingConstraints = corridorConflict.getPreventingConstraints();
            if(preventingConstraints[0].rejects(super.agentPlans.get(corridorConflict.agent1))
                    && preventingConstraints[1].rejects(super.agentPlans.get(corridorConflict.agent2))){
                return corridorConflict;
            }
            // if it isn't usable, remove it.
            this.corridorConflicts.remove(corridorConflict);
        }
        return super.selectConflict();
    }
}
