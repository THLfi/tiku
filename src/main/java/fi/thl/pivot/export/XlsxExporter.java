package fi.thl.pivot.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import fi.thl.pivot.model.Dimension;
import fi.thl.pivot.model.DimensionNode;
import fi.thl.pivot.model.Label;
import fi.thl.pivot.model.Pivot;
import fi.thl.pivot.model.PivotCell;

public class XlsxExporter {

    private static final short FONT_SIZE = (short) 10;
    private static final String FONT_FAMILY = "Arial";
    private String language = "fi";
    private MessageSource messageSource;
    private Locale locale;
    private CellStyle numberStyle;
    private CellStyle headerStyle;
    private CellStyle headerLastRowStyle;
    private CellStyle defaultStyle;
    private Map<Integer, CellStyle> decimalStyles = new HashMap<>();
    private Font valueFont;

    public XlsxExporter(String language, MessageSource messageSource) {
        this.language = language;
        this.locale = new Locale(language);
        this.messageSource = messageSource;
    }

    public void export(Model model, OutputStream out) throws IOException {
        try {
            doExport(model, out);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private void doExport(Model model, OutputStream out) throws IOException {

        Map<String, ?> params = model.asMap();
        Workbook wb = new XSSFWorkbook();

        valueFont = createValueFont(wb);

        this.defaultStyle = wb.createCellStyle();
        defaultStyle.setFont(valueFont);
        defaultStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        this.numberStyle = wb.createCellStyle();
        numberStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("#,##0"));
        numberStyle.setFont(valueFont);
        numberStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);

        Font headerFont = createHeaderFont(wb);
        createHeaderStyle(wb, headerFont);

        this.headerLastRowStyle = wb.createCellStyle();
        headerLastRowStyle.setFont(headerFont);
        headerLastRowStyle.setWrapText(true);
        headerLastRowStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
        headerLastRowStyle.setBorderBottom((short) 1);

        Sheet sheet = wb
                .createSheet(WorkbookUtil.createSafeSheetName(((Label) params.get("cubeLabel")).getValue(language)));

        Pivot pivot = (Pivot) params.get("pivot");
        int rowNumber = 0;
        boolean showCodes = params.containsKey("sc");

        rowNumber = createColumnHeaders(pivot, sheet, showCodes);
        rowNumber = printData(sheet, pivot, rowNumber, showCodes);
        mergeRowHeaders(sheet, pivot);
        rowNumber = printFilters(params, sheet, rowNumber, pivot.getColumnCount() + pivot.getColumns().size());
        printCopyrightNotice(sheet, rowNumber, params, pivot.getColumnCount() + pivot.getColumns().size());
        printCurrentMeasureIfOnlyOneMeasureShown(params, sheet, pivot);
        mergeTopLeftCorner(sheet, pivot);

        autosizeColumns(sheet, pivot);
        sheet.createFreezePane(pivot.getRows().size(), pivot.getColumns().size());

        wb.write(out);
        wb.close();
    }

    private Font createValueFont(Workbook wb) {
        Font valueFont = wb.createFont();
        valueFont.setBold(false);
        valueFont.setFontName(FONT_FAMILY);
        valueFont.setFontHeightInPoints(FONT_SIZE);
        return valueFont;
    }

    private void createHeaderStyle(Workbook wb, Font headerFont) {
        this.headerStyle = wb.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setWrapText(true);
        headerStyle.setVerticalAlignment(CellStyle.VERTICAL_TOP);
    }

    private Font createHeaderFont(Workbook wb) {
        Font headerFont = wb.createFont();
        headerFont.setBold(true);
        headerFont.setFontName(FONT_FAMILY);
        headerFont.setFontHeightInPoints(FONT_SIZE);
        return headerFont;
    }

    private void mergeTopLeftCorner(Sheet sheet, Pivot pivot) {
        sheet.addMergedRegion(new CellRangeAddress(0, pivot.getColumns().size() - 1, 0, pivot.getRows().size() - 1));
    }

    @SuppressWarnings("unchecked")
    private void printCurrentMeasureIfOnlyOneMeasureShown(Map<String, ?> params, Sheet sheet, Pivot pivot) {
        for (int r = 0; r < pivot.getColumns().size(); ++r) {
            for (int c = 0; c < pivot.getRows().size(); ++c) {
                sheet.getRow(r).createCell(c);
            }
        }
        if (!(Boolean) params.get("multipleMeasuresShown")) {
            Cell c1 = sheet.getRow(0).getCell(0);
            c1.setCellStyle(headerStyle);
            for (DimensionNode f : (Collection<DimensionNode>) params.get("filters")) {
                if ("measure".equals(f.getDimension().getId())) {
                    c1.setCellValue(f.getLabel().getValue(language));
                }
            }
        }
    }

