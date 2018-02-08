package org.iglooproject.basicapp.web.application.administration.page;

import static org.iglooproject.basicapp.web.application.property.BasicApplicationWebappPropertyIds.PORTFOLIO_ITEMS_PER_PAGE;

import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.Session;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.iglooproject.basicapp.core.business.user.model.UserGroup;
import org.iglooproject.basicapp.core.business.user.service.IUserGroupService;
import org.iglooproject.basicapp.core.util.binding.Bindings;
import org.iglooproject.basicapp.web.application.BasicApplicationSession;
import org.iglooproject.basicapp.web.application.administration.form.UserGroupPopup;
import org.iglooproject.basicapp.web.application.administration.template.AdministrationTemplate;
import org.iglooproject.basicapp.web.application.common.renderer.ActionRenderers;
import org.iglooproject.basicapp.web.application.common.util.CssClassConstants;
import org.iglooproject.commons.util.functional.SerializablePredicate;
import org.iglooproject.spring.property.service.IPropertyService;
import org.iglooproject.wicket.more.link.descriptor.IPageLinkDescriptor;
import org.iglooproject.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import org.iglooproject.wicket.more.link.model.ComponentPageModel;
import org.iglooproject.wicket.more.markup.html.action.AbstractOneParameterAjaxAction;
import org.iglooproject.wicket.more.markup.html.factory.AbstractDetachableFactory;
import org.iglooproject.wicket.more.markup.html.feedback.FeedbackUtils;
import org.iglooproject.wicket.more.markup.html.link.BlankLink;
import org.iglooproject.wicket.more.markup.html.template.js.bootstrap.modal.behavior.AjaxModalOpenBehavior;
import org.iglooproject.wicket.more.markup.html.template.model.BreadCrumbElement;
import org.iglooproject.wicket.more.markup.repeater.table.builder.DataTableBuilder;
import org.iglooproject.wicket.more.model.BindingModel;
import org.iglooproject.wicket.more.model.GenericEntityModel;
import org.iglooproject.wicket.more.model.ReadOnlyCollectionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.wiquery.core.events.MouseEvent;

public class AdministrationUserGroupListPage extends AdministrationTemplate {

	private static final long serialVersionUID = 2733071974944289365L;

	private static final Logger LOGGER = LoggerFactory.getLogger(AdministrationUserGroupListPage.class);

	public static final IPageLinkDescriptor linkDescriptor() {
		return LinkDescriptorBuilder.start()
				.page(AdministrationUserGroupListPage.class);
	}

	@SpringBean
	private IPropertyService propertyService;

	@SpringBean
	private IUserGroupService userGroupService;

	public AdministrationUserGroupListPage(PageParameters parameters) {
		super(parameters);
		
		addBreadCrumbElement(new BreadCrumbElement(new ResourceModel("navigation.administration.userGroup"),
				AdministrationUserGroupListPage.linkDescriptor()));
		
		IModel<List<UserGroup>> userGroupListModel = new LoadableDetachableModel<List<UserGroup>>() {
			private static final long serialVersionUID = 1L;
			@Override
			protected List<UserGroup> load() {
				return userGroupService.list();
			}
		};
		
		UserGroupPopup addPopup = new UserGroupPopup("addPopup");
		
		add(
				addPopup,
				new BlankLink("add")
						.add(new AjaxModalOpenBehavior(addPopup, MouseEvent.CLICK)),
				
				DataTableBuilder.start(ReadOnlyCollectionModel.of(userGroupListModel, GenericEntityModel.factory()))
						.addLabelColumn(new ResourceModel("business.userGroup.name"), Bindings.userGroup().name())
								.withLink(AdministrationUserGroupDetailPage.MAPPER_SOURCE.setParameter2(new ComponentPageModel(this)))
						.addLabelColumn(new ResourceModel("business.userGroup.description"), Bindings.userGroup().description())
								.withClass(CssClassConstants.CELL_HIDDEN_SM_AND_LESS)
						.addActionColumn()
								.addConfirmAction(ActionRenderers.delete())
										.title(new AbstractDetachableFactory<IModel<UserGroup>, IModel<String>>() {
											private static final long serialVersionUID = 1L;
											@Override
											public IModel<String> create(IModel<UserGroup> parameter) {
												return new StringResourceModel(
														"administration.userGroup.action.delete.confirmation.title",
														BindingModel.of(parameter, Bindings.userGroup().name())
												);
											}
										})
										.content(new AbstractDetachableFactory<IModel<UserGroup>, IModel<String>>() {
											private static final long serialVersionUID = 1L;
											@Override
											public IModel<String> create(IModel<UserGroup> parameter) {
												return new StringResourceModel(
														"administration.userGroup.action.delete.confirmation.content",
														BindingModel.of(parameter, Bindings.userGroup().name())
												);
											}
										})
										.confirm()
										.onClick(new AbstractOneParameterAjaxAction<IModel<UserGroup>>() {
											private static final long serialVersionUID = 1L;
											@Override
											public void execute(AjaxRequestTarget target, IModel<UserGroup> parameter) {
												try {
													userGroupService.delete(parameter.getObject());
													Session.get().success(getString("common.success"));
													throw new RestartResponseException(getPage());
												} catch (RestartResponseException e) {
													throw e;
												} catch (Exception e) {
													LOGGER.error("Error when delete a user group.", e);
													getSession().error(getString("common.error.unexpected"));
													FeedbackUtils.refreshFeedback(target, getPage());
												}
											}
										})
										.when(new SerializablePredicate<UserGroup>() {
											private static final long serialVersionUID = 1L;
											@Override
											public boolean apply(UserGroup userGroup) {
												return BasicApplicationSession.get().hasRoleAdmin() && !userGroup.isLocked();
											}
										})
										.withClassOnElements(CssClassConstants.BTN_XS)
								.end()
								.withClass("actions actions-1x")
						.decorate()
								.count("administration.userGroup.list.count")
						.build("results", propertyService.get(PORTFOLIO_ITEMS_PER_PAGE))
		);
	}

	@Override
	protected Class<? extends WebPage> getSecondMenuPage() {
		return AdministrationUserGroupListPage.class;
	}
}
