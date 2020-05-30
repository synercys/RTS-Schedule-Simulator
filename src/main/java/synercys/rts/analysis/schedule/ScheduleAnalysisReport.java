package synercys.rts.analysis.schedule;

import synercys.rts.analysis.Report;
import synercys.rts.framework.Interval;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;

import java.util.HashMap;
import java.util.List;

public class ScheduleAnalysisReport extends Report {
    public TaskSet taskSet;

    public int contextSwitches;
    public HashMap<Task, List<Double>> rawResponseTimeRatioToPeriod;
    public HashMap<Task, Double> meanResponseTimeRatioToPeriod;
    public HashMap<Task, Double> taskExecutionRangeRatioToPeriod;
    public HashMap<Task, Double> taskDeadlineMissRate;

    public double getMeanResponseTimeRatioToPeriod() {
        int ratioLength = 0;
        double ratioSum = 0.0;
        for (List<Double> ratioList : rawResponseTimeRatioToPeriod.values()) {
            ratioLength += ratioList.size();
            for (Double ratio : ratioList) {
                ratioSum += ratio;
            }
        }
        // for (Double ratio : meanResponseTimeRatioToPeriod.values()) {
        //     ratioLength += 1;
        //     ratioSum += ratio;
        // }
        return ratioSum/(double)ratioLength;
    }

    public double getMeanTaskExecutionRangeRatioToPeriod() {
        double geometricMean = 1.0;
        double length = 0.0;
        for (double ratio : taskExecutionRangeRatioToPeriod.values()) {
            if (ratio==0)
                continue;
            geometricMean *= ratio;
            length++;
        }
        geometricMean = Math.pow(geometricMean, 1.0/length);
        return geometricMean;
    }

    public double getMeanDeadlineMissRate() {
        double sum = 0;
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            sum += taskDeadlineMissRate.get(task);
        }
        return sum/taskDeadlineMissRate.size();
    }
}
