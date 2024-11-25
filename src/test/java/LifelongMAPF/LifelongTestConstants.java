package LifelongMAPF;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import static BasicMAPF.TestConstants.Coordiantes.*;
import static BasicMAPF.TestConstants.Maps.*;

public class LifelongTestConstants {

    public static final long DEFAULT_TIMEOUT = 30L * 1000;


    public static final LifelongAgent agent33to12 = new LifelongAgent(new Agent(0, coor33, coor12), new I_Coordinate[]{coor33, coor14, coor12});
    public static final LifelongAgent agent12to33 = new LifelongAgent(new Agent(1, coor12, coor33), new I_Coordinate[]{coor12, coor22, coor33});
    public static final LifelongAgent agent53to05 = new LifelongAgent(new Agent(2, coor53, coor05), new I_Coordinate[]{coor53, coor04, coor33, coor10, coor00, coor04, coor32, coor05});
    public static final LifelongAgent agent43to11 = new LifelongAgent(new Agent(3, coor43, coor11), new I_Coordinate[]{coor43, coor04, coor54, coor33, coor54, coor35, coor32, coor11});
    public static final LifelongAgent agent04to54 = new LifelongAgent(new Agent(4, coor04, coor54), new I_Coordinate[]{coor04, coor00, coor04, coor10, coor34, coor35, coor10, coor54});
    public static final LifelongAgent agent54to04 = new LifelongAgent(new Agent(5, coor54, coor04), new I_Coordinate[]{coor54, coor10, coor04, coor00, coor04, coor10, coor04});
    public static final LifelongAgent agent00to10 = new LifelongAgent(new Agent(6, coor00, coor10), new I_Coordinate[]{coor00, coor04, coor33, coor00, coor43, coor04, coor54, coor10});
    public static final LifelongAgent agent10to00 = new LifelongAgent(new Agent(7, coor10, coor00), new I_Coordinate[]{coor10, coor53, coor35, coor00, coor05, coor54, coor10, coor00});
    public static final LifelongAgent agent04to00 = new LifelongAgent(new Agent(8, coor04, coor00), new I_Coordinate[]{coor04, coor00, coor53, coor32, coor14, coor00, coor10, coor00});
    public static final LifelongAgent agent33to35 = new LifelongAgent(new Agent(9, coor33, coor35), new I_Coordinate[]{coor33, coor35, coor53, coor15, coor43, coor04, coor00, coor35});
    public static final LifelongAgent agent34to32 = new LifelongAgent(new Agent(10, coor34, coor32), new I_Coordinate[]{coor34, coor04, coor35, coor00, coor54, coor12, coor15, coor32});
    public static final LifelongAgent agent43to14 = new LifelongAgent(new Agent(11, coor43, coor14), new I_Coordinate[]{coor43, coor04, coor12, coor00, coor54,  coor35, coor15, coor14});
    public static final LifelongAgent agent14to43 = new LifelongAgent(new Agent(12, coor14, coor43), new I_Coordinate[]{coor14, coor04, coor12, coor00, coor54,  coor35, coor15, coor43});
    public static final LifelongAgent agent34to43 = new LifelongAgent(new Agent(13, coor34, coor43), new I_Coordinate[]{coor34, coor15, coor35, coor54, coor04, coor00, coor12, coor43});

    public static final MAPF_Instance instanceCircle1 = new MAPF_Instance("instanceCircle1", mapCircle, new Agent[]{agent33to12, agent12to33});
    public static final MAPF_Instance instanceCircle2 = new MAPF_Instance("instanceCircle2", mapCircle, new Agent[]{agent12to33, agent33to12});
    public static final MAPF_Instance instanceEmpty1 = new MAPF_Instance("instanceEmpty", mapEmpty,
            new Agent[]{agent33to12, agent12to33, agent53to05, agent43to11, agent04to54, agent00to10, agent10to00, agent34to32});
    public static final MAPF_Instance instanceUnsolvable = new MAPF_Instance("instanceUnsolvable", mapWithPocket, new Agent[]{
            new LifelongAgent(new Agent(5, coor00, coor10), new I_Coordinate[]{coor00, coor10, coor00, coor10}),
            new LifelongAgent(new Agent(6, coor10, coor00), new I_Coordinate[]{coor10, coor00, coor10, coor00})
    });
    public static final MAPF_Instance instanceSmallMaze = new MAPF_Instance("instanceSmallMaze", mapSmallMaze, new Agent[]{agent04to00, agent00to10});
    public static final MAPF_Instance instanceSmallMazeDense = new MAPF_Instance("instanceSmallMazeDense", mapSmallMaze,
            new Agent[]{
                    agent33to12,
                    agent12to33,
                    agent04to54,
                    agent34to32,
                    agent43to14
            });
    public static final MAPF_Instance instanceSmallMazeDenser = new MAPF_Instance("instanceSmallMazeDense", mapSmallMaze,
            new Agent[]{
                    agent33to12,
                    agent12to33,
                    agent04to54,
                    agent34to32,
                    agent43to14,
                    agent14to43,
                    agent54to04,
            });
    public static final MAPF_Instance instanceStartAdjacentGoAround = new MAPF_Instance("instanceStartAdjacentGoAround", mapSmallMaze, new Agent[]{agent33to35, agent34to32});
}
