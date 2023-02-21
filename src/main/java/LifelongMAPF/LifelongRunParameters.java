package LifelongMAPF;

import BasicMAPF.Solvers.RunParameters;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class LifelongRunParameters extends RunParameters {

    /**
     * At any point in time, must not take longer than this to respond and advance the simulation time.
     */
    public final long minResponseTime;

    /**
     * Can reach, at most, this time step. IF reached and not all agents finished all destinations, return a partial solution.
     */
    public final int maxTimeSteps;

    public LifelongRunParameters(RunParameters rp, @Nullable Long minResponseTime, @Nullable Integer maxTimeSteps) {
        super(rp);
        this.minResponseTime = Objects.requireNonNullElse(minResponseTime, 1000L);
        if (this.minResponseTime < 1){
            throw new IllegalArgumentException("min response time should be at least 1ms");
        }
        this.maxTimeSteps = Objects.requireNonNullElse(maxTimeSteps, 201);
        if (this.maxTimeSteps < 1){
            throw new IllegalArgumentException("max time steps should be at least 1 time step");
        }
//        if (this.minResponseTime * this.maxTimeSteps > super.timeout){
//            throw new IllegalArgumentException("there must be enough time withing the global timeout to do minResponseTime * maxTimeSteps (in the event that we end up planning at every time step");
//        }
    }
    public LifelongRunParameters(RunParameters rp) {
        this(rp, null, null);
    }

    public LifelongRunParameters(RunParameters rp, long minResponseTime) {
        this(rp, minResponseTime, null);
    }

    public LifelongRunParameters(RunParameters rp, int maxTimeSteps) {
        this(rp, null, maxTimeSteps);
    }

}
