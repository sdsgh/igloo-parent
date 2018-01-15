package org.iglooproject.showcase.web.application.widgets.page;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.iglooproject.showcase.core.business.user.model.User;
import org.iglooproject.showcase.core.business.user.service.IUserService;
import org.iglooproject.showcase.web.application.widgets.component.SortableUserListPanel;
import org.iglooproject.wicket.more.link.descriptor.IPageLinkDescriptor;
import org.iglooproject.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import org.iglooproject.wicket.more.markup.html.template.model.BreadCrumbElement;

public class SortableListPage extends WidgetsTemplate {

	private static final long serialVersionUID = -4802009584951257187L;

	public static final IPageLinkDescriptor linkDescriptor() {
		return LinkDescriptorBuilder.start()
				.page(SortableListPage.class);
	}

	@SpringBean
	private IUserService userService;
	
	public SortableListPage(PageParameters parameters) {
		super(parameters);
		
		addBreadCrumbElement(new BreadCrumbElement(new ResourceModel("widgets.menu.sortable"), SortableListPage.linkDescriptor()));
		
		IModel<List<User>> userListModel = new LoadableDetachableModel<List<User>>() {
			private static final long serialVersionUID = 9076101423574115944L;
			
			@Override
			protected List<User> load() {
				List<User> userList = userService.list();
				Collections.sort(userList, new Comparator<User>() {
					@Override
					public int compare(User o1, User o2) {
						return o1.getPosition().compareTo(o2.getPosition());
					}
				});
				
				return userList;
			}
		};
		
		SortableUserListPanel sortablePanel = new SortableUserListPanel("sortableUserPanel", userListModel);
		add(sortablePanel);
	}
	
	@Override
	protected Class<? extends WebPage> getSecondMenuPage() {
		return SortableListPage.class;
	}
}
