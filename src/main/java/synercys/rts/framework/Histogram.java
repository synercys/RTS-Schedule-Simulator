package synercys.rts.framework;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Histogram.java
 * Purpose: A custom histogram class that supports Interval operations.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.0 - 2017, 3/28
 *
 */
public class Histogram {
    HashMap<Long, Long> map = new HashMap<Long, Long>();

    public void touch(long inLocation) {
        if (map.get(inLocation) == null) {
            map.put(inLocation, (long)1);
        } else {
            map.put(inLocation, map.get(inLocation)+1);
        }
    }

    public void touchInterval(Interval inInterval) {
        if (inInterval != null) {
            // Note: we don't include the last point when touching the interval
            for (long i = inInterval.getBegin(); i < inInterval.getEnd(); i++) {
                touch(i);
            }
        }
    }

    public void touchIntervals(ArrayList<Interval> inIntervals) {
        for (Interval thisInterval : inIntervals) {
            touchInterval(thisInterval);
        }
    }

    @Override
    public String toString() {
        String outputStr = "";
        long begin = getBegin();
        long end = getEnd();
        for (long i=begin; i<=end; i++) {
            if (map.get(i) != null) {
                outputStr += i + " \t " + map.get(i) + "\r\n";
            }
        }
        return outputStr;
    }

    /* Find the first index that has count > 0 */
    /* It returns 0 if nothing is found. */
    public long getBegin() {
        long beginIndex = 0;
        Boolean firstNoneZeroValueIndexFound = false;
        for (long thisIndex : map.keySet()) {
            if (map.get(thisIndex) > 0) {
                if (firstNoneZeroValueIndexFound == false) {
                    beginIndex = thisIndex;
                    firstNoneZeroValueIndexFound = true;
                    continue;
                } else {
                    beginIndex = thisIndex < beginIndex ? thisIndex : beginIndex;
                }
            }
        }
        return beginIndex;
    }

    /* Find the last index that has count > 0 */
    /* It returns 0 if nothing is found. */
    public long getEnd() {
        long endIndex = 0;
        Boolean lastNoneZeroValueIndexFound = false;
        for (long thisIndex : map.keySet()) {
            if (map.get(thisIndex) > 0) {
                if (lastNoneZeroValueIndexFound == false) {
                    endIndex = thisIndex;
                    lastNoneZeroValueIndexFound = true;
                    continue;
                } else {
                    endIndex = thisIndex > endIndex ? thisIndex : endIndex;
                }
            }
        }
        return endIndex;
    }

    public long getValue(long keyNum) {
        if (map.containsKey(keyNum)) {
            return map.get(keyNum);
        } else {
            return 0;
        }
    }

    public long getMostWeightedValue() {
        if (map.size() == 0) {
            return 0;
        }
        return Collections.max(map.values());
    }

    public ArrayList<Interval> getMostWeightedIntervals() {
        ArrayList<Interval> resultIntervals = new ArrayList<>();

        // Return the empty array if nothing is in the map.
        if (map.size() == 0) {
            return resultIntervals;
        }

        long highestWeight = Collections.max(map.values());;
        Boolean isBuildingAInterval = false;
        long currentIntervalBegin = 0;
        for (long i=getBegin(); i<=getEnd(); i++) {
            long currentWeight = map.get(i)==null ? 0 : map.get(i);

            if (currentWeight == highestWeight) {
                if (isBuildingAInterval) {
                    continue;
                } else {
                    currentIntervalBegin = i;
                    isBuildingAInterval = true;
                }
            } else {
                if (isBuildingAInterval) {
                    Interval newInterval = new Interval(currentIntervalBegin, i-1);
                    resultIntervals.add(newInterval);
                    isBuildingAInterval = false;
                }
            }
        }

        // Check if the last arrival interval has not yet closed.
        if (isBuildingAInterval) {
            Interval newInterval = new Interval(currentIntervalBegin, getEnd());
            resultIntervals.add(newInterval);
            //isBuildingAInterval = false;
        }

        if (resultIntervals.size() == 0) {
            //ProgMsg.debugErrPutline("It should never happen.");
        }

        return resultIntervals;
    }

    public ArrayList<Interval> getLeastNonZeroWeightedIntervals() {
        ArrayList<Interval> resultIntervals = new ArrayList<>();

        // Return the empty array if nothing is in the map.
        if (map.size() == 0) {
            return resultIntervals;
        }

        long leastWeight = Collections.min(map.values());;
        Boolean isBuildingAInterval = false;
        long currentIntervalBegin = 0;
        for (long i=getBegin(); i<=getEnd(); i++) {
            long currentWeight = map.get(i)==null ? 0 : map.get(i);

            if (currentWeight == leastWeight) {
                if (isBuildingAInterval) {
                    continue;
                } else {
                    currentIntervalBegin = i;
                    isBuildingAInterval = true;
                }
            } else {
                if (isBuildingAInterval) {
                    Interval newInterval = new Interval(currentIntervalBegin, i-1);
                    resultIntervals.add(newInterval);
                    isBuildingAInterval = false;
                }
            }
        }

        // Check if the last arrival interval has not yet closed.
        if (isBuildingAInterval) {
            Interval newInterval = new Interval(currentIntervalBegin, getEnd());
            resultIntervals.add(newInterval);
            //isBuildingAInterval = false;
        }

        if (resultIntervals.size() == 0) {
            //ProgMsg.debugErrPutline("It should never happen.");
        }

        return resultIntervals;
    }


    public ArrayList<Interval> getLeastWeightedIntervals(long begin, long end) {
        ArrayList<Interval> resultIntervals = new ArrayList<>();

        long leastWeight = getLeastWeight(begin, end);
        Boolean isBuildingAInterval = false;
        long currentIntervalBegin = 0;
        for (long i=begin; i<=end; i++) {
            long currentWeight = map.get(i)==null ? 0 : map.get(i);

            if (currentWeight == leastWeight) {
                if (isBuildingAInterval) {
                    continue;
                } else {
                    currentIntervalBegin = i;
                    isBuildingAInterval = true;
                }
            } else {
                if (isBuildingAInterval) {
                    Interval newInterval = new Interval(currentIntervalBegin, i-1);
                    resultIntervals.add(newInterval);
                    isBuildingAInterval = false;
                }
            }
        }

        // Check if the last arrival interval has not yet closed.
        if (isBuildingAInterval) {
            Interval newInterval = new Interval(currentIntervalBegin, getEnd());
            resultIntervals.add(newInterval);
            //isBuildingAInterval = false;
        }

        if (resultIntervals.size() == 0) {
            //ProgMsg.debugErrPutline("It should never happen.");
        }

        return resultIntervals;
    }

    public long getLeastWeight(long begin, long end) {
        boolean first = true;
        long smallestWeight = 0;
        for (long i=begin; i<end; i++) {
            if (first) {
                smallestWeight = map.getOrDefault(i, Long.valueOf(0));
                first = false;
                continue;
            }
            smallestWeight = Math.min(smallestWeight, map.getOrDefault(i, Long.valueOf(0)).longValue());
        }
        return smallestWeight;
    }
}
