package synercys.rts.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.TaskSetContainer;
import synercys.rts.simulator.TaskSetGenerator;
import synercys.rts.util.JsonLogExporter;
import synercys.rts.util.JsonLogLoader;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Created by cy on 1/6/2018.
 */

@CommandLine.Command(name = "rtaskgen", header = "%n@|Generate real-time task sets|@")
public class RtTaskGen implements Callable {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    @Option(names = {"-n", "--size"}, required = false, description = "The number of tasks in a task set.")
    int taskSize = 5;

    @CommandLine.Option(names = {"-i", "--in"}, required = false, description = "A file that contains task configurations.")
    String taskInputFile = "";

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file for storing schedule simulation output.")
    String outputFilePrefix = "";

    public static void main(String... args) {
        //String[] testArgs = { "-n", "1"};
        String[] testArgs = {"-n", "5", "-i", "\"sampleLogs\\\\task_config.txt\"", "-o", "\"sampleLogs\\\\jsontask_out.txt\""};
        args = testArgs;
        CommandLine.call(new RtTaskGen(), System.err, args);
    }

    @Override
    public Object call() throws Exception {
        TaskSetContainer taskSetContainer = new TaskSetContainer();
        if (taskInputFile.equalsIgnoreCase("")) {
            // Generate a task set using default values.
            TaskSetGenerator taskSetGenerator = new TaskSetGenerator();
            taskSetContainer.addTaskSet(taskSetGenerator.generate(taskSize, 1).getTaskSets().get(0));
        } else {
            // Generate task sets using settings from the input file.
            JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFile);
            ArrayList<TaskSetGenerator> taskSetGenerators = (ArrayList<TaskSetGenerator>) jsonLogLoader.getResult();

            for (TaskSetGenerator taskSetGenerator : taskSetGenerators) {
                TaskSetContainer thisTaskSetContainer = taskSetGenerator.generate();
                taskSetContainer.addTaskSets(thisTaskSetContainer.getTaskSets());
            }
        }

        if (!outputFilePrefix.equalsIgnoreCase("")) {
            JsonLogExporter logExporter = new JsonLogExporter(outputFilePrefix);

            if (taskSetContainer.size() == 1) {
                logExporter.exportSingleTaskSet(taskSetContainer.getTaskSets().get(0));
            } else {
                logExporter.exportTaskSets(taskSetContainer);
            }
        }

        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            loggerConsole.info(taskSet.toString());
        }

        return null;
    }
}
