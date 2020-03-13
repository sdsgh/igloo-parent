package org.iglooproject.basicapp.web.application.common.template.theme.advanced;

import java.util.List;
import java.util.function.Supplier;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.iglooproject.wicket.behavior.ClassAttributeAppender;
import org.iglooproject.wicket.markup.html.basic.CoreLabel;
import org.iglooproject.wicket.more.condition.Condition;
import org.iglooproject.wicket.more.markup.html.basic.EnclosureContainer;
import org.iglooproject.wicket.more.markup.html.template.model.NavigationMenuItem;

import com.google.common.collect.ImmutableList;

public class SidebarMenuPanel extends Panel {

	private static final long serialVersionUID = -925673378107818377L;

	public SidebarMenuPanel(
		String id,
		Supplier<List<NavigationMenuItem>> mainNavSupplier,
		Supplier<Class<? extends WebPage>> firstMenuPageSupplier,
		Supplier<Class<? extends WebPage>> secondMenuPageSupplier
	) {
		super(id);
		
		add(Condition.anyChildVisible(this).thenShow());
		
		EnclosureContainer sidenavContainer = new EnclosureContainer("sidenavContainer");
		add(sidenavContainer.anyChildVisible());
		
		List<NavigationMenuItem> menuItems =
			mainNavSupplier.get().stream()
				.filter(
					mainNavItem -> {
						return	(mainNavItem.getPageLinkGenerator() != null && Condition.visible(mainNavItem.linkHidingIfInvalid("navLink")).applies())
							||	Condition.componentsAnyVisible(
									mainNavItem.getSubMenuItems()
										.stream()
										.map(subMenuItem -> subMenuItem.linkHidingIfInvalid("navLink"))
										.collect(ImmutableList.toImmutableList())
								).applies();
					}
				)
				.collect(ImmutableList.toImmutableList());
		
		sidenavContainer.add(
			new ListView<NavigationMenuItem>("sidenavItems", menuItems) {
				private static final long serialVersionUID = 1L;
				
				@Override
				protected void populateItem(ListItem<NavigationMenuItem> item) {
					NavigationMenuItem navItem = item.getModelObject();
					MarkupContainer navLink = navItem.linkHidingIfInvalid("navLink");
					
					item.add(navLink);
					
					navLink
						.add(
							new EnclosureContainer("icon")
								.condition(Condition.hasText(navItem.getIconClassesModel()))
								.add(new ClassAttributeAppender(navItem.getIconClassesModel())),
							new CoreLabel("label", navItem.getLabelModel())
						);
					
					EnclosureContainer sidenavSubContainer = new EnclosureContainer("sidenavSubContainer");
					item.add(
						sidenavSubContainer
							.anyChildVisible()
							.setOutputMarkupId(true)
					);
					
					Condition isSubVisibleCondition = Condition.componentVisible(sidenavSubContainer);
					
					IModel<Boolean> isSubOpenModel = Model.of(navItem.getSubMenuForceOpenModel().getObject());
					Condition isSubOpenCondition = Condition.isTrue(isSubOpenModel);
					
					IModel<Boolean> isSubActiveModel = Model.of(false);
					Condition isSubActiveCondition = Condition.isTrue(isSubActiveModel);
					
					List<NavigationMenuItem> subMenuItems =
						navItem.getSubMenuItems().stream()
							.filter(subMenuItem -> Condition.visible(subMenuItem.linkHidingIfInvalid("navLink")).applies())
							.collect(ImmutableList.toImmutableList());
					
					sidenavSubContainer
						.add(
							new ListView<NavigationMenuItem>("sidenavSubItems", subMenuItems) {
								private static final long serialVersionUID = -2257358650754295013L;
								
								@Override
								protected void populateItem(ListItem<NavigationMenuItem> item) {
									NavigationMenuItem navItem = item.getModelObject();
									MarkupContainer navLink = navItem.linkHidingIfInvalid("navLink");
									
									item.add(navLink);
									
									navLink.add(new CoreLabel("label", navItem.getLabelModel()));
									
									item.add(new ClassAttributeAppender(navItem.getCssClassesModel()));
									
									if (navItem.isActive(secondMenuPageSupplier.get())) {
										item.add(new ClassAttributeAppender(Model.of("active")));
										isSubOpenModel.setObject(true);
										isSubActiveModel.setObject(true);
									}
								}
							}
								.setVisibilityAllowed(!subMenuItems.isEmpty())
						);
					
					if (navItem.getPageLinkGenerator() == null) {
						item.add(isSubVisibleCondition.thenShow());
					}
					
					navLink.add(
						new Behavior() {
							private static final long serialVersionUID = 1L;
							@Override
							public void onComponentTag(Component component, ComponentTag tag) {
								tag.put("href", "#" + sidenavSubContainer.getMarkupId());
								tag.put("data-toggle", "collapse");
								tag.put("aria-expanded", isSubOpenModel.getObject().toString());
								super.onComponentTag(component, tag);
							}
							@Override
							public boolean isEnabled(Component component) {
								return isSubVisibleCondition.applies();
							}
						}
					);
					
					navLink.add(
						new ClassAttributeAppender(
							isSubVisibleCondition
								.then(Model.of("sidenav-link-sub"))
								.otherwise(Model.of())
						)
					);
					
					navLink.add(
						new ClassAttributeAppender(
							isSubVisibleCondition.and(isSubOpenCondition.negate())
								.then(Model.of("collapsed"))
								.otherwise(Model.of(""))
						)
					);
					
					sidenavSubContainer.add(
						new ClassAttributeAppender(
							isSubOpenCondition
								.then(Model.of("show"))
								.otherwise(Model.of("collapse"))
						)
					);
					
					item.add(new ClassAttributeAppender(navItem.getCssClassesModel()));
					
					item.add(
						new ClassAttributeAppender(
							isSubVisibleCondition.and(isSubActiveCondition)
								.then(Model.of("active-sub"))
								.otherwise(
									Condition.isTrue(() -> navItem.isActive(firstMenuPageSupplier.get()))
										.then(Model.of("active"))
										.otherwise(Model.of())
								)
						)
					);
				}
			}
				.setVisibilityAllowed(!menuItems.isEmpty())
		);
	}

}
