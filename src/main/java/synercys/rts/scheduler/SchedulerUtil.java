package synercys.rts.scheduler;

import cy.utility.Class;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

public class SchedulerUtil {
    public static String SCHEDULER_RM = "RM";
    public static String SCHEDULER_EDF = "EDF";
    public static String SCHEDULER_TASKSHUFFLER = "TaskShuffler";
    public static String SCHEDULER_REORDER = "ReOrder";

    public static AdvanceableSchedulerInterface getScheduler(String schedulingPolicy, TaskSet taskSet, boolean executionVariation) {
        if (schedulingPolicy.isEmpty() || schedulingPolicy.equalsIgnoreCase("RM") || schedulingPolicy.equalsIgnoreCase("TaskShuffler0"))
            return new FixedPriorityScheduler(taskSet, executionVariation);
        else if (schedulingPolicy.equalsIgnoreCase("EDF") || schedulingPolicy.equalsIgnoreCase("ReOrder0"))
            return new EdfScheduler(taskSet, executionVariation);
        else if (schedulingPolicy.substring(0,"ReOrder".length()).equalsIgnoreCase("ReOrder")) {  // ReOrder1, 2, 3, 4
            String randomizationLevelStr = schedulingPolicy.substring("ReOrder".length());
            int randomizationLevel;
            if (randomizationLevelStr.equalsIgnoreCase("")) {
                randomizationLevel = 4;
            } else {
                randomizationLevel = Integer.valueOf(randomizationLevelStr);
            }
            ReorderScheduler scheduler = new ReorderScheduler(taskSet, executionVariation);
            scheduler.setRandomizationLevel(randomizationLevel);
            return scheduler;
        } else if (schedulingPolicy.substring(0,"TaskShuffler".length()).equalsIgnoreCase("TaskShuffler")) {  // TaskShuffler1, 2, 3, 4
            String randomizationLevelStr = schedulingPolicy.substring("TaskShuffler".length());
            int randomizationLevel;
            if (randomizationLevelStr.equalsIgnoreCase("")) {
                randomizationLevel = 4;
            } else {
                randomizationLevel = Integer.valueOf(randomizationLevelStr);
            }
            TaskShufflerScheduler scheduler = new TaskShufflerScheduler(taskSet, executionVariation);
            scheduler.setRandomizationLevel(randomizationLevel);
            return scheduler;
        } else  // Use RM by default.
            return new FixedPriorityScheduler(taskSet, executionVariation);
    }

    public static ArrayList<String> getSchedulerNames() {
        // This function is from cy.utility
        return Class.getPrefixMatchedVariableStringValues(SchedulerUtil.class, "SCHEDULER_");
    }

    public static String getSchedulerName(AdvanceableSchedulerInterface scheduler) {
        if (scheduler instanceof TaskShufflerScheduler)
            return SCHEDULER_TASKSHUFFLER;
        if (scheduler instanceof ReorderScheduler)
            return SCHEDULER_REORDER;
        if (scheduler instanceof FixedPriorityScheduler)
            return SCHEDULER_RM;
        if (scheduler instanceof EdfScheduler)
            return SCHEDULER_EDF;
        return "Unknown";
    }
}
