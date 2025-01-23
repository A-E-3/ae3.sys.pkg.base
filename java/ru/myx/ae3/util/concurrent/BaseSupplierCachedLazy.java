package ru.myx.ae3.util.concurrent;

import ru.myx.ae3.Engine;
import ru.myx.ae3.act.Act;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecCallableBoth;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.reflect.ReflectionHidden;
import ru.myx.ae3.util.base.BaseSupplier;

/** Provides an object that makes getter or properties for a value that is lazily initialized and
 * periodically checked asynchronously on demand.
 *
 * @author myx
 * @param <T> */
public class BaseSupplierCachedLazy<T extends BaseObject> extends BaseFunctionAbstract implements BaseSupplier<T>, ExecCallableBoth.NativeJ0 {

	/** Same as constructor
	 *
	 * @return */
	public static <T extends BaseObject> BaseSupplierCachedLazy<T> createInstance() {

		return new BaseSupplierCachedLazy<>();
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

	/** null value is initial and when error only! */
	private T lastData = null;

	/** null value is initial only! */
	private RuntimeException lastError = null;

	private long lastDate = Long.MIN_VALUE;

	/** other threads supposed to wait */
	private boolean stateWait = false;

	private boolean stateTask = false;

	/** context for running call-backs */
	protected final ExecProcess ctx;

	/**
	 */
	@ReflectionHidden
	public BaseSupplierCachedLazy() {

		this.ctx = Exec.createProcess(Exec.currentProcess(), this.getClass().getSimpleName());
	}

	/** @param ctx */
	public BaseSupplierCachedLazy(final ExecProcess ctx) {

		this.ctx = Exec.createProcess(ctx, this.getClass().getSimpleName());
	}

	@Override
	public BaseObject callNJ0(final BaseObject instance) {

		return this.get();
	}

	/** Current settings object
	 *
	 * @return */
	@Override
	public T get() {

		final long date = Engine.fastTime();
		final long timeoutRefresh = this.cacheTimeoutRefresh;
		T currentData = this.lastData;

		checks : {
			final boolean doWait;
			rebuild : {
				
				/** initial state, or last result needs a refresh ? */
				if (this.lastDate + timeoutRefresh < date) {
					synchronized (this) {
						/** some other thread started rebuild already ? */
						if (this.stateWait) {
							for (;;) {
								try {
									this.wait();
								} catch (final InterruptedException e) {
									throw new RuntimeException(e);
								}
								if (!this.stateWait) {
									/** got result, some other thread made it */
									currentData = this.lastData;
									break checks;
								}
							}
						}

						if (this.lastDate + timeoutRefresh >= date) {
							break checks;
						}

						/** task is already started, no need to wait either */
						if (this.stateTask) {
							break checks;
						}
						/** expired */
						if (this.cacheTimeoutExpire != -1L && this.lastDate + this.cacheTimeoutExpire < date) {
							/** all requests must wait */
							doWait = this.stateWait = true;
						} else {
							/** all requests will continue as normal */
							this.lastDate = Engine.fastTime();
							doWait = false;
						}

						/** setting flag, the task will be started after sync block */
						this.stateTask = true;
					}
					break rebuild;
				}

				/** seems there is nothing pending */
				break checks;
			}

			/** REBUILD */

			/** start task */
			Act.launch(this.ctx, new Runnable() {

				@Override
				public void run() {

					try {
						BaseSupplierCachedLazy.this.checkReRead();
					} catch (final Throwable e) {
						BaseSupplierCachedLazy.this.checkFailed(e);
					}
				}
			});

			if (doWait) {
				synchronized (this) {
					if (this.stateWait) {
						wait : for (;;) {
							try {
								this.wait();
							} catch (final InterruptedException e) {
								throw new RuntimeException(e);
							}
							if (!this.stateWait) {
								break wait;
							}
						}
					}
					/** got result, some other thread made it */
					currentData = this.lastData;
					break checks;
				}
			}
		}

		if (this.lastError != null) {
			throw this.lastError;
		}

		/** return currently active map */
		return currentData;
	}

	/** @param mapper
	 *            function(currentData, previousResult)
	 * @return */
	public BaseSupplierCachedLazy<T> setChangeCallback(final BaseFunction mapper) {

		this.functionChangeCallback = mapper;
		return this;
	}

	/** @param millis
	 *            use -1 for contents never to expire
	 * @return */
	public BaseSupplierCachedLazy<T> setExpireMillis(final long millis) {

		this.cacheTimeoutExpire = millis;
		return this;
	}

	/** @param millis
	 *            use -1 to refresh continuously
	 * @return */
	public BaseSupplierCachedLazy<T> setRefreshMillis(final long millis) {

		this.cacheTimeoutRefresh = millis;
		return this;
	}

	/** @param function
	 *            function(currentData, previousResult)
	 * @return */
	public BaseSupplierCachedLazy<T> setValueSource(final BaseFunction function) {

		this.functionValueSource = function;
		return this;
	}

	void checkFailed(final Throwable e) {

		synchronized (this) {
			if (this.stateTask) {
				this.lastError = e instanceof final RuntimeException runtimeException
					? runtimeException
					: new RuntimeException(e);
				this.lastData = null;
				this.lastDate = Engine.fastTime();
				this.stateTask = false;
			}
			if (this.stateWait) {
				this.stateWait = false;
				this.notifyAll();
			}
		}
	}

	void checkReRead() {

		final BaseObject initialData = this.lastData == null
			? BaseObject.UNDEFINED
			: this.lastData;

		@SuppressWarnings("unchecked")
		final BaseObject replacementData = this.lastData = (T) this.functionValueSource.callNE1(//
				this.ctx, //
				this, //
				initialData//
		);

		this.lastError = null;
		this.lastDate = Engine.fastTime();

		synchronized (this) {
			if (this.stateWait) {
				this.stateWait = false;
				this.notifyAll();
			}
			this.stateTask = false;
		}

		if (this.functionChangeCallback != null && initialData != replacementData) {
			this.functionChangeCallback.callVE1(//
					this.ctx,
					this,
					replacementData//
			);
		}
	}
}
