package BasicMAPF.Solvers.AStar;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.Instances.Maps.I_Location;

import java.util.HashMap;
import java.util.List;

public class RunParameters_SAAStarSIPP extends RunParameters_SAAStar {
    public HashMap<I_Location, List<SingleAgentAStarSIPP_Solver.Interval>> safeIntervalsByLocation;

    public RunParameters_SAAStarSIPP(RunParameters parameters) {
        super(parameters);
    }
}
