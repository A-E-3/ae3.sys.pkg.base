package ru.myx.ae3.util.base;

import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.util.concurrent.BaseMapperCachedLazy;

/** @author myx */
public class MapperBuilder {
	
	/** @return */
	public static MapperBuilder builderCachedLazy() {
		
		return new MapperBuilder().setCachedLazy();
	}
	
	private boolean wrapCached = false;
	/**
	 *
	 */
	private long cacheTimeoutRefresh = 5000;
	
	/**
	 *
	 */
	private long cacheTimeoutExpire = 30000;
	
	/** Called to produce a value once with all other threads waiting. */
	private BaseFunction functionValueSource = null;
	
	/** Called after the value is ready and left synchronized context. */
	private BaseFunction functionChangeCallback = null;
	
	/** @return */
	public BaseMapper<?, ?> build() {
		
		if (this.wrapCached) {
			return new BaseMapperCachedLazy<>()//
					.setExpireMillis(this.cacheTimeoutExpire)//
					.setRefreshMillis(this.cacheTimeoutRefresh)//
					.setValueSourceFixed(this.functionValueSource)//
					.setChangeCallback(this.functionChangeCallback)//
			;
		}
		
		{
			throw new IllegalStateException("MapperCachedLazy ONLY (yet?)");
		}
		
	}
	
	/** @return */
	public MapperBuilder setCachedLazy() {
		
		this.wrapCached = true;
		return this;
	}
	/** @param mapper
	 *            function(currentData, previousResult)
	 * @return */
	public MapperBuilder setChangeCallback(final BaseFunction mapper) {
		
		this.functionChangeCallback = mapper;
		return this;
	}
	
	/** @param millis
	 *            use -1 for contents never to expire
	 * @return */
	public MapperBuilder setExpireMillis(final long millis) {
		
		this.cacheTimeoutExpire = millis;
		return this;
	}
	
	/** @param millis
	 *            use -1 to refresh continuously
	 * @return */
	public MapperBuilder setRefreshMillis(final long millis) {
		
		this.cacheTimeoutRefresh = millis;
		return this;
	}
	
	/** @param function
	 *            function(currentData, previousResult)
	 * @return */
	public MapperBuilder setValueSourceFixed(final BaseFunction function) {
		
		this.functionValueSource = function;
		return this;
	}
}
