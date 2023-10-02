package BasicMAPF.Solvers.LaCAM;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.A_Solver;

import java.util.Stack;

public class LaCAM_Solver extends A_Solver {

    /**
     * open stack of high-level nodes.
     * The use in stack means that the algorithm performs a Depth First Search.
     */
    private Stack<HighLevelNode> open;

    /**
     *
     */
    private Stack<HighLevelNode> explored;
    protected void init(MAPF_Instance instance, RunParameters parameters){
        super.init(instance, parameters);
        this.open = new Stack<>();
        this.explored = new Stack<>();
    }
    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        return null;
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
    }
}
