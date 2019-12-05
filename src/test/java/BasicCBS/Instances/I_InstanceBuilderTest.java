package BasicCBS.Instances;

import BasicCBS.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.MapDimensions;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;


public class I_InstanceBuilderTest {


    /*      =Cell Types=   */
    private final char EMPTY = '.';
    private final char WALL = '@';

    private HashMap<Character, Enum_MapCellType> cellTypeHashMap = new HashMap<>(){{
        put(EMPTY,Enum_MapCellType.EMPTY);
        put(WALL,Enum_MapCellType.WALL);
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
        MapDimensions mapDimensions = new MapDimensions(8,8);

        Character[][] actualMap = I_InstanceBuilder.build2D_CharacterMap(mapAsString,mapDimensions,"");

        //  Check every Character in the array
        for (int i = 0; i < charMap_Instance_8_15_5.length; i++) {
            for (int j = 0; j < charMap_Instance_8_15_5[i].length; j++) {
                Assert.assertEquals(charMap_Instance_8_15_5[i][j],actualMap[i][j]);
            }
        }
    }


    @Test
    public void testObstacleCalculation_build_2D_cellTypeMap(){

        InstanceProperties properties = new InstanceProperties();
        InstanceProperties.ObstacleWrapper obstacle = properties.obstacles;
        obstacle.setMinRate(0.15);
        obstacle.setMaxRate(0.15);

        I_InstanceBuilder.build_2D_cellTypeMap( this.charMap_Instance_8_15_5, this.cellTypeHashMap, MapDimensions.Enum_mapOrientation.Y_HORIZONTAL_X_VERTICAL, obstacle);

        Assert.assertEquals( 15, obstacle.getReportPercentage());

    }
}
