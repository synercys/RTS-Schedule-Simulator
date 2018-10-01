package synercys.rts.util;

import org.apache.poi.ss.usermodel.*;
import synercys.rts.event.BusyIntervalEvent;
import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.event.SchedulerIntervalEvent;
import org.apache.poi.xssf.usermodel.*;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by cy on 3/25/2017.
 */
public class ExcelLogHandler {
    private static int EXCEL_COLUMN_LIMIT = 16380;
    private static String DEFAULT_XLSX_FILE_PATH = "out.xlsx";
    XSSFWorkbook workbook;
    XSSFSheet sheet;
    int columnOffset = 1;
    int rowIndex = 0;

    public ExcelLogHandler() {
        workbook = new XSSFWorkbook();
        createNewSheet("log");

        // Set default column width
        sheet.setDefaultColumnWidth(1);

        // Set title column.
        sheet.setColumnWidth(0, 4000);
    }

    public void createNewSheet(String sheetName) {
        sheet = workbook.createSheet(sheetName);
    }

    public void genRowSchedulerIntervalEvents(EventContainer inEvents) {
        Row row = sheet.createRow(rowIndex++);

        // Set title column.
        row.createCell(0).setCellValue("Schedule");

        /* Initialize all the cells with "0" */
        long eventContainerEndTimestamp = inEvents.getEndTimeStamp();
        for (long i=0; i<eventContainerEndTimestamp; i++) {
            if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                // Do nothing if it exceeds excel's display limit.
            } else {
                Cell cell = row.createCell((int) (i + columnOffset));
                cell.setCellValue(0);
            }
        }

        for (SchedulerIntervalEvent thisScheduleEvent : inEvents.getSchedulerEvents()) {
            for (long i=thisScheduleEvent.getOrgBeginTimestamp(); i<thisScheduleEvent.getOrgEndTimestamp(); i++) {
                if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                    // Display nothing as it exceeds excel's display limit.
                } else {
                    //Cell cell = row.createCell((int) (i + columnOffset)); // Create a new cell
                    Cell cell = row.getCell((int) (i + columnOffset));  // Obtain the existing cell
                    cell.setCellValue(thisScheduleEvent.getTask().getId());
                    setCellColor(cell, (short)(thisScheduleEvent.getTask().getId()+1));
                }
            }
        }
    }

    public void genSchedulerIntervalEventsOnLadderDiagram(EventContainer eventContainer, long ladderWidth) {
        if (ladderWidth+columnOffset > EXCEL_COLUMN_LIMIT)
            return;

        /* Fill the first row with width index (timestemp, rather). */
        Row firstRow = sheet.createRow(rowIndex++);
        firstRow.createCell(0).setCellValue("Timestamps");
        for (long i=0; i<ladderWidth; i++) {
            Cell cell = firstRow.createCell((int) i + columnOffset);
            cell.setCellValue(i);
        }

        /* Set lastTimestamp to the first column of the first row where first schedule interval event appears. */
        SchedulerIntervalEvent firstEvent = eventContainer.getSchedulerEvents().get(0);
        long lastTimestamp = (firstEvent.getOrgBeginTimestamp()/ladderWidth)*ladderWidth;
        Row currentRow = null;// = sheet.createRow(rowIndex++);
        //currentRow.createCell(0).setCellValue(lastTimestamp);

        for (SchedulerIntervalEvent thisScheduleEvent : eventContainer.getSchedulerEvents()) {
            for (; lastTimestamp<thisScheduleEvent.getOrgEndTimestamp(); lastTimestamp++) {
                if (lastTimestamp%ladderWidth == 0) {
                    currentRow = sheet.createRow(rowIndex++);
                    currentRow.createCell(0).setCellValue(lastTimestamp);
                }

                Cell cell = currentRow.createCell((int) (lastTimestamp % ladderWidth + columnOffset)); // Create a new cell
                if (lastTimestamp<thisScheduleEvent.getOrgBeginTimestamp()) {
                    /* Fill the gaps with idle task. */
                    cell.setCellValue(0);
                } else {
                    cell.setCellValue(thisScheduleEvent.getTask().getId());
                    setCellColor(cell, (short)(thisScheduleEvent.getTask().getId()+1));
                }

            }
        }
    }

    public void genRowBusyIntervals(BusyIntervalEventContainer inBis) {
        Row row = sheet.createRow(rowIndex++);

        // Set title column.
        row.createCell(0).setCellValue("Busy Intervals");

        for (long i=inBis.getBeginTime(); i<inBis.getEndTime(); i++) {
            if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
            } else {
                Cell cell = row.createCell((int) (i + columnOffset));
                cell.setCellValue("0");
            }
        }

        for (BusyIntervalEvent thisBi : inBis.getBusyIntervals()) {
            for (long i=thisBi.getOrgBeginTimestamp(); i<thisBi.getOrgEndTimestamp(); i++) {
                if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                    // Display nothing as it exceeds excel's display limit.
                } else {
                    Cell cell = row.createCell((int) (i + columnOffset));
                    cell.setCellValue("1");
                }
            }
        }
    }

    private void setColumnWidthRange(int inIndexBegin, int inIndexEnd, int inWidth) {
        for (int i=inIndexBegin; i<=inIndexEnd; i++) {
            sheet.setColumnWidth(i, inWidth);
        }
    }


    private void setCellColor(Cell inCell, short inColorIndex) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();

        /* foreground color */
//        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
        cellStyle.setFillForegroundColor(inColorIndex);
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        /* font color */
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.getIndex());
        cellStyle.setFont(font);

        // Apply style to the cell
        inCell.setCellStyle(cellStyle);
    }

    public Boolean saveAndClose(String inFilePath) {
        String filePath;
        if (inFilePath == null) {
            filePath = DEFAULT_XLSX_FILE_PATH;
        } else {
            filePath = inFilePath;
        }
        try {
            FileOutputStream outputStream = new FileOutputStream(filePath);
            workbook.write(outputStream);
            workbook.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