    private int printData(Sheet sheet, Pivot pivot, int rowNumber, boolean showCodes) {
        Row row = null;
        int dataRowNumber = 0;
        int dataColumnNumber = 0;
        for (PivotCell cell : pivot) {
            if (cell.getColumnNumber() == 0) {
                row = createRow(pivot, cell, sheet, rowNumber++, dataRowNumber++, showCodes);
                dataColumnNumber = 0;
            }
            setCellValue(pivot, row, cell, dataColumnNumber++);
        }
        return rowNumber;
    }

    private void autosizeColumns(Sheet sheet, Pivot pivot) {
        for (int i = 0; i < pivot.getColumns().size() + pivot.getColumnCount(); ++i) {
            sheet.autoSizeColumn(i, true);
        }
    }

    private void mergeRowHeaders(Sheet sheet, Pivot pivot) {
        int rowOffset = pivot.getColumns().size();
        for (int c = 0; c < pivot.getRows().size(); ++c) {
            int lastNodeId = 0;
            int firstRowWithSameHeader = 0;
            int lastRowWithSameHeader = 0;
            for (int r = 0; r < pivot.getRowCount(); ++r) {
                DimensionNode node = pivot.getRowAt(c, r);
                if (c == 0 && node.getSurrogateId() == lastNodeId) {
                    ++lastRowWithSameHeader;
                } else if (node.getSurrogateId() == lastNodeId && matchesLeft(pivot, c, r)) {
                    ++lastRowWithSameHeader;
                } else {
                    mergeInRow(sheet, c, firstRowWithSameHeader + rowOffset, lastRowWithSameHeader + rowOffset);
                    firstRowWithSameHeader = r;
                    lastRowWithSameHeader = r;
                    lastNodeId = node.getSurrogateId();
                }
            }
            mergeInRow(sheet, c, firstRowWithSameHeader + rowOffset, lastRowWithSameHeader + rowOffset);
        }
    }

    private boolean matchesLeft(Pivot pivot, int c, int r) {
       if(r == 0) {
           return true;
       }
       for(int cc = c; cc > 0; --cc) {
           DimensionNode a = pivot.getRowAt(cc, r);
           DimensionNode b = pivot.getRowAt(cc, r - 1);
           if(a.getSurrogateId() != b.getSurrogateId()) {
               return false;
           }
       }
       return true;
    }
    
    private boolean matchesTop(Pivot pivot, int r, int c) {
        if(c == 0) {
            return true;
        }
        for(int rr = r; rr > 0; --rr) {
            System.out.println(rr + " " + c);
            DimensionNode a = pivot.getColumnAt(rr, c);
            DimensionNode b = pivot.getColumnAt(rr - 1, c);
            if(a.getSurrogateId() != b.getSurrogateId()) {
                return false;
            }
        }
        return true;
     }

    private String message(String msgKey, String defaultValue) {
        return messageSource.getMessage(msgKey, null, defaultValue, locale);
    }

    private void printCopyrightNotice(Sheet sheet, int initialRowNumber, Map<String, ?> params, int columns) {
        int rowNumber = initialRowNumber + 1;
        Boolean isOpenData = (Boolean) params.get("isOpenData");
        Cell c1 = sheet.createRow(++rowNumber).createCell(0);
        c1.setCellValue(
                String.format("%1$s %2$te.%2$tm.%2$tY", message("cube.updated", "date"), params.get("updated")));
        c1.setCellStyle(defaultStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, columns - 1));

