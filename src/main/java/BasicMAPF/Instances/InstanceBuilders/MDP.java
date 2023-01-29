package BasicMAPF.Instances.InstanceBuilders;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MDP {

    public final Set<String> states;

    private final Map<String, Map<String, Double>> transitionProbabilities;

    public MDP(@NotNull Map<String, Map<String, Double>> transitionProbabilities) {
        validate(transitionProbabilities);
        this.states = Collections.unmodifiableSet(transitionProbabilities.keySet());
        this.transitionProbabilities = transitionProbabilities;
    }

    private void validate(Map<String, Map<String, Double>> transitionProbabilities) {
        for (Map.Entry<String, Map<String, Double>> entry:
             transitionProbabilities.entrySet()) {
            String s1 = entry.getKey();
            Map<String, Double> transitions = entry.getValue();
            double sumProbas = 0;
            for (Map.Entry<String, Double> transition :
                    transitions.entrySet()) {
                String s2 = entry.getKey();
                if ( ! this.states.contains(s2)){
                    throw new IllegalArgumentException("all states must be contained in top level");
                }
                double transitionProba = transition.getValue();
                sumProbas += transitionProba;
            }
            if (sumProbas > 1.01 || sumProbas < 0.99){
                throw new IllegalArgumentException("transition probabilities for each state must sum to 1");
            }
        }
    }


}
