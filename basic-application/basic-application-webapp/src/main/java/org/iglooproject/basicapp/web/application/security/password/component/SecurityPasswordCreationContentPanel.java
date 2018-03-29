package org.iglooproject.basicapp.web.application.security.password.component;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.validation.EqualPasswordInputValidator;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.core.security.service.ISecurityManagementService;
import org.iglooproject.basicapp.web.application.common.typedescriptor.user.UserTypeDescriptor;
import org.iglooproject.basicapp.web.application.common.validator.EmailExistsValidator;
import org.iglooproject.basicapp.web.application.common.validator.UserPasswordValidator;
import org.iglooproject.wicket.markup.html.basic.CoreLabel;
import org.iglooproject.wicket.markup.html.panel.GenericPanel;
import org.iglooproject.wicket.more.markup.html.feedback.FeedbackUtils;
import org.iglooproject.wicket.more.markup.html.form.LabelPlaceholderBehavior;
import org.iglooproject.wicket.more.util.model.Detachables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityPasswordCreationContentPanel extends GenericPanel<User> {

	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPasswordCreationContentPanel.class);

	private final IModel<String> emailModel = Model.of("");

	private final IModel<String> passwordModel = Model.of("");

	@SpringBean
	private ISecurityManagementService securityManagementService;

	public SecurityPasswordCreationContentPanel(String wicketId, IModel<User> userModel) {
		super(wicketId, userModel);
		
		UserTypeDescriptor<?> typeDescriptor = UserTypeDescriptor.get(getModelObject());
		
		Form<?> form = new Form<Void>("form");
		TextField<String> passwordField = new PasswordTextField("password", passwordModel);
		TextField<String> confirmPasswordField = new PasswordTextField("confirmPassword", Model.of(""));
		
		add(form);
		form.add(
				new RequiredTextField<String>("email", emailModel)
						.setLabel(new ResourceModel("business.user.email"))
						.add(EmailAddressValidator.getInstance())
						.add(EmailExistsValidator.get())
						.add(new LabelPlaceholderBehavior()),
				passwordField
						.setLabel(new ResourceModel("business.user.password"))
						.setRequired(true)
						.add(
								new UserPasswordValidator(UserTypeDescriptor.get(userModel.getObject()))
										.userModel(userModel)
						)
						.add(new LabelPlaceholderBehavior()),
				new CoreLabel("passwordHelp",
						new ResourceModel(
								typeDescriptor.securityTypeDescriptor().resourceKeyGenerator().resourceKey("password.help"),
								new ResourceModel(UserTypeDescriptor.USER.securityTypeDescriptor()
										.resourceKeyGenerator().resourceKey("password.help"))
						)
				),
				confirmPasswordField
						.setLabel(new ResourceModel("business.user.confirmPassword"))
						.setRequired(true)
						.add(new LabelPlaceholderBehavior()),
				new AjaxButton("validate", form) {
					private static final long serialVersionUID = 1L;
					
					@Override
					protected void onSubmit(AjaxRequestTarget target) {
						try {
							User user = SecurityPasswordCreationContentPanel.this.getModelObject();
							securityManagementService.updatePassword(user, passwordModel.getObject());
							
							getSession().success(getString("security.password.creation.validate.success"));
							
							throw UserTypeDescriptor.get(user).securityTypeDescriptor()
									.loginSuccessPageLinkDescriptor().newRestartResponseException();
						} catch (RestartResponseException e) {
							throw e;
						} catch (Exception e) {
							LOGGER.error("Error occurred while creating password", e);
							getSession().error(getString("common.error.unexpected"));
						}
						
						FeedbackUtils.refreshFeedback(target, getPage());
					}
					
					@Override
					protected void onError(AjaxRequestTarget target) {
						FeedbackUtils.refreshFeedback(target, getPage());
					}
				}
		);
		form.add(new EqualPasswordInputValidator(passwordField, confirmPasswordField));
	}

	@Override
	protected void onDetach() {
		super.onDetach();
		Detachables.detach(emailModel, passwordModel);
	}

}