package ru.myx.ae3.util.base;

import java.util.function.Supplier;

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
public final class BaseFunctionSupplierGetter extends BaseFunctionAbstract implements ExecCallableJava.NativeJ0, ExecCallable.ForStore.UseStore0 {
	
	private final Supplier<? extends BaseObject> supplier;

	/** @param supplier */
	public BaseFunctionSupplierGetter(final Supplier<? extends BaseObject> supplier) {
		this.supplier = supplier;
	}

	@Override
	public BaseObject callNJ0(final BaseObject instance) {
		
		return this.supplier.get();
	}

	@Override
	public ExecStateCode execCallPrepare(final ExecProcess ctx, final BaseObject instance, final ResultHandler store, final boolean inline) {
		
		return store.execReturn(ctx, this.supplier.get());
	}

}
