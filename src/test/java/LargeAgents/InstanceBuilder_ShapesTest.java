package LargeAgents;

import GraphMapPackage.I_InstanceBuilder;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.MAPF_Instance;
import Environment.IO_Package.IO_Manager;
import LargeAgents_CBS.Instances.InstanceBuilder_Shapes;
import org.junit.Assert;
import org.junit.Test;

public class InstanceBuilder_ShapesTest {



    I_InstanceBuilder instanceBuilder_shapes = new InstanceBuilder_Shapes();


    @Test
    /*  Must create the instance properly   */
    public void prepareInstance_CleanMap_20_20(){

        String instanceName = "Instances\\LargeAgents\\CleanMap_20_20";
        String expectedMapPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP});
        String expectedScenarioPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP + InstanceBuilder_MovingAI.FILE_TYPE_SCENARIO});
        InstanceManager.Moving_AI_Path expectedPath = new InstanceManager.Moving_AI_Path(expectedMapPath,expectedScenarioPath);

        InstanceProperties properties = new InstanceProperties();
        this.instanceBuilder_shapes.prepareInstances("CleanMap_20_20", expectedPath, properties);

        // Check that instance was created successfully and added to the list
        MAPF_Instance nextInstance = this.instanceBuilder_shapes.getNextExistingInstance();
        Assert.assertNotNull(nextInstance);
    }


    @Test
    /*  Must create the instance properly   */
    public void prepareInstance_Instance_16_0_7(){

        String instanceName = "Instances\\LargeAgents\\Instance-16-0-7";
        String expectedMapPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP});
        String expectedScenarioPath = IO_Manager.buildPath(new String[]{IO_Manager.testResources_Directory, instanceName + InstanceBuilder_MovingAI.FILE_TYPE_MAP + InstanceBuilder_MovingAI.FILE_TYPE_SCENARIO});
        InstanceManager.Moving_AI_Path expectedPath = new InstanceManager.Moving_AI_Path(expectedMapPath,expectedScenarioPath);

        InstanceProperties properties = new InstanceProperties();
        this.instanceBuilder_shapes.prepareInstances("Instance_16_0_7", expectedPath, properties);

        // Check that instance was created successfully and added to the list
        MAPF_Instance nextInstance = this.instanceBuilder_shapes.getNextExistingInstance();
        Assert.assertNotNull(nextInstance);
    }
}