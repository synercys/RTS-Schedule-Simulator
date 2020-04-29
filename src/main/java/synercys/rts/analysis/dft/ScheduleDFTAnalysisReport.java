package synercys.rts.analysis.dft;

import synercys.rts.framework.TaskSet;

import java.util.*;

public class ScheduleDFTAnalysisReport {

    TaskSet taskSet;

    double baseFreq;
    int dataLength;

    LinkedHashMap<Double, Double> freqSpectrumAmplitudeMap;
    LinkedHashMap<Double, Double> freqSpectrumPhaseMap;

    ArrayList<Double> peakFrequencies = null;

    public double getBaseFreq() {
        return baseFreq;
    }

    public int getDataLength() {
        return dataLength;
    }

    public double getPeakFreq() {
        return peakFrequencies.get(0);
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

    public ArrayList<Double> getPeakFrequencies() {
        if (peakFrequencies == null) {
            peakFrequencies = new ArrayList<>();
            for (Double freq : sortMapByValueDescending(freqSpectrumAmplitudeMap).keySet()) {
                peakFrequencies.add(freq);
            }
        }

        return peakFrequencies;
    }

    public double getClosestBinFrequency(double targetFreq) {
        double closestFreq = 0;
        for (double freq : freqSpectrumAmplitudeMap.keySet()) {
            if (Math.abs(targetFreq-freq) < Math.abs(targetFreq-closestFreq))
                closestFreq = freq;
        }
        return closestFreq;
    }

    /* This function is modified from https://mkyong.com/java/how-to-sort-a-map-in-java/
     * This function takes a map instance and return a value-sorted (in a descending order) map instance. */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortMapByValueDescending(Map<K, V> unsortMap) {

        List<Map.Entry<K, V>> list =
                new LinkedList<Map.Entry<K, V>>(unsortMap.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }

        return result;
    }
}
