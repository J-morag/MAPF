package BasicMAPF.Instances;

import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.Maps.Enum_MapLocationType;
import BasicMAPF.Instances.Maps.MapDimensions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;


public class I_InstanceBuilderTest {

    @BeforeEach
    void setUp(TestInfo testInfo) {
        System.out.printf("test started: %s: %s\n", testInfo.getTestClass().isPresent() ? testInfo.getTestClass().get() : "", testInfo.getDisplayName());
    }


    /*      =Location Types=   */
    private final char EMPTY = '.';
    private final char WALL = '@';

    private HashMap<Character, Enum_MapLocationType> locationTypeHashMap = new HashMap<>(){{
        put(EMPTY, Enum_MapLocationType.EMPTY);
        put(WALL, Enum_MapLocationType.WALL);
    }};

    /*  =Expected value=    */
    Character[][] charMap_Instance_8_15_5 = new Character[][]   {
            {'.','.','.','.','.','.','.','.'},
            {'.','.','.','.','.','.','.','.'},
            {'.','.','.','@','.','.','.','.'},
            {'@','.','.','.','.','.','.','.'},
            {'.','.','.','.','.','.','.','.'},
            {'.','.','.','.','.','.','.','@'},
            {'@','.','@','.','.','.','.','@'},
            {'@','.','.','@','@','.','.','.'}
    };


    @Test
    public void build2D_CharacterMap_Instance_8_15_5(){


        String[] mapAsString = new String[] {
                                                "........",
                                                "........",
                                                "...@....",
                                                "@.......",
                                                "........",
                                                ".......@",
                                                "@.@....@",
                                                "@..@@..."

                                            };
        MapDimensions mapDimensions = new MapDimensions(new int[]{8,8}, MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL);

        Character[][] actualMap = I_InstanceBuilder.build2D_CharacterMap(mapAsString,mapDimensions,"");

        //  Check every Character in the array
        for (int i = 0; i < charMap_Instance_8_15_5.length; i++) {
            for (int j = 0; j < charMap_Instance_8_15_5[i].length; j++) {
                assertEquals(charMap_Instance_8_15_5[i][j],actualMap[i][j]);
            }
        }
    }


    @Test
    public void testObstacleCalculation_build_2D_locationTypeMap(){

        InstanceProperties properties = new InstanceProperties();
        InstanceProperties.ObstacleWrapper obstacle = properties.obstacles;
        obstacle.setMinRate(0.15);
        obstacle.setMaxRate(0.15);

        I_InstanceBuilder.build_2D_locationTypeMap( this.charMap_Instance_8_15_5, this.locationTypeHashMap, MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL, obstacle);

        assertEquals( 15, obstacle.getReportPercentage());

    }
}
