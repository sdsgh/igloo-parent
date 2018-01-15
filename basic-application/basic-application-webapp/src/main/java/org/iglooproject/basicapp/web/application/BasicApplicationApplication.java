package org.iglooproject.basicapp.web.application;

import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.ConverterLocator;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.resource.loader.ClassStringResourceLoader;
import org.iglooproject.basicapp.core.business.common.model.PostalCode;
import org.iglooproject.basicapp.core.business.history.model.atomic.HistoryEventType;
import org.iglooproject.basicapp.core.business.user.model.BasicUser;
import org.iglooproject.basicapp.core.business.user.model.TechnicalUser;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.core.business.user.model.UserGroup;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationBasicUserDescriptionPage;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationBasicUserPortfolioPage;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationTechnicalUserDescriptionPage;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationTechnicalUserPortfolioPage;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationUserGroupDescriptionPage;
import org.iglooproject.basicapp.web.application.administration.page.AdministrationUserGroupPortfolioPage;
import org.iglooproject.basicapp.web.application.common.converter.PostalCodeConverter;
import org.iglooproject.basicapp.web.application.common.renderer.AuthorityRenderer;
import org.iglooproject.basicapp.web.application.common.renderer.UserGroupRenderer;
import org.iglooproject.basicapp.web.application.common.renderer.UserRenderer;
import org.iglooproject.basicapp.web.application.common.template.MainTemplate;
import org.iglooproject.basicapp.web.application.common.template.styles.StylesScssResourceReference;
import org.iglooproject.basicapp.web.application.common.template.styles.old.notification.NotificationLessCssResourceReference;
import org.iglooproject.basicapp.web.application.console.notification.demo.page.ConsoleNotificationDemoIndexPage;
import org.iglooproject.basicapp.web.application.history.renderer.HistoryValueRenderer;
import org.iglooproject.basicapp.web.application.navigation.page.HomePage;
import org.iglooproject.basicapp.web.application.navigation.page.MaintenancePage;
import org.iglooproject.basicapp.web.application.profile.page.ProfilePage;
import org.iglooproject.basicapp.web.application.referencedata.page.ReferenceDataPage;
import org.iglooproject.basicapp.web.application.resources.application.BasicApplicationApplicationResources;
import org.iglooproject.basicapp.web.application.resources.business.BasicApplicationBusinessResources;
import org.iglooproject.basicapp.web.application.resources.common.BasicApplicationCommonResources;
import org.iglooproject.basicapp.web.application.resources.console.BasicApplicationConsoleResources;
import org.iglooproject.basicapp.web.application.resources.enums.BasicApplicationEnumResources;
import org.iglooproject.basicapp.web.application.resources.navigation.BasicApplicationNavigationResources;
import org.iglooproject.basicapp.web.application.resources.notifications.BasicApplicationNotificationResources;
import org.iglooproject.basicapp.web.application.security.login.page.SignInPage;
import org.iglooproject.basicapp.web.application.security.password.page.SecurityPasswordCreationPage;
import org.iglooproject.basicapp.web.application.security.password.page.SecurityPasswordExpirationPage;
import org.iglooproject.basicapp.web.application.security.password.page.SecurityPasswordRecoveryPage;
import org.iglooproject.basicapp.web.application.security.password.page.SecurityPasswordResetPage;
import org.iglooproject.infinispan.model.impl.Node;
import org.iglooproject.jpa.more.business.history.model.embeddable.HistoryValue;
import org.iglooproject.jpa.security.business.authority.model.Authority;
import org.iglooproject.spring.property.service.IPropertyService;
import org.iglooproject.wicket.bootstrap4.console.common.model.ConsoleMenuSection;
import org.iglooproject.wicket.bootstrap4.console.maintenance.infinispan.renderer.INodeRenderer;
import org.iglooproject.wicket.bootstrap4.console.navigation.page.ConsoleAccessDeniedPage;
import org.iglooproject.wicket.bootstrap4.console.navigation.page.ConsoleLoginFailurePage;
import org.iglooproject.wicket.bootstrap4.console.navigation.page.ConsoleLoginSuccessPage;
import org.iglooproject.wicket.bootstrap4.console.navigation.page.ConsoleSignInPage;
import org.iglooproject.wicket.bootstrap4.console.template.ConsoleConfiguration;
import org.iglooproject.wicket.bootstrap4.console.template.style.ConsoleLessCssResourceReference;
import org.iglooproject.wicket.more.application.CoreWicketAuthenticatedApplication;
import org.iglooproject.wicket.more.link.descriptor.parameter.CommonParameters;
import org.iglooproject.wicket.more.markup.html.pages.monitoring.DatabaseMonitoringPage;
import org.iglooproject.wicket.more.rendering.BooleanRenderer;
import org.iglooproject.wicket.more.rendering.EnumRenderer;
import org.iglooproject.wicket.more.rendering.LocaleRenderer;
import org.iglooproject.wicket.more.security.page.LoginFailurePage;
import org.iglooproject.wicket.more.security.page.LoginSuccessPage;
import org.iglooproject.wicket.more.util.convert.HibernateProxyAwareConverterLocator;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.ImmutableList;

public class BasicApplicationApplication extends CoreWicketAuthenticatedApplication {
	
	public static final String NAME = "BasicApplicationApplication";
	
	@Autowired
	private IPropertyService propertyService;
	
