package fr.openwide.core.imports.excel.mapping;

import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang3.Validate;

import com.google.common.base.Equivalence;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

import fr.openwide.core.commons.util.functional.Predicates2;
import fr.openwide.core.imports.excel.event.ExcelImportEvent.ExcelImportErrorEvent;
import fr.openwide.core.imports.excel.event.ExcelImportEvent.ExcelImportInfoEvent;
import fr.openwide.core.imports.excel.event.ExcelImportEvent;
import fr.openwide.core.imports.excel.event.IExcelImportEventHandler;
import fr.openwide.core.imports.excel.event.exception.ExcelImportContentException;
import fr.openwide.core.imports.excel.event.exception.ExcelImportMappingException;
import fr.openwide.core.imports.excel.location.ExcelImportLocation;
import fr.openwide.core.imports.excel.location.ExcelImportLocationContext;
import fr.openwide.core.imports.excel.location.IExcelImportNavigator;
import fr.openwide.core.imports.excel.mapping.column.IExcelImportColumnDefinition;
import fr.openwide.core.imports.excel.mapping.column.IExcelImportColumnDefinition.IMappedExcelImportColumnDefinition;
import fr.openwide.core.imports.excel.mapping.column.MappedExcelImportColumnDefinitionImpl;
import fr.openwide.core.imports.excel.mapping.column.builder.AbstractColumnBuilder;
import fr.openwide.core.imports.excel.mapping.column.builder.IExcelImportColumnMapper;
import fr.openwide.core.imports.excel.mapping.column.builder.MappingConstraint;
import fr.openwide.core.imports.excel.mapping.column.builder.state.TypeState;

/**
 * The central class of this Excel import framework.
 * See TestApachePoiExcelImporter for an example on how to use this class.
 * @author yrodiere
 */
public abstract class AbstractExcelImportColumnSet<TSheet, TRow, TCell, TCellReference> {
	private static final Comparator<? super String> DEFAULT_HEADER_LABEL_COLLATOR;
	static {
		Collator collator = Collator.getInstance(Locale.ROOT);
		collator.setStrength(Collator.IDENTICAL);
		DEFAULT_HEADER_LABEL_COLLATOR = Ordering.from(collator).nullsFirst();
	}
	
	private final Comparator<? super String> defaultHeaderLabelCollator;
	
	private final AbstractColumnBuilder<TSheet, TRow, TCell, TCellReference> builder;
	
	private final Collection<Column<?>> columns = Lists.newArrayList();
	
	public AbstractExcelImportColumnSet(AbstractColumnBuilder<TSheet, TRow, TCell, TCellReference> builder) {
		this(builder, DEFAULT_HEADER_LABEL_COLLATOR);
	}
	
	public AbstractExcelImportColumnSet(AbstractColumnBuilder<TSheet, TRow, TCell, TCellReference> builder, Comparator<? super String> defaultHeaderLabelCollator) {
		super();
		this.builder = builder;
		this.defaultHeaderLabelCollator = defaultHeaderLabelCollator;
	}

