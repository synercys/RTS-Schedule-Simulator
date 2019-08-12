package synercys.rts.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import synercys.rts.framework.Histogram;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class ExcelLogHandlerTest {
    ExcelLogHandler excelLogHandler = new ExcelLogHandler();

    @Test
    void createNewSheet() {
    }

    @Test
    void genRowSchedulerIntervalEvents() {
    }

    @Test
    void genSchedulerIntervalEventsOnLadderDiagram() {
    }

    @Test
    void genIntervalsOnLadderDiagram() {
    }

    @Test
    void genRowBusyIntervals() {
    }

    @Test
    void genHistogramRow() {
        Histogram histogram = new Histogram();
        int occurrenceRange = 30;
        int occurrenceCountGT = 1000;

        for (int i=0; i<occurrenceCountGT; i++) {
            histogram.touch(new Random().nextInt(occurrenceRange)); // [0...occurrenceRange-1]);
        }

        assertTrue(excelLogHandler.sheet.getRow(0) == null);

        excelLogHandler.genHistogramRow(histogram);

        assertTrue(excelLogHandler.sheet.getRow(0) != null);

        double occurrenceSum = 0.0;
        for (int i=excelLogHandler.columnOffset; i<occurrenceRange+excelLogHandler.columnOffset; i++) {
            if (excelLogHandler.sheet.getRow(0).getCell(i) != null)
                occurrenceSum += excelLogHandler.sheet.getRow(0).getCell(i).getNumericCellValue();
        }

        assertEquals(occurrenceSum,(double)occurrenceCountGT);

        //excelLogHandler.saveAndClose(null);
    }

    @Test
    void saveAndClose() {
    }
}