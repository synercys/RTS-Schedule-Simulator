package synercys.rts.analysis.dft;

import cy.utility.Umath;
import synercys.rts.framework.TaskSet;

import java.text.DecimalFormat;
import java.util.*;

public class ScheduleDFTAnalysisReport {

    TaskSet taskSet;

    double baseFreq;
    int dataLength;

    LinkedHashMap<Double, Double> freqSpectrumAmplitudeMap;
    LinkedHashMap<Double, Double> freqSpectrumPhaseMap;

    ArrayList<Double> peakFrequencies = null;
    ArrayList<Double> sortedFrequencies = null;

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

    public ArrayList<Double> getFrequenciesSortedByAmplitudes() {
        if (sortedFrequencies == null) {
            sortedFrequencies = new ArrayList<>();
            for (Double freq : sortMapByValueDescending(freqSpectrumAmplitudeMap).keySet()) {
                sortedFrequencies.add(freq);
            }
        }

        return sortedFrequencies;
    }

    public ArrayList<Double> getPeakFrequenciesSignalDetector() {
        if (peakFrequencies == null) {
            peakFrequencies = new ArrayList<>();
        }


        DecimalFormat df = new DecimalFormat("#0.000");

        SignalDetector signalDetector = new SignalDetector();
        int lag = (int)(10/baseFreq);
        double threshold = 3.5;
        double influence = 0.5;

        System.out.println("Base Freq: " + baseFreq + " Hz");

        List<Double> amplitudeList = new ArrayList<>(freqSpectrumAmplitudeMap.values());
        List<Double> freqList = new ArrayList<>(freqSpectrumAmplitudeMap.keySet());
        HashMap<String, List> resultsMap = signalDetector.analyzeDataForSignals(amplitudeList, lag, threshold, influence);
        // print algorithm params
        System.out.println("lag: " + lag + "\t\tthreshold: " + threshold + "\t\tinfluence: " + influence);

        System.out.println("Data size: " + freqSpectrumAmplitudeMap.size());
        System.out.println("Signals size: " + resultsMap.get("signals").size());

        // print data
        // System.out.print("Data:\t\t");
        // for (double d : amplitudeList) {
        //     System.out.print(df.format(d) + "\t");
        // }
        // System.out.println();

        // print signals
        // System.out.print("Signals:\t");
        List<Integer> signalsList = resultsMap.get("signals");
        // for (int i : signalsList) {
        //     System.out.print(df.format(i) + "\t");
        // }
        // System.out.println();

        // print filtered data
        // System.out.print("Filtered Data:\t");
        // List<Double> filteredDataList = resultsMap.get("filteredData");
        // for (double d : filteredDataList) {
        //     System.out.print(df.format(d) + "\t");
        // }
        // System.out.println();

        // print running average
        // System.out.print("Avg Filter:\t");
        // List<Double> avgFilterList = resultsMap.get("avgFilter");
        // for (double d : avgFilterList) {
        //     System.out.print(df.format(d) + "\t");
        // }
        // System.out.println();

        // print running std
        // System.out.print("Std filter:\t");
        // List<Double> stdFilterList = resultsMap.get("stdFilter");
        // for (double d : stdFilterList) {
        //     System.out.print(df.format(d) + "\t");
        // }
        // System.out.println();

        // System.out.println();
        // for (int i = 0; i < signalsList.size(); i++) {
        //     if (signalsList.get(i) != 0) {
        //         System.out.println("Point " + i + " gave signal " + signalsList.get(i));
        //     }
        // }

        double lastSignal = 0;
        double lastFreq = 0;
        double lastAmplitude = 0;
        HashMap<Double, Double> peakMap  = new HashMap<>();
        for (int i=0; i<signalsList.size(); i++) {
            if (i==0) {
                lastSignal = signalsList.get(i);
                lastFreq = freqList.get(i);
                lastAmplitude = amplitudeList.get(i);
                continue;
            }

            if (signalsList.get(i)==1 && lastSignal!=1) {
            // if (signalsList.get(i) < lastSignal) {
                // peakFrequencies.add(lastFreq);
                peakMap.put(lastFreq, lastAmplitude);
            }

            lastSignal = signalsList.get(i);
            lastFreq = freqList.get(i);
            lastAmplitude = amplitudeList.get(i);

        }

        for (Double freq : sortMapByValueDescending(peakMap).keySet()) {
            // if (freq <= 100)
                peakFrequencies.add(freq);
        }

        return peakFrequencies;
    }

    public ArrayList<Double> getPeakFrequenciesRisingDetection() {
        if (peakFrequencies == null) {
            peakFrequencies = new ArrayList<>();
            double lastAmplitude = 0;
            double lastFreq = 0;
            boolean firstLoop = true;
            boolean rising = true;
            HashMap<Double, Double> peakMap  = new HashMap<>();
            for (Map.Entry<Double, Double> entry : freqSpectrumAmplitudeMap.entrySet()) {
                if (firstLoop) {
                    lastFreq = entry.getKey();
                    lastAmplitude = entry.getValue();
                    firstLoop = false;
                    continue;
                }

                if (entry.getValue() < lastAmplitude && rising) {
                    peakMap.put(lastFreq, lastAmplitude);
                    // peakFrequencies.add(lastFreq);
                    rising = false;
                } else if (entry.getValue() >= lastAmplitude) {
                    rising = true;
                }

                lastFreq = entry.getKey();
                lastAmplitude = entry.getValue();

            }

            for (Double freq : sortMapByValueDescending(peakMap).keySet()) {
                peakFrequencies.add(freq);
            }
        }

        return peakFrequencies;
    }

    public ArrayList<Double> getPeakFrequenciesThreshold() {
        double threshold = 0.1;
        if (peakFrequencies == null) {
            peakFrequencies = new ArrayList<>();
            ArrayList<Double> normalizedAmplitudes = Umath.getMinMaxNormalizedList(new ArrayList<>(freqSpectrumAmplitudeMap.values()));
            // threshold = Umath.getMean(normalizedAmplitudes);
            HashMap<Double, Double> peakMap  = new HashMap<>();
            // List<Double> amplitudeList = new ArrayList<>(freqSpectrumAmplitudeMap.values());
            List<Double> freqList = new ArrayList<>(freqSpectrumAmplitudeMap.keySet());
            for (int i=0; i<normalizedAmplitudes.size(); i++) {
                if (normalizedAmplitudes.get(i) >= threshold) {
                    peakMap.put(freqList.get(i), normalizedAmplitudes.get(i));
                }
            }
            for (Double freq : sortMapByValueDescending(peakMap).keySet()) {
                peakFrequencies.add(freq);
            }

            // System.out.println(threshold);
            // System.out.println(Umath.getSampleVariance(normalizedAmplitudes));
        }
        return peakFrequencies;
    }

    public ArrayList<Double> getPeakFrequencies() {
        // return getPeakFrequenciesRisingDetection();
        return getPeakFrequenciesSignalDetector();
        // return getPeakFrequenciesThreshold();
    }

    public double getNormalizedSampleVariance() {
        ArrayList<Double> normalizedAmplitudes = Umath.getMinMaxNormalizedList(new ArrayList<>(freqSpectrumAmplitudeMap.values()));
        return Umath.getSampleVariance(normalizedAmplitudes);
    }


    public double getSampleVariance() {
        return Umath.getSampleVariance(new ArrayList<>(freqSpectrumAmplitudeMap.values()));
    }


    public ArrayList<Double> getNormalizedAmplitudeList() {
        return Umath.getMinMaxNormalizedList(new ArrayList<>(freqSpectrumAmplitudeMap.values()));
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
