package LargeAgents_CBS.Solvers.LowLevel;


import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import LargeAgents_CBS.Instances.Maps.Coordinate_2D_LargeAgent;
import LargeAgents_CBS.Instances.Maps.Enum_direction;
import LargeAgents_CBS.Instances.Maps.GraphMapVertex_LargeAgents;

import java.util.*;

public class GraphLocationGroup implements I_Location {

    private GraphMapVertex_LargeAgents[][] mapCells; // 2D representation of LargeAgent
//    private GraphMapCell minNeighbors; // A cell in the group having minimal neighbors

    private List<GraphMapVertex_LargeAgents> innerCells = new ArrayList<>();
    private Queue<GraphMapVertex_LargeAgents> outerCells = new PriorityQueue<>(new Comparator<GraphMapVertex_LargeAgents>() {
        @Override
        public int compare(GraphMapVertex_LargeAgents vertex_1, GraphMapVertex_LargeAgents vertex_2) {
            if( vertex_1.getNeighbors().size() <=  vertex_2.getNeighbors().size()){
                return 1;
            }
            return -1;
        }
    });


    public GraphLocationGroup(GraphMapVertex_LargeAgents[][] mapCells) {

        this.mapCells = mapCells;
        this.addCellsToInnerOuter();
    }


    public GraphLocationGroup(GraphLocationGroup other, Enum_direction direction){

        this.mapCells = other.mapCells;

        // Change group to it's neighbor at direction
        for (int i = 0; i < this.mapCells.length; i++) {
            for (int j = 0; j < this.mapCells[i].length; j++) {
                this.mapCells[i][j] = (GraphMapVertex_LargeAgents) this.mapCells[i][j].getLocationByDirection(direction);
            }
        }
        this.addCellsToInnerOuter(); // Set Inner Outer lists
    }

    /**
     * Help method to Constructor
     * Filters cells in {@link #mapCells} to {@link #innerCells}, {@link #outerCells}
     */
    private void addCellsToInnerOuter(){

        // Filter cells to inner/outer
        for (int i = 0; i < this.mapCells.length; i++) {
            for (int j = 0; j < this.mapCells[i].length; j++) {
                if( this.mapCells[i][j] == null){ continue; } // missing cells as null
                if( i > 0 && j > 0 && i < this.mapCells.length - 1 && j < this.mapCells[i].length - 1){
                    this.innerCells.add(this.mapCells[i][j]); // inner cell
                }else{
                    this.outerCells.add(this.mapCells[i][j]); // outer cell
                }
            }
        }
    }

    @Override
    public Enum_MapCellType getType() {
        return null;
    }


    @Override
    public List<I_Location> getNeighbors() {

        List<GraphMapVertex_LargeAgents> outerCellsList = new ArrayList<>(this.outerCells);
        GraphMapVertex_LargeAgents minNeighborsCell = outerCellsList.remove(0); // the cell with the minNeighbors
        List<I_Location> validNeighborsWithAllCells = new ArrayList<>(); // Init GraphLocationGroup neighbors

        // Iterate over the minNeighborsCell directions
        for (Enum_direction direction : minNeighborsCell.getDirectionCollection()) {

            Set<GraphMapVertex_LargeAgents> innerCellsOfNeighbor = new HashSet<>(outerCellsList.size());

            // Check direction this all outer cells
            for (GraphMapVertex_LargeAgents outerCell : outerCellsList) {
                if ( outerCell.getLocationByDirection(direction) != null){
                    innerCellsOfNeighbor.add(outerCell); // if cell has a neighbor in direction
                }else {
                    // go to next direction, at least one of the outer cells doesn't have a neighbor in direction
                    break ;
                }
            }

            if( innerCellsOfNeighbor.size() == outerCellsList.size()){ // all outer cells have neighbors in direction
                validNeighborsWithAllCells.add(new GraphLocationGroup(this, direction));
            }
        }
        return validNeighborsWithAllCells;
    }





    @Override
    public I_Coordinate getCoordinate() {

        Coordinate_2D[][] coordinates = new Coordinate_2D[this.mapCells.length][this.mapCells[0].length];

        for (int i = 0; i < coordinates.length; i++) {
            for (int j = 0; j < coordinates[i].length; j++) {
                coordinates[i][j] = (Coordinate_2D) this.mapCells[i][j].getCoordinate();
            }
        }
        Coordinate_2D_LargeAgent coordinate_2D_largeAgent = new Coordinate_2D_LargeAgent(coordinates);
        return coordinate_2D_largeAgent;
    }



    @Override
    public boolean isNeighbor(I_Location other) {

        if (!(other instanceof GraphLocationGroup)){
            return false; // checks that I_Location is GraphLocationGroup
        }

        GraphLocationGroup otherGroup = (GraphLocationGroup) other;

        // imp - Check if another location is a neighbor of this group

        return false;
    }


    public List<GraphMapVertex_LargeAgents> getAllCells(){
        List<GraphMapVertex_LargeAgents> allCells = new ArrayList<>();
        allCells.addAll(this.innerCells);
        allCells.addAll(this.outerCells);
        return allCells;
    }


}
