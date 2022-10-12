package BasicMAPF.Solvers.LargeNeighborhoodSearch;

import BasicMAPF.Solvers.AStar.AStarHeuristic;
import BasicMAPF.Solvers.RunParameters;

public class RunParametersLNS extends RunParameters {

    public final AStarHeuristic aStarHeuristic;

    public RunParametersLNS(RunParameters runParameters, AStarHeuristic aStarHeuristic) {
        super(runParameters);
        this.aStarHeuristic = aStarHeuristic;
    }
}
