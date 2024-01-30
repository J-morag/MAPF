package TransientMAPF;

public enum TransientMAPFBehaviour {
    regularMAPF, // regular MAPF
    transientMAPF, // transient MAPF
    transientMAPFWithBlacklist,// transient MAPF with avoiding the targets of other agents

    transientMAPFsstWithBlacklist; // transient MAPF that optimize SST

    public boolean isTransientMAPF() {
        return this == transientMAPF || this == transientMAPFWithBlacklist || this == transientMAPFsstWithBlacklist;
    }
}
