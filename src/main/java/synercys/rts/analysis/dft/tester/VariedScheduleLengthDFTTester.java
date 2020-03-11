package synercys.rts.analysis.dft.tester;

import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.analysis.dft.ScheduleDFTAnalysisReport;
import synercys.rts.analysis.dft.ScheduleDFTAnalyzer;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.scheduler.AdvanceableSchedulerInterface;
import synercys.rts.scheduler.SchedulerUtil;
import synercys.rts.util.JsonLogExporter;

import java.util.ArrayList;
import java.util.HashMap;

public class VariedScheduleLengthDFTTester {
    private static final Logger loggerConsole = LogManager.getLogger("console");

    TaskSet taskSet;
    AdvanceableSchedulerInterface scheduler;
    HashMap<Task, ArrayList<ScheduleDFTAnalysisReport> > taskDFTReports = new HashMap<>();

    public VariedScheduleLengthDFTTester(TaskSet taskSet, String schedulingPolicy, boolean executionVariation) {
        this.taskSet = taskSet;
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            taskDFTReports.put(task, new ArrayList<>());
        }
        scheduler = SchedulerUtil.getScheduler(schedulingPolicy, taskSet, executionVariation);
    }

    /**
     *
     * @param simDurationMultiple the number of periods to be examined for each task
     */
    public void run(long simDurationMultiple) {
        long largestSimDuration = taskSet.getLargestPeriod()*simDurationMultiple;
        EventContainer eventContainer = scheduler.runSim(largestSimDuration);
        for (Task task : taskSet.getRunnableTasksAsArray()) {
            for (int i=1; i<=simDurationMultiple; i++) {
                long simDurationToExamine = task.getPeriod()*i;
                ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
                analyzer.setTaskSet(taskSet);
                analyzer.setBinarySchedule(eventContainer.toBinaryScheduleDouble(0, simDurationToExamine));
                ScheduleDFTAnalysisReport report = analyzer.computeFreqSpectrum();
                taskDFTReports.get(task).add(report);
            }
        }
    }

    public void exportAll(String filePath) {
        // Handle the file's full path + base name
        String fileFullPathBasePrefix = FilenameUtils.getFullPath(filePath);
        if (FilenameUtils.getBaseName(filePath).isEmpty()) {
            fileFullPathBasePrefix = FilenameUtils.concat(fileFullPathBasePrefix, String.valueOf(taskSet.getId()));
        } else {
            fileFullPathBasePrefix = FilenameUtils.concat(fileFullPathBasePrefix, FilenameUtils.getBaseName(filePath));
        }

        for (Task task : taskSet.getRunnableTasksAsArray()) {
            // Get file name
            String taskFilePathPrefix = String.format("%s_%s", fileFullPathBasePrefix, task.getId());
            for (int i=0; i<taskDFTReports.get(task).size(); i++) {
                String trueFilePathName = String.format("%s_%d.rtdft", taskFilePathPrefix, i+1);
                JsonLogExporter exporter = new JsonLogExporter(trueFilePathName);
                exporter.exportDFTAnalysisReport(taskDFTReports.get(task).get(i));
            }
        }
    }
}
