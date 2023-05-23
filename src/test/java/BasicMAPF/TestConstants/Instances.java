package BasicMAPF.TestConstants;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;

import static BasicMAPF.TestConstants.Agents.*;
import static BasicMAPF.TestConstants.Maps.mapEmpty;

public class Instances {

    public final static MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00});
}
