package synercys.rts.analysis.dft;

import synercys.rts.framework.TaskSet;
import java.util.Map;

public class ScheduleDFTAnalysisReport {

    TaskSet taskSet;

    double baseFreq;
    int dataLength;

    Map<Double, Double> freqSpectrumAmplitudeMap;
    Map<Double, Double> freqSpectrumPhaseMap;

    double peakFreq;

    public double getBaseFreq() {
        return baseFreq;
    }

    public int getDataLength() {
        return dataLength;
    }

    public double getPeakFreq() {
        return peakFreq;
    }

    public Map<Double, Double> getFreqSpectrumAmplitudeMap() {
        return freqSpectrumAmplitudeMap;
    }

    public Map<Double, Double> getFreqSpectrumPhaseMap() {
        return freqSpectrumPhaseMap;
    }

    public TaskSet getTaskSet() {
        return taskSet;
    }
}
