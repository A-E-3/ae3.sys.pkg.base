package ru.myx.ae3.util.base;

import java.util.function.Supplier;

import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitive;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.base.BaseProperty;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.exec.ExecStateCode;
import ru.myx.ae3.exec.ResultHandler;
import ru.myx.ae3.reflect.ReflectionHidden;

/** @author myx
 *
 * @param <T> */
@FunctionalInterface
public interface BaseSupplier<T extends BaseObject> extends Supplier<T>/* , Value<T> */ {

	/** 'get' string */
	final static BasePrimitiveString STR_GET = Base.forString("get");

	/** Returns a function to be used as a property getter. It is bound to this particular instance
	 * of supplier and will return proper settings map regardless of object this getter is applied
	 * on.
	 *
	 * @return */
	default BaseFunction buildGetter() {

		return new BaseFunctionSupplierGetter(this);
	}

	/** returns 'this.get()' by default. */
	/** <code>
	&#64;Override
	default T baseValue() throws WaitTimeoutException {

		return this.get();
	}
	</code> */

	/** TODO: BaseProperty & BaseObject at the same time maybe?
	 *
	 * @return */
	default BaseObject buildProperty() {

		return BaseObject.createObject()//
				.putAppend(BaseSupplier.STR_GET, this.buildGetter())//
		;
	}

	/** TODO: BaseProperty & BaseObject at the same time maybe?
	 *
	 * @param propertyPrototype
	 * @return */
	default BaseObject buildProperty(final BaseObject propertyPrototype) {

		return BaseObject.createObject(propertyPrototype)//
				.putAppend(BaseSupplier.STR_GET, this.buildGetter())//
		;
	}

	/** @param propertyAttributes
	 * @return */
	@ReflectionHidden
	default BaseProperty buildProperty(final short propertyAttributes) {

		return new BaseProperty() {

			@Override
			public short propertyAttributes(final CharSequence name) {

				return propertyAttributes;
			}

			@Override
			public BaseObject propertyGet(final BaseObject instance, final BasePrimitiveString name) {

				return BaseSupplier.this.get();
			}

			@Override
			public BaseObject propertyGet(final BaseObject instance, final String name) {

				return BaseSupplier.this.get();
			}

			@Override
			public BaseObject propertyGetAndSet(final BaseObject instance, final String name, final BaseObject value) {

				return BaseSupplier.this.get();
			}

			@Override
			public ExecStateCode propertyGetCtxResult(final ExecProcess ctx, final BaseObject instance, final BasePrimitive<?> name, final ResultHandler store) {

				return store.execReturn(ctx, BaseSupplier.this.get());
			}

		};
	}

	@Override
	T get();
}
