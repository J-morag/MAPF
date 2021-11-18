# CBS
* This is an implementation of the Conflict Based Search (CBS) algorithm, written in Java 15. 
* This project aims to create a modular implementation, to facilitate the easier development of future CBS variants.

### How to 
* How to create a single instance
    
        >> Note: An example is provided in Main.java, using the solveOneInstanceExample() method.
        
        In order to create a single MAPF_Instance you will need:
        1. An InstanceManager
           public InstanceManager(I_InstanceBuilder instanceBuilder)
           1.1 Instance Builders parse instance files
                examples: 'InstanceBuilder_MovingAI', 'InstanceBuilder_BGU'
  
        2. An absulute path to the file, wrapped as an InstancePath object
        
        In the Environment.A_RunManager class there is a static method you can call:
        public static MAPF_Instance getInstanceFromPath(InstanceManager manager, 
                                                        InstanceManager.InstancePath absolutePath)
                                                        
* How to run single\multiple Experiments    
        
        >> Note: An example is provided in Main.java, using the runMultipleExperimentsExample() method.
        
        In order to run experiments you will need:
        RunManager class that extends Environment.A_RunManager and implements the following
        1.  abstract void setSolvers(); // choose solvers to add (one or more)
            Example: solvers.add(new CBS_Solver())
            
        2.  abstract void setExperiments(); // choose experiments
            
            * You can view or modify the default report fields in method 'setReport'. 
            Add an Experiment class:
            public Experiment(String experimentName, InstanceManager instanceManager, int numOfInstances)
                2.1 Experiment name: give it a unique name, to differentiate between experiment
                2.2 An InstanceManager       
                    public InstanceManager(String sourceDirectory,
                                               I_InstanceBuilder instanceBuilder,
                                               InstanceProperties properties)
                    2.2.1   sourceDirectory - A path to the directory with the instances
                    2.2.2   Instance Builders  parse instance files
                            examples: 'InstanceBuilder_MovingAI', 'InstanceBuilder_BGU'
                    2.2.3   Instance Properties - In case you want to filter instances by criteria
                            Constructor:
                            @param mapSize - {@link MapDimensions} indicates the Axis lengths. 0 for unknown
                            @param obstacles - For unknown obstacles enter -1
                            @param numOfAgents - An array of different amounts of agents. 
            
                2.3 numOfInstances - You can choose how many instances you want for the experiment
                                     Note: default = Integer.MAX_VALUE

### Acknowledgements 
    Designed by Jonathan Morag and Yonatan Zax.
    Created in 2019 at the heuristic search group of the Department of Software and Information Systems Engineering, Ben-Gurion University of the Negev
    Conflict Based Search is based on:
        Sharon, G., Stern, R., Felner, A., & Sturtevant, N. R. (2015). Conflict-based search for optimal multi-agent pathfinding. Artificial Intelligence, 219, 40-66.
        And:
        Li, Jiaoyang, et al. "New techniques for pairwise symmetry breaking in multi-agent path finding." Proceedings of the International Conference on Automated Planning and Scheduling. Vol. 30. 2020.
    ICTS is based on:
        Sharon, Guni, et al. "The increasing cost tree search for optimal multi-agent pathfinding." Artificial Intelligence 195 (2013): 470-495.
        And:
        Sharon, Guni, et al. "Pruning techniques for the increasing cost tree search for optimal multi-agent pathfinding." Fourth Annual Symposium on Combinatorial Search. 2011.
        ICTS implementation taken with permission and modified from github.com/idomarko98/CBS_ICTS

