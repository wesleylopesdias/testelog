package com.logicalis.serviceinsight.web.view;

import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseDocumentBuilder {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	protected XSSFWorkbook workbook;
	
	protected CellStyle headerTableHeaderCellStyle() {
		CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 20);
        style.setFont(font);
        return style;
	}
	
	protected CellStyle headerTableBodyCellStyle() {
		CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
	}
	
	protected CellStyle headerCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.GREY_80_PERCENT.index);
        style.setFillPattern(CellStyle.SOLID_FOREGROUND);
        font.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
	
	protected CellStyle baselineRowCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
	protected CellStyle baselineRowCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        style.setDataFormat((short) 8);
        style.setFont(font);
        return style;
    }
	
	protected CellStyle baselineRowCellPercentStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        style.setDataFormat((short) 10);
        style.setFont(font);
        return style;
    }
	
	protected CellStyle baselineRowDateStyle() {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
	
	protected CellStyle addedRowCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREEN.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
	protected CellStyle addedRowCellDateStyle() {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREEN.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
    
	protected CellStyle addedRowCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat((short) 8);
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.GREEN.index);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        return style;
    }
	
	protected CellStyle removedRowCellStyle() {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.RED.index);
        font.setFontHeightInPoints((short) 12);
        font.setStrikeout(true);
        style.setFont(font);
        return style;
    }
    
	protected CellStyle removedRowCellDateStyle() {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("mm/dd/yyyy"));
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.RED.index);
        font.setFontHeightInPoints((short) 12);
        font.setStrikeout(true);
        style.setFont(font);
        return style;
    }
    
	protected CellStyle removedRowCellCurrencyStyle() {
        CellStyle style = workbook.createCellStyle();
        style.setDataFormat((short) 8);
        Font font = workbook.createFont();
        font.setFontName("Arial");
        font.setColor(HSSFColor.RED.index);
        font.setFontHeightInPoints((short) 12);
        font.setStrikeout(true);
        style.setFont(font);
        return style;
    }
	
}
