package synercys.rts.util;

/**
 * Created by cy on 3/29/2018.
 */
public class JsonLogStr {
    /* data type */
    public static String DATA_TYPE_MULTI = "multi"; // A "multi" type can wrap many of the following type data
    public static String DATA_TYPE_RT_TASK_GEN_SETTINGS = "rtTaskGen settings";
    public static String DATA_TYPE_SINGLE_TASKSET = "single taskset";
    public static String DATA_TYPE_TASKSETS = "tasksets";
    public static String DATA_TYPE_RT_SIM_RAW_SCHEDULE = "rtSim raw schedule";
    public static String DATA_TYPE_DFT_REPORT = "dft-report";
    public static String DATA_TYPE_STFT_REPORT = "stft-report";
    public static String DATA_TYPE_STFT_UNEVEN_REPORT = "stft-uneven_report";

    /* key names */
    /* root */
    public static String ROOT_FORMAT_VERSION = "formatVersion";
    public static String ROOT_DATA_TYPE = "dataType";
    public static String ROOT_DATA = "data";

    /* data basics */
    public static String TICK_UNIT = "tickUnitInNs";

    /* tasksets */
    public static String DATA_TASKSETS = "tasksets";

    /* taskset */
    //public static String DATA_TASKSET = "taskset";
    public static String TASKSET_ID = "id";
    public static String TASKSET_TASKS = "tasks";

    /* task */
    public static String TASK_ID = "id";
    public static String TASK_NAME = "name";
    public static String TASK_TYPE = "type";    // type defined below
    public static String TASK_ARRIVAL_TYPE = "arrivalType"; // arrivalType defined below
    public static String TASK_PERIOD = "period";
    public static String TASK_DEADLINE = "deadline";
    public static String TASK_WCET = "wcet";
    public static String TASK_PRIORITY = "priority";
    public static String TASK_PHASE = "phase";

    /* taskArrival type */
    public static String TASK_ARRIVAL_TYPE_PERIODIC = "periodic";
    public static String TASK_ARRIVAL_TYPE_SPORADIC = "sporadic";

    /* RtTaskGen */
    public static String DATA_TASK_GEN_SETTINGS = "configs";

    /* rtSim */
    public static String DATA_RT_SIM_SCHEDULING_POLICY = "schedulingPolicy";
    public static String SCHEDULING_POLICY_UNKNOWN = "unknown";
    public static String SCHEDULING_POLICY_FIXED_PRIORITY = "fixedPriority";
    public static String SCHEDULING_POLICY_EDF = "edf";

    public static String DATA_RT_SIM_SCHEDULE_INTERVAL_EVENTS = "scheduleIntervalEvents";
    public static String DATA_RT_SIM_TASK_INSTANT_EVENTS = "taskInstantEvents";
    public static String DATA_RT_SIM_TASKSET = "taskSet";

    /* scheduleIntervalEvents (interval events) */
    public static String SCHEDULE_INTERVAL_EVENT_TASK_ID = "taskId";
    public static String SCHEDULE_INTERVAL_EVENT_BEGIN_TIME = "begin";
    public static String SCHEDULE_INTERVAL_EVENT_END_TIME = "end";
    public static String SCHEDULE_INTERVAL_EVENT_BEGIN_STATE = "beginState";    // state defined below
    public static String SCHEDULE_INTERVAL_EVENT_END_STATE = "endState";    // state defined below
    public static String SCHEDULE_INTERVAL_EVENT_NOTE = "note";

    /* beginState and endState (interval event state) */
    public static String SCHEDULE_INTERVAL_EVENT_STATE_UNKNOWN = "unknown";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_START = "start";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_RESUME = "resume";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_SUSPEND = "suspend";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_END = "end";
    public static String SCHEDULE_INTERVAL_EVENT_STATE_END_DEADLINE_MISSED = "endDeadlineMissed";

    /* taskInstantEvents */
    public static String TASK_INSTANT_EVENT_TASK_ID = "taskId";
    public static String TASK_INSTANT_EVENT_BEGIN = "begin";
    public static String TASK_INSTANT_EVENT_RECORD = "record";
    public static String TASK_INSTANT_EVENT_NOTE = "note";

    /* rtTaskGen settings (DATA_TYPE_RT_TASK_GEN_SETTINGS) */
    public static String TASK_GEN_SETTINGS_NUM_TASK_PER_TASKSET = "numTaskPerSet";
    public static String TASK_GEN_SETTINGS_NUM_TASKSET = "numTaskSet";
    public static String TASK_GEN_SETTINGS_MAX_HP = "maxHyperPeriod";
    public static String TASK_GEN_SETTINGS_MIN_PERIOD = "minPeriod";
    public static String TASK_GEN_SETTINGS_MAX_PERIOD = "maxPeriod";
    public static String TASK_GEN_SETTINGS_MIN_WCET = "minWcet";
    public static String TASK_GEN_SETTINGS_MAX_WCET = "maxWcet";
    public static String TASK_GEN_SETTINGS_MIN_OFFSET = "minInitOffset";
    public static String TASK_GEN_SETTINGS_MAX_OFFSET = "maxInitOffset";
    public static String TASK_GEN_SETTINGS_MIN_UTIL = "minUtil";
    public static String TASK_GEN_SETTINGS_MAX_UTIL = "maxUtil";
    public static String TASK_GEN_SETTINGS_FROM_HP_DIVISORS = "generateFromHpDivisors";
    public static String TASK_GEN_SETTINGS_NON_HARMONIC_ONLY = "nonHarmonicOnly";
    public static String TASK_GEN_SETTINGS_DISTINCT_PERIOD_ONLY = "distinctPeriodOnly";
    public static String TASK_GEN_SETTINGS_TEST_RM_SCHEDULABILITY = "rmSchedulabilityTest";
    /* ScheduLeak related options */
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_OBSERVER = "needGenObserverTask";
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_HARMONIC_OBSERVER = "needGenHarmonicObserverTask";
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_MAX_OB_RATIO = "maxObservationRatio";
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_MIN_OB_RATIO = "minObservationRatio";
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_OBSERVER_PRIORITY = "observerTaskPriority";
    public static String TASK_GEN_SETTINGS_SCHEDULEAK_VICTIM_PRIORITY = "victimTaskPriority";

    /* dft-report */
    public static String DFT_REPORT_SAMPLE_COUNT = "sampleCount";
    public static String DFT_REPORT_TASKSET = "taskSet";
    public static String DFT_REPORT_SPECTRUM_CSV = "spectrumCSV";
    // public static String DFT_REPORT_SPECTRUM_MAGNITUDE_CSV = "spectrumMagnitudeCSV";
    // public static String DFT_REPORT_SPECTRUM_PHASE_CSV = "spectrumPhase_CSV";

    /* stft-report */
    public static String STFT_REPORT_TASKSET = "taskSet";
    public static String STFT_REPORT_SPECTRUM_CSV = "spectrumCSV";
    public static String STFT_REPORT_TASK_FREQ_RANKING = "taskFreqRanking";
    public static String STFT_REPORT_TASK_FREQ_RANKING_RANKING = "ranking";

    /* stft-uneven_report */
    public static String STFT_UNEVEN_REPORT_SPECTRUM = "unevenSpectrum";
    public static String STFT_UNEVEN_REPORT_SPECTRUM_TIME = "time";
    public static String STFT_UNEVEN_REPORT_SPECTRUM_FREQUENCIES = "frequencies";
    public static String STFT_UNEVEN_REPORT_SPECTRUM_MAGNITUDES = "magnitudes";

}
