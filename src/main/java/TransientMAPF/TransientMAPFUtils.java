package TransientMAPF;

import BasicMAPF.DataTypesAndStructures.RunParameters;
import BasicMAPF.DataTypesAndStructures.Solution;
import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.MAPF_Instance;
import BasicMAPF.Instances.Maps.Coordinates.I_Coordinate;
import BasicMAPF.Instances.Maps.I_ExplicitMap;
import BasicMAPF.Instances.Maps.I_Location;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAStarGoalCondition;
import BasicMAPF.Solvers.AStar.GoalConditions.VisitedTargetAndBlacklistAStarGoalCondition;

import java.util.*;

public class TransientMAPFUtils {
    public static Comparator<I_Location> createSeparatingVerticesComparator(Set<I_Location> separatingVerticesSet) {
        return (loc1, loc2) -> {
            boolean isLoc1SV = separatingVerticesSet.contains(loc1);
            boolean isLoc2SV = separatingVerticesSet.contains(loc2);
            if (!isLoc1SV && isLoc2SV) return -1;
            if (isLoc1SV && !isLoc2SV) return 1;
            return 0;
        };
    }

    public static Set<I_Coordinate> createSeparatingVerticesSetOfCoordinates(MAPF_Instance instance, RunParameters runParameters) {
        Set<I_Coordinate> separatingVerticesSet = new HashSet<>();
        Set<I_Location> SVSet;
        if (runParameters.separatingVertices != null) {
            SVSet = runParameters.separatingVertices;
        }
        else {
            if (instance.map instanceof I_ExplicitMap) {
                SVSet = SeparatingVerticesFinder.findSeparatingVertices((I_ExplicitMap) instance.map);
            }
            else {
                throw new IllegalArgumentException("Transient using Separating Vertices only supported for I_ExplicitMap.");
            }
        }
        for (I_Location separatingVertex : SVSet) {
            separatingVerticesSet.add(separatingVertex.getCoordinate());
        }
        return separatingVerticesSet;
    }

    public static VisitedTargetAStarGoalCondition createLowLevelGoalConditionForTransientMAPF(TransientMAPFSettings transientMAPFSettings, Set<I_Coordinate> separatingVerticesSet, List<Agent> agents, Agent currentAgent, Solution solutionSoFar) {
        Set<I_Coordinate> blackList = null;
        if (transientMAPFSettings.avoidOtherAgentsTargets()) {
            blackList = new HashSet<>();
            for (Agent agent : agents) {
                if (!agent.equals(currentAgent) && (solutionSoFar == null || !solutionSoFar.contains(agent))) {
                    blackList.add(agent.target);
                }
            }
        }
        if (transientMAPFSettings.avoidSeparatingVertices()) {
            blackList = Objects.requireNonNullElseGet(blackList, HashSet::new);
            blackList.addAll(separatingVerticesSet);
            blackList.remove(currentAgent.target);
        }

        if (blackList == null) {
            return new VisitedTargetAStarGoalCondition();
        }
        else {
            return new VisitedTargetAndBlacklistAStarGoalCondition(blackList);
        }
    }
}
