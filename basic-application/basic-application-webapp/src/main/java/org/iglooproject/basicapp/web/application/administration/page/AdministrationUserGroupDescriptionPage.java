package org.iglooproject.basicapp.web.application.administration.page;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.iglooproject.basicapp.core.business.user.model.UserGroup;
import org.iglooproject.basicapp.core.util.binding.Bindings;
import org.iglooproject.basicapp.web.application.administration.component.UserGroupDescriptionPanel;
import org.iglooproject.basicapp.web.application.administration.component.UserGroupMembersPanel;
import org.iglooproject.basicapp.web.application.administration.template.AdministrationTemplate;
import org.iglooproject.basicapp.web.application.navigation.link.LinkFactory;
import org.iglooproject.wicket.markup.html.basic.CoreLabel;
import org.iglooproject.wicket.more.condition.Condition;
import org.iglooproject.wicket.more.link.descriptor.IPageLinkDescriptor;
import org.iglooproject.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import org.iglooproject.wicket.more.link.descriptor.generator.IPageLinkGenerator;
import org.iglooproject.wicket.more.link.descriptor.mapper.IOneParameterLinkDescriptorMapper;
import org.iglooproject.wicket.more.link.descriptor.mapper.ITwoParameterLinkDescriptorMapper;
import org.iglooproject.wicket.more.link.descriptor.parameter.CommonParameters;
import org.iglooproject.wicket.more.link.model.PageModel;
import org.iglooproject.wicket.more.markup.html.template.model.BreadCrumbElement;
import org.iglooproject.wicket.more.model.BindingModel;
import org.iglooproject.wicket.more.model.GenericEntityModel;

public class AdministrationUserGroupDescriptionPage extends AdministrationTemplate {

	private static final long serialVersionUID = -5780326896837623229L;

	public static final IOneParameterLinkDescriptorMapper<IPageLinkDescriptor, UserGroup> MAPPER =
			LinkDescriptorBuilder.start()
					.model(UserGroup.class).map(CommonParameters.ID).mandatory()
					.page(AdministrationUserGroupDescriptionPage.class);
	
	public static final ITwoParameterLinkDescriptorMapper<IPageLinkDescriptor, UserGroup, Page> MAPPER_SOURCE =
			LinkDescriptorBuilder.start()
					.model(UserGroup.class).map(CommonParameters.ID).mandatory()
					.model(Page.class).pickSecond().map(CommonParameters.SOURCE_PAGE_ID).optional()
					.page(AdministrationUserGroupDescriptionPage.class);
	
	public static final IPageLinkDescriptor linkDescriptor(IModel<UserGroup> userGroupModel, IModel<Page> sourcePageModel) {
		return MAPPER_SOURCE.map(userGroupModel, sourcePageModel);
	}

	public AdministrationUserGroupDescriptionPage(PageParameters parameters) {
		super(parameters);
		
		IModel<UserGroup> userGroupModel = new GenericEntityModel<Long, UserGroup>(null);
		IModel<Page> sourcePageModel = new PageModel<Page>();
		
		linkDescriptor(userGroupModel, sourcePageModel)
				.extractSafely(
						parameters,
						AdministrationUserGroupPortfolioPage.linkDescriptor(),
						getString("administration.usergroup.error")
				);
		
		addBreadCrumbElement(new BreadCrumbElement(new ResourceModel("navigation.administration.usergroup"),
				AdministrationUserGroupPortfolioPage.linkDescriptor()));
		
		addBreadCrumbElement(new BreadCrumbElement(BindingModel.of(userGroupModel, Bindings.userGroup().name()),
				AdministrationUserGroupDescriptionPage.linkDescriptor(userGroupModel, sourcePageModel)));
		
		Component backToSourcePage =
				LinkFactory.get().linkGenerator(
						sourcePageModel,
						AdministrationUserGroupPortfolioPage.class
				)
				.link("backToSourcePage").hideIfInvalid();
		
		add(
				new CoreLabel("pageTitle", BindingModel.of(userGroupModel, Bindings.userGroup().name())),
				
				backToSourcePage,
				AdministrationUserGroupPortfolioPage.linkDescriptor().link("backToList")
						.add(Condition.componentVisible(backToSourcePage).thenHide())
		);
		
		add(
				new UserGroupDescriptionPanel("description", userGroupModel),
				new UserGroupMembersPanel("members", userGroupModel)
		);
	}

	public static IPageLinkGenerator linkGenerator(IModel<UserGroup> userGroupModel) {
		return linkDescriptor(userGroupModel, Model.of((Page)null));
	}

	@Override
	protected Class<? extends WebPage> getSecondMenuPage() {
		return AdministrationUserGroupPortfolioPage.class;
	}
}
