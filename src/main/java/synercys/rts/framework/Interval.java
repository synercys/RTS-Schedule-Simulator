package synercys.rts.framework;

import java.util.ArrayList;

/**
 * Created by CY on 7/13/2015.
 */
public class Interval {
    private long begin;
    private long end;

    public Interval(long inBegin, long inEnd) {
        begin = inBegin;
        end = inEnd;
    }

    // Create a new Interval and duplicate the value from another existing Interval object.
    public Interval(Interval inInterval) {
        this(inInterval.getBegin(), inInterval.getEnd());
    }

    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }

    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }

    public long getLength() {
        return (end - begin);
    }

    /* Calculate intersection and return a new Interval object. */
    // [a, b)
    // so [a,b) intersect with [b,c) equals nothing.
    public Interval intersect(Interval inInterval)
    {
        Interval leftInterval, rightInterval;
        long resultBegin=0, resultEnd=0;

        // Check which one is on the left.
        if (begin <= inInterval.getBegin())
        {// Me is on the left
            leftInterval = this;
            rightInterval = inInterval;
        }
        else
        {
            leftInterval = inInterval;
            rightInterval = this;
        }

        /* Determine begin value. */
        if (leftInterval.getEnd() < rightInterval.getBegin())
        {
            // They have no intersection.
            return null;
        }
        else
        {
            resultBegin = rightInterval.getBegin();
        }

        /* Determine end value. */
        if (leftInterval.getEnd() < rightInterval.getEnd())
        {
            resultEnd = leftInterval.getEnd();
        }
        else
        {
            resultEnd = rightInterval.getEnd();
        }

        if (resultBegin == resultEnd) {
            // [a,b) intersect with [b,c) equals nothing.
            return null;
        } else {
            return new Interval(resultBegin, resultEnd);
        }
    }

    public void shift(long inOffset)
    {
        begin += inOffset;
        end += inOffset;
    }

    public Boolean contains(long inPoint)
    {
        if ((begin <= inPoint)
                && (end >= inPoint))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public Boolean within(Interval inLarger) {
        if ( (inLarger.begin<=begin) && (inLarger.end>=end) ) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<Interval> union(Interval inInterval) {
        ArrayList<Interval> resultIntervals = new ArrayList<>();
        if (intersect(inInterval) != null) {
            // Is continuous.

            long earliestBegin = 0;
            long latestEnd = 0;

            // Find earliest begin time and latest end time.
            earliestBegin = inInterval.begin < begin ? inInterval.begin : begin;
            latestEnd = inInterval.end > end ? inInterval.end : end;

            resultIntervals.add(new Interval(earliestBegin, latestEnd));

        } else {
            resultIntervals.add(new Interval(this));
            resultIntervals.add(new Interval(inInterval));
        }

        return resultIntervals;
    }

    public ArrayList<Interval> minus(Interval inInterval) {
        ArrayList<Interval> resultIntervals = new ArrayList<>();
        Interval intersectedInterval = intersect(inInterval);

        if (intersect(inInterval) == null) {
            // return untouched interval.
            resultIntervals.add(new Interval(begin, end));
            return resultIntervals;
        }

        if (begin < intersectedInterval.begin) {
            resultIntervals.add(new Interval(begin, intersectedInterval.begin));
        }

        if (end > intersectedInterval.end) {
            resultIntervals.add(new Interval(intersectedInterval.end, end));
        }

        return resultIntervals;
    }

    public Boolean isEqual(Interval inInterval) {
        if ( (begin==inInterval.getBegin()) && (end==inInterval.getEnd()) ) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "[" + begin + ", " + end + "] => " + getLength();
    }
}