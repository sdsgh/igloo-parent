package org.iglooproject.wicket.more.markup.html.factory;

import org.iglooproject.commons.util.functional.SerializableFunction;
import org.iglooproject.wicket.more.util.model.Detachables;
import org.javatuples.Unit;

import com.google.common.base.Function;

public final class DetachableFactories {

	private DetachableFactories() {
	}

	public static final <T, R> IDetachableFactory<Unit<? extends T>, R> forUnit(final IDetachableFactory<T, R> factory) {
		return new IDetachableFactory<Unit<? extends T>, R>() {
			private static final long serialVersionUID = 1L;
			@Override
			public R create(Unit<? extends T> parameter) {
				return factory.create(parameter.getValue0());
			}
			@Override
			public void detach() {
				IDetachableFactory.super.detach();
				Detachables.detach(factory);
			}
		};
	}

	public static final <A, B, R> IDetachableFactory<A, R> compose(final IDetachableFactory<B, R> factory,
			final Function<A, ? extends B> function) {
		return new IDetachableFactory<A, R>() {
			private static final long serialVersionUID = 1L;
			@Override
			public R create(A parameter) {
				return factory.create(function.apply(parameter));
			}
			@Override
			public void detach() {
				IDetachableFactory.super.detach();
				Detachables.detach(factory);
			}
		};
	}

	public static final <A, B, R> IDetachableFactory<A, R> compose(final IDetachableFactory<B, R> first,
			final IDetachableFactory<A, ? extends B> second) {
		return new IDetachableFactory<A, R>() {
			private static final long serialVersionUID = 1L;
			@Override
			public R create(A parameter) {
				return first.create(second.create(parameter));
			}
			@Override
			public void detach() {
				IDetachableFactory.super.detach();
				Detachables.detach(first, second);
			}
		};
	}

	public static final <T, R> Function<IDetachableFactory<T, R>, R> toApplyFunction(final T parameter) {
		return new SerializableFunction<IDetachableFactory<T, R>, R>() {
			private static final long serialVersionUID = 1L;
			@Override
			public R apply(IDetachableFactory<T, R> input) {
				return input.create(parameter);
			}
		};
	}

}
