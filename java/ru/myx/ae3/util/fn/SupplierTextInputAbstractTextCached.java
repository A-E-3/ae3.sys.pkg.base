package ru.myx.ae3.util.fn;

import ru.myx.ae3.Engine;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;

/** @author myx */
public abstract class SupplierTextInputAbstractTextCached extends BaseFunctionAbstract implements SupplierAbstractFunctionInterface {

	/**
	 *
	 */
	protected long lastDate = 0L;

	/**
	 *
	 */
	protected CharSequence lastSource = null;

	/**
	 *
	 */
	protected BaseFunction resultMapper = null;

	/**
	 *
	 */
	protected BaseObject lastResult = null;

	@Override
	public final BaseObject get() {

		BaseObject currentData = this.lastResult;

		final BaseObject previousData = currentData == null
			? BaseObject.UNDEFINED
			: currentData;

		checkReady : {
			final CharSequence source;
			checkRebuild : {
				if (currentData != null && this.lastDate + 2500L >= Engine.fastTime()) {
					break checkReady;
				}

				source = this.loadSource();

				/** No settings */
				if (source == null || source.length() == 0) {
					/** No change? */
					if (this.lastSource == null || this.lastSource.length() == 0) {
						if (currentData == null) {
							currentData = this.parseText(null);
							break checkRebuild;
						}
						break checkReady;
					}

					/** Settings Disappear */
					{
						currentData = this.parseText(null);
						break checkRebuild;
					}
				}

				/** Have settings, they've changed */
				if (this.lastSource == null || !this.lastSource.equals(source)) {
					currentData = this.parseText(source);
					break checkRebuild;
				}

				/** Still have settings, but they are the same */
				{
					break checkReady;
				}
			}

			if (this.resultMapper != null) {
				currentData = this.resultMapper.callNJ2(//
						currentData, //
						currentData, //
						previousData //
				);
			}

			this.lastDate = Engine.fastTime();
			this.lastSource = source;
			this.lastResult = currentData;

		}

		return currentData;
	}

	/** @return */
	protected abstract CharSequence loadSource();

	/** @param source
	 *            (NULL when empty/default)
	 * @return */
	protected abstract BaseObject parseText(CharSequence source);

	/** @param function
	 * @return */
	public SupplierTextInputAbstractTextCached setMapper(final BaseFunction function) {

		this.resultMapper = function;
		return this;
	}
}
