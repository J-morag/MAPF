package BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees;

import BasicMAPF.CostFunctions.I_SolutionCostFunction;
import BasicMAPF.CostFunctions.SumOfCosts;
import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.RunParametersBuilder;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.DataTypesAndStructures.Timeout;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.CanonicalSolversFactory;
import Environment.Config;
import Environment.Metrics.InstanceReport;
import com.google.common.collect.Collections2;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static BasicMAPF.Solvers.PrioritisedPlanningWithGuarantees.PathAndPrioritySearch.*;

/**
 * Find the best priority ordering by running PCS for each possible ordering and choosing the best one.
 */
public class NaivePaPS extends A_Solver {

    private final PathAndPrioritySearch PCS;
    private final I_SolutionCostFunction costFunction;
    private final int timeoutPerOrdering;

    private int numSolvableOrderingsFound;
    private int numUnsolvableOrderingsFound;
    private int worstCost;

    private int generatedVirtualNodes;
    private int fullyGeneratedNodes;
    private int generateNodeInstances;

    public NaivePaPS(PathAndPrioritySearch pcs, I_SolutionCostFunction costFunction, Integer timeoutPerOrdering) {
        this.PCS = Objects.requireNonNullElseGet(pcs, CanonicalSolversFactory::createPCSSolver);
        this.costFunction = Objects.requireNonNullElseGet(costFunction, SumOfCosts::new);
        this.timeoutPerOrdering = Objects.requireNonNullElse(timeoutPerOrdering, -1);

        generatedVirtualNodes = 0;
        fullyGeneratedNodes = 0;
        generateNodeInstances = 0;

        this.name = "NaiveOptimalPrioritySearch";
    }

    @Override
    protected void init(MAPF_Instance instance, RunParameters parameters) {
        super.init(instance, parameters);
        numSolvableOrderingsFound = 0;
        numUnsolvableOrderingsFound = 0;
        worstCost = Integer.MIN_VALUE;
    }

    @Override
    protected Solution runAlgorithm(MAPF_Instance instance, RunParameters parameters) {
        List<Agent> bestOrdering = null;
        Solution bestSolution = null;
        int bestCost = Integer.MAX_VALUE;
        Collection<List<Agent>> permutations = getAllOrderPermutations(new ArrayList<>(instance.agents));
        for (List<Agent> ordering : permutations) {
            if (checkTimeout()){
                if (Config.INFO >= 2) System.out.println("Global Timeout");
                return null;
            }
            MAPF_Instance instanceWithOrdering = new MAPF_Instance(instance.name, instance.map, ordering.toArray(new Agent[0]));
            RunParameters parametersWithOrdering = getSubproblemRunParameters(parameters, ordering);
            if (Config.INFO >= 2) System.out.println("Running PCS with ordering: " + ordering);
            Timeout subproblemTimeout = new Timeout(Timeout.getCurrentTimeMS_NSAccuracy(), getSubproblemTimeLeft());
            Solution solution = PCS.solve(instanceWithOrdering, parametersWithOrdering);
            digestSubproblemReport(parametersWithOrdering.instanceReport);
            if (solution != null) {
                numSolvableOrderingsFound++;
                int cost = costFunction.solutionCost(solution);
                if (cost < bestCost) {
                    if (Config.INFO >= 2) System.out.println("Found new best ordering (solution) with cost: " + cost);
                    bestCost = cost;
                    bestOrdering = ordering;
                    bestSolution = solution;
                }
                if (cost > worstCost) {
                    worstCost = cost;
                }
            } else if (subproblemTimeout.isTimeoutExceeded()){
                if (Config.INFO >= 2) System.out.println("Subproblem Timeout");
                return null;
            }
            else {
                numUnsolvableOrderingsFound++;
                if (Config.INFO >= 2) System.out.println("No solution found for ordering");
            }
        }
        return bestSolution;
    }

    @Override
    protected void digestSubproblemReport(InstanceReport subproblemReport) {
        super.digestSubproblemReport(subproblemReport);
        generatedVirtualNodes += subproblemReport.getIntegerValue(GENERATED_VIRTUAL_NODES_STR);
        fullyGeneratedNodes += subproblemReport.getIntegerValue(GENERATED_FULL_NODES_STR);
        generateNodeInstances += subproblemReport.getIntegerValue(GENERATED_NODE_INSTANCES_STR);
        generatedNodes += subproblemReport.getIntegerValue(InstanceReport.StandardFields.generatedNodes);
        expandedNodes += subproblemReport.getIntegerValue(InstanceReport.StandardFields.expandedNodes);
    }

    public static @NotNull Collection<List<Agent>> getAllOrderPermutations(List<Agent> agents) {
        return Collections2.orderedPermutations(agents, Comparator.comparingInt((a) -> a.iD));
    }

    private RunParameters getSubproblemRunParameters(RunParameters parameters, List<Agent> ordering) {
        long timeLeftToTimeout = getSubproblemTimeLeft();
        InstanceReport subproblemReport = new InstanceReport();
        return new RunParametersBuilder().setInstanceReport(subproblemReport)
                .setPriorityOrder(ordering.toArray(new Agent[0])).setTimeout(timeLeftToTimeout).createRP();
    }

    private long getSubproblemTimeLeft() {
        return timeoutPerOrdering > 0 ? timeoutPerOrdering :
                Math.max(super.maximumRuntime - (System.nanoTime()/1000000 - super.startTime), 0);
    }

    @Override
    protected void writeMetricsToReport(Solution solution) {
        super.writeMetricsToReport(solution);
        instanceReport.putIntegerValue("numSolvableOrderingsFound", numSolvableOrderingsFound);
        instanceReport.putIntegerValue("numUnsolvableOrderingsFound", numUnsolvableOrderingsFound);
        instanceReport.putIntegerValue("worstCost", worstCost);

        instanceReport.putIntegerValue(GENERATED_VIRTUAL_NODES_STR, generatedVirtualNodes);
        instanceReport.putIntegerValue(GENERATED_FULL_NODES_STR, fullyGeneratedNodes);
        instanceReport.putIntegerValue(GENERATED_NODE_INSTANCES_STR, generateNodeInstances);
    }

    @Override
    protected void releaseMemory() {
        super.releaseMemory();
    }
}
