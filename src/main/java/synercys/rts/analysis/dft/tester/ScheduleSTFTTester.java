package synercys.rts.analysis.dft.tester;

import cy.utility.Umath;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.dft.ScheduleSTFTAnalysisReport;
import synercys.rts.analysis.dft.ScheduleSTFTAnalyzer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.TaskSetGenerator;
import synercys.rts.util.JsonLogExporter;


public class ScheduleSTFTTester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    AdvanceableSchedulerInterface scheduler;
    TaskSet taskSet;
    ScheduleSTFTAnalysisReport report;
    ScheduleSTFTAnalyzer analyzer = new ScheduleSTFTAnalyzer();


    public ScheduleSTFTTester(TaskSet taskSet, String schedulingPolicy, boolean executionVariation) {
        this.taskSet = taskSet;
        scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
        analyzer.setTaskSet(taskSet);
    }


    public ScheduleSTFTAnalysisReport run(long duration) {
        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(duration).toBinaryScheduleDouble());
        report = analyzer.compute((int)duration/10, (int)duration/20);
        return report;
    }

    public ScheduleSTFTAnalysisReport runScheduLeakAttackDuration(int iteration) {

        loggerConsole.info("Used Scheduler: {}", SchedulerUtil.getSchedulerName(scheduler));

        Task[] observerVictimTasks = TaskSetGenerator.getDefaultObserverVictimTasks(taskSet);
        int lcmToTv = (int)Umath.lcm(observerVictimTasks[0].getPeriod(), observerVictimTasks[1].getPeriod());
        long runDuration = 10*lcmToTv*iteration;
        int windowLength = lcmToTv;
        int windowShift = lcmToTv/2;

        loggerConsole.info("Simulation duration: {}", runDuration);
        loggerConsole.info("STFT window size: {}", windowLength);
        loggerConsole.info("STFT window shift: {}", windowShift);

        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(runDuration).toBinaryScheduleDouble());
        report = analyzer.compute(windowLength, windowShift);
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
