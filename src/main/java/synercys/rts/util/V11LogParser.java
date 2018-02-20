package synercys.rts.util;

import synercys.rts.event.EventContainer;
import synercys.rts.event.TaskInstantEvent;
import synercys.rts.framework.Task;
import synercys.rts.framework.TaskSet;
import synercys.util.ProgMsg;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * Created by CY on 2/13/17.
 */
public class V11LogParser implements LogParser {
    public static final int PARSER_VERSION = 10;

    private static final int LOG_BLOCK_UNKNOWN = 0;
    private static final int LOG_BLOCK_TASK_LIST = 1;
    private static final int LOG_BLOCK_SCHEDULER_LOG = 2;
    private static final int LOG_BLOCK_APP_LOG = 3;
    //private static final int LOG_BLOCK_HACKER_LOG = 4;
    private static final int LOG_BLOCK_MIXED_LOG = 5;   // Mixed log is combination of scheduler and app logs.
    private static final int LOG_BLOCK_HACKER_LIST = 6;

    private long firstTimestamp = -1;
    private EventContainer eventContainer = new EventContainer();
    private TaskSet taskSet = new TaskSet();

    public void setTaskSet(TaskSet taskSet) {
        this.taskSet = taskSet;
    }

    @Override
    public int getParserVersion() {
        return PARSER_VERSION;
    }

    /**
     * Load logs from the file and put them in the Events Container.
     * @param fileReader The BufferedReader of the opened file.
     * @return return 'false' if it fails to load the log file. Otherwise, return 'true'.
     */
    @Override
    public Boolean parseLog(BufferedReader fileReader) {
        int lineCounter = 1;
        firstTimestamp = -1;

        try {
            String line = null;
            eventContainer.clearAll();
            int currentLogBlock = LOG_BLOCK_UNKNOWN;
            while ((line = fileReader.readLine()) != null)
            {
                lineCounter++;

                if (line.trim().length()==0) // Empty line
                    continue;
                else if (line.trim().substring(0, 1).equalsIgnoreCase("#")) // Comment line
                    continue;
                else if (line.trim().toLowerCase().equalsIgnoreCase("@TaskList")) {
                    currentLogBlock = LOG_BLOCK_TASK_LIST;    // Reading task list block
                    continue;
                }
                else if (line.trim().toLowerCase().equalsIgnoreCase("@MixedLog")) {
                    currentLogBlock = LOG_BLOCK_MIXED_LOG;    // Reading log block
                    continue;
                }
                else if (line.trim().toLowerCase().equalsIgnoreCase("@SchedulerLog")) {
                    currentLogBlock = LOG_BLOCK_SCHEDULER_LOG;    // Reading log block
                    continue;
                }
                else if (line.trim().toLowerCase().equalsIgnoreCase("@AppLog")) {
                    currentLogBlock = LOG_BLOCK_APP_LOG;    // Reading log block
                    continue;
                }
                //else if (line.trim().toLowerCase().equalsIgnoreCase("@HackerLog")) {
                //    currentLogBlock = LOG_BLOCK_HACKER_LOG;    // Reading log block
                //    continue;
                //}
                else if (line.trim().toLowerCase().equalsIgnoreCase("@HackerList")) {
                    currentLogBlock = LOG_BLOCK_HACKER_LIST;
                    continue;
                }
                else if (currentLogBlock == LOG_BLOCK_UNKNOWN) // Unknown lines.
                    continue;


                Boolean parseResult = false;
                switch (currentLogBlock)
                {
                    case LOG_BLOCK_TASK_LIST:
                        parseResult = parseLogLineTaskList(line);
                        break;
                    case LOG_BLOCK_SCHEDULER_LOG:
                    case LOG_BLOCK_APP_LOG:
                    //case LOG_BLOCK_HACKER_LOG:
                    case LOG_BLOCK_MIXED_LOG:
                        parseResult = parseLogLineMixedLog(currentLogBlock, line);
                        break;
                    case LOG_BLOCK_HACKER_LIST:
                        //parseResult = parseLogLineHackerList(line);
                        break;
                    default:
                        break;
                }

                if (parseResult == false)
                {
                    System.err.format("Incorrect log file format at line %d.\n", lineCounter);
                    return false;
                }

            }

            if (currentLogBlock == 0) {
                System.err.format("No valid block in the file.\r\n");
                return false;
            }
        }
        catch (IOException x)
        {
            System.err.format("IOException @ reading file: %s%n", x);
            return false;
        }

        ProgMsg.putLine("%d lines loaded from the log file.", lineCounter);
        System.out.format("%d lines loaded from the log file.\n", lineCounter);

        ProgMsg.putLine("first time stamp: %d", firstTimestamp);
        return true;
    }

    @Override
    public EventContainer getEventContainer() {
        return null;
    }

