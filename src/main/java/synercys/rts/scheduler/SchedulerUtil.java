package synercys.rts.scheduler;

import cy.utility.Class;
import synercys.rts.framework.TaskSet;

import java.util.ArrayList;

public class SchedulerUtil {
    public static String SCHEDULER_RM = "RM";
    public static String SCHEDULER_EDF = "EDF";
    public static String SCHEDULER_TASKSHUFFLER = "TaskShuffler";
    public static String SCHEDULER_REORDER = "ReOrder";
    public static String SCHEDULER_LAPLACE = "Laplace";

    public static AdvanceableSchedulerSimulator getScheduler(String schedulingPolicy, TaskSet taskSet, boolean executionVariation) {
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
        } else if (schedulingPolicy.substring(0,"Laplace".length()).equalsIgnoreCase("Laplace")) {
            // It could be "Laplace0.01" where "0.01" will be taken as epsilon value
            String epsilonString = schedulingPolicy.substring("Laplace".length());
            double epsilon;
            if (epsilonString.equalsIgnoreCase("")) {
                epsilon = 100.0;    // default value
            } else {
                epsilon = Double.valueOf(epsilonString);
            }
            return new LaplaceScheduler(taskSet, executionVariation, epsilon);
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

    public static String getSchedulerName(AdvanceableSchedulerSimulator scheduler) {
        if (scheduler instanceof LaplaceScheduler)
            return SCHEDULER_LAPLACE;
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
