# datafusion_cdap_batch_sink_plugin
Complete example project to create a custom Google cloud datafusion (CDAP) batch sink plugin. Sourced and adapted from the documentation where there is no quickstart project.

## Batch Sink Plugin
A BatchSink plugin is used to write data in either batch or real-time data pipelines. It is used to prepare and configure the output of a batch of data from a pipeline run.

In order to implement a Batch Sink, you extend the BatchSink class. Similar to a Batch Source, you need to define the types of the KEY and VALUE that the Batch Sink will write in the Batch job and the type of object that it will accept from the previous stage (which could be either a Transformation or a Batch Source).

After defining the types, only one method is required to be implemented: prepareRun()

## Methods
### prepareRun():
Used to configure the output for each run of the pipeline. This is called by the client that will submit the job for the pipeline run.
### onRunFinish():
Used to run any required logic at the end of a pipeline run. This is called by the client that submitted the job for the pipeline run.
### configurePipeline():
Used to perform any validation on the application configuration that is required by this plugin or to create any datasets if the fieldName for a dataset is not a macro.
### initialize():
Initialize the Batch Sink. Guaranteed to be executed before any call to the plugin’s transform method. This is called by each executor of the job. For example, if the MapReduce engine is being used, each mapper will call this method.
### destroy():
Destroy any resources created by initialize. Guaranteed to be executed after all calls to the plugin’s transform method have been made. This is called by each executor of the job. For example, if the MapReduce engine is being used, each mapper will call this method.
### transform():
This method will be called for every object that is received from the previous stage. The logic inside the method will transform the object to the key-value pair expected by the Batch Sink's output format. If you don't override this method, the incoming object is set as the key and the value is set to null.