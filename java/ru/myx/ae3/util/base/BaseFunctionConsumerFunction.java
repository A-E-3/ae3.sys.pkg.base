package ru.myx.ae3.util.base;

import java.util.function.Function;

import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.ExecCallable;
import ru.myx.ae3.exec.ExecCallableJava;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.exec.ExecStateCode;
import ru.myx.ae3.exec.ResultHandler;
import ru.myx.ae3.reflect.ReflectionDisable;

/** @author myx */
@ReflectionDisable
public final class BaseFunctionConsumerFunction extends BaseFunctionAbstract implements ExecCallableJava.NativeJ1, ExecCallable.ForStore.UseStore1 {

	private final Function<BaseObject, ? extends BaseObject> mapper;
	
	/** @param mapper */
	public BaseFunctionConsumerFunction(final Function<? extends BaseObject, ? extends BaseObject> mapper) {
		@SuppressWarnings("unchecked")
		final Function<BaseObject, ? extends BaseObject> mapperConverted = (Function<BaseObject, ? extends BaseObject>) mapper;
		this.mapper = mapperConverted;
	}
	
	@Override
	public BaseObject callNJ1(final BaseObject instance, final BaseObject argument) {

		return this.mapper.apply(argument);
	}
	
	@Override
	public ExecStateCode execCallPrepare(final ExecProcess ctx, final BaseObject instance, final ResultHandler store, final boolean inline, final BaseObject argument) {

		return store.execReturn(ctx, this.mapper.apply(argument));
	}
	
}
