package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.base.BaseHostEmpty;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveString;
import ru.myx.ae3.reflect.Reflect;
import ru.myx.ae3.reflect.ReflectionExplicit;
import ru.myx.ae3.reflect.ReflectionManual;

/** @author myx */
@ReflectionManual
public class TaskEvent extends BaseHostEmpty {

	private static final BaseObject PROTOTYPE = Reflect.classToBasePrototype(TaskEvent.class);

	/**
	 *
	 */
	@ReflectionExplicit
	public final long elapsed;

	/**
	 *
	 */
	@ReflectionExplicit
	public final BasePrimitiveString origin;

	/**
	 *
	 */
	@ReflectionExplicit
	public final BaseObject type;

	/**
	 *
	 */
	@ReflectionExplicit
	public final BaseObject hl;

	/**
	 *
	 */
	@ReflectionExplicit
	public final BaseObject peer;
	private BasePrimitiveString peerCached;

	/**
	 *
	 */
	@ReflectionExplicit
	public final BaseObject detail;
	private BasePrimitiveString detailCached;

	/** @param elapsed
	 * @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	@ReflectionExplicit
	public TaskEvent(final long elapsed, final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		this.elapsed = elapsed;
		this.origin = origin.baseToString();
		this.type = type;
		this.hl = variant;
		this.peer = peer;
		this.detail = detail;
	}

	@Override
	public BaseObject basePrototype() {

		return TaskEvent.PROTOTYPE;
	}

	/** @return */
	@ReflectionExplicit
	public BasePrimitiveString getDetail() {

		return this.detailCached != null
			? this.detailCached
			: (this.detailCached = this.detail.baseToString());
	}

	/** @return */
	@ReflectionExplicit
	public BasePrimitiveString getPeer() {

		return this.peerCached != null
			? this.peerCached
			: (this.peerCached = this.peer.baseToString());
	}

	@Override
	@ReflectionExplicit
	public String toString() {

		return this.origin + " " + this.type + " " + this.hl + " " + this.getPeer() + " " + this.getDetail();
	}
}
