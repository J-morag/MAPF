package BasicMAPF.Instances.InstanceBuilders;

import BasicMAPF.Instances.Agent;
import BasicMAPF.Instances.InstanceManagerFromFileSystem;
import BasicMAPF.Instances.Maps.Coordinates.Coordinate_2D;
import BasicMAPF.Instances.Maps.GraphMap;
import Environment.IO_Package.Enum_IO;
import Environment.IO_Package.Reader;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;

public abstract class ScenarioBuilder_Warehouse {

    public static final int SKIP_LINES_SCENARIO = 1;
    public static final String SEPARATOR_SCENARIO = ",";
    public static final int INDEX_XVALUE = 1;
    public static final int INDEX_YVALUE = 2;

    public Agent[] getAgents(InstanceManagerFromFileSystem.Moving_AI_Path moving_ai_path, int numOfNeededAgents, Set<Coordinate_2D> canonicalCoordinates, GraphMap map, boolean lifelong) {
        ArrayList<ArrayList<String>> agentLinesList = getAgentLines(moving_ai_path, numOfNeededAgents);
        return getAgents(agentLinesList, numOfNeededAgents, canonicalCoordinates);
    }

    // Returns agentLines from scenario file as a queue. each entry is a list of lines (targets) for one agent.
    private ArrayList<ArrayList<String>> getAgentLines(InstanceManagerFromFileSystem.Moving_AI_Path moving_ai_path, int numOfNeededAgents) {

        // Open scenario file
        Reader reader = new Reader();
        Enum_IO enum_io = reader.openFile(moving_ai_path.scenarioPath);
        if( !enum_io.equals(Enum_IO.OPENED) ){
            reader.closeFile();
            return null; /* couldn't open the file */
        }

        /*  =Get data from reader=  */
        reader.skipFirstLines(SKIP_LINES_SCENARIO); // skip first line (header = ["agent_id", "x", "y", "tag"])

        ArrayList<ArrayList<String>> agentsLines = new ArrayList<>(); // Init queue of agents lines

        // Each agent gets a list of targets (one per line). Add line batches as the num of needed agents
        ArrayList<String> currAgentLines = new ArrayList<>();
        for (int current_agent_id = 0; current_agent_id < numOfNeededAgents;) {
            String nextLine = reader.getNextLine();
            // lines are batched per agent
            if (nextLine != null && Integer.parseInt(nextLine.split(SEPARATOR_SCENARIO, 2)[0]) == current_agent_id){
                currAgentLines.add(nextLine);
            }
            if (nextLine == null || Integer.parseInt(nextLine.split(SEPARATOR_SCENARIO, 2)[0]) != current_agent_id){
                agentsLines.add(currAgentLines);
                if (nextLine == null){
                    break;
                }
                else{
                    currAgentLines = new ArrayList<>();
                    currAgentLines.add(nextLine);
                    current_agent_id++;
                }
            }
        }

        reader.closeFile();
        return agentsLines;
    }

    // Returns an array of agents using the line queue
    private Agent[] getAgents(ArrayList<ArrayList<String>> agentLinesList, int numOfAgents, Set<Coordinate_2D> canonicalCoordinates) {
        if( agentLinesList == null){ return null; }
        agentLinesList.removeIf(Objects::isNull);
        Agent[] arrayOfAgents = new Agent[Math.min(numOfAgents,agentLinesList.size())];

        if(agentLinesList.isEmpty()){ return null; }

        // Iterate over all the agents in numOfAgents
        for (int id = 0; id < numOfAgents; id++) {

            if( id < arrayOfAgents.length ){
                Agent agentToAdd = buildSingleAgent(id ,agentLinesList.get(id), canonicalCoordinates);
                arrayOfAgents[id] =  agentToAdd; // Wanted agent to add
            }
        }
        return arrayOfAgents;
    }

    private Agent buildSingleAgent(int id, ArrayList<String> agentLines, Set<Coordinate_2D> canonicalCoordinates) {
        // take the last target as target, and the one before last as source. this approximates sampling from steady state
        String[] splitLineSource = agentLines.get(agentLines.size()-2).split(SEPARATOR_SCENARIO);
        String[] splitLineTarget = agentLines.get(agentLines.size()-1).split(SEPARATOR_SCENARIO);
        return new Agent(id, toCoor2D(splitLineSource[INDEX_XVALUE].strip(), splitLineSource[INDEX_YVALUE].strip(), canonicalCoordinates),
                toCoor2D(splitLineTarget[INDEX_XVALUE].strip(), splitLineTarget[INDEX_YVALUE].strip(), canonicalCoordinates));
    }

    public static Coordinate_2D toCoor2D(String coorString, Set<Coordinate_2D> canonicalCoordinates){
        String[] coorstrings = coorString.split("_");
        return toCoor2D(coorstrings[0], coorstrings[1], canonicalCoordinates);
    }

    public static Coordinate_2D toCoor2D(String xString, String yString, Set<Coordinate_2D> canonicalCoordinates){
        return InstanceBuilder_Warehouse.toCoor2D(Integer.parseInt(xString), Integer.parseInt(yString), canonicalCoordinates);
    }

}


