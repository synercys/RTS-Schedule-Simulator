package synercys.rts;

import picocli.CommandLine;

/**
 * Created by cy on 3/14/2017.
 */
public class RtsConfig implements CommandLine.IVersionProvider {
    static void RtsConfig() {
    }

    public static String VERSION = "0.6.8";

    /* Configuration from log */
    //public static double LOG_TIMESTAMP_UNIT_NS = 1; // 1 tick represents 1ns in log
    public static int TIMESTAMP_UNIT_NS = 100_000; // 1 tick is 100 us
    public static double TIMESTAMP_MS_TO_UNIT_MULTIPLIER = 1_000_000.0 / (double) TIMESTAMP_UNIT_NS;
    public static double TIMESTAMP_UNIT_TO_MS_MULTIPLIER = (double) TIMESTAMP_UNIT_NS / 1_000_000.0;
    public static double TIMESTAMP_UNIT_TO_S_MULTIPLIER = (double) TIMESTAMP_UNIT_NS / 1_000_000_000.0;

//    public static Boolean assignValueByVariableName(String varName, String valString) {
//        if (varName.equalsIgnoreCase("TIMESTAMP_UNIT_NS")) {
//            ProgMsg.sysPutLine("TIMESTAMP_UNIT_NS = %d -> %d", TIMESTAMP_UNIT_NS, Integer.valueOf(valString.replace("_", "")));
//            setTimestampUnitNs(Integer.valueOf(valString.replace("_", "")));
//        } else if (varName.equalsIgnoreCase("TRACE_HORIZONTAL_SCALE_FACTOR")) {
//            TRACE_HORIZONTAL_SCALE_FACTOR = Double.valueOf(valString.replace("_", ""));
//        } else {
//            return false;
//        }
//
//        return true;
//    }

    public static void setTimestampUnitNs(int val) {
        TIMESTAMP_UNIT_NS = val;
        TIMESTAMP_MS_TO_UNIT_MULTIPLIER = 1_000_000.0 / (double) TIMESTAMP_UNIT_NS;
        TIMESTAMP_UNIT_TO_MS_MULTIPLIER = (double) TIMESTAMP_UNIT_NS / 1_000_000.0;
    }

    @Override
    public String[] getVersion() throws Exception {
        return new String[]{VERSION};
    }
}