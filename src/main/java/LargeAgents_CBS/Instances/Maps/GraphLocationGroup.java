package LargeAgents_CBS.Instances.Maps;

import BasicCBS.Instances.Maps.Coordinates.Coordinate_2D;
import BasicCBS.Instances.Maps.Coordinates.I_Coordinate;
import BasicCBS.Instances.Maps.Enum_MapCellType;
import BasicCBS.Instances.Maps.I_Location;
import BasicCBS.Instances.Maps.I_Map;
import GraphMapPackage.GraphMapVertex_LargeAgents;

import java.util.*;

public class GraphLocationGroup implements I_Location {

    private GraphMapVertex_LargeAgents[][] mapCells; // 2D representation of LargeAgent
//    private GraphMapCell minNeighbors; // A cell in the group having minimal neighbors

    private List<GraphMapVertex_LargeAgents> innerCells = new ArrayList<>();
    private Queue<GraphMapVertex_LargeAgents> outerCells = new PriorityQueue<>(new Comparator<GraphMapVertex_LargeAgents>() {
        @Override
        public int compare(GraphMapVertex_LargeAgents cell_1, GraphMapVertex_LargeAgents cell_2) {
            if( cell_1.getNeighbors().size() <=  cell_2.getNeighbors().size()){
                return -1; // One has less neighbors than Two
            }
            return 1; // Two has less neighbors that One
        }
    });


    public GraphLocationGroup(Coordinate_2D[][] coordinate_2D, I_Map map) {

        GraphMapVertex_LargeAgents[][] mapCells = new GraphMapVertex_LargeAgents[coordinate_2D.length][coordinate_2D[0].length];

        for (int i = 0; i < coordinate_2D.length; i++) {
            for (int j = 0; j < coordinate_2D[i].length; j++) {
                I_Location location = map.getMapCell(coordinate_2D[i][j]);
                mapCells[i][j] = (GraphMapVertex_LargeAgents) location;
            }
        }
        this.mapCells = mapCells;
        this.addCellsToInnerOuter();
    }

    public GraphLocationGroup(GraphMapVertex_LargeAgents[][] mapCells) {
        this.mapCells = mapCells;
        this.addCellsToInnerOuter();
    }


    public GraphLocationGroup(GraphLocationGroup other, Enum_direction direction){

        this.mapCells = new GraphMapVertex_LargeAgents[other.mapCells.length][other.mapCells[0].length];

        // Change group to it's neighbor at direction
        for (int i = 0; i < this.mapCells.length; i++) {
            for (int j = 0; j < this.mapCells[i].length; j++) {
                this.mapCells[i][j] = (GraphMapVertex_LargeAgents) other.mapCells[i][j].getLocationByDirection(direction);
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


    public I_Location getCellWithMinimumNeighbors(){
        return this.outerCells.peek();
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

        List<I_Location> myNeighbors = this.getNeighbors();
        if( myNeighbors == null ){
            return false;
        }

        return myNeighbors.contains(other);

        // imp - Check if another location is a neighbor of this group
    }


    public Enum_direction getNeighborDirection(I_Location other){
        if(!(other instanceof GraphLocationGroup) || ! isNeighbor(other)){
            return null;
        }

        List<GraphMapVertex_LargeAgents> groupCells = this.getAllCells();
        List<GraphMapVertex_LargeAgents> otherCells = ((GraphLocationGroup) other).getAllCells();
        GraphMapVertex_LargeAgents hasMinNeighbors = this.outerCells.peek();
        int expectedNeighborCount = groupCells.size();

        for (I_Location neighbor : hasMinNeighbors.getNeighbors()) {

            Enum_direction direction = hasMinNeighbors.getDirection(neighbor);
            for (GraphMapVertex_LargeAgents cellInGroup : groupCells) {
                GraphMapVertex_LargeAgents neighborAtDirection = (GraphMapVertex_LargeAgents) cellInGroup.getLocationByDirection(direction);
                if(! otherCells.contains(neighborAtDirection)){
                    expectedNeighborCount = groupCells.size();
                    break;
                }
                expectedNeighborCount--;
            }

            if( expectedNeighborCount == 0 ){
                return direction;
            }

        }

        return null;
    }


    public List<GraphMapVertex_LargeAgents> getAllCells(){
        List<GraphMapVertex_LargeAgents> allCells = new ArrayList<>();
        allCells.addAll(this.innerCells);
        allCells.addAll(this.outerCells);
        return allCells;
    }

    @Override
    public boolean equals(Object other){
        if( !(other instanceof GraphLocationGroup)){
            return false;
        }

        GraphMapVertex_LargeAgents[][] myCells = this.mapCells;
        GraphMapVertex_LargeAgents[][] otherCells = ((GraphLocationGroup) other).mapCells;

        if( myCells == null || otherCells == null || myCells.length != otherCells.length){
            return false;
        }


        for (int i = 0; i < myCells.length; i++)
            for (int j = 0; j < myCells[i].length; j++)
                if( myCells[i][j] != otherCells[i][j] )
                    return false;

        // all cells are equals
        return true;

    }


}
