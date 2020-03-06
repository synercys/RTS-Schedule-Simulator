package synercys.rts.analysis.dft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import java.util.*;

import static synercys.rts.RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER;


public class ScheduleDFTAnalyzer {
    static int DFT_DATA_PADDING_MODE_NEXT_POWER_OF_TWO = 0;
    static int DFT_DATA_PADDING_MODE_LAST_POWER_OF_TWO = 1;

    // configurations
    int paddingMode = DFT_DATA_PADDING_MODE_LAST_POWER_OF_TWO;

    double[] binarySchedule = null;
    Map<Double, Double> freqSpectrumAmplitudeMap = new HashMap<>();
    Map<Double, Double> freqSpectrumPhaseMap = new HashMap<>();

    ScheduleDFTAnalysisReport report = new ScheduleDFTAnalysisReport();

    public void setBinarySchedule(EventContainer schedule) {
        binarySchedule = schedule.toBinaryScheduleDouble();
        validateBinarySchedule(-1);
    }


    /**
     * The task set is only stored in report and is not used in DFT analysis.
     * @param taskSet task set instance that associates with the given schedule
     */
    public void setTaskSet(TaskSet taskSet) {
        report.taskSet = taskSet;
    }


    protected void validateBinarySchedule(int admittedDataLength) {
        // determine the actual length that will be used for FFT computation
        // (the length must be in power of two)
        if (admittedDataLength <= 0) {
            admittedDataLength = binarySchedule.length;
        }

        int actualPowerOfTwoLength = computeValidDataLength(admittedDataLength);

        // Add padding zeros
        double[] zeroPaddedData = new double[actualPowerOfTwoLength];
        System.arraycopy(binarySchedule, 0, zeroPaddedData, 0, Math.min(actualPowerOfTwoLength, binarySchedule.length));

        binarySchedule = zeroPaddedData;
    }

    public ScheduleDFTAnalysisReport computeFreqSpectrum() {
        // Transform
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftComplexArray = transformer.transform(binarySchedule, TransformType.FORWARD);

        double baseFreq = getBaseFreq();
        for (int i=1; i<(fftComplexArray.length/2)+1; i++) {
            double im = fftComplexArray[i].getImaginary();
            double re = fftComplexArray[i].getReal();
            double amplitude = Math.sqrt(re*re + im*im);
            double phase = Math.atan2(im, re);
            freqSpectrumAmplitudeMap.put(i*baseFreq, amplitude);
            freqSpectrumPhaseMap.put(i*baseFreq, phase);
        }

        concludeReport();

        return report;
    }


    protected int computeValidDataLength(int rawLength) {
        if (paddingMode == DFT_DATA_PADDING_MODE_NEXT_POWER_OF_TWO) {
            return getNextPowerOfTwoInclusive(rawLength);
        } else {
            return getLastPowerOfTwoInclusive(rawLength);
        }
    }

    protected void concludeReport() {
        report.baseFreq = getBaseFreq();
        report.dataLength = getAnalyzedDataLength();
        report.freqSpectrumAmplitudeMap = freqSpectrumAmplitudeMap;
        report.freqSpectrumPhaseMap = freqSpectrumPhaseMap;
        report.peakFreq = getPeakFreq();
    }


    protected int getAnalyzedDataLength() {
        if (binarySchedule == null) {
            return 0;
        } else {
            return binarySchedule.length;
        }
    }


    static public int getNextPowerOfTwoInclusive(int value) {
        int valueOfHighestOneBit = Integer.highestOneBit(value);
        // if the value is NOT already at power of 2, then we need to find the next power of 2 value
        if (value > valueOfHighestOneBit) {
            valueOfHighestOneBit *= 2;
        }
        return Math.max(1, valueOfHighestOneBit); // 2^0 = 1
    }


    static public int getLastPowerOfTwoInclusive(int value) {
        return Math.max(1, Integer.highestOneBit(value));
    }


    protected double getPeakFreq() {
        if (freqSpectrumAmplitudeMap.size() == 0) {
            return 0.0;
        }
        return sortMapByValueDescending(freqSpectrumAmplitudeMap).keySet().iterator().next();
    }


    protected double getBaseFreq() {
        int sampleRate = (int)(1/TIMESTAMP_UNIT_TO_S_MULTIPLIER);
        return (double)sampleRate/getAnalyzedDataLength();
    }


    public ScheduleDFTAnalysisReport getReport() {
        return report;
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
