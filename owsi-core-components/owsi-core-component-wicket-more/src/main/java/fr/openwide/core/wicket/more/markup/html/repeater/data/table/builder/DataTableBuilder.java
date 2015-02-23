package fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder;

import java.util.Date;
import java.util.Map;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import com.google.common.base.Function;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import fr.openwide.core.commons.util.binding.AbstractCoreBinding;
import fr.openwide.core.jpa.more.business.sort.ISort;
import fr.openwide.core.wicket.markup.html.basic.CoreLabel;
import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.link.descriptor.factory.BindingLinkGeneratorFactory;
import fr.openwide.core.wicket.more.link.descriptor.factory.LinkGeneratorFactory;
import fr.openwide.core.wicket.more.markup.html.bootstrap.label.renderer.BootstrapLabelRenderer;
import fr.openwide.core.wicket.more.markup.html.factory.IParameterizedComponentFactory;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreBooleanLabelColumn;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreBootstrapBadgeColumn;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreBootstrapLabelColumn;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreDataTable;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreHeadersToolbar;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreLabelColumn;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.CoreNoRecordsToolbar;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.DecoratedCoreDataTablePanel;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.DecoratedCoreDataTablePanel.AddInPlacement;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.DecoratedCoreDataTablePanel.AjaxPagerAddInComponentFactory;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.DecoratedCoreDataTablePanel.CountAddInComponentFactory;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.DecoratedCoreDataTablePanel.PagerAddInComponentFactory;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.ICoreColumn;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IAddedBooleanLabelColumnState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IAddedColumnState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IAddedCoreColumnState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IAddedLabelColumnState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IBuildState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IColumnState;
import fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.state.IDecoratedBuildState;
import fr.openwide.core.wicket.more.markup.html.sort.ISortIconStyle;
import fr.openwide.core.wicket.more.markup.html.sort.SortIconStyle;
import fr.openwide.core.wicket.more.markup.html.sort.TableSortLink.CycleMode;
import fr.openwide.core.wicket.more.markup.html.sort.model.CompositeSortModel;
import fr.openwide.core.wicket.more.model.BindingModel;
import fr.openwide.core.wicket.more.model.ReadOnlyModel;
import fr.openwide.core.wicket.more.rendering.Renderer;
import fr.openwide.core.wicket.more.util.IDatePattern;

public final class DataTableBuilder<T, S extends ISort<?>> implements IColumnState<T, S> {

	private final IDataProvider<T> dataProvider;

	private final CompositeSortModel<S> sortModel;

	private final Map<IColumn<T, S>, Condition> columns = Maps.newLinkedHashMap();

	private String noRecordsResourceKey;

	private DataTableBuilder(IDataProvider<T> dataProvider, CompositeSortModel<S> sortModel) {
		super();
		this.dataProvider = dataProvider;
		this.sortModel = sortModel;
	}

	public static <T, S extends ISort<?>> DataTableBuilder<T, S> start(IDataProvider<T> dataProvider, CompositeSortModel<S> sortModel) {
		return new DataTableBuilder<T, S>(dataProvider, sortModel);
	}

	@Override
	public IAddedColumnState<T, S> addColumn(final IColumn<T, S> column) {
		columns.put(column, null);
		return new AddedColumnState<IAddedColumnState<T, S>>() {
			@Override
			protected IColumn<T, S> getColumn() {
				return column;
			}
			@Override
			protected IAddedColumnState<T, S> getNextState() {
				return this;
			}
		};
	}

	@Override
	public IAddedCoreColumnState<T, S> addColumn(final ICoreColumn<T, S> column) {
		columns.put(column, null);
		return new AddedCoreColumnState<IAddedCoreColumnState<T, S>>() {
			@Override
			protected ICoreColumn<T, S> getColumn() {
				return column;
			}
			@Override
			protected IAddedCoreColumnState<T, S> getNextState() {
				return this;
			}
		};
	}

	protected IAddedLabelColumnState<T, S> addLabelColumn(CoreLabelColumn<T, S> column) {
		columns.put(column, null);
		return new AddedLabelColumnState(column);
	}

