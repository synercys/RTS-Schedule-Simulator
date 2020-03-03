package synercys.rts.analysis.dft;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.scheduler.FixedPriorityScheduler;

import java.util.Map;
import java.util.TreeMap;

import static synercys.rts.RtsConfig.TIMESTAMP_MS_TO_UNIT_MULTIPLIER;
import static synercys.rts.RtsConfig.TIMESTAMP_UNIT_TO_S_MULTIPLIER;

class ScheduleDFTAnalyzerTest {

    @Test
    void getNextPowerOfTwoInclusive() {
        ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
        int power = 0;
        for (int i=0; i<1025; i++) {
            int truePowerOfTwo = (int)Math.pow(2, power);
            assertEquals(truePowerOfTwo, analyzer.getNextPowerOfTwoInclusive(i));
            if (truePowerOfTwo == i) {
                power += 1;
            }
        }
    }

    @Test
    void getFrequencySpectrumOfSchedule() {
        long SIM_DURATION = (long)(250*TIMESTAMP_MS_TO_UNIT_MULTIPLIER);
        long TASK_PERIOD_MS = 30;

        // Set up a sample task set
        TaskSet taskSet = new TaskSet();
        taskSet.addTask(new Task(1, "", Task.TASK_TYPE_APP, (long)(TASK_PERIOD_MS*TIMESTAMP_MS_TO_UNIT_MULTIPLIER), (long)(TASK_PERIOD_MS*TIMESTAMP_MS_TO_UNIT_MULTIPLIER), 1, 1));
        taskSet.addIdleTask();

        FixedPriorityScheduler fixedPriorityScheduler = new FixedPriorityScheduler(taskSet, false);
        EventContainer eventContainer = fixedPriorityScheduler.runSim(SIM_DURATION);

        double taskFreq = taskSet.getRunnableTasksAsArray().get(0).getFreq();
        int sampleRate = (int)(1/TIMESTAMP_UNIT_TO_S_MULTIPLIER);

        // Analyze FFT
        ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
        analyzer.setBinarySchedule(eventContainer);
        analyzer.computeFreqSpectrum();
        double baseFreq = analyzer.getBaseFreq();
        double peakFreq = analyzer.getPeakFreq();

        /* uncomment to print raw spectrum values */
        // System.out.println("Freq\t:\tAmplitude");
        // System.out.println("=====================");
        // Map<Double, Double> sortedFreqSpectrum = new TreeMap<>(analyzer.freqSpectrumAmplitudeMap);
        // for (Map.Entry<Double, Double> entry : sortedFreqSpectrum.entrySet()) {
        //     System.out.println(String.format("%.2f\t:\t%.2f", entry.getKey(), entry.getValue()));
        // }
        // System.out.println("=====================");
        // System.out.println("");

        System.out.println("Tested Task Freq: " + taskFreq);
        System.out.println("Sample Rate: " + sampleRate);
        System.out.println("Base Freq: " + baseFreq);
        System.out.println("Peak Freq: " + peakFreq);

        // Check if the peak freq is a multiple of taskFreq Hz and within 10% error
        assertEquals(true, peakFreq%taskFreq<taskFreq*0.1 || peakFreq%taskFreq>taskFreq*0.9);
        assertNotEquals(0.0, peakFreq);
    }
}