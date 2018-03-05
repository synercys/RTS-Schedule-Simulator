package synercys.rts.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine;
import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.QuickFPSchedulerJobContainer;
import synercys.rts.simulator.QuickFixedPrioritySchedulerSimulator;
import synercys.rts.util.ExcelLogHandler;
import synercys.rts.util.LogExporter;
import synercys.rts.util.LogLoader;

import java.util.concurrent.Callable;

/**
 * Created by cy on 2/19/2018.
 */
@CommandLine.Command(name = "rtsim", header = "%n@|Simulate RT schedule|@")
public class RtSim implements Callable {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    protected String[] args;

    @CommandLine.Option(names = {"-i", "--in"}, required = true, description = "A file that contains task configurations.")
    String taskInputFile = "";

    @CommandLine.Option(names = {"-o", "--out"}, required = false, description = "A file for storing schedule simulation output.")
    String outputFilePrefix = "";

    @CommandLine.Option(names = {"-d", "--duration"}, required = true, description = "Simulation duration in 0.1ms (e.g., 10 is 1ms).")
    long simDuration = 0;

    @CommandLine.Option(names = {"-b", "--bibs"}, required = false, description = "Output busy intervals as binary string.")
    boolean optionGenBisBinaryString = false;


    public static void main(String... args) {
        //String[] testArgs = { "-n", "1"};
        //String[] testArgs = {"-i", "D:\\myProgram\\Java\\RTS-Schedule-Simulator\\sampleLogs\\tasks.txt", "-d", "1000"};
        String[] testArgs = {"-i", "sampleLogs\\1tasks.txt", "-o", "sampleLogs\\1task_out.txt", "-d", "10000"};
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

        // New and configure a RM scheduling simulator.
        QuickFixedPrioritySchedulerSimulator rmSimulator = new QuickFixedPrioritySchedulerSimulator();
        rmSimulator.setTaskSet(taskSet);

        //for (int i=1; i<=3; i++) {
        //    taskSet.getOneTaskByPriority(2+i).setSporadicTask(true);
        //}

        // Pre-schedule
        QuickFPSchedulerJobContainer simJobContainer = rmSimulator.preSchedule(simDuration);

        // Run simulation.
        rmSimulator.simJobs(simJobContainer);
        EventContainer eventContainer = rmSimulator.getSimEventContainer();

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

        if (!outputFilePrefix.equalsIgnoreCase("")) {
            LogExporter logExporter = new LogExporter();
            logExporter.openToWriteFile(outputFilePrefix);
            logExporter.exportBusyIntervalsBinaryString(biEvents);
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
