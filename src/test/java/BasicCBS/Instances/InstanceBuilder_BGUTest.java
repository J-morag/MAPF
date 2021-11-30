package BasicCBS.Instances;

import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_BGU;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import Environment.IO_Package.IO_Manager;
import BasicCBS.Instances.Maps.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

public class InstanceBuilder_BGUTest {

    private InstanceBuilder_BGU instanceBuilderBgu = new InstanceBuilder_BGU();

    private final Enum_MapLocationType e = Enum_MapLocationType.EMPTY;
    private final Enum_MapLocationType w = Enum_MapLocationType.WALL;


    /*  Check that map is valid  */
    private boolean checkAllMapLocations(Enum_MapLocationType[][] expectedLocationTypeMap, I_Map actualMap){

        for (int xAxis_value = 0; xAxis_value < expectedLocationTypeMap.length; xAxis_value++) {
            for (int yAxis_value = 0; yAxis_value < expectedLocationTypeMap[0].length; yAxis_value++) {
                // Create coordinate
                I_Coordinate coordinate = new Coordinate_2D(xAxis_value, yAxis_value);
                // Get the relevant mapLocation
                I_Location actualMapLocation = actualMap.getMapLocation(coordinate);

                // Check that wall doesnt exists in actualMap
                if( actualMapLocation == null && expectedLocationTypeMap[xAxis_value][yAxis_value] == w){ continue; }

                // check that actualMapLocation is the same as the expectedLocationTypeMap[xAxis_value][yAxis_value]
                if( actualMapLocation != null && actualMapLocation.getType() == expectedLocationTypeMap[xAxis_value][yAxis_value]){ continue; }

                assertFalse(true);
                return false; // Invalid value
            }
        }

        return true; // All locations are valid
    }




    @Test
    public void prepareInstances_Instance_16_0_7() {

        /*  Set properties  */
       InstanceProperties instanceProperties = new InstanceProperties(
                                                                new MapDimensions(new int[]{16,16}),
                                                                (float)0,
                                                                new int[]{7,10,15}
        );


        /*  Set path  */
       String path_16_0_7 = IO_Manager.buildPath(
                                                new String[]{  IO_Manager.testResources_Directory,
                                                               "Instances\\Instance-16-0-7-0"}
       );

        InstanceManager.InstancePath instancePath_Instance_16_0_7 = new InstanceManager.InstancePath(path_16_0_7);


        /*****  =Expected values=   *****/
        List<Agent> expectedAgents = new ArrayList<Agent>(7);
        addAgents_Instance_16_0_7(expectedAgents);




        int yAxis_length = 16;
        int xAxis_length = 16;

        /*      =Create expected locationType Map=       */
        Enum_MapLocationType[][] expectedLocationTypeMap = new Enum_MapLocationType[xAxis_length][yAxis_length];

        for (int xIndex = 0; xIndex < xAxis_length; xIndex++) {
            for (int yIndex = 0; yIndex < yAxis_length; yIndex++) {

                Enum_MapLocationType locationType = Enum_MapLocationType.EMPTY;
                expectedLocationTypeMap[xIndex][yIndex] = locationType;
            }
        }


        /*****  =Actual values=   *****/

        String instanceName = "Instance-16-0-7"; // Name from the InstanceManager
        this.instanceBuilderBgu.prepareInstances(instanceName, instancePath_Instance_16_0_7, instanceProperties);
        MAPF_Instance mapf_instance = instanceBuilderBgu.getNextExistingInstance();

        assertNotNull(mapf_instance);

        List<Agent> actualAgents = mapf_instance.agents;

        /*  =Check Agents=  */
        assertEquals(actualAgents.size(), expectedAgents.size());
        for (int i = 0; i < actualAgents.size(); i++) {
            assertEquals(expectedAgents.get(i) , actualAgents.get(i));
        }


        /*  = Check map =  */
        I_Map actualMap = mapf_instance.map;
        assertTrue(checkAllMapLocations(expectedLocationTypeMap,actualMap));


    }


    private void addAgents_Instance_16_0_7(List<Agent> expectedAgents){

        /*
        Agents from file: instance-16-0-7-0
        Agent line meaning: < id > , < x_target , y_target > , < x_start , y_start >
        */


        // 0,5,2,9,7
        expectedAgents.add( new Agent(0,
                            new Coordinate_2D(9,7),
                            new Coordinate_2D(5,2)));
        // 1,1,7,10,6
        expectedAgents.add( new Agent(1,
                            new Coordinate_2D(10,6),
                            new Coordinate_2D(1,7)));
        // 2,12,10,3,1
        expectedAgents.add( new Agent(2,
                            new Coordinate_2D(3,1),
                            new Coordinate_2D(12,10)));
        // 3,4,11,13,8
        expectedAgents.add( new Agent(3,
                            new Coordinate_2D(13,8),
                            new Coordinate_2D(4,11)));
        // 4,13,6,10,1
        expectedAgents.add( new Agent(4,
                            new Coordinate_2D(10,1),
                            new Coordinate_2D(13,6)));
        // 5,1,1,15,10
        expectedAgents.add( new Agent(5,
                            new Coordinate_2D(15,10),
                            new Coordinate_2D(1,1)));
        // 6,7,7,7,11
        expectedAgents.add( new Agent(6,
                            new Coordinate_2D(7,11),
                            new Coordinate_2D(7,7)));
    }







