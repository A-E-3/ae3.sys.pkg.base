package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.AbstractSAPI;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveString;

class TaskLogSelfLess extends TaskLogSelfNone {

	private final static BasePrimitiveString STR_STARTED = Base.forString("started");

	protected final BaseList<BaseObject> list = BaseObject.createArray();
	protected final BaseObject task;

	public TaskLogSelfLess(final BaseObject task) {

		super(task);
		this.task = task;
	}

	@Override
	public BaseList<?> getList() {

		return this.list;
	}

	@Override
	public void logAlert(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		++this.alertCount;
		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfLess.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		synchronized (this.list) {
			this.list.baseDefaultPush(event);
		}
		AbstractSAPI.logDebug("TaskLog::Less: " + event);
	}

	@Override
	public void logError(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		++this.errorCount;
		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfLess.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		synchronized (this.list) {
			this.list.baseDefaultPush(event);
		}
		AbstractSAPI.logDebug("TaskLog::Less: " + event);
	}

	@Override
	public void logEvent(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfLess.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		synchronized (this.list) {
			this.list.baseDefaultPush(event);
		}
		AbstractSAPI.logDebug("TaskLog::Less: " + event);
	}

}
