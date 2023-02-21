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
        A_Solver stationaryAgentsReplanSinglePartialAllowedClassic = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), 
                        true, true, null), null, null, null, null);
        stationaryAgentsReplanSinglePartialAllowedClassic.name = "stationaryAgentsReplanSinglePartialAllowedClassic";
        return stationaryAgentsReplanSinglePartialAllowedClassic;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedClassic(){
        A_Solver stationaryAgentsPrPPartialAllowedClassic = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new WidePartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPPartialAllowedClassic.name = "stationaryAgentsPrPPartialAllowedClassic";
        return stationaryAgentsPrPPartialAllowedClassic;
    }

    public static I_Solver stationaryAgentsPrPPartialAllowedRHCR5(){
        A_Solver stationaryAgentsPrPPartialAllowedRHCR5 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new WidePartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPPartialAllowedRHCR5.name = "stationaryAgentsPrPPartialAllowedRHCR5";
        return stationaryAgentsPrPPartialAllowedRHCR5;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingClassic(){
        A_Solver stationaryAgentsPrPAllOrNothingClassic = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPAllOrNothingClassic.name = "stationaryAgentsPrPAllOrNothingClassic";
        return stationaryAgentsPrPAllOrNothingClassic;
    }

    public static I_Solver stationaryAgentsPrPAllOrNothingRHCR5(){
        A_Solver stationaryAgentsPrPAllOrNothingRHCR5 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPAllOrNothingRHCR5.name = "stationaryAgentsPrPAllOrNothingRHCR5";
        return stationaryAgentsPrPAllOrNothingRHCR5;
    }

    public static I_Solver baselineRHCR_w05_h03(){
        A_Solver baselineRHCR_w05_h03 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        baselineRHCR_w05_h03.name = "baselineRHCR_w05_h03";
        return baselineRHCR_w05_h03;
    }

    public static I_Solver stationaryAgentsPrPNoPartial(){
        A_Solver stationaryAgentsPrPNoPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPNoPartial.name = "stationaryAgentsPrPNoPartial";
        return stationaryAgentsPrPNoPartial;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w5_h3(){
        A_Solver stationaryAgentsPrPNoPartialRHCR_w5_h3 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPNoPartialRHCR_w5_h3.name = "stationaryAgentsPrPNoPartialRHCR_w5_h3";
        return stationaryAgentsPrPNoPartialRHCR_w5_h3;
    }

    public static I_Solver stationaryAgentsPrPNoPartialRHCR_w5_h3_lookahead3(){
        A_Solver stationaryAgentsPrPNoPartialRHCR_w5_h3_lookahead3 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, 3);
        stationaryAgentsPrPNoPartialRHCR_w5_h3_lookahead3.name = "stationaryAgentsPrPNoPartialRHCR_w5_h3_lookahead3";
        return stationaryAgentsPrPNoPartialRHCR_w5_h3_lookahead3;
    }

    public static I_Solver stationaryAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3(){
        A_Solver stationaryAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        stationaryAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3.name = "stationaryAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3";
        return stationaryAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3;
    }

    public static I_Solver allAgentsPrPNoPartial(){
        A_Solver allAgentsPrPNoPartial = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, null), null, new DisallowedPartialSolutionsStrategy(), null, null);
        allAgentsPrPNoPartial.name = "allAgentsPrPNoPartial";
        return allAgentsPrPNoPartial;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w5_h3(){
        A_Solver allAgentsPrPNoPartialRHCR_w5_h3 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, null);
        allAgentsPrPNoPartialRHCR_w5_h3.name = "allAgentsPrPNoPartialRHCR_w5_h3";
        return allAgentsPrPNoPartialRHCR_w5_h3;
    }

    public static I_Solver allAgentsPrPNoPartialRHCR_w5_h3_lookahead3(){
        A_Solver allAgentsPrPNoPartialRHCR_w5_h3_lookahead3 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), null, 3);
        allAgentsPrPNoPartialRHCR_w5_h3_lookahead3.name = "allAgentsPrPNoPartialRHCR_w5_h3_lookahead3";
        return allAgentsPrPNoPartialRHCR_w5_h3_lookahead3;
    }

    public static I_Solver allAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3(){
        A_Solver allAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, true, 5), null, new DisallowedPartialSolutionsStrategy(), new OneActionFailPolicy(true), 3);
        allAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3.name = "allAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3";
        return allAgentsPrPNoPartialOneActionFPRHCR_w5_h3_lookahead3;
    }

    public static I_Solver stationaryAgentsPrPWidePartial(){
        A_Solver stationaryAgentsPrPWidePartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new WidePartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPWidePartial.name = "stationaryAgentsPrPWidePartial";
        return stationaryAgentsPrPWidePartial;
    }
    
    public static I_Solver stationaryAgentsPrPWidePartialOneActionFP(){
        A_Solver stationaryAgentsPrPWidePartialOneActionFP = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new WidePartialSolutionsStrategy(), new OneActionFailPolicy(true), null);
        stationaryAgentsPrPWidePartialOneActionFP.name = "stationaryAgentsPrPWidePartialOneActionFP";
        return stationaryAgentsPrPWidePartialOneActionFP;
    }

    public static I_Solver stationaryAgentsPrPDeepPartial(){
        A_Solver stationaryAgentsPrPDeepPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPDeepPartial.name = "stationaryAgentsPrPDeepPartial";
        return stationaryAgentsPrPDeepPartial;
    }
    
    public static I_Solver stationaryAgentsPrPDeepPartialOneActionFP(){
        A_Solver stationaryAgentsPrPDeepPartialOneActionFP = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepPartialSolutionsStrategy(), new OneActionFailPolicy(true), null);
        stationaryAgentsPrPDeepPartialOneActionFP.name = "stationaryAgentsPrPDeepPartialOneActionFP";
        return stationaryAgentsPrPDeepPartialOneActionFP;
    }

    public static I_Solver stationaryAgentsPrPOneDeepThenWidePartial(){
        A_Solver stationaryAgentsPrPOneDeepThenWidePartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new OneDeepThenWidePartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPOneDeepThenWidePartial.name = "stationaryAgentsPrPOneDeepThenWidePartial";
        return  stationaryAgentsPrPOneDeepThenWidePartial;
    }

    public static I_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartial(){
        A_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new DeepUntilFoundFullPartialSolutionsStrategy(), null, null);
        stationaryAgentsPrPDeepUntilFoundThenWidePartial.name = "stationaryAgentsPrPDeepUntilFoundThenWidePartial";
        return stationaryAgentsPrPDeepUntilFoundThenWidePartial;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial(){
        A_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null), null, null);
        stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial";
        return stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial(){
        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.75, null), null, null);
        stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial";
        return stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial(){
        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
        stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial";
        return stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial(){
        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null), null, null);
        stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial";
        return stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial(){
        A_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42), null, null);
        stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial.name = "stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial";
        return stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartial(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartial = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        stationaryAgentsPrPCutoff25PercentPartial.name = "stationaryAgentsPrPCutoff25PercentPartial";
        return stationaryAgentsPrPCutoff25PercentPartial;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartial(){
        A_Solver allAgentsPrPCutoff25PercentPartial = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        allAgentsPrPCutoff25PercentPartial.name = "allAgentsPrPCutoff25PercentPartial";
        return allAgentsPrPCutoff25PercentPartial;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w05.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w05";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w05;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w01(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w01 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 1), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w01.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w01";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w01;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5";
        return stationaryAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead5;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialRHCR_lookahead5(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialRHCRlookahead5 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        stationaryAgentsPrPCutoff25PercentPartialRHCRlookahead5.name = "stationaryAgentsPrPCutoff25PercentPartialRHCR_lookahead5";
        return stationaryAgentsPrPCutoff25PercentPartialRHCRlookahead5;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03(){
        A_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, null);
        allAgentsPrPCutoff25PercentPartialRHCR_w05_h03.name = "allAgentsPrPCutoff25PercentPartialRHCR_w05_h03";
        return allAgentsPrPCutoff25PercentPartialRHCR_w05_h03;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3(){
        A_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3.name = "allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3";
        return allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead3;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5(){
        A_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5 = new LifelongSimulationSolver(null, new AllAgentsSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 5);
        allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5.name = "allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5";
        return allAgentsPrPCutoff25PercentPartialRHCR_w05_h03_lookahead5;
    }

    public static I_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3(){
        A_Solver allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3 = new LifelongSimulationSolver(null, new AllAgentsSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), null, 3);
        allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3.name = "allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3";
        return allAgentsPrPCutoff25PercentPartialRHCR_w05_lookahead3;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFP() {
        A_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFP = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        stationaryAgentsPrPCutoff25PercentPartialOneActionFP.name = "stationaryAgentsPrPCutoff25PercentPartialOneActionFP";
        return stationaryAgentsPrPCutoff25PercentPartialOneActionFP;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05.name = "stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05";
        return stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05;
    }

    public static I_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05_h03(){
        A_Solver stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(new PeriodicSelector(3)),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, 5), null, new IndexBasedPartialSolutionsStrategy(0.25), new OneActionFailPolicy(true), null);
        stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05.name = "stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05";
        return stationaryAgentsPrPCutoff25PercentPartialOneActionFPRHCR_w05;
    }

    public static I_Solver replanSingleCongestion0(){
        A_Solver replanSingleCongestion0 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null), null, new DeepPartialSolutionsStrategy(), null, null);
        replanSingleCongestion0.name = "replanSingleCongestion0";
        return replanSingleCongestion0;

    }

    public static I_Solver replanSingleCongestion0Point5(){
        A_Solver replanSingleCongestion0Point5 = new LifelongSimulationSolver(null, new StationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
                        true, true, null), 0.5, new DeepPartialSolutionsStrategy(), null, null);
        replanSingleCongestion0Point5.name = "replanSingleCongestion0Point5";
        return replanSingleCongestion0Point5;
    }

}
