package ru.myx.ae3.util.fn;

import ru.myx.ae3.Engine;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;

/** @author myx */
public abstract class SupplierMapAbstractCached extends BaseFunctionAbstract implements SupplierAbstractFunctionInterface {
	
	private static final BaseObject UNCHANGED = BaseObject.createObject(null);
	
	/**
	 *
	 */
	protected long lastDate = 0L;
	
	/**
	 *
	 */
	protected BaseFunction resultMapper = null;
	
	/**
	 *
	 */
	protected BaseObject lastResult = null;
	
	/** @param unchanged
	 * @return */
	protected abstract BaseObject checkReload(BaseObject unchanged);
	
	@Override
	public final BaseObject get() {
		
		BaseObject currentData = this.lastResult;
		
		final BaseObject previousData = currentData == null
			? BaseObject.UNDEFINED
			: currentData;
		
		checkReady : {
			final BaseObject source;
			checkRebuild : {
				if (currentData != null && this.lastDate + 2500L >= Engine.fastTime()) {
					break checkReady;
				}
				
				source = this.checkReload(SupplierMapAbstractCached.UNCHANGED);
				if (source == SupplierMapAbstractCached.UNCHANGED) {
					if (currentData == null) {
						currentData = BaseObject.createObject();
						break checkRebuild;
					}
					this.lastDate = Engine.fastTime();
					return currentData;
				}
				
				currentData = source == null
					? BaseObject.UNDEFINED
					: source;
				break checkRebuild;
			}
			
			if (this.resultMapper != null) {
				currentData = this.resultMapper.callNJ2(//
						currentData, //
						currentData, //
						previousData //
				);
			}
			
			this.lastResult = currentData;
			this.lastDate = Engine.fastTime();
			
		}
		
		return currentData;
	}
	
	/** Sets result object mapper
	 *
	 * @param function
	 * @return */
	public SupplierMapAbstractCached setResultMapper(final BaseFunction function) {
		
		this.resultMapper = function;
		return this;
	}
}
