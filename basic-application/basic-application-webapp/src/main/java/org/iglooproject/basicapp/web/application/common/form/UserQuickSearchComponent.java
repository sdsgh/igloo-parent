package org.iglooproject.basicapp.web.application.common.form;

import java.util.List;

import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.model.IModel;
import org.iglooproject.basicapp.core.business.user.model.User;
import org.iglooproject.basicapp.web.application.administration.template.AdministrationUserDetailTemplate;
import org.iglooproject.basicapp.web.application.common.typedescriptor.user.UserTypeDescriptor;
import org.iglooproject.wicket.more.markup.html.form.AbstractQuickSearchComponent;
import org.iglooproject.wicket.more.model.GenericEntityModel;

public class UserQuickSearchComponent<U extends User> extends AbstractQuickSearchComponent<U> {

	private static final long serialVersionUID = -7717935272455937918L;

	private static final UserChoiceRenderer USER_CHOICE_RENDERER = new UserChoiceRenderer();
	
	private final UserTypeDescriptor<U> typeDescriptor;

	public UserQuickSearchComponent(String id, UserTypeDescriptor<U> typeDescriptor) {
		this(id, new GenericEntityModel<Long, U>(), typeDescriptor);
	}

	private UserQuickSearchComponent(String id, IModel<U> userModel, UserTypeDescriptor<U> typeDescriptor) {
		super(id, userModel, USER_CHOICE_RENDERER,
				AdministrationUserDetailTemplate.<U>mapper().ignoreParameter2().map(userModel));
		this.typeDescriptor = typeDescriptor;
	}

	@Override
	protected List<U> searchAutocomplete(String term, int limit, int offset) {
		return typeDescriptor.newSearchQuery().nameAutocomplete(term).list(offset, limit);
	}

	private static final class UserChoiceRenderer extends ChoiceRenderer<User> {
		private static final long serialVersionUID = 1L;
		
		@Override
		public Object getDisplayValue(User user) {
			return user != null ? user.getFullName() : "";
		}
		
		@Override
		public String getIdValue(User user, int index) {
			return user != null ? user.getId().toString() : "-1";
		}
	}
}
