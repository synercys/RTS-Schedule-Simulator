package synercys.rts.analysis.dft.tester;

import cy.utility.Umath;
import org.apache.commons.io.FilenameUtils;
import synercys.rts.analysis.dft.ScheduleSTFTAnalysisReport;
import synercys.rts.analysis.dft.ScheduleSTFTAnalyzer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.TaskSetGenerator;
import synercys.rts.util.JsonLogExporter;


public class ScheduleSTFTTester {
    AdvanceableSchedulerInterface scheduler;
    TaskSet taskSet;
    ScheduleSTFTAnalysisReport report;
    ScheduleSTFTAnalyzer analyzer = new ScheduleSTFTAnalyzer();


    public ScheduleSTFTTester(TaskSet taskSet, String schedulingPolicy, boolean executionVariation) {
        this.taskSet = taskSet;
        scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
        analyzer.setTaskSet(taskSet);
    }

    public ScheduleSTFTAnalysisReport runScheduLeakAttackDuration(int iteration) {
        Task[] observerVictimTasks = TaskSetGenerator.getDefaultObserverVictimTasks(taskSet);
        int lcmToTv = (int)Umath.lcm(observerVictimTasks[0].getPeriod(), observerVictimTasks[1].getPeriod());
        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(10*lcmToTv*iteration).toBinaryScheduleDouble());
        report = analyzer.compute(lcmToTv, lcmToTv/2);
        return report;
    }

    public void exportReport(String filePath) {
        // Handle the file's full path + base name
        String fileFullPathBasePrefix = FilenameUtils.concat(FilenameUtils.getFullPath(filePath), FilenameUtils.getBaseName(filePath));

        if (FilenameUtils.getBaseName(filePath).isEmpty()) {
            fileFullPathBasePrefix = FilenameUtils.concat(fileFullPathBasePrefix, String.valueOf(taskSet.getId()));
        }

        /* Export the task set and the STFT to a json file */
        JsonLogExporter exporter = new JsonLogExporter(fileFullPathBasePrefix + ".rtstft");
        exporter.exportSTFTAnalysisReport(report);

        /* Export the STFT to a CSV file */
        JsonLogExporter csvExporter = new JsonLogExporter(fileFullPathBasePrefix + ".csv");
        csvExporter.exportSTFTAnalysisReportToCSV(report);
    }


}
