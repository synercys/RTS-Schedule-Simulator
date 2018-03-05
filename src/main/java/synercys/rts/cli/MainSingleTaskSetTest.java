package synercys.rts.cli;

import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.simulator.QuickFPSchedulerJobContainer;
import synercys.rts.simulator.QuickFixedPrioritySchedulerSimulator;
import synercys.rts.simulator.TaskSetContainer;
import synercys.rts.simulator.TaskSetGenerator;
import synercys.rts.util.ExcelLogHandler;
import cy.utility.Umath;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

/**
 * Created by cy on 3/28/2017.
 */
public class MainSingleTaskSetTest {
    static long SIM_DURATION = 100000;

    private static final int VICTIM_PRI = 2;
    private static final int OBSERVER_PRI = 1;

    private static final Logger loggerConsole = LogManager.getLogger("console");

    public static void main(String[] args) {
        loggerConsole.info("Test starts.");

        // Generate a task set.
        TaskSetGenerator taskSetGenerator = new TaskSetGenerator();

        //taskSetGenerator.setMaxPeriod(100);
        //taskSetGenerator.setMinPeriod(50);

        //taskSetGenerator.setMaxExecTime(20);
        //taskSetGenerator.setMinExecTime(5);

        taskSetGenerator.setMaxUtil(0.6);
        taskSetGenerator.setMinUtil(0.5);

        taskSetGenerator.setNonHarmonicOnly(true);

        //taskSetGenerator.setMaxHyperPeriod(70070);
        //taskSetGenerator.setGenerateFromHpDivisors(true);

        /* Optimal attack condition experiment. */
        taskSetGenerator.setNeedGenObserverTask(true);
        taskSetGenerator.setObserverTaskPriority(OBSERVER_PRI);
        taskSetGenerator.setVictimTaskPriority(VICTIM_PRI);

        taskSetGenerator.setMaxObservationRatio(999);
        taskSetGenerator.setMinObservationRatio(1);

        TaskSetContainer taskSets = taskSetGenerator.generate(15, 1);

        TaskSet taskSet = taskSets.getTaskSets().get(0);

        // victim and observer task
        Task victimTask = taskSet.getOneTaskByPriority(VICTIM_PRI);
        Task observerTask = taskSet.getOneTaskByPriority(OBSERVER_PRI);

        double gcd = Umath.gcd(victimTask.getPeriod(), observerTask.getPeriod());
        double lcm = Umath.lcm(victimTask.getPeriod(), observerTask.getPeriod());
        double observationRatio = observerTask.getExecTime() / gcd;


        loggerConsole.info(taskSet.toString());
        long taskSetHyperPeriod = taskSet.calHyperPeriod();
        loggerConsole.info("Task Hyper-period: " + taskSetHyperPeriod);

        // New and configure a RM scheduling simulator.
        QuickFixedPrioritySchedulerSimulator rmSimulator = new QuickFixedPrioritySchedulerSimulator();
        rmSimulator.setTaskSet(taskSet);

        //for (int i=1; i<=3; i++) {
        //    taskSet.getOneTaskByPriority(2+i).setSporadicTask(true);
        //}

        // Pre-schedule
        QuickFPSchedulerJobContainer simJobContainer = rmSimulator.preSchedule(SIM_DURATION);

        // Run simulation.
        rmSimulator.simJobs(simJobContainer);
        EventContainer eventContainer = rmSimulator.getSimEventContainer();

        // Build busy intervals for ScheduLeak
        BusyIntervalEventContainer biEvents = new BusyIntervalEventContainer();
        biEvents.createBusyIntervalsFromEvents(eventContainer);
        biEvents.removeBusyIntervalsBeforeButExcludeTimeStamp(victimTask.getInitialOffset());

        // Get only observable busy intervals
        BusyIntervalEventContainer observedBiEvents =
                null;
        try {
            observedBiEvents = new BusyIntervalEventContainer( biEvents.getObservableBusyIntervalsByTask(observerTask) );
        } catch (Exception e) {
            e.printStackTrace();
        }



        // Create Excel file
        ExcelLogHandler excelLogHandler = new ExcelLogHandler();

        excelLogHandler.genRowSchedulerIntervalEvents(eventContainer);
        excelLogHandler.genRowBusyIntervals(biEvents);
        excelLogHandler.genRowBusyIntervals(observedBiEvents);
        //excelLogHandler.genRowSchedulerIntervalEvents(decomposedEvents);

        // Output inferred arrival window
        /*
        EventContainer arrivalWindowEventContainer = new EventContainer();
        for (SchedulerIntervalEvent thisEvent : arrivalWindow.getArrivalWindowEventByTime(0)) {
            arrivalWindowEventContainer.add(thisEvent);
        }
        excelLogHandler.genRowSchedulerIntervalEvents(arrivalWindowEventContainer);
        */

        excelLogHandler.saveAndClose(null);

        loggerConsole.info("The number of observed BIs: " + observedBiEvents.size());

        loggerConsole.info(eventContainer.getAllEvents());

    }
}
