package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.AbstractSAPI;
import ru.myx.ae3.base.Base;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BasePrimitiveString;

class TaskLogSelfFull extends TaskLogSelfMore {
	
	private final static BasePrimitiveString STR_STARTED = Base.forString("started");
	
	public TaskLogSelfFull(final BaseObject task) {
		
		super(task);
	}
	
	@Override
	public void logDebug(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {
		
		final TaskEvent event = new TaskEvent(
				System.currentTimeMillis() - this.task.baseGet(TaskLogSelfFull.STR_STARTED, BaseObject.UNDEFINED).baseToJavaLong(),
				origin,
				type,
				variant,
				peer,
				detail);
		synchronized (this.list) {
			this.list.baseDefaultPush(event);
		}
		AbstractSAPI.logDebug("TaskLog::Full: " + event);
	}
	
}
