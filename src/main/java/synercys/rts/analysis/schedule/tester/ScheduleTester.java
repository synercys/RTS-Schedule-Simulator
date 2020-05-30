package synercys.rts.analysis.schedule.tester;

import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.Report;
import synercys.rts.analysis.Tester;
import synercys.rts.analysis.schedule.ScheduleAnalysisReport;
import synercys.rts.framework.Interval;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ScheduleTester extends Tester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    public ScheduleTester(TaskSet taskSet, String schedulingPolicy, boolean executionVariation) {
        super(taskSet, schedulingPolicy, executionVariation);
        report = new ScheduleAnalysisReport();
        ((ScheduleAnalysisReport)report).taskSet = taskSet;
    }

    @Override
    public Report run(long simDuration) {
        HashMap<Task, Interval> taskMinMaxExecutionSlots = new HashMap<>();
        HashMap<Task, List<Double>> rawResponseTimeRatioToPeriod = new HashMap<>();
        HashMap<Task, Integer> taskPeriodCount = new HashMap<>();
        HashMap<Task, Integer> taskDeadlineMissCount = new HashMap<>();
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            // taskMinMaxExecutionSlots.put(task, new Interval(task.getPeriod(), 0));
            rawResponseTimeRatioToPeriod.put(task, new ArrayList<>());
            taskPeriodCount.put(task, 0);
            taskDeadlineMissCount.put(task, 0);
        }
        HashMap<Task, SchedulerIntervalEvent> currentTaskJobBeginEvent = new HashMap<>();
        int contextSwitchCount = 0;

        EventContainer eventContainer = scheduler.runSim(simDuration);

        long lastEndTimestamp = 0;
        for (SchedulerIntervalEvent event : eventContainer.getSchedulerEvents()) {
            Task task = event.getTask();

            if (task.isIdleTaskType())
                continue;

            contextSwitchCount++;
            if (lastEndTimestamp != event.getOrgBeginTimestamp()) // there is idle time in between
                contextSwitchCount++;

            if (event.isStartEvent()) {
                taskPeriodCount.put(task, taskPeriodCount.get(task)+1);

                // for determining response time
                currentTaskJobBeginEvent.put(task, event);

                // for determining execution range (left)
                long jobBeginSlot = (event.getOrgBeginTimestamp()-task.getInitialOffset())%task.getPeriod();
                if (!taskMinMaxExecutionSlots.containsKey(task)) {
                    taskMinMaxExecutionSlots.put(task, new Interval(jobBeginSlot, jobBeginSlot));
                } else if (jobBeginSlot < taskMinMaxExecutionSlots.get(task).getBegin()) {
                    taskMinMaxExecutionSlots.get(task).setBegin(jobBeginSlot);
                }

                continue;
            }

            if (event.isEndEvent()) {
                if (!currentTaskJobBeginEvent.containsKey(task) || !taskMinMaxExecutionSlots.containsKey(task)) {
                    throw new AssertionError("A schedule closing event does not have a starting event.");
                }

                // for determining response time
                long jobArrivalTime = ((event.getOrgEndTimestamp()-task.getInitialOffset())/task.getPeriod())*task.getPeriod() + task.getInitialOffset();
                long responseTime = event.getOrgEndTimestamp()-jobArrivalTime;
                rawResponseTimeRatioToPeriod.get(task).add((double)responseTime/(double)task.getPeriod());

                // for determining execution range (right)
                long jobEndSlot = (event.getOrgEndTimestamp()-task.getInitialOffset())%task.getPeriod();
                if (jobEndSlot > taskMinMaxExecutionSlots.get(task).getEnd()) {
                    taskMinMaxExecutionSlots.get(task).setEnd(jobEndSlot);
                }

                // record deadline misses
                if (event.isDeadlineMissed()) {
                    taskDeadlineMissCount.put(task, taskDeadlineMissCount.get(task) + 1);
                }

                currentTaskJobBeginEvent.remove(task); // so that we can detect an error if begin and end do not match.
            }
        }

        ((ScheduleAnalysisReport)report).contextSwitches = contextSwitchCount;

        ((ScheduleAnalysisReport)report).rawResponseTimeRatioToPeriod = rawResponseTimeRatioToPeriod;
        ((ScheduleAnalysisReport)report).meanResponseTimeRatioToPeriod = new HashMap<>();
        ((ScheduleAnalysisReport)report).taskExecutionRangeRatioToPeriod = new HashMap<>();
        ((ScheduleAnalysisReport)report).taskDeadlineMissRate = new HashMap<>();
        for (Task task : taskSet.getRunnableTasksAsArray()) {

            // for response time
            double ratioSum = 0.0;
            // double ratioSum = 1.0;
            for (double ratio : rawResponseTimeRatioToPeriod.get(task)) {
                ratioSum += ratio;
                // ratioSum *= ratio;
            }
            double ratioCount = rawResponseTimeRatioToPeriod.get(task).size();
            if (ratioCount > 0) {
                ((ScheduleAnalysisReport) report).meanResponseTimeRatioToPeriod.put(task, ratioSum / ratioCount);
                // ((ScheduleAnalysisReport) report).meanResponseTimeRatioToPeriod.put(task, Math.pow(ratioSum, 1.0/ratioCount));
            }

            // for execution range
            double executionRangeRatio = (double)taskMinMaxExecutionSlots.get(task).getLength()/(double)task.getPeriod();
            ((ScheduleAnalysisReport)report).taskExecutionRangeRatioToPeriod.put(task, executionRangeRatio);

            // for deadline miss rate
            ((ScheduleAnalysisReport) report).taskDeadlineMissRate.put(task, (double)taskDeadlineMissCount.get(task)/taskPeriodCount.get(task));
        }


        return report;
    }

    // public ScheduleAnalysisReport runCompare(long simDuration) {
    //
    // }
}
