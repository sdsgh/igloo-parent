package org.iglooproject.basicapp.web.application.console.notification.demo.page;

import java.util.Date;
import java.util.List;

import org.apache.wicket.Session;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.iglooproject.basicapp.core.business.notification.service.INotificationService;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.web.application.BasicApplicationSession;
import org.iglooproject.basicapp.web.application.console.notification.demo.template.ConsoleNotificationDemoTemplate;
import org.iglooproject.basicapp.web.application.console.notification.demo.util.NotificationDemoEntry;
import org.iglooproject.jpa.exception.ServiceException;
import org.iglooproject.spring.notification.exception.NotificationContentRenderingException;
import org.iglooproject.spring.notification.model.INotificationContentDescriptor;
import org.iglooproject.wicket.bootstrap4.console.template.ConsoleTemplate;
import org.iglooproject.wicket.more.link.descriptor.IPageLinkDescriptor;
import org.iglooproject.wicket.more.link.descriptor.builder.LinkDescriptorBuilder;
import org.iglooproject.wicket.more.markup.repeater.collection.SpecificModelCollectionView;
import org.iglooproject.wicket.more.util.model.SequenceProviders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Range;

public class ConsoleNotificationDemoIndexPage extends ConsoleNotificationDemoTemplate {

	private static final long serialVersionUID = -6767518941118385548L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleNotificationDemoIndexPage.class);

	public static final IPageLinkDescriptor linkDescriptor() {
		return LinkDescriptorBuilder.start().page(ConsoleNotificationDemoIndexPage.class);
	}

	public static final String DEFAULT_USERNAME = "admin";

	private static final Range<Long> DEFAULT_ID_RANGE = Range.closed(1L, 100L);

	@SpringBean
	private INotificationService notificationService;

	public ConsoleNotificationDemoIndexPage(PageParameters parameters) {
		super(parameters);
		
		addHeadPageTitleKey("console.notifications");
		
		add(new Link<Void>("sendExample") {
			private static final long serialVersionUID = 1L;
			@Override
			public void onClick() {
				try {
					notificationService.sendExampleNotification(BasicApplicationSession.get().getUser());
					getSession().success(getString("console.notifications.example.send.success"));
				} catch (ServiceException e) {
					LOGGER.error("Error while sending example notification", e);
					getSession().error(getString("common.error.unexpected"));
				}
			}
		});
		
		add(new SpecificModelCollectionView<INotificationContentDescriptor, NotificationDemoEntry>(
				"notifications", SequenceProviders.fromItemModels(createDemoEntries())) {
			private static final long serialVersionUID = 1L;
			@Override
			protected void populateItem(SpecificModelItem item) {
				final NotificationDemoEntry entry = item.getSpecificModel();
				Link<Void> link = new Link<Void>("link") {
					private static final long serialVersionUID = 1L;
					@Override
					public void onClick() {
						try {
							setResponsePage(new NotificationDemoPage(entry));
						} catch (NotificationContentRenderingException e) {
							LOGGER.error("Error while instanciating notification demo page", e);
							Session.get().error(getString("common.error.unexpected"));
						}
					}
				};
				link.add(new Label("label", entry.getLabelModel()));
				item.add(link);
			}
		});
		
		add(new WebMarkupContainer("emptyList") {
			private static final long serialVersionUID = 6700720373087584498L;
			@Override
			public boolean isVisible() {
				return createDemoEntries().isEmpty();
			}
		});
	}

	private List<NotificationDemoEntry> createDemoEntries() {
		return Lists.<NotificationDemoEntry>newArrayList(
				new NotificationDemoEntry("example") {
					private static final long serialVersionUID = 1L;
					@Override
					public INotificationContentDescriptor getDescriptor() {
						return descriptorService.example(getFirstInRange(User.class, DEFAULT_ID_RANGE), new Date());
					}
				}
				// Add new demo entries here
		);
	}
	
	@Override
	protected Class<? extends ConsoleTemplate> getMenuSectionPageClass() {
		return ConsoleNotificationDemoIndexPage.class;
	}
	
	@Override
	protected Class<? extends ConsoleTemplate> getMenuItemPageClass() {
		return ConsoleNotificationDemoIndexPage.class;
	}
}
