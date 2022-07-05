package ru.myx.ae3.util.concurrent;

import ru.myx.ae3.act.Act;
import ru.myx.ae3.base.BaseFunction;
import ru.myx.ae3.base.BaseFunctionAbstract;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.exec.Exec;
import ru.myx.ae3.exec.ExecCallableBoth;
import ru.myx.ae3.exec.ExecProcess;
import ru.myx.ae3.util.base.BaseSupplier;

/** Provides an object that makes getter or properties for a value that is lazily initialized and
 * returned as a constant afterwards.
 *
 * @author myx
 * @param <T> */
public class BaseSupplierOnceLazy<T extends BaseObject> extends BaseFunctionAbstract implements BaseSupplier<T>, ExecCallableBoth.NativeJ0 {

	/** Same as constructor
	 *
	 * @return */
	public static <T extends BaseObject> BaseSupplierOnceLazy<T> createInstance() {

		return new BaseSupplierOnceLazy<>();
	}

	/** Called to produce a value once with all other threads waiting. */
	protected BaseFunction functionValueSource = null;

	/** Called after the value is ready and left synchronized context. */
	protected BaseFunction functionChangeCallback = null;

	/** null value is initial and when error only! */
	private T lastData = null;

	/** null value is initial only! */
	private RuntimeException lastError = null;

	private boolean stateWait = false;

	/** context for running call-backs */
	protected final ExecProcess ctx;

	/**
	 *
	 */
	public BaseSupplierOnceLazy() {
		
		this.ctx = Exec.createProcess(Exec.currentProcess(), this.getClass().getSimpleName());
	}

	@Override
	public BaseObject callNJ0(final BaseObject instance) {

		return this.get();
	}

	void checkFailed(final Throwable e) {

		synchronized (this) {
			this.lastError = e instanceof final RuntimeException runtimeException
				? runtimeException
				: new RuntimeException(e);
			this.stateWait = false;
			this.notifyAll();
		}
	}

	final void checkReRead() {

		final BaseObject initialData = this.lastData != null
			? BaseObject.UNDEFINED
			: this.lastData;

		@SuppressWarnings("unchecked")
		final BaseObject replacementData = this.lastData = (T) this.functionValueSource.callNE1(//
				this.ctx,
				this,
				initialData//
		);
		
		this.lastError = null;

		synchronized (this) {
			this.stateWait = false;
			this.notifyAll();
		}

		if (this.functionChangeCallback != null && initialData != replacementData) {
			this.functionChangeCallback.callVE1(//
					this.ctx,
					this,
					replacementData//
			);
		}
	}

	/** Current settings object
	 *
	 * @return */
	@Override
	public final T get() {

		T currentData = this.lastData;
		if (currentData != null) {
			return currentData;
		}

		check : {

			if (this.lastError != null) {
				throw this.lastError;
			}

			synchronized (this) {
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
							break check;
						}
					}
				}

				currentData = this.lastData;
				/** changed while was waiting for sync? */
				if (currentData != null) {
					break check;
				}
				/** changed while was waiting for sync? */
				if (this.lastError != null) {
					break check;
				}

				/** settings flag, the will be started after sync block */
				this.stateWait = true;
			}

			/** start task */
			Act.launch(this.ctx, new Runnable() {

				@Override
				public void run() {

					try {
						BaseSupplierOnceLazy.this.checkReRead();
					} catch (final Throwable e) {
						BaseSupplierOnceLazy.this.checkFailed(e);
						throw new RuntimeException(e);
					}
				}
			});

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
				break check;
			}
		}

		if (this.lastError != null) {
			throw this.lastError;
		}

		/** return currently active map */
		return currentData;
	}

	/** @param function
	 *            function(currentData, previousResult)
	 * @return */
	public BaseSupplierOnceLazy<T> setChangeCallback(final BaseFunction function) {

		this.functionChangeCallback = function;
		return this;
	}

	/** @param function
	 *            function(currentData, previousResult)
	 * @return */
	public BaseSupplierOnceLazy<T> setValueSource(final BaseFunction function) {

		this.functionValueSource = function;
		return this;
	}
}
