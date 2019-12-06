package LargeAgents_CBS.Instances;


import BasicCBS.Instances.Agent;
import BasicCBS.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicCBS.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicCBS.Instances.InstanceManager;
import BasicCBS.Instances.InstanceProperties;
import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.GraphMap;
import BasicCBS.Instances.Maps.I_Map;
import BasicCBS.Instances.Maps.MapDimensions;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.IO_Manager;
import Environment.IO_Package.Reader;
import LargeAgents_CBS.Instances.Maps.Coordinate_2D_LargeAgent;
import LargeAgents_CBS.Instances.Maps.GraphMap_LargeAgents;
import LargeAgents_CBS.Instances.Maps.MapFactory_LargeAgents;

import java.util.HashMap;

public class InstanceBuilder_Shapes extends InstanceBuilder_MovingAI {

    protected static final MapDimensions.Enum_mapOrientation mapOrientation = MapDimensions.Enum_mapOrientation.X_HORIZONTAL_Y_VERTICAL;


    private final int INDEX_AGENT_SOURCE_XVALUE = 1;
    private final int INDEX_AGENT_SOURCE_YVALUE = 2;
    private final int INDEX_AGENT_TARGET_XVALUE = 3;
    private final int INDEX_AGENT_TARGET_YVALUE = 4;
    private final int INDEX_AGENT_SIZE_XVALUE = 5;
    private final int INDEX_AGENT_SIZE_YVALUE = 6;

    // Skip Lines
    protected final int SKIP_LINES_MAP = 1;
    protected final int SKIP_LINES_SCENARIO = 3;



    protected I_Map getMap(InstanceManager.InstancePath instancePath, InstanceProperties instanceProperties ){

        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(instancePath.path);
        if( !enum_io.equals(Enum_IO.OPENED) ){ return null; /* couldn't open the file */ }

        /*  =Init values=  */
        GraphMap_LargeAgents graphMap = null;
        MapDimensions dimensionsFromProperties = ( instanceProperties == null || instanceProperties.mapSize == null ? new MapDimensions() : instanceProperties.mapSize);
        dimensionsFromProperties.setMapOrientation(this.getMapOrientation());
        MapDimensions dimensionsFromFile = new MapDimensions();
        dimensionsFromFile.setMapOrientation(this.getMapOrientation());


        /*  =Get data from reader=  */
        String nextLine = reader.skipFirstLines(this.getSKIP_LINES_MAP()); // First line

        while ( nextLine != null ){

            if(nextLine.startsWith(this.INDICATOR_HEIGHT)){
                String[] splitedLineHeight = nextLine.split(this.SEPARATOR_DIMENSIONS);
                if( IO_Manager.isPositiveInt(splitedLineHeight[1])){
                    dimensionsFromFile.yAxis_length = Integer.parseInt(splitedLineHeight[1]);
                    dimensionsFromFile.numOfDimensions++;
                    if( dimensionsFromProperties.yAxis_length > 0
                            && dimensionsFromFile.yAxis_length != dimensionsFromProperties.yAxis_length ){
                        reader.closeFile();
                        return null; // Bad yAxis length
                    }
                }

            }else if ( nextLine.startsWith(this.INDICATOR_WIDTH) ){
                String[] splitedLineWidth = nextLine.split(this.SEPARATOR_DIMENSIONS);
                if( IO_Manager.isPositiveInt(splitedLineWidth[1])){
                    dimensionsFromFile.xAxis_length = Integer.parseInt(splitedLineWidth[1]);
                    dimensionsFromFile.numOfDimensions++;
                    if( dimensionsFromProperties.xAxis_length > 0
                            && dimensionsFromFile.xAxis_length != dimensionsFromProperties.xAxis_length ){
                        reader.closeFile();
                        return null; // Bad xAxis length
                    }
                }

            }else if( nextLine.startsWith(this.INDICATOR_MAP) ){
                String[] mapAsStrings = I_InstanceBuilder.buildMapAsStringArray(reader, dimensionsFromFile);

                // build map
                graphMap = (GraphMap_LargeAgents) buildGraphMap(mapAsStrings, this.SEPARATOR_MAP, dimensionsFromFile, super.cellTypeHashMap, instanceProperties.obstacles);
                break;
            }
            nextLine = reader.getNextLine();
        }

        reader.closeFile(); // No more data in the file
        return graphMap;
    }


    // todo - this method in I_InstanceBuilder
    static I_Map buildGraphMap(String[] mapAsStrings, String mapSeparator, MapDimensions mapDimensions, HashMap<Character, Enum_MapCellType> cellTypeHashMap, InstanceProperties.ObstacleWrapper obstacle) {
        Character[][] mapAsCharacters_2d = I_InstanceBuilder.build2D_CharacterMap(mapAsStrings,mapDimensions,mapSeparator);
        Enum_MapCellType[][] mapAsCellType_2D = I_InstanceBuilder.build_2D_cellTypeMap(mapAsCharacters_2d, cellTypeHashMap, mapDimensions.mapOrientation, obstacle);
        if( mapAsCellType_2D == null){
            return null; // Error while building the map
        }
        return MapFactory_LargeAgents.newSimple4Connected2D_GraphMap(mapAsCellType_2D);
    }



    protected Agent buildSingleAgent(int id, String agentLine) {

        // done - build agent from string
        String[] splitLine = agentLine.split(super.SEPARATOR_SCENARIO);
        // Init coordinates
        int xSize = this.getSize(splitLine, INDEX_AGENT_SIZE_XVALUE);
        int ySize = this.getSize(splitLine, INDEX_AGENT_SIZE_YVALUE);

        // Initiate source and target coordinates
        int source_xValue = Integer.parseInt(splitLine[this.INDEX_AGENT_SOURCE_XVALUE]);
        int source_yValue = Integer.parseInt(splitLine[this.INDEX_AGENT_SOURCE_YVALUE]);
        Coordinate_2D_LargeAgent source = generateLargeAgentCoordinates(source_xValue, xSize, source_yValue, ySize);

        int target_xValue = Integer.parseInt(splitLine[this.INDEX_AGENT_TARGET_XVALUE]);
        int target_yValue = Integer.parseInt(splitLine[this.INDEX_AGENT_TARGET_YVALUE]);
        Coordinate_2D_LargeAgent target = generateLargeAgentCoordinates(target_xValue, xSize, target_yValue, ySize);

        return new LargeAgent(id, source, target);
    }


    private int getSize(String[] agentLine, int index){
        // Check if index is valid and value is an positive int
        if(agentLine.length < index + 1 || ! IO_Manager.isPositiveInt(agentLine[index])){
            return 1;
        }
        return Integer.parseInt(agentLine[index]); // parse value
    }


    private Coordinate_2D_LargeAgent generateLargeAgentCoordinates(int refPoint_xValue, int xSize, int refPoint_yValue, int ySize){

        Coordinate_2D[][] coordinates = new Coordinate_2D[xSize][ySize];

        for (int xValue = 0; xValue < xSize; xValue++) {
            for (int yValue = 0; yValue < ySize; yValue++) {
                coordinates[xValue][yValue] = new Coordinate_2D(xValue + refPoint_xValue, yValue + refPoint_yValue);
            }
        }
        return new Coordinate_2D_LargeAgent(coordinates);
    }


    /*  = Skip getters =  */
    protected int getSKIP_LINES_MAP(){
        return this.SKIP_LINES_MAP;
    }

    protected int getSKIP_LINES_SCENARIO(){
        return this.SKIP_LINES_SCENARIO;
    }
}
