import BasicMAPF.Instances.InstanceBuilders.I_InstanceBuilder;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_MovingAI;
import BasicMAPF.Instances.InstanceBuilders.InstanceBuilder_Warehouse;
import BasicMAPF.Instances.InstanceManager;
import BasicMAPF.Instances.InstanceProperties;
import BasicMAPF.Instances.Maps.GraphMap;
import Environment.IO_Package.IO_Manager;
import Environment.Visualization.GridCentralityVisualizer;
import org.apache.commons.cli.*;

import java.io.File;

/**
 * Computes and visualizes the centrality of each vertex in a graph using different centrality measures.
 * Uses the JGraphT library.
 */
public class CentralityMeasures {

    public static final String STR_MAP_DIR = "mapDir";
    public static final String STR_MAP_NAME = "mapName";
    public static final String STR_MAP_FORMAT = "mapFormat";
    public static void main(String[] args) {
        Options options = new Options();

        Option instancesDirOption = Option.builder(STR_MAP_DIR).longOpt(STR_MAP_DIR)
                .argName(STR_MAP_DIR)
                .hasArg()
                .required(true)
                .desc("Set the directory (path) where map is to be found. Required.")
                .build();
        options.addOption(instancesDirOption);

        Option mapFileNameOption = Option.builder(STR_MAP_NAME).longOpt(STR_MAP_NAME)
                .argName(STR_MAP_NAME)
                .hasArg()
                .required(true)
                .desc("Will run centrality measure for this map. Required.")
                .build();
        options.addOption(mapFileNameOption);

        Option mapFormatOption = Option.builder(STR_MAP_FORMAT).longOpt(STR_MAP_FORMAT)
                .argName(STR_MAP_FORMAT)
                .hasArg()
                .required(false)
                .desc("Set the map format. Optional (default is " + Main.STR_MOVING_AI + ").")
                .build();
        options.addOption(mapFormatOption);

        CommandLine cmd;
        CommandLineParser parser = new DefaultParser();
        HelpFormatter helper = new HelpFormatter();

        try {
            String mapDir;
            I_InstanceBuilder instanceBuilder = new InstanceBuilder_MovingAI();
            String mapFileName;

            cmd = parser.parse(options, args);


            String optMapDir = cmd.getOptionValue(STR_MAP_DIR);
            System.out.println("Map Dir: " + optMapDir);
            mapDir = optMapDir;
            if (! new File(mapDir).exists()){
                System.out.printf("Could not locate the provided map dir (%s)\n", mapDir);
                System.exit(0);
            }

            String optMapName = cmd.getOptionValue(STR_MAP_NAME);
            System.out.println("Map name: " + optMapName);
            mapFileName = optMapName;

            if (cmd.hasOption(STR_MAP_FORMAT)) {
                String optMapFormat = cmd.getOptionValue(STR_MAP_FORMAT);
                System.out.println("Map Format: " + optMapFormat);
                switch (optMapFormat) {
                    case Main.STR_MOVING_AI -> instanceBuilder = new InstanceBuilder_MovingAI();
                    case Main.STR_WAREHOUSE -> instanceBuilder = new InstanceBuilder_Warehouse(null, null, null);
                    default -> {
                        System.out.printf("Unrecognized map format: %s\n", optMapFormat);
                        System.exit(0);
                    }
                }
            }
            else {
                System.out.printf("Using default map format %s\n", Main.STR_MOVING_AI);
            }

            centralityMeasuresOneMap(mapDir, mapFileName, instanceBuilder);

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            helper.printHelp("Usage:", options, true);
            System.exit(0);
        }
    }

    private static void centralityMeasuresOneMap(String mapDir, String mapFileName, I_InstanceBuilder instanceBuilder) {
        String mapPath = IO_Manager.buildPath( new String[]{mapDir, mapFileName});
        GraphMap graphMap;
        if (instanceBuilder instanceof InstanceBuilder_MovingAI instanceBuilderMovingAI){
            graphMap = instanceBuilderMovingAI.getMap(new InstanceManager.Moving_AI_Path(mapPath, null), new InstanceProperties());
            GridCentralityVisualizer.computeCentralitiesAndVisualize(graphMap, mapFileName);
        } else if (instanceBuilder instanceof InstanceBuilder_Warehouse instanceBuilderWarehouse){
            graphMap = instanceBuilderWarehouse.getMap(new InstanceManager.Moving_AI_Path(mapPath, null), new InstanceProperties());
            GridCentralityVisualizer.computeCentralitiesAndVisualize(graphMap, mapFileName);
        }
        else {
            System.out.println("Unrecognized instance builder");
            System.exit(0);
        }
    }
}
