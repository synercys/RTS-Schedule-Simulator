package synercys.rts.cli;

import picocli.CommandLine;
import picocli.CommandLine.Option;

import java.util.concurrent.Callable;

/**
 * Created by cy on 1/6/2018.
 */

@CommandLine.Command(name = "gentasks", header = "%n@|Generate real-time task sets|@")
public class GenTasks implements Callable {
    protected String[] args;

    @Option(names = {"-n", "--tasknum"}, required = true, description = "The number of tasks in a task set.")
    int taskNum = 5;

    public static void main(String... args) {
        //String[] testArgs = { "-n", "1"};
        String[] testArgs = {};
        args = testArgs;
        CommandLine.call(new GenTasks(), System.err, args);
    }

    @Override
    public Object call() throws Exception {
        System.out.println("Hello, " + taskNum);
        return null;
    }
}