        Cell c2 = sheet.createRow(++rowNumber).createCell(0);
        c2.setCellValue(
                String.format("(c) %s %d %s", message("site.company", "THL"), Calendar.getInstance().get(Calendar.YEAR),
                        isOpenData != null && isOpenData ? ", " + message("site.license.dd", "CC BY 4.0") : ""));
        c2.setCellStyle(defaultStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, columns - 1));

    }

    @SuppressWarnings("unchecked")
    private int printFilters(Map<String, ?> params, Sheet sheet, int initialRowNumber, int columns) {
        int rowNumber = initialRowNumber + 2;
        Row r = sheet.createRow(rowNumber);
        Cell c1 = r.createCell(0);
        c1.setCellValue(message("cube.filter.selected", ""));
        c1.setCellStyle(defaultStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 0, columns - 1));

        for (Dimension d : (Collection<Dimension>) params.get("dimensions")) {
            for (DimensionNode f : (Collection<DimensionNode>) params.get("filters")) {
                if (f.getDimension().getId().equals(d.getId())) {
                    Row filterRow = sheet.createRow(++rowNumber);
                    Cell cell1 = filterRow.createCell(0);
                    cell1.setCellStyle(defaultStyle);
                    cell1.setCellValue(d.getLabel().getValue(language));

                    Cell cell2 = filterRow.createCell(1);
                    cell2.setCellStyle(defaultStyle);
                    cell2.setCellValue(f.getLabel().getValue(language));
                    sheet.addMergedRegion(new CellRangeAddress(rowNumber, rowNumber, 1, columns - 1));

                }
            }
        }

        return rowNumber;
    }

    protected void setCellValue(Pivot pivot, Row row, PivotCell cell, int dataColumnNumber) {
        if (null == cell || null == pivot || null == pivot.getRows() || null == row) {
            return;
        }
        Cell c = row.createCell(dataColumnNumber + pivot.getRows().size());
        if (cell.isNumber()) {
            int d = cell.determineDecimals();
            double decimals = Math.pow(10, d);
            long value = Math.round(cell.getNumberValue() * decimals);
            c.setCellValue(value / decimals);
            c.setCellStyle(measureStyle(c.getSheet().getWorkbook(), d));
        } else {
            c.setCellValue(cell.getValue());
            c.setCellStyle(defaultStyle);
        }
    }

    private CellStyle measureStyle(Workbook wb, int decimals) {
        if (decimals == 0) {
            return numberStyle;
        }
        if (decimalStyles.containsKey(decimals)) {
            return decimalStyles.get(decimals);
        }
        CellStyle style = wb.createCellStyle();
        String format = String.format("#,##0.%0" + decimals + "d", 0);
        style.setDataFormat(
                wb.getCreationHelper().createDataFormat().getFormat(format));
        style.setFont(valueFont);
        decimalStyles.put(decimals, style);
        return style;

    }

    private Row createRow(Pivot pivot, PivotCell cell, Sheet sheet, int rowNumber, int dataRowNumber,
            boolean showCodes) {
        Row row = sheet.createRow(rowNumber);
        for (int rowLevel = 0; rowLevel < pivot.getRows().size(); ++rowLevel) {
            Cell c = row.createCell(rowLevel);
            DimensionNode node = pivot.getRowAt(rowLevel, dataRowNumber);
            if (showCodes) {
                c.setCellValue(node.getCode() + " - " + node.getLabel().getValue(language));
            } else {
                c.setCellValue(node.getLabel().getValue(language));
            }
            c.setCellStyle(headerStyle);
        }
        return row;
    }

    private int createColumnHeaders(Pivot pivot, Sheet sheet, boolean showCodes) {
        int rowNumber = 0;
        int columnOffset = pivot.getRows().size();
        for (int columnLevel = 0; columnLevel < pivot.getColumns().size(); ++columnLevel) {
            Row row = sheet.createRow(rowNumber);

            int firstColumnWithSameHeader = 0;
            int lastColumnWithSameHeader = 0;
            int lastNodeId = 0;

            for (int column = 0; column < pivot.getColumnCount(); ++column) {
                DimensionNode node = pivot.getColumnAt(columnLevel, column);
                if (columnLevel == 0 && node.getSurrogateId() == lastNodeId) {
                    ++lastColumnWithSameHeader;
                } else if(node.getSurrogateId() == lastNodeId && matchesTop(pivot, columnLevel, column)) {
                    ++lastColumnWithSameHeader;
                } else {
                    Cell cell = row.createCell(column + pivot.getRows().size());
                    if (showCodes) {
                        cell.setCellValue(node.getCode() + " - " + node.getLabel().getValue(language));
                    } else {
                        cell.setCellValue(node.getLabel().getValue(language));
                    }
                    if (isLastHeaderRow(pivot, columnLevel)) {
                        cell.setCellStyle(headerLastRowStyle);
                    } else {
                        cell.setCellStyle(headerStyle);
                    }

                    mergeInColumn(sheet, rowNumber, firstColumnWithSameHeader + columnOffset,
                            lastColumnWithSameHeader + columnOffset);
                    firstColumnWithSameHeader = column;
                    lastColumnWithSameHeader = column;
                    lastNodeId = node.getSurrogateId();
                }
            }
            mergeInColumn(sheet, rowNumber, firstColumnWithSameHeader + columnOffset,
                    lastColumnWithSameHeader + columnOffset);
            ++rowNumber;
        }
        return rowNumber;
    }

    private boolean isLastHeaderRow(Pivot pivot, int columnLevel) {
        return columnLevel + 1 == pivot.getColumns().size();
    }

    private void mergeInColumn(Sheet sheet, int rowNumber, int firstColumnWithSameHeader,
            int lastColumnWithSameHeader) {
        if (lastColumnWithSameHeader - firstColumnWithSameHeader > 0) {
            sheet.addMergedRegion(
                    new CellRangeAddress(rowNumber, rowNumber, firstColumnWithSameHeader, lastColumnWithSameHeader));
        }
    }

    private void mergeInRow(Sheet sheet, int columnNumber, int firstRowWithSameHeader, int lastRowWithSameHeader) {
        if (lastRowWithSameHeader - firstRowWithSameHeader > 0) {
            sheet.addMergedRegion(
                    new CellRangeAddress(firstRowWithSameHeader, lastRowWithSameHeader, columnNumber, columnNumber));
        }
    }
}
