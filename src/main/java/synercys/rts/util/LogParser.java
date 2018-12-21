package synercys.rts.util;

import synercys.rts.framework.event.EventContainer;

import java.io.BufferedReader;

/**
 * Created by jjs on 2/13/17.
 */
public interface LogParser {
    public int getParserVersion();
    public Boolean parseLog(BufferedReader fileReader);
    public EventContainer getEventContainer();
}
