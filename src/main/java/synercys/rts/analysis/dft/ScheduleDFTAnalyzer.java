package synercys.rts.analysis.dft;

import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;
import synercys.rts.framework.event.BusyIntervalEventContainer;
import synercys.rts.framework.event.EventContainer;


public class ScheduleDFTAnalyzer {

    public double[] getFrequencySpectrumOfSchedule(EventContainer schedule) {
        BusyIntervalEventContainer biContainer = new BusyIntervalEventContainer(schedule);
        double[] rawBinarySchedule = biContainer.toBinaryDouble();
        return transformFFTPowerOfTwo(rawBinarySchedule, 0);
    }


    /**
     *
     * @param data raw data points to be analyzed by FFT
     * @param admittedDataLength desired minimum length for the data to be analyzed;
     *                           a value <=0 indicates that data.length will be used;
     *                           the actual length of data to be analyzed will be a power of 2 value greater than
     *                           this value (or data.length in the case of admittedDataLength <= 0)
     * @return a double array representing the resulting frequency spectrum
     */
    public double[] transformFFTPowerOfTwo(double[] data, int admittedDataLength) {

        // determine the actual length that will be used for FFT computation
        // (the length must be in power of two)
        if (admittedDataLength <= 0) {
            admittedDataLength = data.length;
        }
        int actualPowerOfTwoLength = getNextPowerOfTwoInclusive(admittedDataLength);
        // int actualPowerOfTwoLength = getLastPowerOfTwoInclusive(admittedDataLength);

        // Add padding zeros
        double[] zeroPaddedData = new double[actualPowerOfTwoLength];
        System.arraycopy(data, 0, zeroPaddedData, 0, Math.min(actualPowerOfTwoLength, data.length));

        // Transform
        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fftComplexArray = transformer.transform(zeroPaddedData, TransformType.FORWARD);

        double spectrum[] = new double[(fftComplexArray.length/2)+1];
        for (int i=0; i<(fftComplexArray.length/2)+1; i++) {
            // spectrum[i] = fftComplexArray[i].abs() * fftComplexArray[i].abs();
            spectrum[i] = fftComplexArray[i].abs() * fftComplexArray[i].abs() + fftComplexArray[i].getImaginary() * fftComplexArray[i].getImaginary();
        }

        return spectrum;
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
}