	public static BasicApplicationApplication get() {
		final Application application = Application.get();
		if (application instanceof BasicApplicationApplication) {
			return (BasicApplicationApplication) application;
		}
		throw new WicketRuntimeException("There is no BasicApplicationApplication attached to current thread " +
				Thread.currentThread().getName());
	}
	
	@Override
	public void init() {
		super.init();
		
		// si on n'est pas en développement, on précharge les feuilles de styles pour éviter la ruée et permettre le remplissage du cache
		if (!propertyService.isConfigurationTypeDevelopment()) {
			preloadStyleSheets(
					ConsoleLessCssResourceReference.get(),
					NotificationLessCssResourceReference.get(),
//					ApplicationAccessLessCssResourceReference.get(),
					StylesScssResourceReference.get()
			);
		}

		getResourceSettings().getStringResourceLoaders().addAll(
				0, // Override the keys in existing resource loaders with the following 
				ImmutableList.of(
						new ClassStringResourceLoader(BasicApplicationApplicationResources.class),
						new ClassStringResourceLoader(BasicApplicationBusinessResources.class),
						new ClassStringResourceLoader(BasicApplicationCommonResources.class),
						new ClassStringResourceLoader(BasicApplicationConsoleResources.class),
						new ClassStringResourceLoader(BasicApplicationEnumResources.class),
						new ClassStringResourceLoader(BasicApplicationNavigationResources.class),
						new ClassStringResourceLoader(BasicApplicationNotificationResources.class)
				)
		);
	}
	
	@Override
	protected IConverterLocator newConverterLocator() {
		ConverterLocator converterLocator = new ConverterLocator();
		
		converterLocator.set(Authority.class, AuthorityRenderer.get());
		converterLocator.set(User.class, UserRenderer.get());
		converterLocator.set(TechnicalUser.class, UserRenderer.get());
		converterLocator.set(BasicUser.class, UserRenderer.get());
		converterLocator.set(UserGroup.class, UserGroupRenderer.get());
		
		converterLocator.set(Locale.class, LocaleRenderer.get());
		converterLocator.set(Boolean.class, BooleanRenderer.withPrefix("common.boolean.yesNo"));
		
		converterLocator.set(HistoryValue.class, HistoryValueRenderer.get());
		converterLocator.set(HistoryEventType.class, EnumRenderer.get());
		
		converterLocator.set(PostalCode.class, PostalCodeConverter.get());
		
		converterLocator.set(Node.class, INodeRenderer.get());
		
		return new HibernateProxyAwareConverterLocator(converterLocator);
	}

	@Override
	protected void mountApplicationPages() {
		
		// Sign in
		mountPage("/login/", getSignInPageClass());
		mountPage("/login/failure/", LoginFailurePage.class);
		mountPage("/login/success/", LoginSuccessPage.class);
		
		mountPage("/security/password/recovery/", SecurityPasswordRecoveryPage.class);
		mountPage("/security/password/expiration/", SecurityPasswordExpirationPage.class);
		mountParameterizedPage("/security/password/reset/", SecurityPasswordResetPage.class);
		mountParameterizedPage("/security/password/creation/", SecurityPasswordCreationPage.class);
		
		// Console sign in
		mountPage("/console/login/", ConsoleSignInPage.class);
		mountPage("/console/login/failure/", ConsoleLoginFailurePage.class);
		mountPage("/console/login/success/", ConsoleLoginSuccessPage.class);
		mountPage("/console/access-denied/", ConsoleAccessDeniedPage.class);
		
		// Profile
		mountPage("/profile/", ProfilePage.class);
		
		// Maintenance
		mountPage("/maintenance/", MaintenancePage.class);
		
		// Administration
		mountPage("/administration/basic-user/", AdministrationBasicUserPortfolioPage.class);
		mountParameterizedPage("/administration/basic-user/${" + CommonParameters.ID + "}/", AdministrationBasicUserDescriptionPage.class);
		mountPage("/administration/technical-user/", AdministrationTechnicalUserPortfolioPage.class);
		mountParameterizedPage("/administration/technical-user/${" + CommonParameters.ID + "}/", AdministrationTechnicalUserDescriptionPage.class);
		mountPage("/administration/user-group/", AdministrationUserGroupPortfolioPage.class);
		mountParameterizedPage("/administration/user-group/${" + CommonParameters.ID + "}/", AdministrationUserGroupDescriptionPage.class);
		
		// Reference data
		mountPage("/reference-data/", ReferenceDataPage.class);
		
		// Console
		ConsoleConfiguration consoleConfiguration = ConsoleConfiguration.build("console", propertyService);
		consoleConfiguration.mountPages(this);
		
		ConsoleMenuSection notificationMenuSection = new ConsoleMenuSection("notificationsMenuSection", "console.notifications",
				"notifications", ConsoleNotificationDemoIndexPage.class);
		consoleConfiguration.addMenuSection(notificationMenuSection);
		
		mountPage("/console/notifications/", ConsoleNotificationDemoIndexPage.class);
		
		// Monitoring
		mountPage("/monitoring/db-access/", DatabaseMonitoringPage.class);
	}

	@Override
	protected void mountApplicationResources() {
		mountStaticResourceDirectory("/application", MainTemplate.class);
	}

	@Override
	protected Class<? extends AuthenticatedWebSession> getWebSessionClass() {
		return BasicApplicationSession.class;
	}

	@Override
	public Class<? extends Page> getHomePage() {
		return HomePage.class;
	}

	@Override
	public Class<? extends WebPage> getSignInPageClass() {
		return SignInPage.class;
	}
	
}
