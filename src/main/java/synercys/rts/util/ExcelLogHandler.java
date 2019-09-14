package synercys.rts.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;

import synercys.rts.framework.Histogram;
import synercys.rts.framework.Interval;
import synercys.rts.framework.Task;
import synercys.rts.framework.event.BusyIntervalEvent;
import synercys.rts.framework.event.BusyIntervalEventContainer;
import synercys.rts.framework.event.EventContainer;
import synercys.rts.framework.event.SchedulerIntervalEvent;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

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

    public void genRowSchedulerIntervalEvents(EventContainer inEvents, boolean showIndependentTaskSchedule) {
        Row row = sheet.createRow(rowIndex++);

        // Set title column.
        row.createCell(0).setCellValue("Schedule");

        /* Initialize all the cells with "0" */
        long eventContainerEndTimestamp = inEvents.getEndTimeStamp();
        createCellsAndSetValues(row, eventContainerEndTimestamp, 0);

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

        /* display schedules for different tasks in separate rows */
        if (showIndependentTaskSchedule) {
            for (Task task : inEvents.getTaskSet().getAppTaskAsArraySortedByPeriod()) {
                genRowTaskSchedule(inEvents, task);
            }
        }

    }

    public void genRowTaskSchedule(EventContainer eventContainer, Task task) {
        Row row = sheet.createRow(rowIndex++);

        // Set title column.
        row.createCell(0).setCellValue(task.getTitle() + " (ID=" + task.getId() + ", T=" + task.getPeriod() + ")");

        /* Initialize all the cells with "0" */
        long eventContainerEndTimestamp = eventContainer.getEndTimeStamp();
        createCellsAndSetValues(row, eventContainerEndTimestamp, null);

        /* execution intervals */
        for (SchedulerIntervalEvent thisScheduleEvent : eventContainer.getSchedulerEvents()) {
            if (thisScheduleEvent.getTask() != task)
                continue;

            for (long i=thisScheduleEvent.getOrgBeginTimestamp(); i<thisScheduleEvent.getOrgEndTimestamp(); i++) {
                if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                    // Display nothing as it exceeds excel's display limit.
                } else {
                    //Cell cell = row.createCell((int) (i + columnOffset)); // Create a new cell
                    Cell cell = row.getCell((int) (i + columnOffset));  // Obtain the existing cell

                    if (thisScheduleEvent.getBeginTimeScheduleState()==SchedulerIntervalEvent.SCHEDULE_STATE_START && i==thisScheduleEvent.getOrgBeginTimestamp()) {
                        cell.setCellValue(thisScheduleEvent.getTask().getId() + "*");
                    } else {
                        cell.setCellValue(thisScheduleEvent.getTask().getId());
                    }

                    setCellColor(cell, (short)(thisScheduleEvent.getTask().getId()+1));
                }
            }
        }

        /* mark arrival time points (with using left boarders) */
        long taskPhase = task.getInitialOffset();
        long taskPeriod = task.getPeriod();
        for (long i=taskPhase; i<eventContainerEndTimestamp && (i+columnOffset<EXCEL_COLUMN_LIMIT); i+=taskPeriod) {
            Cell cell = row.getCell((int) (i + columnOffset));  // Obtain the existing cell

            CellStyle style = cell.getCellStyle();

            /* If the cell style is not set before, create a new instance.
               Note that default cell style has zero index value.
            */
            if (style.getIndex() == 0) {
                style = workbook.createCellStyle();
            }

            style.setBorderLeft(BorderStyle.THICK);
            style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
            cell.setCellStyle(style);
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
                    if (thisScheduleEvent.getBeginTimeScheduleState()==SchedulerIntervalEvent.SCHEDULE_STATE_START
                            && lastTimestamp==thisScheduleEvent.getOrgBeginTimestamp()) {
                        cell.setCellValue(thisScheduleEvent.getTask().getId() + "*");
                    } else {
                        if (thisScheduleEvent.getTask() != null)
                            cell.setCellValue(thisScheduleEvent.getTask().getId());
                    }

                    if (thisScheduleEvent.getTask() != null)
                        setCellColor(cell, (short)(thisScheduleEvent.getTask().getId()+1));
                    else
                        setCellColor(cell, (short)99);

                }

            }
        }
    }


    public void genIntervalsOnLadderDiagram(ArrayList<Interval> intervals, long ladderWidth) {
        if (ladderWidth+columnOffset > EXCEL_COLUMN_LIMIT)
            return;

        EventContainer eventContainer = new EventContainer();
        for (Interval thisInterval : intervals) {
            eventContainer.add(new SchedulerIntervalEvent(thisInterval.getBegin(), thisInterval.getEnd(), null, ""));
        }

        genSchedulerIntervalEventsOnLadderDiagram(eventContainer, ladderWidth);
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

    public void genHistogramRow(Histogram inHistogram) {
        Row row = sheet.createRow(rowIndex++);

        for (long i=0; i<=inHistogram.getEnd(); i++) {
            Cell cell = row.createCell((int) (i + columnOffset));
            cell.setCellValue(Double.valueOf(inHistogram.getValue(i)));
        }

        SheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
        ConditionalFormattingRule rule1 = sheetCF.createConditionalFormattingColorScaleRule();
        ColorScaleFormatting clrFmt = rule1.getColorScaleFormatting();

        /* Setting upper and lower threshold of the color scale.
         * Configuring [0] and [1] for 2-color scale and [0],[2] and [3] for 3-color scale. */
        clrFmt.getThresholds()[0].setRangeType(ConditionalFormattingThreshold.RangeType.MIN);
        clrFmt.getThresholds()[1].setRangeType(ConditionalFormattingThreshold.RangeType.MAX);

        /* Colors of the color-scale.
         * [2] for 2-color scale. [3] for 3-color scale. */
        Color[] colors = new Color[2];
        colors[0] = new XSSFColor(java.awt.Color.WHITE, null);
        colors[1] = new XSSFColor(java.awt.Color.RED, null);
        clrFmt.setColors(colors);

        /* Select range. */
        Cell firstCell = row.getCell(columnOffset);
        String rangeString = firstCell.getAddress().formatAsString() + ":" + row.getCell((int)inHistogram.getEnd()+columnOffset).getAddress().formatAsString();
        CellRangeAddress[] regions = { CellRangeAddress.valueOf(rangeString) };

        sheetCF.addConditionalFormatting(regions, rule1);

    }

    public void genEmptyRow() {
        sheet.createRow(rowIndex++);
    }

    private void setColumnWidthRange(int inIndexBegin, int inIndexEnd, int inWidth) {
        for (int i=inIndexBegin; i<=inIndexEnd; i++) {
            sheet.setColumnWidth(i, inWidth);
        }
    }

    protected void createCellsAndSetValues(Row row, long length, Integer value) {
        if (row == null)
            return;

        /* Initialize all the cells with "0" */
        for (long i=0; i<length; i++) {
            if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                // Do nothing if it exceeds excel's display limit.
            } else {
                Cell cell = row.createCell((int) (i + columnOffset));
                if (value != null)
                    cell.setCellValue(value);
            }
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
