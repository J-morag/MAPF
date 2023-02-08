package LifelongMAPF.LifelongRunManagers;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Solvers.A_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.PrioritisedPlanning_Solver;
import BasicMAPF.Solvers.PrioritisedPlanning.RestartsStrategy;
import BasicMAPF.Solvers.PrioritisedPlanning.partialSolutionStrategies.*;
import LifelongMAPF.AgentSelectors.AllStationaryAgentsSubsetSelector;
import LifelongMAPF.LifelongSimulationSolver;
import org.jetbrains.annotations.NotNull;

public class LifelongRunManagerWarehouse extends A_LifelongRunManager {

    private final String warehouseMaps;
    private final Integer maxNumAgents;

    public LifelongRunManagerWarehouse(String warehouseMaps, Integer maxNumAgents) {
        this.warehouseMaps = warehouseMaps;
        this.maxNumAgents = maxNumAgents;
    }

    @Override
    public void setSolvers() {
//        A_Solver stationaryAgentsReplanSinglePartialAllowedClassic = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, true, true, null));
//        stationaryAgentsReplanSinglePartialAllowedClassic.name = "stationaryAgentsReplanSinglePartialAllowedClassic";
//        super.solvers.add(stationaryAgentsReplanSinglePartialAllowedClassic);

//        A_Solver stationaryAgentsPrPPartialAllowedClassic = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new WidePartialSolutionStrategy(), null));
//        stationaryAgentsPrPPartialAllowedClassic.name = "stationaryAgentsPrPPartialAllowedClassic";
//        super.solvers.add(stationaryAgentsPrPPartialAllowedClassic);

//        A_Solver stationaryAgentsPrPPartialAllowedRHCR5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, true, 5));
//        stationaryAgentsPrPPartialAllowedRHCR5.name = "stationaryAgentsPrPPartialAllowedRHCR5";
//        super.solvers.add(stationaryAgentsPrPPartialAllowedRHCR5);
//
//        A_Solver stationaryAgentsPrPPartialAllowedRHCR10 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, true, 10));
//        stationaryAgentsPrPPartialAllowedRHCR10.name = "stationaryAgentsPrPPartialAllowedRHCR10";
//        super.solvers.add(stationaryAgentsPrPPartialAllowedRHCR10);
//
//        A_Solver stationaryAgentsPrPPartialAllowedRHCR15 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, true, 15));
//        stationaryAgentsPrPPartialAllowedRHCR15.name = "stationaryAgentsPrPPartialAllowedRHCR15";
//        super.solvers.add(stationaryAgentsPrPPartialAllowedRHCR15);



//        A_Solver stationaryAgentsReplanSingleAllOrNothingClassic = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0), true, true, new DisallowedPartialSolutionsStrategy(), null));
//        stationaryAgentsReplanSingleAllOrNothingClassic.name = "stationaryAgentsReplanSingleAllOrNothingClassic";
//        super.solvers.add(stationaryAgentsReplanSingleAllOrNothingClassic);

//        A_Solver stationaryAgentsPrPAllOrNothingClassic = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DisallowedPartialSolutionsStrategy(), null));
//        stationaryAgentsPrPAllOrNothingClassic.name = "stationaryAgentsPrPAllOrNothingClassic";
//        super.solvers.add(stationaryAgentsPrPAllOrNothingClassic);

//        A_Solver stationaryAgentsPrPAllOrNothingRHCR5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, new DisallowedPartialSolutionsStrategy(), 5));
//        stationaryAgentsPrPAllOrNothingRHCR5.name = "stationaryAgentsPrPAllOrNothingRHCR5";
//        super.solvers.add(stationaryAgentsPrPAllOrNothingRHCR5);
//
//        A_Solver stationaryAgentsPrPAllOrNothingRHCR10 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, new DisallowedPartialSolutionsStrategy(), 10));
//        stationaryAgentsPrPAllOrNothingRHCR10.name = "stationaryAgentsPrPAllOrNothingRHCR10";
//        super.solvers.add(stationaryAgentsPrPAllOrNothingRHCR10);
//
//        A_Solver stationaryAgentsPrPAllOrNothingRHCR15 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 10, RestartsStrategy.RestartsKind.randomRestarts), true, true, new DisallowedPartialSolutionsStrategy(), 15));
//        stationaryAgentsPrPAllOrNothingRHCR15.name = "stationaryAgentsPrPAllOrNothingRHCR15";
//        super.solvers.add(stationaryAgentsPrPAllOrNothingRHCR15);



//        A_Solver baselineRHCR_w05_h03 = new LifelongSimulationSolver(null, new AllAgentsEveryPTimestepsSubsetSeletor(3),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, true, false, 5));
//        baselineRHCR_w05_h03.name = "baselineRHCR_w05_h03";
//        super.solvers.add((baselineRHCR_w05_h03));
//
//        A_Solver baselineRHCR_w10_h05 = new LifelongSimulationSolver(null, new AllAgentsEveryPTimestepsSubsetSeletor(5),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, true, false, 10));
//        baselineRHCR_w10_h05.name = "baselineRHCR_w10_h05";
//        super.solvers.add((baselineRHCR_w10_h05));
//
//        A_Solver baselineRHCR_w15_h10 = new LifelongSimulationSolver(null, new AllAgentsEveryPTimestepsSubsetSeletor(10),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, true, false, 15));
//        baselineRHCR_w15_h10.name = "baselineRHCR_w15_h10";
//        super.solvers.add((baselineRHCR_w15_h10));
//
//        A_Solver baselineRHCR_w20_h05 = new LifelongSimulationSolver(null, new AllAgentsEveryPTimestepsSubsetSeletor(5),
//                new PrioritisedPlanning_Solver(null, null, null, new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 30), true, true, false, 20));
//        baselineRHCR_w20_h05.name = "baselineRHCR_w20_h05";
//        super.solvers.add((baselineRHCR_w20_h05));



//        A_Solver stationaryAgentsPrPNoPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DisallowedPartialSolutionsStrategy(), null), null);
//        stationaryAgentsPrPNoPartial.name = "stationaryAgentsPrPNoPartial";
//        super.solvers.add(stationaryAgentsPrPNoPartial);

//        A_Solver stationaryAgentsPrPWidePartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, false, null), null, new WidePartialSolutionsStrategy());
//        stationaryAgentsPrPWidePartial.name = "stationaryAgentsPrPWidePartial";
//        super.solvers.add(stationaryAgentsPrPWidePartial);
//
//        A_Solver stationaryAgentsPrPDeepPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, false, null), null, new DeepPartialSolutionsStrategy());
//        stationaryAgentsPrPDeepPartial.name = "stationaryAgentsPrPDeepPartial";
//        super.solvers.add(stationaryAgentsPrPDeepPartial);
//
//        A_Solver stationaryAgentsPrPOneDeepThenWidePartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, false, null), null, new OneDeepThenWidePartialSolutionsStrategy());
//        stationaryAgentsPrPOneDeepThenWidePartial.name = "stationaryAgentsPrPOneDeepThenWidePartial";
//        super.solvers.add(stationaryAgentsPrPOneDeepThenWidePartial);
//
//        A_Solver stationaryAgentsPrPDeepUntilFoundThenWidePartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, false, null), null, new DeepUntilFoundFullPartialSolutionsStrategy());
//        stationaryAgentsPrPDeepUntilFoundThenWidePartial.name = "stationaryAgentsPrPDeepUntilFoundThenWidePartial";
//        super.solvers.add(stationaryAgentsPrPDeepUntilFoundThenWidePartial);


        A_Solver stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(1.0, null));
        stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial";
        super.solvers.add(stationaryAgentsPrPCutoffStochasticIndexNoWeightPartial);

        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.75, null));
        stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial";
        super.solvers.add(stationaryAgentsPrPCutoffStochasticIndex0Point75WeightPartial);

        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null));
        stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial";
        super.solvers.add(stationaryAgentsPrPCutoffStochasticIndex0Point50WeightPartial);

        A_Solver stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new StochasticIndexBasedPartialSolutionsStrategy(0.50, null));
        stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial.name = "stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial";
        super.solvers.add(stationaryAgentsPrPCutoffStochasticIndex0Point25WeightPartial);

        A_Solver stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new AdaptiveIndexPartialSolutionsStrategy(0.25, 0.1, 42));
        stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial.name = "stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial";
        super.solvers.add(stationaryAgentsPrPCutoffAdaptiveIndex25PercentInitCutoffPartial);

        A_Solver stationaryAgentsPrPCutoff25PercentPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
                new PrioritisedPlanning_Solver(null, null, null,
                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
                        true, false, null), null, new IndexBasedPartialSolutionsStrategy(0.25));
        stationaryAgentsPrPCutoff25PercentPartial.name = "stationaryAgentsPrPCutoff25PercentPartial";
        super.solvers.add(stationaryAgentsPrPCutoff25PercentPartial);

