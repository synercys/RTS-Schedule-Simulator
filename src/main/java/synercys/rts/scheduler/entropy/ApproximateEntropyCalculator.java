package synercys.rts.scheduler.entropy;

import synercys.rts.framework.event.EventContainer;

import java.util.ArrayList;
import java.util.List;

public class ApproximateEntropyCalculator implements ScheduleEntropyCalculatorInterface {
    static public String name = EntropyCalculatorUtility.ENTROPY_APPROXIMATE;
    long m = 1; // window width
    int threshold = 0; // hamming distance threshold
    List<List<String>> slotWindowSchedules = new ArrayList<>();
    List<List<Integer>> slotWindowScheduleSimilarityCounts = new ArrayList<>();
    long beginTimestamp;
    long length;
    int totalScheduleCount = 0;

    public ApproximateEntropyCalculator(long beginTimestamp, long length) {
        this.beginTimestamp = beginTimestamp;
        this.length = length;
        for (int i=0; i<length; i++) {
            slotWindowSchedules.add(new ArrayList<>());
            slotWindowScheduleSimilarityCounts.add(new ArrayList<>());
        }

        threshold = (int)(length*0.1);
        threshold = (threshold==0) ? 1 : threshold;

        m = (int)(length*0.35);
        m = (m==0) ? 1 : m;

        /* The following configuration yields entropy close to the upper-approximate schedule entropy. */
        // threshold = 0;
        // m = 1;
    }


    @Override
    public void applyOneSchedule(EventContainer schedule) {

        for (int i=0; i<length; i++) {
            List<String> thisSlotWindowSchedules = slotWindowSchedules.get(i);
            List<Integer> thisSlotWindowScheduleSimilarityCounts = slotWindowScheduleSimilarityCounts.get(i);
            String slotWindowScheduleString;
            if (i<length-m+1)
                slotWindowScheduleString = schedule.toRawScheduleString(beginTimestamp+i, beginTimestamp+i+m);
            else {
                slotWindowScheduleString = schedule.toRawScheduleString(beginTimestamp+i, beginTimestamp+length);
                slotWindowScheduleString += ", " + schedule.toRawScheduleString(beginTimestamp, beginTimestamp+m-(length-i));
            }
            slotWindowScheduleString = slotWindowScheduleString.replaceAll(",| ", "");
            thisSlotWindowSchedules.add(slotWindowScheduleString);
            thisSlotWindowScheduleSimilarityCounts.add(1);

            /* Iterating through existing rounds to update the dissimilarity count */
            for (int k=0; k<totalScheduleCount; k++) {
                int hammingDistance = computeHammingDistance(thisSlotWindowSchedules.get(k), slotWindowScheduleString);
                if (hammingDistance <= threshold) {
                    thisSlotWindowScheduleSimilarityCounts.set(k, thisSlotWindowScheduleSimilarityCounts.get(k)+1);
                    thisSlotWindowScheduleSimilarityCounts.set(totalScheduleCount, thisSlotWindowScheduleSimilarityCounts.get(totalScheduleCount)+1);
                }
            }
        }

        totalScheduleCount++;
    }


    @Override
    public double concludeEntropy() {
        double finalEntropy = 0.0;
        for (int i=0; i<length; i++) {
            double slotEntropy = 0.0;
            for (int k=0; k<totalScheduleCount; k++) {
                double c = slotWindowScheduleSimilarityCounts.get(i).get(k)/(double)totalScheduleCount;
                slotEntropy += log2(c);
            }
            slotEntropy = -slotEntropy/(double)totalScheduleCount;
            finalEntropy += slotEntropy;
        }
        finalEntropy = finalEntropy/(double)m;
        return finalEntropy;
    }


    protected int computeHammingDistance(String a, String b) {
        int i = 0;
        int count = 0;
        while (i < a.length() && i < b.length())
        {
            if (a.charAt(i) != b.charAt(i))
                count++;
            i++;
        }
        return count;
    }

    public static double log2(double x)
    {
        return Math.log(x) / Math.log(2);
    }
}
