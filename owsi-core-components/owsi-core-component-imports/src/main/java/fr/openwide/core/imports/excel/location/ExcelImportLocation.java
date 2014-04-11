package fr.openwide.core.imports.excel.location;

import java.io.Serializable;

public class ExcelImportLocation implements Serializable {
	private static final long serialVersionUID = 6866449558201453287L;
	
	private final String fileName;
	private final String sheetName;
	private final Integer rowIndexZeroBased;
	private final String cellAddress;
	
	public ExcelImportLocation(String fileName, String sheetName, Integer rowIndexZeroBased, String cellAddress) {
		super();
		this.fileName = fileName;
		this.sheetName = sheetName;
		this.rowIndexZeroBased = rowIndexZeroBased;
		this.cellAddress = cellAddress;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public String getSheetName() {
		return sheetName;
	}
	
	public Integer getRowIndexZeroBased() {
		return rowIndexZeroBased;
	}
	
	public Integer getRowIndexOneBased() {
		return rowIndexZeroBased == null ? null : rowIndexZeroBased + 1;
	}
	
	public String getCellAddress() {
		return cellAddress;
	}
}