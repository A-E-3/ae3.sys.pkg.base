package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.AbstractSAPI;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveString;

class TaskLogSelfMore extends TaskLogSelfNorm {

	private final static BasePrimitiveString STR_STARTED = Base.forString("started");

	public TaskLogSelfMore(final BaseObject task) {

		super(task);
	}

	@Override
	public void logDebug(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfMore.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		/** only to console: */
		// this.list.baseDefaultPush(event);
		AbstractSAPI.logDebug("TaskLog::More: " + event);
	}

	@Override
	public void logDetail(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfMore.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		synchronized (this.list) {
			this.list.baseDefaultPush(event);
		}
		AbstractSAPI.logDebug("TaskLog::More: " + event);
	}

}
