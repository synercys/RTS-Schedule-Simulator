package synercys.rts.util;

import cy.utility.file.FileHandler;
import synercys.rts.event.BusyIntervalEvent;
import synercys.rts.event.BusyIntervalEventContainer;

import java.io.IOException;

/**
 * Created by cy on 2/20/2018.
 */
public class LogExporter extends FileHandler {
    public boolean exportBusyIntervalsBinaryString(BusyIntervalEventContainer inBis) {
        if (!isFileWriterOpened())
            return false;

       return writeString(inBis.toBinaryString());
    }
}
