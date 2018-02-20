package synercys.rts.util;

import synercys.rts.event.BusyIntervalEvent;
import synercys.rts.event.BusyIntervalEventContainer;
import synercys.rts.event.EventContainer;
import synercys.rts.event.SchedulerIntervalEvent;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.*;

//import javafx.scene.paint.Color;

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
        createNewSheet();

        // Set default column width
        sheet.setDefaultColumnWidth(1);

        // Set title column.
        sheet.setColumnWidth(0, 4000);
    }

    public void createNewSheet() {
        sheet = workbook.createSheet("log");
    }

    public void genRowSchedulerIntervalEvents(EventContainer inEvents) {
        Row row = sheet.createRow(rowIndex++);

        // Set title column.
        row.createCell(0).setCellValue("Schedule");

        for (SchedulerIntervalEvent thisScheduleEvent : inEvents.getSchedulerEvents()) {
            for (long i=thisScheduleEvent.getOrgBeginTimestamp(); i<thisScheduleEvent.getOrgEndTimestamp(); i++) {
                if (i+columnOffset > EXCEL_COLUMN_LIMIT) {
                    // Display nothing as it exceeds excel's display limit.
                } else {
                    Cell cell = row.createCell((int) (i + columnOffset));
                    cell.setCellValue(thisScheduleEvent.getTask().getId());
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


//    private void setCellColor(XSSFCell inCell) {
//        XSSFCellStyle cellStyle = workbook.createCellStyle();
//        cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
//        cellStyle.setFillForegroundColor(XSSFColor.toXSSFColor());
//        //new XSSFColor()
//        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        inCell.setCellStyle(cellStyle);
//    }

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
