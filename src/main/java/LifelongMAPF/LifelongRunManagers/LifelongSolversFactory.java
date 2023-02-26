package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.*;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.LifelongSimulationSolver;
import LifelongMAPF.SingleAgentFailPolicies.OneActionFailPolicy;

public class LifelongSolversFactory {

    public static I_Solver stationaryAgentsReplanSinglePartialAllowedClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), 
                        true, true, null), null, null, null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 15), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 15), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 20), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 20), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h10(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(10)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 20), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialOneActionFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 10), null, new DisallowedPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPWidePartialOneActionFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new WidePartialSolutionsStrategy(), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPDeepPartialOneActionFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new OneDeepThenWidePartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPWidePartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new WidePartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPOneDeepThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepUntilFoundThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new DeepUntilFoundFullPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoffStochasticIndexNoWeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.75, null), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff50PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff75PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff50PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff75PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCRLookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialOneActionRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFP() {
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 1);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead4(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 4);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead7(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 7);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w10_h03Lookahead10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), 10);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff50PercentPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.50), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff75PercentPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new IndexBasedPartialSolutionsStrategy(0.75), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartialOneActionFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 10), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), new OneActionFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver replanSingleCongestion0(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null), null, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;

    }

    public static I_Solver replanSingleCongestion0Point5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null), 0.5, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

}
