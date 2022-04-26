package com.alecuba16.cdap;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Macro;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.cdap.api.data.batch.Output;
import io.cdap.cdap.api.data.format.StructuredRecord;
import io.cdap.cdap.api.data.schema.Schema;
import io.cdap.cdap.api.dataset.lib.KeyValue;
import io.cdap.cdap.api.dataset.lib.FileSet;
import io.cdap.cdap.api.dataset.lib.FileSetArguments;
import io.cdap.cdap.api.dataset.lib.FileSetProperties;
import io.cdap.cdap.api.plugin.PluginConfig;
import io.cdap.cdap.etl.api.Emitter;
import io.cdap.cdap.etl.api.PipelineConfigurer;
import io.cdap.cdap.etl.api.batch.*;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapred.TextOutputFormat;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A BatchSink plugin is used to write data in either batch or real-time data pipelines. It is used to prepare and
 * configure the output of a batch of data from a pipeline run.
 * In order to implement a Batch Sink, you extend the BatchSink class. Similar to a Batch Source, you need to define the
 * types of the KEY and VALUE that the Batch Sink will write in the Batch job and the type of object that it will accept from the previous stage (which could be either a Transformation or a Batch Source).
 * After defining the types, only one method is required to be implemented: prepareRun()
 * Methods
 * prepareRun(): Used to configure the output for each run of the pipeline. This is called by the client that will
 * submit the job for the pipeline run.
 *
 * onRunFinish(): Used to run any required logic at the end of a pipeline run. This is called by the client that
 * submitted the job for the pipeline run.
 *
 * configurePipeline(): Used to perform any validation on the application configuration that is required by this plugin
 * or to create any datasets if the fieldName for a dataset is not a macro.
 *
 * initialize(): Initialize the Batch Sink. Guaranteed to be executed before any call to the plugin’s transform method.
 * This is called by each executor of the job. For example, if the MapReduce engine is being used, each mapper will call
 * this method.
 *
 * destroy(): Destroy any resources created by initialize. Guaranteed to be executed after all calls to the plugin’s
 * transform method have been made. This is called by each executor of the job. For example, if the MapReduce engine is
 * being used, each mapper will call this method.
 *
 * transform(): This method will be called for every object that is received from the previous stage. The logic inside
 * the method will transform the object to the key-value pair expected by the Batch Sink's output format. If you don't
 * override this method, the incoming object is set as the key and the value is set to null.

 * Batch Source that reads from a FileSet that has its data formatted as text.
 *
 * LongWritable is the first parameter because that is the key used by Hadoop's {@link TextInputFormat}.
 * Similarly, Text is the second parameter because that is the value used by Hadoop's {@link TextInputFormat}.
 * {@link StructuredRecord} is the third parameter because that is what the source will output.
 * All the plugins included with Hydrator operate on StructuredRecord.
 */
/**
 * Batch Sink that writes to a FileSet in text format.
 * Each record will be written as a single line, with record fields separated by a configurable separator.
 *
 * StructuredRecord is the first parameter because that is the input to the sink.
 * The second and third parameters are the key and value expected by Hadoop's {@link TextOutputFormat}.
 */
@Plugin(type = BatchSink.PLUGIN_TYPE)
@Name(TextFileSetSink.NAME)
@Description("Writes to a FileSet in text format.")
public class TextFileSetSink extends BatchSink<StructuredRecord, NullWritable, Text> {
    public static final String NAME = "TextFileSet";
    private final Conf config;

    /**
     * Config properties for the plugin.
     */
    public static class Conf extends PluginConfig {
        public static final String FILESET_NAME = "fileSetName";
        public static final String OUTPUT_DIR = "outputDir";
        public static final String FIELD_SEPARATOR = "fieldSeparator";

        // The name annotation tells CDAP what the property name is. It is optional, and defaults to the variable name.
        // Note:  only primitives (including boxed types) and string are the types that are supported
        @Name(FILESET_NAME)
        @Description("The name of the FileSet to write to.")
        private String fileSetName;

        // Macro enabled properties can be set to a placeholder value ${key} when the pipeline is deployed.
        // At runtime, the value for 'key' can be given and substituted in.
        @Macro
        @Name(OUTPUT_DIR)
        @Description("The FileSet directory to write to.")
        private String outputDir;

        @Nullable
        @Name(FIELD_SEPARATOR)
        @Description("The separator to use to join input record fields together. Defaults to ','.")
        private String fieldSeparator;

        // Use a no-args constructor to set field defaults.
        public Conf() {
            fileSetName = "";
            fieldSeparator = ",";
        }
    }

    // CDAP will pass in a config with its fields populated based on the configuration given when creating the pipeline.
    public TextFileSetSink(Conf config) {
        this.config = config;
    }

    // configurePipeline is called exactly once when the pipeline is being created.
    // Any static configuration should be performed here.
    @Override
    public void configurePipeline(PipelineConfigurer pipelineConfigurer) {
        // create the FileSet here.
        pipelineConfigurer.createDataset(config.fileSetName,
                FileSet.class,
                FileSetProperties.builder()
                        .setInputFormat(TextInputFormat.class)
                        .setOutputFormat(TextOutputFormat.class)
                        .setEnableExploreOnCreate(true)
                        .setExploreFormat("text")
                        .setExploreSchema("text string")
                        .build()
        );
    }

    // prepareRun is called before every pipeline run, and is used to configure what the input should be,
    // as well as any arguments the input should use. It is called by the client that is submitting the batch job.
    @Override
    public void prepareRun(BatchSinkContext context) throws Exception {
        Map<String, String> arguments = new HashMap<>();
        FileSetArguments.setOutputPath(arguments, config.outputDir);
        context.addOutput(Output.ofDataset(config.fileSetName, arguments));
    }

    // onRunFinish is called at the end of the pipeline run by the client that submitted the batch job.
    @Override
    public void onRunFinish(boolean succeeded, BatchSinkContext context) {
        // perform any actions that should happen at the end of the run.
    }

    // initialize is called by each job executor before any call to transform is made.
    // This occurs at the start of the batch job run, after the job has been successfully submitted.
    // For example, if mapreduce is the execution engine, each mapper will call initialize at the start of the program.
    @Override
    public void initialize(BatchRuntimeContext context) throws Exception {
        super.initialize(context);
        // create any resources required by transform()
    }

    // destroy is called by each job executor at the end of its life.
    // For example, if mapreduce is the execution engine, each mapper will call destroy at the end of the program.
    @Override
    public void destroy() {
        // clean up any resources created by initialize
    }

    @Override
    public void transform(StructuredRecord input, Emitter<KeyValue<NullWritable, Text>> emitter) throws Exception {
        StringBuilder joinedFields = new StringBuilder();
        Iterator<Schema.Field> fieldIter = input.getSchema().getFields().iterator();
        if (!fieldIter.hasNext()) {
            // shouldn't happen
            return;
        }

        Object val = input.get(fieldIter.next().getName());
        if (val != null) {
            joinedFields.append(val);
        }
        while (fieldIter.hasNext()) {
            String fieldName = fieldIter.next().getName();
            joinedFields.append(config.fieldSeparator);
            val = input.get(fieldName);
            if (val != null) {
                joinedFields.append(val);
            }
        }
        emitter.emit(new KeyValue<>(NullWritable.get(), new Text(joinedFields.toString())));
    }

}