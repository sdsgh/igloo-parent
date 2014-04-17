package fr.openwide.core.commons.util.functional.builder.function.generic;

import com.google.common.base.Function;

import fr.openwide.core.commons.util.functional.Functions2;
import fr.openwide.core.commons.util.functional.builder.function.BooleanFunctionBuildState;
import fr.openwide.core.commons.util.functional.builder.function.DateFunctionBuildState;
import fr.openwide.core.commons.util.functional.builder.function.DoubleFunctionBuildState;
import fr.openwide.core.commons.util.functional.builder.function.IntegerFunctionBuildState;
import fr.openwide.core.commons.util.functional.builder.function.LongFunctionBuildState;
import fr.openwide.core.commons.util.functional.builder.function.StringFunctionBuildState;

public abstract class GenericBooleanFunctionBuildStateImpl
		<
		TBuildResult,
		TStateSwitcher extends FunctionBuildStateSwitcher<TBuildResult, Boolean, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>,
		TBooleanState extends BooleanFunctionBuildState<TBuildResult, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>,
		TDateState extends DateFunctionBuildState<?, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>, 
		TIntegerState extends IntegerFunctionBuildState<?, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>,
		TLongState extends LongFunctionBuildState<?, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>,
		TDoubleState extends DoubleFunctionBuildState<?, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>,
		TStringState extends StringFunctionBuildState<?, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>
		>
		extends GenericFunctionBuildStateImpl<TBuildResult, Boolean, TStateSwitcher, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState>
		implements BooleanFunctionBuildState<TBuildResult, TBooleanState, TDateState, TIntegerState, TLongState, TDoubleState, TStringState> {

	@Override
	public TBooleanState not() {
		return toBoolean(new Function<Boolean, Boolean>() {
			@Override
			public Boolean apply(Boolean input) {
				return input != null ? !input : null;
			}
		});
	}
	
	@Override
	public TBuildResult withDefault(final Boolean defaultValue) {
		return toBoolean(Functions2.defaultValue(defaultValue)).build();
	}

}
