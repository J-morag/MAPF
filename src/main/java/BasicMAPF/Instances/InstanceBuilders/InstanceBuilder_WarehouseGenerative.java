package BasicMAPF.Instances.InstanceBuilders;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class InstanceBuilder_WarehouseGenerative extends InstanceBuilder_Warehouse {

    private final List<Integer> seeds;
    private final MDP mdp;

    public InstanceBuilder_WarehouseGenerative(Boolean dropDisabledEdges, Boolean forceEdgesBidirectional,
                                               @NotNull List<Integer> seeds, @NotNull MDP mdp)
    {
        super(dropDisabledEdges, forceEdgesBidirectional);
        this.seeds = seeds;
        this.mdp = mdp;
    }


}