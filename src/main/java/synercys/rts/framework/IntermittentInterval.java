package synercys.rts.framework;

import java.util.ArrayList;

/**
 * IntermittentInterval.java
 * Purpose: A fundamental class that stores intermittent (discontinuous) intervals.
 *
 * @author CY Chen (cchen140@illinois.edu)
 * @version 1.0 - 2017, 3/26
 *
 */
public class IntermittentInterval {
    protected ArrayList<Interval> intervals = new ArrayList<>();

    public IntermittentInterval() {}

    public IntermittentInterval(ArrayList<Interval> inIntervals) {
        intervals.addAll(inIntervals);
    }

    public IntermittentInterval(Interval inInterval) {
        intervals.add(inInterval);
    }

    public long getBegin() {
        long begin = 0;
        Boolean firstLoop = true;
        for (Interval thisInterval : intervals) {
            if (firstLoop) {
                firstLoop = false;
                begin = thisInterval.getBegin();
            } else {
                begin = thisInterval.getBegin() < begin ? thisInterval.getBegin() : begin;
            }
        }
        return begin;
    }

    public long getEnd() {
        long end = 0;
        Boolean firstLoop = true;
        for (Interval thisInterval : intervals) {
            if (firstLoop) {
                firstLoop = false;
                end = thisInterval.getEnd();
            } else {
                end = thisInterval.getEnd() > end ? thisInterval.getEnd() : end;
            }
        }
        return end;
    }

    public void shift(long inShift) {
        for (Interval thisInterval : intervals) {
            thisInterval.shift(inShift);
        }
    }

    public void union(Interval inInterval) {
        intervals = getUnion(inInterval).intervals;
    }

    public void union(IntermittentInterval interInterval) {
        intervals = getUnion(interInterval).intervals;
    }

    public IntermittentInterval getUnion(Interval inInterval) {

        // Check which intervals are intersected.
        ArrayList<Interval> intersectedIntervals = new ArrayList<>();
        for (Interval thisInterval : intervals) {
            if (thisInterval.intersect(inInterval) != null) {
                intersectedIntervals.add(thisInterval);
            }
        }

        // Create new getUnion interval if there is any intersection.
        if (intersectedIntervals.size() > 0) {
            Interval unionInterval = inInterval;
            for (Interval thisInterval : intersectedIntervals) {
                unionInterval = unionInterval.union(thisInterval).get(0);
            }

            // New object
            IntermittentInterval resultIntervals = new IntermittentInterval(intervals);
            resultIntervals.intervals.removeAll(intersectedIntervals);
            resultIntervals.intervals.add(unionInterval);
            return resultIntervals;
        } else {
            IntermittentInterval resultIntervals = new IntermittentInterval(intervals);
            resultIntervals.intervals.add(inInterval);
            return resultIntervals;
        }
    }

    public IntermittentInterval getUnion(IntermittentInterval interInterval) {
        IntermittentInterval resultInterInterval = interInterval;
        for (Interval thisInterval : intervals) {
            resultInterInterval = resultInterInterval.getUnion(thisInterval);
        }
        return resultInterInterval;
    }

    public void intersect(Interval inInterval) {
        intervals = getIntersection(inInterval).intervals;
    }

    public void intersect(IntermittentInterval interInterval) {
        intervals = getIntersection(interInterval).intervals;
    }

    public IntermittentInterval getIntersection(Interval inInterval) {
        // Check which intervals are intersected.
        ArrayList<Interval> intersectedIntervals = new ArrayList<>();
        for (Interval thisInterval : intervals) {
            Interval thisIntersectedInterval = thisInterval.intersect(inInterval);
            if (thisIntersectedInterval != null) {
                intersectedIntervals.add(thisIntersectedInterval);
            }
        }

        // Create new intersected interval if there is any intersection.
        if (intersectedIntervals.size() > 0) {
            return new IntermittentInterval(intersectedIntervals);
        } else {
            // return empty interval.
            return new IntermittentInterval();
        }
    }

    public IntermittentInterval getIntersection(IntermittentInterval interInterval) {
        IntermittentInterval resultInterInterval = new IntermittentInterval();
        for (Interval thisInterval : intervals) {
            resultInterInterval = resultInterInterval.getUnion(interInterval.getIntersection(thisInterval));
        }
        return resultInterInterval;
    }

    public IntermittentInterval getSubtraction(Interval inInterval) {
        ArrayList<Interval> resultIntervals = new ArrayList<>();
        for (Interval thisInterval : intervals) {
            resultIntervals.addAll(thisInterval.minus(inInterval));
        }
        return new IntermittentInterval(resultIntervals);
    }

    public IntermittentInterval getSubtraction(IntermittentInterval interInterval) {
        IntermittentInterval resultInterInterval = new IntermittentInterval(intervals);
        for (Interval thisInterval : interInterval.intervals) {
            resultInterInterval = resultInterInterval.getSubtraction(thisInterval);
        }
        return resultInterInterval;
    }

    public void minus(Interval inInterval) {
        intervals = getSubtraction(inInterval).intervals;
    }
    public void minus(IntermittentInterval interInterval) {
        intervals = getSubtraction(interInterval).intervals;
    }

    public Boolean hasInterval(Interval inInterval) {
        for (Interval thisInterval : intervals) {
            if (thisInterval.isEqual(inInterval)) {
                return true;
            }
        }
        return false;
    }

    public Interval getLongestInterval() {
        Interval longestInterval = null;
        Boolean firstLoop = true;
        for (Interval thisInterval : intervals) {
            if (firstLoop == true) {
                longestInterval = thisInterval;
                firstLoop = false;
            }

            if (thisInterval.getLength() > longestInterval.getLength()) {
                longestInterval = thisInterval;
            }
        }
        return longestInterval;
    }

    public Interval getIntervalContainingPoint(long inPoint) {
        for (Interval thisInterval : intervals) {
            if (thisInterval.contains(inPoint) == true) {
                return thisInterval;
            }
        }
        return null;
    }

    /**
     * Add the given interval into the existing interval set. The overlap is pushed back.
     * @param inInterval
     */
    public void cascadeInterval(Interval inInterval) {
        Interval inIntervalCloned = new Interval(inInterval);
        IntermittentInterval intersectedIntervals;
        long lastTotalLength;
        do {
            lastTotalLength = inIntervalCloned.getLength();
            intersectedIntervals = getIntersection(inIntervalCloned);
            inIntervalCloned.setEnd(inInterval.getEnd()+intersectedIntervals.getTotalLength());
        } while (inIntervalCloned.getLength() != lastTotalLength);
        union(inIntervalCloned);
    }

    public ArrayList<Interval> getIntervals() {
        return intervals;
    }

    public long getTotalLength() {
        long totalLength = 0;
        for (Interval thisInterval : intervals) {
            totalLength += thisInterval.getLength();
        }
        return totalLength;
    }

    @Override
    public String toString() {
        String outputStr = "";
        outputStr += "Interval Count = " + intervals.size() + "\r\n";
        for (Interval thisInterval : intervals) {
            outputStr += "\t" + thisInterval.toString() + "\r\n";
        }
        return outputStr;
    }
}
