package fr.openwide.core.wicket.more.markup.html.repeater.data.table;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IStyledColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

import com.google.common.collect.Sets;
import com.impossibl.postgres.utils.guava.Joiner;

import fr.openwide.core.jpa.more.business.sort.ISort;
import fr.openwide.core.wicket.more.markup.html.sort.ISortIconStyle;
import fr.openwide.core.wicket.more.markup.html.sort.TableSortLink.CycleMode;

public abstract class AbstractCoreColumn<T, S extends ISort<?>> implements IStyledColumn<T, S>, ICoreColumn<T, S> {
	
	private static final long serialVersionUID = -8616599378805216510L;

	private final IModel<?> headerLabelModel;
	
	private final Set<String> cssClasses = Sets.newHashSet();
	
	private S sortProperty;
	
	private IModel<String> sortTooltipTextModel;
	
	private ISortIconStyle sortIconStyle;
	
	private CycleMode sortCycleMode;

	public AbstractCoreColumn(IModel<?> headerLabelModel) {
		super();
		this.headerLabelModel = checkNotNull(headerLabelModel);
	}
	
	@Override
	public abstract void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel);

	@Override
	public Component getHeader(String componentId) {
		return new Label(componentId, headerLabelModel);
	}

	@Override
	public S getSortProperty() {
		return sortProperty;
	}
	
	@Override
	public void setSortProperty(S sortProperty) {
		this.sortProperty = sortProperty;
	}

	@Override
	public boolean isSortable() {
		return sortProperty != null;
	}

	@Override
	public IModel<String> getSortTooltipTextModel() {
		return sortTooltipTextModel;
	}

	@Override
	public void setSortTooltipTextModel(IModel<String> sortTooltipTextModel) {
		this.sortTooltipTextModel = sortTooltipTextModel;
	}

	@Override
	public ISortIconStyle getSortIconStyle() {
		return sortIconStyle;
	}

	@Override
	public void setSortIconStyle(ISortIconStyle sortIconStyle) {
		this.sortIconStyle = sortIconStyle;
	}

	@Override
	public CycleMode getSortCycleMode() {
		return sortCycleMode;
	}

	@Override
	public void setSortCycleMode(CycleMode sortCycleMode) {
		this.sortCycleMode = sortCycleMode;
	}

	@Override
	public void detach() {
		headerLabelModel.detach();
		if (sortTooltipTextModel != null) {
			sortTooltipTextModel.detach();
		}
	}

	@Override
	public String getCssClass() {
		return Joiner.on(" ").join(cssClasses);
	}
	
	@Override
	public ICoreColumn<T, S> addCssClass(String cssClass) {
		cssClasses.add(cssClass);
		return this;
	}

}
