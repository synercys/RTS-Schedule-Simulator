package synercys.rts.util;

import cy.utility.file.FileHandler;
import synercys.rts.framework.event.BusyIntervalEventContainer;
import synercys.rts.framework.event.EventContainer;

/**
 * Created by cy on 2/20/2018.
 */
public class LogExporter extends FileHandler {
    public boolean exportBusyIntervalsBinaryString(BusyIntervalEventContainer inBis) {
        if (!isFileWriterOpened())
            return false;

       return writeString(inBis.toBinaryString());
    }

    public boolean exportRawScheduleString(EventContainer inEventContainer) {
        if (!isFileWriterOpened())
            return false;

        return writeString(inEventContainer.toRawScheduleString());
    }
}
