package BasicMAPF.Solvers.MultiAgentAStar;

import Environment.Config;

import java.util.Comparator;

public class MAAStarStateCompLexical implements Comparator<MAAStarState> {
    public static final MAAStarStateCompLexical DEFAULT_INSTANCE = new MAAStarStateCompLexical();

    @Override
    public int compare(MAAStarState o1, MAAStarState o2) {
        int numAgents = o1.locations.size();
        if (numAgents != o2.locations.size()) {
            throw new RuntimeException(this.getClass().getSimpleName() + ": should only encounter states with the same number of agents, but got " + o1.locations.size() + " and " + o2.locations.size());
        }

        for (int i = 0; i < numAgents; i++) {
            // Compare per-agent f values first (with epsilon for floating point precision issues)
            if (o1.gArr[i] + o1.hArr[i] + Config.Misc.FLOAT_EPSILON < o2.gArr[i] + o2.hArr[i]) {
                return -1;
            }
            if (o1.gArr[i] + o1.hArr[i] - Config.Misc.FLOAT_EPSILON > o2.gArr[i] + o2.hArr[i]) {
                return 1;
            }
        }
        // If f values are equal, compare h values (with epsilon for floating point precision issues)
        for (int i = 0; i < numAgents; i++) {
            if (o1.hArr[i] + Config.Misc.FLOAT_EPSILON < o2.hArr[i]) {
                return -1;
            }
            if (o1.hArr[i] - Config.Misc.FLOAT_EPSILON > o2.hArr[i]) {
                return 1;
            }
        }
        // If h values are also equal, compare id
        return Integer.compare(o1.id, o2.id);
    }
}
