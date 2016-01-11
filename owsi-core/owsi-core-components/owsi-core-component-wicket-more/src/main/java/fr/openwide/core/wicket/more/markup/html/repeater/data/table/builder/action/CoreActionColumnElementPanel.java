package fr.openwide.core.wicket.more.markup.html.repeater.data.table.builder.action;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;

import fr.openwide.core.wicket.more.markup.html.basic.PlaceholderContainer;

public abstract class CoreActionColumnElementPanel<T> extends Panel {

	private static final long serialVersionUID = -1236107673112549105L;

	public CoreActionColumnElementPanel(String id, final IModel<T> rowModel) {
		super(id);
		
		Component link = getLink("link", rowModel);
		add(link);
		add(getPlaceholder("placeholder", rowModel).component(link));
	}

	protected abstract Component getLink(String string, IModel<T> rowModel);

	protected abstract PlaceholderContainer getPlaceholder(String string, IModel<T> rowModel);

}