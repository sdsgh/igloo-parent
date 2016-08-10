package fr.openwide.core.wicket.more.link.descriptor.builder.impl.main;

import org.apache.wicket.Page;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.resource.ResourceReference;
import org.bindgen.BindingRoot;
import org.bindgen.binding.AbstractBinding;
import org.javatuples.Unit;

import com.google.common.base.Predicate;

import fr.openwide.core.wicket.more.condition.Condition;
import fr.openwide.core.wicket.more.link.descriptor.builder.impl.parameter.LinkParameterTypeInformation;
import fr.openwide.core.wicket.more.link.descriptor.builder.state.main.common.IMainState;
import fr.openwide.core.wicket.more.link.descriptor.builder.state.main.generic.IGenericOneMappableParameterMainState;
import fr.openwide.core.wicket.more.link.descriptor.builder.state.parameter.chosen.common.IOneChosenParameterState;
import fr.openwide.core.wicket.more.link.descriptor.builder.state.parameter.mapping.IAddedParameterMappingState;
import fr.openwide.core.wicket.more.link.descriptor.parameter.mapping.factory.ILinkParameterMappingEntryFactory;
import fr.openwide.core.wicket.more.link.descriptor.parameter.validator.factory.ILinkParameterValidatorFactory;
import fr.openwide.core.wicket.more.markup.html.factory.IDetachableFactory;

/**
 * This class exists mainly to avoid having to repeatedly write down TSelf in its expanded form
 * (see {@link OneMappableParameterMainStateImpl}), which would be really verbose.
 * 
 * @see OneMappableParameterMainStateImpl
 * @see IGenericOneMappableParameterMainState
 */
abstract class AbstractGenericOneMappableParameterMainStateImpl
		<
		TSelf extends IMainState<TSelf>,
		TParam1,
		TEarlyTargetDefinitionLinkDescriptor,
		TLateTargetDefinitionPageLinkDescriptor,
		TLateTargetDefinitionResourceLinkDescriptor,
		TLateTargetDefinitionImageResourceLinkDescriptor,
		TEarlyTargetDefinitionResult,
		TLateTargetDefinitionPageResult,
		TLateTargetDefinitionResourceResult,
		TLateTargetDefinitionImageResourceResult
		>
		extends AbstractOneOrMoreMappableParameterMainStateImpl
						<
						TSelf,
						TEarlyTargetDefinitionLinkDescriptor,
						TLateTargetDefinitionPageLinkDescriptor,
						TLateTargetDefinitionResourceLinkDescriptor,
						TLateTargetDefinitionImageResourceLinkDescriptor
						>
		implements IGenericOneMappableParameterMainState
						<
						TSelf,
						TParam1,
						TEarlyTargetDefinitionResult,
						TLateTargetDefinitionPageResult,
						TLateTargetDefinitionResourceResult,
						TLateTargetDefinitionImageResourceResult
						> {

	AbstractGenericOneMappableParameterMainStateImpl(
			NoMappableParameterMainStateImpl<
					TEarlyTargetDefinitionLinkDescriptor,
					TLateTargetDefinitionPageLinkDescriptor,
					TLateTargetDefinitionResourceLinkDescriptor,
					TLateTargetDefinitionImageResourceLinkDescriptor
					> previousState,
			LinkParameterTypeInformation<?> addedParameterType) {
		super(previousState, addedParameterType);
	}

	protected abstract IOneChosenParameterState<
			TSelf,
			TParam1,
			TLateTargetDefinitionPageResult,
			TLateTargetDefinitionResourceResult,
			TLateTargetDefinitionImageResourceResult
			> pickLast();

	@Override
	public IAddedParameterMappingState<TSelf> map(String parameterName) {
		return pickLast().map(parameterName);
	}

	@Override
	public IAddedParameterMappingState<TSelf> map(
			ILinkParameterMappingEntryFactory<? super Unit<IModel<TParam1>>> parameterMappingEntryFactory) {
		return pickLast().map(parameterMappingEntryFactory);
	}

	@Override
	public IAddedParameterMappingState<TSelf> renderInUrl(String parameterName) {
		return pickLast().renderInUrl(parameterName);
	}

	@Override
	public IAddedParameterMappingState<TSelf> renderInUrl(String parameterName,
			AbstractBinding<? super TParam1, ?> binding) {
		return pickLast().renderInUrl(parameterName, binding);
	}

	@Override
	public TSelf validator(
			ILinkParameterValidatorFactory<? super Unit<IModel<TParam1>>> parameterValidatorFactory) {
		return pickLast().validator(parameterValidatorFactory);
	}
	
	@Override
	public TSelf validator(IDetachableFactory<? super Unit<IModel<TParam1>>, ? extends Condition> conditionFactory) {
		return pickLast().validator(conditionFactory);
	}

	@Override
	public TSelf validator(Predicate<? super TParam1> predicate) {
		return pickLast().validator(predicate);
	}

	@Override
	public TSelf permission(String permissionName) {
		return pickLast().permission(permissionName);
	}

	@Override
	public TSelf permission(String firstPermissionName, String... otherPermissionNames) {
		return pickLast().permission(firstPermissionName, otherPermissionNames);
	}

	@Override
	public TSelf permission(BindingRoot<? super TParam1, ?> binding,
			String firstPermissionName, String... otherPermissionNames) {
		return pickLast().permission(binding, firstPermissionName, otherPermissionNames);
	}
	
	@Override
	public TLateTargetDefinitionPageResult page(
			IDetachableFactory<
					? super Unit<IModel<TParam1>>,
					? extends IModel<? extends Class<? extends Page>>
					> pageClassFactory) {
		return pickLast().page(pageClassFactory);
	}
	
	@Override
	public TLateTargetDefinitionResourceResult resource(
			IDetachableFactory<
					? super Unit<IModel<TParam1>>,
					? extends IModel<? extends ResourceReference>
					> resourceReferenceFactory) {
		return pickLast().resource(resourceReferenceFactory);
	}
	
	@Override
	public TLateTargetDefinitionImageResourceResult imageResource(
			IDetachableFactory<
					? super Unit<IModel<TParam1>>,
					? extends IModel<? extends ResourceReference>
					> resourceReferenceFactory) {
		return pickLast().imageResource(resourceReferenceFactory);
	}

}
