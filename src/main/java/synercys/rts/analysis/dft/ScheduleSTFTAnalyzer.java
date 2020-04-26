package synercys.rts.analysis.dft;

import synercys.rts.framework.TaskSet;

public class ScheduleSTFTAnalyzer {
    ScheduleSTFTAnalysisReport report = new ScheduleSTFTAnalysisReport();

    double[] binarySchedule = null;

    // for reports only
    TaskSet taskSet = null;

    /**
     * The task set is only stored in report and is not used in analysis.
     * @param taskSet task set instance that associates with the given schedule
     */
    public void setTaskSet(TaskSet taskSet) {
        this.taskSet = taskSet;
        report.taskSet = taskSet;
    }

    public void setBinarySchedule(double[] binarySchedule) {
        this.binarySchedule = binarySchedule;
    }

    public ScheduleSTFTAnalysisReport compute(int windowLength, int shiftLength) {

        report.timeFreqSpectrumMap.clear();

        // x * shiftLength + windowLength <= data.length
        // x <= (data.len - windowLength) / shiftLength
        int timeSliceCount = (binarySchedule.length-windowLength)/shiftLength + 1;
        for (int i=0; i<timeSliceCount; i++) {
            /* Record this time bin's exact value */
            double thisTimeBin = i*shiftLength + (double)windowLength/2;
            // timeBins.add(thisTimeBin);

            /* Prepare the windowed schedule data */
            double[] windowedSchedule = new double[windowLength];
            System.arraycopy(binarySchedule, i*shiftLength, windowedSchedule, 0, windowLength);

            ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
            analyzer.setTaskSet(taskSet);   // it doesn't matter if taskSet is null
            analyzer.setBinarySchedule(windowedSchedule);
            report.timeFreqSpectrumMap.put(thisTimeBin, analyzer.computeFreqSpectrum());
        }
        return report;
    }

}