//        A_Solver stationaryAgentsPrPCutoff50PercentPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new IndexBasedPartialSolutionsStrategy(0.5), null), null);
//        stationaryAgentsPrPCutoff50PercentPartial.name = "stationaryAgentsPrPCutoff50PercentPartial";
//        super.solvers.add(stationaryAgentsPrPCutoff50PercentPartial);
//
//        A_Solver stationaryAgentsPrPCutoff75PercentPartial = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new IndexBasedPartialSolutionsStrategy(0.75), null), null);
//        stationaryAgentsPrPCutoff75PercentPartial.name = "stationaryAgentsPrPCutoff75PercentPartial";
//        super.solvers.add(stationaryAgentsPrPCutoff75PercentPartial);



//        A_Solver stationaryAgentsPrPDeepPartialCongestion0 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DeepPartialSolutionsStrategy(), null), null);
//        stationaryAgentsPrPDeepPartialCongestion0.name = "stationaryAgentsPrPDeepPartialCongestion0";
//        super.solvers.add(stationaryAgentsPrPDeepPartialCongestion0);
//
//        A_Solver stationaryAgentsPrPDeepPartialCongestion0Point5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 0.5);
//        stationaryAgentsPrPDeepPartialCongestion0Point5.name = "stationaryAgentsPrPDeepPartialCongestion0Point5.0";
//        super.solvers.add(stationaryAgentsPrPDeepPartialCongestion0Point5);
//
//        A_Solver stationaryAgentsPrPDeepPartialCongestion1 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 1.0);
//        stationaryAgentsPrPDeepPartialCongestion1.name = "stationaryAgentsPrPDeepPartialCongestion1.0";
//        super.solvers.add(stationaryAgentsPrPDeepPartialCongestion1);
//
//        A_Solver stationaryAgentsPrPDeepPartialCongestion1Point5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 1.5);
//        stationaryAgentsPrPDeepPartialCongestion1Point5.name = "stationaryAgentsPrPDeepPartialCongestion1.5";
//        super.solvers.add(stationaryAgentsPrPDeepPartialCongestion1Point5);
//
//        A_Solver stationaryAgentsPrPDeepPartialCongestion2 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.randomRestarts, 100, RestartsStrategy.RestartsKind.randomRestarts),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 2.0);
//        stationaryAgentsPrPDeepPartialCongestion2.name = "stationaryAgentsPrPDeepPartialCongestion2.0";
//        super.solvers.add(stationaryAgentsPrPDeepPartialCongestion2);
//
//        A_Solver replanSingleCongestion0 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
//                        true, true, new DeepPartialSolutionsStrategy(), null), null);
//        replanSingleCongestion0.name = "replanSingleCongestion0";
//        super.solvers.add(replanSingleCongestion0);
//
//        A_Solver replanSingleCongestion0Point5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 0.5);
//        replanSingleCongestion0Point5.name = "replanSingleCongestion0Point5";
//        super.solvers.add(replanSingleCongestion0Point5);
//
//        A_Solver replanSingleCongestion1 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 1.0);
//        replanSingleCongestion1.name = "replanSingleCongestion1";
//        super.solvers.add(replanSingleCongestion1);
//
//        A_Solver replanSingleCongestion1Point5 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 1.5);
//        replanSingleCongestion1Point5.name = "replanSingleCongestion1Point5";
//        super.solvers.add(replanSingleCongestion1Point5);
//
//        A_Solver replanSingleCongestion2 = new LifelongSimulationSolver(null, new AllStationaryAgentsSubsetSelector(),
//                new PrioritisedPlanning_Solver(null, null, null,
//                        new RestartsStrategy(RestartsStrategy.RestartsKind.none, 0),
//                        true, true, new DeepPartialSolutionsStrategy(), null), 2.0);
//        replanSingleCongestion2.name = "replanSingleCongestion2";
//        super.solvers.add(replanSingleCongestion2);

    }

    @Override
    public void setExperiments() {
        addAllMapsAndInstances(this.maxNumAgents, this.warehouseMaps);
    }

    /* = Experiments =  */

    @NotNull
    @Override
    protected String getExperimentName() {
        return "LifelongWarehouse";
    }

    @Override
    protected @NotNull I_InstanceBuilder getInstanceBuilder() {
        return new InstanceBuilder_Warehouse();
    }

    @NotNull
    @Override
    protected InstanceProperties getInstanceProperties() {
//        return new InstanceProperties(null, -1, IntStream.rangeClosed(1, maxNumAgents).toArray());
//        return new InstanceProperties(null, -1, new int[]{maxNumAgents});
        return new InstanceProperties(null, -1, new int[]{200, 250, 300, 350});
    }
}
