package ru.myx.ae3.util.queues;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import ru.myx.ae3.base.BaseObject;

/** @author myx */
public class BaseMapQueueEx implements MapQueue {

	private final int capacity;
	private final Map<BaseObject, BaseObject> map;

	BaseMapQueueEx(final int capacity) {
		this.capacity = capacity;
		this.map = new ConcurrentHashMap<>(capacity);
	}
	
	@Override
	public int getCapacity() {
		
		return this.capacity;
	}

	@Override
	public int getLength() {
		
		return this.map.size();
	}

	public BaseObject putCheckEnqueue(final BaseObject key, final BaseObject value) {
		
		final int racyLength = this.map.size();
		if (racyLength >= this.capacity) {
			return BaseObject.UNDEFINED;
		}
		return this.map.computeIfAbsent(key, this.mapper);
	}

	public BaseObject removeIfQueued(final BaseObject key) {
		
		return this.map.remove(key);
	}
}
