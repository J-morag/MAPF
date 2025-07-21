package BasicMAPF.Solvers.ConstraintsAndConflicts;

import BasicMAPF.Instances.Agent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static BasicMAPF.TestConstants.Agents.agent33to12;
import static BasicMAPF.TestConstants.Agents.agent53to05;
import static BasicMAPF.TestConstants.Coordinates.coor14;
import static BasicMAPF.TestConstants.Coordinates.coor15;
import static BasicMAPF.TestConstants.Maps.mapEmpty;
import static org.junit.jupiter.api.Assertions.*;

class SwappingConflictTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }

    @Test
    void testEquals() {
        Agent agent1 = agent33to12;
        Agent agent2 = agent53to05;
        assertEquals(new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)),
                new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)));
        assertEquals(new SwappingConflict(agent2, agent1, 3, mapEmpty.getMapLocation(coor14), mapEmpty.getMapLocation(coor15)),
                new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)));
    }

    @Test
    void testHashCode() {
        Agent agent1 = agent33to12;
        Agent agent2 = agent53to05;
        assertEquals(new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)).hashCode(),
                new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)).hashCode());
        assertEquals(new SwappingConflict(agent2, agent1, 3, mapEmpty.getMapLocation(coor14), mapEmpty.getMapLocation(coor15)).hashCode(),
                new SwappingConflict(agent1, agent2, 3, mapEmpty.getMapLocation(coor15), mapEmpty.getMapLocation(coor14)).hashCode());
    }
}