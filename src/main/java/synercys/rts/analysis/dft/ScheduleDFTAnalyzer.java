package synercys.rts.analysis.dft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import synercys.rts.framework.event.EventContainer;

import static synercys.rts.RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER;


public class ScheduleDFTAnalyzer {
    static int DFT_DATA_PADDING_MODE_NEXT_POWER_OF_TWO = 0;
    static int DFT_DATA_PADDING_MODE_LAST_POWER_OF_TWO = 1;

    // configurations
    int paddingMode = DFT_DATA_PADDING_MODE_LAST_POWER_OF_TWO;

    double[] binarySchedule = null;
    double[] spectrum = null;


    public void setBinarySchedule(EventContainer schedule) {
        binarySchedule = schedule.toBinaryScheduleDouble();
        validateBinarySchedule(-1);
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

    public double[] computeFreqSpectrum() {
        // Transform
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftComplexArray = transformer.transform(binarySchedule, TransformType.FORWARD);

        spectrum = new double[(fftComplexArray.length/2)+1];
        for (int i=0; i<(fftComplexArray.length/2)+1; i++) {
            // spectrum[i] = fftComplexArray[i].abs() * fftComplexArray[i].abs();
            spectrum[i] = fftComplexArray[i].abs() * fftComplexArray[i].abs() + fftComplexArray[i].getImaginary() * fftComplexArray[i].getImaginary();
        }

        return spectrum;
    }


    protected int computeValidDataLength(int rawLength) {
        if (paddingMode == DFT_DATA_PADDING_MODE_NEXT_POWER_OF_TWO) {
            return getNextPowerOfTwoInclusive(rawLength);
        } else {
            return getLastPowerOfTwoInclusive(rawLength);
        }
    }


    public int getAnalyzedDataLength() {
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


    public double getPeakFreq() {
        if (spectrum == null) {
            return 0.0;
        }

        double baseFreq = getBaseFreq();
        double maxAmplitude = 0.0;
        double peakFreq = 0.0;
        for (int i=0; i<spectrum.length; i++) {
            if (spectrum[i] > maxAmplitude) {
                maxAmplitude = spectrum[i];
                peakFreq = baseFreq*i;
            }
        }
        return peakFreq;
    }


    public double getBaseFreq() {
        int sampleRate = (int)(1/TIMESTAMP_UNIT_TO_S_MULTIPLIER);
        return (double)sampleRate/getAnalyzedDataLength();
    }
}
