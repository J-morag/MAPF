package BasicCBS.Instances;

import GraphMapPackage.I_InstanceBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class InstanceManager {

    private String sourceDirectory;
    private I_InstanceBuilder instanceBuilder;
    private InstanceProperties instanceProperties;

    private List<InstancePath> instancesPaths_list = new ArrayList<InstancePath>();
    int currentPathIndex = 0;

    public InstanceManager(String sourceDirectory,
                           I_InstanceBuilder instanceBuilder,
                           InstanceProperties properties) {

        this(sourceDirectory, instanceBuilder);

        // Set properties
        this.instanceProperties = (properties == null ? new InstanceProperties() : properties);

        this.instanceProperties.mapSize.setMapOrientation(instanceBuilder.getMapOrientation());
    }

    public InstanceManager(I_InstanceBuilder instanceBuilder){
        this.instanceBuilder = instanceBuilder;
        this.instanceProperties = new InstanceProperties(this.instanceBuilder.getMapOrientation());
    }


    public InstanceManager(String sourceDirectory, I_InstanceBuilder instanceBuilder){

        this.sourceDirectory = sourceDirectory;
        this.instanceBuilder = instanceBuilder;

        if(this.sourceDirectory != null){
            this.addInstancesPaths_toStack( this.sourceDirectory );
        }
    }


    public MAPF_Instance getSpecificInstance(InstancePath currentPath){

        String regexSeparator = "\\\\"; //  this is actually: '\\'
        String[] splitedPath = currentPath.path.split(regexSeparator);
        String instanceName = splitedPath[splitedPath.length-1];

        this.instanceBuilder.prepareInstances(instanceName, currentPath, this.instanceProperties);
        return this.instanceBuilder.getNextExistingInstance();
    }

    public MAPF_Instance getNextInstance(){
        /* Returns null in case of an error */

        // Tries to get the next Existing Instance
        MAPF_Instance nextInstance = this.instanceBuilder.getNextExistingInstance();
        while(nextInstance == null){

            if(this.instancesPaths_list.isEmpty() || this.currentPathIndex >= this.instancesPaths_list.size()){
                /* NiceToHave - create new instances */ return null;
            }

            InstancePath currentPath = this.instancesPaths_list.get(this.currentPathIndex);
            nextInstance = getSpecificInstance(currentPath);
            if(nextInstance == null){
                this.instancesPaths_list.remove(this.currentPathIndex); // Instance path is irrelevant
            } else {
                this.currentPathIndex++; // point to the next path
            }


        }

        return nextInstance;
    }


    private void addInstancesPaths_toStack(String directoryPath){
        InstancePath[] instancePaths = this.instanceBuilder.getInstancesPaths(directoryPath);
        this.instancesPaths_list.addAll(Arrays.asList(instancePaths));
    }


    public void resetPathIndex(){
        this.currentPathIndex = 0;
    }

    /***  =Instance path wrapper=  ***/

    public static class InstancePath{

        public final String path;
        public InstancePath(String path){ this.path = path; }
    }

    public static class Moving_AI_Path extends InstancePath{

        public final String scenarioPath;
        public Moving_AI_Path(String mapPath, String scenarioPath) {
            super(mapPath);
            this.scenarioPath = scenarioPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Moving_AI_Path)) return false;
            Moving_AI_Path that = (Moving_AI_Path) o;
            return Objects.equals(scenarioPath, that.scenarioPath) &&
                    Objects.equals(this.path, that.path);
        }
    }

}
