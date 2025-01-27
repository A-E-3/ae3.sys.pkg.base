package ru.myx.ae3.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;

import ru.myx.ae3.Engine;

/**
 *
 */
public class HostApiHelper {

	/** @return */
	public static double assessHostCpuLoad() {

		final OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
		return Math.min(1.0, 1.0 * osBean.getSystemLoadAverage() / osBean.getAvailableProcessors());
	}
	
	/** @return */
	public static double assessVmMemoryUsage() {

		final MemoryUsage heapMemoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		return Math.min(1.0, 1.0 * heapMemoryUsage.getUsed() / Math.max(heapMemoryUsage.getCommitted(), heapMemoryUsage.getMax()));
	}
	
	/** @return */
	public static double assessVmStorageUsage() {

		return 1 - 1.0 * Engine.PATH_PRIVATE.getUsableSpace() / Engine.PATH_PRIVATE.getTotalSpace();
	}
	
	/** @return */
	public static String getHostName() {

		return Engine.HOST_NAME;
	}

	/** @return */
	public static long getUptime() {

		return ManagementFactory.getRuntimeMXBean().getUptime();
	}
}
