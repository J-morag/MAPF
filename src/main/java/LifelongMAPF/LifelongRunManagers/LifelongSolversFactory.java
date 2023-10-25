package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Solvers.AStar.SingleAgentAStar_Solver;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.I_Solver;
import BasicMAPF.Solvers.PIBT.PIBT_Solver;
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
                        true, true, null, null, null), null, null, null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new WidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingClassic(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingRHCR5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w05_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 5, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w10_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 15, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w15_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 15, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h05(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(5)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver baselineRHCR_w20_h10(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(10)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 20, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, 
                new DisallowedPartialSolutionsStrategy(), null, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new AvoidFailPolicy(true), 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPNoPartialAvoidFPRHCR_w10_h01Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new AvoidFailPolicy(true), 2, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), null, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialAvoidFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null,
                new DisallowedPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialAvoidFPRHCR_w10_h01(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPNoPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, 10, null), null, new DisallowedPartialSolutionsStrategy(), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPWidePartialAvoidFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new WidePartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }
    
    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFP(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new OneDeepThenWidePartialSolutionsStrategy(), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h01Lookahead2(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 2, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAllStayFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AllStayFailPolicy(), 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPWidePartialAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new WidePartialSolutionsStrategy(), new AvoidFailPolicy(true), 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPOneDeepThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepUntilFoundThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new DeepUntilFoundFullPartialSolutionsStrategy(), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoffStochasticIndexNoWeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.75, null), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff50PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff75PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff50PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.50), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff75PercentPartial(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.75), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 5, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), null, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w10Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCRLookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialAvoidRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPDeepPartialAvoidRHCR_w10_h01(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 5, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialAvoidFP() {
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, null, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10_h03(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new IndexBasedPartialSolutionsStrategy(0.25), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead4(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 4, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1IntegratedAvoid(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }


    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5IntegratedAvoid(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new AvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1AvoidASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new Avoid1ASFP();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }


    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5Avoid1ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new AvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new Avoid1ASFP();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialStayOnceFPLookahead1WaterfallPPRASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), null, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 1, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead5WaterfallPPRASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new AvoidFailPolicy(true);
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), null, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead6(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 6, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead7(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 7, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead8(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 8, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead9(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 9, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPRHCR_w10_h03Lookahead10(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 10, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff50PercentPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new IndexBasedPartialSolutionsStrategy(0.50), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoff75PercentPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new IndexBasedPartialSolutionsStrategy(0.75), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), new AvoidFailPolicy(true),
                3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInit0Point1IncrementPartialAvoidFPRHCR_w10_h03Lookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null, 10, null), null, 
                new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), 
                new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1(){
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1IntegratedFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new AvoidFailPolicy(true);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1IntegratedFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver WaterfallPPRASFP_lockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1Go_1ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(1);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1AvoidASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidFailPolicy();
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_2ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(2);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_3ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(3);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_3ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(3);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayFPLookahead3Go_3ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(3);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_4ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(4);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_4ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(4);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(5);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(5);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayFPLookahead1PPRGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(5), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayFPLookahead1InterruptsPPRGo_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(5), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_5FPLookahead1Go_5ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new GoASFP(5);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(5);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayFPLookahead1Go_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayFPLookahead1PPRGo_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(10), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_30ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(30);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_20FPGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new GoASFP(20);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Go_20FPAvoid_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new GoASFP(20);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_10ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(10);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_30ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(30);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_20FPAvoid_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new AvoidASFP(20);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new AvoidASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver Avoid_20FPGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new AvoidASFP(20);
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(20);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLH_1PPRGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_1(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 1;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_3(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 3;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_6(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 6;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_3PPRGo_20ASFPCapacity_6RHCR_w10_h3(){
        Integer RHCRHorizon = 10;
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        int targetsCapacity = 6;
        int selectionLookahead = 3;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, selectionLookahead, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_6RHCR_w10_h1(){
        Integer RHCRHorizon = 10;
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        int targetsCapacity = 6;
        int selectionLookahead = 1;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, selectionLookahead, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_9(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 9;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_12(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 12;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1PPRGo_20ASFPCapacity_15(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 15;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18DynamicTimeout0p75(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 0.75f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18DynamicTimeout1p0(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.0f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18DynamicTimeout1p25(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.25f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go5ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(5);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go5ASFP_Cap18_PIBT(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(5);
        PIBT_Solver pibt = new PIBT_Solver(null);

        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(replanningPeriod)),
                pibt, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go5ASFP_Cap18_PrP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(5);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));

        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go10ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go20ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(20);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Go30ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(30);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Avoid5ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new AvoidASFP(5);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Avoid10ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new AvoidASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Avoid20ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new AvoidASFP(20);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Avoid30ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new AvoidASFP(30);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Approach5ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new ApproachASFP(5);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Approach10ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new ApproachASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Approach20ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new ApproachASFP(20);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_Approach30ASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new ApproachASFP(30);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH1_WaterfallPPRASFP_Cap18_Timeout1p5(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), false, null);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver RandSelectASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new RandomASFPSelector(new I_AStarFailPolicy[]{
                new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), false, null),
                new GoASFP(5),
                new GoASFP(10),
                new GoASFP(20),
                new GoASFP(30),
                new AvoidASFP(5),
                new AvoidASFP(10),
                new AvoidASFP(20),
                new AvoidASFP(30),
                new ApproachASFP(5),
                new ApproachASFP(10),
                new ApproachASFP(20),
                new ApproachASFP(30),
        }, null);

        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.5f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18DynamicTimeout1p75(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 1.75f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver LH_1Go_10ASFPCapacity_18DynamicTimeout2p0(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        int targetsCapacity = 18;
        I_AStarFailPolicy asfpf = new GoASFP(10);
        PrioritisedPlanning_Solver prp = new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp));
        prp.dynamicAStarTimeAllocation = true;
        prp.aStarTimeAllocationFactor = 2.0f;
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                prp, null, new DeepPartialSolutionsStrategy(), fp, null, targetsCapacity);
        solver.name = new Object() {}.getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1InterruptsPPRGo_20ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPRHCR_w10_h03Lookahead5PPRGo_20ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPRHCR_w10_h03Lookahead5InterruptsPPRGo_20ASFP(){
        int replanningPeriod = 3;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = 10;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new PostProcGoASFPFactory(20), false, null);
        A_Solver solver = new LifelongSimulationSolver(null, new FailedOrStationaryAgentsSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead1Go_100ASFP(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        I_AStarFailPolicy asfpf = new GoASFP(100);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockInf_cong0p1(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                0.1, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_noLockInf(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToInfinity = false;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToInfinity, RHCRHorizon);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver StayOnceFPLookahead1WaterfallPPRASFP_lockPeriod(){
        int replanningPeriod = 1;
        I_SingleAgentFailPolicy fp = new StayFailPolicy();
        Integer RHCRHorizon = null;
        boolean requireLockableToHorizon = true;
        I_AStarFailPolicy asfpf = new PostProcessRankingAStarFP(new WaterfallPPRASFPComparatorFactory(null, null), requireLockableToHorizon, replanningPeriod);
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(replanningPeriod)),
                new PrioritisedPlanning_Solver(new SingleAgentAStar_Solver(asfpf), null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, RHCRHorizon, new FailPolicy(replanningPeriod, fp)),
                null, new DeepPartialSolutionsStrategy(), fp, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver allAgentsPrPReplanSingleAvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver stationaryAgentsPrPDeepPartialAvoidFPLookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPRHCR_w10Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 10, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPRHCR_w20Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 20, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPRHCR_w30Lookahead1(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, 30, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead3(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 3, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver AvoidFPLookahead5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), new AvoidFailPolicy(true), 5, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

    public static I_Solver replanSingleCongestion0(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), null, new DeepPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;

    }

    public static I_Solver replanSingleCongestion0Point5(){
        A_Solver solver = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null, null, null), 0.5, new DeepPartialSolutionsStrategy(), null, null, null);
        solver.name = new Object() {} .getClass().getEnclosingMethod().getName();
        return solver;
    }

}
