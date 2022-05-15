package ru.myx.ae3.util.fn;

import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.ExecCallableBoth;
import ru.myx.ae3.util.base.BaseSupplier;

/** @author myx */
public interface SupplierAbstractFunctionInterface extends BaseFunction, BaseSupplier<BaseObject>, ExecCallableBoth.NativeJ0 {
	
	@Override
	default public BaseObject callNJ0(final BaseObject instance) {
		
		return this.get();
	}
}
