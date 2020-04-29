package synercys.rts.analysis.dft;

import synercys.rts.framework.TaskSet;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

public class ScheduleSTFTAnalysisReport {

    TaskSet taskSet;

    Map<Double, ScheduleDFTAnalysisReport> timeFreqSpectrumMap = new LinkedHashMap<>();

    public ArrayList<Double> getTimes() {
        return new ArrayList<>( timeFreqSpectrumMap.keySet() );
    }

    public TaskSet getTaskSet() {
        return taskSet;
    }

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
}
