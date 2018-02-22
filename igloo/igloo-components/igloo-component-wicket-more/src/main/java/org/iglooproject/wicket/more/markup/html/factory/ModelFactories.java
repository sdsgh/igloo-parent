package org.iglooproject.wicket.more.markup.html.factory;

import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;
import org.iglooproject.wicket.more.model.ReadOnlyModel;
import org.iglooproject.wicket.more.util.model.Detachables;

import com.google.common.base.Function;

public final class ModelFactories {

	private ModelFactories() {
	}

	public static final <T> IDetachableFactory<IModel<? extends T>, StringResourceModel> stringResourceModel(
			final String resourceKey) {
		return new IDetachableFactory<IModel<? extends T>, StringResourceModel>() {
			private static final long serialVersionUID = 1L;
			@Override
			public StringResourceModel create(IModel<? extends T> parameter) {
				return new StringResourceModel(resourceKey, parameter)
						.setParameters(parameter);
			}
		};
	}

	public static final <T> IDetachableFactory<IModel<? extends T>, StringResourceModel> stringResourceModel(
			final String resourceKey, final Function<? super T, ?> positionalParameterFunction) {
		return new IDetachableFactory<IModel<? extends T>, StringResourceModel>() {
			private static final long serialVersionUID = 1L;
			@Override
			public StringResourceModel create(IModel<? extends T> parameter) {
				return new StringResourceModel(resourceKey, parameter)
						.setParameters(ReadOnlyModel.of(parameter, positionalParameterFunction));
			}
		};
	}

	public static final <R extends IModel<?>, T> IDetachableFactory<T, R> constant(final R model) {
		return new IDetachableFactory<T, R>() {
			private static final long serialVersionUID = 1L;
			@Override
			public R create(T parameter) {
				return model;
			}
			@Override
			public void detach() {
				IDetachableFactory.super.detach();
				Detachables.detach(model);
			}
		};
	}

}
