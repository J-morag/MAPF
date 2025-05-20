# MAPF - Multi-Agent Path Finding
## A Java implementation of several MAPF algorithms

[![CI-tests](https://github.com/J-morag/MAPF/actions/workflows/CI-tests.yml/badge.svg)](https://github.com/J-morag/MAPF_dev/actions/workflows/CI-tests.yml)
&emsp;[Continuous Benchmark](https://j-morag.github.io/MAPF/dev/bench/master/)

## Getting Started
### Running the project using the CLI

Example arguments to solve all instances in a directory, with 10 agents:<br>
`-a 10 -iDir <instances_path>`


* If compiling the code yourself:
  * Run the main function with the argument `-h` to see the available options.
* If running the code from a jar file:
  * Run the jar file with the argument `-h` to see the available options.
  * Example: `java -jar MAPF.jar -h`


The following algorithms are set to run by default: CBS and Prioritised Planning. 
Other algorithms can be selected using the `-s` flag. Use `-h` to see the available options.

The default instance format is from the [MovingAI benchmark](https://movingai.com/benchmarks/mapf/index.html).

### Running the project by modifying the code

Modify the `Main.java` file to run your experiment. Examples are provided in the `ExampleMain.java` file.

## News
* 2025–02: Added selecting solvers as a command line argument
* 2024-07: Added LaCAM algorithm
* 2024-04: Added PCS algorithm
  
## Usage Notes

* How to create a single instance
    
        In order to create a single MAPF_Instance you will need:
        1. An InstanceManager
           public InstanceManager(I_InstanceBuilder instanceBuilder)
           1.1 Instance Builders parse instance files
                examples: 'InstanceBuilder_MovingAI', 'InstanceBuilder_BGU'
  
        2. An absolute path to the file, wrapped as an InstancePath object
        
        In the Environment.RunManagers.A_RunManager class there is a static method you can call:
        public static MAPF_Instance getInstanceFromPath(InstanceManager manager, 
                                                        InstanceManager.InstancePath absolutePath)
                                                        
* How to run single\multiple Experiments
        
        In order to run experiments you can use GenericRunManager.
        Alternatively, you can create you own RunManager class that extends Environment.RunManagers.A_RunManager, 
        and implements the following methods:
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
                                     Note: default = unlimited

## Acknowledgements 
    Originally designed by Jonathan Morag and Yonatan Zax.
    Started in 2019 at the heuristic search group of the Department of Software and Information Systems Engineering, Ben-Gurion University of the Negev
    Conflict Based Search (CBS) is based on:
        Sharon, G., Stern, R., Felner, A., & Sturtevant, N. R. (2015). Conflict-based search for optimal multi-agent pathfinding. Artificial Intelligence, 219, 40-66.
        And:
        Li, Jiaoyang, et al. "New techniques for pairwise symmetry breaking in multi-agent path finding." Proceedings of the International Conference on Automated Planning and Scheduling. Vol. 30. 2020.
    Increasing Cost Tree Search (ICTS) is based on:
        Sharon, Guni, et al. "The increasing cost tree search for optimal multi-agent pathfinding." Artificial Intelligence 195 (2013): 470-495.
        And:
        Sharon, Guni, et al. "Pruning techniques for the increasing cost tree search for optimal multi-agent pathfinding." Fourth Annual Symposium on Combinatorial Search. 2011.
        ICTS implementation based on (with permission) github.com/idomarko98/CBS_ICTS
    Prioritised Planning is based on: 
        Silver, David. "Cooperative pathfinding." Proceedings of the aaai conference on artificial intelligence and interactive digital entertainment. Vol. 1. No. 1. 2005.
        And:
        Andreychuk, Anton, and Konstantin Yakovlev. "Two techniques that enhance the performance of multi-robot prioritized path planning." arXiv preprint arXiv:1805.01270 (2018).
    Large Neighborhood Search (LNS) is based on:
        Li, Jiaoyang, et al. "Anytime multi-agent path finding via large neighborhood search." Proceedings of the International Joint Conference on Artificial Intelligence (IJCAI). 2021.
    Priority Inheritance with Backtracking (PIBT) is based on:
        Okumura, Keisuke, et al. "Priority inheritance with backtracking for iterative multi-agent path finding." Artificial Intelligence 310 (2022).
    Lazy Constraints Addition Search (LaCAM) is based on:
        Okumura, Keisuke. "Improving lacam for scalable eventually optimal multi-agent pathfinding." arXiv preprint arXiv:2305.03632 (2023).
    Priority Constrained Search (PCS) is based on:
        Morag, Jonathan, et al. "Prioritised Planning with Guarantees." Proceedings of the International Symposium on Combinatorial Search. Vol. 17. 2024.
    Safe Interval Path Planning (SIPP) is based on:
        Phillips, Mike, and Maxim Likhachev. "Sipp: Safe interval path planning for dynamic environments." 2011 IEEE international conference on robotics and automation. IEEE, 2011.
