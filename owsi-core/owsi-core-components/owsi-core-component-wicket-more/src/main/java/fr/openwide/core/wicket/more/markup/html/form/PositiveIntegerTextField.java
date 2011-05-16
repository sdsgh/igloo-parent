package fr.openwide.core.wicket.more.markup.html.form;

import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.MinimumValidator;

import fr.openwide.core.wicket.markup.html.form.FormComponentHelper;

public class PositiveIntegerTextField extends TextField<Integer> {
	private static final long serialVersionUID = -3071860178961793589L;
	
	private static IValidator<Integer> minimumValidator = new MinimumValidator<Integer>(0);
	
	public PositiveIntegerTextField(String id, IModel<Integer> model, String fieldName) {
		super(id, model, Integer.class);
		add(minimumValidator);
		FormComponentHelper.setLabel(this, fieldName);
	}
}