	public final TypeState<TSheet, TRow, TCell, TCellReference> withHeader(String headerLabel) {
		return withHeader(headerLabel, MappingConstraint.REQUIRED);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withHeader(String headerLabel, Comparator<? super String> collator) {
		return withHeader(headerLabel, collator, 0, MappingConstraint.REQUIRED);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withHeader(String headerLabel, MappingConstraint mappingConstraint) {
		return withHeader(headerLabel, 0, mappingConstraint);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withHeader(String headerLabel, int indexAmongMatchedColumns, MappingConstraint mappingConstraint) {
		return withHeader(headerLabel, defaultHeaderLabelCollator, indexAmongMatchedColumns, mappingConstraint);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withHeader(String headerLabel, Equivalence<? super String> headerEquivalence, int indexAmongMatchedColumns, MappingConstraint mappingConstraint) {
		return builder.withHeader(this, headerLabel, headerEquivalence.equivalentTo(headerLabel), indexAmongMatchedColumns, mappingConstraint);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withHeader(String headerLabel, Comparator<? super String> collator, int indexAmongMatchedColumns, MappingConstraint mappingConstraint) {
		return builder.withHeader(this, headerLabel, Predicates2.comparesEqualTo(headerLabel, collator), indexAmongMatchedColumns, mappingConstraint);
	}
	
	public final TypeState<TSheet, TRow, TCell, TCellReference>  withIndex(int index) {
		return builder.withIndex(this, index);
	}
	
	/**
	 * The actual column implementation.
	 * <p>This class is implemented as an inner class in order to get rid of the <TSheet, TRow, TCellReference, TCell, TValue> generic
	 * parameters when the client references columns.
	 */
	public class Column<TValue> implements IExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue> {
		private final IExcelImportColumnMapper<TSheet, TRow, TCell, TCellReference> mapper;
		
		private final Function<? super TCell, ? extends TValue> cellToValueFunction;
		
		private final Predicate<? super TValue> mandatoryValuePredicate;

		public Column(IExcelImportColumnMapper<TSheet, TRow, TCell, TCellReference> mapper,
				Function<? super TCell, ? extends TValue> cellToValueFunction, Predicate<? super TValue> mandatoryValuePredicate) {
			super();
			this.mapper = mapper;
			this.cellToValueFunction = cellToValueFunction;
			this.mandatoryValuePredicate = mandatoryValuePredicate;
			
			// Register the new column
			AbstractExcelImportColumnSet.this.columns.add(this);
		}

		@Override
		public IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue> map(TSheet sheet, IExcelImportNavigator<TSheet, TRow, TCell, TCellReference> navigator,
				IExcelImportEventHandler eventHandler) throws ExcelImportMappingException {
			Function<? super TRow, ? extends TCellReference> rowToCellReferenceFunction = mapper.tryMap(sheet, navigator, eventHandler);
			return new MappedExcelImportColumnDefinitionImpl<TSheet, TRow, TCell, TCellReference, TValue>(sheet, rowToCellReferenceFunction, navigator, cellToValueFunction, mandatoryValuePredicate);
		}
	}
	
	public final SheetContext map(TSheet sheet, IExcelImportNavigator<TSheet, TRow, TCell, TCellReference> navigator, IExcelImportEventHandler eventHandler) throws ExcelImportMappingException {
		return new SheetContext(sheet, navigator, eventHandler);
	}
	
	public final class SheetContext extends ExcelImportLocationContext implements Iterable<RowContext> {
		
		private final TSheet sheet;
		private final IExcelImportNavigator<TSheet, TRow, TCell, TCellReference> navigator;
		private final IExcelImportEventHandler eventHandler;
		
		private final Map<Column<?>, IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, ?>> mappings;
		
		private SheetContext(TSheet sheet, IExcelImportNavigator<TSheet, TRow, TCell, TCellReference> navigator, IExcelImportEventHandler eventHandler)
				throws ExcelImportMappingException {
			super(eventHandler);
			Validate.notNull(sheet);
			
			this.sheet = sheet;
			this.navigator = navigator;
			this.eventHandler = eventHandler;

			Map<Column<?>, IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, ?>> mutableMappings = Maps.newHashMap();
			for (Column<?> columnDefinition : columns) {
				mutableMappings.put(columnDefinition, columnDefinition.map(sheet, navigator, eventHandler));
			}
			this.mappings = Collections.unmodifiableMap(mutableMappings);
			
			this.eventHandler.checkNoMappingErrorOccurred();
		}
		
		public TSheet getSheet() {
			return sheet;
		}
		
		@Override
		public Iterator<RowContext> iterator() {
			return toRowContexts(navigator.rows(sheet));
		}
		
		protected Iterator<RowContext> toRowContexts(Iterator<TRow> rows) {
			return Iterators.transform(rows, new Function<TRow, RowContext>() {
				@Override
				public RowContext apply(TRow input) {
					return row(input);
				}
			});
		}
		
		public Iterable<RowContext> nonEmptyRows() {
			return new Iterable<RowContext>() {
				@Override
				public Iterator<RowContext> iterator() {
					return toRowContexts(navigator.nonEmptyRows(sheet));
				}
			};
		}

		@SuppressWarnings("unchecked")
		private <TValue> IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue> getMappedColumn(
				IExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue> columnDefinition) {
			IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue> mappedColumn =
					(IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, TValue>) mappings.get(columnDefinition);
			if (mappedColumn == null) {
				throw new IllegalStateException("Column " + columnDefinition
						+ " was not properly registered, hence it has not been mapped. Please use AbstractColumns.add() before using AbstractColumns.newMapping().");
			}
			return mappedColumn;
		}
		
		public RowContext row(TRow row) {
			return new RowContext(this, row);
		}
		
		public <TValue> ColumnContext<TValue> column(Column<TValue> columnDefinition) {
			return new ColumnContext<TValue>(this, columnDefinition);
		}
		
		public <TValue> CellContext<TValue> cell(TRow row, Column<TValue> columnDefinition) {
			return row(row).cell(columnDefinition);
		}
		
		/**
		 * @see ExcelImportLocationContext The event recording methods error(), info(), etc. defined in the superclass.
		 */
		@Override
		public ExcelImportLocation getLocation() {
			return navigator.getLocation(sheet, null, null);
		}
		
		public void event(ExcelImportErrorEvent event, String message, TRow row, Object ... args) throws ExcelImportContentException {
			event(event, message, row, null, (Object[])args);
		}
		
		public void event(ExcelImportInfoEvent event, String message, TRow row, Object ... args) {
			event(event, message, row, null, (Object[])args);
		}
		
		public void event(ExcelImportErrorEvent event, String message, TRow row, TCellReference cellReference, Object ... args) throws ExcelImportContentException {
			eventHandler.event(event, navigator.getLocation(sheet, row, cellReference), message, (Object[])args);
		}
		
		public void event(ExcelImportInfoEvent event, String message, TRow row, TCellReference cellReference, Object ... args) {
			eventHandler.event(event, navigator.getLocation(sheet, row, cellReference), message, (Object[])args);
		}
	}
	
	public final class RowContext extends ExcelImportLocationContext {
		
		private final SheetContext sheetContext;
		private final TRow row;

		private RowContext(SheetContext sheetContext, TRow row) {
			super(sheetContext.eventHandler);
			this.sheetContext = sheetContext;
			this.row = row;
		}
		
		public boolean hasContent() {
			return sheetContext.navigator.rowHasContent(row);
		}

		public <TValue> CellContext<TValue> cell(Column<TValue> columnDefinition) {
			return new CellContext<>(this, sheetContext.getMappedColumn(columnDefinition));
		}
		
		/**
		 * @see ExcelImportLocationContext The event recording methods error(), info(), etc. defined in the superclass.
		 */
		@Override
		public ExcelImportLocation getLocation() {
			return sheetContext.navigator.getLocation(sheetContext.sheet, row, null);
		}
		
		public void event(ExcelImportErrorEvent event, String message, TCellReference cellReference, Object ... args) throws ExcelImportContentException {
			sheetContext.event(event, message, row, cellReference, (Object[])args);
		}
		
		public void event(ExcelImportInfoEvent event, String message, TCellReference cellReference, Object ... args) {
			sheetContext.event(event, message, row, cellReference, (Object[])args);
		}
	}
	
	public final class ColumnContext<TValue> {
		
		private final SheetContext sheetContext;
		private final Column<TValue> columnDefinition;

		private ColumnContext(SheetContext sheetContext, Column<TValue> columnDefinition) {
			super();
			this.sheetContext = sheetContext;
			this.columnDefinition = columnDefinition;
		}

		public CellContext<TValue> cell(TRow row) {
			return sheetContext.row(row).cell(columnDefinition);
		}
		
		public boolean exists() {
			return sheetContext.getMappedColumn(columnDefinition).isBound();
		}
	}
	
	public final class CellContext<T> extends ExcelImportLocationContext {

		private final RowContext rowContext;
		private final IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, T> mappedColumn;

		private CellContext(RowContext rowContext, IMappedExcelImportColumnDefinition<TSheet, TRow, TCell, TCellReference, T> mappedColumn) {
			super(rowContext.sheetContext.eventHandler);
			this.rowContext = rowContext;
			this.mappedColumn = mappedColumn;
		}

		public T get() {
			return mappedColumn.getValue(rowContext.row);
		}

		public T getMandatory(String message, Object ... args) throws ExcelImportContentException {
			return getMandatory(ExcelImportEvent.ERROR, message, (Object[])args);
		}

		public T getMandatory(ExcelImportErrorEvent event, String message, Object ... args) throws ExcelImportContentException {
			T value = mappedColumn.getMandatoryValue(rowContext.row);
			if (value == null) {
				event(event, message, (Object[])args);
			}
			return value;
		}

		public T getMandatory(ExcelImportInfoEvent event, String message, Object ... args) {
			T value = mappedColumn.getMandatoryValue(rowContext.row);
			if (value == null) {
				event(event, message, (Object[])args);
			}
			return value;
		}

		public boolean hasContent() {
			return mappedColumn.hasContent(rowContext.row);
		}
		
		/**
		 * @deprecated Use {@link #error(String, Object...)} instead.
		 */
		@Deprecated
		public void missingValue(String message) throws ExcelImportContentException {
			error(message);
		}
		
		/**
		 * @see ExcelImportLocationContext The event recording methods error(), info(), etc. defined in the superclass.
		 */
		@Override
		public ExcelImportLocation getLocation() {
			return rowContext.sheetContext.navigator.getLocation(rowContext.sheetContext.sheet, rowContext.row, mappedColumn.getCellReference(rowContext.row));
		}
	}
}