package ru.myx.ae3.internal;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;

import ru.myx.ae3.Engine;

/**
 *
 */
public class HostApiHelper {

	private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
	private static final MemoryMXBean MEMORY_MX_BEAN = ManagementFactory.getMemoryMXBean();
	private static final OperatingSystemMXBean OPERATING_SYSTEM_MX_BEAN = ManagementFactory.getOperatingSystemMXBean();

	/** @return */
	public static double assessHostCpuLoad() {

		final OperatingSystemMXBean osBean = HostApiHelper.OPERATING_SYSTEM_MX_BEAN;
		return Math.min(1.0, 1.0 * osBean.getSystemLoadAverage() / osBean.getAvailableProcessors());
	}
	
	/** @return */
	public static double assessVmMemoryUsage() {

		final MemoryUsage heapMemoryUsage = HostApiHelper.MEMORY_MX_BEAN.getHeapMemoryUsage();
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

		return HostApiHelper.RUNTIME_MX_BEAN.getUptime();
	}
}
