package fr.openwide.core.wicket.more.markup.html.repeater.data.table;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.bindgen.BindingRoot;

import fr.openwide.core.jpa.more.business.sort.ISort;
import fr.openwide.core.wicket.more.markup.html.bootstrap.label.component.BootstrapBadge;
import fr.openwide.core.wicket.more.markup.html.bootstrap.label.renderer.BootstrapLabelRenderer;
import fr.openwide.core.wicket.more.model.BindingModel;

public class CoreBootstrapBadgeColumn<T, S extends ISort<?>, C> extends AbstractCoreColumn<T, S> {

	private static final long serialVersionUID = -5344972073351010752L;

	private final BindingRoot<? super T, C> binding;

	private final BootstrapLabelRenderer<? super C> renderer;

	public CoreBootstrapBadgeColumn(IModel<?> headerLabelModel, final BindingRoot<? super T, C> binding,
			final BootstrapLabelRenderer<? super C> renderer) {
		super(headerLabelModel);
		this.binding = binding;
		this.renderer = renderer;
	}

	@Override
	public void populateItem(Item<ICellPopulator<T>> cellItem, String componentId, IModel<T> rowModel) {
		cellItem.add(new BootstrapBadge<>(componentId, BindingModel.of(rowModel, binding), renderer));
	}

}
