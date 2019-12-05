package BasicCBS.Solvers.ConstraintsAndConflicts.ConflictManagement;

import BasicCBS.Solvers.ConstraintsAndConflicts.A_Conflict;
import BasicCBS.Solvers.SingleAgentPlan;

public interface I_ConflictManager {

    /**
     * Removes current conflicts and other information related to the agent in the given {@link SingleAgentPlan}.
     * Adds current conflicts and other information related to the given {@link SingleAgentPlan}.
     * @param singleAgentPlan a new {@link SingleAgentPlan}. The {@link SingleAgentPlan#agent} may already have a plan
     *                        in the table.
     */
    void addPlan(SingleAgentPlan singleAgentPlan);

    /**
     * Selects the next {@link A_Conflict} that should be resolved.
     * @return the next {@link A_Conflict} that should be resolved.
     */
    A_Conflict selectConflict();

    /**
     * @return a deep copy of this class.
     */
    I_ConflictManager copy();

}
