package BasicMAPF.TestConstants;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.*;
import static BasicMAPF.TestConstants.Maps.mapSmallMaze;

public class Instances {

    public final static MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty1", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00});
    public final static MAPF_Instance instanceEmpty2 = new MAPF_Instance("instanceEmpty2", mapEmpty, new Agent[]{agent33to35, agent34to32, agent31to14, agent40to02, agent30to33});
    public final static MAPF_Instance instanceEmptySameTarget = new MAPF_Instance("instanceEmptySameTarget", mapEmpty, new Agent[]{agent10to00, agent04to00});
    public final static MAPF_Instance instanceEmptyEasy = new MAPF_Instance("instanceEmptyEasy", mapEmpty, new Agent[]{agent33to12, agent04to00});
    public final static MAPF_Instance instanceEmptyHarder = new MAPF_Instance("instanceEmptyHarder", mapEmpty, new Agent[]
            {agent33to12, agent12to33, agent53to05, agent43to11, agent04to00, agent00to10, agent55to34, agent34to32, agent31to14, agent40to02});
    public final static MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle,
            new Agent[]{agent33to12, agent12to33});
    public final static MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle1", mapCircle,
            new Agent[]{agent12to33, agent33to12});
    public final static MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket,
            new Agent[]{agent00to10, agent10to00});
    public final static MAPF_Instance instanceSmallMaze = new MAPF_Instance("instanceSmallMaze", mapSmallMaze,
            new Agent[]{agent04to00, agent00to10});
    public final static MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze,
            new Agent[]{agent33to35, agent34to32});
}
