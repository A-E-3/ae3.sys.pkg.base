package ru.myx.ae3.util.base;

import java.util.function.Function;

import ru.myx.ae3.base.BaseObject;

/** @author myx
 *
 * @param <T>
 * @param <R> */
public interface BaseMapper<T extends BaseObject, R extends BaseObject> extends Function<T, R> {
	
	@Override
	@Deprecated
	default R apply(final T t) {

		return this.map(t);
	}
	
	/** @param t
	 * @return */
	R map(T t);
}
