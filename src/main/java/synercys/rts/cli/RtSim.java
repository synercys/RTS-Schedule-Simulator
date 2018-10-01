package synercys.rts.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.EdfSchedulerSimulator;
import synercys.rts.simulator.QuickFixedPrioritySchedulerSimulator;
import synercys.rts.util.ExcelLogHandler;
import synercys.rts.util.LogExporter;
import synercys.rts.util.LogLoader;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by cy on 2/19/2018.
 */
@CommandLine.Command(name = "rtsim", header = "%n@|Simulate RT schedule|@")
public class RtSim implements Callable {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "A file that contains task configurations.")
    String taskInputFile = "";

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "File names (including their formats) for schedule simulation output. The output format is determined by the given file extension: \".xlsx\", \".txt\".")
    List<String> outputFilePathAndFormat;

    @CommandLine.Option(names = {"-p", "--policy"}, required = true, description = "Scheduling policy (\"EDF\" or \"RM\".")
    String schedulingPolicy = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    long simDuration = 0;

    @CommandLine.Option(names = {"-b", "--bibs"}, required = false, description = "Output busy intervals as binary string.")
    boolean optionGenBisBinaryString = false;

    @CommandLine.Option(names = {"-l", "--ladder"}, required = false, description = "Applicable for xlsx format. Width of a ladder diagram.")
    long optionLadderDiagramWidth = 0;

    @CommandLine.Option(names = {"-v", "--evar"}, required = false, description = "Enable execution time variation.")
    boolean optionExecutionVariation = false;


    public static void main(String... args) {
        //String[] testArgs = { "-n", "1"};
        //String[] testArgs = {"-i", "D:\\myProgram\\Java\\RTS-Schedule-Simulator\\sampleLogs\\tasks.txt", "-d", "1000"};
        String[] testArgs = {"-i", "sampleLogs/tasks.txt", "-o", "sampleLogs/1task_out.txt", "-o", "sampleLogs/output.xlsx", "-l", "320",  "-d", "10000", "-p", "EDF"};
        args = testArgs;

        CommandLine.call(new RtSim(), System.err, args);
    }

    @Override
    public Object call() throws Exception {
        //System.out.println("Hello, " + taskInputFile);
        LogLoader logLoader = new LogLoader();

        if (!logLoader.parseLog(taskInputFile)) {
            return null;
        }

        TaskSet taskSet = logLoader.getEventContainer().getTaskSet();
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
