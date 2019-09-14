package synercys.rts.scheduler.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.*;
import synercys.rts.framework.event.BusyIntervalEventContainer;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.EdfScheduler;
import synercys.rts.scheduler.FixedPriorityScheduler;
import synercys.rts.scheduler.TaskSetContainer;
import synercys.rts.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * RtSim.java
 * Purpose: A command to run real-time schedule simulation. Currently it supports the EDF and RM schedulers.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.1 - 2018, 12/23
 * @version 1.0 - 2018, 2/19
 */
@Command(name = "rtsim", versionProvider = synercys.rts.RtsConfig.class, header = "@|blue | RT Schedule Simulator | |@")
public class RtSim implements Callable {
    protected static final Logger loggerConsole = LogManager.getLogger("console");

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Display version info.")
    protected boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    protected boolean usageHelpRequested;

    @Option(names = {"-i", "--in"}, required = true, description = "A file that contains taskset parameters.")
    protected String taskInputFile = "";

    @Option(names = {"-o", "--out"}, required = false, description = "File names (including their formats) for schedule simulation output. The output format is determined by the given file extension: \".xlsx\", \".txt\", \".rtschedule\".")
    protected List<String> outputFilePathAndFormat = new ArrayList<>();

    @Option(names = {"-p", "--policy"}, required = true, description = "Scheduling policy (\"EDF\" or \"RM\").")
    protected String schedulingPolicy = "";

    @Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    protected long simDuration = 0;

    @Option(names = {"-b", "--bibs"}, required = false, description = "Output busy intervals as binary string.")
    protected boolean optionGenBisBinaryString = false;

    @Option(names = {"-l", "--ladder"}, required = false, description = "Applicable for xlsx format. Width of a ladder diagram.")
    protected long optionLadderDiagramWidth = 0;

    @Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    protected boolean optionExecutionVariation = false;

    protected TaskSet taskSet = null;
    protected EventContainer eventContainer = null;

    public static void main(String... args) {
        /* A few test command and parameters. Uncomment one to test it. */
        // args = new String[]{"-h"};
        // args = new String[]{"-i", "sampleLogs/5tasks.tasksets", "-o", "sampleLogs/5tasks_out.txt", "-o", "sampleLogs/5tasks_out.xlsx", "-l", "200", "-o", "sampleLogs/5tasks_out.rtschedule", "-d", "10000", "-p", "EDF"};
        // args = new String[]{"-i", "sampleLogs/5tasks.tasksets", "-d", "100", "-p", "EDF"};

        CommandLine commandLine = new CommandLine(new RtSim());
        try {
            commandLine.parse(args);
        } catch (MissingParameterException ex) {
            System.err.println(ex.getMessage());
            System.err.println("Use -h to see required options.");
            return;
        }
        if (commandLine.isUsageHelpRequested()) {
            commandLine.usage(System.out);
            return;
        } else if (commandLine.isVersionHelpRequested()) {
            commandLine.printVersionHelp(System.out);
            return;
        }
        CommandLine.call(new RtSim(), System.err, args);
    }

    @Override
    public Object call() throws Exception {


        if (importTaskSet() == false) {
            loggerConsole.error("Failed to import the taskset.");
            return null;
        }

        if (runScheduleSimulation() == false) {
            loggerConsole.error("Unknown scheduler: \"{}\"", schedulingPolicy);
            return null;
        }

        // Build busy intervals for ScheduLeak
        BusyIntervalEventContainer biEvents = new BusyIntervalEventContainer();
        biEvents.createBusyIntervalsFromEvents(eventContainer);
        //biEvents.removeBusyIntervalsBeforeButExcludeTimeStamp(victimTask.getInitialOffset());


        /* Output generation */
        for (int i=0; i<outputFilePathAndFormat.size(); i++) {
            String thisOutputFileName = outputFilePathAndFormat.get(i);

            /* Extract the extension name. */
            String outputExtension = "";
            int extensionNameIndex = thisOutputFileName.lastIndexOf('.');
            if (extensionNameIndex > 0)
                outputExtension = thisOutputFileName.substring(extensionNameIndex+1);

            if (outputExtension.equalsIgnoreCase("txt")) {
                loggerConsole.info("Generate output in txt format.");
                LogExporter logExporter = new LogExporter();
                logExporter.openToWriteFile(thisOutputFileName);
                if (optionGenBisBinaryString == true)
                    logExporter.exportBusyIntervalsBinaryString(biEvents);
                else
                    logExporter.exportRawScheduleString(eventContainer);
            } else if (outputExtension.equalsIgnoreCase("xlsx")) {
                loggerConsole.info("Generate output in xlsx format.");
                // Create Excel file
                ExcelLogHandler excelLogHandler = new ExcelLogHandler();
                if (optionLadderDiagramWidth > 0) {
                    // Width for the ladder diagram is specified, so output with a ladder diagram.
                    excelLogHandler.genSchedulerIntervalEventsOnLadderDiagram(eventContainer, optionLadderDiagramWidth);
                } else {
                    // Output normal schedule format in a single row.
                    excelLogHandler.genRowSchedulerIntervalEvents(eventContainer, true);
                }
                excelLogHandler.saveAndClose(thisOutputFileName);
            } else if (outputExtension.equalsIgnoreCase("rtschedule")) {
                loggerConsole.info("Generate output in rtschedule (json) format.");
                JsonLogExporter jsonLogExporter = new JsonLogExporter(thisOutputFileName);
                jsonLogExporter.exportRawSchedule(eventContainer);
            } else {
                loggerConsole.info("Invalid output extension.");
            }
        }

        loggerConsole.info(eventContainer.getAllEvents());

        return null;
    }

    protected boolean importTaskSet() {
        JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFile);
        try {
            TaskSetContainer taskSetContainer = (TaskSetContainer) jsonLogLoader.getResult();
            taskSet = taskSetContainer.getTaskSets().get(0);
        } catch (Exception e) {
            //loggerConsole.error(e);
            return false;
        }
        loggerConsole.info(taskSet.toString());
        return true;
    }

    protected boolean runScheduleSimulation() {
        if (schedulingPolicy.equalsIgnoreCase("RM")) {
            loggerConsole.info("RM selected.");
            FixedPriorityScheduler rmSimulator = new FixedPriorityScheduler(taskSet, optionExecutionVariation);
            eventContainer = rmSimulator.runSim(simDuration);
        } else if (schedulingPolicy.equalsIgnoreCase("EDF")) { // EDF
            loggerConsole.info("EDF selected.");
            EdfScheduler edfSimulator = new EdfScheduler(taskSet, optionExecutionVariation);
            eventContainer = edfSimulator.runSim(simDuration);
        } else {
            eventContainer = null;
        }

        if (eventContainer != null)
            return true;
        else
            return false;
    }
}
