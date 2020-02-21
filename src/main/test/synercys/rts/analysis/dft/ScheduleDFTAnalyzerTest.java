package synercys.rts.analysis.dft;

import org.junit.jupiter.api.Test;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.scheduler.FixedPriorityScheduler;

import static org.junit.jupiter.api.Assertions.*;
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
        FixedPriorityScheduler fixedPriorityScheduler = new FixedPriorityScheduler(taskSet, false);
        EventContainer eventContainer = fixedPriorityScheduler.runSim(SIM_DURATION);

        double taskFreq = taskSet.getRunnableTasksAsArray().get(0).getFreq();
        int sampleRate = (int)(1/TIMESTAMP_UNIT_TO_S_MULTIPLIER);
        double baseFreq = (double)sampleRate/(ScheduleDFTAnalyzer.getNextPowerOfTwoInclusive((int)SIM_DURATION));
        // double freq = (double)sampleRate/(ScheduleDFTAnalyzer.getLastPowerOfTwoInclusive((int)simDuration));

        // Analyze FFT
        ScheduleDFTAnalyzer analyzer = new ScheduleDFTAnalyzer();
        double[] spectrum = analyzer.getFrequencySpectrumOfSchedule(eventContainer);
        double maxAmplitude = 0.0;
        double peakFreq = 0.0;
        for (int i=0; i<spectrum.length; i++) {
            //System.out.println(String.format("%.2f",baseFreq*i) + "\t:\t" + String.format("%.2f", spectrum[i]));

            // Find the peak frequency
            if (spectrum[i] > maxAmplitude) {
                maxAmplitude = spectrum[i];
                peakFreq = baseFreq*i;
            }
        }

        //System.out.println("");
        System.out.println("Tested Task Freq: " + taskFreq);
        System.out.println("Sample Rate: " + sampleRate);
        System.out.println("Base Freq: " + baseFreq);
        System.out.println("Peak Freq: " + peakFreq);

        // Check if the peak freq is a multiple of taskFreq Hz and within 10% error
        assertEquals(true, peakFreq%taskFreq<taskFreq*0.1 || peakFreq%taskFreq>taskFreq*0.9);
    }
}