    @Test
    public void prepareInstances_Instance_8_15_5() {

        /*  Set path  */
        String path_8_15_5 = IO_Manager.buildPath(
                                    new String[]{   IO_Manager.testResources_Directory,
                                    "Instances\\\\Instance-8-15-5-17 - hard one - cost 29 and some corridors"}
        );

        InstanceManager.InstancePath instancePath_Instance_8_15_5 = new InstanceManager.InstancePath(path_8_15_5);


        /*  Set properties  */
        InstanceProperties instanceProperties = new InstanceProperties(
                new MapDimensions(new int[]{8,8}), (float)0.15, new int[]{7,5,15}
        );





        /*****  =Expected values=   *****/
        List<Agent> expectedAgents = new ArrayList<Agent>(5);
        addAgents_Instance_8_15_5(expectedAgents);



        /*      =Create expected locationType Map=       */

        /* Note: Map from file
                ........
                ........
                ...@....
                @.......
                ........
                .......@
                @.@....@
                @..@@...

        */
        Enum_MapLocationType[][] expectedLocationTypeMap = new Enum_MapLocationType[][]{
                {e,e,e,e,e,e,e,e},
                {e,e,e,e,e,e,e,e},
                {e,e,e,w,e,e,e,e},
                {w,e,e,e,e,e,e,e},
                {e,e,e,e,e,e,e,e},
                {e,e,e,e,e,e,e,w},
                {w,e,w,e,e,e,e,w},
                {w,e,e,w,w,e,e,e},

        };


        /*****  =Actual values=   *****/

        String instanceName = "Instance-8-15-5"; // Name from the InstanceManager
        this.instanceBuilderBgu.prepareInstances(instanceName, instancePath_Instance_8_15_5, instanceProperties);
        MAPF_Instance mapf_instance = instanceBuilderBgu.getNextExistingInstance();

        assertNotNull(mapf_instance);

        List<Agent> actualAgents = mapf_instance.agents;


        /*  =Check Agents=  */
        assertEquals(actualAgents.size(), expectedAgents.size());
        for (int i = 0; i < actualAgents.size(); i++) {
            assertEquals(expectedAgents.get(i) , actualAgents.get(i));
        }


        /*  = Check map =  */
        I_Map actualMap = mapf_instance.map;
        assertTrue(checkAllMapLocations(expectedLocationTypeMap,actualMap));


    }


    private void addAgents_Instance_8_15_5(List<Agent> expectedAgents){

        /*
        Agents from file: instance-8-15-5
        Agent line meaning: < id > , < x_target , y_target > , < x_start , y_start >
        */


        // 0,7,2,5,1
        expectedAgents.add( new Agent(0,
                            new Coordinate_2D(5,1),
                            new Coordinate_2D(7,2)));
        // 1,4,5,3,5
        expectedAgents.add( new Agent(1,
                            new Coordinate_2D(3,5),
                            new Coordinate_2D(4,5)));
        // 2,7,1,7,1
        expectedAgents.add( new Agent(2,
                            new Coordinate_2D(7,1),
                            new Coordinate_2D(7,1)));
        // 3,0,7,3,1
        expectedAgents.add( new Agent(3,
                            new Coordinate_2D(3,1),
                            new Coordinate_2D(0,7)));
        // 4,5,1,2,5
        expectedAgents.add( new Agent(4,
                            new Coordinate_2D(2,5),
                            new Coordinate_2D(5,1)));
    }



    @Test
    public void nullProperties(){

        /*  =   Set Path   =*/
        String path = IO_Manager.buildPath( new String[]{   IO_Manager.testResources_Directory,
                "Instances\\\\Instance-8-15-5-17 - hard one - cost 29 and some corridors"}
        );
        InstanceManager.InstancePath instancePath = new InstanceManager.InstancePath(path);

        InstanceManager instanceManager = new InstanceManager(new InstanceBuilder_BGU());

        MAPF_Instance instance = instanceManager.getSpecificInstance(instancePath);
        assertNotNull(instance);


    }


}