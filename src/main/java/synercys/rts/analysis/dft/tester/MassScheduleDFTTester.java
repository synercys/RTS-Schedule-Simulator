package synercys.rts.analysis.dft.tester;

import cy.utility.Umath;
import cy.utility.file.FileHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.RtsConfig;
import synercys.rts.analysis.MassTester;
import synercys.rts.analysis.dft.ScheduleDFTAnalysisReport;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.scheduler.AdvanceableSchedulerSimulator;
import synercys.rts.scheduler.TaskSetContainer;

import java.util.ArrayList;

public class MassScheduleDFTTester extends MassTester {
    public static final String TEST_CASES_VARIED_SCHEDULE_LENGTH = "VARIED_SCHEDULE_LENGTH";
    public static final String TEST_CASES_DFT_DURATION= "DFT_DURATION";
    public static final String TEST_CASES_DFT_DURATION_BY_LARGEST_PERIOD= "DFT_DURATION_BY_LARGEST_PERIOD";
    public static final String TEST_CASES_STFT = "STFT";
    public static final String TEST_CASES_STFT_SCHEDULEAK_LCM = "STFT_SCHEDULEAK_LCM";
    public static final String TEST_CASES_STFT_CUMULATIVE_UNEVEN = "STFT_CUMULATIVE_UNEVEN";
    public static final String TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN = "STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN";
    public static final String TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE_EVEN = "STFT_SCHEDULEAK_VICTIM_CUMULATIVE_EVEN";

    private static final Logger loggerConsole = LogManager.getLogger("console");

    public MassScheduleDFTTester(String logFilePath, TaskSetContainer taskSetContainer) {
        super(logFilePath, taskSetContainer);
    }

    public boolean run(String testCase) {
        boolean status;
        switch (testCase) {
            case TEST_CASES_DFT_DURATION:
                status = runDFTDurationTest();
                break;
            case TEST_CASES_DFT_DURATION_BY_LARGEST_PERIOD:
                status = runDFTDurationByLargestPeriodTest();
                break;
            case TEST_CASES_STFT:
                status = runSTFTTest();
                break;
            case TEST_CASES_STFT_SCHEDULEAK_LCM:
                status= runSTFTScheduLeakTest();
                break;
            case TEST_CASES_STFT_CUMULATIVE_UNEVEN:
                status= runSTFTSCumulativeTest(true);
                break;
            case TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE_UNEVEN:
                status= runSTFTScheduLeakVictimCumulativeTest(true);
                break;
            case TEST_CASES_STFT_SCHEDULEAK_VICTIM_CUMULATIVE_EVEN:
                status= runSTFTScheduLeakVictimCumulativeTest(false);
                break;
            case TEST_CASES_VARIED_SCHEDULE_LENGTH: default:
                status = runVariedScheduleLengthTest();
                break;
        }
        return status;
    }

