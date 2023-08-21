package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.*;
import LifelongMAPF.AgentSelectors.AllAgentsSelector;
import LifelongMAPF.AgentSelectors.FailedOrStationaryAgentsSelector;
import LifelongMAPF.AgentSelectors.PeriodicSelector;
import LifelongMAPF.AgentSelectors.StationaryAgentsSubsetSelector;
import LifelongMAPF.FailPolicies.*;
import LifelongMAPF.FailPolicies.AStarFailPolicies.*;
import LifelongMAPF.LifelongSimulationSolver;

public class LifelongSolversFactory {

    public static I_Solver stationaryAgentsReplanSinglePartialAllowedClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), 
                        true, true, null, null, null), null, null, null, null);
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
                        true, true, null, null, null), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new WidePartialSolutionsStrategy(), null, null);
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
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 15, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 15, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h10(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(10)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
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
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
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
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialIAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialIAvoidFPRHCR_w10_h01Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 2);
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
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
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
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
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
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialIAvoidFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialIAvoidFPRHCR_w10_h01(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialIAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 3);
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
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null);
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
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPWidePartialIAvoidFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
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
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null);
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
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
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
                        true, false, null, null, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new OneDeepThenWidePartialSolutionsStrategy(), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h01Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAllStayFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AllStayOnceFailPolicy(), 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPWidePartialIAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new WidePartialSolutionsStrategy(), new IAvoidFailPolicy(true), 5);
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
                        true, false, null, null, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null);
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
                        true, false, null, null, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null);
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
                        true, false, null, null, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 2);
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
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null);
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
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null);
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
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.75, null), null, null);
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
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
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
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
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
                        true, false, null, null, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null);
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
                        true, false, null, null, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
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
                        true, false, null, 5, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), null, 5);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
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
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialIAvoidRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialIAvoidRHCR_w10_h01(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
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
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
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
                        true, false, null, 5, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialIAvoidFP() {
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialIAvoidFPRHCR_w10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialIAvoidFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 1);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead4(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 4);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IntegratedIAvoid(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }


    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5IntegratedIAvoid(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IAvoidASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IAvoid1ASFP();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }


    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5IAvoid1ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new IAvoid1ASFP();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1WaterfallPPRASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), null, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead5WaterfallPPRASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), null, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead6(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 6);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead7(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 7);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead8(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 8);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead9(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 9);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPRHCR_w10_h03Lookahead10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 10);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff50PercentPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.50), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff75PercentPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.75), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartialIAvoidFPRHCR_w10_h03Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), new IAvoidFailPolicy(true), 2);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1(){
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IntegratedFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy(true);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1IntegratedFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1WaterfallPPRASFP_lockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_1ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(1);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IAvoidASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IAvoidFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_2ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(2);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_3ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(3);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead3IGo_3ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(3);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_4ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(4);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(5);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1PPRIGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(5), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1InterruptsPPRIGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(5), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IGo_5FPLookahead1IGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IGoASFP(5);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(5);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(10);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1PPRIGo_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(10), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1PPRIGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1InterruptsPPRIGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPRHCR_w10_h03Lookahead5PPRIGo_20ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPRHCR_w10_h03Lookahead5InterruptsPPRIGo_20ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcIGoFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead1IGo_100ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new IAvoidFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new IGoASFP(100);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockInf_cong0p1(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                0.1, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_noLockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = false;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockPeriod(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayOnceFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToHorizon = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToHorizon, replanningPeriod);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver allAgentsPrPReplanSingleIAvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialIAvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPRHCR_w10Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 10, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPRHCR_w20Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 20, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPRHCR_w30Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 30, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 3);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

    public static I_Solver IAvoidFPLookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new IAvoidFailPolicy(true), 5);
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
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null);
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
                        true, true, null, null, null), 0.5, new DeepPartialSolutionsStrategy(), null, null);
        solver.name = new Object() {}
                .getClass()
                .getEnclosingMethod()
                .getName();
        return solver;
    }

}