	@Override
	public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel) {
		return addLabelColumn(new SimpleLabelColumn<T, S>(headerModel));
	}
	
	private static class SimpleLabelColumn<T, S extends ISort<?>> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		public SimpleLabelColumn(IModel<String> headerLabelModel) {
			super(headerLabelModel);
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, rowModel);
		}
	}

	@Override
	public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, final Renderer<? super T> renderer) {
		return addLabelColumn(new RendererLabelColumn<T, S>(headerModel, renderer));
	}
	
	private static class RendererLabelColumn<T, S extends ISort<?>> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		private final Renderer<? super T> renderer;
		public RendererLabelColumn(IModel<String> displayModel, Renderer<? super T> renderer) {
			super(displayModel);
			this.renderer = renderer;
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, renderer.asModel(rowModel));
		}
	}

	@Override
	public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, final Function<? super T, C> function) {
		return addLabelColumn(new FunctionLabelColumn<T, S, C>(headerModel, function));
	}
	
	private static class FunctionLabelColumn<T, S extends ISort<?>, C> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		private final Function<? super T, C> function;
		public FunctionLabelColumn(IModel<String> displayModel, Function<? super T, C> function) {
			super(displayModel);
			this.function = function;
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, ReadOnlyModel.of(rowModel, function));
		}
	}

	@Override
	public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel,
			final Function<? super T, C> function, final Renderer<? super C> renderer) {
		return addLabelColumn(new FunctionRendererLabelColumn<T, S, C>(headerModel, function, renderer));
	}
	
	private static class FunctionRendererLabelColumn<T, S extends ISort<?>, C> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		private final Function<? super T, C> function;
		private final Renderer<? super C> renderer;
		public FunctionRendererLabelColumn(IModel<String> displayModel, Function<? super T, C> function, Renderer<? super C> renderer) {
			super(displayModel);
			this.function = function;
			this.renderer = renderer;
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, renderer.asModel(ReadOnlyModel.of(rowModel, function)));
		}
	}

	@Override
	public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, final AbstractCoreBinding<? super T, C> binding) {
		return addLabelColumn(new BindingLabelColumn<T, S, C>(headerModel, binding));
	}
	
	private static class BindingLabelColumn<T, S extends ISort<?>, C> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		private final AbstractCoreBinding<? super T, C> binding;
		public BindingLabelColumn(IModel<String> displayModel, AbstractCoreBinding<? super T, C> binding) {
			super(displayModel);
			this.binding = binding;
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, BindingModel.of(rowModel, binding));
		}
	}

	@Override
	public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel,
			final AbstractCoreBinding<? super T, C> binding, final Renderer<? super C> renderer) {
		return addLabelColumn(new BindingRendererLabelColumn<T, S, C>(headerModel, binding, renderer));
	}
	
	private static class BindingRendererLabelColumn<T, S extends ISort<?>, C> extends CoreLabelColumn<T, S> {
		private static final long serialVersionUID = 1L;
		private final AbstractCoreBinding<? super T, C> binding;
		private final Renderer<? super C> renderer;
		public BindingRendererLabelColumn(IModel<String> displayModel, AbstractCoreBinding<? super T, C> binding, Renderer<? super C> renderer) {
			super(displayModel);
			this.binding = binding;
			this.renderer = renderer;
		}
		@Override
		protected CoreLabel newLabel(String componentId, IModel<T> rowModel) {
			return new CoreLabel(componentId, renderer.asModel(BindingModel.of(rowModel, binding)));
		}
	}

	@Override
	public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel,
			AbstractCoreBinding<? super T, ? extends Date> binding, IDatePattern datePattern) {
		return addLabelColumn(headerModel, binding, Renderer.fromDatePattern(datePattern));
	}
	
	@Override
	public <C> IAddedCoreColumnState<T, S> addBootstrapLabelColumn(IModel<String> headerModel,
			final AbstractCoreBinding<? super T, C> binding, final BootstrapLabelRenderer<? super C> renderer) {
		return addColumn(new CoreBootstrapLabelColumn<T, S, C>(headerModel, binding, renderer));
	}
	
	@Override
	public <C> IAddedCoreColumnState<T, S> addBootstrapBadgeColumn(IModel<String> headerModel,
			final AbstractCoreBinding<? super T, C> binding, final BootstrapLabelRenderer<? super C> renderer) {
		return addColumn(new CoreBootstrapBadgeColumn<T, S, C>(headerModel, binding, renderer));
	}
	
	@Override
	public <C> IAddedBooleanLabelColumnState<T, S> addBooleanLabelColumn(IModel<String> headerModel,
			final AbstractCoreBinding<? super T, Boolean> binding) {
		CoreBooleanLabelColumn<T, S> column = new CoreBooleanLabelColumn<T, S>(headerModel, binding);
		columns.put(column, null);
		return new AddedBooleanLabelColumnState(column);
	}
	
	@Override
	public DataTableBuilder<T, S> withNoRecordsResourceKey(String noRecordsResourceKey) {
		this.noRecordsResourceKey = noRecordsResourceKey;
		return this;
	}
	
	@Override
	public CoreDataTable<T, S> build(String id) {
		return build(id, Long.MAX_VALUE);
	}

	@Override
	public CoreDataTable<T, S> build(String id, long rowsPerPage) {
		CoreDataTable<T, S> dataTable = new CoreDataTable<T, S>(id, columns, dataProvider, rowsPerPage);
		finalizeBuild(dataTable);
		return dataTable;
	}
	
	protected void finalizeBuild(CoreDataTable<T, S> dataTable) {
		dataTable.addTopToolbar(new CoreHeadersToolbar<S>(dataTable, sortModel));
		dataTable.addBodyBottomToolbar(new CoreNoRecordsToolbar(dataTable, new ResourceModel(noRecordsResourceKey != null ? noRecordsResourceKey : "common.emptyList")));
	}
	
	@Override
	public IDecoratedBuildState<T, S> decorate() {
		return new DecoratedBuildState();
	}

	private abstract class DataTableBuilderWrapper implements IColumnState<T, S> {

		@Override
		public IAddedColumnState<T, S> addColumn(IColumn<T, S> column) {
			return DataTableBuilder.this.addColumn(column);
		}
		
		@Override
		public IAddedCoreColumnState<T, S> addColumn(ICoreColumn<T, S> column) {
			return DataTableBuilder.this.addColumn(column);
		}

		@Override
		public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel) {
			return DataTableBuilder.this.addLabelColumn(headerModel);
		}

		@Override
		public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, Renderer<? super T> renderer) {
			return DataTableBuilder.this.addLabelColumn(headerModel, renderer);
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, AbstractCoreBinding<? super T, C> binding) {
			return DataTableBuilder.this.addLabelColumn(headerModel, binding);
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, AbstractCoreBinding<? super T, C> binding,
				Renderer<? super C> renderer) {
			return DataTableBuilder.this.addLabelColumn(headerModel, binding, renderer);
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, Function<? super T, C> function) {
			return DataTableBuilder.this.addLabelColumn(headerModel, function);
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, Function<? super T, C> function,
				Renderer<? super C> renderer) {
			return DataTableBuilder.this.addLabelColumn(headerModel, function, renderer);
		}

		@Override
		public IAddedLabelColumnState<T, S> addLabelColumn(IModel<String> headerModel, AbstractCoreBinding<? super T, ? extends Date> binding,
				IDatePattern datePattern) {
			return DataTableBuilder.this.addLabelColumn(headerModel, binding, datePattern);
		}

		@Override
		public <C> IAddedCoreColumnState<T, S> addBootstrapLabelColumn(IModel<String> headerModel, AbstractCoreBinding<? super T, C> binding,
				BootstrapLabelRenderer<? super C> renderer) {
			return DataTableBuilder.this.addBootstrapLabelColumn(headerModel, binding, renderer);
		}

		@Override
		public <C> IAddedCoreColumnState<T, S> addBootstrapBadgeColumn(IModel<String> headerModel, AbstractCoreBinding<? super T, C> binding,
				BootstrapLabelRenderer<? super C> renderer) {
			return DataTableBuilder.this.addBootstrapBadgeColumn(headerModel, binding, renderer);
		}
		
		@Override
		public <C> IAddedBooleanLabelColumnState<T, S> addBooleanLabelColumn(IModel<String> headerModel,
				final AbstractCoreBinding<? super T, Boolean> binding) {
			return DataTableBuilder.this.addBooleanLabelColumn(headerModel, binding);
		}

		@Override
		public IBuildState<T, S> withNoRecordsResourceKey(String noRecordsResourceKey) {
			return DataTableBuilder.this.withNoRecordsResourceKey(noRecordsResourceKey);
		}

		@Override
		public CoreDataTable<T, S> build(String id) {
			return DataTableBuilder.this.build(id);
		}

		@Override
		public CoreDataTable<T, S> build(String id, long rowsPerPage) {
			return DataTableBuilder.this.build(id, rowsPerPage);
		}
		
		@Override
		public IDecoratedBuildState<T, S> decorate() {
			return DataTableBuilder.this.decorate();
		}
	}

	private abstract class AddedColumnState<NextState extends IAddedColumnState<T, S>> extends DataTableBuilderWrapper implements IAddedColumnState<T, S> {
		
		protected abstract IColumn<T, S> getColumn();
		
		protected abstract NextState getNextState();

		@Override
		public NextState when(Condition condition) {
			columns.put(getColumn(), condition);
			return getNextState();
		}
		
	}
	
	private abstract class AddedCoreColumnState<NextState extends IAddedCoreColumnState<T, S>> extends AddedColumnState<NextState>
			implements IAddedCoreColumnState<T, S> {
		
		@Override
		protected abstract ICoreColumn<T, S> getColumn();
		
		@Override
		public NextState withClass(String cssClass) {
			getColumn().addCssClass(cssClass);
			return getNextState();
		}

		@Override
		public NextState withSort(S sort) {
			return withSort(sort, SortIconStyle.DEFAULT, CycleMode.NONE_DEFAULT);
		}

		@Override
		public NextState withSort(S sort, ISortIconStyle sortIconStyle) {
			return withSort(sort, sortIconStyle, CycleMode.NONE_DEFAULT);
		}

		@Override
		public NextState withSort(S sort, ISortIconStyle sortIconStyle, CycleMode cycleMode) {
			ICoreColumn<T, S> column = getColumn();
			column.setSortProperty(sort);
			column.setSortCycleMode(cycleMode);
			column.setSortIconStyle(sortIconStyle);
			return getNextState();
		}
	}

	private class AddedLabelColumnState extends AddedCoreColumnState<IAddedLabelColumnState<T, S>> implements IAddedLabelColumnState<T, S> {
		
		private final CoreLabelColumn<T, S> column;
		
		public AddedLabelColumnState(CoreLabelColumn<T, S> column) {
			super();
			this.column = column;
		}

		@Override
		protected CoreLabelColumn<T, S> getColumn() {
			return column;
		}
		
		@Override
		public IAddedLabelColumnState<T, S> getNextState() {
			return this;
		}

		@Override
		public IAddedLabelColumnState<T, S> multiline() {
			getColumn().multiline();
			return this;
		}

		@Override
		public IAddedLabelColumnState<T, S> showPlaceholder() {
			getColumn().showPlaceholder();
			return this;
		}

		@Override
		public IAddedLabelColumnState<T, S> showPlaceholder(IModel<String> placeholderModel) {
			getColumn().showPlaceholder(placeholderModel);
			return this;
		}

		@Override
		public IAddedLabelColumnState<T, S> withLink(LinkGeneratorFactory<T> linkGeneratorFactory) {
			if (getColumn().getSideLinkGeneratorFactory() != null) {
				throw new IllegalStateException("link and side link cannot be both set.");
			}
			getColumn().setLinkGeneratorFactory(linkGeneratorFactory);
			return this;
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> withLink(AbstractCoreBinding<? super T, C> binding, LinkGeneratorFactory<C> linkGeneratorFactory) {
			return withLink(new BindingLinkGeneratorFactory<>(binding, linkGeneratorFactory));
		}

		@Override
		public IAddedLabelColumnState<T, S> withSideLink(LinkGeneratorFactory<T> sideLinkGeneratorFactory) {
			if (getColumn().getLinkGeneratorFactory() != null) {
				throw new IllegalStateException("link and side link cannot be both set.");
			}
			getColumn().setSideLinkGeneratorFactory(sideLinkGeneratorFactory);
			return this;
		}

		@Override
		public <C> IAddedLabelColumnState<T, S> withSideLink(AbstractCoreBinding<? super T, C> binding, LinkGeneratorFactory<C> linkGeneratorFactory) {
			return withSideLink(new BindingLinkGeneratorFactory<>(binding, linkGeneratorFactory));
		}

		@Override
		public IAddedLabelColumnState<T, S> disableIfInvalid() {
			getColumn().disableIfInvalid();
			return this;
		}

		@Override
		public IAddedLabelColumnState<T, S> hideIfInvalid() {
			getColumn().hideIfInvalid();
			return this;
		}
	}

	private class AddedBooleanLabelColumnState extends AddedCoreColumnState<IAddedBooleanLabelColumnState<T, S>> implements IAddedBooleanLabelColumnState<T, S> {
		
		private final CoreBooleanLabelColumn<T, S> column;
		
		public AddedBooleanLabelColumnState(CoreBooleanLabelColumn<T, S> column) {
			super();
			this.column = column;
		}

		@Override
		protected CoreBooleanLabelColumn<T, S> getColumn() {
			return column;
		}
		
		@Override
		public IAddedBooleanLabelColumnState<T, S> hideIfNullOrFalse() {
			getColumn().hideIfNullOrFalse();
			return this;
		}

		@Override
		protected IAddedBooleanLabelColumnState<T, S> getNextState() {
			return this;
		}

	}

	private class DecoratedBuildState implements IDecoratedBuildState<T, S> {
		private String countResourceKey = null;
		
		private final Multimap<AddInPlacement, IParameterizedComponentFactory<?, ? super DecoratedCoreDataTablePanel<T, S>>>
				addInComponentFactories = ArrayListMultimap.create();
		
		@Override
		public IDecoratedBuildState<T, S> count(String countResourceKey) {
			return count(AddInPlacement.TOP_LEFT, countResourceKey);
		}
		
		@Override
		public IDecoratedBuildState<T, S> count(AddInPlacement placement, String countResourceKey) {
			this.countResourceKey = countResourceKey;
			return addIn(placement, new CountAddInComponentFactory(dataProvider, countResourceKey));
		}
		
		@Override
		public IDecoratedBuildState<T, S> pagers() {
			return pager(AddInPlacement.TOP_RIGHT).pager(AddInPlacement.BOTTOM_RIGHT);
		}
		
		@Override
		public IDecoratedBuildState<T, S> pager(AddInPlacement placement) {
			return addIn(placement, new PagerAddInComponentFactory());
		}

		@Override
		public IDecoratedBuildState<T, S> ajaxPagers() {
			return ajaxPager(AddInPlacement.TOP_RIGHT).ajaxPager(AddInPlacement.BOTTOM_RIGHT);
		}
		
		@Override
		public IDecoratedBuildState<T, S> ajaxPager(AddInPlacement placement) {
			return addIn(placement, new AjaxPagerAddInComponentFactory());
		}
		
		@Override
		public IDecoratedBuildState<T, S> addIn(AddInPlacement placement,
				IParameterizedComponentFactory<?, ? super DecoratedCoreDataTablePanel<T, S>> addInComponentFactory) {
			addInComponentFactories.put(placement, addInComponentFactory);
			return this;
		}

		@Override
		public DecoratedCoreDataTablePanel<T, S> build(String id) {
			return build(id, Long.MAX_VALUE);
		}
		
		@Override
		public DecoratedCoreDataTablePanel<T, S> build(String id, long rowsPerPage) {
			DecoratedCoreDataTablePanel<T, S> panel = new DecoratedCoreDataTablePanel<T, S>(id, columns, dataProvider, rowsPerPage,
					addInComponentFactories);
			if (noRecordsResourceKey == null && countResourceKey != null) {
				withNoRecordsResourceKey(countResourceKey + ".zero");
			}
			DataTableBuilder.this.finalizeBuild(panel.getDataTable());
			return panel;
		}
	}
}
