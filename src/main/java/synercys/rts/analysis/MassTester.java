package synercys.rts.analysis;

import cy.utility.Class;
import cy.utility.file.FileHandler;
import org.apache.commons.io.FilenameUtils;
import synercys.rts.analysis.dft.tester.MassScheduleDFTTester;
import synercys.rts.scheduler.TaskSetContainer;

import java.nio.file.Paths;
import java.util.ArrayList;

public abstract class MassTester {
    protected TaskSetContainer taskSetContainer = null;
    protected String schedulingPolicy = "RM";
    protected boolean executionVariation = false;

    protected String logFileFolderPath = "";
    protected String logFileBaseNamePrefix = "";

    public MassTester(String logFilePath, TaskSetContainer taskSetContainer) {
        setLogFilePrefixPath(logFilePath);
        this.taskSetContainer = taskSetContainer;
    }

    public void setSchedulingPolicy(String schedulingPolicy) {
        this.schedulingPolicy = schedulingPolicy;
    }

    public void setExecutionVariation(boolean executionVariation) {
        this.executionVariation = executionVariation;
    }

    static public ArrayList<String> getTestCaseNames() {
        // This function is from cy.utility
        return Class.getPrefixMatchedVariableStringValues(MassScheduleDFTTester.class, "TEST_CASES_");
    }

    protected boolean setLogFilePrefixPath(String logFilePrefixPath) {
        this.logFileFolderPath = FilenameUtils.getFullPath(logFilePrefixPath);
        this.logFileBaseNamePrefix = FilenameUtils.getBaseName(logFilePrefixPath);

        return true;
    }

    protected FileHandler openLogFileToWrite(String fileBaseNameSuffix, String fileExtension) {
        FileHandler newLogFile = new FileHandler();
        newLogFile.openToWriteFile(getLogFullPathFileName(fileBaseNameSuffix, fileExtension));
        return newLogFile;
    }

    protected String getLogFullPathFileName(String baseNameSuffix) {
        return getLogFullPathFileName(baseNameSuffix, "");
    }

    protected String getLogFullPathFileName(String baseNameSuffix, String extension) {
        String fileName = "";

        // File base name
        if (!logFileBaseNamePrefix.isEmpty()) {
            fileName = String.format("%s_", logFileBaseNamePrefix);
        }

        fileName += baseNameSuffix;

        // Extension
        if (!extension.isEmpty()) {
            fileName += "." + extension;
        }

        return Paths.get(logFileFolderPath, fileName).toString();
    }

    abstract public boolean run(String testCase);
}
