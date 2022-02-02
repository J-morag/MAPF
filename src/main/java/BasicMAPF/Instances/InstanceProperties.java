package BasicMAPF.Instances;

import BasicMAPF.Instances.Maps.MapDimensions;

public class InstanceProperties {

    public final MapDimensions mapSize;
    public final ObstacleWrapper obstacles;
    public final int[] numOfAgents;


    public InstanceProperties() {
        this.mapSize = new MapDimensions();
        this.obstacles = new ObstacleWrapper();
        this.numOfAgents = new int[0];
    }

    public InstanceProperties(MapDimensions.Enum_mapOrientation enumMapOrientation){
        this();
        this.mapSize.setMapOrientation(enumMapOrientation);
    }


    /***
     * Properties constructor
     * @param mapSize - {@link MapDimensions} indicates the Axis lengths , zero for unknown
     * @param obstacleRate - A double that indicates the obstacle rate of the map. For unknown obstacles enter -1
     * @param numOfAgents - An array of different num of agents.
     */
    public InstanceProperties(MapDimensions mapSize, double obstacleRate, int[] numOfAgents) {
        this.mapSize = (mapSize == null ? new MapDimensions() :  mapSize);
        this.obstacles = (obstacleRate == -1 ? new ObstacleWrapper() : new ObstacleWrapper(obstacleRate));
        this.numOfAgents = (numOfAgents == null ? new int[0] : numOfAgents);
    }



    public class ObstacleWrapper {

        public static final double DEFAULT_OBSTACLE_RATE = -1;
        private double wantedObstacleRate = DEFAULT_OBSTACLE_RATE;
        private double minRate = 0;
        private double maxRate = 1;

        /* Rate for the report:
            {@link #reportRate} is the map's rate, it will be updated from
            {@link I_InstanceBuilder#build_2D_locationTypeMap(Character[][], HashMap, MapDimensions.Enum_mapOrientation, ObstacleWrapper)}
         */
        private double reportRate  = DEFAULT_OBSTACLE_RATE; // this we be updated from

        public ObstacleWrapper(){}

        public ObstacleWrapper(double rate){
            this.setWithRate(rate);
        }

        public ObstacleWrapper(int percentage){
            this.setWithPercentage(percentage);
        }

        public void setWithRate(double rate){
            this.wantedObstacleRate = rate;
        }

        public void setWithPercentage(int percentage){
            this.reportRate = (double) percentage / (double)100;
        }


        /***
         * Get Obstacle as a ratio, Like: 0.15
         * @return A double The obstacle rate in the map
         */
        public double getAsRate() {
            return this.reportRate;
        }


        /*  Min Max getters, setters */

        public boolean isValidNumOfObstacle(int boardSize, int actualNumOfObstacle){

            // Formula: floor( obstaclesRate * BoardSize) = actual numOfObstacles

            int minNumOfObstacles = (int) Math.floor((this.minRate * boardSize));
            int maxNumOfObstacles = (int) Math.floor((this.maxRate * boardSize));
            int expectedNumOfObstacles = (int) Math.floor((this.wantedObstacleRate * boardSize));
            expectedNumOfObstacles = expectedNumOfObstacles < 0 ? -1 : expectedNumOfObstacles;

            if ( actualNumOfObstacle <= maxNumOfObstacles && actualNumOfObstacle >= minNumOfObstacles){
                if( expectedNumOfObstacles == DEFAULT_OBSTACLE_RATE || expectedNumOfObstacles == actualNumOfObstacle){
                    return true;
                }
            }
            return false;
        }

        public void setMinRate(double minRate) {
            this.minRate = minRate;
        }

        public void setMaxRate(double maxRate) {
            this.maxRate = maxRate;
        }

        /***
         * Get Obstacle as a percentage, Like: 15%
         * @return An int. The obstacle percentage in the map
         */
        public int getReportPercentage() {
            if (this.reportRate == -1) { return -1; }
            return (int)Math.round(this.reportRate * 100); // Returns 15
        }
    }
}
