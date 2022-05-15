package ru.myx.ae3.util.concurrent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecCallableBoth;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.util.base.BaseMapper;

/** @author myx
 *
 * @param <T>
 * @param <R> */
public class BaseMapperCachedLazy<T extends BaseObject, R extends BaseObject> extends BaseFunctionAbstract implements BaseMapper<T, R>, ExecCallableBoth.NativeJ1 {
	
	static final class EntryComputerBindSupplier<T extends BaseObject, R extends BaseObject> implements Function<T, Supplier<R>> {
		
		private final BaseMapperCachedLazy<T, R> cache;
		EntryComputerBindSupplier(final BaseMapperCachedLazy<T, R> cache) {
			this.cache = cache;
		}
		
		@Override
		public Supplier<R> apply(final T k) {
			
			final BaseMapperCachedLazy<T, R> cache = this.cache;
			return new BaseSupplierCachedLazy<R>(cache.ctx)//
					.setValueSource(cache.functionValueSource.baseBind(cache, k))//
					.setChangeCallback(cache.functionChangeCallback)//
					.setExpireMillis(cache.cacheTimeoutExpire)//
					.setRefreshMillis(cache.cacheTimeoutRefresh);
		}
	}
	
	static final class EntryComputerMapSupplier<T extends BaseObject, R extends BaseObject> implements Function<T, Supplier<R>> {
		
		private final BaseMapperCachedLazy<T, R> cache;
		EntryComputerMapSupplier(final BaseMapperCachedLazy<T, R> cache) {
			this.cache = cache;
		}
		
		@Override
		public Supplier<R> apply(final T k) {
			
			final BaseMapperCachedLazy<T, R> cache = this.cache;
			return new BaseSupplierCachedLazy<R>(cache.ctx)//
					.setValueSource(cache.functionValueSource.baseBind(cache, k))//
					.setChangeCallback(cache.functionChangeCallback)//
					.setExpireMillis(cache.cacheTimeoutExpire)//
					.setRefreshMillis(cache.cacheTimeoutRefresh);
		}
	}
	
	/** Same as constructor
	 *
	 * @return */
	public static <T extends BaseObject, R extends BaseObject> BaseMapperCachedLazy<T, R> createInstance() {
		
		return new BaseMapperCachedLazy<>();
	}
	
	/**
	 *
	 */
	protected long cacheTimeoutRefresh = 5000;
	
	/**
	 *
	 */
	protected long cacheTimeoutExpire = 30000;
	
	/** Called to produce a value once with all other threads waiting. */
	protected BaseFunction functionValueSource = null;
	
	/** Called after the value is ready and left synchronized context. */
	protected BaseFunction functionChangeCallback = null;
	
	/** context for running call-backs */
	protected final ExecProcess ctx;
	
	private final Map<T, Supplier<R>> cache;
	
	private EntryComputerBindSupplier<T, R> computer;
	
	/**
	 */
	public BaseMapperCachedLazy() {
		this.ctx = Exec.createProcess(Exec.currentProcess(), this.getClass().getSimpleName());
		this.cache = new ConcurrentHashMap<>();
	}
	
	@Override
	public BaseObject callNJ1(final BaseObject instance, final BaseObject argument) {
		
		@SuppressWarnings("unchecked")
		final T argumentConverted = argument == BaseObject.UNDEFINED
			? null
			: (T) argument;
		return this.map(argumentConverted);
	}
	
	@Override
	public R map(final T t) {
		
		return this.cache.computeIfAbsent(t, this.computer).get();
		/** <code>
		{
			final Supplier<R> supplier = this.cache.get(t);
			if (supplier != null) {
				final R result = supplier.get();
				return result;
			}
		}
		{
				final Supplier<R> supplier = new BaseSupplierCachedLazy<R>()//
						.setValueSource(this.functionValueSource.baseBind(this, t))//
						.setChangeCallback(this.functionChangeCallback)//
						.setExpireMillis(this.cacheTimeoutExpire)//
						.setRefreshMillis(this.cacheTimeoutRefresh);
				final Supplier<R> existing = this.cache.putIfAbsent(t, supplier);
				return existing == null
					? supplier.get()
					: existing.get();
		}
		</code> */
	}
	
	/** @param mapper
	 *            function(currentData, previousResult)
	 * @return */
	public BaseMapperCachedLazy<T, R> setChangeCallback(final BaseFunction mapper) {
		
		this.functionChangeCallback = mapper;
		return this;
	}
	
	/** @param millis
	 *            use -1 for contents never to expire
	 * @return */
	public BaseMapperCachedLazy<T, R> setExpireMillis(final long millis) {
		
		this.cacheTimeoutExpire = millis;
		return this;
	}
	
	/** @param millis
	 *            use -1 to refresh continuously
	 * @return */
	public BaseMapperCachedLazy<T, R> setRefreshMillis(final long millis) {
		
		this.cacheTimeoutRefresh = millis;
		return this;
	}
	
	/** @param function
	 *            function(currentData, previousResult)
	 * @return */
	public BaseMapperCachedLazy<T, R> setValueSourceFixed(final BaseFunction function) {
		
		this.functionValueSource = function;
		this.computer = new EntryComputerBindSupplier<>(this);
		return this;
	}
	
}
