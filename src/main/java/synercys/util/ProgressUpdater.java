package synercys.util;

/**
 * Created by CY on 6/9/2015.
 */
public class ProgressUpdater {
    private String progressMessage;
    private Double progressPercent;
    private Boolean isStarted = false;

    private Boolean isFinished = false;

    public ProgressUpdater()
    {
        progressMessage = "";
        progressPercent = 0.0;
        isStarted = false;
        isFinished = false;
    }

    public void setProgressPercent(Double inPercent)
    {
        progressPercent = inPercent;
    }

    public void setProgressMessage(String inMessage)
    {
        progressMessage = inMessage;
    }

    public String getProgressMessage() {
        return progressMessage;
    }

    public Double getProgressPercent() {
        return progressPercent;
    }

    public void setIsStarted(Boolean inIsStarted)
    {
        isStarted = inIsStarted;
    }

    public void setIsFinished(Boolean inIsFinished)
    {
        isFinished = inIsFinished;
    }

    public Boolean isStarted() {
        return isStarted;
    }

    public Boolean isFinished() {
        return isFinished;
    }
}