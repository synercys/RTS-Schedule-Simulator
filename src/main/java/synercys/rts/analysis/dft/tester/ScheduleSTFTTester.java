package synercys.rts.analysis.dft.tester;

import cy.utility.Umath;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.RtsConfig;
import synercys.rts.analysis.dft.ScheduleSTFTAnalysisReport;
import synercys.rts.analysis.dft.ScheduleSTFTAnalyzer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.scheduler.TaskSetGenerator;
import synercys.rts.util.JsonLogExporter;

import java.util.ArrayList;


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
        report = analyzer.compute((int) duration / 10, (int) duration / 20);
        return report;
    }
    public ScheduleSTFTAnalysisReport runCumulativeSTFTDurationByLargestPeriod(long simDurationFactor, boolean unevenSpectrum) {
        long simDuration = taskSet.getLargestPeriod()*simDurationFactor;
        long windowLength = taskSet.getLargestPeriod();
        // long windowShift = windowLength/2;

        loggerConsole.info("Used Scheduler: {}", SchedulerUtil.getSchedulerName(scheduler));
        loggerConsole.info("Simulation duration: {} ({}ms)", simDuration, simDuration*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        loggerConsole.info("STFT window size: {} ({}ms)", windowLength, windowLength*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        // loggerConsole.info("STFT window shift: {} ({}ms)", windowShift, windowShift*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);

        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(simDuration).toBinaryScheduleDouble());
        if (unevenSpectrum)
            report = analyzer.computeCumulativeSTFT_uneven((int)windowLength);
        else
            report = analyzer.computeCumulativeSTFT_even((int)windowLength);

        return report;
    }

    public ScheduleSTFTAnalysisReport runScheduLeakAttackDuration(int iteration) {

        loggerConsole.info("Used Scheduler: {}", SchedulerUtil.getSchedulerName(scheduler));

        Task[] observerVictimTasks = TaskSetGenerator.getDefaultObserverVictimTasks(taskSet);
        int lcmToTv = (int)Umath.lcm(observerVictimTasks[0].getPeriod(), observerVictimTasks[1].getPeriod());
        long simDuration = 10*lcmToTv*iteration;
        int windowLength = lcmToTv;
        int windowShift = lcmToTv/2;

        loggerConsole.info("Simulation duration: {} ({}ms)", simDuration, simDuration*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        loggerConsole.info("STFT window size: {} ({}ms)", windowLength, windowLength*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        loggerConsole.info("STFT window shift: {} ({}ms)", windowShift, windowShift*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);

        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(simDuration).toBinaryScheduleDouble());
        report = analyzer.compute(windowLength, windowShift);
        return report;
    }

    public ScheduleSTFTAnalysisReport runScheduLeakVictimCumulativeSTFT(int simDurationFactor, boolean unevenSpectrum) {

        long victimTaskPeriod;
        if (taskSet.getRunnableTasksAsArray().size() == 1)
            victimTaskPeriod = taskSet.getHighestPriorityTask().getPeriod();
        else
            victimTaskPeriod = TaskSetGenerator.getDefaultObserverVictimTasks(taskSet)[1].getPeriod();

        long simDuration = simDurationFactor*victimTaskPeriod;

        loggerConsole.info("Used Scheduler: {}", SchedulerUtil.getSchedulerName(scheduler));
        loggerConsole.info("Simulation duration: {} ({}ms)", simDuration, simDuration*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
        loggerConsole.info("STFT window size: {} ({}ms)", victimTaskPeriod, victimTaskPeriod*RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);

        analyzer.setBinarySchedule(scheduler.runSimWithDefaultOffset(simDuration).toBinaryScheduleDouble());
        if (unevenSpectrum)
            report = analyzer.computeCumulativeSTFT_uneven((int)victimTaskPeriod);
        else
            report = analyzer.computeCumulativeSTFT_even((int)victimTaskPeriod);

        double victimFreq = 1.0/(victimTaskPeriod*RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER);

        if (!unevenSpectrum) {
            double closestFreq = report.getTimeFreqSpectrumMap().values().iterator().next().getClosestBinFrequency(victimFreq);
            loggerConsole.info("Expected vs found freq: {} <> {}", victimFreq, closestFreq);
        }

        ArrayList<Integer> victimFreqRanking = report.getFrequencyRankingList(victimFreq);
        StringBuilder outStr = new StringBuilder("");
        for (int ranking : victimFreqRanking) {
            outStr.append(String.format("%d, ", ranking));
        }
        outStr.deleteCharAt(outStr.length()-1); // delete redundant ", "
        outStr.deleteCharAt(outStr.length()-1);

        loggerConsole.info("Victim's frequency {} peak ranking:", victimFreq);
        loggerConsole.info(outStr.toString());

        return report;
    }

    public void exportReport(String filePath) {
        // Handle the file's full path + base name
        String fileFullPathBasePrefix = FilenameUtils.concat(FilenameUtils.getFullPath(filePath), FilenameUtils.getBaseName(filePath));

        if (FilenameUtils.getBaseName(filePath).isEmpty()) {
            fileFullPathBasePrefix = FilenameUtils.concat(fileFullPathBasePrefix, String.valueOf(taskSet.getId()));
        }

        /* Export the task set and the STFT to a json file */
        loggerConsole.info("Export to .rtstft (uneven = {}) ...", report.isUnevenSpectrum());
        JsonLogExporter exporter = new JsonLogExporter(fileFullPathBasePrefix + ".rtstft");
        if (report.isUnevenSpectrum())
            exporter.exportSTFTAnalysisReport_uneven(report);
        else
            exporter.exportSTFTAnalysisReport(report);

        /* Export the STFT to a CSV file */
        // if (!report.isUnevenSpectrum()) {
        //     loggerConsole.info("Export to .csv ...");
        //     JsonLogExporter csvExporter = new JsonLogExporter(fileFullPathBasePrefix + ".csv");
        //     csvExporter.exportSTFTAnalysisReportToCSV(report);
        // }
    }


}
