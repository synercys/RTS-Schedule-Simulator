package synercys.rts.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import picocli.CommandLine.*;
import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.EdfSchedulerSimulator;
import synercys.rts.simulator.QuickFixedPrioritySchedulerSimulator;
import synercys.rts.simulator.TaskSetContainer;
import synercys.rts.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by cy on 2/19/2018.
 */
@Command(name = "rtsim", versionProvider = synercys.rts.RtsConfig.class, header = "@|blue | RT Schedule Simulator | |@")
public class RtSim implements Callable {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    @Option(names = {"-V", "--version"}, versionHelp = true, description = "Display version info.")
    boolean versionInfoRequested;

    @Option(names = {"-h", "--help"}, usageHelp = true, description = "Display this help message.")
    boolean usageHelpRequested;

    @Option(names = {"-i", "--in"}, required = true, description = "A file that contains task configurations.")
    String taskInputFile = "";

    @Option(names = {"-o", "--out"}, required = false, description = "File names (including their formats) for schedule simulation output. The output format is determined by the given file extension: \".xlsx\", \".txt\", \".rtschedule\".")
    List<String> outputFilePathAndFormat = new ArrayList<>();

    @Option(names = {"-p", "--policy"}, required = true, description = "Scheduling policy (\"EDF\" or \"RM\").")
    String schedulingPolicy = "";

    @Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    long simDuration = 0;

    @Option(names = {"-b", "--bibs"}, required = false, description = "Output busy intervals as binary string.")
    boolean optionGenBisBinaryString = false;

    @Option(names = {"-l", "--ladder"}, required = false, description = "Applicable for xlsx format. Width of a ladder diagram.")
    long optionLadderDiagramWidth = 0;

    @Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    boolean optionExecutionVariation = false;


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

        TaskSet taskSet;
        JsonLogLoader jsonLogLoader = new JsonLogLoader(taskInputFile);
        try {
            TaskSetContainer taskSetContainer = (TaskSetContainer) jsonLogLoader.getResult();
            taskSet = taskSetContainer.getTaskSets().get(0);
        } catch (Exception e) {
            loggerConsole.error(e);
            return null;
        }
        loggerConsole.info(taskSet.toString());

        EventContainer eventContainer;
        if (schedulingPolicy.equalsIgnoreCase("RM")) {
            loggerConsole.info("RM selected.");

            // New and configure a RM scheduling simulator.
            QuickFixedPrioritySchedulerSimulator rmSimulator = new QuickFixedPrioritySchedulerSimulator(taskSet);
            rmSimulator.setRunTimeVariation(optionExecutionVariation); // it is by default ON.

            //for (int i=1; i<=3; i++) {
            //    taskSet.getOneTaskByPriority(2+i).setSporadicTask(true);
            //}

            // Run simulation.
            eventContainer = rmSimulator.runSim(simDuration);
        } else { // EDF
            loggerConsole.info("EDF selected.");
            EdfSchedulerSimulator edfSimulator = new EdfSchedulerSimulator(taskSet);
            edfSimulator.setRunTimeVariation(optionExecutionVariation); // it is by default ON.
            eventContainer = edfSimulator.runSim(simDuration);
        }

        // Build busy intervals for ScheduLeak
        BusyIntervalEventContainer biEvents = new BusyIntervalEventContainer();
        biEvents.createBusyIntervalsFromEvents(eventContainer);
        //biEvents.removeBusyIntervalsBeforeButExcludeTimeStamp(victimTask.getInitialOffset());

        // Get only observable busy intervals
//        BusyIntervalEventContainer observedBiEvents =
//                null;
//        try {
//            observedBiEvents = new BusyIntervalEventContainer( biEvents.getObservableBusyIntervalsByTask(observerTask) );
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

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
                    excelLogHandler.genRowSchedulerIntervalEvents(eventContainer);
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


        // Create Excel file
        //ExcelLogHandler excelLogHandler = new ExcelLogHandler();

        //excelLogHandler.genRowSchedulerIntervalEvents(eventContainer);
        //excelLogHandler.genRowBusyIntervals(biEvents);
        //excelLogHandler.genRowBusyIntervals(observedBiEvents);
        //excelLogHandler.genRowSchedulerIntervalEvents(decomposedEvents);

        // Output inferred arrival window
        /*
        EventContainer arrivalWindowEventContainer = new EventContainer();
        for (SchedulerIntervalEvent thisEvent : arrivalWindow.getArrivalWindowEventByTime(0)) {
            arrivalWindowEventContainer.add(thisEvent);
        }
        excelLogHandler.genRowSchedulerIntervalEvents(arrivalWindowEventContainer);
        */

        //excelLogHandler.saveAndClose(null);

        //loggerConsole.info("The number of observed BIs: " + observedBiEvents.size());

        loggerConsole.info(eventContainer.getAllEvents());

        return null;
    }
}
