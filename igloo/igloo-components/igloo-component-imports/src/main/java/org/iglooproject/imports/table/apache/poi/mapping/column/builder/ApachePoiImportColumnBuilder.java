package org.iglooproject.imports.table.apache.poi.mapping.column.builder;

import java.text.NumberFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;

import org.iglooproject.imports.table.apache.poi.util.ApachePoiImportUtils;
import org.iglooproject.imports.table.common.mapping.AbstractTableImportColumnSet;
import org.iglooproject.imports.table.common.mapping.column.builder.AbstractTableImportColumnBuilder;
import org.iglooproject.imports.table.common.mapping.column.builder.ITableImportColumnMapper;
import org.iglooproject.imports.table.common.mapping.column.builder.MappingConstraint;
import org.iglooproject.imports.table.common.mapping.column.builder.state.BigDecimalState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.DateState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.DoubleState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.IntegerState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.LongState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.StringState;
import org.iglooproject.imports.table.common.mapping.column.builder.state.TypeState;

public class ApachePoiImportColumnBuilder extends AbstractTableImportColumnBuilder<Sheet, Row, Cell, CellReference> {
	
	@Override
	public ApachePoiTypeState withHeader(AbstractTableImportColumnSet<Sheet, Row, Cell, CellReference> columnSet, String headerLabel,
			Predicate<? super String> predicate, int indexAmongMatchedColumns, MappingConstraint mappingConstraint) {
		return new ApachePoiTypeState(columnSet, new HeaderLabelApachePoiImportColumnMapper(headerLabel, predicate, indexAmongMatchedColumns, mappingConstraint));
	}

	@Override
	public ApachePoiTypeState withIndex(AbstractTableImportColumnSet<Sheet, Row, Cell, CellReference> columnSet, int columnIndex) {
		return new ApachePoiTypeState(columnSet, new StaticIndexApachePoiImportColumnMapper(columnIndex));
	}

	@Override
	public ApachePoiTypeState unmapped(AbstractTableImportColumnSet<Sheet, Row, Cell, CellReference> columnSet) {
		return new ApachePoiTypeState(columnSet, new UnmappableApachePoiImportColumnMapper());
	}
	
	private static class ApachePoiTypeState extends TypeState<Sheet, Row, Cell, CellReference> {

		public ApachePoiTypeState(AbstractTableImportColumnSet<Sheet, Row, Cell, CellReference> columnSet, ITableImportColumnMapper<Sheet, Row, Cell, CellReference> columnMapper) {
			super(columnSet, columnMapper);
		}
		
		@Override
		public IntegerState<Sheet, Row, Cell, CellReference> asInteger() {
			return asDouble().toInteger();
		}
		
		@Override
		public LongState<Sheet, Row, Cell, CellReference> asLong() {
			return asDouble().toLong(); // Potential loss of data, but we cannot do better using the main API
		}
		
		@Override
		public DoubleState<Sheet, Row, Cell, CellReference> asDouble() {
			return new TypeStateSwitcher<Cell>(Functions.<Cell>identity()).toDouble(new Function<Cell, Double>() {
				@Override
				public Double apply(Cell cell) {
					if (cell == null) {
						return null;
					}
					
					switch(ApachePoiImportUtils.getCellActualValueType(cell)) {
						case NUMERIC:
							return cell.getNumericCellValue();
						default:
							return null;
					}
				}
			});
		}
		
		@Override
		public BigDecimalState<Sheet, Row, Cell, CellReference> asBigDecimal() {
			return asDouble().toBigDecimal(); // Potential loss of data, but we cannot do better using the main API
		}

		@Override
		public StringState<Sheet, Row, Cell, CellReference> asString(final Supplier<? extends NumberFormat> formatIfNumeric) {
			return new TypeStateSwitcher<Cell>(Functions.<Cell>identity()).toString(new Function<Cell, String>() {
				@Override
				public String apply(Cell cell) {
					if (cell == null) {
						return null;
					}
					
					switch(ApachePoiImportUtils.getCellActualValueType(cell)) {
						case NUMERIC:
							return formatIfNumeric.get().format(cell.getNumericCellValue());
						case STRING:
							return StringUtils.trimToNull(cell.getStringCellValue());
						default:
							return null;
					}
				}
			});
		}

		@Override
		public DateState<Sheet, Row, Cell, CellReference> asDate() {
			return new TypeStateSwitcher<Cell>(Functions.<Cell>identity()).toDate(new Function<Cell, Date>() {
				@Override
				public Date apply(Cell cell) {
					if (cell == null) {
						return null;
					}
					
					switch(ApachePoiImportUtils.getCellActualValueType(cell)) {
						case STRING:
							return null;
						default:
							return cell.getDateCellValue();
					}
				}
			});
		}
	}

}