    protected boolean runDFTDurationTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: iteration (duration) is negative or zero.");
            return false;
        }

        loggerConsole.info("------------------------------");
        loggerConsole.info("Start DFT test ...");
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);


        FileHandler fileTestLog = openLogFileToWrite("dftDuration", "csv");

        // title row
        fileTestLog.writeString(
                "Task Set ID,"
                        + "Raw Task Set ID,"
                        + "The Number of Tasks,"
                        + "Utilization,"
                        + "Hyper Period,"
                        + "Test Length,"
                        + "Sample Variance,"
                        + "Normalized Sample Variance,"
                        + "Z-Score Based Peak Count,"
                        + "Above Threshold Bin Count,"
                        + "Deadline Miss Count,"
                        + "Deadline Miss Ratio,"
                        + "Max Consecutive Deadline Misses,"
                        + "Mean Mean Task Frequency Error,"
                        + "Geometric Mean Mean Task Frequency Error,"
                        + "Worst Mean Task Frequency Error,"
                        + "Mean Task Under Performance Ratio,"
                        + "Worst Task Under Performance Ratio"
                // + "Context Switches,"
                // + "Mean Response Time Ratio To Period,"
                // + "Mean Execution Range Ratio To Period"
        );
        fileTestLog.writeString("\n");

        double threshold = 0.2;
        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;
            long simDuration = runDuration;
            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);
            loggerConsole.info("\tDuration = {}ms", simDuration* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);
            loggerConsole.info("\tThreshold = {}", threshold);

            ScheduleDFTTester tester = new ScheduleDFTTester(taskSet, schedulingPolicy, executionVariation);
            ScheduleDFTAnalysisReport report = tester.run(simDuration);
            // tester.exportAll(getLogFullPathFileName(String.valueOf(taskSet.getId())));
            double normalizedSampleVariance = report.getNormalizedSampleVariance();
            double sampleVariance = report.getSampleVariance();

            int aboveThresholdCount = 0;
            for (Double val : report.getNormalizedAmplitudeList()) {
                if (val >= threshold)
                    aboveThresholdCount++;
            }

            int zScorePeakCount = report.getPeakFrequenciesSignalDetector().size();

            AdvanceableSchedulerSimulator scheduler = tester.getScheduler();
            long deadlineMissCount=0, maxConsecutiveDeadlineMisses=0, taskSetTotalJobCount=0, taskSetUnderPerformanceCount=0;
            double worstMeanPeriodError=0.0, worstUnderPerformanceRatio=0.0;
            ArrayList<Double> meanPeriodicErrorList = new ArrayList<>();
            for (Task task : taskSet.getRunnableTasksAsArray()) {
                taskSetTotalJobCount += scheduler.getTaskInterArrivalTimeTrace().get(task).size();

                long taskTotalJobCount = scheduler.getTaskInterArrivalTimeTrace().get(task).size();

                Long sum = (long) 0;
                long taskUnderPerformanceCount = 0;
                for (Long interArrivalTime : scheduler.getTaskInterArrivalTimeTrace().get(task)) {
                    sum += interArrivalTime;

                    if (interArrivalTime > task.getPeriod()) {
                        taskUnderPerformanceCount += 1;
                    }
                }
                double meanInterArrivalTime = sum.doubleValue()/taskTotalJobCount;
                double meanInterArrivalTimeError = Math.abs((meanInterArrivalTime-task.getPeriod())/task.getPeriod());
                if (meanInterArrivalTimeError > worstMeanPeriodError) {
                    worstMeanPeriodError = meanInterArrivalTimeError;
                }
                meanPeriodicErrorList.add(meanInterArrivalTimeError);

                double underPerformanceRatio = (double)taskUnderPerformanceCount/taskTotalJobCount;
                if (underPerformanceRatio > worstUnderPerformanceRatio) {
                    worstUnderPerformanceRatio = underPerformanceRatio;
                }
                taskSetUnderPerformanceCount += taskUnderPerformanceCount;

                deadlineMissCount += scheduler.getTaskDeadlineMissCount().get(task);
                if (scheduler.getTaskMaxConsecutiveDeadlineMissCount().get(task) > maxConsecutiveDeadlineMisses) {
                    maxConsecutiveDeadlineMisses = scheduler.getTaskMaxConsecutiveDeadlineMissCount().get(task);
                }

            }
            double deadlineMissRatio = (double)deadlineMissCount/taskSetTotalJobCount;
            double meanUnderPerformanceRatio = (double)taskSetUnderPerformanceCount/taskSetTotalJobCount;
            double geometricMeanMeanPeriodError = Umath.getGeometricMean(meanPeriodicErrorList);
            double meanMeanPeriodError = Umath.getMean(meanPeriodicErrorList);

            fileTestLog.writeString(taskSetCount + ",");
            fileTestLog.writeString(taskSet.getId() + ",");
            fileTestLog.writeString(taskSet.getRunnableTasksAsArray().size() + ",");
            fileTestLog.writeString(taskSet.getUtilization() + ",");
            fileTestLog.writeString(taskSet.calHyperPeriod() + ",");
            fileTestLog.writeString(simDuration + ",");
            fileTestLog.writeString(String.format("%.6f", sampleVariance) + ",");
            fileTestLog.writeString(String.format("%.6f", normalizedSampleVariance) + ",");
            fileTestLog.writeString(zScorePeakCount + ",");
            fileTestLog.writeString(aboveThresholdCount + ",");
            // fileTestLog.writeString(report.contextSwitches + ",");
            // fileTestLog.writeString(String.format("%.4f", meanResponseTimeRatioToPeriod) + ",");
            // fileTestLog.writeString(String.format("%.4f", meanExecutionRangeRatioToPeriod) + "\n");
            fileTestLog.writeString(deadlineMissCount + ",");
            fileTestLog.writeString(String.format("%.6f", deadlineMissRatio) + ",");
            fileTestLog.writeString(maxConsecutiveDeadlineMisses + ",");
            fileTestLog.writeString(String.format("%.6f", meanMeanPeriodError) + ",");
            fileTestLog.writeString(String.format("%.6f", geometricMeanMeanPeriodError) + ",");
            fileTestLog.writeString(String.format("%.6f", worstMeanPeriodError) + ",");
            fileTestLog.writeString(String.format("%.6f", meanUnderPerformanceRatio) + ",");
            fileTestLog.writeString(String.format("%.6f", worstUnderPerformanceRatio) + "\n");

            loggerConsole.info("\tDone. Variance={} AboveThresholdCount={} Z-ScorePeakCount={} deadlineMissRatio={} meanMeanPeriodError={} geometricMeanMeanPeriodError={} meanUnderPerformanceRatio={}", String.format("%.6f", normalizedSampleVariance), aboveThresholdCount, zScorePeakCount, String.format("%.6f", deadlineMissRatio), String.format("%.6f", meanMeanPeriodError), String.format("%.6f", geometricMeanMeanPeriodError), String.format("%.6f", meanUnderPerformanceRatio));
        }

        return true;
    }

    protected boolean runDFTDurationByLargestPeriodTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: iteration (duration) is negative or zero.");
            return false;
        }

        loggerConsole.info("------------------------------");
        loggerConsole.info("Start DFT test ...");
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);


        FileHandler fileTestLog = openLogFileToWrite("dftByLargestPeriod", "csv");

        // title row
        fileTestLog.writeString(
            "Task Set ID,"
            + "Raw Task Set ID,"
            + "The Number of Tasks,"
            + "Utilization,"
            + "Hyper Period,"
            + "Test Length,"
            + "Normalized Sample Variance"
            // + "Context Switches,"
            // + "Mean Response Time Ratio To Period,"
            // + "Mean Execution Range Ratio To Period"
        );
        fileTestLog.writeString("\n");


        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;
            long simDuration = taskSet.getLargestPeriod()*runDuration;
            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);
            loggerConsole.info("\tDuration = {}ms", simDuration* RtsConfig.TIMESTAMP_UNIT_TO_MS_MULTIPLIER);

            ScheduleDFTTester tester = new ScheduleDFTTester(taskSet, schedulingPolicy, executionVariation);
            ScheduleDFTAnalysisReport report = tester.run(simDuration);
            // tester.exportAll(getLogFullPathFileName(String.valueOf(taskSet.getId())));
            double normalizedSampleVariance = report.getNormalizedSampleVariance();

            fileTestLog.writeString(taskSetCount + ",");
            fileTestLog.writeString(taskSet.getId() + ",");
            fileTestLog.writeString(taskSet.getRunnableTasksAsArray().size() + ",");
            fileTestLog.writeString(taskSet.getUtilization() + ",");
            fileTestLog.writeString(taskSet.calHyperPeriod() + ",");
            fileTestLog.writeString(simDuration + ",");
            fileTestLog.writeString(String.format("%.6f", normalizedSampleVariance) + "\n");
            // fileTestLog.writeString(report.contextSwitches + ",");
            // fileTestLog.writeString(String.format("%.4f", meanResponseTimeRatioToPeriod) + ",");
            // fileTestLog.writeString(String.format("%.4f", meanExecutionRangeRatioToPeriod) + "\n");

            loggerConsole.info("\tDone. Variance={}", String.format("%.6f", normalizedSampleVariance));
        }

        return true;
    }

    protected boolean runVariedScheduleLengthTest() {
        loggerConsole.info("Scheduler: {}", schedulingPolicy);

        int taskSetCount = 0;
        int totalNumberOfTaskSet = taskSetContainer.size();
        for (TaskSet taskSet : taskSetContainer.getTaskSets()) {
            taskSetCount++;
            loggerConsole.info("Testing TaskSet #{}\t{}/{} ...", taskSet.getId(), taskSetCount, totalNumberOfTaskSet);

            VariedScheduleLengthDFTTester tester = new VariedScheduleLengthDFTTester(taskSet, schedulingPolicy, executionVariation);
            tester.run(10);
            tester.exportAll(getLogFullPathFileName(String.valueOf(taskSet.getId())));
        }

        return true;
    }

    protected boolean runSTFTTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: duration is negative or zero.");
            return false;
        }

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.run(runDuration);
        stftTester.exportReport(getLogFullPathFileName());

        return true;
    }

    protected boolean runSTFTScheduLeakTest() {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: iteration (duration) is negative or zero.");
            return false;
        }

        loggerConsole.info("------------------------------");
        loggerConsole.info("Start STFT test ...");
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);
        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.runScheduLeakAttackDuration((int)runDuration);
        stftTester.exportReport(getLogFullPathFileName());

        loggerConsole.info("------------------------------");

        return true;
    }

    protected boolean runSTFTScheduLeakVictimCumulativeTest(boolean unevenSpectrum) {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: sim factor is negative or zero.");
            return false;
        }

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);

        loggerConsole.info("------------------------------");
        loggerConsole.info("[{}] Start STFT test ...", taskSet.getId());
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);

        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.runScheduLeakVictimCumulativeSTFT((int)runDuration, unevenSpectrum);

        loggerConsole.info("Export experiment results ...");
        stftTester.exportReport(getLogFullPathFileName());

        loggerConsole.info("------------------------------");

        return true;
    }

    // runDuration is the taken as a multiplier. simDuration = runDuration * largestPeriod
    protected boolean runSTFTSCumulativeTest(boolean unevenSpectrum) {
        if (runDuration <= 0) {
            loggerConsole.error("Test aborted: sim factor is negative or zero.");
            return false;
        }

        TaskSet taskSet = taskSetContainer.getTaskSets().get(0);

        loggerConsole.info("------------------------------");
        loggerConsole.info("[{}] Start STFT test ...", taskSet.getId());
        loggerConsole.info("Set Scheduler: {}", schedulingPolicy);
        loggerConsole.info("Variation: {}", executionVariation);

        ScheduleSTFTTester stftTester = new ScheduleSTFTTester(taskSet, schedulingPolicy, executionVariation);
        stftTester.runCumulativeSTFTDurationByLargestPeriod((int)runDuration, unevenSpectrum);

        loggerConsole.info("Export experiment results ...");
        stftTester.exportReport(getLogFullPathFileName());

        loggerConsole.info("------------------------------");

        return true;
    }
}