    /**
     * Parse one log line for task list block.
     * @param line One log line string to be parsed. Format: [taskId, TaskName]
     * @return 'true' for successfully parsing the line, whereas 'false' for not.
     */
    // id, task type, task name, period, computation time, priority
    private Boolean parseLogLineTaskList(String line)
    {
        /* Format: [taskId, taskType, taskName, taskPeriod, taskComputationTime, taskPriority] */
        String splitStrings[] = line.split(",");
        if (splitStrings.length == 6) {
            int taskId = Integer.valueOf(splitStrings[0].trim()).intValue();
            int taskType = Integer.valueOf(splitStrings[1].trim()).intValue();
            String taskTitle = splitStrings[2].trim().substring(1, splitStrings[2].trim().length() - 1);
            long taskPeriod = Integer.valueOf(splitStrings[3].trim()).intValue();
            long taskExecTime = Integer.valueOf(splitStrings[4].trim()).intValue();
            int taskPriority = Integer.valueOf(splitStrings[5].trim()).intValue();

            // Let deadline be the same as period.
            taskSet.addTask( taskId, taskTitle, taskType, taskPeriod, taskPeriod, taskExecTime, taskPriority);
            return true;
        }
        else {
            // Incorrect task list log format.
            System.out.println("Task List is wrong in the log file.");
            return false;
        }
    }

    private Boolean parseLogLineMixedLog(int inCurrentLogBlock, String line)
    {
        /* format: [timestamp, event_taskId, event_data, event_string]. (62039364, 0, 3, "IN") */
        String splitStrings[] = line.split(",");
        if (splitStrings.length == 4) {
            long timeStamp = Double.valueOf(splitStrings[0].trim()).intValue();
            if (firstTimestamp == -1) {
                // Initialize the earliest timestamp
                firstTimestamp = timeStamp;
            }
            /* TODO: the scale of the timestamp should be flexible and configurable. */
            //int timestampNs = (int) ((timeStamp - firstTimestamp) * GuiConfig.TIMESTAMP_UNIT_NS );/// GuiConfig.TRACE_HORIZONTAL_SCALE_DIVIDER);
            timeStamp = timeStamp - firstTimestamp;

            int eventTaskId = Integer.valueOf(splitStrings[1].trim()).intValue();
            int eventData = Integer.valueOf(splitStrings[2].trim()).intValue();
            String eventString = splitStrings[3].trim().substring(1, splitStrings[3].trim().length() - 1);

            switch (inCurrentLogBlock)
            {
                case LOG_BLOCK_SCHEDULER_LOG:
                    eventContainer.addNextEvent(EventContainer.SCHEDULER_EVENT, timeStamp, eventTaskId, eventData, eventString);
                    break;
                case LOG_BLOCK_APP_LOG:
                    eventContainer.addNextEvent(EventContainer.INSTANT_EVENT, timeStamp, eventTaskId, eventData, eventString);
                    break;
                //case LOG_BLOCK_HACKER_LOG:
                    //eventContainer.addNextEvent(EventContainer.HACKER_EVENT, timestampNs, eventTaskId, eventData, eventString);
                    //break;
                default:
                    break;
            }

            return true;
        }
        else {
            // Incorrect task list log format.
            return false;
        }
    }

//    private Boolean parseLogLineHackerList(String line)
//    {
//        String splitStrings[] = line.split(",");
//        if (splitStrings.length == 2) {
//            String hacker = splitStrings[1].trim();
//            if (hacker.equalsIgnoreCase("H")) {
//                HackerEvent.setHighHackerId(Integer.valueOf(splitStrings[0].trim()).intValue());
//                return true;
//            } else if (hacker.equalsIgnoreCase("L")) {
//                HackerEvent.setLowHackerId(Integer.valueOf(splitStrings[0].trim()).intValue());
//                return true;
//            }
//        }
//
//        // Incorrect task list log format.
//        return false;
//    }

    public TaskInstantEvent parseSerialLogString(String inString) {
        String splitStrings[] = inString.split(",");
        if (splitStrings.length == 4) {
            long timeStamp = Double.valueOf(splitStrings[0].trim()).intValue();
            int eventTaskId = Integer.valueOf(splitStrings[1].trim()).intValue();
            int eventData = Integer.valueOf(splitStrings[2].trim()).intValue();
            String eventString = splitStrings[3].trim().substring(1, splitStrings[3].trim().length() - 1);

            Task thisTask = taskSet.getTaskById(eventTaskId);
            if (thisTask == null) {
                taskSet.addTask(eventTaskId, "", Task.TASK_TYPE_APP, 0, 0, 0, 0);
                thisTask = taskSet.getTaskById(eventTaskId);
            }

            return new TaskInstantEvent(timeStamp, thisTask, eventData, eventString);

        } else {
            return null;
        }
    }
}
