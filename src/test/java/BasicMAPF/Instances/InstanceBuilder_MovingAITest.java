package BasicMAPF.Instances;

import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import Environment.IO_Package.IO_Manager;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.MapDimensions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;


public class InstanceBuilder_MovingAITest {

    InstanceBuilder_MovingAI instanceBuilder_movingAI = new InstanceBuilder_MovingAI();


    @Test
    /*  Must create the instance properly   */
    public void prepareInstance_16_0_7(){

        String instanceName = "Instances\\MovingAI\\Instance-16-0-7";
        String expectedMapPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP});
        String expectedScenarioPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP + InstanceBuilder_MovingAI.FILE_TYPE_SCENARIO});
        InstanceManager.Moving_AI_Path expectedMovingAiPath = new InstanceManager.Moving_AI_Path(expectedMapPath,expectedScenarioPath);

        InstanceProperties properties = new InstanceProperties(new MapDimensions(new int[]{16,16}),(float)-1, new int[]{5});
        this.instanceBuilder_movingAI.prepareInstances("Default name", expectedMovingAiPath, properties);

        // Check that instance was created successfully and added to the list
        MAPF_Instance nextInstance = this.instanceBuilder_movingAI.getNextExistingInstance();
        assertNotNull(nextInstance);



    }



    @Test
    /*  Must have files in resources: resources\\BasicCBS.Instances\\MovingAI\\Instance-16-0-7.map    */
    public void getInstancesPath_16_0_7() {

        String instanceName = "Instances\\MovingAI\\Instance-16-0-7";
        String expectedMapPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP});
        String expectedScenarioPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP + InstanceBuilder_MovingAI.FILE_TYPE_SCENARIO});
        InstanceManager.Moving_AI_Path expectedMovingAiPath = new InstanceManager.Moving_AI_Path(expectedMapPath,expectedScenarioPath);

        InstanceManager.InstancePath[] instancePaths = instanceBuilder_movingAI.getInstancesPaths(IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory,"Instances\\MovingAI"}));

        for (InstanceManager.InstancePath path :instancePaths ) {
            InstanceManager.Moving_AI_Path moving_ai_path = (InstanceManager.Moving_AI_Path) path;
            if (moving_ai_path.equals(expectedMovingAiPath)){
                assertTrue(true); // Found the expected path
                break;
            }
        }
    }



    @Test
    public void twelveAndTwoAgentsOverTwoBatches_8RoomMap(){


        /*  = Expected Values =  */
        List<Agent> expectedFirstAgentList = new ArrayList<>();
        /**
         92	370	87	372
         500	366	497	371
         50	322	55	324
         114	387	121	388
         109	85	113	84
         366	492	366	485
         487	230	483	228
         437	433	441	437
         402	301	401	306
         369	94	365	92
         277	348	269	349
         431	413	436	412
         */
        expectedFirstAgentList.add(new Agent( 0  , new Coordinate_2D(92, 370)  , new Coordinate_2D(87, 372)));
        expectedFirstAgentList.add(new Agent( 1  , new Coordinate_2D(500, 366) , new Coordinate_2D(497, 371)));
        expectedFirstAgentList.add(new Agent( 2  , new Coordinate_2D(50, 322)  , new Coordinate_2D(55, 324)));
        expectedFirstAgentList.add(new Agent( 3  , new Coordinate_2D(114, 387)  , new Coordinate_2D(121, 388)));
        expectedFirstAgentList.add(new Agent( 4  , new Coordinate_2D(109, 85)  , new Coordinate_2D(113, 84)));
        expectedFirstAgentList.add(new Agent( 5  , new Coordinate_2D(366, 492)  , new Coordinate_2D(366, 485)));
        expectedFirstAgentList.add(new Agent( 6  , new Coordinate_2D(487, 230)  , new Coordinate_2D(483, 228)));
        expectedFirstAgentList.add(new Agent( 7  , new Coordinate_2D(437, 433)  , new Coordinate_2D(441, 437)));
        expectedFirstAgentList.add(new Agent( 8  , new Coordinate_2D(402, 301)  , new Coordinate_2D(401, 306)));
        expectedFirstAgentList.add(new Agent( 9  , new Coordinate_2D(369, 94)  , new Coordinate_2D(365, 92)));
        expectedFirstAgentList.add(new Agent( 10 , new Coordinate_2D(277, 348)  , new Coordinate_2D(269, 349)));
        expectedFirstAgentList.add(new Agent( 11 , new Coordinate_2D(431, 413)  , new Coordinate_2D(436, 412)));


        List<Agent> expectedSecondAgentList = new ArrayList<>();
        /**
         370	133	368	142
         437	42	442	30
         */
        expectedSecondAgentList.add(new Agent( 0  , new Coordinate_2D(370, 133)  , new Coordinate_2D(368, 142)));
        expectedSecondAgentList.add(new Agent( 1  , new Coordinate_2D(437, 42) , new Coordinate_2D(442, 30)));




        String instanceName = "Instances\\MovingAI\\8room_000";
        String expectedMapPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP});
        String expectedScenarioPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP + InstanceBuilder_MovingAI.FILE_TYPE_SCENARIO});
        InstanceManager.Moving_AI_Path expectedMovingAiPath = new InstanceManager.Moving_AI_Path(expectedMapPath,expectedScenarioPath);

        InstanceProperties properties = new InstanceProperties(new MapDimensions(new int[]{512,512}),(float)-1, new int[]{12,2});
        this.instanceBuilder_movingAI.reuseAgents = false;
        this.instanceBuilder_movingAI.prepareInstances("8Room_map", expectedMovingAiPath,properties);


        /*  =   Actual Values   = */
        MAPF_Instance firstInstance = this.instanceBuilder_movingAI.getNextExistingInstance();
        MAPF_Instance secondInstance = this.instanceBuilder_movingAI.getNextExistingInstance();


        List<Agent> actualFirstAgentList = firstInstance.agents;
        List<Agent> actualSecondAgentList = secondInstance.agents;


        /*  = Test Actual Values   = */
        assertEquals(expectedFirstAgentList, actualFirstAgentList);
        assertEquals(expectedSecondAgentList, actualSecondAgentList);



    }
}