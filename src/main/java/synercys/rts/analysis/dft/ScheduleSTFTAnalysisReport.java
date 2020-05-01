package synercys.rts.analysis.dft;

import synercys.rts.framework.TaskSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleSTFTAnalysisReport {

    TaskSet taskSet;
    boolean unevenSpectrum = false;

    // Key: timestamp
    Map<Double, ScheduleDFTAnalysisReport> timeFreqSpectrumMap = new LinkedHashMap<>();

    public ArrayList<Double> getTimes() {
        return new ArrayList<>( timeFreqSpectrumMap.keySet() );
    }

    public TaskSet getTaskSet() {
        return taskSet;
    }

    /**
     * Extract time-frequency-amplitude from all the reports.
     * @return Key: timestamp, Value: frequency-amplitude map
     */
    public Map<Double, Map<Double, Double>> getExpandedTimeFreqSpectrumAmplitudeMap() {
        Map<Double, Map<Double, Double>> spectrumMap = new LinkedHashMap<>();
        for (Map.Entry<Double, ScheduleDFTAnalysisReport> entry : timeFreqSpectrumMap.entrySet()) {
            spectrumMap.put(entry.getKey(), entry.getValue().freqSpectrumAmplitudeMap);
        }
        return spectrumMap;
    }

    public Map<Double, ScheduleDFTAnalysisReport> getTimeFreqSpectrumMap() {
        return timeFreqSpectrumMap;
    }

    public ArrayList<Integer> getFrequencyRankingList(double targetFreq) {
        ArrayList<Integer> freqRanking = new ArrayList<>();
        boolean firstLoop = true;
        double closestBinFreq = 0.0;
        for (ScheduleDFTAnalysisReport report : timeFreqSpectrumMap.values()) {
            if (unevenSpectrum) {
                // if the spectrum is uneven, frequency bins will be different and hence recalculation is required
                closestBinFreq = report.getClosestBinFrequency(targetFreq);
            } else {
                if (firstLoop) {
                    closestBinFreq = report.getClosestBinFrequency(targetFreq);
                    firstLoop = false;
                }
            }
            freqRanking.add(report.getPeakFrequencies().indexOf(closestBinFreq) + 1);
        }
        return freqRanking;
    }

    public boolean isUnevenSpectrum() {
        return unevenSpectrum;
    }
}
