package synercys.rts.scheduler.entropy;

import cy.utility.Class;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

public class EntropyCalculatorUtility {
    static public String ENTROPY_SHANNON = "Shannon";
    static public String ENTROPY_UPPER_APPROXIMATE = "UApEn";
    static public String ENTROPY_APPROXIMATE = "ApEn";

    public static ScheduleEntropyCalculatorInterface getEntropyCalculator(String entropyAlgorithm, TaskSet taskSet, long simOffset, long simDuration) {
        if (entropyAlgorithm.equalsIgnoreCase(ENTROPY_SHANNON))
            return new ShannonScheduleEntropyCalculator(simOffset, simDuration);
        else if (entropyAlgorithm.equalsIgnoreCase(ENTROPY_UPPER_APPROXIMATE))
            return new UpperApproximateEntropyCalculator(taskSet, simOffset, simDuration);
        else if (entropyAlgorithm.equalsIgnoreCase(ENTROPY_APPROXIMATE))
            return new ApproximateEntropyCalculator(simOffset, simDuration);
        else
            return null;
    }

    static public ArrayList<String> getEntropyNames() {
        // This function is from cy.utility
        return Class.getPrefixMatchedVariableStringValues(EntropyCalculatorUtility.class, "ENTROPY_");
    }
}
