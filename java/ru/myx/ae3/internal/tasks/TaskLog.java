package ru.myx.ae3.internal.tasks;

import ru.myx.ae3.base.BaseHost;
import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.base.BaseObjectNoOwnProperties;
import ru.myx.ae3.base.BaseString;
import ru.myx.ae3.reflect.Reflect;
import ru.myx.ae3.reflect.ReflectionHidden;
import ru.myx.ae3.reflect.ReflectionThisArgument;

/** @author myx */
public interface TaskLog extends BaseHost, BaseObjectNoOwnProperties {
	
	/**
	 *
	 */
	static final BaseObject PROTOTYPE = Reflect.classToBasePrototype(TaskLog.class);
	
	/** @param instance
	 * @return */
	@ReflectionThisArgument
	static TaskLog checkCreate(final BaseObject instance) {
		
		{
			final BaseObject taskLog = instance.baseGet("taskLog", BaseObject.NULL);
			if (BaseObject.NULL != taskLog && taskLog instanceof TaskLog) {
				return (TaskLog) taskLog;
			}
		}
		
		{
			final String loggingDetail = instance.baseGet("log", BaseString.EMPTY).baseToJavaString();
			final TaskLog byDetail = TaskLog.checkCreateByLoggingDetail(instance, loggingDetail);
			if (byDetail != null) {
				instance.baseDefine("taskLog", byDetail);
				return byDetail;
			}
		}
		
		final BaseObject parent = instance.baseGet("parent", BaseObject.UNDEFINED);
		if (BaseObject.UNDEFINED == parent) {
			final TaskLog normal = new TaskLogSelfNorm(instance);
			instance.baseDefine("taskLog", normal);
			return normal;
		}
		
		{
			final String loggingDetail = parent.baseGet("childrenLog", BaseString.EMPTY).baseToJavaString();
			final TaskLog byDetail = TaskLog.checkCreateByLoggingDetail(instance, loggingDetail);
			if (byDetail != null) {
				instance.baseDefine("taskLog", byDetail);
				return byDetail;
			}
		}
		
		{
			final BaseObject parentLogger = parent.baseGet("taskLogger", BaseObject.UNDEFINED);
			if (parentLogger instanceof TaskLog) {
				instance.baseDefine("taskLog", parentLogger);
				return (TaskLog) parentLogger;
			}
		}

		{
			final TaskLog normal = new TaskLogSelfNorm(instance);
			instance.baseDefine("taskLog", normal);
			return normal;
		}
	}
	
	/** @param instance
	 * @param loggingDetail
	 * @return */
	@ReflectionHidden
	static TaskLog checkCreateByLoggingDetail(final BaseObject instance, final String loggingDetail) {
		
		if ("normal".equals(loggingDetail)) {
			return new TaskLogSelfNorm(instance);
		}
		if ("none".equals(loggingDetail)) {
			return new TaskLogSelfNone(instance);
		}
		if ("less".equals(loggingDetail)) {
			return new TaskLogSelfLess(instance);
		}
		if ("more".equals(loggingDetail)) {
			return new TaskLogSelfMore(instance);
		}
		if ("full".equals(loggingDetail)) {
			return new TaskLogSelfFull(instance);
		}
		return null;
	}
	
	@Override
	default BaseObject basePrototype() {
		
		return TaskLog.PROTOTYPE;
	}
	
	/** number of warnings logged
	 *
	 * @return */
	int getAlertCount();
	
	/** number of errors logged
	 *
	 * @return */
	int getErrorCount();
	
	/** @return event list */
	BaseList<?> getList();
	
	/** 'warning', hl="true"
	 *
	 * @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	void logAlert(BaseObject origin, BaseObject type, BaseObject variant, BaseObject peer, BaseObject detail);
	
	/** 'debug', hl="disabled"
	 *
	 * @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	void logDebug(BaseObject origin, BaseObject type, BaseObject variant, BaseObject peer, BaseObject detail);
	
	/** 'detail', hl="local"
	 *
	 * @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	void logDetail(BaseObject origin, BaseObject type, BaseObject variant, BaseObject peer, BaseObject detail);
	
	/** 'error', hl="error"
	 *
	 * @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	void logError(BaseObject origin, BaseObject type, BaseObject variant, BaseObject peer, BaseObject detail);
	
	/** @param origin
	 * @param type
	 * @param variant
	 * @param peer
	 * @param detail
	 */
	void logEvent(BaseObject origin, BaseObject type, BaseObject variant, BaseObject peer, BaseObject detail);
}
