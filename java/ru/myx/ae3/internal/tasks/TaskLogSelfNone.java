package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseObject;

class TaskLogSelfNone implements TaskLog {

	protected int alertCount = 0;
	protected int errorCount = 0;

	/** @param task */
	public TaskLogSelfNone(final BaseObject task) {

		//
	}
	
	@Override
	public int getAlertCount() {

		return this.alertCount;
	}
	
	@Override
	public int getErrorCount() {

		return this.errorCount;
	}
	
	@Override
	public BaseList<?> getList() {

		return BaseObject.createArray();
	}
	
	@Override
	public void logAlert(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		++this.alertCount;
		
		/** none of these */
		// final TaskEvent event = new TaskEvent(origin, type, variant, peer, detail);
		// this.list.baseDefaultPush(event);
		// AbstractSAPI.logDebug("TaskLog::None: " + event);
	}
	
	@Override
	public void logDebug(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		/** none of these */
		// final TaskEvent event = new TaskEvent(origin, type, variant, peer, detail);
		// this.list.baseDefaultPush(event);
		// AbstractSAPI.logDebug("TaskLog::None: " + event);
	}
	
	@Override
	public void logDetail(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		/** none of these */
		// final TaskEvent event = new TaskEvent(origin, type, variant, peer, detail);
		// this.list.baseDefaultPush(event);
		// AbstractSAPI.logDebug("TaskLog::None: " + event);
	}
	
	@Override
	public void logError(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		++this.errorCount;

		/** none of these */
		// final TaskEvent event = new TaskEvent(origin, type, variant, peer, detail);
		// this.list.baseDefaultPush(event);
		// AbstractSAPI.logDebug("TaskLog::None: " + event);
	}
	
	@Override
	public void logEvent(final BaseObject origin, final BaseObject type, final BaseObject variant, final BaseObject peer, final BaseObject detail) {

		/** none of these */
		// final TaskEvent event = new TaskEvent(origin, type, variant, peer, detail);
		// this.list.baseDefaultPush(event);
		// AbstractSAPI.logDebug("TaskLog::None: " + event);
	}
	
}
