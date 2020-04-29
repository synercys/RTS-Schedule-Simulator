package synercys.rts.analysis.dft;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import synercys.rts.framework.TaskSet;

public class ScheduleSTFTAnalyzer {
    private static final Logger loggerConsole = LogManager.getLogger("console");

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

            loggerConsole.info("#{}\tBegin DFT analysis for the interval [{}, {}]...", i+1, i*shiftLength, i*shiftLength+windowLength);

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

    public ScheduleSTFTAnalysisReport computeCumulativeSTFT(int windowLength) {

        report.timeFreqSpectrumMap.clear();

        int maxScheduleLengthForAnalysis = binarySchedule.length - (binarySchedule.length % windowLength);

        // TODO: include i=0?
        for (int i=1; i*windowLength<=binarySchedule.length; i++) {

            /* Record this time bin's exact value */
            double thisTimeBin = i*windowLength;

            loggerConsole.info("#{}\tBegin DFT analysis for the interval [{}, {}]...", i, 0, thisTimeBin);

            /* Prepare the windowed schedule data */
            // default values in a double array are zeros
            // (https://docs.oracle.com/javase/tutorial/java/nutsandbolts/datatypes.html)
            double[] windowedSchedule = new double[maxScheduleLengthForAnalysis];   // use consistent length to get consistent frequency bins
            System.arraycopy(binarySchedule, 0, windowedSchedule, 0, windowLength*i);

            ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
            analyzer.setTaskSet(taskSet);   // it doesn't matter if taskSet is null
            analyzer.setBinarySchedule(windowedSchedule);
            report.timeFreqSpectrumMap.put(thisTimeBin, analyzer.computeFreqSpectrum());
        }
        return report;
    }

}
