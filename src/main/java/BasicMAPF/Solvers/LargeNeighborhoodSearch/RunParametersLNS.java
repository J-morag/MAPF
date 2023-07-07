package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.Solvers.AStar.CostsAndHeuristics.AStarGAndH;
import BasicMAPF.DataTypesAndStructures.RunParameters;

public class RunParametersLNS extends RunParameters {

    public final AStarGAndH aStarGAndH;

    public RunParametersLNS(RunParameters runParameters, AStarGAndH aStarGAndH) {
        super(runParameters);
        this.aStarGAndH = aStarGAndH;
    }
}
