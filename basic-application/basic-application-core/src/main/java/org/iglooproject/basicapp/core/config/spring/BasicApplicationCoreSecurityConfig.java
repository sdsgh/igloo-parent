package org.iglooproject.basicapp.core.config.spring;

import static org.iglooproject.basicapp.core.property.BasicApplicationCorePropertyIds.SECURITY_PASSWORD_USER_FORBIDDEN_PASSWORDS;

import org.iglooproject.basicapp.core.business.user.model.BasicUser;
import org.iglooproject.basicapp.core.business.user.model.TechnicalUser;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.core.security.model.BasicApplicationPermission;
import org.iglooproject.basicapp.core.security.model.SecurityOptions;
import org.iglooproject.basicapp.core.security.service.BasicApplicationPermissionEvaluator;
import org.iglooproject.basicapp.core.security.service.BasicApplicationUserDetailsServiceImpl;
import org.iglooproject.basicapp.core.security.service.IBasicApplicationUserDetailsService;
import org.iglooproject.basicapp.core.security.service.ISecurityManagementService;
import org.iglooproject.basicapp.core.security.service.SecurityManagementServiceImpl;
import org.iglooproject.jpa.security.config.spring.DefaultJpaSecurityConfig;
import org.iglooproject.jpa.security.password.rule.SecurityPasswordRulesBuilder;
import org.iglooproject.jpa.security.runas.CoreRunAsManagerImpl;
import org.iglooproject.jpa.security.service.AuthenticationUsernameComparison;
import org.iglooproject.jpa.security.service.ICorePermissionEvaluator;
import org.iglooproject.jpa.security.service.NamedPermissionFactory;
import org.iglooproject.spring.property.service.IPropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.access.intercept.RunAsImplAuthenticationProvider;
import org.springframework.security.access.intercept.RunAsManager;
import org.springframework.security.acls.domain.PermissionFactory;

@Configuration
@Import(DefaultJpaSecurityConfig.class)
public class BasicApplicationCoreSecurityConfig {

	@Autowired
	protected DefaultJpaSecurityConfig defaultJpaSecurityConfig;

	@Autowired
	protected IPropertyService propertyService;

	@Bean
	@Scope(proxyMode = ScopedProxyMode.INTERFACES)
	public ICorePermissionEvaluator permissionEvaluator() {
		return new BasicApplicationPermissionEvaluator();
	}

	@Bean
	public AuthenticationUsernameComparison authenticationUsernameComparison() {
		return AuthenticationUsernameComparison.CASE_SENSITIVE;
	}

	@Bean
	public IBasicApplicationUserDetailsService userDetailsService() {
		BasicApplicationUserDetailsServiceImpl userDetailsService = new BasicApplicationUserDetailsServiceImpl();
		userDetailsService.setAuthenticationUsernameComparison(authenticationUsernameComparison());
		return userDetailsService;
	}

	@Bean
	public PermissionFactory permissionFactory() {
		return new NamedPermissionFactory(BasicApplicationPermission.ALL);
	}

	@Bean
	public RunAsManager runAsManager() {
		CoreRunAsManagerImpl runAsManager = new CoreRunAsManagerImpl();
		runAsManager.setKey(defaultJpaSecurityConfig.getRunAsKey());
		return runAsManager;
	}

	@Bean
	public RunAsImplAuthenticationProvider runAsAuthenticationProvider() {
		RunAsImplAuthenticationProvider runAsAuthenticationProvider = new RunAsImplAuthenticationProvider();
		runAsAuthenticationProvider.setKey(defaultJpaSecurityConfig.getRunAsKey());
		return runAsAuthenticationProvider;
	}

	@Bean
	public ISecurityManagementService securityManagementService() {
		SecurityManagementServiceImpl securityManagementService = new SecurityManagementServiceImpl();
		securityManagementService
			.setOptions(
				TechnicalUser.class,
				new SecurityOptions()
					.passwordAdminRecovery()
					.passwordAdminUpdate()
					.passwordUserRecovery()
					.passwordUserUpdate()
					.passwordRules(
						SecurityPasswordRulesBuilder.start()
							.minMaxLength(User.MIN_PASSWORD_LENGTH, User.MAX_PASSWORD_LENGTH)
							.forbiddenUsername()
							.forbiddenPasswords(propertyService.get(SECURITY_PASSWORD_USER_FORBIDDEN_PASSWORDS))
							.build()
					)
			)
			.setOptions(
				BasicUser.class,
				new SecurityOptions()
					.passwordExpiration()
					.passwordHistory()
					.passwordUserRecovery()
					.passwordUserUpdate()
					.passwordRules(
						SecurityPasswordRulesBuilder.start()
							.minMaxLength(User.MIN_PASSWORD_LENGTH, User.MAX_PASSWORD_LENGTH)
							.forbiddenUsername()
							.forbiddenPasswords(propertyService.get(SECURITY_PASSWORD_USER_FORBIDDEN_PASSWORDS))
							.build()
					)
			)
			.setDefaultOptions(
				SecurityOptions.DEFAULT
			);
		return securityManagementService;
	}

}
