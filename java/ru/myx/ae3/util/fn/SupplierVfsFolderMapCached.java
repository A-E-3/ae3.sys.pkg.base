package ru.myx.ae3.util.fn;

import java.util.Map;
import java.util.TreeMap;

import ru.myx.ae3.base.BaseList;
import ru.myx.ae3.base.BaseMapEditable;
import ru.myx.ae3.base.BaseObject;
import ru.myx.ae3.vfs.Entry;
import ru.myx.ae3.vfs.TreeReadType;

/** @author myx */
public abstract class SupplierVfsFolderMapCached extends SupplierMapAbstractCached {
	
	/**
	 *
	 */
	protected final Entry folder;
	
	String previousCheck = null;
	
	/** @param folder */
	public SupplierVfsFolderMapCached(final Entry folder) {
		
		this.folder = folder;
	}
	
	@Override
	public BaseObject checkReload(final BaseObject unchanged) {
		
		final Entry folder = this.folder;
		if (!folder.isContainer()) {
			return BaseObject.createObject();
		}
		
		final BaseMapEditable result = BaseObject.createObject();
		{
			Map<String, Entry> folders = null;
			Map<String, Entry> files = null;
			
			final StringBuilder check = new StringBuilder();
			final BaseList<Entry> contents = folder.toContainer().getContentCollection(TreeReadType.ITERABLE).baseValue();
			for (final Entry entry : contents) {
				if (entry.getKey().startsWith(".")) {
					continue;
				}
				if (entry.isContainer()) {
					final String accepted = this.runFolderFilter(entry);
					if (accepted != null) {
						if (folders == null) {
							folders = new TreeMap<>();
						}
						if (folders.putIfAbsent(accepted, entry) == null) {
							check.append("dir:").append(entry.getLastModified()).append('\n');
						}
					}
					continue;
				}
				{
					final String accepted = this.runDescriptorFilter(entry.getKey());
					if (accepted != null) {
						if (files == null) {
							files = new TreeMap<>();
						}
						if (files.putIfAbsent(accepted, entry) == null) {
							check.append(entry.toBinary().getBinaryContentLength()).append(":").append(entry.getLastModified()).append('\n');
						}
					}
					continue;
				}
			}
			
			if (unchanged != null && this.previousCheck != null && this.previousCheck.contentEquals(check)) {
				return unchanged;
			}
			
			if (folders != null) {
				for (final Map.Entry<String, Entry> entry : folders.entrySet()) {
					this.runFolderMapper(entry.getKey(), entry.getValue());
				}
			}
			
			if (files != null) {
				for (final Map.Entry<String, Entry> entry : files.entrySet()) {
					final String name = entry.getKey();
					final BaseObject descriptor = this.runDescriptorMapper(entry.getValue(), name);
					if (descriptor == null || descriptor == BaseObject.UNDEFINED || descriptor == BaseObject.NULL) {
						continue;
					}

					final BaseObject reduceResult = this.runDescriptorReducer(result, descriptor, name);
					if (reduceResult != result) {
						throw new IllegalStateException("Map is replaced by reducer callback, this: " + this);
					}
				}
			}
			
			this.previousCheck = check.toString();
		}
		
		return result;
	}
	
	/** @param name
	 * @return NULL when not accepted or key. */
	protected abstract String runDescriptorFilter(final String name);
	
	/** @param key
	 * @param entry
	 * @return NULL when not accepted */
	protected abstract BaseObject runDescriptorMapper(final Entry entry, final String key);
	
	/** @param result
	 * @param descriptior
	 * @param name
	 * @return must return same result object */
	protected abstract BaseObject runDescriptorReducer(final BaseMapEditable result, final BaseObject descriptior, final String name);
	
	/** @param entry
	 * @return NULL when not accepted */
	protected abstract String runFolderFilter(final Entry entry);

	/** @param key
	 * @param entry
	 * @return NULL when not accepted */
	protected abstract BaseObject runFolderMapper(final String key, final Entry entry);
}
