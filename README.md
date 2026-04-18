# MAPF - Multi-Agent Path Finding
## A system for running MAPF experiments

[![CI-tests](https://github.com/J-morag/MAPF/actions/workflows/CI-tests.yml/badge.svg)](https://github.com/J-morag/MAPF/actions/workflows/CI-tests.yml)
&emsp;[Continuous Benchmark](https://j-morag.github.io/MAPF/dev/bench/master/)

![til](./resources/images/MAPF_text_bold_splash_delay.gif)

## Getting Started

### Requirements

* JDK 18 or higher for compiling and running the main application
  * Or JRE 18 or higher for just running the jar file
* Maven 3.6+ for building the project

### Running the project using the CLI

Example arguments to solve all instances in a directory, with 10 agents:<br>
`-a 10 -iDir <instances_path>`

Other common arguments:
* `-iRegex <instance_name_regex>`
* `-resDir <results_output_dir>`
* `-s <solver_name>` to select a solver (can select multiple solvers). Use `-h` to see the list of all defined solvers. The following algorithms are set to run by default: CBS and Prioritised Planning.
* `-t <timeout_milliseconds>` for each run of solver + instance in the experiment.
* `-v` to visualize the solution. Avoid when solving numerous instances.

If compiling the code yourself:
* Run the main function with the argument `-h` to see all available options.

If running the code from a jar file:
* Run the jar file with the argument `-h` to see the available options.
* Example: `java -jar MAPF.jar -h`

The default instance format is from the [MovingAI benchmark](https://movingai.com/benchmarks/mapf/index.html).

### Running the project by modifying the code

Use `GenericRunManager` directly in code for programmatic experiments, or extend `A_RunManager` for deeper customization. See `ExampleMain.java` for working examples and the [Usage Notes](#usage-notes) section below for details.

## News
* 2026-01: Added the Multi-agent A* (MAAStar) algorithm.
* 2025-06: Added the PaPS algorithm. PCS still exists, now as a special case of PaPS, and PFCS was added in the same manner.
* 2025-05: 
  * Assorted bug fixes and performance and functionality improvements
  * Added the LNS2 algorithm
  * Improvements to visualisation
  * Added selecting solvers as a command line argument
* 2024-10: Added Config class, support for loading arbitrary graphs instance format. Added various small performance and usability improvements/fixes.
* 2024-07: Added the LaCAM algorithm
* 2024-04: Added the PCS algorithm
  
## Usage Notes

### CLI Usage (Recommended)

The primary way to run experiments is via the command line. Example:

```
java -jar MAPF.jar -a 10,20,50 -iDir <instances_path>
```

Key options:
* `-a <nums>` – comma-separated agent counts to try, e.g. `-a 10,20,50` (required)
* `-iDir <path>` – path to the directory containing map and scenario files (required)
* `-s <solver>` – select solver(s) by name, comma-separated. Use `-h` to see available solvers. Default: CBS and Prioritised Planning.
* `-t <timeout_ms>` – timeout per run in milliseconds
* `-iRegex <regex>` – filter instances by regex pattern
* `-resDir <path>` – directory to save results (created if it doesn't exist)
* `-v` – visualize solutions (not recommended for large batch runs)
* `-h` – show full help and the list of available solvers

Run `java -jar MAPF.jar -h` for the complete list of options.

### Programmatic Usage

For programmatic experiments without modifying the CLI, use `GenericRunManager` directly:

```java
new GenericRunManager(
    instancesDir,                  // path to instances directory
    new int[]{10, 20},             // agent counts to try
    new InstanceBuilder_MovingAI(),// instance format parser
    "My Experiment",               // experiment name
    false,                         // skip remaining agent counts after a failure
    null,                          // optional regex filter on instance names
    resultsOutputDir,              // where to save results
    "myResults",                   // results file prefix
    null,                          // optional visualizer
    60_000,                        // timeout per run in milliseconds
    null                           // solver override (null = use defaults)
).runAllExperiments();
```

See `ExampleMain.java` for more usage examples, including solving a single instance and running experiments with visualization.

### Custom Instances and Deeper Modifications

* **How to create a single instance**

  To create a single `MAPF_Instance` you will need:
  1. An `InstanceManager`:
     ```java
     new InstanceManager(I_InstanceBuilder instanceBuilder)
     ```
     Instance builders parse instance files. Examples: `InstanceBuilder_MovingAI`, `InstanceBuilder_BGU`.
  2. An absolute path to the file, wrapped as an `InstanceManager.InstancePath` object.

  In `A_RunManager` there is a static helper method:
  ```java
  public static MAPF_Instance getInstanceFromPath(InstanceManager manager,
                                                  InstanceManager.InstancePath absolutePath)
  ```

* **How to create a custom RunManager**

  Extend `A_RunManager` and implement two methods:
  1. `void setSolvers()` – add one or more solvers, e.g. `solvers.add(new CBS_Solver())`
  2. `void setExperiments()` – add `Experiment` objects:
     ```java
     new Experiment(String experimentName, InstanceManager instanceManager, int numOfInstances)
     ```
     * `experimentName` – a unique name for the experiment.
     * `instanceManager`:
       ```java
       new InstanceManager(String sourceDirectory,
                           I_InstanceBuilder instanceBuilder,
                           InstanceProperties properties)
       ```
       * `sourceDirectory` – path to the directory with the instances.
       * `instanceBuilder` – parses instance files (e.g. `InstanceBuilder_MovingAI`, `InstanceBuilder_BGU`).
       * `properties` – optionally filter instances by map size, obstacle rate, or agent counts.
     * `numOfInstances` – maximum number of instances to run (default: unlimited).

  See `RunManagerSimpleExample.java` for a complete example.

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
